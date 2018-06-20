package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityLightningSigil extends EntityMagicConstruct {

	public EntityLightningSigil(World world){
		super(world);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.ticksExisted > 600 && this.getCaster() == null && !this.world.isRemote){
			this.setDead();
		}

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(1.0d, this.posX, this.posY,
				this.posZ, this.world);

		for(EntityLivingBase target : targets){

			if(this.isValidTarget(target)){

				double velX = target.motionX;
				double velY = target.motionY;
				double velZ = target.motionZ;

				// Only works if target is actually damaged to account for hurtResistantTime
				if(target.attackEntityFrom(getCaster() != null ? MagicDamage.causeIndirectMagicDamage(this, getCaster(),
						DamageType.SHOCK) : DamageSource.MAGIC, 6)){

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					this.playSound(WizardrySounds.SPELL_SPARK, 1.0f, 1.0f);

					// Secondary chaining effect
					double seekerRange = 5.0d;

					List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange,
							target.posX, target.posY + target.height / 2, target.posZ, world);

					for(int j = 0; j < Math.min(secondaryTargets.size(), 3); j++){

						EntityLivingBase secondaryTarget = secondaryTargets.get(j);

						if(secondaryTarget != target && this.isValidTarget(secondaryTarget)){

							if(!world.isRemote){
								EntityArc arc = new EntityArc(world);
								arc.setEndpointCoords(target.posX, target.posY + target.height / 2, target.posZ,
										secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height / 2,
										secondaryTarget.posZ);
								world.spawnEntity(arc);
							}else{
								for(int k = 0; k < 8; k++){
									ParticleBuilder.create(Type.SPARK)
									.pos(secondaryTarget.posX + world.rand.nextFloat() - 0.5,
										secondaryTarget.getEntityBoundingBox().minY + secondaryTarget.height / 2 + world.rand.nextFloat() * 2 - 1,
										secondaryTarget.posZ + world.rand.nextFloat() - 0.5)
									.spawn(world);
									world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
											secondaryTarget.posX + world.rand.nextFloat() - 0.5,
											secondaryTarget.getEntityBoundingBox().minY + secondaryTarget.height / 2
													+ world.rand.nextFloat() * 2 - 1,
											secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
								}
							}

							secondaryTarget.playSound(WizardrySounds.SPELL_SPARK, 1.0F,
									world.rand.nextFloat() * 0.4F + 1.5F);

							secondaryTarget.attackEntityFrom(
									MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.SHOCK), 4);
						}

					}
					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}

		if(this.world.isRemote && this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble() * 0.3;
			double angle = rand.nextDouble() * Math.PI * 2;
			ParticleBuilder.create(Type.SPARK)
			.pos(this.posX + radius * Math.cos(angle), this.posY + 0.1, this.posZ + radius * Math.sin(angle))
			.spawn(world);
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
