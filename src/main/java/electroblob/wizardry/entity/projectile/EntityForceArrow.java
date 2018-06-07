package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityForceArrow extends EntityMagicArrow {
	
    /** Basic shell constructor. Should only be used by the client. */
	public EntityForceArrow(World world) {
		super(world);
	}
	
    /** Creates a projectile at position xyz in world, with no motion. Do not create a projectile with this 
     * constructor and then call setVelocity() as that method is, bizarrely, client-side only. */
    public EntityForceArrow(World world, double x, double y, double z)
    {
        super(world, x, y, z);
    }

    /** Creates a projectile at the position of the caster, pointing at the given target. The trajectory seems to be altered 
     * slightly by a random amount determined by the last parameter. For reference, skeletons set this to 10 on easy, 6 on
     * normal and 2 on hard difficulty. */
    public EntityForceArrow(World world, EntityLivingBase caster, Entity target, float speed, float aimingError, float damageMultiplier)
    {
        super(world, caster, target, speed, aimingError, damageMultiplier);
    }

    /** Creates a projectile pointing in the direction the caster is looking, with the given speed. 
     * USE THIS CONSTRUCTOR FOR NORMAL SPELLS. */
    public EntityForceArrow(World world, EntityLivingBase caster, float speed, float damageMultiplier)
    {
        super(world, caster, speed, damageMultiplier);
    }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
        this.playSound("fireworks.blast", 1.0F, 1.0F);
	}
	
	@Override
	public void tickInGround(){
        this.setDead();
	}
	
	@Override
	public void onBlockHit(){
        this.playSound("fireworks.blast", 1.0F, 1.0F);
	}
	
	@Override
	public void tickInAir(){
    	if (this.ticksExisted > 20){
    		this.setDead();
    	}
    }
	
	@Override
    public double getDamage(){
    	return 7.0d;
    }
	
	@Override
	public DamageType getDamageType(){
		return DamageType.FORCE;
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