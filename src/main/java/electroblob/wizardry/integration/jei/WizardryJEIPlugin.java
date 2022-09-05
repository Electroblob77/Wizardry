package electroblob.wizardry.integration.jei;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.enchantment.Imbuement;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemWandUpgrade;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.registry.WizardryItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Arrays;
import java.util.Locale;

@JEIPlugin
public class WizardryJEIPlugin implements IModPlugin {

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry){

		if(!Wizardry.settings.jeiIntegration) return;

		registry.addRecipeCategories(new ArcaneWorkbenchRecipeCategory(registry));
		registry.addRecipeCategories(new ImbuementAltarRecipeCategory(registry));
	}

	@Override
	public void register(IModRegistry registry){

		if(!Wizardry.settings.jeiIntegration) return;

		// Add arcane workbench as the item required to use arcane workbench recipes
		registry.addRecipeCatalyst(new ItemStack(WizardryBlocks.arcane_workbench), ArcaneWorkbenchRecipeCategory.UID);
		registry.addRecipes(ArcaneWorkbenchRecipeCategory.generateRecipes(), ArcaneWorkbenchRecipeCategory.UID);
		registry.getRecipeTransferRegistry().addRecipeTransferHandler(new ArcaneWorkbenchTransferHandler());

		// Add imbuement altar as the item required to use imbuement altar recipes
		registry.addRecipeCatalyst(new ItemStack(WizardryBlocks.imbuement_altar), ImbuementAltarRecipeCategory.UID);
		registry.addRecipes(ImbuementAltarRecipeCategory.generateRecipes(), ImbuementAltarRecipeCategory.UID);

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

		addItemInfo(registry, WizardryItems.crystal_magic);
		addItemInfo(registry, WizardryItems.wizard_handbook);
		addItemInfo(registry, WizardryItems.crystal_shard);
		addItemInfo(registry, WizardryItems.grand_crystal);
		addItemInfo(registry, WizardryItems.identification_scroll, ".desc_extended");
		addItemInfo(registry, WizardryItems.spell_book);
		addItemInfo(registry, WizardryItems.scroll);
		addItemInfo(registry, WizardryItems.resplendent_thread, ".desc_extended");
		addItemInfo(registry, WizardryItems.crystal_silver_plating, ".desc_extended");
		addItemInfo(registry, WizardryItems.ethereal_crystalweave, ".desc_extended");
		addItemInfo(registry, WizardryItems.purifying_elixir, ".desc_extended");
		addItemInfo(registry, WizardryItems.astral_diamond);
		addItemInfo(registry, WizardryItems.ruined_spell_book);

		for(Item item : Item.REGISTRY){

			if(item instanceof ItemArtefact){

				ItemStack stack = new ItemStack(item);
				registry.addIngredientInfo(stack, VanillaTypes.ITEM, "item." + Wizardry.MODID + ":"
						+ ((ItemArtefact)item).getType().toString().toLowerCase(Locale.ROOT) + ".generic.desc");

			}else if(item instanceof ItemWandUpgrade || item instanceof ItemArcaneTome){

				NonNullList<ItemStack> subItems = NonNullList.create();
				item.getSubItems(item.getCreativeTab(), subItems);

				for(ItemStack stack : subItems){
					registry.addIngredientInfo(stack, VanillaTypes.ITEM, "item." + Wizardry.MODID +
							":wand_upgrade.generic.desc");
				}
			}
		}

		addEnchantmentInfo(registry, WizardryEnchantments.magic_protection);
		addEnchantmentInfo(registry, WizardryEnchantments.frost_protection);
		addEnchantmentInfo(registry, WizardryEnchantments.shock_protection);

	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry){

		if(!Wizardry.settings.jeiIntegration) return;

		// TODO: Probably need to distinguish between normal/legendary armour, at the very least
		// ... that being said, we may well be changing that mechanic anyway

	}

	private static void addItemInfo(IModRegistry registry, Item item){
		addItemInfo(registry, item, ".desc");
	}

	private static void addItemInfo(IModRegistry registry, Item item, String... suffixes){
		NonNullList<ItemStack> subItems = NonNullList.create();
		item.getSubItems(item.getCreativeTab(), subItems);
		for(ItemStack stack : subItems) addItemInfo(registry, stack, suffixes);
	}

	private static void addItemInfo(IModRegistry registry, ItemStack stack, String... suffixes){
		String prefix = stack.getItem().getTranslationKey(stack);
		String[] keys = Arrays.stream(suffixes).map(s -> prefix + s).toArray(String[]::new);
		registry.addIngredientInfo(stack, VanillaTypes.ITEM, keys);
	}

	private static void addEnchantmentInfo(IModRegistry registry, Enchantment enchantment){
		addEnchantmentInfo(registry, enchantment, ".desc");
	}

	private static void addEnchantmentInfo(IModRegistry registry, Enchantment enchantment, String... suffixes){
		for(int level = enchantment.getMinLevel(); level <= enchantment.getMaxLevel(); level++){
			addEnchantmentInfo(registry, new EnchantmentData(enchantment, level), suffixes);
		}
	}

	private static void addEnchantmentInfo(IModRegistry registry, EnchantmentData data, String... suffixes){
		String prefix = data.enchantment.getName().replace(":", "."); // Compat with WAWLA descriptions
		String[] keys = Arrays.stream(suffixes).map(s -> prefix + s).toArray(String[]::new);
		registry.addIngredientInfo(data, VanillaTypes.ENCHANT, keys);
	}

}
