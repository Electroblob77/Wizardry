package electroblob.wizardry.data;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

import java.util.function.BiFunction;

/**
 * Instances of this interface act as keys which allow data of any type to be stored in {@link WizardData} at
 * runtime. This means spells (or anything else, for that matter) may define their own variables to be stored
 * with the player and handle those variables themselves. This prevents {@code WizardData} from being cluttered
 * with spell-specific fields and allows addon mods to leverage {@code WizardData} for their own spells or other data,
 * rather than defining their own capability. This system is somewhat similar to {@code DataManager}.
 * <p></p>
 * Instances should be created once and stored statically (or pseudo-statically) in some sensible location, such
 * as a spell class. They can then be used as keys to access the values themselves via {@link WizardData}.
 * Encapsulation can also be achieved by simply restricting access to the keys.
 * <p></p>
 * @param <T> The type of variable stored.
 */
public interface IVariable<T> {

	// To reiterate, instances of this interface are KEYS. They are both accessors for the data and define how
	// it is stored and handled, but they DO NOT CONTAIN THE ACTUAL DATA.
	// Only one instance exists for each thing to be stored, and is shared across instances of WizardData.

	/** Convenience method that allows this variable to define tick behaviour. This is particularly useful for
	 * trivial operations such as decrementing a value, for which a dedicated event handling method would be
	 * unnecessarily verbose. */
	T update(EntityPlayer player, T value);

	/**
	 * Returns whether this variable persists when data is copied.
	 * @param respawn True if the player died and is respawning, false if they are just travelling between dimensions.
	 * @return True if the variable should be copied over, false if not.
	 */
	boolean isPersistent(boolean respawn);

	/**
	 * Returns whether this variable requires syncing with clients.
	 * @return True if the variable should be synced with clients, false if not.
	 */
	boolean isSynced();

	/**
	 * Writes this variable's value to the given {@link ByteBuf}.
	 */
	void write(ByteBuf buf, T value);

	/**
	 * Reads this variable's value from the given {@link ByteBuf}.
	 */
	T read(ByteBuf buf);

	/** If you're storing a lot of data, you can optionally implement this method to define a condition which, if
	 * satisfied, will result in the data being removed from storage, reducing unnecessary syncing and saving. This
	 * is particularly relevant if the value is synced as it reduces packet size. */
	default boolean canPurge(EntityPlayer player, T value){
		return false;
	}

	/**
	 * General-purpose implementation of {@link IVariable} for non-stored variables. These may still, however, persist
	 * across player respawn/dimension change.
	 * <p></p>
	 * @param <T> The type of variable stored.
	 */
	class Variable<T> implements IVariable<T> {

		private final Persistence persistence;

		private BiFunction<EntityPlayer, T, T> ticker;

		public Variable(Persistence persistence){
			this.persistence = persistence;
			this.ticker = (p, t) -> t;
		}

		/**
		 * Replaces this variable's update method with the given update function. <i>Beware of auto-unboxing of
		 * primitive types! For lambda expressions, check the second parameter isn't null before operating on it.
		 * For method references, do not reference a method that takes a primitive type. Otherwise, this will cause
		 * a (difficult to debug) {@link NullPointerException} if the key was not stored.</i>
		 * @param ticker A {@link BiFunction} specifying the actions to be performed on this variable each tick. The
		 *               {@code BiFunction} returns the new value for this variable.
		 * @return This {@code Variable} object, allowing this method to be chained onto object creation.
		 */
		public Variable<T> withTicker(BiFunction<EntityPlayer, T, T> ticker){
			this.ticker = ticker;
			return this;
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
			return false;// Not implemented for now, maybe we will one day
		}

		@Override
		public void write(ByteBuf buf, T value){
			// NYI
		}

		@Override
		public T read(ByteBuf buf){
			return null; // NYI
		}
	}
}
