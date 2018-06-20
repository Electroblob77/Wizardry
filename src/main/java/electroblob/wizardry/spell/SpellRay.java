package electroblob.wizardry.spell;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which use a raytrace to do something and (optionally) spawn particles along that
 * trajectory. This is for both continuous ('stream') spells and non-continuous ('bolt') spells This allows all the
 * relevant code to be centralised. This class differs from most other spell superclasses in that it is abstract and as
 * such must be subclassed to define what the spell actually does. This is because ray-like spells do a wider variety of
 * different things, so it does not make sense to define more specific functions in this class since they would be
 * redundant in the majority of cases.
 * <p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p>
 * By default, this type of spell requires a packet to be sent. {@link Spell#doesSpellRequirePacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see SpellStream
 */
public abstract class SpellRay extends Spell {
	
	// TODO: Implement the 'aim assist' (borderSize) effect for rayTracing
	
	/** The distance below the caster's eyes that the bolt particles start from. */
	private static final double Y_OFFSET = 0.4;
	
	/** The base range of this spell. */
	protected final double baseRange;
	/** The sound that gets played when this spell is cast. */
	@Nullable
	protected final SoundEvent sound;
	
	/** The volume of the sound played when this spell is cast. Defaults to 1. */
	protected float volume = 1;
	/** The pitch of the sound played when this spell is cast. Defaults to 1. */
	protected float pitch = 1;
	/** The pitch variation of the sound played when this spell is cast. Defaults to 0. */
	protected float pitchVariation = 0;
	/** The distance between spawned particles. Defaults to 0.85. */
	// 0.85 was chosen to keep it similar to the most common method used previously, which gave an effective spacing of
	// 10/12 = 0.8333 when the spell did not hit anything.
	protected double particleSpacing = 0.85;
	/** The maximum dither (random position offset) for spawned particles. Defaults to 0.1. */
	protected double particleDither = 0.1;
	/** The velocity of spawned particles in the direction the caster is aiming, can be negative. Defaults to 0. */
	protected double particleVelocity = 0;
	/** Whether entities are ignored when raytracing. Defaults to false. */
	protected boolean ignoreEntities = false;
	/** Whether liquids count as blocks when raytracing. Defaults to false. */
	protected boolean hitLiquids = false;

	public SpellRay(String name, Tier tier, Element element, SpellType type, int cost, int cooldown, boolean isContinuous, double baseRange, SoundEvent sound){
		this(Wizardry.MODID, name, tier, element, type, cost, cooldown, isContinuous, baseRange, sound);
	}

	public SpellRay(String modID, String name, Tier tier, Element element, SpellType type, int cost, int cooldown, boolean isContinuous, double baseRange, SoundEvent sound){
		super(modID, name, tier, element, type, cost, cooldown, EnumAction.NONE, isContinuous);
		this.baseRange = baseRange;
		this.sound = sound;
	}
	
	/**
	 * Sets the sound parameters for this spell.
	 * @param volume 
	 * @param pitch
	 * @param pitchVariation
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellRay soundValues(float volume, float pitch, float pitchVariation) {
		this.volume = volume;
		this.pitch = pitch;
		this.pitchVariation = pitchVariation;
		return this;
	}
	
	/**
	 * Sets the distance between spawned particles.
	 * @param particleSpacing The distance between particles in the ray effect.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellRay particleSpacing(double particleSpacing){
		this.particleSpacing = particleSpacing;
		return this;
	}
	
	/**
	 * Sets the maximum dither (random position offset) for spawned particles.
	 * @param particleDither The maximum dither for particles in the ray effect.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellRay particleDither(double particleDither){
		this.particleDither = particleDither;
		return this;
	}
	
	/**
	 * Sets the velocity of spawned particles.
	 * @param particleVelocity The velocity of spawned particles in the direction the caster is aiming, can be negative.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellRay particleVelocity(double particleVelocity){
		this.particleVelocity = particleVelocity;
		return this;
	}
	
	/**
	 * Sets whether entities are ignored when raytracing.
	 * @param ignoreEntities Whether to ignore entities when raytracing. If this is true, the spell will pass through
	 * entities as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellRay ignoreEntities(boolean ignoreEntities){
		this.ignoreEntities = ignoreEntities;
		return this;
	}
	
	/**
	 * Sets whether liquids count as blocks when raytracing.
	 * @param hitLiquids Whether to hit liquids when raytracing. If this is false, the spell will pass through
	 * liquids as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellRay hitLiquids(boolean hitLiquids){
		this.hitLiquids = hitLiquids;
		return this;
	}
	
	@Override public boolean canBeCastByNPCs(){ return true; }

	// Finally everything in here is standardised and written in a form that's actually readable - it was long overdue!
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		
		Vec3d look = caster.getLookVec();
		Vec3d origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight() - Y_OFFSET,
				caster.posZ);
		
		double range = baseRange * modifiers.get(WizardryItems.range_upgrade);
		
		// The first method will hit the first block it touches, passing through entities as if they weren't there. 
		// The second method will hit the first thing it touches, whether that's a block or an entity.
		// Note that when it hits a block, the exact position hit is returned in hitVec, whereas when it hits an
		// entity, it just returns the entity's position in hitVec.
		RayTraceResult rayTrace = ignoreEntities
				? WizardryUtilities.standardBlockRayTrace(world, caster, range, hitLiquids)
				: WizardryUtilities.standardEntityRayTrace(world, caster, range, hitLiquids);
		
		boolean flag = false;
		
		if(rayTrace != null){
			// Doesn't matter which way round these are, they're mutually exclusive
			if(rayTrace.typeOfHit == RayTraceResult.Type.ENTITY){
				
				Entity target = rayTrace.entityHit;
				// Do whatever the spell does when it hits an entity
				flag = onEntityHit(world, target, caster, ticksInUse, modifiers);
				// If the spell succeeded, clip the particles to the correct distance so they don't go through the entity
				if(flag){
					// The most pragmatic solution is to use the target's centre point for reasons explained earlier
					double dx = origin.x - target.posX;
					double dy = origin.y - (target.getEntityBoundingBox().minY + target.height/2);
					double dz = origin.z - target.posZ;
					range = MathHelper.sqrt(dx*dx + dy*dy + dz*dz);
				}
				
			}else if(rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){
				// Do whatever the spell does when it hits an block
				flag = onBlockHit(world, rayTrace.getBlockPos(), rayTrace.sideHit, caster, ticksInUse, modifiers);
				// If the spell succeeded, clip the particles to the correct distance so they don't go through the block
				if(flag) range = origin.distanceTo(rayTrace.hitVec);
			}
		}
		
		// If flag is false, either the spell missed or the relevant entity/block hit method returned false
		if(!flag && !onMiss(world, caster, ticksInUse, modifiers)) return false;
		
		// Particle spawning
		if(world.isRemote){
			spawnParticleRay(world, origin, look, range);
		}
		
		if(!isContinuous) caster.swingArm(hand); // Bit of a dirty fix but I don't think it'll be a problem!
		if(sound != null) WizardryUtilities.playSoundAtPlayer(caster, sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		// IDEA: Add in an aiming error and trigger onMiss accordingly
		Vec3d origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight() - Y_OFFSET, caster.posZ);
		Vec3d direction = null;
		
		boolean flag = false;
		
		if(target != null){

			if(!ignoreEntities && onEntityHit(world, target, caster, ticksInUse, modifiers)){
				
				direction = new Vec3d(target.posX, target.getEntityBoundingBox().minY + target.height/2, target.posZ)
						.subtract(origin);
				flag = true;
				
			}else{ // Will run if the spell does not do anything special on entity hit.
				
				int x = MathHelper.floor(target.posX);
				int y = (int)target.getEntityBoundingBox().minY - 1; // -1 because we need the block under the target
				int z = MathHelper.floor(target.posZ);
				BlockPos pos = new BlockPos(x, y, z);
				
				// This works as if the NPC had actually aimed at the floor beneath the target, so it needs to check
				// that the block is not air and (optionally) not a liquid.
				if(!world.isAirBlock(pos) && (!world.getBlockState(pos).getMaterial().isLiquid() || hitLiquids)
						&& onBlockHit(world, pos, EnumFacing.UP, caster, ticksInUse, modifiers)){
					
					direction = new Vec3d(x + 0.5, y + 1, z + 0.5).subtract(origin);
					flag = true;
				}
			}
		}
		
		// Wizards don't miss... yet
		if(!flag) return false;
		
		// Particle spawning (direction should never be null at this point but no harm in checking)
		if(world.isRemote && direction != null){
			spawnParticleRay(world, origin, direction.normalize(), direction.lengthVector());
		}
		
		if(!isContinuous) caster.swingArm(hand); // Bit of a dirty fix but I don't think it'll be a problem!
		if(sound != null) caster.playSound(sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
		return true;
	}
	
	// Private helper method, no-one will need to override it since it's pretty much the whole point of this class.
	private void spawnParticleRay(World world, Vec3d origin, Vec3d direction, double distance){
		
		Vec3d velocity = direction.scale(particleVelocity);
		
		for(double d = particleSpacing; d <= distance; d += particleSpacing){
			double x = origin.x + d*direction.x + particleDither * (world.rand.nextDouble()*2 - 1);
			double y = origin.y + d*direction.y + particleDither * (world.rand.nextDouble()*2 - 1);
			double z = origin.z + d*direction.z + particleDither * (world.rand.nextDouble()*2 - 1);
			spawnParticle(world, x, y, z, velocity.x, velocity.y, velocity.z);
		}
	}
	
	/**
	 * Called when the spell hits an entity. Will never be called if ignoreEntities is true.
	 * @param world The world the entity is in.
	 * @param target The entity that was hit.
	 * @param caster The caster of this spell, or null if this spell was cast from a dispenser.
	 * @param ticksInUse The number of ticks the spell has already been cast for (used only for continuous spells).
	 * @param modifiers The modifiers this spell was cast with.
	 * @return True to continue with spell casting and spawn particles, false to trigger a miss (N.B. you will need to
	 * return false from {@link SpellRay#onMiss(World, EntityLivingBase, int, SpellModifiers)} if a miss should not consume
	 * mana).
	 */
	protected abstract boolean onEntityHit(World world, Entity target, @Nullable EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers);
	
	/**
	 * Called when the spell hits a block.
	 * @param world The world the block is in.
	 * @param pos The BlockPos of the block that was hit.
	 * @param side The side of the block that was hit.
	 * @param caster The caster of this spell, or null if this spell was cast from a dispenser.
	 * @param ticksInUse The number of ticks the spell has already been cast for (used only for continuous spells).
	 * @param modifiers The modifiers this spell was cast with.
	 * @return True to continue with spell casting and spawn particles, false to trigger a miss (N.B. you will need to
	 * return false from {@link SpellRay#onMiss(World, EntityLivingBase, int, SpellModifiers)} if a miss should not consume
	 * mana).
	 */
	protected abstract boolean onBlockHit(World world, BlockPos pos, EnumFacing side, @Nullable EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers);

	/**
	 * Called when the spell does not hit anything or when the spell hits something it has no effect on. Most of the time
	 * this will just return true or false, but some spells may, for example, display a chat readout.
	 * @param world The world the spell is in.
	 * @param caster The caster of this spell, or null if this spell was cast from a dispenser.
	 * @param ticksInUse The number of ticks the spell has already been cast for (used only for continuous spells).
	 * @param modifiers The modifiers this spell was cast with.
	 * @return True to continue with spell casting and spawn particles, false to cause the spell to fail.
	 */
	protected abstract boolean onMiss(World world, @Nullable EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers);
	
	/**
	 * Called at each point along the spell trajectory to spawn one or more particles at that point. Only called
	 * client-side. Does nothing by default.
	 * @param world The world in which to spawn the particle.
	 * @param x The x-coordinate to spawn the particle at, with dither already applied.
	 * @param y The y-coordinate to spawn the particle at, with dither already applied.
	 * @param z The z-coordinate to spawn the particle at, with dither already applied.
	 * @param vx The x velocity to spawn the particle with. Usually this is only non-zero for continuous spells.
	 * @param vy The y velocity to spawn the particle with. Usually this is only non-zero for continuous spells.
	 * @param vz The z velocity to spawn the particle with. Usually this is only non-zero for continuous spells.
	 */
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){}
	
}
