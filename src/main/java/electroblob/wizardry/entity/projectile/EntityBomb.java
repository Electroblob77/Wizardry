package electroblob.wizardry.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Same as {@link EntityMagicProjectile}, but with an additional blast multiplier field which is synced and saved to
 * allow for the spread of particles to be changed depending on the blast area.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
public abstract class EntityBomb extends EntityMagicProjectile {

	/** The entity blast multiplier. This is now synced and saved centrally from {@link EntityBomb}. */
	public float blastMultiplier = 1.0f;

	public EntityBomb(World world){
		super(world);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer){
		buffer.writeFloat(blastMultiplier);
		super.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(ByteBuf buffer){
		blastMultiplier = buffer.readFloat();
		super.readSpawnData(buffer);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("blastMultiplier", blastMultiplier);
	}

}
