package vf.terra.calc.model.enumeration

import utopia.flow.collection.immutable.Pair
import utopia.paradigm.measurement.DistanceUnit
import utopia.paradigm.measurement.DistanceUnit.{Feet, Meter}

/**
 * Represents different styles of recording length / distance values
 *
 * @author Mikko Hilpinen
 * @since 04.01.2025, v0.1
 */
sealed trait DistanceUnitType
{
	/**
	 * @return Name of this unit type
	 */
	def name: String
	/**
	 * @return Default unit in this category
	 */
	def defaultUnit: DistanceUnit
}

object DistanceUnitType
{
	// ATTRIBUTES   -----------------------
	
	/**
	 * All distance unit types
	 */
	lazy val values = Pair[DistanceUnitType](Metric, Imperial)
	
	
	// VALUES   ---------------------------
	
	case object Metric extends DistanceUnitType
	{
		override val name: String = "metric"
		override val defaultUnit: DistanceUnit = Meter
	}
	
	case object Imperial extends DistanceUnitType
	{
		override val name: String = "imperial"
		override val defaultUnit: DistanceUnit = Feet
	}
}
