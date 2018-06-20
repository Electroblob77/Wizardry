package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityFireSigil extends EntityMagicConstruct {

	public EntityFireSigil(World world){
		super(world);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(width/2, posX, posY, posZ, world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					target.attackEntityFrom(this.getCaster() != null
							? MagicDamage.causeIndirectMagicDamage(this, this.getCaster(), DamageType.FIRE)
							: DamageSource.MAGIC, 6);

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target)) target.setFire(10);

					this.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);

					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble() * 0.3;
			double angle = rand.nextDouble() * Math.PI * 2;
			world.spawnParticle(EnumParticleTypes.FLAME, this.posX + radius * Math.cos(angle), this.posY + 0.1,
					this.posZ + radius * Math.sin(angle), 0, 0, 0);
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
