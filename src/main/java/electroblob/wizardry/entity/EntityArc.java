package electroblob.wizardry.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityArc extends Entity implements IEntityAdditionalSpawnData {

	public double x1, y1, z1, x2, y2, z2;
	/** The number of ticks the arc lasts for before disappearing. */
	public int lifetime = 3;
	/** False if the arc is locked to an entity, true otherwise. False by default. */
	public boolean freeEnd = false;
	/** A random long value used by the renderer as a seed to generate its vertices from, ensuring they remain the same
	 * across multiple frames. Not synced. */
	public final long seed;

	public EntityArc(World par1World){
		super(par1World);
		seed = this.rand.nextLong();
		this.ignoreFrustumCheck = true;
	}

	public void setEndpointCoords(double x1, double y1, double z1, double x2, double y2, double z2){
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x2 = x2;
		this.y2 = y2;
		this.z2 = z2;
		this.setPosition(x2, y2, z2);
	}

	@Override
	public void onUpdate(){
		if(this.ticksExisted >= lifetime){
			this.setDead();
		}
	}

	protected void entityInit(){
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){
		// Nothing needed here; arc is merely a graphic effect that only exists for a few ticks; as such there is no
		// need to save it.
	}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeDouble(this.x1);
		data.writeDouble(this.y1);
		data.writeDouble(this.z1);
        data.writeBoolean(freeEnd);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		this.x1 = data.readDouble();
		this.y1 = data.readDouble();
		this.z1 = data.readDouble();
		this.freeEnd = data.readBoolean();
	}

}
