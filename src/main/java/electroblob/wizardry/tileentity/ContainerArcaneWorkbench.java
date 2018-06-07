package electroblob.wizardry.tileentity;

import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArmourUpgrade;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ContainerArcaneWorkbench extends Container
{
    public TileEntityArcaneWorkbench tileEntityArcaneWorkbench;
    
    public static final int CRYSTAL_SLOT = 8;
    public static final int WAND_SLOT = 9;
    public static final int UPGRADE_SLOT = 10;
    
    private static final int[][][] spellBookSlotCoords = {
    		{{80, 22}, {121, 51}, {106, 98}, {54, 98}, {39, 51}, {-999, -999}, {-999, -999}, {-999, -999}},
    		{{80, 22}, {117, 43}, {117, 85}, {80, 106}, {43, 85}, {43, 43}, {-999, -999}, {-999, -999}},
    		{{80, 22}, {113, 38}, {121, 74}, {98, 102}, {62, 102}, {39, 74}, {47, 38}, {-999, -999}},
    		{{80, 22}, {111, 33}, {122, 64}, {111, 95}, {80, 106}, {49, 95}, {38, 64}, {49, 33}}
    };
    
    // Does slot.func_146977_a have something to do with whether the slot is enabled?

    public ContainerArcaneWorkbench(IInventory inventory, TileEntityArcaneWorkbench tileentity){
    	
        this.tileEntityArcaneWorkbench = tileentity;
        
        ItemStack wand = tileentity.getStackInSlot(WAND_SLOT);
        
        for(int i=0; i<8; i++){
        	this.addSlotToContainer(new SlotWizardry(tileentity, i, -999, -999, 1, -1, Wizardry.spellBook));
        }
        
        this.addSlotToContainer(new SlotWizardry(tileentity, CRYSTAL_SLOT, 8, 88, 64, 1, Wizardry.magicCrystal));
        this.addSlotToContainer(new SlotWandArmour(tileentity, WAND_SLOT, 80, 64, this));
        this.addSlotToContainer(new SlotWizardry(tileentity, UPGRADE_SLOT, 8, 106, 1, 2, Wizardry.arcaneTome,
        		Wizardry.condenserUpgrade, Wizardry.siphonUpgrade, Wizardry.storageUpgrade, Wizardry.rangeUpgrade,
        		Wizardry.durationUpgrade, Wizardry.cooldownUpgrade, Wizardry.blastUpgrade, Wizardry.attunementUpgrade,
        		Wizardry.armourUpgrade));

        for(int x = 0; x < 9; x++){
			this.addSlotToContainer(new Slot(inventory, x, 8 + x * 18, 196));
		}
		
		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 9; x++){
				this.addSlotToContainer(new Slot(inventory, 9 + x + y * 9, 8 + x * 18, 138 + y * 18));
			}
		}
		
		this.onSlotChanged(WAND_SLOT, wand, null);
    }
    
    @Override
    public boolean canInteractWith(EntityPlayer par1EntityPlayer)
    {
        return this.tileEntityArcaneWorkbench.isUseableByPlayer(par1EntityPlayer);
    }
    
    public void onSlotChanged(int slotNumber, ItemStack stack, EntityPlayer player) {

    	if(slotNumber == WAND_SLOT){
    		
    		if(stack == null || (!(stack.getItem() instanceof ItemWand) && stack.getItem() != Wizardry.blankScroll)){
        		// If the stack has been removed
    			for(int i=0; i<CRYSTAL_SLOT; i++){
        			Slot slot1 = this.getSlot(i);
    				// 'Removes' the slot from the container (moves it off the screen)
    				slot1.xDisplayPosition = -100;
    				slot1.yDisplayPosition = -100;
    				
    				ItemStack stack1 = slot1.getStack();
    				// This doesn't cause an infinite loop because slot i can never be a SlotWandArmour. In effect, it's
    				// exactly the same as shift-clicking the slot, so why re-invent the wheel?
    				ItemStack remainder = this.transferStackInSlot(player, i);
    				
    				if(remainder == null && stack1 != null){
    					slot1.putStack(null);
    					// The second parameter is never used...
    					if(player != null) player.dropPlayerItemWithRandomChoice(stack1, false);
    				}
    			}
    			
    		}else{
    			
    			if(stack.getItem() == Wizardry.blankScroll){
    				// If a blank scroll is added
    				// The first slot is shown
    				this.getSlot(0).xDisplayPosition = this.spellBookSlotCoords[0][0][0];
    				this.getSlot(0).yDisplayPosition = this.spellBookSlotCoords[0][0][1];
    				
    				// The rest of the slots are hidden
    				for(int i=1; i<CRYSTAL_SLOT; i++){
    					
    					Slot slot1 = this.getSlot(i);
    					
        				slot1.xDisplayPosition = -100;
        				slot1.yDisplayPosition = -100;
        				
        				ItemStack stack1 = slot1.getStack();
        				// This doesn't cause an infinite loop because slot i can never be a SlotWandArmour. In effect, it's
        				// exactly the same as shift-clicking the slot, so why re-invent the wheel?
        				ItemStack remainder = this.transferStackInSlot(player, i);
        				
        				if(remainder == null && stack1 != null){
        					slot1.putStack(null);
        					// The second parameter is never used...
        					if(player != null) player.dropPlayerItemWithRandomChoice(stack1, false);
        				}
    				}
    				
    			}else{
	    			
	    			for(int i=0; i<CRYSTAL_SLOT; i++){
	    				
		    			int n = WandHelper.getUpgradeLevel(stack, Wizardry.attunementUpgrade);
	    				int[] coords = this.spellBookSlotCoords[n][i];
	    				
	    				Slot slot1 = this.getSlot(i);
	    				// Puts the slot back in the correct position
	    				slot1.xDisplayPosition = coords[0];
	    				slot1.yDisplayPosition = coords[1];
	    				
	    				if(slot1.xDisplayPosition < 0 || slot1.yDisplayPosition < 0){
	    					
	    					ItemStack stack1 = slot1.getStack();
	        				// This doesn't cause an infinite loop because slot i can never be a SlotWandArmour. In effect, it's
	        				// exactly the same as shift-clicking the slot, so why re-invent the wheel?
	        				ItemStack remainder = this.transferStackInSlot(player, i);
	        				
	        				if(remainder == null && stack1 != null){
	        					slot1.putStack(null);
	        					// The second parameter is never used...
	        					if(player != null) player.dropPlayerItemWithRandomChoice(stack1, false);
	        				}
	    				}
	    			}
    			}
    			
    		}
    	}
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int clickedSlotId)
    {
        ItemStack remainder = null;
        Slot slot = (Slot)this.inventorySlots.get(clickedSlotId);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack = slot.getStack(); // The stack that was there originally
            remainder = itemstack.copy(); // A copy of that stack

            // Workbench -> inventory
            if(clickedSlotId <= UPGRADE_SLOT){
	            // Tries to move the stack into the player's inventory. If this fails...
	            if (!this.mergeItemStack(itemstack, UPGRADE_SLOT + 1, this.inventorySlots.size(), true)){
	                    return null; // ...nothing else happens.
	            }
                
            // Inventory -> workbench
            }else{
            	// The following logic prevents shift-clicking transferring the items to the wrong slot.
            	int minSlotId = 0;
            	int maxSlotId = UPGRADE_SLOT;
            	
            	if(itemstack.getItem() instanceof ItemSpellBook){
            		minSlotId = 0;
            		maxSlotId = CRYSTAL_SLOT-1;
            	}
            	else if(itemstack.getItem() == Wizardry.magicCrystal){
            		minSlotId = CRYSTAL_SLOT;
            		maxSlotId = CRYSTAL_SLOT;
            	}
            	else if(itemstack.getItem() instanceof ItemWand || itemstack.getItem() instanceof ItemWizardArmour
            			|| itemstack.getItem() == Wizardry.blankScroll){
            		minSlotId = WAND_SLOT;
            		maxSlotId = WAND_SLOT;
            	}
            	else if(itemstack.getItem() instanceof ItemArcaneTome
            			|| itemstack.getItem() instanceof ItemArmourUpgrade
            			|| itemstack.getItem() == Wizardry.condenserUpgrade
            			|| itemstack.getItem() == Wizardry.siphonUpgrade
            			|| itemstack.getItem() == Wizardry.rangeUpgrade
            			|| itemstack.getItem() == Wizardry.cooldownUpgrade
            			|| itemstack.getItem() == Wizardry.durationUpgrade
            			|| itemstack.getItem() == Wizardry.storageUpgrade
            			|| itemstack.getItem() == Wizardry.blastUpgrade
            			|| itemstack.getItem() == Wizardry.attunementUpgrade){
            		minSlotId = UPGRADE_SLOT;
            		maxSlotId = UPGRADE_SLOT;
            	}
            	else{
            		return null; // If none of the above cases were true, then the item won't fit in the workbench.
            	}
            	
            	if(!this.mergeItemStack(itemstack, minSlotId, maxSlotId + 1, false))
                {
                    return null;
                }
            }

            if (itemstack.stackSize == 0)
            {
                slot.putStack((ItemStack)null);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack.stackSize == remainder.stackSize)
            {
                return null;
            }

            slot.onPickupFromSlot(par1EntityPlayer, itemstack);
        }

        return remainder;
    }
    
    // Overridden to stop stacks merging into 'removed' slots.
    @Override
    protected boolean mergeItemStack(ItemStack stack, int minSlotID, int maxSlotID, boolean p_75135_4_) {
    	
    	for(int i=minSlotID; i<maxSlotID; i++){
    		//System.out.println(this.getSlot(i).xDisplayPosition);
    		if(this.getSlot(i).xDisplayPosition >= 0 && this.getSlot(i).yDisplayPosition >= 0 && !this.getSlot(i).getHasStack()){
    	    	return super.mergeItemStack(stack, minSlotID, maxSlotID, p_75135_4_);
    		}
    	}
    	// Only returns false if none of the slots given are enabled and empty
    	return false;
    }

}
