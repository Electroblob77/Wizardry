package electroblob.wizardry.integration.baubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.IBauble;
import baubles.api.cap.BaublesCapabilities;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles all of wizardry's integration with the <i>Baubles</i> mod. This class contains only the code
 * that requires Baubles to be loaded in order to run. Conversely, all code that requires Baubles to be loaded is
 * located within this class or another class in the package {@code electroblob.wizardry.integration.baubles}.
 *
 * @since Wizardry 4.2
 * @author Electroblob
 */
public final class WizardryBaublesIntegration {

	public static final String BAUBLES_MOD_ID = "baubles";

	private static final Map<ItemArtefact.Type, BaubleType> ARTEFACT_TYPE_MAP = new EnumMap<>(ItemArtefact.Type.class);

	private static boolean baublesLoaded;

	public static void init(){

		baublesLoaded = Loader.isModLoaded(BAUBLES_MOD_ID);

		if(!enabled()) return;

		ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.RING, BaubleType.RING);
		ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.AMULET, BaubleType.AMULET);
		ARTEFACT_TYPE_MAP.put(ItemArtefact.Type.CHARM, BaubleType.CHARM);
	}

	public static boolean enabled(){
		return Wizardry.settings.baublesIntegration && baublesLoaded;
	}

	// Wrappers for BaublesApi methods

	/**
	 * Return true if the given item is equipped in any bauble slot.
	 * @param player The player whose inventory is to be checked.
	 * @param item The item to check for.
	 * @return True if the given item is equipped in a bauble slot, false otherwise.
	 */
	public static boolean isBaubleEquipped(EntityPlayer player, Item item){
		return BaublesApi.isBaubleEquipped(player, item) >= 0;
	}

	/**
	 * Returns a list of artefact stacks equipped of the given types. <i>This method does not check whether artefacts
	 * have been disabled in the config! {@link ItemArtefact#getActiveArtefacts(EntityPlayer, ItemArtefact.Type...)}
	 * should be used instead of this method in nearly all cases.</i>
	 * @param player The player whose inventory is to be checked.
	 * @param types Zero or more artefact types to check for. If omitted, searches for all types.
	 * @return A list of equipped artefact {@code ItemStacks}.
	 */
	// This could return all ItemStacks, but if an artefact type is given this doesn't really make sense.
	public static List<ItemArtefact> getEquippedArtefacts(EntityPlayer player, ItemArtefact.Type... types){

		List<ItemArtefact> artefacts = new ArrayList<>();

		for(ItemArtefact.Type type : types){
			for(int slot : ARTEFACT_TYPE_MAP.get(type).getValidSlots()){
				ItemStack stack = BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
				if(stack.getItem() instanceof ItemArtefact) artefacts.add((ItemArtefact)stack.getItem());
			}
		}

		return artefacts;
	}

	// Shamelessly copied from The Twilight Forest, with a few modifications
	@SuppressWarnings("unchecked")
	public static final class ArtefactBaubleProvider implements ICapabilityProvider {

		private BaubleType type;

		public ArtefactBaubleProvider(ItemArtefact.Type type){
			this.type = ARTEFACT_TYPE_MAP.get(type);
		}

		@Override
		public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing){
			return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE;
		}

		@Override
		public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing){
			// This lambda expression is an implementation of the entire IBauble interface
			return capability == BaublesCapabilities.CAPABILITY_ITEM_BAUBLE ? (T)(IBauble)itemStack -> type : null;
		}
	}

}
