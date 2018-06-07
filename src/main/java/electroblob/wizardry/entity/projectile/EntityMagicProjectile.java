package electroblob.wizardry.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** This class is a generic superclass for all <b>non-directed</b> projectiles, namely: darkness orb, firebolt, firebomb,
 * force orb, ice charge, lightning disc, poison bomb, spark, spark bomb and thunderbolt. Directed (arrow-like) projectiles
 * should instead extend {@link EntityMagicArrow}.
 * <p>
 * This class purely handles saving of the damage multiplier; EntityThrowable is pretty well suited to my purposes
 * as it is. Range is done via the velocity when the constructor is called. Caster is already handled by EntityThrowable.getThrower().
 * 
 * Note that this class does not implement {@link IEntityAdditionalSpawnData}; subclasses that need to transfer extra data
 * to the client should implement that interface themselves. See {@link EntitySparkBomb} for an example.
 * @since Wizardry 1.0
 */
public abstract class EntityMagicProjectile extends EntityThrowable
{
	public float damageMultiplier = 1.0f;
	
    public EntityMagicProjectile(World par1World)
    {
        super(par1World);
    }

    public EntityMagicProjectile(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityMagicProjectile(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier)
    {
        super(par1World, par2EntityLivingBase);
        this.damageMultiplier = damageMultiplier;
    }

    public EntityMagicProjectile(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }
    
    /** Sets this projectile's velocity as a normalised vector towards the target. */
    public void directTowards(Entity target, float velocity){

        double dx = target.posX - this.posX;
        double dy = target.boundingBox.minY + (double)(target.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
        double dz = target.posZ - this.posZ;
        
        this.motionX = dx/this.getDistanceToEntity(target) * velocity;
        this.motionY = dy/this.getDistanceToEntity(target) * velocity;
        this.motionZ = dz/this.getDistanceToEntity(target) * velocity;
    }
    
    @Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
    	super.readEntityFromNBT(nbttagcompound);
        damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("damageMultiplier", damageMultiplier);
	}
	
}
