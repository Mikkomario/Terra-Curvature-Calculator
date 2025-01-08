package vf.terra.calc.test

import utopia.flow.parse.file.FileExtensions._
import utopia.paradigm.generic.ParadigmDataType
import utopia.paradigm.measurement.DistanceExtensions._
import utopia.paradigm.shape.shape2d.vector.size.Size
import utopia.terra.controller.coordinate.GlobeMath
import utopia.terra.model.angular.LatLong
import utopia.terra.model.world.sphere.SpherePoint
import vf.terra.calc.controller.visual.VisualizeHiddenHeight

import java.nio.file.Path

/**
 * A test program generating a visualization of a pre-specified curve measurement case
 *
 * @author Mikko Hilpinen
 * @since 08.01.2025, v0.1
 */
object CurveVisualizationTest extends App
{
	ParadigmDataType.setup()
	
	private val results = GlobeMath.calculateHiddenHeight(
		observer = SpherePoint(LatLong.degrees(59.52777778, 5.90333333), 842.m),
		targetLowestPoint = SpherePoint(LatLong.degrees(60.533680565437685,-1.3907738122234992), 239.m),
		targetHeight = 45.m)
	
	private val img = VisualizeHiddenHeight(results, Size.square(640))
	
	private val imagePath: Path = "data/test-output/visualization.png"
	img.writeToFile(imagePath).get
	imagePath.openInDesktop().get
}
