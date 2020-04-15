package electroblob.wizardry.integration.jei;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.enchantment.Imbuement;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class WizardryJEIPlugin implements IModPlugin {

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry){

		if(!Wizardry.settings.jeiIntegration) return;

		registry.addRecipeCategories(new ArcaneWorkbenchRecipeCategory(registry));
	}

	@Override
	public void register(IModRegistry registry){

		if(!Wizardry.settings.jeiIntegration) return;

		// Add arcane workbench as the item required to use arcane workbench recipes
		registry.addRecipeCatalyst(new ItemStack(WizardryBlocks.arcane_workbench), ArcaneWorkbenchRecipeCategory.UID);

		registry.addRecipes(ArcaneWorkbenchRecipeCategory.generateRecipes(), ArcaneWorkbenchRecipeCategory.UID);

		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new ArcaneWorkbenchTransferHandler());

		// Hide conjured items from JEI
		IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
		for(Item item : Item.REGISTRY){
			if(item instanceof IConjuredItem) blacklist.addIngredientToBlacklist(new ItemStack(item));
		}

		// Hide imbuements from JEI
		for(Enchantment enchantment : Enchantment.REGISTRY){
			if(enchantment instanceof Imbuement){
				for(int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++){
					blacklist.addIngredientToBlacklist(new EnchantmentData(enchantment, level));
				}
			}
		}

	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry){

		if(!Wizardry.settings.jeiIntegration) return;

		// TODO: Probably need to distinguish between normal/legendary armour, at the very least
		// ... that being said, we may well be changing that mechanic anyway

	}

}
