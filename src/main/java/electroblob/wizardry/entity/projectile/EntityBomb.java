package electroblob.wizardry.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * Same as {@link EntityMagicProjectile}, but with an additional blast multiplier field which is synced and saved to
 * allow for the spread of particles to be changed depending on the blast area.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
public abstract class EntityBomb extends EntityMagicProjectile implements IEntityAdditionalSpawnData {

	/** The entity blast multiplier. This is now synced and saved centrally from {@link EntityBomb}. */
	public float blastMultiplier = 1.0f;

	public EntityBomb(World world){
		super(world);
	}

	public EntityBomb(World world, EntityLivingBase thrower){
		super(world, thrower);
	}

	public EntityBomb(World world, EntityLivingBase thrower, float damageMultiplier, float blastMultiplier){
		super(world, thrower, damageMultiplier);
		this.blastMultiplier = blastMultiplier;
	}

	public EntityBomb(World par1World, double par2, double par4, double par6){
		super(par1World, par2, par4, par6);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer){
		buffer.writeFloat(blastMultiplier);
	}

	@Override
	public void readSpawnData(ByteBuf buffer){
		blastMultiplier = buffer.readFloat();
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
