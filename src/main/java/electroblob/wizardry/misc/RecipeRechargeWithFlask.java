package electroblob.wizardry.misc;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemManaFlask;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.Collection;

/**
 * Implements a dynamic crafting recipe for recharging items with mana flasks.
 *
 * @author Corail31, Electroblob
 * @since Wizardry 4.2.2
 */
@Mod.EventBusSubscriber
public class RecipeRechargeWithFlask extends ShapelessOreRecipe {

	private static final Multimap<Item, RecipeRechargeWithFlask> FLASK_RECIPES = HashMultimap.create();

	private final IManaStoringItem chargeable;
	private final ItemManaFlask flask;

	/**
	 * Creates a new charging recipe for the given chargeable item using the given flask item.
	 * @param chargeable The type of item to be charged
	 * @param flask The mana flask used to charge the item
	 */
	public RecipeRechargeWithFlask(Item chargeable, ItemManaFlask flask){
		super(null, new ItemStack(chargeable, 1, OreDictionary.WILDCARD_VALUE),
				new ItemStack(chargeable, 1, OreDictionary.WILDCARD_VALUE), flask);
		if(!(chargeable instanceof IManaStoringItem)) throw new IllegalArgumentException("Item to be charged must be an instance of IManaStoringItem");
		this.chargeable = (IManaStoringItem)chargeable;
		this.flask = flask;
		FLASK_RECIPES.put(chargeable, this);
	}

	// Commented out for now because JEI spams the log with errors about the recipe having no output
//	@Nonnull
//	@Override
//	public ItemStack getRecipeOutput(){
//		return ItemStack.EMPTY; // According to the javadoc, dynamic recipes are supposed to return an empty stack here
//	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv){
		ItemStack result = super.getCraftingResult(inv);
		rechargeItemAndCopyNBT(result, inv);
		return result;
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world){
		ItemStack stack = findItemToCharge(inv);
		if(!stack.isEmpty() && chargeable.isManaFull(stack)) return false;
		return super.matches(inv, world);
	}

	private void rechargeItemAndCopyNBT(ItemStack toCharge, InventoryCrafting inv){
		if(toCharge.getItem() == chargeable){
			ItemStack stack = findItemToCharge(inv);
			if(!stack.isEmpty()) chargeable.setMana(toCharge, chargeable.getMana(stack));
			chargeable.rechargeMana(toCharge, flask.size.capacity);
			toCharge.setTagCompound(stack.getTagCompound()); // Copy NBT to new stack
		}else{
			Wizardry.logger.warn("Tried to recharge item {} with mana flask, but it did not match the recipe result {}!", toCharge.getItem(), chargeable);
		}
	}

	private ItemStack findItemToCharge(InventoryCrafting inv){
		for(int i=0; i<inv.getSizeInventory(); i++){
			ItemStack ingredient = inv.getStackInSlot(i);
			if(ingredient.getItem() == chargeable) return ingredient;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isDynamic(){
		return true; // Stops it appearing in the recipe book
	}

	@SubscribeEvent
	public static void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event){
		// getCraftingResult seems to only work for the result that's displayed, not once it is actually taken
		// This means that although I no longer have to replace the result every tick, I still need to do it here
		// ... I thought the whole point of the new recipe system was so that I DIDN'T have to do this?!
		if(event.craftMatrix instanceof InventoryCrafting){
			Collection<RecipeRechargeWithFlask> recipes = FLASK_RECIPES.get(event.crafting.getItem());
			for(RecipeRechargeWithFlask recipe : recipes){
				if(recipe.matches((InventoryCrafting)event.craftMatrix, event.player.world)){
					// Have to modify the itemstack in the actual event, it cannot be replaced
					recipe.rechargeItemAndCopyNBT(event.crafting, (InventoryCrafting)event.craftMatrix);
				}
			}
		}
	}
}
