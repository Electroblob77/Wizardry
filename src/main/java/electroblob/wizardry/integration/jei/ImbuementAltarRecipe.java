package electroblob.wizardry.integration.jei;

import electroblob.wizardry.Wizardry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a 'recipe' for the imbuement altar.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public class ImbuementAltarRecipe implements IRecipeWrapper {

	private final ItemStack result;

	private final List<List<ItemStack>> inputs;

	public ImbuementAltarRecipe(ItemStack centreStack, List<List<ItemStack>> dusts, ItemStack result){

		this.result = result;

		this.inputs = new ArrayList<>();
		this.inputs.add(Collections.singletonList(centreStack));

		if(dusts.size() > 4){
			Wizardry.logger.warn("Tried to create an imbuement altar JEI recipe with more than 4 dust stacks, ignoring the rest");
			dusts = dusts.subList(0, 4);
		}else if(dusts.size() < 4){
			throw new IllegalArgumentException("Imbuement altar recipes require 4 stacks of dust");
		}

		this.inputs.addAll(dusts);

	}

	@Override
	public void getIngredients(IIngredients ingredients){
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);
		ingredients.setOutput(VanillaTypes.ITEM, result);
	}

}
