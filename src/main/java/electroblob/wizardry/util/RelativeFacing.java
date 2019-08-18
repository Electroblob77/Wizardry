package electroblob.wizardry.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;

/**
 * Like {@link EnumFacing}, but relative!
 */
public enum RelativeFacing {

	DOWN("down", -1),
	UP("up", -1),
	FRONT("front", 0),
	BACK("back", 2),
	LEFT("left", 3),
	RIGHT("right", 1);

	public final String name;
	private final int horizontalIndex;

	private static final RelativeFacing[] HORIZONTALS = new RelativeFacing[4];

	RelativeFacing(String name, int horizontalIndex){
		this.name = name;
		this.horizontalIndex = horizontalIndex;
	}

	static {
		for(RelativeFacing facing : values()){
			if(facing.horizontalIndex > -1) HORIZONTALS[facing.horizontalIndex] = facing;
		}
	}

	public static RelativeFacing relativise(EnumFacing absolute, Entity relativeTo){
		if(absolute == EnumFacing.DOWN) return DOWN;
		if(absolute == EnumFacing.UP) return UP;
		EnumFacing look = relativeTo.getAdjustedHorizontalFacing();
		int relativeIndex = absolute.getHorizontalIndex() - look.getHorizontalIndex();
		if(relativeIndex < 0) relativeIndex += 4;
		return HORIZONTALS[relativeIndex];
	}
}
