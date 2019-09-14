package electroblob.wizardry.entity.data;

import electroblob.wizardry.Wizardry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.network.datasync.DataSerializers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes a synced behavior. They follow the state design pattern, in that
 * each behavior should be switchable over an entity, and is responsible for an
 * update tick. Typically, behaviors are static inner classes, where the outer
 * class extends Behavior and is the superclass of the inner classes.
 * <p>
 * All custom behaviors must be registered via {@link #registerBehaviour(Class)}.
 * As Behaviors are most commonly synced with DataManager, a
 * {@link BehaviorSerializer data serializer} is needed to synchronize server
 * and client. It should be registered with
 * {@link DataSerializers#registerSerializer(DataSerializer)}.
 * <p>
 * Make sure that subclasses receive the instance of entity.
 *
 * @param E Type of entity this behavior is for
 */
public abstract class Behaviour<E extends Entity> {

	private static int nextId = 1;
	private static Map<Integer, Class<? extends Behaviour>> behaviourIdToClass;
	private static Map<Class<? extends Behaviour>, Integer> classToBehaviorId;

	public Behaviour() {
	}

	// Static method called from preInit
	public static void registerBehaviours() {

	}

	protected static int registerBehaviour(Class<? extends Behaviour> behaviourClass) {
		if (behaviourIdToClass == null) {
			behaviourIdToClass = new HashMap<>();
			classToBehaviorId = new HashMap<>();
			nextId = 1;
		}
		int id = nextId++;
		behaviourIdToClass.put(id, behaviourClass);
		classToBehaviorId.put(behaviourClass, id);
		return id;
	}

	/**
	 * Looks up the behavior class by the given Id, then instantiates an instance
	 * with reflection.
	 */
	public static Behaviour lookup(int id, Entity entity) {
		try {

			Behaviour behaviour = behaviourIdToClass.get(id).newInstance();
			return behaviour;

		} catch (Exception e) {

			Wizardry.logger.error("Error constructing behavior...");
			e.printStackTrace();
			return null;

		}
	}

	public int getId() {
		return classToBehaviorId.get(getClass());
	}

	/**
	 * Called every update tick.
	 *
	 * @return Next Behavior. Return <code>this</code> to continue the Behavior.
	 * May never return null.
	 */
	public abstract Behaviour onUpdate(E entity);

	public abstract void fromBytes(PacketBuffer buf);

	public abstract void toBytes(PacketBuffer buf);

	public abstract void load(NBTTagCompound nbt);

	public abstract void save(NBTTagCompound nbt);

	public static class BehaviorSerializer<B extends Behaviour<? extends Entity>> implements DataSerializer<B> {

		// FIXME research- why doesn't read/write get called every time that
		// behavior changes???

		@Override
		public void write(PacketBuffer buf, B value) {
			buf.writeInt(value.getId());
			value.toBytes(buf);
		}

		@Override
		public B read(PacketBuffer buf) throws IOException {
			try {

				Behaviour behaviour = behaviourIdToClass.get(buf.readInt()).newInstance();
				behaviour.fromBytes(buf);
				return (B) behaviour;

			} catch (Exception e) {

				Wizardry.logger.error("Error reading Behavior from bytes");
				e.printStackTrace();
				return null;

			}
		}

		@Override
		public DataParameter<B> createKey(int id) {
			return new DataParameter<>(id, this);
		}

		@Override
		public B copyValue(B behaviour) {
			return behaviour;
		}

	}
}
