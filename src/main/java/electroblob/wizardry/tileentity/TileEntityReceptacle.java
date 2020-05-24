package electroblob.wizardry.tileentity;

import electroblob.wizardry.constants.Element;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class TileEntityReceptacle extends TileEntity {

	private Element element;

	public TileEntityReceptacle(){
		this(null);
	}

	public TileEntityReceptacle(Element element){
		this.element = element;
	}

	public Element getElement(){
		return element;
	}

	public void setElement(Element element){
		this.element = element;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		super.writeToNBT(compound);
		compound.setInteger("Element", element == null ? -1 : element.ordinal());
		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound){
		super.readFromNBT(compound);
		int i = compound.getInteger("Element");
		element = i == -1 ? null : Element.values()[i];
	}

	@Override
	public NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
	}

}
