package electroblob.wizardry.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Simple extension of {@link Slot} which only accepts items from an array defined in the constructor.
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class SlotItemList extends Slot {
	
	private Item[] items;
	private int stackLimit;

	public SlotItemList(IInventory par1iInventory, int index, int x, int y, int stackLimit, Item... allowedItems) {
		super(par1iInventory, index, x, y);
		this.items = allowedItems;
		this.stackLimit = stackLimit;
	}
	
	public int getSlotStackLimit(){
        return stackLimit;
    }
	
	public boolean isItemValid(ItemStack par1ItemStack){
		for(int i=0; i<items.length; i++){
			if(par1ItemStack != null && (par1ItemStack.getItem() == this.items[i])){
				return true;
			}
    	}
		return false;
    }
}
