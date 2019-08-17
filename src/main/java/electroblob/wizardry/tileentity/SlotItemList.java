package electroblob.wizardry.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Simple extension of {@link Slot} which only accepts items from an array of items defined in the constructor.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class SlotItemList extends Slot {

	private final Item[] items;
	private int stackLimit;

	public SlotItemList(IInventory inventory, int index, int x, int y, int stackLimit, Item... allowedItems){
		super(inventory, index, x, y);
		this.items = allowedItems;
		this.stackLimit = stackLimit;
	}

	public int getSlotStackLimit(){
		return stackLimit;
	}

	public boolean isItemValid(ItemStack stack){

		for(Item item : items){
			if(stack.getItem() == item){
				return true;
			}
		}

		return false;
	}
}
