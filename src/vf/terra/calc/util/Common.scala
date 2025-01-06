package vf.terra.calc.util

import utopia.firmament.localization.{Localizer, NoLocalization}

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
}
