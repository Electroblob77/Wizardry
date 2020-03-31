package electroblob.wizardry.inventory;

import electroblob.wizardry.item.ItemSpellBook;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import java.util.List;

public class SlotBookList extends SlotItemClassList {

	private final ContainerArcaneWorkbench container;
	/** The position in the book list this slot occupies (in other words: the slot index, but offset so the first book
	 * list slot starts at 0). */
	private final int listIndex;

	public SlotBookList(IInventory inventory, int index, int x, int y, ContainerArcaneWorkbench container, int listIndex){
		super(inventory, index, x, y, 64, ItemSpellBook.class);
		this.container = container;
		this.listIndex = listIndex;
	}

	/** Returns the {@link VirtualSlot} this slot currently accesses, or null if it does not currently have one. */
	public VirtualSlot getDelegate(){
		return hasDelegate() ? container.getVisibleBookshelfSlots().get(listIndex) : null;
	}

	/** Returns true if this slot currently accesses a {@link VirtualSlot}, false otherwise. */
	public boolean hasDelegate(){
		return listIndex < container.getVisibleBookshelfSlots().size();
	}

	@Override
	public ItemStack getStack(){
		return hasDelegate() ? getDelegate().getStack() : ItemStack.EMPTY; // Delegate item lookup to virtual slot
	}

	// This doesn't depend on the client-side search and sorting stuff so it can be done in here
	@Override
	public boolean isItemValid(ItemStack stack){
		// It's valid if there is a virtual slot that will accept it
		return container.getBookshelfSlots().stream().anyMatch(s -> s.isItemValid(stack));
	}

	// Actual item interaction is delegated client-side before it ever gets here (in the GUI class)
	// Therefore we only need to handle empty slot clicks

	@Override
	public ItemStack decrStackSize(int amount){
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player){
		return false;
	}

	@Override
	public ItemStack onTake(EntityPlayer player, ItemStack stack){
		return stack;
	}

	@Override
	public void putStack(ItemStack stack){
		// Can't handle this here either because it gets tangled up with the 348-line behemoth that is
		// Container#slotClick, which has all sorts of side-effects
		// Instead, we need to delegate client-side, as before
	}

}
