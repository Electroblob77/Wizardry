package electroblob.wizardry.tileentity;

import java.lang.ref.WeakReference;
import java.util.UUID;

import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityPlayerSave extends TileEntity {

	/** The entity that created this construct */
	private WeakReference<EntityLivingBase> caster;
	
	/** The UUID of the caster. Note that this is only for loading purposes; during normal updates
	 * the actual entity instance is stored (so that getEntityByUUID is not called constantly),
	 * so this will not always be synced (this is why it is private). */
	private UUID casterUUID;
	
	public TileEntityPlayerSave(EntityLivingBase caster){
		this.caster = new WeakReference(caster);
	}
	
	public TileEntityPlayerSave(){
		
	}

	public void updateEntity(){
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
        casterUUID = UUID.fromString(tagCompound.getString("casterUUID"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
		if(this.getCaster() != null){
        	tagCompound.setString("casterUUID", this.getCaster().getUniqueID().toString());
        }
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
		this.caster = new WeakReference(caster);
	}

}
