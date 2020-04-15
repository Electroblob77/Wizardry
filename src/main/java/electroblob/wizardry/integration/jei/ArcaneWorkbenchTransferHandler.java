package electroblob.wizardry.integration.jei;

import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI recipe transfer handler for the arcane workbench. This differs from a standard recipe handler in that it returns
 * the active bookshelf (virtual) slots from the workbench as part of the inventory slots, and does not require complete
 * sets of ingredients.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
public class ArcaneWorkbenchTransferHandler implements IRecipeTransferInfo<ContainerArcaneWorkbench> {

	@Override
	public Class<ContainerArcaneWorkbench> getContainerClass(){
		return ContainerArcaneWorkbench.class;
	}

	@Override
	public String getRecipeCategoryUid(){
		return ArcaneWorkbenchRecipeCategory.UID;
	}

	@Override
	public boolean canHandle(ContainerArcaneWorkbench container){
		return true;
	}

	@Override
	public boolean requireCompleteSets(){
		return false; // Arcane workbench maths doesn't work like crafting maths!
	}

	@Override
	public List<Slot> getRecipeSlots(ContainerArcaneWorkbench container){
		return container.inventorySlots.subList(0, ContainerArcaneWorkbench.UPGRADE_SLOT + 1);
	}

	@Override
	public List<Slot> getInventorySlots(ContainerArcaneWorkbench container){
		List<Slot> slots = new ArrayList<>(container.inventorySlots.subList(ContainerArcaneWorkbench.UPGRADE_SLOT + 1, ContainerArcaneWorkbench.UPGRADE_SLOT + 37));
		slots.addAll(container.getBookshelfSlots());
		return slots;
	}

}
