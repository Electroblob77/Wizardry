package electroblob.wizardry.recipe;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class RecipeRechargeWithFlask extends ShapelessOreRecipe {
    public RecipeRechargeWithFlask() {
        super(null, ItemStack.EMPTY, ItemStack.EMPTY, WizardryItems.mana_flask);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack result = lookupForIngredients(inv, true);
        if (!result.isEmpty()) {
            result.setItemDamage(Math.max(output.getItemDamage() - Constants.MANA_PER_FLASK, 0));
        }
        return result;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        return !lookupForIngredients(inv, false).isEmpty();
    }

    /* lookup for the needed ingredients && return the rechargeable itemstack */
    private ItemStack lookupForIngredients(InventoryCrafting inv, boolean copy) {
        ItemStack flask = ItemStack.EMPTY;
        ItemStack rechargeable = ItemStack.EMPTY;
        for (int i = 0; (flask.isEmpty() || rechargeable.isEmpty()) && i < inv.getSizeInventory(); i++) {
            ItemStack itemstack = inv.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                if (flask.isEmpty() && itemstack.getItem() == WizardryItems.mana_flask) {
                    flask = itemstack;
                } else if (rechargeable.isEmpty() && (itemstack.getItem() instanceof ItemWand || itemstack.getItem() instanceof ItemWizardArmour) && itemstack.getItemDamage() > 0) {
                    rechargeable = copy ? itemstack.copy() : itemstack;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        }
        return flask.isEmpty() || rechargeable.isEmpty() ? ItemStack.EMPTY : rechargeable;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
