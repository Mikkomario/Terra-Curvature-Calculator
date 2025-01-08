package vf.terra.calc.view.app

import utopia.firmament.context.base.StaticBaseContext
import utopia.firmament.context.window.WindowContext
import utopia.firmament.model.Margins
import utopia.firmament.model.enumeration.WindowResizePolicy.UserAndProgram
import utopia.flow.collection.immutable.range.NumericSpan
import utopia.genesis.handling.action.{ActionLoop, ActorHandler}
import utopia.genesis.handling.event.keyboard.KeyboardEvents
import utopia.genesis.text.Font
import utopia.genesis.text.FontStyle.Plain
import utopia.genesis.util.{Fps, Screen}
import utopia.paradigm.color.{ColorScheme, ColorSet}
import utopia.paradigm.generic.ParadigmDataType
import utopia.paradigm.measurement.DistanceExtensions._
import utopia.paradigm.measurement.Ppi
import utopia.reach.container.wrapper.Framing
import utopia.reach.context.{ReachWindowContext, StaticReachContentWindowContext}
import utopia.reach.window.ReachWindow
import vf.terra.calc.view.vc.CalculatorView

/**
 * The main application of this project
 *
 * @author Mikko Hilpinen
 * @since 05.01.2025, v0.1
 */
object TerraCurvatureCalcApp extends App
{
	// SETUP    ----------------------------
	
	System.setProperty("sun.awt.noerasebackground", "true")
	ParadigmDataType.setup()
	
	import vf.terra.calc.util.Common._
	
	
	// ATTRIBUTES   -----------------------
	
	private implicit val ppi: Ppi = Screen.ppi
	private val cm = 1.cm.toPixels.round.toInt
	
	private val actorHandler = ActorHandler()
	
	private val colors = ColorScheme.default ++
		ColorScheme.twoTone(
			ColorSet.fromHexes("#212121", "#484848", "#000000").get,
			ColorSet.fromHexes("#ffab00", "#ffdd4b", "#c67c00").get
		)
	private val font = Font("Arial", (cm * 0.5).round.toInt, Plain)
	private val margins = Margins((cm * 0.25).round.toInt)
	private val baseContext = StaticBaseContext(actorHandler, font, colors, margins)
	private implicit val windowContext: StaticReachContentWindowContext =
		ReachWindowContext(WindowContext(actorHandler, borderless = true), colors.primary.light)
			.withResizeLogic(UserAndProgram)
			.withContentContext(baseContext)
	
	private val actionLoop = new ActionLoop(actorHandler, NumericSpan(5, 60).mapTo(Fps.apply))
	
	private val window = ReachWindow.contentContextual.using(Framing) { (_, framingF) =>
		framingF.build(CalculatorView) { _() }
	}
	
	
	// APP CODE -----------------------
	
	KeyboardEvents.specifyExecutionContext(exc)
	KeyboardEvents.setupKeyDownEvents(actorHandler)
	
	window.setToCloseOnEsc()
	window.setToExitOnClose()
	
	actionLoop.runAsync()
	window.display(centerOnParent = true)
}
