package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemManaFlask;
import electroblob.wizardry.misc.RecipeRechargeWithFlask;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

	private static final List<Item> chargeableItems = new ArrayList<>();

	private static boolean registered;

	/** Adds the given item to the list of items that can be charged using mana flasks. Dynamic charging recipes
	 * will be added for these items during {@code RegistryEvent.Register<IRecipe>}. The item must implement
	 * {@link IManaStoringItem} for the recipes to work correctly. This method should be called from the item's
	 * constructor. */
	public static void addToManaFlaskCharging(Item item){

		if(registered){
			Wizardry.logger.warn("Tried to add an item to mana flask charging after it was registered, this will do nothing!");
			return;
		}

		chargeableItems.add(item);
	}

	/** Returns an unmodifiable view of all registered items that can be charged with mana flasks. */
	public static List<Item> getChargeableItems(){
		return Collections.unmodifiableList(chargeableItems);
	}

	/** Now only deals with the dynamic crafting recipes and the smelting recipes. */
	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event){

		IForgeRegistry<IRecipe> registry = event.getRegistry();

		FurnaceRecipes.instance().addSmeltingRecipeForBlock(WizardryBlocks.crystal_ore, new ItemStack(WizardryItems.magic_crystal), 0.5f);

		// Mana flask recipes

		for(Item chargeable : chargeableItems){

			registry.register(new RecipeRechargeWithFlask(chargeable, (ItemManaFlask)WizardryItems.small_mana_flask)
					.setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/small_flask_" + chargeable.getRegistryName().getPath())));

			registry.register(new RecipeRechargeWithFlask(chargeable, (ItemManaFlask)WizardryItems.medium_mana_flask)
					.setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/medium_flask_" + chargeable.getRegistryName().getPath())));

			registry.register(new RecipeRechargeWithFlask(chargeable, (ItemManaFlask)WizardryItems.large_mana_flask)
					.setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/large_flask_" + chargeable.getRegistryName().getPath())));

		}

		registered = true;

	}

}
