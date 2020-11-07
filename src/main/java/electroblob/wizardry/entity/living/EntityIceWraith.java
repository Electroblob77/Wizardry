package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

@Mod.EventBusSubscriber
public class EntityIceWraith extends EntityBlazeMinion {

	/** The version from EntityLivingBase is only used in onLivingUpdate, so it can safely be copied. */
	private int jumpTicks;

	/** Creates a new ice wraith in the given world. */
	public EntityIceWraith(World world){
		super(world);
		this.isImmuneToFire = false;
	}

	@Override
	protected void initEntityAI(){
		super.initEntityAI();
		this.tasks.taskEntries.clear();
		this.tasks.addTask(4, new AIIceShardAttack(this));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
	}

	@Override
	protected void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				float brightness = 0.5f + (rand.nextFloat() / 2);
				ParticleBuilder.create(Type.SPARKLE, this)
				.vel(0, 0.05f, 0)
				.time(20 + rand.nextInt(10))
				.clr(brightness, brightness + 0.1f, 1.0f)
				.spawn(world);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return DrawingUtils.mix(0xffffff, 0x73e1ff, animationProgress);
	}

	@Override
	public void onLivingUpdate(){

		if(!this.onGround && this.motionY < 0.0D){
			this.motionY *= 0.6D;
		}

		if(this.rand.nextInt(24) == 0){
			this.playSound(WizardrySounds.ENTITY_ICE_WRAITH_AMBIENT, 0.3F + this.rand.nextFloat() / 4,
					this.rand.nextFloat() * 0.7F + 1.4F);
		}

		if(this.world.isRemote){
			for(int i = 0; i < 2; ++i){
				this.world.spawnParticle(EnumParticleTypes.CLOUD,
						this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
						this.posY + this.rand.nextDouble() * (double)this.height,
						this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0D, 0.0D, 0.0D);
			}
		}

		// Replaces super call.
		this.livingBaseUpdate();
	}

	/**
	 * Copied from {@link EntityLivingBase#onLivingUpdate()}. The only change is removal of the updateElytra() call
	 * since that's irrelevant here. In actual fact, neither EntityMob nor EntityLiving has any code in its version of
	 * this method that is of use. This isn't exactly ideal, but it's the lesser of two evils since the alternative is
	 * copying the entire EntityBlaze class and its renderer. All to remove one particle effect...
	 */
	// ... demonstrating why critical methods should delegate any non-critical functionality (like particles) to
	// separate protected methods.
	private void livingBaseUpdate(){

		if(this.jumpTicks > 0){
			--this.jumpTicks;
		}

		if(this.newPosRotationIncrements > 0 && !this.canPassengerSteer()){
			double d0 = this.posX + (this.interpTargetX - this.posX) / (double)this.newPosRotationIncrements;
			double d1 = this.posY + (this.interpTargetY - this.posY) / (double)this.newPosRotationIncrements;
			double d2 = this.posZ + (this.interpTargetZ - this.posZ) / (double)this.newPosRotationIncrements;
			double d3 = MathHelper.wrapDegrees(this.interpTargetYaw - (double)this.rotationYaw);
			this.rotationYaw = (float)((double)this.rotationYaw + d3 / (double)this.newPosRotationIncrements);
			this.rotationPitch = (float)((double)this.rotationPitch
					+ (this.interpTargetPitch - (double)this.rotationPitch) / (double)this.newPosRotationIncrements);
			--this.newPosRotationIncrements;
			this.setPosition(d0, d1, d2);
			this.setRotation(this.rotationYaw, this.rotationPitch);
		}else if(!this.isServerWorld()){
			this.motionX *= 0.98D;
			this.motionY *= 0.98D;
			this.motionZ *= 0.98D;
		}

		if(Math.abs(this.motionX) < 0.003D){
			this.motionX = 0.0D;
		}

		if(Math.abs(this.motionY) < 0.003D){
			this.motionY = 0.0D;
		}

		if(Math.abs(this.motionZ) < 0.003D){
			this.motionZ = 0.0D;
		}

		this.world.profiler.startSection("ai");

		if(this.isMovementBlocked()){
			this.isJumping = false;
			this.moveStrafing = 0.0F;
			this.moveForward = 0.0F;
			this.randomYawVelocity = 0.0F;
		}else if(this.isServerWorld()){
			this.world.profiler.startSection("newAi");
			this.updateEntityActionState();
			this.world.profiler.endSection();
		}

		this.world.profiler.endSection();
		this.world.profiler.startSection("jump");

		if(this.isJumping){
			if(this.isInWater()){
				this.handleJumpWater();
			}else if(this.isInLava()){
				this.handleJumpLava();
			}else if(this.onGround && this.jumpTicks == 0){
				this.jump();
				this.jumpTicks = 10;
			}
		}else{
			this.jumpTicks = 0;
		}

		this.world.profiler.endSection();
		this.world.profiler.startSection("travel");
		this.moveStrafing *= 0.98F;
		this.moveForward *= 0.98F;
		this.randomYawVelocity *= 0.9F;
		this.travel(this.moveStrafing, this.moveVertical, this.moveForward);
		this.world.profiler.endSection();
		this.world.profiler.startSection("push");
		this.collideWithNearbyEntities();
		this.world.profiler.endSection();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount){
		// Removes the damage from being wet that applies to blazes by checking if the mob is actually drowning.
		if(source == DamageSource.DROWN && (this.getAir() > 0 || this.isPotionActive(MobEffects.WATER_BREATHING))){
			// In this case, the ice wraith is not actually drowning, so cancel the damage.
			return false;
		}else{
			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	public boolean isBurning(){
		// Uses the datawatcher on both sides because fire is private to Entity (and I'm not using reflection here).
		// This should work, but there may be some issues with updating, so if it doesn't work, copy the
		// version from Entity and use reflection to access the fire field.
		return this.getFlag(0);
	}

	@Override
	public boolean getCanSpawnHere(){
		return super.getCanSpawnHere() && this.isValidLightLevel();
	}

	@SubscribeEvent
	public static void onCheckSpawnEvent(LivingSpawnEvent.CheckSpawn event){
		// We have no way of checking if it's a spawner in getCanSpawnHere() so this has to be done here instead
		if(event.getEntityLiving() instanceof EntityLightningWraith && !event.isSpawner()){
			if(!ArrayUtils.contains(Wizardry.settings.mobSpawnDimensions, event.getWorld().provider.getDimension()))
				event.setResult(Event.Result.DENY);
		}
	}

	/**
	 * Copied straight from EntityBlaze.AIFireballAttack, with the only changes being replacement of fireball spawning
	 * with a one-liner call to WizardryLoot.iceShard.cast(...) and the removal of redundant local variables.
	 */
	static class AIIceShardAttack extends EntityAIBase {

		private final EntityBlaze blaze;
		private int attackStep;
		private int attackTime;

		public AIIceShardAttack(EntityBlaze blazeIn){
			this.blaze = blazeIn;
			this.setMutexBits(3);
		}

		@Override
		public boolean shouldExecute(){
			EntityLivingBase entitylivingbase = this.blaze.getAttackTarget();
			return entitylivingbase != null && entitylivingbase.isEntityAlive();
		}

		@Override
		public void startExecuting(){
			this.attackStep = 0;
		}

		@Override
		public void resetTask(){
			// This might be called setOnFire, but what it really controls is whether the wraith is in attack mode.
			this.blaze.setOnFire(false);
		}

		@Override
		public void updateTask(){
			--this.attackTime;
			EntityLivingBase entitylivingbase = this.blaze.getAttackTarget();
			if(entitylivingbase == null) return; // Dynamic stealth breaks things, let's un-break them
			double d0 = this.blaze.getDistanceSq(entitylivingbase);

			if(d0 < 4.0D){
				if(this.attackTime <= 0){
					this.attackTime = 20;
					this.blaze.attackEntityAsMob(entitylivingbase);
				}

				this.blaze.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY,
						entitylivingbase.posZ, 1.0D);
			}else if(d0 < 256.0D){
				if(this.attackTime <= 0){
					++this.attackStep;

					if(this.attackStep == 1){
						this.attackTime = 60;
						this.blaze.setOnFire(true);
					}else if(this.attackStep <= 4){
						this.attackTime = 6;
					}else{
						this.attackTime = 100;
						this.attackStep = 0;
						this.blaze.setOnFire(false);
					}

					if(this.attackStep > 1){
						// Proof, if it were at all needed, of the elegance and versatility of the spell system.
						Spells.ice_shard.cast(this.blaze.world, this.blaze, EnumHand.MAIN_HAND, 0, entitylivingbase,
								new SpellModifiers());
					}
				}

				this.blaze.getLookHelper().setLookPositionWithEntity(entitylivingbase, 10.0F, 10.0F);
			}else{
				this.blaze.getNavigator().clearPath();
				this.blaze.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY,
						entitylivingbase.posZ, 1.0D);
			}

			super.updateTask();
		}
	}
}
