package electroblob.wizardry.inventory;

import electroblob.wizardry.item.IWorkbenchItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * The central wand/armour/scroll slot in the arcane workbench GUI.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class SlotWorkbenchItem extends Slot {

	private ContainerArcaneWorkbench container;

	public SlotWorkbenchItem(IInventory inventory, int index, int x, int y, ContainerArcaneWorkbench container){
		super(inventory, index, x, y);
		this.container = container;
	}

	@Override
	public void putStack(ItemStack stack){
		super.putStack(stack);
		this.container.onSlotChanged(slotNumber, stack, null);
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack){
		ItemStack result = super.onTake(player, stack);
		this.container.onSlotChanged(slotNumber, ItemStack.EMPTY, player);
		return result;
	}

	@Override
	public int getSlotStackLimit(){
		return 16;
	}

	@Override
	public boolean isItemValid(ItemStack stack){
		return stack.getItem() instanceof IWorkbenchItem && ((IWorkbenchItem)stack.getItem()).canPlace(stack);
	}
}
