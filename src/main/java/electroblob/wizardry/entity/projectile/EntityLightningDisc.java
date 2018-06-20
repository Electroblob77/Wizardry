package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityLightningDisc extends EntityMagicProjectile {
	
	public EntityLightningDisc(World world){
		super(world);
		this.width = 2.0f;
		this.height = 0.5f;
	}

	@Override
	protected void onImpact(RayTraceResult result){
		
		Entity entityHit = result.entityHit;

		if(entityHit != null){
			float damage = 12 * damageMultiplier;

			entityHit.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
					damage);
		}

		this.playSound(WizardrySounds.SPELL_SPARK, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

		if(result.typeOfHit == RayTraceResult.Type.BLOCK) this.setDead();
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		// Particle effect
		if(world.isRemote){
			for(int i = 0; i < 8; i++){
				// TODO: Why are the x and z parameters different?
				ParticleBuilder.create(Type.SPARK).pos(this.posX + rand.nextFloat() * 2 - 1,
						this.posY, this.posZ + rand.nextFloat() * 2 - 1).spawn(world);
			}
		}

		if(!this.collided && !world.isRemote){

			double seekingRange = 5.0d;

			List<EntityLivingBase> entities = WizardryUtilities.getEntitiesWithinRadius(seekingRange, this.posX,
					this.posY, this.posZ, this.world);
			Entity target = null;

			for(Entity possibleTarget : entities){
				// Decides if current entity should be replaced.
				if(target == null || this.getDistance(target) > this.getDistance(possibleTarget)){
					// Decides if new entity is a valid target.
					if(WizardryUtilities.isValidTarget(this.getThrower(), possibleTarget)){
						target = possibleTarget;
					}
				}
			}

			if(target != null && Math.abs(this.motionX) < 1 && Math.abs(this.motionY) < 1
					&& Math.abs(this.motionZ) < 1){
				this.addVelocity((target.posX - this.posX) / 30, (target.posY + target.height / 2 - this.posY) / 30,
						(target.posZ - this.posZ) / 30);
			}
		}

		if(this.ticksExisted > 50){
			this.setDead();
		}

		// Cancels out the slowdown effect in EntityThrowable
		this.motionX /= 0.99;
		this.motionY /= 0.99;
		this.motionZ /= 0.99;
	}

	@Override
	public boolean hasNoGravity(){
		return true;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}
}
