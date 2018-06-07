package electroblob.wizardry.entity.construct;

import java.lang.ref.WeakReference;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBubble extends EntityMagicConstruct {
	
	public boolean isDarkOrb;
	
	private WeakReference<EntityLivingBase> rider;
	
	public EntityBubble(World world){
		super(world);
	}

	public EntityBubble(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, boolean isDarkOrb, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
        //this.setSize(0.1f, 0.1f);
		this.isDarkOrb = isDarkOrb;
	}
	
	@Override
    public double getMountedYOffset()
    {
        return 0.1;
    }
	
	@Override
	public boolean shouldRiderSit(){
		return false;
	}
    
	public void onUpdate(){
		
		super.onUpdate();
		
		// Synchronises the rider field
		if((this.rider == null || this.rider.get() == null) && this.riddenByEntity != null && !this.riddenByEntity.isDead){
			this.rider = new WeakReference(this.riddenByEntity);
		}
		
		// Prevents dismounting
		if(this.riddenByEntity == null && this.rider != null && this.rider.get() != null && !this.rider.get().isDead){
			this.rider.get().mountEntity(this);
		}
		
		// Stops the bubble bursting instantly.
		if(this.ticksExisted < 1 && !isDarkOrb) ((EntityLivingBase)this.riddenByEntity).hurtTime = 0;
		
		this.moveEntity(0, 0.03, 0);
		
		if(isDarkOrb){
			
			if(this.riddenByEntity != null && this.ticksExisted % 30 == 0){
				if(this.getCaster() != null){
					this.riddenByEntity.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.MAGIC), 1*damageMultiplier);
				}else{
					this.riddenByEntity.attackEntityFrom(DamageSource.magic, 1*damageMultiplier);
				}
			}
			
			for(int i=0; i<5; i++){
	            this.worldObj.spawnParticle("portal", this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height + 0.5d, this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, (this.rand.nextDouble() - 0.5D) * 2.0D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
	        }
			if(lifetime - this.ticksExisted == 75){
				this.worldObj.playSoundAtEntity(this, "portal.trigger", 1.5f, 1.0f);
			}else if(this.ticksExisted % 100 == 1 && this.ticksExisted < 150){
				this.worldObj.playSoundAtEntity(this, "portal.portal", 1.5f, 1.0f);
			}
		}
		
		// Bubble bursts if the entity is hurt (see event handler) or killed, or if the bubble has existed for more than 10 seconds.
		if(this.riddenByEntity == null && this.ticksExisted > 1){
			if(!this.isDarkOrb) this.worldObj.playSoundAtEntity(this, "random.pop", 1.5f, 1.0f);
			this.setDead();
		}
	}
	
	@Override
	public void despawn(){
		if(this.riddenByEntity != null){
			((EntityLivingBase)this.riddenByEntity).dismountEntity(this);
		}
		if(!this.isDarkOrb) this.worldObj.playSoundAtEntity(this, "random.pop", 1.5f, 1.0f);
		super.despawn();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		isDarkOrb = nbttagcompound.getBoolean("isDarkOrb");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setBoolean("isDarkOrb", isDarkOrb);
	}

	@Override
	public void writeSpawnData(ByteBuf data) {
		super.writeSpawnData(data);
		data.writeBoolean(this.isDarkOrb);
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		super.readSpawnData(data);
		this.isDarkOrb = data.readBoolean();
	}

}
