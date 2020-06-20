package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Generic superclass for all spells which use a raytrace to do something and (optionally) spawn particles along that
 * trajectory. This is for both continuous ('stream') spells and non-continuous ('bolt') spells. This allows all the
 * relevant code to be centralised. This class differs from most other spell superclasses in that it is abstract and as
 * such must be subclassed to define what the spell actually does. This is because ray-like spells do a wider variety of
 * different things, so it does not make sense to define more specific functions in this class since they would be
 * redundant in the majority of cases.
 * <p></p>
 * <i>N.B. The three abstract methods in this class have a {@link Nullable} caster parameter (the caster is null when
 * the spell is cast by a dispenser). When implementing these methods, be sure to check whether the  caster is
 * {@code null} and deal with it appropriately.</i>
 * <p></p>
 * Properties added by this type of spell: {@link Spell#RANGE}
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastBy(EntityLiving, boolean)}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastBy(TileEntityDispenser)}
 * <p></p>
 * By default, this type of spell requires a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
public abstract class SpellRay extends Spell {
	
	/** The distance below the caster's eyes that the bolt particles start from. */
	protected static final double Y_OFFSET = 0.25;

	/** The distance between spawned particles. Defaults to 0.85. */
	// 0.85 was chosen to keep it similar to the most common method used previously, which gave an effective spacing of
	// 10/12 = 0.8333 when the spell did not hit anything.
	protected double particleSpacing = 0.85;
	/** The maximum jitter (random position offset) for spawned particles. Defaults to 0.1. */
	protected double particleJitter = 0.1;
	/** The velocity of spawned particles in the direction the caster is aiming, can be negative. Defaults to 0. */
	protected double particleVelocity = 0;
	/** Whether living entities are ignored when raytracing. Defaults to false. */
	protected boolean ignoreLivingEntities = false;
	/** Whether liquids count as blocks when raytracing. Defaults to false. */
	protected boolean hitLiquids = false;
	/** Whether to ignore uncollidable blocks when raytracing. Defaults to true. */
	protected boolean ignoreUncollidables = true;
	/** The aim assist to use when raytracing. Defaults to 0. */
	protected float aimAssist = 0;

	public SpellRay(String name, EnumAction action, boolean isContinuous){
		this(Wizardry.MODID, name, action, isContinuous);
	}

	public SpellRay(String modID, String name, EnumAction action, boolean isContinuous){
		super(modID, name, action, isContinuous);
		this.addProperties(RANGE);
		this.npcSelector((e, o) -> true);
	}
	
	// Although this class is abstract, someone might instantiate one of its subclasses more than once to make two
	// different spells, which may require different parameters. These methods allow such instances to neatly set any
	// relevant parameters by chaining them onto the constructor.
	
	/**
	 * Sets the distance between spawned particles.
	 * @param particleSpacing The distance between particles in the ray effect.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell particleSpacing(double particleSpacing){
		this.particleSpacing = particleSpacing;
		return this;
	}
	
	/**
	 * Sets the maximum jitter (random position offset) for spawned particles.
	 * @param particleJitter The maximum jitter for particles in the ray effect.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell particleJitter(double particleJitter){
		this.particleJitter = particleJitter;
		return this;
	}
	
	/**
	 * Sets the velocity of spawned particles; usually used for continuous spells.
	 * @param particleVelocity The velocity of spawned particles in the direction the caster is aiming, can be negative.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell particleVelocity(double particleVelocity){
		this.particleVelocity = particleVelocity;
		return this;
	}
	
	/**
	 * Sets whether entities are ignored when raytracing.
	 * @param ignoreLivingEntities Whether to ignore living entities when raytracing. If this is true, the spell
	 *                             will pass through living entities as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell ignoreLivingEntities(boolean ignoreLivingEntities){
		this.ignoreLivingEntities = ignoreLivingEntities;
		return this;
	}
	
	/**
	 * Sets whether liquids count as blocks when raytracing.
	 * @param hitLiquids Whether to hit liquids when raytracing. If this is false, the spell will pass through
	 * liquids as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell hitLiquids(boolean hitLiquids){
		this.hitLiquids = hitLiquids;
		return this;
	}

	/**
	 * Sets whether uncollidable blocks are ignored when raytracing.
	 * @param ignoreUncollidables Whether to hit uncollidable blocks when raytracing. If this is true, the spell will
	 * pass through uncollidable blocks as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell ignoreUncollidables(boolean ignoreUncollidables){
		this.ignoreUncollidables = ignoreUncollidables;
		return this;
	}
	
	/**
	 * Sets the aim assist to use when raytracing.
	 * @param aimAssist The aim assist to use when raytracing. See {@link RayTracer#rayTrace(World, Vec3d, Vec3d, float, boolean, boolean, boolean, Class, java.util.function.Predicate)} for more details.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell aimAssist(float aimAssist){
		this.aimAssist = aimAssist;
		return this;
	}

	@Override public boolean canBeCastBy(TileEntityDispenser dispenser) { return true; }

	// Finally everything in here is standardised and written in a form that's actually readable - it was long overdue!
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		
		Vec3d look = caster.getLookVec();
		Vec3d origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight() - Y_OFFSET, caster.posZ);
		if(!this.isContinuous && world.isRemote && !Wizardry.proxy.isFirstPerson(caster)){
			origin = origin.add(look.scale(1.2));
		}

		if(!shootSpell(world, origin, look, caster, ticksInUse, modifiers)) return false;
		
		if(casterSwingsArm(world, caster, hand, ticksInUse, modifiers)) caster.swingArm(hand);
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		// IDEA: Add in an aiming error and trigger onMiss accordingly
		Vec3d origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight() - Y_OFFSET, caster.posZ);
		Vec3d targetPos = null;

		if(!ignoreLivingEntities || !EntityUtils.isLiving(target)){
			targetPos = new Vec3d(target.posX, target.getEntityBoundingBox().minY + target.height / 2, target.posZ);

		}else{

			int x = MathHelper.floor(target.posX);
			int y = (int)target.getEntityBoundingBox().minY - 1; // -1 because we need the block under the target
			int z = MathHelper.floor(target.posZ);
			BlockPos pos = new BlockPos(x, y, z);

			// This works as if the NPC had actually aimed at the floor beneath the target, so it needs to check that
			// the block is not air and (optionally) not a liquid.
			if(!world.isAirBlock(pos) && (!world.getBlockState(pos).getMaterial().isLiquid() || hitLiquids)){
				targetPos = new Vec3d(x + 0.5, y + 1, z + 0.5);
			}
		}

		if(targetPos == null) return false; // If there was nothing to aim at (e.g. snare when the target is in the air)

		if(!shootSpell(world, origin, targetPos.subtract(origin).normalize(), caster, ticksInUse, modifiers)) return false;

		if(casterSwingsArm(world, caster, hand, ticksInUse, modifiers)) caster.swingArm(hand);
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}
	
	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		
		Vec3d vec = new Vec3d(direction.getDirectionVec());
		Vec3d origin = new Vec3d(x, y, z);
		
		if(!shootSpell(world, origin, vec, null, ticksInUse, modifiers)) return false;
		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getXOffset(), y - direction.getYOffset(), z - direction.getZOffset(), ticksInUse, duration, modifiers);
		return true;
	}

	/**
	 * Hook allowing subclasses to override the default range calculation on a per-cast basis. For example, grapple
	 * overrides this to change the range based on casting time so that its vine attaches to entities/blocks at the
	 * correct point and moves them accordingly.
	 *
	 * @param world The world in which the spell is being cast.
	 * @param origin A vector representing the coordinates of the origin point of the spell.
	 * @param direction A normalised vector representing the direction in which the spell is being cast.
	 * @param caster The entity casting the spell, or null if the spell is being cast from a dispenser.
	 * @param ticksInUse The number of ticks the spell has already been cast for. For all non-continuous spells,
	 *                   this is 0 and is not used.
	 * @param modifiers The SpellModifiers object with which this spell is being cast.
	 * @return The range to be used for this particular casting of the spell.
	 */
	// Technically you could alter the range in the SpellModifiers object by overriding the cast method but that
	// would be a bit of a hack since it's not really what spell modifiers are for.
	protected double getRange(World world, Vec3d origin, Vec3d direction, @Nullable EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade);
	}

	/**
	 * Hook allowing subclasses to determine whether the caster swings their arm when casting the spell. By default,
	 * returns true for non-continuous spells without an action.
	 *
	 * @param world A reference to the world object. This is for convenience, you can also use caster.world.
	 * @param caster The EntityLivingBase that cast the spell.
	 * @param hand The hand that is holding the item used to cast the spell. If no item was used, this will be the
	 *        main hand.
	 * @param ticksInUse The number of ticks the spell has already been cast for. For all non-continuous spells, this is
	 *        0 and is not used. For continuous spells, it is passed in as the maximum use duration of the item minus
	 *        the count parameter in onUsingItemTick and therefore it increases by 1 each tick.
	 * @return True if the caster should swing their arm when casting this spell, false if not.
	 */
	protected boolean casterSwingsArm(World world, EntityLivingBase caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		return !this.isContinuous && this.action == EnumAction.NONE;
	}

	/** Takes care of the shared stuff for the three casting methods. This is mainly for internal use. */
	protected boolean shootSpell(World world, Vec3d origin, Vec3d direction, @Nullable EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		double range = getRange(world, origin, direction, caster, ticksInUse, modifiers);
		Vec3d endpoint = origin.add(direction.scale(range));
			
		// Change the filter depending on whether living entities are ignored or not
		RayTraceResult rayTrace = RayTracer.rayTrace(world, origin, endpoint, aimAssist, hitLiquids,
				ignoreUncollidables, false, Entity.class, ignoreLivingEntities ? EntityUtils::isLiving
				: RayTracer.ignoreEntityFilter(caster));
		
		boolean flag = false;

		if(rayTrace != null){
			// Doesn't matter which way round these are, they're mutually exclusive
			if(rayTrace.typeOfHit == RayTraceResult.Type.ENTITY){
				// Do whatever the spell does when it hits an entity
				// FIXME: Some spells (e.g. lightning web) seem to not render when aimed at item frames
				flag = onEntityHit(world, rayTrace.entityHit, rayTrace.hitVec, caster, origin, ticksInUse, modifiers);
				// If the spell succeeded, clip the particles to the correct distance so they don't go through the entity
				if(flag) range = origin.distanceTo(rayTrace.hitVec);
				
			}else if(rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){
				// Do whatever the spell does when it hits an block
				flag = onBlockHit(world, rayTrace.getBlockPos(), rayTrace.sideHit, rayTrace.hitVec, caster, origin, ticksInUse, modifiers);
				// Clip the particles to the correct distance so they don't go through the block
				// Unlike with entities, this is done regardless of whether the spell succeeded, since no spells go
				// through blocks (and in fact, even the ray tracer itself doesn't do that)
				range = origin.distanceTo(rayTrace.hitVec);
			}
		}
		
		// If flag is false, either the spell missed or the relevant entity/block hit method returned false
		if(!flag && !onMiss(world, caster, origin, direction, ticksInUse, modifiers)) return false;
		
		// Particle spawning
		if(world.isRemote){
			spawnParticleRay(world, origin, direction, caster, range);
		}
		
		return true;
	}
	
	// The exact behaviour of the returned values of the following three methods can be a little confusing. Normally,
	// either onEntityHit or onBlockHit (or both) will return true when the spell succeeded in hitting the block or
	// entity, and false if not (note that those two methods are mutually exclusive). If false is returned, onMiss will
	// be called - if either of the other methods returns true, onMiss will only be called for a complete miss.
	
	/**
	 * Called when the spell hits an entity. Will never be called if ignoreLivingEntities is true.
	 * @param world The world the entity is in.
	 * @param target The entity that was hit.
	 * @param hit A vector representing the exact position at which the spell first hit the entity. Usually used for
	 * particle spawning.
	 * @param caster The caster of this spell, or null if this spell was cast from a dispenser. <i> N.B. It is strongly
	 * recommended that the origin parameter is used instead of taking the caster's position directly.</i>
	 * @param origin The position at which this spell originated. If the caster is not null, this will be at the caster's
	 * eyes.
	 * @param ticksInUse The number of ticks the spell has already been cast for (used only for continuous spells).
	 * @param modifiers The modifiers this spell was cast with.
	 * @return True to continue with spell casting and spawn particles, false to trigger a miss (N.B. you will need to
	 * return false from {@link SpellRay#onMiss(World, EntityLivingBase, Vec3d, Vec3d, int, SpellModifiers)} if a miss
	 * should not consume mana). Returning false from this method will make it look as if the spell passed right
	 * through it, so if a spell spawns particles when it misses this method should return true even for non-living
	 * entities.
	 */
	protected abstract boolean onEntityHit(World world, Entity target, Vec3d hit, @Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers);
	
	/**
	 * Called when the spell hits a block.
	 * @param world The world the block is in.
	 * @param pos The BlockPos of the block that was hit.
	 * @param side The side of the block that was hit.
	 * @param hit A vector representing the exact position at which the spell first hit the block. Usually used for
	 * particle spawning.
	 * @param caster The caster of this spell, or null if this spell was cast from a dispenser.
	 * @param origin The position at which this spell originated. If the caster is not null, this will be at the caster's
	 * eyes.
	 * @param ticksInUse The number of ticks the spell has already been cast for (used only for continuous spells).
	 * @param modifiers The modifiers this spell was cast with.
	 * @return True to continue with spell casting and spawn particles, false to trigger a miss (N.B. you will need to
	 * return false from {@link SpellRay#onMiss(World, EntityLivingBase, Vec3d, Vec3d, int, SpellModifiers)} if a miss should not consume
	 * mana).
	 */
	protected abstract boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, @Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers);

	/**
	 * Called when the spell does not hit anything or when the spell hits something it has no effect on. Most of the time
	 * this will just return true or false, but some spells may, for example, display a chat readout or spawn custom
	 * particles. It is worth noting that this can affect how easy the spell is to identify.
	 * @param world The world the spell is in.
	 * @param caster The caster of this spell, or null if this spell was cast from a dispenser.
	 * @param origin The position at which this spell originated. If the caster is not null, this will be at the caster's
	 * eyes.
	 * @param direction A normalised vector in the direction this spell was cast (useful for custom particle effects).
	 * @param ticksInUse The number of ticks the spell has already been cast for (used only for continuous spells).
	 * @param modifiers The modifiers this spell was cast with.
	 * @return True to continue with spell casting and spawn particles, false to cause the spell to fail.
	 */
	protected abstract boolean onMiss(World world, @Nullable EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers);
	
	/**
	 * Highest-level particle spawning method, only called client-side. 'Normal' subclasses should not need to override
	 * this method; by default it spawns a line of particles, applying jitter and then calling
	 * {@link SpellRay#spawnParticle(World, double, double, double, double, double, double)} at each point. Override to replace this with
	 * an entirely custom particle effect - this is done by a few spells in the main mod to spawn beam-type particles.
	 * @param world The world in which to spawn the particles.
	 * @param origin A vector representing the start point of the line of particles.
	 * @param direction A normalised vector representing the direction of the line of particles.
	 * @param caster The entity that cast this spell, or null if it was cast by a dispenser.
	 * @param distance The length of the line of particles, already set to the appropriate distance based on the spell's
	 */
	// The caster argument is only really useful for spawning targeted particles continuously
	protected void spawnParticleRay(World world, Vec3d origin, Vec3d direction, @Nullable EntityLivingBase caster, double distance){
		
		Vec3d velocity = direction.scale(particleVelocity);
		
		for(double d = particleSpacing; d <= distance; d += particleSpacing){
			double x = origin.x + d*direction.x + particleJitter * (world.rand.nextDouble()*2 - 1);
			double y = origin.y + d*direction.y + particleJitter * (world.rand.nextDouble()*2 - 1);
			double z = origin.z + d*direction.z + particleJitter * (world.rand.nextDouble()*2 - 1);
			spawnParticle(world, x, y, z, velocity.x, velocity.y, velocity.z);
		}
	}
	
	/**
	 * Called at each point along the spell trajectory to spawn one or more particles at that point. Only called
	 * client-side. Does nothing by default.
	 * @param world The world in which to spawn the particle.
	 * @param x The x-coordinate to spawn the particle at, with jitter already applied.
	 * @param y The y-coordinate to spawn the particle at, with jitter already applied.
	 * @param z The z-coordinate to spawn the particle at, with jitter already applied.
	 * @param vx The x velocity to spawn the particle with. Usually this is only non-zero for continuous spells.
	 * @param vy The y velocity to spawn the particle with. Usually this is only non-zero for continuous spells.
	 * @param vz The z velocity to spawn the particle with. Usually this is only non-zero for continuous spells.
	 */
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){}
	
}
