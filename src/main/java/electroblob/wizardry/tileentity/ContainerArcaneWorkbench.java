package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.SpellBindEvent;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;

import java.util.HashSet;
import java.util.Set;

public class ContainerArcaneWorkbench extends Container {

	/** The arcane workbench tile entity associated with this container. */
	public TileEntityArcaneWorkbench tileentity;

	public static final ResourceLocation EMPTY_SLOT_CRYSTAL = new ResourceLocation(Wizardry.MODID, "gui/empty_slot_crystal");
	public static final ResourceLocation EMPTY_SLOT_UPGRADE = new ResourceLocation(Wizardry.MODID, "gui/empty_slot_upgrade");

	public static final int CRYSTAL_SLOT = 8;
	public static final int CENTRE_SLOT = 9;
	public static final int UPGRADE_SLOT = 10;
	
	public static final int SLOT_RADIUS = 42;

	public ContainerArcaneWorkbench(IInventory inventory, TileEntityArcaneWorkbench tileentity){

		this.tileentity = tileentity;

		ItemStack wand = tileentity.getStackInSlot(CENTRE_SLOT);

		for(int i = 0; i < 8; i++){
			Slot slot = new SlotItemClassList(tileentity, i, -999, -999, 1, ItemSpellBook.class);
			this.addSlotToContainer(slot);
		}

		this.addSlotToContainer(new SlotItemList(tileentity, CRYSTAL_SLOT, 13, 101, 64,
				WizardryItems.magic_crystal, WizardryItems.crystal_shard, WizardryItems.grand_crystal))
				.setBackgroundName(EMPTY_SLOT_CRYSTAL.toString());

		this.addSlotToContainer(new SlotWorkbenchItem(tileentity, CENTRE_SLOT, 80, 64, this));

		Set<Item> upgrades = new HashSet<>(WandHelper.getSpecialUpgrades()); // Can't be done statically.
		upgrades.add(WizardryItems.arcane_tome);
		upgrades.add(WizardryItems.armour_upgrade);

		this.addSlotToContainer(new SlotItemList(tileentity, UPGRADE_SLOT, 147, 17, 1, upgrades.toArray(new Item[0])))
				.setBackgroundName(EMPTY_SLOT_UPGRADE.toString());

		for(int x = 0; x < 9; x++){
			this.addSlotToContainer(new Slot(inventory, x, 8 + x * 18, 196));
		}

		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 9; x++){
				this.addSlotToContainer(new Slot(inventory, 9 + x + y * 9, 8 + x * 18, 138 + y * 18));
			}
		}

		this.onSlotChanged(CENTRE_SLOT, wand, null);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player){
		return this.tileentity.isUsableByPlayer(player);
	}
	
	/**
	 * Shows the given slot in the container GUI at the given position. Intended to do the opposite of
	 * {@link ContainerArcaneWorkbench#hideSlot(int, EntityPlayer)}.
	 * @param index The index of the slot to show.
	 * @param x The x position to put the slot in.
	 * @param y The y position to put the slot in.
	 */
	private void showSlot(int index, int x, int y){
		
		Slot slot = this.getSlot(index);
		slot.xPos = x;
		slot.yPos = y;
	}
	
	/**
	 * Hides the given slot from the container GUI (moves it off the screen) and returns its contents to the given
	 * player. If some or all of the items do not fit in the player's inventory, or if the player is null, they are
	 * dropped on the floor.
	 * @param index The index of the slot to hide.
	 * @param player The player that is using this container.
	 */
	private void hideSlot(int index, EntityPlayer player){
		
		Slot slot = this.getSlot(index);
		
		// 'Removes' the slot from the container (moves it off the screen)
		slot.xPos = -999;
		slot.yPos = -999;

		ItemStack stack = slot.getStack();
		// This doesn't cause an infinite loop because slot i can never be a SlotWandArmour. In effect, it's
		// exactly the same as shift-clicking the slot, so why re-invent the wheel?
		ItemStack remainder = this.transferStackInSlot(player, index);

		if(remainder == ItemStack.EMPTY && stack != ItemStack.EMPTY){
			slot.putStack(ItemStack.EMPTY);
			// The second parameter is never used...
			if(player != null) player.dropItem(stack, false);
		}
	}

	/** Called from the central wand/armour slot when its item is changed or removed. */
	// In case I forget again and think it should have @Override: I wrote this!
	public void onSlotChanged(int slotNumber, ItemStack stack, EntityPlayer player){

		if(slotNumber == CENTRE_SLOT){

			if(stack.isEmpty()){
				// If the stack has been removed, hide all the spell book slots
				for(int i = 0; i < CRYSTAL_SLOT; i++){
					this.hideSlot(i, player);
				}

			}else{
				
				if(stack.getItem() instanceof IWorkbenchItem){ // Should always be true here.
					
					int spellSlots = ((IWorkbenchItem)stack.getItem()).getSpellSlotCount(stack);
					
					int centreX = this.getSlot(CENTRE_SLOT).xPos;
					int centreY = this.getSlot(CENTRE_SLOT).yPos;
					
					// Show however many spell book slots are necessary
					for(int i = 0; i < spellSlots; i++){
						
						float angle = i * (2 * (float)Math.PI)/spellSlots;
						int x = centreX + Math.round(SLOT_RADIUS * MathHelper.sin(angle));
						// -cos because +y is downwards
						int y = centreY + Math.round(SLOT_RADIUS * -MathHelper.cos(angle));
						
						showSlot(i, x, y);
					}
					
					// Hide the rest
					for(int i = spellSlots; i < CRYSTAL_SLOT; i++){
						hideSlot(i, player);
					}
					
				}
			}
		}

		// FIXME: It only seems to be syncing correctly when a stack is put into the slot, not taken out.
		// 		  This is because markDirty isn't called in the tileentity, I think.
		this.tileentity.sync();
	}

	// FIXME: Shift-clicking a stack of special upgrades when in the arcane workbench causes the whole stack to be
	//		  transferred when it should be just one (this is a bug with vanilla as well - try putting a stack of
	//		  bottles into a brewing stand). I have at least made it so only one gets used now, so it has no impact on
	//		  the game.
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int clickedSlotId){

		ItemStack remainder = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(clickedSlotId);

		if(slot != null && slot.getHasStack()){

			ItemStack stack = slot.getStack(); // The stack that was there originally
			remainder = stack.copy(); // A copy of that stack

			// Workbench -> inventory
			if(clickedSlotId <= UPGRADE_SLOT){
				// Tries to move the stack into the player's inventory. If this fails...
				if(!this.mergeItemStack(stack, UPGRADE_SLOT + 1, this.inventorySlots.size(), true)){
					return ItemStack.EMPTY; // ...nothing else happens.
				}
			}
			// Inventory -> workbench
			else{
				// The following logic prevents shift-clicking transferring the items to the wrong slot.
				int minSlotId = 0;
				int maxSlotId = UPGRADE_SLOT;

				if(stack.getItem() instanceof ItemSpellBook){
					minSlotId = 0;
					maxSlotId = CRYSTAL_SLOT - 1;
				}else if(getSlot(CRYSTAL_SLOT).isItemValid(stack)){
					minSlotId = CRYSTAL_SLOT;
					maxSlotId = CRYSTAL_SLOT;
				}else if(getSlot(CENTRE_SLOT).isItemValid(stack)){
					minSlotId = CENTRE_SLOT;
					maxSlotId = CENTRE_SLOT;
				}else if(getSlot(UPGRADE_SLOT).isItemValid(stack)){
					minSlotId = UPGRADE_SLOT;
					maxSlotId = UPGRADE_SLOT;
				}else{
					// If none of the above cases were true, then the item won't fit in the workbench.
					return ItemStack.EMPTY;
				}

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

	// Overridden to stop stacks merging into 'removed' slots.
	@Override
	protected boolean mergeItemStack(ItemStack stack, int minSlotID, int maxSlotID, boolean p_75135_4_){

		for(int i = minSlotID; i < maxSlotID; i++){
			// System.out.println(this.getSlot(i).xDisplayPosition);
			if(this.getSlot(i).xPos >= 0 && this.getSlot(i).yPos >= 0 && !this.getSlot(i).getHasStack()){
				return super.mergeItemStack(stack, minSlotID, maxSlotID, p_75135_4_);
			}
		}
		// Only returns false if none of the slots given are enabled and empty
		return false;
	}

	/**
	 * Called (via {@link electroblob.wizardry.packet.PacketControlInput PacketControlInput}) when the apply button in
	 * the arcane workbench GUI is pressed.
	 */
	// As of 2.1, for the sake of events and neatness of code, this was moved here from TileEntityArcaneWorkbench.
	// As of 4.2, the spell binding/charging/upgrading code was delegated (via IWorkbenchItem) to the items themselves.
	public void onApplyButtonPressed(EntityPlayer player){

		if(MinecraftForge.EVENT_BUS.post(new SpellBindEvent(player, this))) return;
		
		Slot centre = this.getSlot(CENTRE_SLOT);
		
		if(centre.getStack().getItem() instanceof IWorkbenchItem){ // Should always be true, but no harm in checking.
			
			Slot[] spellBooks = this.inventorySlots.subList(0, 8).toArray(new Slot[8]);
			
			if(((IWorkbenchItem)centre.getStack().getItem())
				.onApplyButtonPressed(player, centre, this.getSlot(CRYSTAL_SLOT), this.getSlot(UPGRADE_SLOT), spellBooks)){

				if(player instanceof EntityPlayerMP){
					WizardryAdvancementTriggers.arcane_workbench.trigger((EntityPlayerMP)player, centre.getStack());
				}
			}
		}
	}

}
