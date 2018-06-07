package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityFirebolt extends EntityMagicProjectile
{
    public EntityFirebolt(World par1World)
    {
        super(par1World);
    }

    public EntityFirebolt(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityFirebolt(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
    }

    public EntityFirebolt(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }
    
    /** This is the speed */ 
    @Override
    protected float func_70182_d()
    {
        return 2.5F;
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    @Override
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
    	Entity entityHit = par1MovingObjectPosition.entityHit;
    	
        if (entityHit != null)
        {
            float damage = 5 * damageMultiplier;
            
            entityHit.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(), damage);
            
            if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit)) entityHit.setFire(5);
        }

        this.playSound("liquid.lavapop", 2, 0.8f + rand.nextFloat()*0.3f);

        // Particle effect
        if(worldObj.isRemote){
			for(int i=0;i<8;i++){
				//Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkFX(worldObj, this.posX + rand.nextFloat() - 0.5, this.posY + this.height/2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0, 0));
				worldObj.spawnParticle("lava", this.posX + rand.nextFloat() - 0.5, this.posY + this.height/2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0, 0);
			}
        }

        this.setDead();
    }
    
    @Override
    public void onUpdate(){
    	
    	super.onUpdate();
    	
    	if(worldObj.isRemote){
    		for(int i=0; i<4; i++){
    			worldObj.spawnParticle("flame", this.posX + rand.nextFloat()*0.2 - 0.1, this.posY + this.height/2 + rand.nextFloat()*0.2 - 0.1, this.posZ + rand.nextFloat()*0.2 - 0.1, 0, 0, 0);
    		}
    	}
    	
    	if(this.ticksExisted > 8){
            this.setDead();
    	}
    }
    
    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    @Override
    protected float getGravityVelocity()
    {
        return 0.0F;
    }
    
    /**
     * Return whether this entity should be rendered as on fire.
     */
    @Override
    public boolean canRenderOnFire()
    {
        return false;
    }
}
