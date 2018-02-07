package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityLightningWraith extends EntityBlazeMinion {

	public EntityLightningWraith(World world){
		super(world);
	}

	public EntityLightningWraith(World world, double x, double y, double z, EntityLivingBase caster, int lifetime){
		super(world, x, y, z, caster, lifetime);
		this.isImmuneToFire = false;
	}

	@Override
	protected void initEntityAI(){
		super.initEntityAI();
		this.tasks.taskEntries.clear();
		this.tasks.addTask(4, new AILightningAttack(this));
		this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
		this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(8, new EntityAILookIdle(this));
	}

	@Override
	protected void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				float brightness = 0.3f + (rand.nextFloat() / 2);
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, this.posX - 0.5d + rand.nextDouble(),
						this.posY + this.height / 2 - 0.5d + rand.nextDouble(), this.posZ - 0.5d + rand.nextDouble(), 0,
						0.05f, 0, 20 + rand.nextInt(10), brightness, brightness + 0.2f, 1.0f);
			}
		}
	}

	@Override
	public void onLivingUpdate(){
		// Fortunately, lightning wraiths don't replace any of blazes' particle effects or the fire sound, they only
		// add the sparks, so it's fine to call super here.
		if(world.isRemote){
			Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world,
					this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
					this.posY + this.rand.nextDouble() * (double)this.height,
					this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0, 0, 0, 3);
		}
		super.onLivingUpdate();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount){
		// Removes the damage from being wet that applies to blazes by checking if the mob is actually drowning.
		if(source == DamageSource.DROWN && (this.getAir() > 0 || this.isPotionActive(MobEffects.WATER_BREATHING))){
			// In this case, the lightning wraith is not actually drowning, so cancel the damage.
			return false;
		}else{
			return super.attackEntityFrom(source, amount);
		}
	}

	@Override
	public boolean isBurning(){
		// Uses the datawatcher on both sides because fire is private to Entity (and I'm not using reflection here).
		// TESTME: This should work, but there may be some issues with updating, so if it doesn't work, copy the
		// version from Entity and use reflection to access the fire field.
		return this.getFlag(0);
	}

	/**
	 * Copied straight from EntityBlaze.AIFireballAttack, with the only changes being replacement of fireball spawning
	 * with a one-liner call to WizardryRegistry.arc.cast(...) and the removal of redundant local variables.
	 */
	static class AILightningAttack extends EntityAIBase {

		private final EntityBlaze blaze;
		private int attackStep;
		private int attackTime;

		public AILightningAttack(EntityBlaze blazeIn){
			this.blaze = blazeIn;
			this.setMutexBits(3);
		}

		/**
		 * Returns whether the EntityAIBase should begin execution.
		 */
		public boolean shouldExecute(){
			EntityLivingBase entitylivingbase = this.blaze.getAttackTarget();
			return entitylivingbase != null && entitylivingbase.isEntityAlive();
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void startExecuting(){
			this.attackStep = 0;
		}

		/**
		 * Resets the task
		 */
		public void resetTask(){
			// This might be called setOnFire, but what it really controls is whether the wraith is in attack mode.
			this.blaze.setOnFire(false);
		}

		/**
		 * Updates the task
		 */
		public void updateTask(){
			--this.attackTime;
			EntityLivingBase entitylivingbase = this.blaze.getAttackTarget();
			double d0 = this.blaze.getDistanceSqToEntity(entitylivingbase);

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
						Spells.arc.cast(this.blaze.world, this.blaze, EnumHand.MAIN_HAND, 0, entitylivingbase,
								new SpellModifiers());
						// TODO: Decide if an event should be fired here. I'm guessing no.
					}
				}

				this.blaze.getLookHelper().setLookPositionWithEntity(entitylivingbase, 10.0F, 10.0F);
			}else{
				this.blaze.getNavigator().clearPathEntity();
				this.blaze.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY,
						entitylivingbase.posZ, 1.0D);
			}

			super.updateTask();
		}
	}
}
