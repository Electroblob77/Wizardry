package electroblob.wizardry.spell;

import java.util.function.Function;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which conjure constructs (i.e. instances of {@link EntityMagicConstruct}).
 * This allows all the relevant code to be centralised, since these spells all work in a similar way. Usually, a simple
 * instantiation of this class is sufficient to create a construct spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the construct entity class instead whenever possible.
 * <p>
 * This class spawns the construct entity at the caster's feet, like ring of fire and healing aura. Use
 * {@link SpellConstructRanged} (which extends this class) for spells that spawn constructs at an aimed-at position.
 * <p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#doesSpellRequirePacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see SpellConstructRanged
 */
public class SpellConstruct<T extends EntityMagicConstruct> extends Spell {
	
	/** A factory that creates construct entities. */
	protected final Function<World, T> constructFactory;
	/** The base lifetime of the construct created by this spell, or -1 if the construct does not despawn. */
	protected final int baseDuration;
	/** The sound that gets played when this spell is cast, or null if the spell plays no sound. */
	@Nullable
	protected final SoundEvent sound;
	
	/** The volume of the sound played when this spell is cast. Defaults to 1. */
	protected float volume = 1;
	/** The pitch of the sound played when this spell is cast. Defaults to 1. */
	protected float pitch = 1;
	/** The pitch variation of the sound played when this spell is cast. Defaults to 0. */
	protected float pitchVariation = 0;
	/** Whether the construct must be spawned on the ground. Defaults to false. */
	protected boolean requiresFloor = false;
	/** Whether constructs spawned by this spell may overlap. Defaults to false. */
	protected boolean allowOverlap = false;

	public SpellConstruct(String name, Tier tier, Element element, SpellType type, int cost, int cooldown, EnumAction action, Function<World, T> constructFactory, int baseDuration, SoundEvent sound){
		this(Wizardry.MODID, name, tier, element, type, cost, cooldown, action, constructFactory, baseDuration, sound);
	}

	public SpellConstruct(String modID, String name, Tier tier, Element element, SpellType type, int cost, int cooldown, EnumAction action, Function<World, T> constructFactory, int baseDuration, SoundEvent sound){
		super(modID, name, tier, element, type, cost, cooldown, action, false);
		this.constructFactory = constructFactory;
		this.baseDuration = baseDuration;
		this.sound = sound;
	}
	
	/**
	 * Sets the sound parameters for this spell.
	 * @param volume The volume of the sound, relative to 1.
	 * @param pitch The pitch of the sound, relative to 1.
	 * @param pitchVariation The pitch variation. The pitch will be set to a random value within this range either side
	 * of the set pitch value.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellConstruct<T> soundValues(float volume, float pitch, float pitchVariation) {
		this.volume = volume;
		this.pitch = pitch;
		this.pitchVariation = pitchVariation;
		return this;
	}

	@Override public boolean doesSpellRequirePacket(){ return false; }

	@Override public boolean canBeCastByNPCs(){ return true; }
	
	/**
	 * Sets whether the construct must be spawned on the ground.
	 * @param requiresFloor True to require that the construct be spawned on the ground, false to allow it in mid-air.
	 * Defaults to false.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellConstruct<T> floor(boolean requiresFloor){
		this.requiresFloor = requiresFloor;
		return this;
	}
	
	/**
	 * Sets whether constructs spawned by this spell may overlap.
	 * @param allowOverlap True to allow overlapping, false to prevent it. Defaults to false.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellConstruct<T> overlap(boolean allowOverlap){
		this.allowOverlap = allowOverlap;
		return this;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		
		if(caster.onGround || !requiresFloor){
			if(!spawnConstruct(world, caster.posX, caster.posY, caster.posZ, caster, modifiers)) return false;
			if(sound != null) WizardryUtilities.playSoundAtPlayer(caster, sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){

		if(target != null){
			if(caster.onGround || !requiresFloor){
				if(!spawnConstruct(world, caster.posX, caster.posY, caster.posZ, caster, modifiers)) return false;
				if(sound != null) caster.playSound(sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Actually spawns the construct. By default, spawns the construct at the position of the caster and always returns
	 * true. Returning false will cause the spell to fail.
	 * @param world The world to spawn the construct in.
	 * @param x The x coordinate to spawn the construct at.
	 * @param y The y coordinate to spawn the construct at.
	 * @param z The z coordinate to spawn the construct at.
	 * @param caster The EntityLivingBase that cast this spell.
	 * @param modifiers The modifiers with which the spell was cast.
	 * @return false to cause the spell to fail, true to continue with casting.
	 */
	protected boolean spawnConstruct(World world, double x, double y, double z, EntityLivingBase caster, SpellModifiers modifiers){
		
		if(!world.isRemote){
			// Creates a new constuct using the supplied factory
			T construct = constructFactory.apply(world);
			// Sets the position of the construct (and initialises its bounding box)
			construct.setPosition(x, y, z);
			// Prevents overlapping of multiple constructs of the same type. Since we have an instance here this is
			// very simple. The trade-off is that we have to create the entity before the spell fails, but unless
			// world.spawnEntity(...) is called, its scope is limited to this method so it should be fine.
			if(!allowOverlap && !world.getEntitiesWithinAABB(construct.getClass(), caster.getEntityBoundingBox()).isEmpty()) return false;
			// Sets the various parameters
			construct.setCaster(caster);
			construct.lifetime = baseDuration == -1 ? -1 : (int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade));
			construct.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
			addConstructExtras(construct, caster, modifiers);
			// Spawns the construct in the world 
			world.spawnEntity(construct);
		}
		
		return true;
	}
	
	/**
	 * Called just before each construct is spawned. Does nothing by default, but is provided to allow subclasses to call
	 * extra methods on the spawned entity. This method is only called server-side so cannot be used to spawn particles
	 * directly.
	 * @param construct The entity being spawned.
	 * @param caster The caster of this spell.
	 * @param modifiers The modifiers this spell was cast with.
	 */
	// This is the reason this class is generic: it allows subclasses to do whatever they want to their specific entity,
	// without needing to cast to it.
	protected void addConstructExtras(T construct, EntityLivingBase caster, SpellModifiers modifiers){}

}
