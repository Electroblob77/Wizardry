package electroblob.wizardry.integration.jei;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.registry.WizardryItems;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a 'recipe' for the arcane workbench.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public class ArcaneWorkbenchRecipe implements IRecipeWrapper {

	private final ItemStack centreStack;
	private final List<ItemStack> books;
	private final List<ItemStack> crystals;
	private final List<ItemStack> upgrades;
	private final ItemStack result;

	private final int bookSlots;

	private final List<List<ItemStack>> inputs;

	public ArcaneWorkbenchRecipe(ItemStack centreStack, List<ItemStack> books, List<ItemStack> crystals, List<ItemStack> upgrades, ItemStack result){

		this.centreStack = centreStack;
		this.books = books; // CAUTION! This list is an OUTER LIST!
		this.crystals = crystals;
		this.upgrades = upgrades;
		this.result = result;

		this.inputs = new ArrayList<>();
		for(ItemStack book : books) this.inputs.add(Collections.singletonList(book));
		this.inputs.add(crystals);
		this.inputs.add(Collections.singletonList(centreStack));
		this.inputs.add(upgrades);

		if(centreStack.getItem() instanceof IWorkbenchItem){
			bookSlots = ((IWorkbenchItem)centreStack.getItem()).getSpellSlotCount(centreStack);
		}else{
			bookSlots = 0;
		}

	}

	public ArcaneWorkbenchRecipe(ItemStack centreStack, List<ItemStack> books, int mana, List<ItemStack> upgrades, ItemStack result){
		this(centreStack, books, generateCrystalStacks(mana), upgrades, result);
	}

	@Override
	public void getIngredients(IIngredients ingredients){
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		ingredients.setOutput(VanillaTypes.ITEM, result);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY){
		// Can't do this in IRecipeCategory#drawExtras because we have no access to the recipe there!
		// ArcaneWorkbenchRecipeCategory.TEXTURE is already bound at this point
		for(int i = 0; i < bookSlots; i++){
			int x = ArcaneWorkbenchRecipeCategory.CENTRE_SLOT_X + ContainerArcaneWorkbench.getBookSlotXOffset(i, bookSlots) - 9;
			int y = ArcaneWorkbenchRecipeCategory.CENTRE_SLOT_Y + ContainerArcaneWorkbench.getBookSlotYOffset(i, bookSlots) - 9;
			DrawingUtils.drawTexturedRect(x, y, 0, ArcaneWorkbenchRecipeCategory.HEIGHT, 36, 36, 256, 256);
		}
	}

	/**
	 * Returns a list of item stacks, one for each type of crystal (regular, elemental, grand and shards), each with
	 * the minimum quantity needed to supply the given amount of mana. Types of crystal for which more than the max.
	 * stack size would be needed are ignored.
	 */
	public static List<ItemStack> generateCrystalStacks(int mana){

		if(mana < 0) throw new IllegalArgumentException("Cannot create an arcane workbench recipe with negative mana!");

		if(mana == 0) return Collections.emptyList();

		List<ItemStack> crystalStacks = new ArrayList<>();

		int count = MathHelper.ceil((float)mana / Constants.MANA_PER_CRYSTAL);
		// A stack of crystals will almost certainly be enough mana, but you never know!
		// Using ItemStack.EMPTY to avoid deprecated method; crystals' stack size is not stack-sensitive so it doesn't matter
		if(count <= WizardryItems.magic_crystal.getItemStackLimit(ItemStack.EMPTY)){
			for (Element element : Element.values()) {
				crystalStacks.add(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wizardry.MODID, element.name().toLowerCase() + "_crystal")), count));
			}
		}

		count = MathHelper.ceil((float)mana / Constants.MANA_PER_SHARD);

		if(count <= WizardryItems.crystal_shard.getItemStackLimit(ItemStack.EMPTY)){
			crystalStacks.add(new ItemStack(WizardryItems.crystal_shard, count));
		}

		count = MathHelper.ceil((float)mana / Constants.GRAND_CRYSTAL_MANA);

		if(count <= WizardryItems.grand_crystal.getItemStackLimit(ItemStack.EMPTY)){
			crystalStacks.add(new ItemStack(WizardryItems.grand_crystal, count));
		}

		return crystalStacks;

	}

}
