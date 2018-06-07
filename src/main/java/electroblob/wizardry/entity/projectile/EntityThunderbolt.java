package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityThunderbolt extends EntityMagicProjectile
{
    public EntityThunderbolt(World par1World)
    {
        super(par1World);
    }

    public EntityThunderbolt(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityThunderbolt(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
    }

    public EntityThunderbolt(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }
    
    /** This is the speed */ 
    protected float func_70182_d()
    {
        return 2.5F;
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
    	Entity entityHit = par1MovingObjectPosition.entityHit;
    	
        if(entityHit != null){
        	
            float damage = 3 * damageMultiplier;
            
            entityHit.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(), damage);
            
            // Knockback
            entityHit.addVelocity(this.motionX*0.2, this.motionY*0.2, this.motionZ*0.2);
        }

        this.playSound("fireworks.largeBlast", 1.4F, 0.5f + this.rand.nextFloat() * 0.1F);

        // Particle effect
        if(worldObj.isRemote){
			worldObj.spawnParticle("largeexplode", this.posX, this.posY, this.posZ, 0, 0, 0);
        }

        this.setDead();
    }
    
    public void onUpdate(){
    	
    	super.onUpdate();
    	
    	if(worldObj.isRemote){
    		Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX + rand.nextFloat()*0.2 - 0.1, this.posY + this.height/2 + rand.nextFloat()*0.2 - 0.1, this.posZ + rand.nextFloat()*0.2 - 0.1, 0, 0, 0, 3);
    		for(int i=0; i<4; i++){
    			worldObj.spawnParticle("smoke", this.posX + rand.nextFloat()*0.2 - 0.1, this.posY + this.height/2 + rand.nextFloat()*0.2 - 0.1, this.posZ + rand.nextFloat()*0.2 - 0.1, 0, 0, 0);
    		}
    	}
    	
    	if(this.ticksExisted > 8){
            this.setDead();
    	}
    }
    
    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    protected float getGravityVelocity()
    {
        return 0.0F;
    }
    
    /**
     * Return whether this entity should be rendered as on fire.
     */
    public boolean canRenderOnFire()
    {
        return false;
    }
}
