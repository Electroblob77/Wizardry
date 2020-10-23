package electroblob.wizardry.entity.construct;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Extension of {@link EntityMagicConstruct} that implements saving and loading of size (blast) multipliers. What
 * the entity actually does with the multiplier value is up to subclasses to define; however, by default this class
 * scales the entity bounding box according to the size multiplier (this can be controlled by overriding
 * {@link EntityScaledConstruct#shouldScaleWidth()} and {@link EntityScaledConstruct#shouldScaleHeight()}).
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public abstract class EntityScaledConstruct extends EntityMagicConstruct {

	/** The size multiplier for this construct, usually determined by the blast modifier the spell was cast with. */
	protected float sizeMultiplier = 1;

	public EntityScaledConstruct(World world){
		super(world);
	}

	public float getSizeMultiplier(){
		return sizeMultiplier;
	}

	public void setSizeMultiplier(float sizeMultiplier){
		this.sizeMultiplier = sizeMultiplier;
		setSize(shouldScaleWidth() ? width * sizeMultiplier : width, shouldScaleHeight() ? height * sizeMultiplier : height);
	}

	/** Returns true if the width of this entity's bounding box should be scaled by the size multiplier on creation. */
	protected boolean shouldScaleWidth(){
		return true;
	}

	/** Returns true if the height of this entity's bounding box should be scaled by the size multiplier on creation. */
	protected boolean shouldScaleHeight(){
		return true;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt){
		super.readEntityFromNBT(nbt);
		setSizeMultiplier(nbt.getFloat("sizeMultiplier"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt){
		super.writeEntityToNBT(nbt);
		nbt.setFloat("sizeMultiplier", sizeMultiplier);

	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		setSizeMultiplier(data.readFloat()); // Set the width correctly on the client side
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeFloat(sizeMultiplier);
	}
}
