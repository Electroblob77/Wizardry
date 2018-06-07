package electroblob.wizardry.entity.projectile;

import java.util.List;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityPoisonBomb extends EntityMagicProjectile implements IEntityAdditionalSpawnData
{
	/** The entity blast multiplier. Only some projectiles cause a blast, which is why this isn't in EntityMagicProjectile. */
	public float blastMultiplier = 1.0f;
	
    public EntityPoisonBomb(World par1World)
    {
        super(par1World);
    }

    public EntityPoisonBomb(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityPoisonBomb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier, float blastMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
        this.blastMultiplier = blastMultiplier;
    }

    public EntityPoisonBomb(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
    	Entity entityHit = par1MovingObjectPosition.entityHit;
    	
        if (entityHit != null)
        {
        	// This is if the poison bomb gets a direct hit
            float damage = 5 * damageMultiplier;

            entityHit.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.POISON).setProjectile(), damage);
            
            if(entityHit instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.POISON, entityHit)) ((EntityLivingBase)entityHit).addPotionEffect(new PotionEffect(Potion.poison.id, 120, 1));
        }

        // Particle effect
        if(worldObj.isRemote){
    		this.worldObj.spawnParticle("largeexplode", this.posX, this.posY, this.posZ, 0, 0, 0);
			for(int i=0;i<60*blastMultiplier;i++){
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 35, 0.2f + rand.nextFloat()*0.3f, 0.6f, 0.0f);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 0, 0.2f + rand.nextFloat()*0.2f, 0.8f, 0.0f);
			}
        }

        if(!this.worldObj.isRemote){
        	
	    	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "game.potion.smash", 1.5F, rand.nextFloat() * 0.4F + 0.6F);
	    	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "random.fizz", 1.2F, 1.0f);

	        double range = 3.0d*blastMultiplier;
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(range, this.posX, this.posY, this.posZ, this.worldObj);
			
			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower() && !MagicDamage.isEntityImmune(DamageType.POISON, target)){
					target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.POISON), 4.0f * damageMultiplier);
		            target.addPotionEffect(new PotionEffect(19, 100, 1));
				}
			}
			
            this.setDead();
        }
    }

	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeFloat(blastMultiplier);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		blastMultiplier = buffer.readFloat();
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
