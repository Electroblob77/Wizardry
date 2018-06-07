package electroblob.wizardry.entity.construct;

import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.Wizardry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityIceSpike extends EntityMagicConstruct {

	public EntityIceSpike(World world) {
		super(world);
		this.setSize(0.5f, 1.0f);
	}
	
	public EntityIceSpike(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier){
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.setSize(0.5f, 1.0f);
	}
	
	public void onUpdate(){
		
		if(lifetime - this.ticksExisted < 15){
			this.motionY = -0.01*(this.ticksExisted-(lifetime-15));
		}else if(lifetime - this.ticksExisted < 25){
			this.motionY = 0;
		}else if(lifetime - this.ticksExisted < 28){
			this.motionY = 0.25;
		}
		
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		
		if(lifetime - this.ticksExisted == 30) this.playSound("wizardry:ice", 1, 2);
		
		if(!this.worldObj.isRemote){
			for(Object entity : this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox)){
				if(entity instanceof EntityLivingBase && this.isValidTarget((EntityLivingBase)entity)){
					// Potion effect only gets added if the damage succeeded.
					if(((EntityLivingBase)entity).attackEntityFrom(MagicDamage.causeDirectMagicDamage(this.getCaster(), DamageType.FROST), 5*this.damageMultiplier))
						((EntityLivingBase)entity).addPotionEffect(new PotionEffect(Wizardry.frost.id, 100, 0));
				}
			}
		}
		
		super.onUpdate();
	}

}
