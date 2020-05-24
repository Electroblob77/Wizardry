package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public class TileEntityPlayerSave extends TileEntity {

	/** The UUID of the caster. As of Wizardry 4.2, this <b>is</b> synced, and rather than storing the caster
	 * instance via a weak reference, it is fetched from the UUID each time it is needed in
	 * {@link TileEntityPlayerSave#getCaster()}. */
	private UUID casterUUID;

	public TileEntityPlayerSave(){}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		this.world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		casterUUID = tagCompound.getUniqueId("casterUUID");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){

		super.writeToNBT(tagCompound);

		if(this.getCaster() != null){
			tagCompound.setUniqueId("casterUUID", casterUUID);
		}

		return tagCompound;
	}

	/**
	 * Returns the EntityLivingBase that created this construct, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this construct simply had no caster in the first place.
	 */
	@Nullable
	public EntityLivingBase getCaster(){

		Entity entity = WizardryUtilities.getEntityByUUID(world, casterUUID);

		if(entity != null && !(entity instanceof EntityLivingBase)){ // Should never happen
			Wizardry.logger.warn("{} has a non-living owner!", this);
			entity = null;
		}

		return (EntityLivingBase)entity;
	}

	public void setCaster(@Nullable EntityLivingBase caster){
		this.casterUUID = caster == null ? null : caster.getUniqueID();
		this.sync();
	}

	@Override
	public final NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
	}

}
