package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.Spells;
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
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.ArrayUtils;

@Mod.EventBusSubscriber
public class EntityLightningWraith extends EntityBlazeMinion {

	public EntityLightningWraith(World world){
		super(world);
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
				ParticleBuilder.create(Type.SPARKLE, this).vel(0, 0.05, 0).time(20 + rand.nextInt(10))
				.clr(brightness, brightness + 0.2f, 1.0f).spawn(world);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return DrawingUtils.mix(0xffffff, 0x0092ff, animationProgress);
	}

	@Override
	public void onLivingUpdate(){
		// Fortunately, lightning wraiths don't replace any of blazes' particle effects or the fire sound, they only
		// add the sparks, so it's fine to call super here.
		if(world.isRemote){
			ParticleBuilder.create(Type.SPARK, this).spawn(world);
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

	@Override
	public boolean getCanSpawnHere(){
		return super.getCanSpawnHere() && this.isValidLightLevel();
	}

	@SubscribeEvent
	public static void onCheckSpawnEvent(LivingSpawnEvent.CheckSpawn event){
		// We have no way of checking if it's a spawner in getCanSpawnHere() so this has to be done here instead
		if(event.getEntityLiving() instanceof EntityLightningWraith && !event.isSpawner()){
			if(!event.getWorld().isThundering()) event.setResult(Event.Result.DENY);
			if(!ArrayUtils.contains(Wizardry.settings.mobSpawnDimensions, event.getWorld().provider.getDimension()))
				event.setResult(Event.Result.DENY);
		}
	}

	/**
	 * Copied straight from EntityBlaze.AIFireballAttack, with the only changes being replacement of fireball spawning
	 * with a one-liner call to WizardryLoot.arc.cast(...) and the removal of redundant local variables.
	 */
	static class AILightningAttack extends EntityAIBase {

		private final EntityBlaze blaze;
		private int attackStep;
		private int attackTime;

		public AILightningAttack(EntityBlaze blazeIn){
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
						Spells.arc.cast(this.blaze.world, this.blaze, EnumHand.MAIN_HAND, 0, entitylivingbase, new SpellModifiers());
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
