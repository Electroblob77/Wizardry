package electroblob.wizardry.util;

import electroblob.wizardry.entity.living.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

// A note on the use of the vanilla damagesources:
// When using indirect damage sources, the SECOND argument is the original entity (i.e. the caster), and the
// FIRST argument is the actual projectile or whatever that does the damage. getTrueSource() will return
// the original entity, and getImmediateSource() will return the projectile.

// The vanilla approach to damage types is inconsistent, to say the least. Poison is simply 'magic', and relies on
// EntityLivingBase.isPotionApplicable to determine whether an entity is affected or not. Wither, on the other hand, is
// its own damage type, but again relies on the potion to determine what is immune (which in vanilla is nothing). Fire
// has a proper implementation of course, with both block-based and projectile-based types, and any other damagesource
// can also be designated as fire damage with setFireDamage(). Likewise, projectile and explosion damage can also be
// applied to any damagesource, but these aren't considered types in their own right; rather, they seem to be damage
// type 'attributes'. This is my attempt to collect all of these into a reasonably coherent system.

/**
 * <i>"Ouch, that hurt!"</i>
 * <p></p>
 * As of wizardry 1.1, this class has replaced the damagesource-related methods in WizardryUtilities, allowing a
 * {@link DamageType} to be specified with the damage. The main reason for this is so that damage sources can fit with
 * the vanilla behaviour on armour enchantments and such like whilst still being classified as wizardry damage for the
 * purposes of friendly fire, etc.
 * <p></p>
 * <i> In the future, there is scope for an entirely standalone mod based on this idea. Perhaps 'Elemental Damage' could
 * be a config-only mod which allows its users to define any number of specific damage types, then give any creature a
 * resistance, immunity or vulnerability to each, as well as being able to specify the sources for each type, such as
 * enchantments, potions, specific items, certain entities, even particular situations and string damagesource names
 * from other mods. </i>
 * 
 * @see IndirectMagicDamage
 * @see IElementalDamage
 * @since Wizardry 1.1
 * @author Electroblob
 */
public class MagicDamage extends EntityDamageSource implements IElementalDamage {

	/** The name of the damagesource for direct magic damage from the wizardry mod. */
	public static final String DIRECT_MAGIC_DAMAGE = "wizardry_magic";
	/** The name of the damagesource for indirect magic damage from the wizardry mod. */
	public static final String INDIRECT_MAGIC_DAMAGE = "indirect_wizardry_magic";
	// Technically, I don't need to specify that the classes in this map must extend entity, but it's good practice to.
	private static final Map<Class<? extends Entity>, EnumSet<DamageType>> immunityMapping = new HashMap<>();

	private final DamageType type;
	private final boolean isRetaliatory;

	/**
	 * A simple set of constants for the types of damage. The names are deliberately different to those in EnumElement
	 * to avoid confusion. All types are classified as magic damage in the vanilla system, so witches are resistant to
	 * them.
	 */
	public enum DamageType {
		/** Generic magic damage from wizardry. Like vanilla magic damage, except it doesn't bypass armour. */
		MAGIC,
		/** Fire damage from wizardry. Counts as fire damage in the vanilla system, so is blocked by any mobs
		 * that are immune to fire and entities with the fire resistance effect, and is affected by the fire protection
		 * enchantment. */
		FIRE,
		/** Frost (ice) damage from wizardry. Snow golems, ice wraiths and ice giants are immune. */
		FROST,
		/** Shock (lightning) damage from wizardry. Charges creepers. Lightning wraiths and storm elementals are immune. */
		SHOCK,
		/** Wither damage from wizardry. Withers, wither skeletons and shadow wraiths are immune. */
		WITHER,
		/** Poison damage from wizardry. Spiders, cave spiders and undead mobs are immune. */
		POISON,
		/** Force damage from wizardry. */ // Insubstantial creatures (ghast, shadow wraith, etc.) are immune?
		FORCE,
		/** Blast damage from wizardry. Affected by the blast protection enchantment. */
		BLAST,
		/** Radiant damage from wizardry. Sets fire to undead entities. */
		RADIANT
	}

	static {
		// Of course, the entities that are immune to fire already are since there's a vanilla system for that, but
		// they're included here anyway for completeness and in case anyone wants to check if an entity is immune to
		// an unspecified element for reasons other than dealing damage.
		setEntityImmunities(EntityPhoenix.class, DamageType.FIRE);
		setEntityImmunities(EntityBlaze.class, DamageType.FIRE);
		setEntityImmunities(EntityBlazeMinion.class, DamageType.FIRE);
		setEntityImmunities(EntityPigZombie.class, DamageType.FIRE, DamageType.POISON);
		setEntityImmunities(EntityMagmaCube.class, DamageType.FIRE);
		setEntityImmunities(EntityGhast.class, DamageType.FIRE);
		setEntityImmunities(EntityDragon.class, DamageType.FIRE);
		setEntityImmunities(EntityStormElemental.class, DamageType.FIRE, DamageType.SHOCK);
		setEntityImmunities(EntityWither.class, DamageType.FIRE, DamageType.WITHER);
		setEntityImmunities(EntitySnowman.class, DamageType.FROST);
		setEntityImmunities(EntityPolarBear.class, DamageType.FROST);
		setEntityImmunities(EntityIceWraith.class, DamageType.FROST);
		setEntityImmunities(EntityIceGiant.class, DamageType.FROST);
		setEntityImmunities(EntityLightningWraith.class, DamageType.SHOCK);
		setEntityImmunities(EntityShadowWraith.class, DamageType.WITHER);
		setEntityImmunities(EntityWitherSkeleton.class, DamageType.WITHER);
		setEntityImmunities(EntitySpider.class, DamageType.POISON);
		setEntityImmunities(EntitySpiderMinion.class, DamageType.POISON);
		setEntityImmunities(EntityCaveSpider.class, DamageType.POISON);
		setEntityImmunities(EntityZombie.class, DamageType.POISON);
		setEntityImmunities(EntitySkeleton.class, DamageType.POISON);
		setEntityImmunities(EntityZombieMinion.class, DamageType.POISON);
		setEntityImmunities(EntitySkeletonMinion.class, DamageType.POISON);
	}

	public MagicDamage(String name, Entity caster, DamageType type, boolean isRetaliatory){
		super(name, caster);
		this.type = type;
		this.isRetaliatory = isRetaliatory;
		this.setMagicDamage();
		if(type == DamageType.FIRE) this.setFireDamage();
		if(type == DamageType.BLAST) this.setExplosion();
	}

	/**
	 * Returns true if the given entity is immune to the given damage type according to the entity immunity mappings,
	 * false otherwise. When you want to check for resistances, check this method rather than just checking the result
	 * of attackEntityFrom, since that could return false for all sorts of reasons besides immunities. However, if you
	 * don't need to know whether the damage succeeded or not, there's no point in checking this method. A common use of
	 * this method is to check for immunity and if so display the "[mob] resisted [spell]" chat message. See
	 * {@link electroblob.wizardry.spell.Arc Arc} for a good example of this, and also of when not to use it.
	 */
	public static boolean isEntityImmune(DamageType type, Entity entity){

		if(type == DamageType.FIRE && entity.isImmuneToFire()) return true;
		EnumSet immunities = immunityMapping.get(entity.getClass());

		return immunities != null && immunities.contains(type);
	}

	/** Registers the given type of entity as immune to all of the passed in damage types. */
	public static void setEntityImmunities(Class<? extends Entity> entityType, DamageType... immunities){
		EnumSet<DamageType> immunitySet = EnumSet.noneOf(DamageType.class);
		Collections.addAll(immunitySet, immunities);
		immunityMapping.put(entityType, immunitySet);
	}

	/** Adds the passed in damage type to the list of damage types to which the given entity is immune. */
	public static void addEntityImmunity(Class<? extends Entity> entityType, DamageType immunity){
		EnumSet<DamageType> immunitySet = immunityMapping.get(entityType);
		if (immunitySet == null) immunitySet = EnumSet.of(immunity);
		else immunitySet.add(immunity);

		immunityMapping.put(entityType, immunitySet);
	}

	/**
	 * Returns a DamageSource called "wizardry_magic" with the given entity as the caster. Use in preference to vanilla
	 * types to allow things to distinguish between magic and regular melee/swords. Unlike DamageSource.MAGIC, it does
	 * not bypass armour and has a player as the source (rather than nothing). It is still classed as magic damage (for
	 * the record, all this does in vanilla is make witches 85% resistant to it - but that seems kinda right anyway).
	 * isRetaliatory defaults to false.
	 * <p></p>
	 * Now that this is its own class, this static method is largely redundant, but it's not worth refactoring the
	 * entire mod just to get rid of this method and use the constructor instead.
	 * 
	 * @param caster The player or other living entity causing the damage
	 * @param type The type that this damage belongs to; used for resistances and wand perks. Use
	 *        {@link DamageType#MAGIC} for regular, non-elemental magic damage (sometimes you might not want an element
	 *        even though the spell has one - for example, not all necromancy spells are 'withery', so some of them
	 *        might reasonably affect creatures that are usually immune to wither effects).
	 * @return A damagesource object of type EntityDamageSource
	 */
	public static DamageSource causeDirectMagicDamage(Entity caster, DamageType type){
		return causeDirectMagicDamage(caster, type, false);
	}

	/**
	 * Returns a DamageSource called "wizardry_magic" with the given entity as the caster. Use in preference to vanilla
	 * types to allow things to distinguish between magic and regular melee/swords. Unlike DamageSource.MAGIC, it does
	 * not bypass armour and has a player as the source (rather than nothing). It is still classed as magic damage (for
	 * the record, all this does in vanilla is make witches 85% resistant to it - but that seems kinda right anyway).
	 * <p></p>
	 * Now that this is its own class, this static method is largely redundant, but it's not worth refactoring the
	 * entire mod just to get rid of this method and use the constructor instead.
	 * 
	 * @param caster The player or other living entity causing the damage
	 * @param type The type that this damage belongs to; used for resistances and wand perks. Use
	 *        {@link DamageType#MAGIC} for regular, non-elemental magic damage (sometimes you might not want an element
	 *        even though the spell has one - for example, not all necromancy spells are 'withery', so some of them
	 *        might reasonably affect creatures that are usually immune to wither effects).
	 * @param isRetaliatory whether this damage source came from a retaliatory attack; prevents infinite loops occurring
	 *        when two entities damage each other with retaliatory effects.
	 * @return A damagesource object of type EntityDamageSource
	 */
	public static DamageSource causeDirectMagicDamage(Entity caster, DamageType type, boolean isRetaliatory){
		return new MagicDamage(DIRECT_MAGIC_DAMAGE, caster, type, isRetaliatory);
	}

	/**
	 * Returns a DamageSource called "indirect_wizardry_magic" with the player as the caster. Use in preference to vanilla
	 * types to allow things to distinguish between magic and regular arrows/throwables. Unlike
	 * DamageSource.causeIndirectMagicDamage, it does not bypass armour. It is still classed as magic damage (for the
	 * record, all this does in vanilla is make witches 85% resistant to it - but that seems kinda right anyway).
	 * isRetaliatory defaults to false.
	 * <p></p>
	 * Now that this is its own class, this static method is largely redundant, but it's not worth refactoring the
	 * entire mod just to get rid of this method and use the constructor instead.
	 * 
	 * @param magic The entity that actually caused the damage
	 * @param caster The player or other living entity that cast the spell originally
	 * @param type The type that this damage belongs to; used for resistances and wand perks. Use
	 *        {@link DamageType#MAGIC} for regular, non-elemental magic damage (sometimes you might not want an element
	 *        even though the spell has one - for example, not all necromancy spells are 'withery', so some of them
	 *        might reasonably affect creatures that are usually immune to wither effects).
	 * @return A damagesource object of type EntityDamageSourceIndirect
	 */
	public static DamageSource causeIndirectMagicDamage(Entity magic, Entity caster, DamageType type){
		return causeIndirectMagicDamage(magic, caster, type, false);
	}

	/**
	 * Returns a DamageSource called "indirect_wizardry_magic" with the player as the caster. Use in preference to vanilla
	 * types to allow things to distinguish between magic and regular arrows/throwables. Unlike
	 * DamageSource.causeIndirectMagicDamage, it does not bypass armour. It is still classed as magic damage (for the
	 * record, all this does in vanilla is make witches 85% resistant to it - but that seems kinda right anyway).
	 * <p></p>
	 * Now that this is its own class, this static method is largely redundant, but it's not worth refactoring the
	 * entire mod just to get rid of this method and use the constructor instead.
	 * 
	 * @param magic The entity that actually caused the damage
	 * @param caster The player or other living entity that cast the spell originally
	 * @param type The type that this damage belongs to; used for resistances and wand perks. Use
	 *        {@link DamageType#MAGIC} for regular, non-elemental magic damage (sometimes you might not want an element
	 *        even though the spell has one - for example, not all necromancy spells are 'withery', so some of them
	 *        might reasonably affect creatures that are usually immune to wither effects).
	 * @param isRetaliatory whether this damage source came from a retaliatory attack; prevents infinite loops occurring
	 *        when two entities damage each other with retaliatory effects.
	 * @return A damagesource object of type EntityDamageSourceIndirect
	 */
	public static DamageSource causeIndirectMagicDamage(Entity magic, Entity caster, DamageType type,
			boolean isRetaliatory){
		return new IndirectMagicDamage(INDIRECT_MAGIC_DAMAGE, magic, caster, type, isRetaliatory);
	}

	@Override
	public DamageType getType(){
		return type;
	}

	@Override
	public boolean isRetaliatory(){
		return isRetaliatory;
	}

}
