package electroblob.wizardry.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.concurrent.Immutable;

/** Simple wrapper class that stores a {@link BlockPos} and an integer dimension ID. */
@Immutable
public class Location {

	public final BlockPos pos;
	public final int dimension;

	public Location(BlockPos pos, int dimension){
		this.pos = pos;
		this.dimension = dimension;
	}

	/** Returns true if the given location refers to the same coordinates and dimension as this one. */
	@Override
	public boolean equals(Object that){

		if(this == that) return true;

		if(that instanceof Location){
			return this.pos.equals(((Location)that).pos) && this.dimension == ((Location)that).dimension;
		}

		return false;
	}

	/** Creates and returns an {@link NBTTagCompound} representing this location. The returned compound tag is the
	 * same as that returned by {@link NBTUtil#createPosTag(BlockPos)}, but with an extra "dimension" key. */
	public NBTTagCompound toNBT(){
		NBTTagCompound nbt = NBTUtil.createPosTag(pos);
		nbt.setInteger("dimension", dimension);
		return nbt;
	}

	/** Creates a new {@code Location} from the given {@link NBTTagCompound}. The given compound tag should be the
	 * same as that returned by {@link NBTUtil#createPosTag(BlockPos)}, but with an extra "dimension" key. */
	public static Location fromNBT(NBTTagCompound nbt){
		return new Location(NBTUtil.getPosFromTag(nbt), nbt.getInteger("dimension"));
	}
}
