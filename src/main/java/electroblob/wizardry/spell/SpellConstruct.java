package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Generic superclass for all spells which conjure constructs (i.e. instances of {@link EntityMagicConstruct}).
 * This allows all the relevant code to be centralised, since these spells all work in a similar way. Usually, a simple
 * instantiation of this class is sufficient to create a construct spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the construct entity class instead whenever possible.
 * <p></p>
 * This class spawns the construct entity at the caster's feet, like ring of fire and healing aura. Use
 * {@link SpellConstructRanged} (which extends this class) for spells that spawn constructs at an aimed-at position.
 * <p></p>
 * Properties added by this type of spell: {@link Spell#DURATION} (if the construct is not permanent)
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastByDispensers()}
 * <p></p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see SpellConstructRanged
 */
public class SpellConstruct<T extends EntityMagicConstruct> extends Spell {
	
	/** A factory that creates construct entities. */
	protected final Function<World, T> constructFactory;
	/** Whether the construct lasts indefinitely, i.e. does not disappear after a set time. */
	protected final boolean permanent;
	/** Whether the construct must be spawned on the ground. Defaults to false. */
	protected boolean requiresFloor = false;
	/** Whether constructs spawned by this spell may overlap. Defaults to false. */
	protected boolean allowOverlap = false;

	public SpellConstruct(String name, EnumAction action, Function<World, T> constructFactory, boolean permanent){
		this(Wizardry.MODID, name, action, constructFactory, permanent);
	}

	public SpellConstruct(String modID, String name, EnumAction action, Function<World, T> constructFactory, boolean permanent){
		super(modID, name, action, false);
		this.constructFactory = constructFactory;
		this.permanent = permanent;
		if(!permanent) this.addProperties(DURATION);
	}
	
	@Override public boolean requiresPacket(){ return false; }

	@Override public boolean canBeCastByNPCs(){ return true; }
	
	@Override public boolean canBeCastByDispensers() { return true; }
	
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
			if(!spawnConstruct(world, caster.posX, caster.posY, caster.posZ, caster.onGround ? EnumFacing.UP : null,
					caster, modifiers)) return false;
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){

		if(target != null){
			if(caster.onGround || !requiresFloor){
				if(!spawnConstruct(world, caster.posX, caster.posY, caster.posZ, caster.onGround ? EnumFacing.UP : null,
						caster, modifiers)) return false;
				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}

		return false;
	}
	
	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){

		Integer floor = (int)y;

		if(requiresFloor){
			floor = WizardryUtilities.getNearestFloor(world, new BlockPos(x, y, z), 1);
			direction = EnumFacing.UP;
		}

		if(floor != null){
			if(!spawnConstruct(world, x, floor, z, direction, null, modifiers)) return false;
			// This MUST be the coordinates of the actual dispenser, so we need to offset it
			this.playSound(world, x - direction.getXOffset(), y - direction.getYOffset(), z - direction.getZOffset(), ticksInUse, duration, modifiers);
			return true;
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
	 * @param side The side of a block that was hit, or null if the construct is being spawned in mid-air (only happens
	 *             if {@link SpellConstruct#requiresFloor} is true).
	 * @param caster The EntityLivingBase that cast this spell.
	 * @param modifiers The modifiers with which the spell was cast.
	 * @return false to cause the spell to fail, true to continue with casting.
	 */
	protected boolean spawnConstruct(World world, double x, double y, double z, @Nullable EnumFacing side, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		
		if(!world.isRemote){
			// Creates a new construct using the supplied factory
			T construct = constructFactory.apply(world);
			// Sets the position of the construct (and initialises its bounding box)
			construct.setPosition(x, y, z);
			// Sets the various parameters
			construct.setCaster(caster);
			construct.lifetime = permanent ? -1 : (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
			construct.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
			addConstructExtras(construct, side, caster, modifiers);
			// Prevents overlapping of multiple constructs of the same type. Since we have an instance here this is
			// very simple. The trade-off is that we have to create the entity before the spell fails, but unless
			// world.spawnEntity(...) is called, its scope is limited to this method so it should be fine.
			// Needs to be last in case addConstructExtras modifies the bounding box
			if(!allowOverlap && !world.getEntitiesWithinAABB(construct.getClass(), construct.getEntityBoundingBox()).isEmpty()) return false;
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
	 * @param side The side of a block that was hit, or null if the construct is being spawned in mid-air (only happens
	 *             if {@link SpellConstruct#requiresFloor} is true).
	 * @param caster The caster of this spell, or null if it was cast by a dispenser.
	 * @param modifiers The modifiers this spell was cast with.
	 */
	// This is the reason this class is generic: it allows subclasses to do whatever they want to their specific entity,
	// without needing to cast to it.
	protected void addConstructExtras(T construct, EnumFacing side, @Nullable EntityLivingBase caster, SpellModifiers modifiers){}

}
