package electroblob.wizardry.inventory;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ContainerBookshelf extends Container {

	private static final Set<Item> validItems = new HashSet<>();

	/** The bookshelf tile entity associated with this container. */
	public TileEntityBookshelf tileentity;

	public ContainerBookshelf(IInventory inventory, TileEntityBookshelf tileentity){

		this.tileentity = tileentity;

		for(int y = 0; y < 2; y++){
			for(int x = 0; x < BlockBookshelf.SLOT_COUNT / 2; x++){
				this.addSlotToContainer(new SlotBookshelf(tileentity, x + BlockBookshelf.SLOT_COUNT / 2 * y, 35 + x * 18, 17 + y * 18));
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

				if(!isBook(stack)) return ItemStack.EMPTY;

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

	/** Returns true if the given stack counts as a book and can be placed in a bookshelf, false if not. */
	public static boolean isBook(ItemStack stack){
		return validItems.contains(stack.getItem()) || Settings.containsMetaItem(Wizardry.settings.bookItems, stack);
	}

	/**
	 * Adds the given item to the set of items that can be put in a bookshelf. This method should be called from the
	 * {@code init()} phase.
	 * @param item The item to register
	 * @see BlockBookshelf#registerBookModelTexture(Supplier, ResourceLocation)
	 */
	public static void registerBookItem(Item item){
		validItems.add(item);
	}

	/** Called from {@link Wizardry#init(FMLInitializationEvent)} to register the default book items. */
	public static void initDefaultBookItems(){
		registerBookItem(WizardryItems.spell_book);
		registerBookItem(WizardryItems.arcane_tome);
		registerBookItem(WizardryItems.wizard_handbook);
		registerBookItem(Items.BOOK);
		registerBookItem(Items.WRITTEN_BOOK);
		registerBookItem(Items.WRITABLE_BOOK);
		registerBookItem(Items.ENCHANTED_BOOK);
	}

	public class SlotBookshelf extends Slot {

		public SlotBookshelf(IInventory inventory, int index, int x, int y){
			super(inventory, index, x, y);
		}

		@Override
		public void putStack(ItemStack stack){
			boolean statusChanged = this.getStack().isEmpty() != stack.isEmpty()
					|| BlockBookshelf.getBookItems().indexOf(this.getStack().getItem())
					!= BlockBookshelf.getBookItems().indexOf(stack.getItem());
			super.putStack(stack);
			if(statusChanged) ContainerBookshelf.this.onSlotChanged();
		}

		@Override
		public ItemStack onTake(EntityPlayer player, ItemStack stack){
			ItemStack result = super.onTake(player, stack);
			ContainerBookshelf.this.onSlotChanged();
			return result;
		}

		@Override
		public boolean isItemValid(ItemStack stack){
			return isBook(stack);
		}
	}

}
