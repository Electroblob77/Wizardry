package electroblob.wizardry.data;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.packet.PacketDispenserCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.None;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Base class for {@link DispenserCastingData}. Originally this was written because command blocks had a similar system,
 * but that was later removed in favour of spell emitters - however, this class has been kept so that others can use it
 * for different spellcasting blocks if they wish.
 *
 * @since Wizardry 4.2
 * @author Electroblob
 */
public abstract class BlockCastingData<T extends TileEntity> implements INBTSerializable<NBTTagCompound> {

	/** The tile entity this BlockCastingData instance belongs to. */
	protected final T tileEntity;

	/** The continuous spell this tile entity is currently casting, or the {@link None} spell if it is not casting. */
	protected Spell spell;
	/** The coordinates of the current continuous spell's origin. */
	protected double x, y, z;
	/** The time for which this tile entity has been casting a continuous spell. Increments by 1 each tick. */
	protected int castingTick;
	/** SpellModifiers object for the current continuous spell. */
	protected SpellModifiers modifiers;

	public BlockCastingData(T tileEntity){
		this.tileEntity = tileEntity;
		this.spell = Spells.none;
		this.modifiers = new SpellModifiers();
		this.castingTick = 0;
	}

	/** Returns whether this tile entity is currently casting a continuous spell. */
	public boolean isCasting(){
		return this.spell != null && this.spell != Spells.none;
	}

	/** Returns the continuous spell this tile entity is currently casting, or the {@link None} spell if it isn't
	 * casting anything. */
	public Spell currentlyCasting(){
		return spell;
	}

	/** Starts casting the given continuous spell from this tile entity. */
	protected void startCasting(Spell spell, double x, double y, double z, SpellModifiers modifiers){

		if(!spell.isContinuous){
			Wizardry.logger.warn("Tried to start casting a continuous spell from a tile entity, but the given spell was not continuous!");
			return;
		}

		this.spell = spell;
		this.x = x;
		this.y = y;
		this.z = z;
		this.castingTick = 0;
		this.modifiers = modifiers;
	}

	/** Stops casting the current spell. */
	protected void stopCasting(){
		this.spell = Spells.none;
		this.castingTick = 0;
		this.modifiers.reset();
	}

	/** Stops casting the current spell and sends a packet to clients to update them. If called client-side, this just
	 * delegates to {@link BlockCastingData#stopCasting()}. */
	protected void stopCastingAndNotify(){

		stopCasting();

		if(!tileEntity.getWorld().isRemote){
			IMessage msg = new PacketDispenserCastSpell.Message(x, y, z, getDirection(), tileEntity.getPos(), spell, 0, modifiers);
			WizardryPacketHandler.net.sendToDimension(msg, tileEntity.getWorld().provider.getDimension());
		}
	}

	/** Called once per tick to update the block casting data. <b>This is not called automatically</b>, subclasses must
	 * do so using their own tick event handlers. */
	protected void update(){

		if(this.tileEntity.isInvalid()){
			return;
		}

		if(this.isCasting() && this.spell.isContinuous){

			// If the dispenser has stopped receiving power, the spell stops immediately.
			if(!shouldContinueCasting()){
				this.stopCasting(); // This seems to work fine on both sides, so no point sending a packet
				return;
			}

			EnumFacing direction = getDirection();

			if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(getSource(), spell, tileEntity.getWorld(),
					x, y, z, direction, modifiers, castingTick))){
				// When the event is canceled client-side, this will stop the spell on the client only, as specified in
				// the javadoc for SpellCastEvent.Tick.
				this.stopCastingAndNotify();
				return;
			}

			this.spell.cast(tileEntity.getWorld(), x, y, z, direction, castingTick, -1, modifiers);

			castingTick++;

		}else{
			this.castingTick = 0;
		}
	}

	/** Returns the direction to cast the current spell in. */
	protected abstract EnumFacing getDirection();

	/** Returns the source of spells cast from this block. */
	protected abstract SpellCastEvent.Source getSource();

	/** Called each tick during continuous spell casting to determine if the spell should continue or stop. */
	protected abstract boolean shouldContinueCasting();

	@Override
	public NBTTagCompound serializeNBT(){

		NBTTagCompound nbt = new NBTTagCompound();

		nbt.setInteger("spell", spell.metadata());
		nbt.setInteger("castingTick", castingTick);
		nbt.setTag("modifiers", modifiers.toNBT());

		return nbt;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt){

		if(nbt != null){

			this.spell = Spell.byMetadata(nbt.getInteger("spell"));
			this.castingTick = nbt.getInteger("castingTick");
			this.modifiers = SpellModifiers.fromNBT(nbt.getCompoundTag("modifiers"));
		}
	}

	// The two methods below broke EVERYTHING, somehow they made the server think it was the client...

//	// Only fired server-side
//	@SubscribeEvent
//	public static void onWorldTickEvent(TickEvent.WorldTickEvent event){
//
//		if(!event.world.isRemote && event.phase == TickEvent.Phase.END){
//			// This will fire once for each dimension, but since we want dispenser-casting to work in all dimensions,
//			// this is correct (the loaded tile entity list will of course be different in each case.
//			this.update();
//		}
//	}
//
//	// Only called client-side
//	@SubscribeEvent
//	public static void onClientTickEvent(TickEvent.ClientTickEvent event){
//		World world = net.minecraft.client.Minecraft.getMinecraft().world;
//		if(event.phase == TickEvent.Phase.END && !net.minecraft.client.Minecraft.getMinecraft().isGamePaused()
//				&& world != null){
//			this.update();
//		}
//	}

}
