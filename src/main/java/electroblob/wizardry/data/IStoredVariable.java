package electroblob.wizardry.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Extension of {@link IVariable} which adds NBT read/write methods. Instances of this interface must be
 * registered on load using {@link WizardData#registerStoredVariables(IStoredVariable...)} in order for NBT storage
 * to work. A good place to do this is in spell constructors, if that's where the variable is being used.
 * <p></p>
 * This interface is provided for complex cases that require custom NBT handling of some kind. In most cases,
 * {@link StoredVariable} should be sufficient.
 * <p></p>
 * @param <T> The type of variable stored.
 */
public interface IStoredVariable<T> extends IVariable<T> {

	/** Writes the value to the given NBT tag. */
	void write(NBTTagCompound nbt, T value);

	/** Reads the value from the given NBT tag. */
	T read(NBTTagCompound nbt);

	/**
	 * General-purpose implementation of {@link IStoredVariable}. In most cases, this should be sufficient. This class
	 * also contains a number of static methods for common implementations (primitives, {@code String}, {@code UUID},
	 * {@code BlockPos} and {@code ItemStack}).
	 * <p></p>
	 * @param <T> The type of variable stored.
	 * @param <E> The type of NBT tag the variable will be stored as.
	 */
	class StoredVariable<T, E extends NBTBase> implements IStoredVariable<T> {

		private final String key;
		private final Persistence persistence;

		private final Function<T, E> serialiser;
		private final Function<E, T> deserialiser;

		private boolean synced;

		private BiFunction<EntityPlayer, T, T> ticker;

		/**
		 * Creates a new {@code StoredVariable} with the given key and serialisation behaviour.
		 * @param key The string key used to write the value to NBT (should be unique). This serves no other purpose.
		 * @param serialiser A function used to write the value to NBT.
		 * @param deserialiser A function used to read the value from NBT.
		 */
		public StoredVariable(String key, Function<T, E> serialiser, Function<E, T> deserialiser, Persistence persistence){
			this.key = key;
			this.serialiser = serialiser;
			this.deserialiser = deserialiser;
			this.persistence = persistence;
			this.ticker = (p, t) -> t; // Initialise this with a do-nothing function, can be overwritten later
		}

		/**
		 * Replaces this variable's update method with the given update function. <i>Beware of auto-unboxing of
		 * primitive types! For lambda expressions, check the second parameter isn't null before operating on it.
		 * For method references, do not reference a method that takes a primitive type. Otherwise, this will cause
		 * a (difficult to debug) {@link NullPointerException} if the key was not stored.</i>
		 * @param ticker A {@link BiFunction} specifying the actions to be performed on this variable each tick. The
		 *               {@code BiFunction} returns the new value for this variable.
		 * @return This {@code StoredVariable} object, allowing this method to be chained onto object creation.
		 */
		public StoredVariable<T, E> withTicker(BiFunction<EntityPlayer, T, T> ticker){
			this.ticker = ticker;
			return this;
		}

		/**
		 * Adds synchronisation to this variable, meaning it will be sent to clients whenever {@link WizardData#sync()}
		 * is called (this always happens on player login, but other than that you'll need to do it yourself).
		 * @return This {@code StoredVariable} object, allowing this method to be chained onto object creation.
		 */
		public StoredVariable<T, E> setSynced(){
			this.synced = true;
			return this;
		}

		@Override
		public void write(NBTTagCompound nbt, T value){
			if(value != null) nbt.setTag(key, serialiser.apply(value));
		}

		@Override
		@SuppressWarnings("unchecked") // Can't check it due to type erasure
		public T read(NBTTagCompound nbt){
			// A system allowing any kind of variable to be stored on the fly cannot be made without casting somewhere.
			// However, doing it like this means we only cast once, below, and proper regulation of access means we
			// can effectively guarantee the cast is safe.
			return nbt.hasKey(key) ? deserialiser.apply((E)nbt.getTag(key)) : null; // Still gotta check it ain't null
		}

		@Override
		public T update(EntityPlayer player, T value){
			return ticker.apply(player, value);
		}

		@Override
		public boolean isPersistent(boolean respawn){
			return respawn ? persistence.persistsOnRespawn() : persistence.persistsOnDimensionChange();
		}

		@Override
		public boolean isSynced(){
			return synced;
		}

		@Override
		public void write(ByteBuf buf, T value){
			if(!synced) return;
			NBTTagCompound nbt = new NBTTagCompound();
			write(nbt, value);
			ByteBufUtils.writeTag(buf, nbt); // Sure, it's not super-efficient, but it's by far the simplest way!
		}

		@Override
		public T read(ByteBuf buf){
			if(!synced) return null; // Better to check in here because this method should only read if it needs to
			NBTTagCompound nbt = ByteBufUtils.readTag(buf);
			if(nbt == null) return null;
			return read(nbt);
		}

		// Standard implementations to shorten common usages a bit

		/** Creates a new {@code StoredVariable} for a byte value with the given key. */
		public static StoredVariable<Byte, NBTTagByte> ofByte(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagByte::new, NBTTagByte::getByte, persistence);
		}

		/** Creates a new {@code StoredVariable} for a boolean value with the given key. As per Minecraft's usual
		 * NBT conventions, the boolean value is stored as an {@link NBTTagByte} (1 = true, 0 = false). */
		public static StoredVariable<Boolean, NBTTagByte> ofBoolean(String key, Persistence persistence){
			return new StoredVariable<>(key, b -> new NBTTagByte((byte)(b?1:0)), t -> t.getByte() == 1, persistence);
		}

		/** Creates a new {@code StoredVariable} for an integer value with the given key. */
		public static StoredVariable<Integer, NBTTagInt> ofInt(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagInt::new, NBTTagInt::getInt, persistence);
		}

		// I'm not going to do byte and long arrays here, if you really need them it's pretty obvious how to do it

		/** Creates a new {@code StoredVariable} for an integer array value with the given key. */
		public static StoredVariable<int[], NBTTagIntArray> ofIntArray(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagIntArray::new, NBTTagIntArray::getIntArray, persistence);
		}

		/** Creates a new {@code StoredVariable} for a float value with the given key. */
		public static StoredVariable<Float, NBTTagFloat> ofFloat(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagFloat::new, NBTTagFloat::getFloat, persistence);
		}

		/** Creates a new {@code StoredVariable} for a double value with the given key. */
		public static StoredVariable<Double, NBTTagDouble> ofDouble(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagDouble::new, NBTTagDouble::getDouble, persistence);
		}

		/** Creates a new {@code StoredVariable} for a short value with the given key. */
		public static StoredVariable<Short, NBTTagShort> ofShort(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagShort::new, NBTTagShort::getShort, persistence);
		}

		/** Creates a new {@code StoredVariable} for a long value with the given key. */
		public static StoredVariable<Long, NBTTagLong> ofLong(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagLong::new, NBTTagLong::getLong, persistence);
		}

		/** Creates a new {@code StoredVariable} for a {@link String} value with the given key. */
		public static StoredVariable<String, NBTTagString> ofString(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTTagString::new, NBTTagString::getString, persistence);
		}

		/** Creates a new {@code StoredVariable} for a {@link BlockPos} value with the given key. */
		public static StoredVariable<BlockPos, NBTTagCompound> ofBlockPos(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTUtil::createPosTag, NBTUtil::getPosFromTag, persistence);
		}

		/** Creates a new {@code StoredVariable} for a {@link UUID} value with the given key. */
		public static StoredVariable<UUID, NBTTagCompound> ofUUID(String key, Persistence persistence){
			return new StoredVariable<>(key, NBTUtil::createUUIDTag, NBTUtil::getUUIDFromTag, persistence);
		}

		/** Creates a new {@code StoredVariable} for an {@link ItemStack} value with the given key. */
		public static StoredVariable<ItemStack, NBTTagCompound> ofItemStack(String key, Persistence persistence){
			return new StoredVariable<>(key, ItemStack::serializeNBT, ItemStack::new, persistence);
		}

		/** Creates a new {@code StoredVariable} for an {@link NBTTagCompound} value with the given key. */
		public static StoredVariable<NBTTagCompound, NBTTagCompound> ofNBT(String key, Persistence persistence){
			return new StoredVariable<>(key, t -> t, t -> t, persistence); // No conversion required!
		}

		// Neither of these work just ignore them

//		/** Creates a new {@code StoredVariable} for an {@link NBTTagCompound} value with the given key which stores the
//		 * given {@code IVariable} for an entity. Entities cannot be stored directly as an {@code IStoredVariable}
//		 * because they require a world instance on construction. */
//		@SuppressWarnings("unchecked") // Can't check it due to type erasure
//		public static <T extends Entity> StoredVariable<NBTTagCompound, NBTTagCompound> ofNBTForEntity(String key, Persistence persistence, IVariable<T> toStore){
//			return ofNBT(key, persistence).withTicker((p, t) -> {
//				if(WizardData.get(p) != null){
//					try{
//						T e = (T)EntityList.createEntityByIDFromName(new ResourceLocation(t.getString("entityType")), p.world);
//						e.readFromNBT(t);
//						WizardData.get(p).setVariable(toStore, e);
//					}catch(ClassCastException e){
//						Wizardry.logger.error("Error reading entity from NBT: entity not of expected type", e);
//					}
//				}
//				return t;
//			});
//		}

//		/** Creates a new {@code StoredVariable} for an {@link Entity} value with the given key. The returned
//		 * {@code StoredVariable} has a ticker which extracts the entity from the given; this functionality will need to be
//		 * replicated in any replacement ticker function. */
//		@SuppressWarnings("unchecked") // Can't check it due to type erasure
//		public static <T extends Entity> StoredVariable<T, NBTTagCompound> ofEntity(String key, Persistence persistence, IVariable<NBTTagCompound> storage){
//			// Well this is horrible
//			return new IStoredVariable.StoredVariable<>(key,
//					(T e) -> {
//						NBTTagCompound nbt = new NBTTagCompound();
//						nbt.setString("entityType", EntityList.getKey(e).toString());
//						e.writeToNBT(nbt);
//						return nbt;
//					},
//					t -> null, persistence)
//					.withTicker((p, e) -> {
//						if(e == null){
//							try{
//								NBTTagCompound nbt = WizardData.get(p).getVariable(storage);
//								if(nbt == null) return null;
//								e = (T)EntityList.createEntityByIDFromName(new ResourceLocation(nbt.getString("entityType")), p.world);
//								e.readFromNBT(nbt);
//								return e;
//							}catch(ClassCastException x){
//								Wizardry.logger.error("Error reading stored variable from NBT: entity not of expected type", x);
//							}
//						}
//						return null;
//					});
//		}
	}
}
