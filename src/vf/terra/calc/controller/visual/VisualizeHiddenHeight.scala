package vf.terra.calc.controller.visual

import utopia.genesis.graphics.{DrawSettings, StrokeSettings}
import utopia.genesis.image.Image
import utopia.paradigm.angular.Angle
import utopia.paradigm.color.Color
import utopia.paradigm.shape.shape2d.area.Circle
import utopia.paradigm.shape.shape2d.area.polygon.c4.bounds.Bounds
import utopia.paradigm.shape.shape2d.line.Line
import utopia.paradigm.shape.shape2d.vector.point.Point
import utopia.paradigm.shape.shape2d.vector.size.Size
import utopia.paradigm.shape.template.vector.DoubleVector
import utopia.terra.controller.coordinate.GlobeMath.HiddenHeightResults
import utopia.terra.controller.coordinate.world.SphericalEarth

/**
 * An interface for producing visualizations of hidden height calculation results
 *
 * @author Mikko Hilpinen
 * @since 07.01.2025, v0.1
 */
object VisualizeHiddenHeight
{
	// ATTRIBUTES   ------------------------
	
	private lazy val lineOfSightDs = StrokeSettings(Color.green.darkenedBy(2))
	private lazy val entityDs = StrokeSettings(Color.black)
	private lazy val earthDs = DrawSettings.onlyFill(Color.blue)
	
	
	// OTHER    ---------------------------
	
	/**
	 * Draws a visualization of a hidden height calculation
	 * @param results Results to visualize
	 * @param optimalImageSize Maximum / optimal image size
	 * @return An image visualizing the specified results
	 */
	def apply(results: HiddenHeightResults, optimalImageSize: Size) = {
		// Rotates the visualization, so that the observer & target are symmetrically placed
		val middlePoint = results.observer.average(results.target.center)
		val rotation = Angle.up - middlePoint.direction
		val rotatedKeyPoints = Vector(results.observer, results.target.center, results.horizon)
			.map { _.rotated(rotation) }
		
		// Determines the level of scaling suitable for the drawing
		val minViewBounds = Bounds.aroundPoints(rotatedKeyPoints)
		val viewBounds = Bounds.centered(minViewBounds.center,
			size = (minViewBounds.size * 1.05).bottomRight(Size.square(minViewBounds.size.maxDimension * 0.5)))
		val scaling = (optimalImageSize / viewBounds.size).minDimension
		
		// Determines the target locations within the image
		def toImageSpace(coordinate: DoubleVector) =
			((coordinate.rotated(rotation) - viewBounds.position) * scaling).toPoint
		
		val imgObserver = toImageSpace(results.observer)
		val imgTarget = results.target.mapEnds(toImageSpace)
		val imgHorizon = toImageSpace(results.horizon)
		val imgEarthCenter = toImageSpace(Point.origin)
		val imgEarth = Circle(imgEarthCenter, SphericalEarth.globeVectorRadius * scaling)
		val lineOfSight = Line(imgObserver, imgHorizon).mapVector { _ * 10 }
		
		println(s"Original observer: ${ results.observer }")
		println(s"Original target: ${ results.target }")
		println(s"Original horizon: ${ results.horizon }")
		println(s"Original observer angle: ${ results.observer.direction }")
		println(s"Original target angle: ${ results.target.center.direction }")
		println(s"Original horizon angle: ${ results.horizon.direction }")
		println(s"Applied rotation: $rotation")
		println(s"Applied scaling: $scaling")
		println(s"View bounds: $viewBounds")
		println(s"Observer: $imgObserver")
		println(s"Target: $imgTarget")
		println(s"Horizon: $imgHorizon")
		println(s"Earth: $imgEarth")
		
		// Draws an image with the following elements:
		//      1. The sea level
		//      2. The observer (point)
		//      3. The target (line)
		//      4. Line of sight
		Image.paint((viewBounds.size * scaling).round) { drawer =>
			drawer.antialiasing.use { drawer =>
				drawer.draw(lineOfSight)(lineOfSightDs)
				drawer.draw(Line(imgEarthCenter, imgObserver))(entityDs)
				drawer.draw(imgEarth)(earthDs)
				drawer.draw(Circle(imgHorizon, 2.0))(lineOfSightDs)
				drawer.draw(Circle(imgObserver, 3.0))(entityDs)
				drawer.draw(imgTarget)(entityDs)
			}
		}
	}
}
