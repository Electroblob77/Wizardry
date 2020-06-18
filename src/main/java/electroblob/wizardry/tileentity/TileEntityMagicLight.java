package electroblob.wizardry.tileentity;

import net.minecraft.nbt.NBTTagCompound;

// TODO: Remove this class entirely, it can just be a TileEntityTimer (see RenderImbuementAltar for a better solution)
@Deprecated
public class TileEntityMagicLight extends TileEntityTimer {

	public int[] randomiser;
	public int[] randomiser2;

	public TileEntityMagicLight(){
		super();
	}

	public TileEntityMagicLight(int maxTimer){
		super(maxTimer);
		randomiser = new int[30];
		randomiser[0] = -1;
		randomiser2 = new int[30];
		randomiser2[0] = -1;
	}

	@Override
	public boolean shouldRenderInPass(int pass){
		return pass == 1;
	}

	@Override
	public void update(){

		if(randomiser.length > 0 && randomiser[0] == -1){
			for(int i = 0; i < randomiser.length; i++){
				randomiser[i] = this.world.rand.nextInt(10);
			}
		}
		if(randomiser2.length > 0 && randomiser2[0] == -1){
			for(int i = 0; i < randomiser2.length; i++){
				randomiser2[i] = this.world.rand.nextInt(10);
			}
		}
		super.update();
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound){
		super.readFromNBT(tagCompound);
		randomiser = tagCompound.getIntArray("randomiser");
		randomiser2 = tagCompound.getIntArray("randomiser2");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){
		super.writeToNBT(tagCompound);
		tagCompound.setIntArray("randomiser", randomiser);
		tagCompound.setIntArray("randomiser2", randomiser2);
		return tagCompound;
	}

}
