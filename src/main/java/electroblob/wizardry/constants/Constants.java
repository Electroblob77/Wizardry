package electroblob.wizardry.constants;

import electroblob.wizardry.WizardryEventHandler;

/** Stores various global constants used in Wizardry. */
public final class Constants {

	/** The amount of mana each magic crystal is worth */
	public static final int MANA_PER_CRYSTAL = 100;
	/** The amount of mana each mana flask can hold */
	public static final int MANA_PER_FLASK = 700;
	/** The maximum number of one type of wand upgrade which can be applied to a wand. */
	public static final int UPGRADE_STACK_LIMIT = 3;
	/** The fraction by which cooldowns are reduced for each level of cooldown upgrade. */
	public static final float COOLDOWN_REDUCTION_PER_LEVEL = 0.15f;
	/** The fraction by which maximum charge is increased for each level of storage upgrade. */
	public static final float STORAGE_INCREASE_PER_LEVEL = 0.15f;
	/** The fraction by which damage is increased for each tier of matching wand.  */
	public static final float DAMAGE_INCREASE_PER_TIER = 0.15f;
	/** The fraction by which costs are reduced for each piece of matching armour. Note that changing this value will not
	 * affect continuous spells, since they are handled differently. */
	public static final float COST_REDUCTION_PER_ARMOUR = 0.2f;
	/** The fraction by which spell duration is increased for each level of duration upgrade. */
	public static final float DURATION_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which spell range is increased for each level of range upgrade. */
	public static final float RANGE_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which spell blast radius is increased for each level of range upgrade. */
	public static final float BLAST_RADIUS_INCREASE_PER_LEVEL = 0.25f;
	/** The fraction by which movement speed is reduced per level of frost effect. */
	public static final double FROST_SLOWNESS_PER_LEVEL = 0.5;
	/** The fraction by which movement speed is reduced per level of decay effect. */
	public static final double DECAY_SLOWNESS_PER_LEVEL = 0.2;
	/** The fraction by which dig speed is reduced per level of frost effect. */
	public static final float FROST_FATIGUE_PER_LEVEL = 0.45f;
	/** The number of ticks between each mana increase for wands with the condenser upgrade. */
	public static final int CONDENSER_TICK_INTERVAL = 50;
	/** The amount of mana given for a kill for each level of siphon upgrade. A random amount from 0 to this number - 1
	 * is also added. See {@link WizardryEventHandler#onLivingDeathEvent} for more details. */
	public static final int SIPHON_MANA_PER_LEVEL = 3;
	/** The number of ticks between the spawning of patches of decay when an entity has the decay effect.
	 * Note that decay won't spawn again if something is already standing in it. */
	public static final int DECAY_SPREAD_INTERVAL = 8;

}
