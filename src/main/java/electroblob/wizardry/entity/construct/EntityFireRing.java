package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityFireRing extends EntityMagicConstruct {

	public EntityFireRing(World world){
		super(world);
		this.height = 1.0f;
		this.width = 5.0f;
	}

	public void onUpdate(){

		if(this.ticksExisted % 40 == 1){
			this.playSound(SoundEvents.BLOCK_FIRE_AMBIENT, 4.0f, 0.7f);
		}

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(2.5d, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target)){

						target.setFire(10);

						if(this.getCaster() != null){
							target.attackEntityFrom(
									MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.FIRE),
									1 * damageMultiplier);
						}else{
							target.attackEntityFrom(DamageSource.MAGIC, 1 * damageMultiplier);
						}
					}

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;
				}
			}
		}
	}

}
