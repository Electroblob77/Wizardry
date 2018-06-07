package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.client.particle.EntitySparkleFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityForceOrb extends EntityMagicProjectile {
	
	/** The entity blast multiplier. Only some projectiles cause a blast, which is why this isn't in EntityMagicProjectile. */
	public float blastMultiplier;
	
    public EntityForceOrb(World par1World)
    {
        super(par1World);
    }

    public EntityForceOrb(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityForceOrb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier, float blastMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
        this.blastMultiplier = blastMultiplier;
    }

    public EntityForceOrb(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition){
    	
        if (par1MovingObjectPosition.entityHit != null)
        {
        	// This is if the force orb gets a direct hit
            this.playSound("game.neutral.hurt", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
        }
        
        // Particle effect
        if(this.worldObj.isRemote){
            this.worldObj.spawnParticle("largeexplode", this.posX, this.posY, this.posZ, 0, 0, 0);
    		for(int j=0; j<20; j++){
        		float brightness = 0.5f + (rand.nextFloat()/2);
    			double x = this.posX - 0.25d + (rand.nextDouble()/2);
    			double y = this.posY - 0.25d + (rand.nextDouble()/2);
    			double z = this.posZ - 0.25d + (rand.nextDouble()/2);
    			Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, x, y, z, (x - this.posX)*2, (y - this.posY)*2, (z - this.posZ)*2, 6, brightness, 1.0f, brightness + 0.2f);
    		}
    	}

        if(!this.worldObj.isRemote){
        	
	        // 2 gives a cool flanging effect!
	        float pitch = this.rand.nextFloat() * 0.2F + 0.3F;
	        this.playSound("fireworks.blast", 1.5F, pitch);
	        this.playSound("fireworks.blast", 1.5F, pitch - 0.01f);
	        
	        double blastRadius = 4.0d*blastMultiplier;
	        
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(blastRadius, this.posX, this.posY, this.posZ, this.worldObj);
			
			for(EntityLivingBase target : targets){
				if(target != this.getThrower()){
					
					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;
					
					double dx = this.posX-target.posX > 0? -0.5 - (this.posX-target.posX)/8 : 0.5 - (this.posX-target.posX)/8;
					double dz = this.posZ-target.posZ > 0? -0.5 - (this.posZ-target.posZ)/8 : 0.5 - (this.posZ-target.posZ)/8;
					
					float damage = 4*damageMultiplier;

		            target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.BLAST), damage);

					target.motionX = dx;
					target.motionY = velY + 0.4;
					target.motionZ = dz;
				}
			}
			
            this.setDead();
        }
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
    	super.readEntityFromNBT(nbttagcompound);
        blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("blastMultiplier", blastMultiplier);
	}
}
