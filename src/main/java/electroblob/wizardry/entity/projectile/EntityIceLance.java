package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityIceLance extends EntityMagicArrow {
	
    /** Basic shell constructor. Should only be used by the client. */
	public EntityIceLance(World world) {
		super(world);
		this.setKnockbackStrength(1);
	}
	
    /** Creates a projectile at position xyz in world, with no motion. Do not create a projectile with this 
     * constructor and then call setVelocity() as that method is, bizarrely, client-side only. */
    public EntityIceLance(World world, double x, double y, double z)
    {
        super(world, x, y, z);
		this.setKnockbackStrength(1);
    }

    /** Creates a projectile at the position of the caster, pointing at the given target. The trajectory seems to be altered 
     * slightly by a random amount determined by the last parameter. For reference, skeletons set this to 10 on easy, 6 on
     * normal and 2 on hard difficulty. */
    public EntityIceLance(World world, EntityLivingBase caster, Entity target, float speed, float aimingError, float damageMultiplier)
    {
        super(world, caster, target, speed, aimingError, damageMultiplier);
		this.setKnockbackStrength(1);
    }

    /** Creates a projectile pointing in the direction the caster is looking, with the given speed. 
     * USE THIS CONSTRUCTOR FOR NORMAL SPELLS. */
    public EntityIceLance(World world, EntityLivingBase caster, float speed, float damageMultiplier)
    {
        super(world, caster, speed, damageMultiplier);
		this.setKnockbackStrength(1);
    }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		
        // Adds a freeze effect to the target.
        if(!MagicDamage.isEntityImmune(DamageType.FROST, entityHit)) entityHit.addPotionEffect(new PotionEffect(Wizardry.frost.id, 300, 0, true));
        
		this.playSound("game.neutral.hurt", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}
	
	@Override
	public void tickInAir(){
		
    }

	@Override
	public void onBlockHit(){
		// Adds a particle effect when the ice lance hits a block.
    	if(this.worldObj.isRemote){
    		for(int j=0; j<10; j++){
    			double x = this.posX - 0.25d + (rand.nextDouble()/2);
    			double y = this.posY - 0.25d + (rand.nextDouble()/2);
    			double z = this.posZ - 0.25d + (rand.nextDouble()/2);
    			Wizardry.proxy.spawnParticle(EnumParticleType.ICE, worldObj, x, y, z, x - this.posX, y - this.posY, z - this.posZ, 20 + rand.nextInt(10));
    		}
    	}
    	// Parameters for sound: sound event name, volume, pitch.
    	this.playSound("game.potion.smash", 1.0F, rand.nextFloat() * 0.4F + 1.2F);
    	
	}
	
	@Override
	public void tickInGround(){
    	this.setDead();
	}
	
	@Override
    public double getDamage(){
    	return 10.0d;
    }
	
	@Override
	public DamageType getDamageType(){
		return DamageType.FROST;
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
	public boolean doOverpenetration(){
    	return true;
    }

	@Override
	protected void entityInit() {
		
	}
	
	@Override
	public boolean canRenderOnFire() {
		return false;
	}
	
}