package electroblob.wizardry.tileentity;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Simple extension of {@link Slot} which only accepts items from an array of item classes defined in the constructor.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class SlotItemClassList extends Slot {

	private final Class<? extends Item>[] itemClasses;
	private int stackLimit;

	@SafeVarargs
	public SlotItemClassList(IInventory inventory, int index, int x, int y, int stackLimit, Class<? extends Item>... allowedItemClasses){
		super(inventory, index, x, y);
		this.itemClasses = allowedItemClasses;
		this.stackLimit = stackLimit;
	}

	public int getSlotStackLimit(){
		return stackLimit;
	}

	public boolean isItemValid(ItemStack stack){

		for(Class<? extends Item> itemClass : itemClasses){
			if(itemClass.isAssignableFrom(stack.getItem().getClass())){
				return true;
			}
		}

		return false;
	}
}
