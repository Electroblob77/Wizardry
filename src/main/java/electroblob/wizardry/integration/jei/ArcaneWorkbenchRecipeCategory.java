package electroblob.wizardry.integration.jei;

import com.google.common.collect.Streams;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.item.*;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryRecipes;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WandHelper;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JEI recipe category implementation for all 'recipes' in the arcane workbench (of course, the arcane workbench
 * doesn't have 'recipes' in the normal sense, but that's how they're displayed in JEI).
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public class ArcaneWorkbenchRecipeCategory implements IRecipeCategory<ArcaneWorkbenchRecipe> {

	static final String UID = "ebwizardry:arcane_workbench";
	static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/container/arcane_workbench_jei_background.png");

	static final int WIDTH = 166;
	static final int HEIGHT = 126;
	// Annoyingly, these seem to be for 18x18 slots rather than the actual highlighted area, unlike container slots
	static final int CENTRE_SLOT_X = 74;
	static final int CENTRE_SLOT_Y = 54;
	static final int CRYSTAL_SLOT_X = 7;
	static final int CRYSTAL_SLOT_Y = 91;
	static final int UPGRADE_SLOT_X = 141;
	static final int UPGRADE_SLOT_Y = 7;
	static final int OUTPUT_SLOT_X = 141;
	static final int OUTPUT_SLOT_Y = 101;

	private final IDrawable background;

	public ArcaneWorkbenchRecipeCategory(IRecipeCategoryRegistration registry){
		IGuiHelper helper = registry.getJeiHelpers().getGuiHelper();
		background = helper.createDrawable(TEXTURE, 0, 0, WIDTH, HEIGHT);
	}

	@Override
	public String getUid(){
		return UID;
	}

	@Override
	public String getTitle(){
		// JEI is client-side, so client classes can safely be used here
		return I18n.format("integration.jei.category." + UID);
	}

	@Override
	public String getModName(){
		return Wizardry.NAME;
	}

	@Override
	public IDrawable getBackground(){
		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, ArcaneWorkbenchRecipe recipeWrapper, IIngredients ingredients){

		// Okay, they're not technically *slots* but to all intents and purposes, that's how they behave
		IGuiItemStackGroup slots = recipeLayout.getItemStacks();

		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
		List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

		ItemStack centreStack = inputs.get(inputs.size() - 2).get(0);

		int bookSlots = 0;

		if(centreStack.getItem() instanceof IWorkbenchItem){
			bookSlots = ((IWorkbenchItem)centreStack.getItem()).getSpellSlotCount(centreStack);
		}

		// Slot initialisation
		int i = 0;

		while(i < bookSlots){
			int x = CENTRE_SLOT_X + ContainerArcaneWorkbench.getBookSlotXOffset(i, bookSlots);
			int y = CENTRE_SLOT_Y + ContainerArcaneWorkbench.getBookSlotYOffset(i, bookSlots);
			slots.init(i++, true, x, y);
		}

		// Add dummy slots for the hidden book slots so the transfer handler works correctly
		// Sure, we COULD use an IRecipeTransferHandler, but this is far less effort!
		while(i < ContainerArcaneWorkbench.CRYSTAL_SLOT){
			slots.init(i++, true, 0, 0);
		}

		slots.init(i++, true, CRYSTAL_SLOT_X, CRYSTAL_SLOT_Y);
		slots.init(i++, true, CENTRE_SLOT_X, CENTRE_SLOT_Y);
		slots.init(i++, true, UPGRADE_SLOT_X, UPGRADE_SLOT_Y);

		slots.init(i++, false, OUTPUT_SLOT_X, OUTPUT_SLOT_Y);

		// Assign ingredients to slots
		// The number of books we actually have is inputs.size() - 3, probably less than the number of book slots
		for(int j = 0; j < Math.min(bookSlots, inputs.size() - 3); j++){
			slots.set(j, inputs.get(j));
		}

		slots.set(ContainerArcaneWorkbench.CRYSTAL_SLOT, inputs.get(inputs.size() - 3));
		slots.set(ContainerArcaneWorkbench.CENTRE_SLOT, inputs.get(inputs.size() - 2));
		slots.set(ContainerArcaneWorkbench.UPGRADE_SLOT, inputs.get(inputs.size() - 1));

		for(int k = 0; k < outputs.size(); k++) slots.set(11 + k, outputs.get(k));

	}

	/** Called to generate all of wizardry's arcane workbench 'recipes' for JEI. */
	public static Collection<ArcaneWorkbenchRecipe> generateRecipes(){

		List<ArcaneWorkbenchRecipe> recipes = new ArrayList<>();

		recipes.addAll(generateUpgradeRecipes()); // Probably nicest to have these first, they're the most useful
		recipes.addAll(generateChargingRecipes());
		recipes.addAll(generateScrollRecipes());

		return recipes;

	}

	private static Collection<ArcaneWorkbenchRecipe> generateUpgradeRecipes(){

		List<ArcaneWorkbenchRecipe> recipes = new ArrayList<>();

		List<ItemStack> upgrades = new ArrayList<>();

		for(Item item : Item.REGISTRY){
			if(item instanceof ItemArcaneTome || item instanceof ItemArmourUpgrade){
				NonNullList<ItemStack> variants = NonNullList.create();
				item.getSubItems(item.getCreativeTab(), variants);
				upgrades.addAll(variants);
			}
		}

		// Condense all special upgrades into one ingredient in an effort to reduce the number of separate recipes
		List<ItemStack> specialUpgrades = new ArrayList<>();

		for(Item item : WandHelper.getSpecialUpgrades()){
			NonNullList<ItemStack> variants = NonNullList.create();
			item.getSubItems(item.getCreativeTab(), variants);
			specialUpgrades.addAll(variants);
		}

		for(Item item : Item.REGISTRY){

			if(item instanceof IWorkbenchItem){

				ItemStack original = new ItemStack(item);

				for(ItemStack upgrade : upgrades){
					// Copy both input stacks to ignore any modifications to them during the upgrading process
					ItemStack result = ((IWorkbenchItem)item).applyUpgrade(null, original.copy(), upgrade.copy());
					// It's only a valid 'recipe' if something actually changed
					if(!ItemStack.areItemStacksEqual(original, result)){
						recipes.add(new ArcaneWorkbenchRecipe(original, Collections.emptyList(), Collections.emptyList(),
								Collections.singletonList(upgrade), result));
					}
				}

				List<ItemStack> applicableSpecialUpgrades = new ArrayList<>();

				for(ItemStack upgrade : specialUpgrades){
					// Copy both input stacks to ignore any modifications to them during the upgrading process
					ItemStack result = ((IWorkbenchItem)item).applyUpgrade(null, original.copy(), upgrade.copy());
					// It's only a valid 'recipe' if something actually changed
					if(!ItemStack.areItemStacksEqual(original, result)){
						applicableSpecialUpgrades.add(upgrade);
					}
				}

				if(!applicableSpecialUpgrades.isEmpty()){
					recipes.add(new ArcaneWorkbenchRecipe(original, Collections.emptyList(), Collections.emptyList(),
							applicableSpecialUpgrades, original)); // Wands with special upgrades look no different anyway
				}

			}
		}

		return recipes;

	}

	private static Collection<ArcaneWorkbenchRecipe> generateChargingRecipes(){

		List<ArcaneWorkbenchRecipe> recipes = new ArrayList<>();

		List<ItemStack> crystals = new ArrayList<>();
		for(int meta = 0; meta < Element.values().length; meta++) crystals.add(new ItemStack(WizardryItems.magic_crystal, 1, meta));
		List<ItemStack> shard = Collections.singletonList(new ItemStack(WizardryItems.crystal_shard));
		List<ItemStack> grandCrystal = Collections.singletonList(new ItemStack(WizardryItems.grand_crystal));

		for(Item chargeable : WizardryRecipes.getChargeableItems()){

			if(!(chargeable instanceof IManaStoringItem)) throw new IllegalArgumentException("Item to be charged must be an instance of IManaStoringItem");

			ItemStack input = new ItemStack(chargeable);
			((IManaStoringItem)chargeable).setMana(input, 0);

			ItemStack result = new ItemStack(chargeable);
			((IManaStoringItem)chargeable).setMana(result, Constants.MANA_PER_CRYSTAL);
			recipes.add(new ArcaneWorkbenchRecipe(input, Collections.emptyList(), crystals, Collections.emptyList(), result));

			result = new ItemStack(chargeable);
			((IManaStoringItem)chargeable).setMana(result, Constants.MANA_PER_SHARD);
			recipes.add(new ArcaneWorkbenchRecipe(input, Collections.emptyList(), shard, Collections.emptyList(), result));

			result = new ItemStack(chargeable);
			((IManaStoringItem)chargeable).setMana(result, Constants.GRAND_CRYSTAL_MANA);
			recipes.add(new ArcaneWorkbenchRecipe(input, Collections.emptyList(), grandCrystal, Collections.emptyList(), result));
		}

		return recipes;

	}

	private static Collection<ArcaneWorkbenchRecipe> generateScrollRecipes(){

		List<ArcaneWorkbenchRecipe> recipes = new ArrayList<>();

		ItemStack blankScroll = new ItemStack(WizardryItems.blank_scroll);

		// We need not make people register these manually since spells already have control over what they can be put on
		List<Item> spellBooks = Streams.stream(Item.REGISTRY).filter(i -> i instanceof ItemSpellBook).collect(Collectors.toList());
		List<Item> scrolls = Streams.stream(Item.REGISTRY).filter(i -> i instanceof ItemScroll).collect(Collectors.toList());

		for(Spell spell : Spell.getAllSpells()){

			for(Item spellBook : spellBooks){
				for(Item scroll : scrolls){
					if(spell.applicableForItem(spellBook) && spell.applicableForItem(scroll)){
						List<ItemStack> books = Collections.singletonList(new ItemStack(WizardryItems.spell_book, 1, spell.metadata()));
						ItemStack result = new ItemStack(WizardryItems.scroll, 1, spell.metadata());
						recipes.add(new ArcaneWorkbenchRecipe(blankScroll, books, spell.getCost(), Collections.emptyList(), result));
					}
				}
			}
		}

		return recipes;

	}

}
