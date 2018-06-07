package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityDart extends EntityMagicArrow
{
	/** Basic shell constructor. Should only be used by the client. */
	public EntityDart(World world) {
		super(world);
	}
	
    /** Creates a projectile at position xyz in world, with no motion. Do not create a projectile with this 
     * constructor and then call setVelocity() as that method is, bizarrely, client-side only. */
    public EntityDart(World world, double x, double y, double z)
    {
        super(world, x, y, z);
    }

    /** Creates a projectile at the position of the caster, pointing at the given target. The trajectory seems to be altered 
     * slightly by a random amount determined by the last parameter. For reference, skeletons set this to 10 on easy, 6 on
     * normal and 2 on hard difficulty. */
    public EntityDart(World world, EntityLivingBase caster, Entity target, float speed, float aimingError, float damageMultiplier)
    {
        super(world, caster, target, speed, aimingError, damageMultiplier);
    }

    /** Creates a projectile pointing in the direction the caster is looking, with the given speed. 
     * USE THIS CONSTRUCTOR FOR NORMAL SPELLS. */
    public EntityDart(World world, EntityLivingBase caster, float speed, float damageMultiplier)
    {
        super(world, caster, speed, damageMultiplier);
    }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
        //Adds a weakness effect to the target.
        entityHit.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 1, true));
		this.playSound("game.neutral.hurt", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}
	
	@Override
	public void onBlockHit(){
		this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}
	
	@Override
	public void tickInAir(){
		
		if(this.worldObj.isRemote){
			Wizardry.proxy.spawnParticle(EnumParticleType.LEAF, worldObj, this.posX, this.posY, this.posZ, 0, -0.03, 0, 10 + rand.nextInt(5));
        }
    }
	
	// Replicates the original behaviour of staying stuck in block for a few seconds before disappearing.
	@Override
	public void tickInGround(){
		if(this.ticksInGround > 60){
			this.setDead();
		}
    }
	
	@Override
    public double getDamage(){
    	return 4.0d;
    }
	
	@Override
	public DamageType getDamageType(){
		return DamageType.MAGIC;
	}
    
	@Override
    public boolean doGravity(){
    	return true;
    }
    
	@Override
    public boolean doDeceleration(){
    	return true;
    }

	@Override
	protected void entityInit() {
		
	}
	
}