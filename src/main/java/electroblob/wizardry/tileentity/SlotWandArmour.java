package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class SlotWandArmour extends SlotWizardry {
	
	private ContainerArcaneWorkbench container;

	public SlotWandArmour(IInventory par1iInventory, int par2, int par3,
			int par4, ContainerArcaneWorkbench container) {
		super(par1iInventory, par2, par3, par4, 1, -1);
		this.container = container;
	}
	
	@Override
	public void putStack(ItemStack stack){
		super.putStack(stack);
		this.container.onSlotChanged(slotNumber, stack, null);
	}
	
	@Override
	public void onPickupFromSlot(EntityPlayer player, ItemStack stack) {
		super.onPickupFromSlot(player, stack);
		this.container.onSlotChanged(slotNumber, null, player);
	}
	
	public int getSlotStackLimit()
    {
        return 1;
    }
	
	public boolean isItemValid(ItemStack itemstack){
		return itemstack != null && (itemstack.getItem() instanceof ItemWand
				|| itemstack.getItem() instanceof ItemWizardArmour
				|| itemstack.getItem() == Wizardry.blankScroll);
    }
}
