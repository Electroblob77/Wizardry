package electroblob.wizardry.entity.construct;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityArc;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S14PacketEntity.S15PacketEntityRelMove;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class EntityHammer extends EntityMagicConstruct
{
    /** How long the hammer has been falling for. */
    public int fallTime;

    public EntityHammer(World par1World)
    {
        super(par1World);
        this.setSize(1.0f, 1.9F);
        this.noClip = false;
    }

    public EntityHammer(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, float damageMultiplier)
    {
        super(world, x, y, z, caster, lifetime, damageMultiplier);
        this.setSize(1.1f, 1.9F);
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        this.noClip = false;
    }
    
    /**
     * Returns true if the entity is on fire. Used by render to add the fire effect on rendering.
     */
    public boolean isBurning()
    {
        return false;
    }

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return true;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate(){
    	
    	super.onUpdate();
    	
    	if(this.ticksExisted % 20 == 1 && !this.onGround && worldObj.isRemote){
    		// Though this sound does repeat, it stops when it hits the ground.
    		Wizardry.proxy.playMovingSound(this, "wizardry:electricityb", 3.0f, 1.0f, false);
		}
    	
    	if(this.worldObj.isRemote && this.ticksExisted % 3 == 0){
			Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX - 0.5d + rand.nextDouble(), this.posY + 2*rand.nextDouble(), this.posZ - 0.5d + rand.nextDouble(), 0, 0, 0, 3);
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
								arc.setEndpointCoords(this.posX, this.posY+1, this.posZ,
										target.posX, target.posY + target.height/2, target.posZ);
								worldObj.spawnEntityInWorld(arc);
							}else{
								for(int j=0;j<8;j++){
									Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, target.posX + worldObj.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + worldObj.rand.nextFloat()*2 - 1, target.posZ + worldObj.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					    			worldObj.spawnParticle("largesmoke", target.posX + rand.nextFloat(), WizardryUtilities.getEntityFeetPos(target) + target.height/2 + rand.nextFloat(), target.posZ + rand.nextFloat(), 0, 0, 0);
					    		}
							}
							
							worldObj.playSoundAtEntity(target, "wizardry:arc", 1.0F, rand.nextFloat() * 0.4F + 1.5F);
							
							if(this.getCaster() != null){
								target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.SHOCK), 6*damageMultiplier);
							}else{
								target.attackEntityFrom(DamageSource.magic, 6*damageMultiplier);
							}
						}
					}
	            }
	    	}
    	}
    }
    
    public void despawn(){
    	
		this.playSound("random.explode", 1.0F, 1.0f);
		
		if(this.worldObj.isRemote){
			this.worldObj.spawnParticle("largeexplode", this.posX, this.posY, this.posZ, 0, 0, 0);
		}
		
		super.despawn();
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void fall(float par1){
        
    	if(worldObj.isRemote){
	        for(int i=0; i<40; i++){
	        	double particleX = this.posX - 1.0d + 2*rand.nextDouble();
	        	double particleZ = this.posZ - 1.0d + 2*rand.nextDouble();
	        	// Roundabout way of getting a block instance for the block the hammer is standing on (if any).
				Block block = worldObj.getBlock((int)this.posX, (int)this.posY-2, (int)this.posZ);

				if(block != null){
					Wizardry.proxy.spawnDigParticle(worldObj, particleX, this.posY, particleZ,
							particleX - this.posX, 0, particleZ - this.posZ, block);
				}
	        }
	        
    	}else{
			
    		// For some reason, the hammer falls again on world reload, but only by about 1 block.
    		if(this.fallDistance > 10){
    			
    			// For some reason, the hammer 'lands' one block above the ground first time around - on the SERVER as well.
    			// Edit: not any more, it seems... maybe because the yOffset thing got deleted.
    			//this.setPosition(this.posX, this.posY-0.8, this.posZ);

    			EntityLightningBolt entitylightning = new EntityLightningBolt(worldObj, this.posX, this.posY, this.posZ);
    			worldObj.addWeatherEffect(entitylightning);
    			
				// The last three parameters are the difference in position between server and client. What this is doing
				// is essentially hijacking the vanilla packet to force the hammer into the right position. (not sure why,
				// but the packet divides these numbers by 32.)
				S15PacketEntityRelMove packet = new S15PacketEntityRelMove(this.getEntityId(), (byte)0, (byte)-65, (byte)0);
				
				if(this.worldObj instanceof WorldServer){
					EntityTracker et = ((WorldServer)this.worldObj).getEntityTracker();
					// This sends a packet to all players tracking the entity.
					et.func_151248_b(this, packet); 
				}
				//packet.yPosition = -65;
				
				//PacketDispatcher.sendPacketToAllAround(this.posX, this.posY, this.posZ, 256, this.dimension, packet);
				//PacketDispatcher.sendPacketToAllPlayers(packet);
    		}
    	}
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound nbttagcompound)
    {
    	super.writeEntityToNBT(nbttagcompound);
        nbttagcompound.setByte("Time", (byte)this.fallTime);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound nbttagcompound)
    {
    	super.readEntityFromNBT(nbttagcompound);
        this.fallTime = nbttagcompound.getByte("Time") & 255;
        // Undoes the manual position change on world reload to prevent the hammer being in the ground.
		//this.setPosition(this.posX, this.posY+0.8, this.posZ);
    }

    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 1.0F;
    }

	/**
     * Checks using a Vec3d to determine if this entity is within range of that vector to be rendered. Args: vec3D
     */
    public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
    {
        return true;
    }
}
