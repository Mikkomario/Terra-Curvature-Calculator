package vf.terra.calc.model.enumeration

/**
 * An enumeration for types of entities in the calculation
 * @author Mikko Hilpinen
 * @since 04.01.2025, v0.1
 */
sealed trait EntityType
{
	/**
	 * @return Name of this entity
	 */
	def name: String
}

object EntityType
{
	/**
	 * The person or object performing the observation / origin of line-of-sight
	 */
	case object Observer extends EntityType
	{
		override val name: String = "observer"
	}
	/**
	 * The viewed object
	 */
	case object Target extends EntityType
	{
		override val name: String = "target"
	}
}
