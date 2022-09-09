package electroblob.wizardry.integration.jei;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityImbuementAltar;
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
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * JEI recipe category implementation for all 'recipes' in the imbuement altar.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public class ImbuementAltarRecipeCategory implements IRecipeCategory<ImbuementAltarRecipe> {

	static final String UID = "ebwizardry:imbuement_altar";
	static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/integration/jei/imbuement_altar_background.png");

	static final int WIDTH = 134;
	static final int HEIGHT = 74;
	// Annoyingly, these seem to be for 18x18 slots rather than the actual highlighted area, unlike container slots
	static final int CENTRE_SLOT_X = 28;
	static final int CENTRE_SLOT_Y = 28;
	static final int SLOT_SPACING_X = 28;
	static final int SLOT_SPACING_Y = 28;
	static final int OUTPUT_SLOT_X = 112;
	static final int OUTPUT_SLOT_Y = 28;

	private final IDrawable background;

	public ImbuementAltarRecipeCategory(IRecipeCategoryRegistration registry){
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
	public void setRecipe(IRecipeLayout recipeLayout, ImbuementAltarRecipe recipeWrapper, IIngredients ingredients){

		// Okay, they're not technically *slots* but to all intents and purposes, that's how they behave
		IGuiItemStackGroup slots = recipeLayout.getItemStacks();

		List<List<ItemStack>> inputs = ingredients.getInputs(VanillaTypes.ITEM);
		List<List<ItemStack>> outputs = ingredients.getOutputs(VanillaTypes.ITEM);

		// Slot initialisation
		int i = 0;
		slots.init(i++, true, CENTRE_SLOT_X, CENTRE_SLOT_Y); // Centre
		slots.init(i++, true, CENTRE_SLOT_X, CENTRE_SLOT_Y - SLOT_SPACING_Y); // Top
		slots.init(i++, true, CENTRE_SLOT_X - SLOT_SPACING_X, CENTRE_SLOT_Y); // Left
		slots.init(i++, true, CENTRE_SLOT_X, CENTRE_SLOT_Y + SLOT_SPACING_Y); // Bottom
		slots.init(i++, true, CENTRE_SLOT_X + SLOT_SPACING_X, CENTRE_SLOT_Y); // Right
		slots.init(i++, false, OUTPUT_SLOT_X, OUTPUT_SLOT_Y); // Output

		// Assign ingredients to slots
		for(int j = 0; j < inputs.size(); j++) slots.set(j, inputs.get(j));
		for(int k = 0; k < outputs.size(); k++) slots.set(inputs.size() + k, outputs.get(k));

	}

	/** Called to generate all of wizardry's imbuement altar 'recipes' for JEI. */
	public static Collection<ImbuementAltarRecipe> generateRecipes(){

		List<ImbuementAltarRecipe> recipes = new ArrayList<>();

		recipes.addAll(generateBookRecipes());
		recipes.addAll(generateCrystalRecipes());
		recipes.addAll(generateCrystalBlockRecipes());
		recipes.addAll(generateArmourRecipes());

		return recipes;

	}

	private static Collection<ImbuementAltarRecipe> generateBookRecipes(){

		List<ImbuementAltarRecipe> recipes = new ArrayList<>();

		NonNullList<ItemStack> variants = NonNullList.create();
		variants.add(new ItemStack(WizardryItems.spectral_dust_earth));
		variants.add(new ItemStack(WizardryItems.spectral_dust_fire));
		variants.add(new ItemStack(WizardryItems.spectral_dust_healing));
		variants.add(new ItemStack(WizardryItems.spectral_dust_ice));
		variants.add(new ItemStack(WizardryItems.spectral_dust_lightning));
		variants.add(new ItemStack(WizardryItems.spectral_dust_necromancy));
		variants.add(new ItemStack(WizardryItems.spectral_dust_sorcery));

		List<List<ItemStack>> dusts = new ArrayList<>();
		// Generate 4 separate lists, each in a different order to make it obvious they can be any element
		for(int i = 0; i < 4; i++){
			Collections.shuffle(variants);
			dusts.add(new ArrayList<>(variants));
		}

		recipes.add(new ImbuementAltarRecipe(new ItemStack(WizardryItems.ruined_spell_book), dusts, new ItemStack(WizardryItems.spell_book, 1, OreDictionary.WILDCARD_VALUE)));

		return recipes;

	}

	private static Collection<ImbuementAltarRecipe> generateCrystalRecipes(){

		List<ImbuementAltarRecipe> recipes = new ArrayList<>();

		ItemStack input = new ItemStack(WizardryItems.magic_crystal);

		for(Element element : Element.values()){
			List<List<ItemStack>> dusts = Collections.nCopies(4, Collections.singletonList(new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wizardry.MODID,
					"spectral_dust_" + element.name().toLowerCase())))));
			ItemStack output = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wizardry.MODID, element.name().toLowerCase() + "_crystal")));
			recipes.add(new ImbuementAltarRecipe(input, dusts, output));
		}

		return recipes;

	}

	private static Collection<ImbuementAltarRecipe> generateCrystalBlockRecipes(){

		List<ImbuementAltarRecipe> recipes = new ArrayList<>();

		ItemStack input = new ItemStack(WizardryBlocks.magic_crystal_block);

		for (Element element : Element.values()) {
			if (element == Element.MAGIC) { continue; }

			List<List<ItemStack>> dusts = Collections.nCopies(4, Collections.singletonList(new ItemStack(ForgeRegistries.ITEMS.getValue(
					new ResourceLocation(Wizardry.MODID, "spectral_dust_" + element.name().toLowerCase())))));

			ItemStack output = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wizardry.MODID, element.getName().toLowerCase()
					+ "_crystal_block")));
			recipes.add(new ImbuementAltarRecipe(input, dusts, output));
		}

		return recipes;

	}

	private static Collection<ImbuementAltarRecipe> generateArmourRecipes(){

		List<ImbuementAltarRecipe> recipes = new ArrayList<>();

		for(Item item : Item.REGISTRY){

			if(item instanceof ItemWizardArmour){

				ItemStack input = new ItemStack(item);

				for(Element e : Element.values()){

					if(e == Element.MAGIC) continue;

					List<List<ItemStack>> dusts = Collections.nCopies(4, Collections.singletonList(new ItemStack(
							ForgeRegistries.ITEMS.getValue(new ResourceLocation(Wizardry.MODID, "spectral_dust_" + e.name().toLowerCase())))));
					ItemStack output = TileEntityImbuementAltar.getImbuementResult(input, new Element[]{e, e, e, e}, false, null, null);

					if(!output.isEmpty()) recipes.add(new ImbuementAltarRecipe(input, dusts, output));
				}
			}
		}

		return recipes;

	}

}
