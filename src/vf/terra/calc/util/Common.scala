package vf.terra.calc.util

import utopia.firmament.localization.{Localizer, NoLocalization}
import utopia.flow.async.context.ThreadPool
import utopia.flow.util.logging.{Logger, SysErrLogger}

import scala.concurrent.ExecutionContext

/**
 * Contains commonly used static values
 *
 * @author Mikko Hilpinen
 * @since 04.01.2025, v0.1
 */
object Common
{
	implicit val languageCode: String = "en"
	implicit val localizer: Localizer = NoLocalization
	
	implicit val log: Logger = SysErrLogger
	implicit val exc: ExecutionContext = new ThreadPool("Terra-Calculator")
}
