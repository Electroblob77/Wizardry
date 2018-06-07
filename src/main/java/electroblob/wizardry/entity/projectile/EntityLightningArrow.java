package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityLightningArrow extends EntityMagicArrow {

    /** Basic shell constructor. Should only be used by the client. */
	public EntityLightningArrow(World world) {
		super(world);
	}
	
    /** Creates a projectile at position xyz in world, with no motion. Do not create a projectile with this 
     * constructor and then call setVelocity() as that method is, bizarrely, client-side only. */
    public EntityLightningArrow(World world, double x, double y, double z)
    {
        super(world, x, y, z);
    }

    /** Creates a projectile at the position of the caster, pointing at the given target. The trajectory seems to be altered 
     * slightly by a random amount determined by the last parameter. For reference, skeletons set this to 10 on easy, 6 on
     * normal and 2 on hard difficulty. */
    public EntityLightningArrow(World world, EntityLivingBase caster, Entity target, float speed, float aimingError, float damageMultiplier)
    {
        super(world, caster, target, speed, aimingError, damageMultiplier);
    }

    /** Creates a projectile pointing in the direction the caster is looking, with the given speed. 
     * USE THIS CONSTRUCTOR FOR NORMAL SPELLS. */
    public EntityLightningArrow(World world, EntityLivingBase caster, float speed, float damageMultiplier)
    {
        super(world, caster, speed, damageMultiplier);
    }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		
		if(worldObj.isRemote){
			for(int j=0;j<8;j++){
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX + rand.nextFloat() - 0.5, this.posY + this.height/2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0, 0, 3);
			}
		}
		
		if(entityHit instanceof EntityCreeper && !((EntityCreeper)entityHit).getPowered()){
	        entityHit.getDataWatcher().updateObject(17, Byte.valueOf((byte)1));
			if(this.getShootingEntity() instanceof EntityPlayer) ((EntityPlayer)this.getShootingEntity()).triggerAchievement(Wizardry.chargeCreeper);
		}
		
        this.playSound("wizardry:arc", 1.0F, 1.0F);
	}
	
	@Override
	public void tickInAir(){

    	if (this.ticksExisted > 20){
    		this.setDead();
    	}

		if(worldObj.isRemote){
			Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX, this.posY, this.posZ, 0, 0, 0, 3);
        }
		
    }
	
	@Override
    public double getDamage(){
    	return 7.0d;
    }
	
	@Override
	public DamageType getDamageType(){
		return DamageType.SHOCK;
	}
    
	@Override
    public boolean doGravity(){
    	return false;
    }
    
	@Override
    public boolean doDeceleration(){
    	return false;
    }

	@Override
	protected void entityInit() {
		
	}
	
}