package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemManaFlask;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class responsible for defining and registering wizardry's non-JSON recipes (i.e. smelting recipes and dynamic
 * crafting recipes). Also handles dynamic recipe display and usage.
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
@Mod.EventBusSubscriber
public final class WizardryRecipes {

	private WizardryRecipes(){} // No instances!

	private static final Queue<Item> chargingRecipeQueue = new LinkedList<>();

	/** Adds the given item to the list of items that can be charged using mana flasks. Dynamic charging recipes
	 * will be added for these items during {@code RegistryEvent.Register<IRecipe>}. The item must implement
	 * {@link IManaStoringItem} for the recipes to work correctly. This method should be called from the item's
	 * constructor. */
	public static void addToManaFlaskCharging(Item item){
		chargingRecipeQueue.offer(item);
	}

	/** Now only deals with the dynamic crafting recipes and the smelting recipes. */
	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event){

		IForgeRegistry<IRecipe> registry = event.getRegistry();

		FurnaceRecipes.instance().addSmeltingRecipeForBlock(WizardryBlocks.crystal_ore, new ItemStack(WizardryItems.magic_crystal), 0.5f);

		// Mana flask recipes

		ItemStack smallFlaskStack = new ItemStack(WizardryItems.small_mana_flask);
		ItemStack mediumFlaskStack = new ItemStack(WizardryItems.medium_mana_flask);
		ItemStack largeFlaskStack = new ItemStack(WizardryItems.large_mana_flask);

		ItemStack chargeable;

		while(!chargingRecipeQueue.isEmpty()){
			// Use remove() and not poll() because the queue shouldn't be empty in here
			chargeable = new ItemStack(chargingRecipeQueue.remove(), 1, OreDictionary.WILDCARD_VALUE);

			registry.register(new ShapelessOreRecipe(null, chargeable, chargeable, smallFlaskStack){
				@Override public boolean isDynamic(){ return true; } // Stops it appearing in the recipe book
			}.setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/small_flask_" + chargeable.getItem().getRegistryName().getPath())));

			registry.register(new ShapelessOreRecipe(null, chargeable, chargeable, mediumFlaskStack){
				@Override public boolean isDynamic(){ return true; }
			}.setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/medium_flask_" + chargeable.getItem().getRegistryName().getPath())));

			registry.register(new ShapelessOreRecipe(null, chargeable, chargeable, largeFlaskStack){
				@Override public boolean isDynamic(){ return true; }
			}.setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/large_flask_" + chargeable.getItem().getRegistryName().getPath())));

		}
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.phase == TickEvent.Phase.START){

			if(event.player.openContainer instanceof ContainerWorkbench){

				IInventory craftMatrix = ((ContainerWorkbench)event.player.openContainer).craftMatrix;
				ItemStack output = ((ContainerWorkbench)event.player.openContainer).craftResult.getStackInSlot(0);
				processManaFlaskCrafting(craftMatrix, output);

			}else if(event.player.openContainer instanceof ContainerPlayer){

				IInventory craftMatrix = ((ContainerPlayer)event.player.openContainer).craftMatrix;
				ItemStack output = ((ContainerPlayer)event.player.openContainer).craftResult.getStackInSlot(0);
				// Unfortunately I have no choice but to call this method every tick when the player isn't using another
				// inventory, since the only thing tracking whether the player is looking at their inventory is the GUI
				// itself, which is client-side only.
				processManaFlaskCrafting(craftMatrix, output);
			}
		}
	}

	private static void processManaFlaskCrafting(IInventory craftMatrix, ItemStack output){

		// Charges wand using mana flask

		ItemManaFlask flask = null;
		ItemStack input = ItemStack.EMPTY;

		for(int i = 0; i < craftMatrix.getSizeInventory(); i++){

			ItemStack stack = craftMatrix.getStackInSlot(i);

			if(stack.getItem() instanceof ItemManaFlask){
				flask = (ItemManaFlask)stack.getItem();
			}

			if(stack.getItem() instanceof IManaStoringItem){
				input = stack;
			}
		}

		if(flask == null) return;

		if(output.getItem() instanceof IManaStoringItem && !input.isEmpty()){

			output.setTagCompound((input.getTagCompound()));

			int currentMana = ((IManaStoringItem)input.getItem()).getMana(input);

			((IManaStoringItem)output.getItem()).setMana(output, Math.min(currentMana + flask.size.capacity,
					((IManaStoringItem)input.getItem()).getManaCapacity(input)));
		}
	}
}
