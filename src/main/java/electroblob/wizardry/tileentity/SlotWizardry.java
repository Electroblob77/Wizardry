package electroblob.wizardry.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import electroblob.wizardry.item.ItemSpellBook;

public class SlotWizardry extends Slot {
	
	private Item[] items;
	private int stackLimit;
	/** Index for the slot background image, counting left to right in the arcane workbench GUI image, starting at 0.
	 * -1 if the slot has no background. */
	public int backgroundIndex;

	public SlotWizardry(IInventory par1iInventory, int par2, int par3,
			int par4, int stackLimit, int backgroundIndex, Item... allowedItems) {
		super(par1iInventory, par2, par3, par4);
		this.items = allowedItems;
		this.stackLimit = stackLimit;
		this.backgroundIndex = backgroundIndex;
	}
	
	public int getSlotStackLimit()
    {
        return stackLimit;
    }
	
	public boolean isItemValid(ItemStack par1ItemStack)
    {
		for(int i=0; i<items.length; i++){
			if(par1ItemStack != null && (par1ItemStack.getItem() == this.items[i])){
				return true;
			}
    	}
		return false;
    }
}
