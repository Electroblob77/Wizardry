package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityFirebomb extends EntityBomb {
	
    public EntityFirebomb(World par1World)
    {
        super(par1World);
    }

    public EntityFirebomb(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityFirebomb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier, float blastMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier, blastMultiplier);
    }

    public EntityFirebomb(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(RayTraceResult par1RayTraceResult)
    {
    	Entity entityHit = par1RayTraceResult.entityHit;
    	
        if (entityHit != null)
        {
        	// This is if the firebomb gets a direct hit
            float damage = 5 * damageMultiplier;
            
            entityHit.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(), damage);
            
            if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit)) entityHit.setFire(10);
        }

        // Particle effect
        if(worldObj.isRemote){
    		this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
			for(int i=0;i<60*blastMultiplier;i++){
				//this.worldObj.spawnParticle(EnumParticleTypes.FLAME, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0, 0, 0);
				Wizardry.proxy.spawnParticle(WizardryParticleType.MAGIC_FIRE, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0, 0, 0, 15 + rand.nextInt(5), 2 + rand.nextFloat(), 0, 0);
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 0, 1.0f, 0.2f + rand.nextFloat()*0.4f, 0.0f);
			}
        }

        if(!this.worldObj.isRemote){
        	
	    	this.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
	    	this.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
	
	        double range = 3.0d*blastMultiplier;
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(range, this.posX, this.posY, this.posZ, this.worldObj);
			
			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower() && !MagicDamage.isEntityImmune(DamageType.FIRE, target)){
					// Splash damage does not count as projectile damage
					target.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE), 4.0f * damageMultiplier);
					target.setFire(7);
				}
			}
			
            this.setDead();
        }
    }
    
}
