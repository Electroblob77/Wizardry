package electroblob.wizardry.tileentity;

import electroblob.wizardry.block.BlockThorns;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

public class TileEntityPlayerSaveTimed extends TileEntityPlayerSave implements ITickable {

	public int timer = 0;
	public int maxTimer;

	public TileEntityPlayerSaveTimed(){
		this.maxTimer = 600;
	}

	@Override
	public void update(){

		timer++;

		if(timer > maxTimer && !this.world.isRemote){
			this.world.destroyBlock(pos, false);
		}

		if(timer % 2 == 0 && world.getBlockState(pos).getValue(BlockThorns.AGE) < BlockThorns.GROWTH_STAGES - 1){
			world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockThorns.AGE, world.getBlockState(pos).getValue(BlockThorns.AGE) + 1), 2);
			world.setBlockState(pos.up(), world.getBlockState(pos.up()).withProperty(BlockThorns.AGE, world.getBlockState(pos.up()).getValue(BlockThorns.AGE) + 1), 2);
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

}
