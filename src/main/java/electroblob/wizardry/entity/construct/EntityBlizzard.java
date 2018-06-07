package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBlizzard extends EntityMagicConstruct {

	public EntityBlizzard(World par1World) {
		super(par1World);
		this.height = 1.0f;
		this.width = 1.0f;
	}

	public EntityBlizzard(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 1.0f;
		this.width = 1.0f;
	}

	public void onUpdate(){

		if(this.ticksExisted % 120 == 1){
			this.playSound("wizardry:wind", 1.0f, 1.0f);
		}

		super.onUpdate();

		if(!this.worldObj.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.0d, this.posX, this.posY, this.posZ, this.worldObj);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){
					
					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					if(this.getCaster() != null){
						target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.FROST), 1*damageMultiplier);
					}else{
						target.attackEntityFrom(DamageSource.magic, 1*damageMultiplier);
					}

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;
				}
				
				// All entities are slowed, even the caster (except those immune to frost effects)
				if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
					target.addPotionEffect(new PotionEffect(Wizardry.frost.id, 20, 0, true));
			}
		}else{
			for(int i=1; i<10; i++){
				float brightness = 0.5f + (rand.nextFloat()/2);
				Wizardry.proxy.spawnParticle(EnumParticleType.BLIZZARD, worldObj, this.posX, this.posY + rand.nextDouble()*3, this.posZ, 0, 0, 0, 100, brightness, brightness + 0.1f, 1.0f, false, rand.nextDouble() * 2.5d + 0.5d);
				Wizardry.proxy.spawnParticle(EnumParticleType.BLIZZARD, worldObj, this.posX, this.posY + rand.nextDouble()*3, this.posZ, 0, 0, 0, 100, 1.0f, 1.0f, 1.0f, false, rand.nextDouble() * 2.5d + 0.5d);
			}
		}
	}
}
