package electroblob.wizardry.tileentity;

import electroblob.wizardry.block.BlockVanishingCobweb;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class TileEntityTimer extends TileEntity implements ITickable {

	public int timer = 0;
	public int maxTimer;

	public TileEntityTimer(){

	}

	public TileEntityTimer(int maxTimer){
		this.maxTimer = maxTimer;
	}

	@Override
	public void update(){
		timer++;
		if(!this.world.isRemote){
			// System.out.println("Timer: " + timer + "/" + maxTimer);
		}
		if(timer > maxTimer && !this.world.isRemote){// && this.world.getBlockId(xCoord, yCoord, zCoord) ==
														// Wizardry.magicLight.blockID){
			if(this.getBlockType() instanceof BlockVanishingCobweb){
				// destroyBlock breaks the block as if broken by a player, with sound and particles.
				this.world.destroyBlock(pos, false);
			}else{
				this.world.setBlockToAir(pos);
			}
		}
	}

	public void setLifetime(int lifetime){
		this.maxTimer = lifetime;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		timer = tagCompound.getInteger("timer");
		maxTimer = tagCompound.getInteger("maxTimer");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("timer", timer);
		tagCompound.setInteger("maxTimer", maxTimer);
		return tagCompound;
	}

	@Override
	public final NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new SPacketUpdateTileEntity(pos, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		NBTTagCompound tag = pkt.getNbtCompound();
		readFromNBT(tag);
	}

}
