package vf.terra.calc.view.vc

import utopia.firmament.context.text.StaticTextContext
import utopia.firmament.localization.LocalString.stringToLocal
import utopia.firmament.localization.{DisplayFunction, LocalizedString}
import utopia.firmament.model.enumeration.StackLayout.Leading
import utopia.flow.collection.immutable.Pair
import utopia.paradigm.color.ColorRole
import utopia.paradigm.color.ColorShade.Light
import utopia.paradigm.measurement.DistanceExtensions._
import utopia.paradigm.measurement.{Distance, MetricScale}
import utopia.reach.component.factory.FromContextComponentFactoryFactory.Ccff
import utopia.reach.component.factory.Mixed
import utopia.reach.component.factory.contextual.TextContextualFactory
import utopia.reach.component.hierarchy.ComponentHierarchy
import utopia.reach.component.input.selection.RadioButtonGroup
import utopia.reach.component.label.text.{TextLabel, ViewTextLabel}
import utopia.reach.component.template.{ReachComponentLike, ReachComponentWrapper}
import utopia.reach.container.multi.{SegmentGroup, Stack}
import utopia.reach.container.wrapper.AlignFrame
import utopia.terra.controller.coordinate.GlobeMath
import utopia.terra.controller.coordinate.GlobeMath.HiddenHeightResults
import utopia.terra.model.world.sphere.SpherePoint
import vf.terra.calc.model.enumeration.DistanceUnitType
import vf.terra.calc.model.enumeration.DistanceUnitType.{Imperial, Metric}
import vf.terra.calc.model.enumeration.EntityType.{Observer, Target}
import vf.terra.calc.util.Common._

case class CalculatorViewFactory(hierarchy: ComponentHierarchy, context: StaticTextContext)
	extends TextContextualFactory[CalculatorViewFactory]
{
	// IMPLEMENTED  ----------------------
	
	override def self: CalculatorViewFactory = this
	override def withContext(context: StaticTextContext): CalculatorViewFactory = copy(context = context)
	
	
	// OTHER    ----------------------------
	
	def apply() = new CalculatorView(hierarchy, context)
}

object CalculatorView extends Ccff[StaticTextContext, CalculatorViewFactory]
{
	override def withContext(hierarchy: ComponentHierarchy, context: StaticTextContext): CalculatorViewFactory =
		CalculatorViewFactory(hierarchy, context)
}
/**
 * The main view in the curvature calculator
 *
 * @author Mikko Hilpinen
 * @since 04.01.2025, v0.1
 */
class CalculatorView(hierarchy: ComponentHierarchy, context: StaticTextContext) extends ReachComponentWrapper
{
	// ATTRIBUTES   -----------------------
	
	// A vertical stack with:
	//      1. Unit type selection radio buttons
	//      2. Observer inputs
	//      3. Target inputs
	//      4. Results
	private lazy val view = Stack.withContext(hierarchy, context).build(Mixed) { factories =>
		// 1. Unit type selection: [label, radio buttons]
		val (unitType, unitTypeP) = factories(Stack).related.centeredRow
			.build(Mixed) { factories =>
				val unitTypeLabel = factories(TextLabel)("Unit type:")
				val buttons = factories(RadioButtonGroup).centered
					.row(DistanceUnitType.values.map { t => t -> t.name.capitalize.localized })
				
				Pair(unitTypeLabel, buttons) -> buttons.valuePointer
			}
			.parentAndResult
		
		// 2 & 3 Observer & target inputs
		val observer = factories(EntityInputView)(Observer, unitTypeP)
		val target = factories(EntityInputView)(Target, unitTypeP)
		
		// 4. Results:
		// A vertical left-aligned stack with:
		//      1. Distance to horizon
		//      2. Distance to target
		//      3. Target hidden height
		//      4. Target visible height
		val results = factories(AlignFrame).left.withBackground(ColorRole.Gray, Light).build(Stack) { stackF =>
			stackF.related.leading.build(Stack) { stackF =>
				// Prepares the computation and the pointers
				val resultsP = observer.locationPointer
					.mergeWith(target.locationPointer, target.heightPointer) { (observer, target, targetHeight) =>
						observer.flatMap { observer =>
							target.map { target => GlobeMath.calculateHiddenHeight(observer, target, targetHeight) }
						}
					}
				
				// Prepares factories for constructing the result labels
				val rowF = stackF.related.row
				val segmentGroup = SegmentGroup.rowsWithLayouts(Leading, Leading)
				def resultLine(label: LocalizedString)(extractValue: HiddenHeightResults => Distance) = {
					rowF.buildSegmented(Mixed, segmentGroup) { factories =>
						val titleLabel = factories.next()(TextLabel)(label)
						val valueLabel = factories.next()(ViewTextLabel)(resultsP,
							DisplayFunction.noLocalization[Option[HiddenHeightResults]] {
								case Some(results) => resultToString(extractValue(results), unitTypeP.value)
								case None => "---"
							})
						
						Pair(titleLabel, valueLabel) -> valueLabel
					}
				}
				// Creates the result labels
				val horizonLine = resultLine("Horizon distance:") { _.distanceToHorizon }
				val targetLine = resultLine("Target distance:") { _.distanceToTarget }
				val hiddenLine = resultLine("Hidden:") { _.hiddenLength }
				val visibleLine = resultLine("Visible:") { _.visibleLength }
				val lines = Vector(horizonLine, targetLine, hiddenLine, visibleLine)
				
				// Updates the results when changing unit systems
				unitTypeP.addContinuousListener { _ => lines.foreach { _.result.revalidate() } }
				
				lines.map { _.parent }
			}
		}
		
		Vector(unitType, observer, target, results.parent)
	}
	
	
	// IMPLEMENTED  -----------------------
	
	override protected def wrapped: ReachComponentLike = view
	
	
	// OTHER    --------------------------
	
	private def resultToString(distance: Distance, unitType: DistanceUnitType) = {
		if (distance.isZero)
			"---"
		else
			unitType match {
				case Metric =>
					val meters = distance.toMeters
					val scale = MetricScale.appropriateFor(meters)
					s"${ (meters * scale.modifierFrom(MetricScale.Default) * 100).toInt / 100.0 } ${ scale.prefix }m"
				
				case Imperial =>
					if (distance > 1.miles)
						s"${ (distance.toMiles * 100).toInt / 100.0 } miles"
					else
						s"${ (distance.toFeet * 10).toInt / 10.0 } feet"
			}
	}
}
