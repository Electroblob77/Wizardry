package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityMagicMissile extends EntityMagicArrow {
	
    /** Basic shell constructor. Should only be used by the client. */
	public EntityMagicMissile(World world) {
		super(world);
	}
	
    /** Creates a projectile at position xyz in world, with no motion. Do not create a projectile with this 
     * constructor and then call setVelocity() as that method is, bizarrely, client-side only. */
    public EntityMagicMissile(World world, double x, double y, double z)
    {
        super(world, x, y, z);
    }

    /** Creates a projectile at the position of the caster, pointing at the given target. The trajectory seems to be altered 
     * slightly by a random amount determined by the last parameter. For reference, skeletons set this to 10 on easy, 6 on
     * normal and 2 on hard difficulty. */
    public EntityMagicMissile(World world, EntityLivingBase caster, Entity target, float speed, float aimingError, float damageMultiplier)
    {
        super(world, caster, target, speed, aimingError, damageMultiplier);
    }

    /** Creates a projectile pointing in the direction the caster is looking, with the given speed. 
     * USE THIS CONSTRUCTOR FOR NORMAL SPELLS. */
    public EntityMagicMissile(World world, EntityLivingBase caster, float speed, float damageMultiplier)
    {
        super(world, caster, speed, damageMultiplier);
    }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		this.playSound("game.neutral.hurt", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}
	
	@Override
	public void tickInAir(){

    	if (this.ticksExisted > 20){
    		this.setDead();
    	}
		
		if(this.worldObj.isRemote){
      
        	if(this.ticksExisted % 2 == 1){
        		Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX, this.posY, this.posZ, 0, 0, 0, 20 + rand.nextInt(10), 0.5f + (rand.nextFloat()/2), 0.5f + (rand.nextFloat()/2), 0.5f + (rand.nextFloat()/2));
        	}
        	else{
        		Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX, this.posY, this.posZ, 0, 0, 0, 20 + rand.nextInt(10), 0.5f + (rand.nextFloat()/2), 0.5f + (rand.nextFloat()/2), 0.5f + (rand.nextFloat()/2));
        	}
        }
    }
	
	@Override
    public double getDamage(){
    	return 4.0d;
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