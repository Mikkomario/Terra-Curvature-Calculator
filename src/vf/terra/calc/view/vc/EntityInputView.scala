package vf.terra.calc.view.vc

import utopia.firmament.context.text.StaticTextContext
import utopia.firmament.localization.LocalString._
import utopia.firmament.localization.LocalizedString
import utopia.flow.collection.immutable.Pair
import utopia.flow.util.RangeExtensions.NumToSpan
import utopia.flow.view.immutable.eventful.Fixed
import utopia.flow.view.template.eventful.Changing
import utopia.paradigm.color.ColorRole.Primary
import utopia.paradigm.color.ColorShade.Light
import utopia.paradigm.measurement.Distance
import utopia.reach.component.factory.FromContextComponentFactoryFactory.Ccff
import utopia.reach.component.factory.Mixed
import utopia.reach.component.factory.contextual.TextContextualFactory
import utopia.reach.component.hierarchy.ComponentHierarchy
import utopia.reach.component.input.text.TextField
import utopia.reach.component.label.text.TextLabel
import utopia.reach.component.template.{ReachComponentLike, ReachComponentWrapper}
import utopia.reach.container.multi.Stack
import utopia.reach.container.wrapper.{AlignFrame, Framing}
import utopia.terra.model.angular.LatLong
import utopia.terra.model.world.sphere.SpherePoint
import vf.terra.calc.model.enumeration.EntityType.{Observer, Target}
import vf.terra.calc.model.enumeration.{DistanceUnitType, EntityType}
import vf.terra.calc.util.Common._

case class ContextualEntityInputViewFactory(hierarchy: ComponentHierarchy, context: StaticTextContext)
	extends TextContextualFactory[ContextualEntityInputViewFactory]
{
	// IMPLEMENTED  --------------------------
	
	override def self: ContextualEntityInputViewFactory = this
	
	override def withContext(context: StaticTextContext): ContextualEntityInputViewFactory = copy(context = context)
	
	
	// OTHER    -----------------------------
	
	def apply(mode: EntityType, unitTypePointer: Changing[DistanceUnitType]) =
		new EntityInputView(hierarchy, context, mode, unitTypePointer)
}

object EntityInputView extends Ccff[StaticTextContext, ContextualEntityInputViewFactory]
{
	override def withContext(hierarchy: ComponentHierarchy, context: StaticTextContext): ContextualEntityInputViewFactory =
		ContextualEntityInputViewFactory(hierarchy, context)
}
/**
 * A view for inputting the observer or target location for the calculator
 *
 * @author Mikko Hilpinen
 * @since 04.01.2025, v0.1
 */
class EntityInputView(hierarchy: ComponentHierarchy, context: StaticTextContext, mode: EntityType,
                      unitTypeP: Changing[DistanceUnitType])
	extends ReachComponentWrapper
{
	// ATTRIBUTES   ---------------------
	
	// The main contents are wrapped in a solid background framing
	private lazy val (view, (pointP, heightP)) = Framing(hierarchy).withContext(context).small
		.mapInsets { _.withTop(context.margins.aroundVerySmall) }
		.withBackground(Primary, Light)
		// Vertical stack with: [Title, inputs, height input (if applicable)]
		.build(Stack) { stackF =>
			stackF.related.build(Mixed) { factories =>
				// 1. Header label
				val headerLabel = factories(TextLabel).withTextExpandingToRight(mode.name.capitalize.localized)
				
				// 2. Inputs: [Latitude, longitude, altitude]
				val locationInputs = factories(Stack).mapContext { _.toVariableContext }.row
					.build(TextField) { fieldF =>
						// 2.1. Latitude
						val latitude = fieldF.withFieldName("Latitude (N)").double(
							allowedRange = -90.0 spanTo 90.0, expectedNumberOfDecimals = 12, disableLengthHint = true)
						// 2.2. Longitude
						val longitude = fieldF.withFieldName("Longitude (E)")
							.double(allowedRange = -360.0 spanTo 360.0, expectedNumberOfDecimals = 12,
								disableLengthHint = true)
						// 2.3 Altitude
						val altitudeHint: LocalizedString = mode match {
							case Observer => "Distance above sea level at eye height"
							case Target => "Lowest point, from sea level"
						}
						val altitude = fieldF.withFieldNamePointer(nameWithUnitPointer("Altitude (%s)"))
							.withHint(altitudeHint)
							.double(allowedRange = 0.0 spanTo 99999.9, expectedNumberOfDecimals = 1,
								disableLengthHint = true)
						
						// Combines the latitude and longitude pointers
						val locationP = latitude.valuePointer.mergeWith(longitude.valuePointer) { (lat, lon) =>
							lat.flatMap { lat => lon.map { lon => LatLong.degrees(lat, lon) } }
						}
						val altitudeP = altitude.valuePointer.mergeWith(unitTypeP) { (altitude, unit) =>
							altitude.map { Distance(_, unit.defaultUnit) }
						}
						// Combines the location and altitude pointers
						val spherePointP = locationP.mergeWith(altitudeP) { (location, altitude) =>
							location.flatMap { location => altitude.map { SpherePoint(location, _) } }
						}
						
						Vector(latitude, longitude, altitude) -> spherePointP
					}
				
				// 3. Height field (optional)
				val heightField = {
					if (mode == Target)
						Some(factories(AlignFrame).left.build(TextField) { fieldF =>
							fieldF.withFieldNamePointer(nameWithUnitPointer(s"Target height (%s)"))
								.double(allowedRange = 0.0 spanTo 9999.9, expectedNumberOfDecimals = 1,
									disableLengthHint = true) })
					else
						None
				}
				
				// Forwards the input pointers, also
				(Pair(headerLabel, locationInputs.parent) ++ heightField.map { _.parent },
					(locationInputs.result, heightField.map { _.child.valuePointer }))
			}
		}
		.parentAndResult
	
	/**
	 * @return A pointer that contains the target height input.
	 *         None if these fields are for the observer, not the target.
	 */
	lazy val heightPointer = heightP match {
		case Some(p) => p.mergeWith(unitTypeP) { (h, unitType) => Distance(h.getOrElse(0.0), unitType.defaultUnit) }
		case None => Fixed(Distance.zero)
	}
	
	
	// COMPUTED ---------------------------
	
	/**
	 * @return A pointer that contains the specified location, or None
	 */
	def locationPointer = pointP
	
	
	
	// IMPLEMENTED  -----------------------
	
	override protected def wrapped: ReachComponentLike = view
	
	
	// OTHER    ---------------------------
	
	private def nameWithUnitPointer(name: LocalizedString) =
		unitTypeP.map { u => name.interpolated(u.defaultUnit.abbreviation) }
}
