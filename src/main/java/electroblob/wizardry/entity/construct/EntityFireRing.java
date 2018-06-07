package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityFireRing extends EntityMagicConstruct {
	
	public EntityFireRing(World par1World) {
		super(par1World);
		this.height = 1.0f;
		this.width = 5.0f;
	}
	
	public EntityFireRing(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 1.0f;
		this.width = 5.0f;
	}
	
	public void onUpdate(){
		
		if(this.ticksExisted % 40 == 1){
			this.playSound("fire.fire", 4.0f, 0.7f);
		}
		
		super.onUpdate();
		
		if(!this.worldObj.isRemote){
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(2.5d, this.posX, this.posY, this.posZ, this.worldObj);
			
			for(EntityLivingBase target : targets){
				
				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;
					
					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target)){
						
						target.setFire(10);
						
						if(this.getCaster() != null){
							target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.FIRE), 1*damageMultiplier);
						}else{
							target.attackEntityFrom(DamageSource.magic, 1*damageMultiplier);
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
