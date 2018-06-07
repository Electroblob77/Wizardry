package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityHealAura extends EntityMagicConstruct {

	public EntityHealAura(World world) {
		super(world);
		this.height = 1.0f;
		this.width = 5.0f;
	}

	public EntityHealAura(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 1.0f;
		this.width = 5.0f;
	}

	public void onUpdate(){

		if(this.ticksExisted % 25 == 1){
			this.playSound("wizardry:sparkle", 0.1f, 1.0f);
		}

		super.onUpdate();

		if(!this.worldObj.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(2.5d, this.posX, this.posY, this.posZ, this.worldObj);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					if(target.isEntityUndead()){

						double velX = target.motionX;
						double velY = target.motionY;
						double velZ = target.motionZ;

						if(this.getCaster() != null){
							target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.RADIANT), 1*damageMultiplier);
						}else{
							target.attackEntityFrom(DamageSource.magic, 1*damageMultiplier);
						}

						// Removes knockback
						target.motionX = velX;
						target.motionY = velY;
						target.motionZ = velZ;
					}

				}else if(target.getHealth() < target.getMaxHealth() && this.ticksExisted % 5 == 0){
					target.heal(1*damageMultiplier);
				}
			}
		}else{
			for(int i=1; i<3; i++){
				float brightness = 0.5f + (rand.nextFloat()*0.5f);
				double radius = rand.nextDouble()*2.0;
				double angle = rand.nextDouble()*Math.PI*2;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX + radius*Math.cos(angle), this.posY, this.posZ + radius*Math.sin(angle), 0, 0.05f, 0, 48 + this.rand.nextInt(12), 1.0f, 1.0f, brightness);
			}
		}
	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	public boolean canRenderOnFire()
	{
		return false;
	}

}
