package electroblob.wizardry.tileentity;

import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryItems;
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
public class SlotWandArmour extends Slot {

	private ContainerArcaneWorkbench container;

	public SlotWandArmour(IInventory par1iInventory, int index, int x, int y, ContainerArcaneWorkbench container){
		super(par1iInventory, index, x, y);
		this.container = container;
	}

	@Override
	public void putStack(ItemStack stack){
		super.putStack(stack);
		this.container.onSlotChanged(slotNumber, stack, null);
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack){
		this.container.onSlotChanged(slotNumber, ItemStack.EMPTY, player);
		return super.onTake(player, stack);
	}

	@Override
	public int getSlotStackLimit(){
		return 1;
	}

	@Override
	public boolean isItemValid(ItemStack itemstack){
		return (itemstack.getItem() instanceof ItemWand || itemstack.getItem() instanceof ItemWizardArmour
				|| itemstack.getItem() == WizardryItems.blank_scroll);
	}
}
