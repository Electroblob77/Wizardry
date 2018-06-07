package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntitySpark extends EntityMagicProjectile
{
    public EntitySpark(World par1World)
    {
        super(par1World);
    }

    public EntitySpark(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntitySpark(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
    }

    public EntitySpark(World par1World, double par2, double par4, double par6)
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
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
    	Entity entityHit = par1MovingObjectPosition.entityHit;
    	
        if (entityHit != null){
        	
            float damage = 6 * damageMultiplier;
            entityHit.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.SHOCK), damage);
            
        }

        this.playSound("wizardry:arc", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

        // Particle effect
        if(worldObj.isRemote){
			for(int i=0;i<8;i++){
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX + rand.nextFloat() - 0.5, this.posY + this.height/2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0, 0, 3);
			}
        }

        this.setDead();
    }
    
    public void onUpdate(){
    	
    	super.onUpdate();
	    
    	if(!this.isCollided && !worldObj.isRemote){
	    	
    		double seekingRange = 5.0d;
	        
	        List<EntityLivingBase> entities = WizardryUtilities.getEntitiesWithinRadius(seekingRange, this.posX, this.posY, this.posZ, this.worldObj);
			Entity target = null;

			for(Entity possibleTarget : entities){
				// Decides if current entity should be replaced.
				if(target == null || this.getDistanceToEntity(target) > this.getDistanceToEntity(possibleTarget)){
					// Decides if new entity is a valid target.
					if(WizardryUtilities.isValidTarget(this.getThrower(), possibleTarget)){
						target = possibleTarget;
					}
				}
			}
			
			if(target != null && Math.abs(this.motionX) < 1 && Math.abs(this.motionY) < 1 && Math.abs(this.motionZ) < 1){
				this.addVelocity((target.posX - this.posX)/30, (target.posY + target.height/2 - this.posY)/30, (target.posZ - this.posZ)/30);
			}
    	}
    	
    	if(this.ticksExisted > 100){
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
