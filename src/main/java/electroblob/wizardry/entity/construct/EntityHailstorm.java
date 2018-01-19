package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.projectile.EntityIceShard;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityHailstorm extends EntityMagicConstruct {
	
	public EntityHailstorm(World par1World) {
		super(par1World);
		this.height = 3.0f;
		this.width = 5.0f;
	}
	
	public EntityHailstorm(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 3.0f;
		this.width = 5.0f;
	}
	
	public void onUpdate(){
		
		super.onUpdate();
		
		if(!this.worldObj.isRemote){
			//System.out.println(this.rotationYaw);
			EntityIceShard iceshard = new EntityIceShard(worldObj, this.posX + rand.nextDouble()*6 - 3, this.posY + rand.nextDouble()*4 - 2, this.posZ + rand.nextDouble()*6 - 3);
			iceshard.motionX = Math.cos(Math.toRadians(this.rotationYaw + 90));
			iceshard.motionY = -0.6;
			iceshard.motionZ = Math.sin(Math.toRadians(this.rotationYaw + 90));
			iceshard.setShootingEntity(this.getCaster());
			iceshard.damageMultiplier = this.damageMultiplier;
			this.worldObj.spawnEntityInWorld(iceshard);
		}
	}

}
