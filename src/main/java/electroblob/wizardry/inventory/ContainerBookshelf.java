package electroblob.wizardry.inventory;

import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ContainerBookshelf extends Container {

	/** The bookshelf tile entity associated with this container. */
	public TileEntityBookshelf tileentity;

	public ContainerBookshelf(IInventory inventory, TileEntityBookshelf tileentity){

		this.tileentity = tileentity;

		for(int y = 0; y < 2; y++){
			for(int x = 0; x < BlockBookshelf.SLOT_COUNT / 2; x++){
				this.addSlotToContainer(new SlotBookshelf(tileentity, x + BlockBookshelf.SLOT_COUNT / 2 * y, 35 + x * 18, 17 + y * 18, 64, ItemSpellBook.class));
			}
		}

		for(int x = 0; x < 9; x++){
			this.addSlotToContainer(new Slot(inventory, x, 8 + x * 18, 124));
		}

		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 9; x++){
				this.addSlotToContainer(new Slot(inventory, 9 + x + y * 9, 8 + x * 18, 66 + y * 18));
			}
		}

	}

	/** Called from individual slots when their item is changed or removed. */
	public void onSlotChanged(){
		this.tileentity.sync();
	}

	@Override
	public boolean canInteractWith(EntityPlayer player){
		return this.tileentity.isUsableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int clickedSlotId){

		ItemStack remainder = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(clickedSlotId);

		if(slot != null && slot.getHasStack()){

			ItemStack stack = slot.getStack(); // The stack that was there originally
			remainder = stack.copy(); // A copy of that stack

			// Bookshelf -> inventory
			if(clickedSlotId < BlockBookshelf.SLOT_COUNT){
				// Tries to move the stack into the player's inventory. If this fails...
				if(!this.mergeItemStack(stack, BlockBookshelf.SLOT_COUNT, this.inventorySlots.size(), true)){
					return ItemStack.EMPTY; // ...nothing else happens.
				}
			}
			// Inventory -> bookshelf
			else{

				int minSlotId = 0;
				int maxSlotId = BlockBookshelf.SLOT_COUNT - 1;

				if(!(stack.getItem() instanceof ItemSpellBook)) return ItemStack.EMPTY;

				if(!this.mergeItemStack(stack, minSlotId, maxSlotId + 1, false)){
					return ItemStack.EMPTY;
				}
			}

			if(stack.getCount() == 0){
				slot.putStack(ItemStack.EMPTY);
			}else{
				slot.onSlotChanged();
			}

			if(stack.getCount() == remainder.getCount()){
				return ItemStack.EMPTY;
			}

			slot.onTake(player, stack);
		}

		return remainder;
	}

	public class SlotBookshelf extends SlotItemClassList {

		@SafeVarargs
		public SlotBookshelf(IInventory inventory, int index, int x, int y, int stackLimit, Class<? extends Item>... allowedItemClasses){
			super(inventory, index, x, y, stackLimit, allowedItemClasses);
		}

		@Override
		public void putStack(ItemStack stack){
			boolean statusChanged = this.getStack().isEmpty() != stack.isEmpty();
			super.putStack(stack);
			if(statusChanged) ContainerBookshelf.this.onSlotChanged();
		}

		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack){
			ItemStack result = super.onTake(player, stack);
			ContainerBookshelf.this.onSlotChanged();
			return result;
		}

	}

}
