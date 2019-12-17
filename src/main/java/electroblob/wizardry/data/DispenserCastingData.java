package electroblob.wizardry.data;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.BlockDispenser;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal capability for attaching data to dispensers. The sole purpose of this class is to keep track of continuous
 * spell casting for dispensers.
 * <p></p>
 * Forge seems to have separate classes to hold the Capability<...> instance ('key') and methods for getting the
 * capability, but in my opinion there are already too many classes to deal with, so I'm not adding any more than are
 * necessary, meaning those constants and values are kept here instead.
 * 
 * @since Wizardry 4.2
 * @author Electroblob
 */
@Mod.EventBusSubscriber
public class DispenserCastingData extends BlockCastingData<TileEntityDispenser> {

	/** Static instance of what I like to refer to as the capability key. Private because, well, it's internal! */
	// This annotation does some crazy Forge magic behind the scenes and assigns this field a value.
	@CapabilityInject(DispenserCastingData.class)
	private static final Capability<DispenserCastingData> DISPENSER_CASTING_CAPABILITY = null;

	/** The time for which this dispenser will continue casting a continuous spell. When castingTick exceeds this value,
	 * the dispenser will either stop casting or, if it contains more of the same type of scroll, continue casting and
	 * increase this value by the duration that the spell should be cast for. */
	private int duration;
	
	public DispenserCastingData(){
		this(null); // Nullary constructor for the registration method factory parameter
	}

	public DispenserCastingData(TileEntityDispenser dispenser){
		super(dispenser);
	}

	/** Starts casting the given continuous spell from this dispenser. */
	public void startCasting(Spell spell, double x, double y, double z, int duration, SpellModifiers modifiers){
		startCasting(spell, x, y, z, modifiers);
		this.castingTick = 1; // 1 because we already cast it once in BehaviourSpellDispense
		this.duration = duration;
	}

	@Override
	public void stopCasting(){
		super.stopCasting();
	}

	@Override
	protected Source getSource(){
		return Source.DISPENSER;
	}

	@Override
	protected EnumFacing getDirection(){
		return tileEntity.getWorld().getBlockState(tileEntity.getPos()).getValue(BlockDispenser.FACING);
	}

	@Override
	protected boolean shouldContinueCasting(){
		return tileEntity.getWorld().isBlockPowered(tileEntity.getPos());
	}

	@Override
	public void update(){

		super.update();

		// Check whether enough scrolls are left
		if(this.isCasting() && this.spell.isContinuous){

			if(castingTick > duration && !tileEntity.getWorld().isRemote){

				if(findNewScroll()){
					duration += ItemScroll.CASTING_TIME; // Best way to do it for now.
				}else{
					this.stopCastingAndNotify();
				}
			}
		}
	}

	/** Searches through the dispenser's inventory for a new stack of scrolls of the same spell that is currently being
	 * cast and returns true if at least one such stack is found. Also consumes one scroll if a stack is found; if more
	 * than one applicable stack is found then one will be chosen at random. */
	private boolean findNewScroll(){
		
		if(spell == Spells.none) return false;
		
		List<Integer> slots = new ArrayList<Integer>();
		
		for(int i = 0; i < tileEntity.getSizeInventory(); i++){
			ItemStack stack = tileEntity.getStackInSlot(i);
			if(stack.getItem() instanceof ItemScroll && stack.getMetadata() == spell.metadata()) slots.add(i);
		}
		
		if(slots.isEmpty()) return false; // If no stack was found that matched the current spell
		
		tileEntity.decrStackSize(slots.get(tileEntity.getWorld().rand.nextInt(slots.size())), 1); // Consumes 1 scroll
		return true;
	}

	/** Returns the DispenserCastingData instance for the specified dispenser. */
	public static DispenserCastingData get(TileEntityDispenser dispenser){
		return dispenser.getCapability(DISPENSER_CASTING_CAPABILITY, null);
	}
	
	/** Called from preInit in the main mod class to register the DispenserCastingData capability. */
	public static void register(){

		CapabilityManager.INSTANCE.register(DispenserCastingData.class, new IStorage<DispenserCastingData>(){
			
			@Override
			public NBTBase writeNBT(Capability<DispenserCastingData> capability, DispenserCastingData instance, EnumFacing side){
				return null;
			}

			@Override
			public void readNBT(Capability<DispenserCastingData> capability, DispenserCastingData instance, EnumFacing side, NBTBase nbt){}
			
		}, DispenserCastingData::new);
	}

	// Event handlers

	@SubscribeEvent
	// The type parameter here has to be SoundLoopSpellDispenser, not TileEntityDispenser, or the event won't get fired.
	public static void onCapabilityLoad(AttachCapabilitiesEvent<TileEntity> event){

		if(event.getObject() instanceof TileEntityDispenser)
			event.addCapability(new ResourceLocation(Wizardry.MODID, "casting_data"),
					new DispenserCastingData.Provider((TileEntityDispenser)event.getObject()));
	}

	// Only fired server-side
	@SubscribeEvent
	public static void onWorldTickEvent(TickEvent.WorldTickEvent event){

		if(event.phase == TickEvent.Phase.END){

			// This will fire once for each dimension, but since we want dispenser-casting to work in all dimensions,
			// this is correct (the loaded tile entity list will of course be different in each case.

			// Somehow this was throwing a CME, I have no idea why so I'm just going to cheat and copy the list
			List<TileEntity> tileEntities = new ArrayList<>(event.world.loadedTileEntityList);

			for(TileEntity tileentity : tileEntities){
				if(tileentity instanceof TileEntityDispenser){
					if(DispenserCastingData.get((TileEntityDispenser)tileentity) != null){
						DispenserCastingData.get((TileEntityDispenser)tileentity).update();
					}
				}
			}
		}
	}

	/**
	 * This is a nested class for a few reasons: firstly, it makes sense because instances of this and
	 * DispenserCastingData go hand-in-hand; secondly, it's too short to be worth a separate file; and thirdly (and most 
	 * importantly) it allows me to access DISPENSER_CASTING_CAPABILITY while keeping it private.
	 */
	public static class Provider implements ICapabilitySerializable<NBTTagCompound> {

		private final DispenserCastingData data;

		public Provider(TileEntityDispenser dispenser){
			data = new DispenserCastingData(dispenser);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing){
			return capability == DISPENSER_CASTING_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing){

			if(capability == DISPENSER_CASTING_CAPABILITY){
				return DISPENSER_CASTING_CAPABILITY.cast(data);
			}

			return null;
		}

		@Override
		public NBTTagCompound serializeNBT(){
			return data.serializeNBT();
		}

		@Override
		public void deserializeNBT(NBTTagCompound nbt){
			data.deserializeNBT(nbt);
		}

	}

}
