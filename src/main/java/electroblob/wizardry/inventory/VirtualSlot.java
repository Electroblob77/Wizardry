package electroblob.wizardry.inventory;

import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * A {@code VirtualSlot} represents a slot in an inventory other than the one that is currently open. Like regular slots,
 * they hold a single {@link net.minecraft.item.ItemStack}, but unlike regular slots, they are not drawn to the screen
 * and cannot be interacted with directly. A virtual slot is effectively a reference to a specific slot in some other
 * inventory that allows the details of that inventory to be abstracted away from the current container.
 * <p></p>
 * <i>N.B. For normal slots, {@code slotIndex == slotNumber} (index is for the inventory, number is for the container).
 * Importantly, for virtual slots this is <b>not the case</b>, since the {@code Container} they belong to is not the
 * one associated with the virtual slot's {@code IInventory}.</i>
 */
public class VirtualSlot extends Slot {

	private final TileEntity tileEntity;

	public VirtualSlot(IInventory inventory, int index){
		super(inventory, index, -999, -999);
		if(!(inventory instanceof TileEntity)) throw new IllegalArgumentException("Inventory must be a tile entity!");
		this.tileEntity = (TileEntity)inventory;
	}

	@Override
	public boolean isEnabled(){
		return false; // Virtual slots are never displayed
	}

	// We don't really want to be updating the bookshelves every single tick (let alone every frame!), so we're going
	// to have to do some 'assuming things stay the same until told otherwise'. This means it's possible that virtual
	// slots will remain even when their containers are gone, so we need a failsafe for when that happens.

	/** Returns true if this slot is valid, i.e. the tile entity still exists. */
	public boolean isValid(){
		return !tileEntity.isInvalid();
	}

	// Normally the container decides if a stack is valid, but that's not going to work here
	@Override
	public boolean isItemValid(ItemStack stack){
		// getSlotIndex() is required here, NOT slotNumber (which is only populated when the slot is added to a
		// container - the javadoc is wrong, it has nothing to do with inventories)
		return isValid() && inventory.isItemValidForSlot(getSlotIndex(), stack);
	}

	@Override
	public boolean canTakeStack(EntityPlayer playerIn){
		return isValid() && super.canTakeStack(playerIn);
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack){
		return isValid() ? super.onTake(player, stack) : ItemStack.EMPTY;
	}

	@Override
	public ItemStack getStack(){
		return isValid() ? super.getStack() : ItemStack.EMPTY;
	}

	@Override
	public void putStack(ItemStack stack){
		if(isValid() && inventory instanceof TileEntityBookshelf) ((TileEntityBookshelf)inventory).sync();
		if(isValid()) super.putStack(stack);
	}

	@Override
	public ItemStack decrStackSize(int amount){
		if(isValid() && inventory instanceof TileEntityBookshelf) ((TileEntityBookshelf)inventory).sync();
		return isValid() ? super.decrStackSize(amount) : ItemStack.EMPTY;
	}

}
