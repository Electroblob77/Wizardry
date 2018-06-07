package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityDarknessOrb extends EntityMagicProjectile
{
    public EntityDarknessOrb(World par1World)
    {
        super(par1World);
    }

    public EntityDarknessOrb(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityDarknessOrb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
    }

    public EntityDarknessOrb(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }
    
    /** This is the speed */ 
    protected float func_70182_d()
    {
        return 0.5F;
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition movingobjectposition)
    {
    	Entity target = movingobjectposition.entityHit;
    	
        if (target != null && !MagicDamage.isEntityImmune(DamageType.WITHER, target))
        {
            float damage = 8 * damageMultiplier;
            
            target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.WITHER).setProjectile(), damage);
            
            if(target instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.WITHER, target)) ((EntityLivingBase)target).addPotionEffect(new PotionEffect(Potion.wither.id, 150, 1));

            this.playSound("mob.wither.hurt", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
        }

        this.setDead();
    }
    
    public void onUpdate(){
    	
    	super.onUpdate();
    	
    	if(worldObj.isRemote){
	    	float brightness = rand.nextFloat()*0.2f;
	    	Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0, 0, 0, 20 + rand.nextInt(10), brightness, 0.0f, brightness);
	    	Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, worldObj, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
    	}
    	
    	if(this.ticksExisted > 150){
            this.setDead();
    	}
    	
    	// Cancels out the slowdown effect in EntityThrowable
    	this.motionX /= 0.99;
    	this.motionY /= 0.99;
    	this.motionZ /= 0.99;
    }
    
    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    protected float getGravityVelocity()
    {
        return 0.0F;
    }
}
