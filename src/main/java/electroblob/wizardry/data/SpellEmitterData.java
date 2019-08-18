package electroblob.wizardry.data;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.command.SpellEmitter;
import electroblob.wizardry.packet.PacketEmitterData;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for storing and keeping track of {@link SpellEmitter}s. Each world has its own instance of
 * {@code SpellEmitterData} which can be retrieved using {@link SpellEmitterData#get(World)}.<br>
 * <br>
 * To add a new {@code SpellEmitter}, use {@link SpellEmitter#add(Spell, World, double, double, double, EnumFacing, int, SpellModifiers)}.
 *
 * @since Wizardry 4.2
 * @author Electroblob
 */
@Mod.EventBusSubscriber
public class SpellEmitterData extends WorldSavedData {

	public static final String NAME = Wizardry.MODID + "_spell_emitters";

	private final List<SpellEmitter> emitters = new ArrayList<>();

	private NBTTagList emitterTags = null;

	// Required constructors
	public SpellEmitterData(){
		this(NAME);
	}

	public SpellEmitterData(String name){
		super(name);
	}

	/** Returns the spell emitter data for this world, or creates a new instance if it doesn't exist yet. */
	public static SpellEmitterData get(World world){

		SpellEmitterData instance = (SpellEmitterData)world.getPerWorldStorage().getOrLoadData(SpellEmitterData.class, NAME);

		if(instance == null){
			instance = new SpellEmitterData();
			world.getPerWorldStorage().setData(NAME, instance);
		}else if(instance.emitters.isEmpty() && instance.emitterTags != null){
			instance.loadEmitters(world);
		}

		return instance;
	}

	/** Sends the active spell emitters for this world to the specified player's client. */
	public void sync(EntityPlayerMP player){
		PacketEmitterData.Message msg = new PacketEmitterData.Message(emitters);
		WizardryPacketHandler.net.sendTo(msg, player);
		Wizardry.logger.info("Synchronising spell emitters for " + player.getName());
	}

	/** Adds the given {@link SpellEmitter} to the list of emitters for this {@code SpellEmitterData}. */
	public void add(SpellEmitter emitter){
		emitters.add(emitter);
		markDirty();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		emitterTags = nbt.getTagList("emitters", Constants.NBT.TAG_COMPOUND);
	}

	private void loadEmitters(World world){
		emitters.clear();
		emitters.addAll(NBTExtras.NBTToList(emitterTags, (NBTTagCompound t) -> SpellEmitter.fromNBT(world, t)));
		emitterTags = null; // Now we know it's loaded
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		compound.setTag("emitters", NBTExtras.listToNBT(emitters, SpellEmitter::toNBT));
		return compound;
	}

	public static void update(World world){
		SpellEmitterData data = SpellEmitterData.get(world);
		if(!data.emitters.isEmpty()){
			data.emitters.forEach(SpellEmitter::update);
			data.emitters.removeIf(SpellEmitter::needsRemoving);
			data.markDirty(); // Mark dirty if there are changes to be saved
		}
	}

	@SubscribeEvent
	public static void tick(TickEvent.WorldTickEvent event){
		if(!event.world.isRemote && event.phase == TickEvent.Phase.END){
			update(event.world);
		}
	}

	@SubscribeEvent
	public static void onWorldLoadEvent(WorldEvent.Load event){
		// Called to initialise the spell emitter data when a world loads, if it isn't already.
		SpellEmitterData.get(event.getWorld());
	}

	@SubscribeEvent
	public static void onPlayerChangedDimensionEvent(PlayerEvent.PlayerChangedDimensionEvent event){
		// Needs to be done here as well as PlayerLoggedInEvent because SpellEmitterData is dimension-specific
		if(event.player instanceof EntityPlayerMP){
			SpellEmitterData.get(event.player.world).sync((EntityPlayerMP)event.player);
		}
	}

}
