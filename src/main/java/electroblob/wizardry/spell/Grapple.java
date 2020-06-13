package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.IVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class Grapple extends Spell {

	/** The speed at which the vine extends/retracts from the caster, in blocks per tick. */
	public static final String EXTENSION_SPEED = "extension_speed";
	/** The speed at which the vine reels in the caster or target, in blocks per tick. */
	public static final String REEL_SPEED = "reel_speed";

	public static final IVariable<RayTraceResult> TARGET_KEY = new IVariable.Variable<RayTraceResult>(Persistence.NEVER)
																.withTicker(Grapple::update);

	/** The distance from the target position at which the spell will stop reeling in entities. */
	private static final double MINIMUM_REEL_DISTANCE = 3;
	/** The acceleration with which the vine reels in the caster or target. */
	private static final double REEL_ACCELERATION = 0.3;
	/** The speed at which the caster or target is lowered when the caster is sneaking. */
	private static final double PAYOUT_SPEED = 0.25;
	/** Once attached, the vine can stretch beyond the maximum range by this factor before breaking. */
	private static final double STRETCH_LIMIT = 1.5;
	/** The distance between spawned particles. */
	protected static final double PARTICLE_SPACING = 1.5;
	/** The maximum jitter (random position offset) for spawned particles. */
	protected static final double PARTICLE_JITTER = 0.04;

	public Grapple(){
		super("grapple", SpellActions.GRAPPLE, true);
		addProperties(RANGE, EXTENSION_SPEED, REEL_SPEED);
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override){
		return true;
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser){
		return true;
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createSoundsWithSuffixes("shoot", "attach", "pull", "release");
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData data = WizardData.get(caster);

		Vec3d origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight(), caster.posZ);

		float extensionSpeed = getProperty(EXTENSION_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY);

		RayTraceResult hit = data.getVariable(TARGET_KEY);

		// Initial targeting
		if(hit == null){
			hit = findTarget(world, caster, origin, caster.getLookVec(), modifiers);
			data.setVariable(TARGET_KEY, hit);
			caster.swingArm(hand);
			// This condition prevents the sound playing every tick after a missed shot has finished extending
			if(hit.typeOfHit != RayTraceResult.Type.MISS
					|| ticksInUse * extensionSpeed < getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade)){
				this.playSound(world, caster, ticksInUse, -1, modifiers, "shoot");
			}
		}

		Vec3d target = hit.hitVec;

		if(hit.entityHit instanceof EntityLivingBase){
			// If the target is an entity, we need to use the entity's centre rather than the original hit position
			// because the entity will have moved!
			target = new Vec3d(hit.entityHit.posX, hit.entityHit.getEntityBoundingBox().minY + hit.entityHit.height/2, hit.entityHit.posZ);
		}

		double distance = origin.distanceTo(target);
		Vec3d direction = target.subtract(origin).normalize();

		double maxLength = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade) * STRETCH_LIMIT;

		// If the vine stretched too far
		if(distance > maxLength){
			if(world.isRemote && (ticksInUse-1) * extensionSpeed < distance){
				spawnLeafParticles(world, origin.subtract(0, SpellRay.Y_OFFSET, 0), direction, distance);
			}
			data.setVariable(TARGET_KEY, null);
			return false; // The spell is finished
		}

		boolean extending = ticksInUse * extensionSpeed < distance;

		if(extending){
			// Extension
			if(world.isRemote){
				// world.getTotalWorldTime() - ticksInUse generates a constant but unique seed each time the spell is cast
				ParticleBuilder.create(Type.VINE).entity(caster).pos(0, caster.getEyeHeight() - SpellRay.Y_OFFSET, 0)
						.target(origin.add(direction.scale(ticksInUse * extensionSpeed))).tvel(direction.scale(extensionSpeed))
						.seed(world.getTotalWorldTime() - ticksInUse).spawn(world);
			}

		}else{
			// Retraction
			Vec3d velocity = direction.scale(getProperty(REEL_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY));

			int retractTime = ticksInUse - (int)(distance/extensionSpeed);

			switch(hit.typeOfHit){

				case BLOCK:
					// Payout
					if(caster.isSneaking() && ItemArtefact.isArtefactActive(caster, WizardryItems.charm_abseiling))
						velocity = new Vec3d(velocity.x, distance < maxLength-1 ? -PAYOUT_SPEED : distance-maxLength+1, velocity.z);

					// Reel the caster towards the block hit
					double ax = (velocity.x - caster.motionX) * REEL_ACCELERATION;
					double ay = (velocity.y - caster.motionY) * REEL_ACCELERATION;
					double az = (velocity.z - caster.motionZ) * REEL_ACCELERATION;
					caster.addVelocity(ax, ay, az);

					if(caster.motionY > 0 && !Wizardry.settings.replaceVanillaFallDamage) caster.fallDistance = 0; // Reset fall distance if the caster moves upwards

					if(world.isRemote){
						ParticleBuilder.create(Type.VINE).entity(caster).pos(0, caster.getEyeHeight() - SpellRay.Y_OFFSET, 0)
								.target(target).seed(world.getTotalWorldTime() - ticksInUse).spawn(world);
					}

					if(retractTime == 1){ // Just hit
						this.playSound(world, caster, ticksInUse, -1, modifiers,  "pull");
						this.playSound(world, hit.hitVec, ticksInUse, -1, modifiers,  "attach");
					}

					break;

				case ENTITY:
					// Payout
					if(caster.isSneaking() && ItemArtefact.isArtefactActive(caster, WizardryItems.charm_abseiling))
						velocity = new Vec3d(velocity.x, distance < maxLength-1 ? PAYOUT_SPEED : maxLength-1-distance, velocity.z);

					// Reel the entity hit towards the caster
					Entity entity = hit.entityHit;

					if(distance > MINIMUM_REEL_DISTANCE){
						double ax1 = (-velocity.x - entity.motionX) * REEL_ACCELERATION;
						double ay1 = (-velocity.y - entity.motionY) * REEL_ACCELERATION;
						double az1 = (-velocity.z - entity.motionZ) * REEL_ACCELERATION;
						entity.addVelocity(ax1, ay1, az1);
						// Player motion is handled on that player's client so needs packets
						if(entity instanceof EntityPlayerMP){
							((EntityPlayerMP)entity).connection.sendPacket(new SPacketEntityVelocity(entity));
						}
					}

					if(world.isRemote){
						ParticleBuilder.create(Type.VINE).entity(caster).pos(0, caster.getEyeHeight() - SpellRay.Y_OFFSET, 0)
								.target(entity).seed(world.getTotalWorldTime() - ticksInUse).spawn(world);
					}

					if(retractTime == 1){ // Just hit
						this.playSound(world, caster, ticksInUse, -1, modifiers,  "pull");
						this.playSound(world, entity.posX, entity.posY, entity.posZ, ticksInUse, -1, modifiers,  "attach");
					}

					break;

				default:
					// Missed
					if(world.isRemote && (ticksInUse-1) * extensionSpeed < distance){
						spawnLeafParticles(world, origin.subtract(0, SpellRay.Y_OFFSET, 0), direction, distance);
					}
					//caster.resetActiveHand();
					data.setVariable(TARGET_KEY, null);
					return false; // The spell is finished
			}
		}

		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){

		if(target == null) return false;

		Vec3d origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight(), caster.posZ);

		// If the target is an entity, we need to use the entity's centre rather than the original hit position
		// because the entity will have moved!
		Vec3d targetVec = new Vec3d(target.posX, target.getEntityBoundingBox().minY + target.height/2, target.posZ);

		RayTraceResult hit = findTarget(world, caster, origin, targetVec.subtract(origin).normalize(), modifiers);

		if(hit.typeOfHit != RayTraceResult.Type.ENTITY || hit.entityHit != target) return false; // Something was in the way

		double distance = origin.distanceTo(targetVec);

		// Can't cast the spell at all if the target is too far away
		if(ticksInUse <= 1 && distance > getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade))
			return false;

		Vec3d vec = targetVec.subtract(origin).normalize();

		float extensionSpeed = getProperty(EXTENSION_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY);

		// If the vine stretched too far
		if(distance > getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade) * STRETCH_LIMIT){
			if(world.isRemote && (ticksInUse-1) * extensionSpeed < distance){
				spawnLeafParticles(world, origin.subtract(0, SpellRay.Y_OFFSET, 0), vec, distance);
			}
			return false;
		}

		Vec3d hookPosition;

		if(ticksInUse * extensionSpeed < distance){
			// Extension
			hookPosition = origin.add(vec.scale(ticksInUse * extensionSpeed));

		}else{
			// Retraction
			Vec3d velocity = vec.scale(getProperty(REEL_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY));

			// Reel the entity hit towards the caster
			if(distance > MINIMUM_REEL_DISTANCE){
				double ax1 = (-velocity.x - target.motionX) * REEL_ACCELERATION;
				double ay1 = (-velocity.y - target.motionY) * REEL_ACCELERATION;
				double az1 = (-velocity.z - target.motionZ) * REEL_ACCELERATION;
				target.addVelocity(ax1, ay1, az1);
				// Player motion is handled on that player's client so needs packets
				if(target instanceof EntityPlayerMP){
					((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
				}
			}

			hookPosition = targetVec;
		}

		if(world.isRemote){
			// world.getTotalWorldTime() - ticksInUse generates a constant but unique seed each time the spell is cast
			ParticleBuilder.create(Type.VINE).pos(origin).target(hookPosition).tvel(vec.scale(extensionSpeed))
					.seed(world.getTotalWorldTime() - ticksInUse).spawn(world);
		}

		return true;
	}

	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){

		Vec3d origin = new Vec3d(x, y, z);

		RayTraceResult result = findTarget(world, null, origin, new Vec3d(direction.getDirectionVec()), modifiers);

		if(result.entityHit instanceof EntityLivingBase){

			Entity entity = result.entityHit;

			// If the target is an entity, we need to use the entity's centre rather than the original hit position
			// because the entity will have moved!
			Vec3d target = new Vec3d(entity.posX, entity.getEntityBoundingBox().minY + entity.height/2, entity.posZ);

			double distance = origin.distanceTo(target);
			Vec3d vec = target.subtract(origin).normalize();

			float extensionSpeed = getProperty(EXTENSION_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY);

			// If the vine stretched too far
			if(distance > getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade) * STRETCH_LIMIT){
				if(world.isRemote && (ticksInUse-1) * extensionSpeed < distance){
					spawnLeafParticles(world, origin.subtract(0, SpellRay.Y_OFFSET, 0), vec, distance);
				}
				return false;
			}

			Vec3d hookPosition;

			if(ticksInUse * extensionSpeed < distance){
				// Extension
				hookPosition = origin.add(vec.scale(ticksInUse * extensionSpeed));

			}else{
				// Retraction
				Vec3d velocity = vec.scale(getProperty(REEL_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY));

				// Reel the entity hit towards the caster
				if(distance > MINIMUM_REEL_DISTANCE){
					double ax1 = (-velocity.x - entity.motionX) * REEL_ACCELERATION;
					double ay1 = (-velocity.y - entity.motionY) * REEL_ACCELERATION;
					double az1 = (-velocity.z - entity.motionZ) * REEL_ACCELERATION;
					entity.addVelocity(ax1, ay1, az1);
					// Player motion is handled on that player's client so needs packets
					if(entity instanceof EntityPlayerMP){
						((EntityPlayerMP)entity).connection.sendPacket(new SPacketEntityVelocity(entity));
					}
				}

				hookPosition = target;
			}

			if(world.isRemote){
				// world.getTotalWorldTime() - ticksInUse generates a constant but unique seed each time the spell is cast
				ParticleBuilder.create(Type.VINE).pos(origin).target(hookPosition).seed(world.getTotalWorldTime() - ticksInUse).spawn(world);
			}

			return true;
		}

		return false;
	}

	@Override
	public void finishCasting(World world, @Nullable EntityLivingBase caster, double x, double y, double z, EnumFacing facing, int duration, SpellModifiers modifiers){

		Vec3d origin = null;
		Vec3d direction = null;
		Vec3d target = null;

		if(caster != null){

			origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight(), caster.posZ);

			if(caster instanceof EntityPlayer){
				WizardData data = WizardData.get((EntityPlayer)caster);
				if(data != null){
					RayTraceResult hit = data.getVariable(TARGET_KEY);
					if(hit != null) target = hit.hitVec;
				}
			}else if(caster instanceof EntityLiving){
				Entity entity = ((EntityLiving)caster).getAttackTarget();
				if(entity != null) target = new Vec3d(entity.posX, entity.getEntityBoundingBox().minY + entity.height/2, entity.posZ);
			}

			if(target != null) direction = target.subtract(origin).normalize();

			this.playSound(world, caster, duration, duration, modifiers, "release");

		}else if(!Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z)){

			origin = new Vec3d(x, y, z);
			direction = new Vec3d(facing.getDirectionVec());
			RayTraceResult result = findTarget(world, null, origin, direction, modifiers);
			target = result.hitVec;

			this.playSound(world, origin, duration, duration, modifiers, "release");
		}

		if(world.isRemote && origin != null && direction != null){

			float extensionSpeed = getProperty(EXTENSION_SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY);
			double distance = Math.min(target.subtract(origin).length(), duration * extensionSpeed);

			spawnLeafParticles(world, origin, direction, distance);
		}
	}

	private void spawnLeafParticles(World world, Vec3d origin, Vec3d direction, double distance){
		// Copied from SpellRay
		for(double d = PARTICLE_SPACING; d <= distance; d += PARTICLE_SPACING){
			double x = origin.x + d*direction.x;// + PARTICLE_JITTER * (world.rand.nextDouble()*2 - 1);
			double y = origin.y + d*direction.y;// + PARTICLE_JITTER * (world.rand.nextDouble()*2 - 1);
			double z = origin.z + d*direction.z;// + PARTICLE_JITTER * (world.rand.nextDouble()*2 - 1);
			ParticleBuilder.create(Type.LEAF, world.rand, x, y, z, PARTICLE_JITTER, true).time(25 + world.rand.nextInt(5)).spawn(world);
		}
	}

	private RayTraceResult findTarget(World world, @Nullable EntityLivingBase caster, Vec3d origin, Vec3d direction, SpellModifiers modifiers){

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		Vec3d endpoint = origin.add(direction.scale(range));

		RayTraceResult result = RayTracer.rayTrace(world, origin, endpoint, 0, false,
				true, false, Entity.class, RayTracer.ignoreEntityFilter(caster));

		// Non-solid blocks (or if the result is null) count as misses
		if(result == null || result.typeOfHit == RayTraceResult.Type.BLOCK
				&& !world.getBlockState(result.getBlockPos()).getMaterial().isSolid()){
			return new RayTraceResult(RayTraceResult.Type.MISS, endpoint, EnumFacing.DOWN, new BlockPos(endpoint));
		// Immovable entities count as misses too, but the endpoint is the hit vector instead
		}else if(result.entityHit != null && !result.entityHit.canBePushed()){
			return new RayTraceResult(RayTraceResult.Type.MISS, result.hitVec, EnumFacing.DOWN, new BlockPos(endpoint));
		}
		// If the ray trace missed, result.hitVec will be the endpoint anyway - neat!
		return result;
	}

	private static RayTraceResult update(EntityPlayer player, RayTraceResult grapplingTarget){

		if(grapplingTarget != null && (!EntityUtils.isCasting(player, Spells.grapple)
				|| (grapplingTarget.entityHit != null && !grapplingTarget.entityHit.isEntityAlive()))){
			return null;
		}

		return grapplingTarget;
	}
}
