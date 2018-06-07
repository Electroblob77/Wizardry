package electroblob.wizardry.entity.projectile;

import java.util.List;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntitySmokeBomb extends EntityMagicProjectile implements IEntityAdditionalSpawnData
{
	/** The entity blast multiplier. Only some projectiles cause a blast, which is why this isn't in EntityMagicProjectile. */
	public float blastMultiplier = 1.0f;
	
    public EntitySmokeBomb(World par1World)
    {
        super(par1World);
    }

    public EntitySmokeBomb(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntitySmokeBomb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier, float blastMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
        this.blastMultiplier = blastMultiplier;
    }

    public EntitySmokeBomb(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
    	/*
    	Entity entityHit = par1MovingObjectPosition.entityHit;

        if (entityHit != null)
        {
        	// This is if the smoke bomb gets a direct hit
            float damage = 2.0f * damageMultiplier;

            entityHit.attackEntityFrom(WizardryUtilities.causeIndirectEntityMagicDamage(this, this.getThrower()), damage);
            
            if(entityHit instanceof EntityLivingBase) ((EntityLivingBase)entityHit).addPotionEffect(new PotionEffect(Potion.blindness.id, 120, 0));
        }
        */

        // Particle effect
        if(worldObj.isRemote){
    		this.worldObj.spawnParticle("largeexplode", this.posX, this.posY, this.posZ, 0, 0, 0);
			for(int i=0;i<60*blastMultiplier;i++){
				this.worldObj.spawnParticle("largesmoke", this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0, 0, 0);
				float brightness = rand.nextFloat() * 0.3f;
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 0, brightness, brightness, brightness);
			}
        }

        if(!this.worldObj.isRemote){
        	
	    	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "game.potion.smash", 1.5F, rand.nextFloat() * 0.4F + 0.6F);
	    	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "random.fizz", 1.2F, 1.0f);

	        double range = 3.0d * blastMultiplier;
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(range, this.posX, this.posY, this.posZ, this.worldObj);
			
			for(EntityLivingBase target : targets){
				if(target != this.getThrower()){
					// Gives the target blindness if it is a player, mind trick otherwise (since this has the desired
					// effect of preventing targeting)
					if(target instanceof EntityPlayer){
						target.addPotionEffect(new PotionEffect(Potion.blindness.id, 120, 0));
					}else if(target instanceof EntityLiving){
						// New AI
						((EntityLiving)target).setAttackTarget(null);
						// Old AI
						if(target instanceof EntityCreature) ((EntityCreature)target).setTarget(null);
						
						target.addPotionEffect(new PotionEffect(Wizardry.mindTrick.id, 120, 0));
					}
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
