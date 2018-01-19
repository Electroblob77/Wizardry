package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityHammer extends EntityMagicConstruct {

    /** How long the hammer has been falling for. */
    public int fallTime;

    public EntityHammer(World par1World){
        super(par1World);
        this.setSize(1.0f, 1.9F);
        this.noClip = false;
    }

    public EntityHammer(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier){
        super(world, x, y, z, caster, lifetime, damageMultiplier);
        this.setSize(1.0f, 1.9F);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.noClip = false;
    }
    
    @Override
    public boolean isBurning(){
        return false;
    }

    @Override
    public boolean canBeCollidedWith(){
        return true;
    }
    
    @Override
    public AxisAlignedBB getCollisionBoundingBox(){
    	return this.getEntityBoundingBox();
    }

    @Override
    public void onUpdate(){
    	
    	super.onUpdate();
    	
    	if(this.ticksExisted % 20 == 1 && !this.onGround && worldObj.isRemote){
    		// Though this sound does repeat, it stops when it hits the ground.
    		Wizardry.proxy.playMovingSound(this, WizardrySounds.SPELL_LOOP_LIGHTNING, 3.0f, 1.0f, false);
		}
    	
    	if(this.worldObj.isRemote && this.ticksExisted % 3 == 0){
			Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, worldObj, this.posX - 0.5d + rand.nextDouble(), this.posY + 2*rand.nextDouble(), this.posZ - 0.5d + rand.nextDouble(), 0, 0, 0, 3);
    	}
    	
    	if(!this.worldObj.isRemote){

            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            ++this.fallTime;
            this.motionY -= 0.03999999910593033D;
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;
            
	        if(this.onGround){
	        	
	        	this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
                this.motionY *= -0.5D;
	        	
	            if(this.ticksExisted % 40 == 0){
	    		
		            double seekerRange = 10.0d;
					
					List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, this.posX, this.posY+1, this.posZ, worldObj);
					
					// For this spell there is no limit to the amount of secondary targets!
					for(EntityLivingBase target : targets){
						
						if(this.isValidTarget(target)){
							
							if(!worldObj.isRemote){
								EntityArc arc = new EntityArc(worldObj);
								arc.setEndpointCoords(this.posX, this.posY + this.height - 0.1, this.posZ,
										target.posX, target.posY + target.height/2, target.posZ);
								worldObj.spawnEntityInWorld(arc);
							}else{
								for(int j=0;j<8;j++){
									Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, worldObj, target.posX + worldObj.rand.nextFloat() - 0.5, target.getEntityBoundingBox().minY + target.height/2 + worldObj.rand.nextFloat()*2 - 1, target.posZ + worldObj.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					    			worldObj.spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX + rand.nextFloat(), target.getEntityBoundingBox().minY + target.height/2 + rand.nextFloat(), target.posZ + rand.nextFloat(), 0, 0, 0);
					    		}
							}
							
							target.playSound(WizardrySounds.SPELL_SPARK, 1.0F, rand.nextFloat() * 0.4F + 1.5F);
							
							if(this.getCaster() != null){
								WizardryUtilities.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.SHOCK), 6*damageMultiplier);
								WizardryUtilities.applyStandardKnockback(this, target);
							}else{
								target.attackEntityFrom(DamageSource.magic, 6*damageMultiplier);
							}
						}
					}
	            }
	    	}
    	}
    }
    
    @Override
    public void despawn(){
    	
		this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0f);
		
		if(this.worldObj.isRemote){
			this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}
		
		super.despawn();
    }
    
    @Override
    public void fall(float distance, float damageMultiplier) {
    	
    	if(worldObj.isRemote){
	        for(int i=0; i<40; i++){
	        	double particleX = this.posX - 1.0d + 2*rand.nextDouble();
	        	double particleZ = this.posZ - 1.0d + 2*rand.nextDouble();
	        	// Roundabout way of getting a block instance for the block the hammer is standing on (if any).
				IBlockState block = worldObj.getBlockState(new BlockPos(this.posX, this.posY-2, this.posZ));

				if(block != null){
					worldObj.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, this.posY, particleZ,
							particleX - this.posX, 0, particleZ - this.posZ, Block.getStateId(block));
				}
	        }
    	}else{
			// Just to check the hammer has actually fallen from the sky, rather than the block under it being broken.
    		if(this.fallDistance > 10){
    			EntityLightningBolt entitylightning = new EntityLightningBolt(worldObj, this.posX, this.posY, this.posZ, false);
    			worldObj.addWeatherEffect(entitylightning);
    		}
    	}
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
    	super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setByte("Time", (byte)this.fallTime);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
    	super.readEntityFromNBT(nbttagcompound);
        this.fallTime = nbttagcompound.getByte("Time") & 255;
    }

    @Override
    public boolean isInRangeToRenderDist(double distance) {
    	return true;
    }
}
