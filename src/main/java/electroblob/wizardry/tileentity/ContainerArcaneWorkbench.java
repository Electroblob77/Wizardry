package electroblob.wizardry.tileentity;

import java.util.HashSet;
import java.util.Set;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.event.SpellBindEvent;
import electroblob.wizardry.item.ItemArcaneTome;
import electroblob.wizardry.item.ItemArmourUpgrade;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public class ContainerArcaneWorkbench extends Container {

	/** The arcane workbench tile entity associated with this container. */
	public TileEntityArcaneWorkbench tileentity;

	public static final ResourceLocation EMPTY_SLOT_CRYSTAL = new ResourceLocation(Wizardry.MODID,
			"gui/empty_slot_crystal");
	public static final ResourceLocation EMPTY_SLOT_UPGRADE = new ResourceLocation(Wizardry.MODID,
			"gui/empty_slot_upgrade");

	public static final int CRYSTAL_SLOT = 8;
	public static final int WAND_SLOT = 9;
	public static final int UPGRADE_SLOT = 10;

	private static final int[][][] SPELL_BOOK_SLOT_COORDS = {
			{{80, 22}, {121, 51}, {106, 98}, {54, 98}, {39, 51}, {-999, -999}, {-999, -999}, {-999, -999}},
			{{80, 22}, {117, 43}, {117, 85}, {80, 106}, {43, 85}, {43, 43}, {-999, -999}, {-999, -999}},
			{{80, 22}, {113, 38}, {121, 74}, {98, 102}, {62, 102}, {39, 74}, {47, 38}, {-999, -999}},
			{{80, 22}, {111, 33}, {122, 64}, {111, 95}, {80, 106}, {49, 95}, {38, 64}, {49, 33}}};

	public ContainerArcaneWorkbench(IInventory inventory, TileEntityArcaneWorkbench tileentity){

		this.tileentity = tileentity;

		ItemStack wand = tileentity.getStackInSlot(WAND_SLOT);

		for(int i = 0; i < 8; i++){
			this.addSlotToContainer(new SlotItemList(tileentity, i, -999, -999, 1, WizardryItems.spell_book));
		}

		this.addSlotToContainer(new SlotItemList(tileentity, CRYSTAL_SLOT, 8, 88, 64, WizardryItems.magic_crystal))
				.setBackgroundName(EMPTY_SLOT_CRYSTAL.toString());

		this.addSlotToContainer(new SlotWandArmour(tileentity, WAND_SLOT, 80, 64, this));

		Set<Item> upgrades = new HashSet<Item>(WandHelper.getSpecialUpgrades()); // Can't be done statically.
		upgrades.add(WizardryItems.arcane_tome);
		upgrades.add(WizardryItems.armour_upgrade);

		this.addSlotToContainer(new SlotItemList(tileentity, UPGRADE_SLOT, 8, 106, 1, upgrades.toArray(new Item[0])))
				.setBackgroundName(EMPTY_SLOT_UPGRADE.toString());

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
	public boolean canInteractWith(EntityPlayer player){
		return this.tileentity.isUsableByPlayer(player);
	}

	/** Called from the central wand/armour slot when its item is changed or removed. */
	// In case I forget again and think it should have @Override: I wrote this!
	public void onSlotChanged(int slotNumber, ItemStack stack, EntityPlayer player){

		if(slotNumber == WAND_SLOT){

			if(!(stack.getItem() instanceof ItemWand) && stack.getItem() != WizardryItems.blank_scroll){
				// If the stack has been removed
				for(int i = 0; i < CRYSTAL_SLOT; i++){
					Slot slot1 = this.getSlot(i);
					// 'Removes' the slot from the container (moves it off the screen)
					slot1.xPos = -100;
					slot1.yPos = -100;

					ItemStack stack1 = slot1.getStack();
					// This doesn't cause an infinite loop because slot i can never be a SlotWandArmour. In effect, it's
					// exactly the same as shift-clicking the slot, so why re-invent the wheel?
					ItemStack remainder = this.transferStackInSlot(player, i);

					if(remainder == ItemStack.EMPTY && stack1 != ItemStack.EMPTY){
						slot1.putStack(ItemStack.EMPTY);
						// The second parameter is never used...
						if(player != null) player.dropItem(stack1, false);
					}
				}

			}else{

				if(stack.getItem() == WizardryItems.blank_scroll){
					// If a blank scroll is added
					// The first slot is shown
					this.getSlot(0).xPos = SPELL_BOOK_SLOT_COORDS[0][0][0];
					this.getSlot(0).yPos = SPELL_BOOK_SLOT_COORDS[0][0][1];

					// The rest of the slots are hidden
					for(int i = 1; i < CRYSTAL_SLOT; i++){

						Slot slot1 = this.getSlot(i);

						slot1.xPos = -100;
						slot1.yPos = -100;

						ItemStack stack1 = slot1.getStack();
						// This doesn't cause an infinite loop because slot i can never be a SlotWandArmour. In effect,
						// it's
						// exactly the same as shift-clicking the slot, so why re-invent the wheel?
						ItemStack remainder = this.transferStackInSlot(player, i);

						if(remainder == ItemStack.EMPTY && stack1 != ItemStack.EMPTY){
							slot1.putStack(ItemStack.EMPTY);
							// The second parameter is never used...
							if(player != null) player.dropItem(stack1, false);
						}
					}

				}else{

					for(int i = 0; i < CRYSTAL_SLOT; i++){

						int n = WandHelper.getUpgradeLevel(stack, WizardryItems.attunement_upgrade);
						int[] coords = SPELL_BOOK_SLOT_COORDS[n][i];

						Slot slot1 = this.getSlot(i);
						// Puts the slot back in the correct position
						slot1.xPos = coords[0];
						slot1.yPos = coords[1];

						if(slot1.xPos < 0 || slot1.yPos < 0){

							ItemStack stack1 = slot1.getStack();
							// This doesn't cause an infinite loop because slot i can never be a SlotWandArmour. In
							// effect, it's
							// exactly the same as shift-clicking the slot, so why re-invent the wheel?
							ItemStack remainder = this.transferStackInSlot(player, i);

							if(remainder == ItemStack.EMPTY && stack1 != ItemStack.EMPTY){
								slot1.putStack(ItemStack.EMPTY);
								// The second parameter is never used...
								if(player != null) player.dropItem(stack1, false);
							}
						}
					}
				}
			}
		}

		// FIXME: It only seems to be syncing correctly when a stack is put into the slot, not taken out.
		// Was this broken in 1.7.10 as well?
		this.tileentity.sync();
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int clickedSlotId){

		ItemStack remainder = ItemStack.EMPTY;
		Slot slot = (Slot)this.inventorySlots.get(clickedSlotId);

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
				}else if(stack.getItem() == WizardryItems.magic_crystal){
					minSlotId = CRYSTAL_SLOT;
					maxSlotId = CRYSTAL_SLOT;
				}else if(stack.getItem() instanceof ItemWand || stack.getItem() instanceof ItemWizardArmour
						|| stack.getItem() == WizardryItems.blank_scroll){
					minSlotId = WAND_SLOT;
					maxSlotId = WAND_SLOT;
				}else if(stack.getItem() instanceof ItemArcaneTome || stack.getItem() instanceof ItemArmourUpgrade
						|| WandHelper.isWandUpgrade(stack.getItem())){
					minSlotId = UPGRADE_SLOT;
					maxSlotId = UPGRADE_SLOT;
				}else{
					return ItemStack.EMPTY; // If none of the above cases were true, then the item won't fit in the
											// workbench.
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
	// All operations on the items contained in the inventory simply call the corresponding methods in the tileentity.
	// As of 2.1, for the sake of events and neatness of code, this was moved here from TileEntityArcaneWorkbench.
	public void onApplyButtonPressed(EntityPlayer player){

		ItemStack wand = this.getSlot(WAND_SLOT).getStack();
		ItemStack[] spellBooks = new ItemStack[CRYSTAL_SLOT];
		for(int i = 0; i < spellBooks.length; i++){
			spellBooks[i] = this.getSlot(i).getStack();
		}
		ItemStack crystals = this.getSlot(CRYSTAL_SLOT).getStack();
		ItemStack upgrade = this.getSlot(UPGRADE_SLOT).getStack();

		if(MinecraftForge.EVENT_BUS.post(new SpellBindEvent(player, this))) return;

		// Since the workbench now accepts armour as well as wands, this check is needed.
		if(wand.getItem() instanceof ItemWand){

			// Upgrades wand if necessary. Damage is copied, preserving remaining durability,
			// and also the entire NBT tag compound.
			if(upgrade.getItem() == WizardryItems.arcane_tome){

				ItemStack newWand;

				switch(Tier.values()[upgrade.getItemDamage()]){

				case APPRENTICE:
					if(((ItemWand)wand.getItem()).tier == Tier.BASIC){
						newWand = new ItemStack(WizardryUtilities.getWand(Tier.values()[upgrade.getItemDamage()],
								((ItemWand)wand.getItem()).element));
						newWand.setTagCompound(wand.getTagCompound());
						// This needs to be done after copying the tag compound so the max damage for the new wand
						// takes storage
						// upgrades into account.
						newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
						this.putStackInSlot(WAND_SLOT, newWand);
						this.putStackInSlot(UPGRADE_SLOT, ItemStack.EMPTY);
						WizardryAdvancementTriggers.apprentice.triggerFor(player);
					}
					break;

				case ADVANCED:
					if(((ItemWand)wand.getItem()).tier == Tier.APPRENTICE){
						newWand = new ItemStack(WizardryUtilities.getWand(Tier.values()[upgrade.getItemDamage()],
								((ItemWand)wand.getItem()).element));
						newWand.setTagCompound(wand.getTagCompound());
						newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
						this.putStackInSlot(WAND_SLOT, newWand);
						this.putStackInSlot(UPGRADE_SLOT, ItemStack.EMPTY);
					}
					break;

				case MASTER:
					if(((ItemWand)wand.getItem()).tier == Tier.ADVANCED){
						newWand = new ItemStack(WizardryUtilities.getWand(Tier.values()[upgrade.getItemDamage()],
								((ItemWand)wand.getItem()).element));
						newWand.setTagCompound(wand.getTagCompound());
						newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
						this.putStackInSlot(WAND_SLOT, newWand);
						this.putStackInSlot(UPGRADE_SLOT, ItemStack.EMPTY);
						WizardryAdvancementTriggers.master.triggerFor(player);
					}
					break;

				default:
					break;
				}

				// This needs to happen so the charging works on the new wand, not the old one.
				wand = this.getSlot(WAND_SLOT).getStack();

			}else{

				// Special upgrades

				// Used to preserve existing mana when upgrading storage rather than creating free mana.
				int prevMana = wand.getMaxDamage() - wand.getItemDamage();

				if(WandHelper.getTotalUpgrades(wand) < ((ItemWand)wand.getItem()).tier.upgradeLimit
						&& WandHelper.getUpgradeLevel(wand, upgrade.getItem()) < Constants.UPGRADE_STACK_LIMIT){

					WandHelper.applyUpgrade(wand, upgrade.getItem());

					// Special behaviours for specific upgrades
					if(upgrade.getItem() == WizardryItems.storage_upgrade){
						wand.setItemDamage(wand.getMaxDamage() - prevMana);
					}
					if(upgrade.getItem() == WizardryItems.attunement_upgrade){

						Spell[] spells = WandHelper.getSpells(wand);
						Spell[] newSpells = new Spell[5
								+ WandHelper.getUpgradeLevel(wand, WizardryItems.attunement_upgrade)];

						for(int i = 0; i < newSpells.length; i++){
							// Prevents both NPEs and AIOOBEs
							newSpells[i] = i < spells.length && spells[i] != null ? spells[i] : Spells.none;
						}

						WandHelper.setSpells(wand, newSpells);

						int[] cooldown = WandHelper.getCooldowns(wand);
						int[] newCooldown = new int[5
								+ WandHelper.getUpgradeLevel(wand, WizardryItems.attunement_upgrade)];

						if(cooldown.length > 0){
							for(int i = 0; i < cooldown.length; i++){
								newCooldown[i] = cooldown[i];
							}
						}

						WandHelper.setCooldowns(wand, newCooldown);
					}

					this.getSlot(UPGRADE_SLOT).decrStackSize(1);
					WizardryAdvancementTriggers.special_upgrade.triggerFor(player);

					if(WandHelper.getTotalUpgrades(wand) == Tier.MASTER.upgradeLimit){
						WizardryAdvancementTriggers.max_out_wand.triggerFor(player);
					}
				}
			}

			// Reads NBT spell id array to variable, edits this, then writes it back to NBT.
			// Original spells are preserved; if a slot is left empty the existing spell binding will remain.
			// Accounts for spells which cannot be applied because they are above the wand's tier; these spells
			// will not bind but the existing spell in that slot will remain and other applicable spells will
			// be bound as normal, along with any upgrades and crystals.
			Spell[] spells = WandHelper.getSpells(wand);
			if(spells.length <= 0){
				// 5 here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
				spells = new Spell[5];
			}
			for(int i = 0; i < spells.length; i++){
				if(spellBooks[i] != ItemStack.EMPTY && !(Spell
						.get(spellBooks[i].getItemDamage()).tier.level > ((ItemWand)wand.getItem()).tier.level)){
					spells[i] = Spell.get(spellBooks[i].getItemDamage());
				}
			}
			WandHelper.setSpells(wand, spells);

			// Charges wand by appropriate amount
			if(crystals != ItemStack.EMPTY){
				int chargeDepleted = wand.getItemDamage();
				// System.out.println("Charge depleted: " + chargeDepleted);
				// System.out.println("Crystals found: " + crystals.getCount());
				if(crystals.getCount() * Constants.MANA_PER_CRYSTAL < chargeDepleted){
					// System.out.println("charging");
					wand.setItemDamage(chargeDepleted - crystals.getCount() * Constants.MANA_PER_CRYSTAL);
					this.getSlot(CRYSTAL_SLOT).decrStackSize(crystals.getCount());
				}else if(chargeDepleted != 0){
					// System.out.println((int)Math.ceil(((double)chargeDepleted)/50));
					this.getSlot(CRYSTAL_SLOT)
							.decrStackSize((int)Math.ceil(((double)chargeDepleted) / Constants.MANA_PER_CRYSTAL));
					wand.setItemDamage(0);
				}
			}
		}

		// Armour
		else if(wand.getItem() instanceof ItemWizardArmour){
			// Applies legendary upgrade
			if(upgrade.getItem() == WizardryItems.armour_upgrade){
				if(!wand.hasTagCompound()){
					wand.setTagCompound(new NBTTagCompound());
				}
				if(!wand.getTagCompound().hasKey("legendary")){
					wand.getTagCompound().setBoolean("legendary", true);
					this.putStackInSlot(UPGRADE_SLOT, ItemStack.EMPTY);
					WizardryAdvancementTriggers.legendary.triggerFor(player);
				}
			}
			// Charges armour by appropriate amount
			if(crystals != ItemStack.EMPTY){
				int chargeDepleted = wand.getItemDamage();
				if(crystals.getCount() * Constants.MANA_PER_CRYSTAL < chargeDepleted){
					wand.setItemDamage(chargeDepleted - crystals.getCount() * Constants.MANA_PER_CRYSTAL);
					this.getSlot(CRYSTAL_SLOT).decrStackSize(crystals.getCount());
				}else if(chargeDepleted != 0){
					this.getSlot(CRYSTAL_SLOT)
							.decrStackSize((int)Math.ceil(((double)chargeDepleted) / Constants.MANA_PER_CRYSTAL));
					wand.setItemDamage(0);
				}
			}
		}

		// Scrolls
		else if(wand.getItem() == WizardryItems.blank_scroll){
			// Spells can only be bound to scrolls if the player has already cast them (prevents casting of master
			// spells without getting a master wand)
			// This restriction does not apply in creative mode
			if(spellBooks[0] != ItemStack.EMPTY
					&& (player.capabilities.isCreativeMode || (WizardData.get(player) != null
							&& WizardData.get(player).hasSpellBeenDiscovered(Spell.get(spellBooks[0].getItemDamage()))))
					&& crystals != ItemStack.EMPTY && crystals.getCount()
							* Constants.MANA_PER_CRYSTAL > Spell.get(spellBooks[0].getItemDamage()).cost){

				this.getSlot(CRYSTAL_SLOT).decrStackSize((int)Math
						.ceil(((double)Spell.get(spellBooks[0].getItemDamage()).cost) / Constants.MANA_PER_CRYSTAL));
				this.putStackInSlot(WAND_SLOT, new ItemStack(WizardryItems.scroll, 1, spellBooks[0].getItemDamage()));
			}
		}
	}

}
