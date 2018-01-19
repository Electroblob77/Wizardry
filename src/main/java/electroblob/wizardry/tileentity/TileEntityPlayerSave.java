package electroblob.wizardry.tileentity;

import java.lang.ref.WeakReference;
import java.util.UUID;

import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntityPlayerSave extends TileEntity implements ITickable {

	/** The entity that created this construct */
	private WeakReference<EntityLivingBase> caster;
	
	/** The UUID of the caster. Note that this is only for loading purposes; during normal updates
	 * the actual entity instance is stored (so that getEntityByUUID is not called constantly),
	 * so this will not always be synced (this is why it is private). */
	private UUID casterUUID;
	
	public TileEntityPlayerSave(EntityLivingBase caster){
		this.caster = new WeakReference<EntityLivingBase>(caster);
	}
	
	public TileEntityPlayerSave(){
		
	}
	
	@Override
	public void update(){
		if(this.getCaster() == null && this.casterUUID != null){
			Entity entity = WizardryUtilities.getEntityByUUID(worldObj, casterUUID);
			if(entity instanceof EntityLivingBase){
				this.caster = new WeakReference<EntityLivingBase>((EntityLivingBase)entity);
			}
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        casterUUID = tagCompound.getUniqueId("casterUUID");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
    	
        super.writeToNBT(tagCompound);
        
		if(this.getCaster() != null){
        	tagCompound.setUniqueId("casterUUID", this.getCaster().getUniqueID());
        }
		
		return tagCompound;
    }
    
    /**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the
	 * entity may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported
	 * to another dimension, or this construct simply had no caster in the first place.
	 */
	public EntityLivingBase getCaster() {
		return caster == null ? null : caster.get();
	}
	
	public void setCaster(EntityLivingBase caster){
		this.caster = new WeakReference<EntityLivingBase>(caster);
	}

}
