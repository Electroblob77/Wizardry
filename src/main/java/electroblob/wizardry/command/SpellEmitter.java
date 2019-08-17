package electroblob.wizardry.command;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.SpellEmitterData;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

/**
 * A {@code SpellEmitter} represents a continuous spell being cast from a position via commands.
 *
 * @since Wizardry 4.2
 * @author Electroblob
 */
public class SpellEmitter implements ITickable {

	protected final Spell spell;
	protected World world;
	protected final double x, y, z;
	protected final EnumFacing direction;
	protected final int duration;
	protected final SpellModifiers modifiers;

	protected int castingTick = 0;
	protected boolean needsRemoving = false;

	protected SpellEmitter(Spell spell, World world, double x, double y, double z, EnumFacing direction, int duration, SpellModifiers modifiers){
		this.spell = spell;
		this.world = world;
		this.duration = duration;
		this.x = x;
		this.y = y;
		this.z = z;
		this.direction = direction;
		this.modifiers = modifiers;
	}

	/** Marks this spell emitter to be removed next tick. */
	protected void markForRemoval(){
		this.needsRemoving = true;
	}

	/** Returns whether this spell emitter is marked for removal. */
	public boolean needsRemoving(){
		return needsRemoving;
	}

	/** Returns the {@link SpellCastEvent.Source} that should be used for events fired by this spell emitter. */
	protected SpellCastEvent.Source getSource(){
		return SpellCastEvent.Source.COMMAND;
	}

	/** Sets this spell emitter's world. This should only be used on the client side when the world has not yet been
	 * set, otherwise the world will not be changed and a warning will be printed to the console. */
	public void setWorld(World world){
		if(world.isRemote && this.world == null){
			this.world = world;
		}else{
			Wizardry.logger.warn("Tried to change the world for a spell emitter, this shouldn't happen!");
		}
	}

	@Override
	public void update(){

		if(castingTick < duration){

			if(!MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(getSource(), spell, world, x, y, z, direction, modifiers, castingTick))){

				if(spell.cast(world, x, y, z, direction, castingTick, duration, modifiers)){
					if(castingTick == 0) MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(getSource(), spell, world, x, y, z, direction, modifiers));
					castingTick++;
					return;
				}
			}
		}
		// If the time ran out or the spell failed, interrupt spell casting
		MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Finish(getSource(), spell, world, x, y, z, direction, modifiers, castingTick));
		spell.finishCasting(world, null, x, y, z, direction, duration, modifiers);
		markForRemoval();
	}

	/** Writes this {@code SpellEmitter} to the given ByteBuf. */
	public void write(ByteBuf buf){
		buf.writeInt(spell.networkID());
		buf.writeDouble(x);
		buf.writeDouble(y);
		buf.writeDouble(z);
		buf.writeInt(direction.getIndex());
		// This is sent through as the duration, meaning castingTick always starts at zero client-side, which is
		// important for sounds to work correctly. As a consequence, the client's castingTick will be different to the
		// server value if the player changes dimension or re-logs. However, since this is pretty uncommon anyway I
		// think it's an ok compromise.
		buf.writeInt(duration - castingTick);
		modifiers.write(buf);
	}

	/** Reads a {@code SpellEmitter} from the given ByteBuf and returns it. */
	public static SpellEmitter read(ByteBuf buf){

		Spell spell = Spell.byNetworkID(buf.readInt());
		double x = buf.readDouble();
		double y = buf.readDouble();
		double z = buf.readDouble();
		EnumFacing direction = EnumFacing.byIndex(buf.readInt());
		int duration = buf.readInt();
		SpellModifiers modifiers = new SpellModifiers();
		modifiers.read(buf);

		return new SpellEmitter(spell, null, x, y, z, direction, duration, modifiers);
	}

	// INBTSerializable is annoying, it doesn't allow you to have final fields

	/** Returns a new {@link NBTTagCompound} representing this {@code SpellEmitter}. */
	public NBTTagCompound toNBT(){

		NBTTagCompound nbt = new NBTTagCompound();

		nbt.setInteger("spell", spell.metadata());
		nbt.setDouble("x", x);
		nbt.setDouble("y", y);
		nbt.setDouble("z", z);
		nbt.setInteger("direction", direction.getIndex());
		nbt.setInteger("duration", duration);
		nbt.setTag("modifiers", modifiers.toNBT());
		nbt.setInteger("castingTick", castingTick);

		return nbt;
	}

	/** Creates a new {@code SpellEmitter} from the given {@link NBTTagCompound} and returns it. */
	public static SpellEmitter fromNBT(World world, NBTTagCompound nbt){

		Spell spell = Spell.byMetadata(nbt.getInteger("spell"));
		double x = nbt.getDouble("x");
		double y = nbt.getDouble("y");
		double z = nbt.getDouble("z");
		EnumFacing direction = EnumFacing.byIndex(nbt.getInteger("direction"));
		int duration = nbt.getInteger("duration");
		SpellModifiers modifiers = SpellModifiers.fromNBT(nbt.getCompoundTag("modifiers"));
		int castingTick = nbt.getInteger("castingTick");

		SpellEmitter emitter = new SpellEmitter(spell, world, x, y, z, direction, duration, modifiers);
		emitter.castingTick = castingTick;
		return emitter;
	}

	/**
	 * Creates a new {@code SpellEmitter} and adds it to the list of active emitters in {@link SpellEmitterData}. <i>
	 * This method does not perform any syncing.</i>
	 *
	 * @param spell The spell to be cast
	 * @param world The world in which to cast the spell
	 * @param x The x-coordinate of the spell origin
	 * @param y The y-coordinate of the spell origin
	 * @param z The z-coordinate of the spell origin
	 * @param direction The direction to cast the spell in
	 * @param duration The number of ticks to cast the spell for
	 * @param modifiers The {@link SpellModifiers} for the spell
	 */
	public static void add(Spell spell, World world, double x, double y, double z, EnumFacing direction, int duration, SpellModifiers modifiers){
		if(spell.isContinuous){
			if(duration <= 0) Wizardry.logger.warn("Adding a spell emitter with negative or zero duration!");
			SpellEmitterData.get(world).add(new SpellEmitter(spell, world, x, y, z, direction, duration, modifiers));
		}else{
			Wizardry.logger.warn("Tried to add a non-continuous spell emitter for spell {}", spell.getRegistryName());
		}
	}

}
