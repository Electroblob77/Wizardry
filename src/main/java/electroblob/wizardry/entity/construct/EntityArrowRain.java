package electroblob.wizardry.entity.construct;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.world.World;

public class EntityArrowRain extends EntityMagicConstruct {
	
	public EntityArrowRain(World par1World) {
		super(par1World);
		this.height = 3.0f;
		this.width = 5.0f;
	}
	
	public EntityArrowRain(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 3.0f;
		this.width = 5.0f;
	}
	
	public void onUpdate(){
		
		super.onUpdate();
		
		if(!this.worldObj.isRemote){
			//System.out.println(this.rotationYaw);
			EntityArrow arrow = new EntityArrow(worldObj, this.posX + rand.nextDouble()*6 - 3, this.posY + rand.nextDouble()*4 - 2, this.posZ + rand.nextDouble()*6 - 3);
			arrow.motionX = Math.cos(Math.toRadians(this.rotationYaw + 90));
			arrow.motionY = -0.6;
			arrow.motionZ = Math.sin(Math.toRadians(this.rotationYaw + 90));
			arrow.shootingEntity = this.getCaster();
			arrow.setDamage(7.0d*damageMultiplier);
			this.worldObj.spawnEntityInWorld(arrow);
		}
	}

}
