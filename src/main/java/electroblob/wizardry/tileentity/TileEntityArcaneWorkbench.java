package electroblob.wizardry.tileentity;

import java.util.HashSet;
import java.util.Set;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAchievements;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityArcaneWorkbench extends TileEntity implements IInventory, ITickable {

	private ItemStack[] inv;
	/** Controls the rotation of the rune. */
	public float timer = 0;
	/** Controls the change of yOffset. */
	public int yTimer = 0;
	/** Controls the height of the wand. */
	public int yOffset = 300;

	public TileEntityArcaneWorkbench(){
		inv = new ItemStack[ContainerArcaneWorkbench.UPGRADE_SLOT + 1];
	}
	
	@Override
	public void onLoad(){
		timer = 0;
		yTimer = 0;
		yOffset = 300;
	}
	
	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		this.worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 3);
	}

	@Override
	public void update(){

		ItemStack itemstack = this.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);

		// Decrements wand damage (increases mana) every 1.5 seconds if it has a condenser upgrade
		if(itemstack != null && itemstack.getItem() instanceof ItemWand && !this.worldObj.isRemote && itemstack.isItemDamaged()
				&& this.worldObj.getWorldTime() % electroblob.wizardry.constants.Constants.CONDENSER_TICK_INTERVAL == 0){
			// If the upgrade level is 0, this does nothing anyway.
			itemstack.setItemDamage(itemstack.getItemDamage() - WandHelper.getUpgradeLevel(itemstack, WizardryItems.condenser_upgrade));
		}

		// The server doesn't care what these are, and there's no need for them to be synced or saved.
		if(this.worldObj.isRemote){
			if(timer < 359){
				timer++;
			}else{
				timer = 0;
			}
			if(yOffset > 0){
				yTimer--;
			}else{
				yTimer++;
			}
			yOffset += yTimer;
		}
	}

	@Override
	public int getSizeInventory() {
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize <= amt) {
				setInventorySlotContents(slot, null);
			} else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			setInventorySlotContents(slot, null);
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inv[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}               
	}

	@Override
	public String getName() {
		return "container.wizardry:arcane_workbench";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(pos) == this && player.getDistanceSqToCenter(pos) < 64;
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemstack) {

		if(itemstack == null) return true;

		if(slotNumber >= 0 && slotNumber < ContainerArcaneWorkbench.CRYSTAL_SLOT){
			return itemstack.getItem() == WizardryItems.spell_book;

		}else if(slotNumber == ContainerArcaneWorkbench.CRYSTAL_SLOT){
			return itemstack.getItem() == WizardryItems.magic_crystal;

		}else if(slotNumber == ContainerArcaneWorkbench.WAND_SLOT){
			return itemstack != null && (itemstack.getItem() instanceof ItemWand
					|| itemstack.getItem() instanceof ItemWizardArmour
					|| itemstack.getItem() == WizardryItems.blank_scroll);

		}else if(slotNumber == ContainerArcaneWorkbench.UPGRADE_SLOT){
			Set<Item> upgrades = new HashSet<Item>(WandHelper.getSpecialUpgrades());
			upgrades.add(WizardryItems.arcane_tome);
			upgrades.add(WizardryItems.armour_upgrade);
			return upgrades.contains(itemstack.getItem());
		}

		return true;

	}
	@Override
	public void readFromNBT(NBTTagCompound tagCompound){
		
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", NBT.TAG_COMPOUND);
		for(int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inv.length) {
				inv[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}

//		timer = tagCompound.getFloat("timer");
//		yTimer = tagCompound.getInteger("yTimer");
//		yOffset = tagCompound.getInteger("yOffset");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {

		super.writeToNBT(tagCompound);

		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inv.length; i++) {
			ItemStack stack = inv[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}

		tagCompound.setTag("Inventory", itemList);
//		tagCompound.setFloat("timer", timer);
//		tagCompound.setInteger("yTimer", yTimer);
//		tagCompound.setInteger("yOffset", yOffset);

		return tagCompound;
	}
	
	@Override
	public final NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		AxisAlignedBB bb = INFINITE_EXTENT_AABB;
		Block type = getBlockType();
		if (type == WizardryBlocks.arcane_workbench)
		{
			bb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
		}
		else if (type != null)
		{
			AxisAlignedBB cbb = this.getWorld().getBlockState(pos).getBoundingBox(worldObj, pos);
			if (cbb != null)
			{
				bb = cbb;
			}
		}
		return bb;
	}

	// What are all these for?

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for(int i=0; i<this.inv.length; i++){
			this.inv[i] = null;
		}
	}

	/** Called (via {@link electroblob.wizardry.packet.PacketControlInput PacketControlInput}) when the apply button
	 * in the arcane workbench GUI is pressed. */
	// IDEA: Perhaps this should be in the container class?
	public void onApplyButtonPressed(EntityPlayer player){

		ItemStack wand = this.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);
		ItemStack[] spellBooks = new ItemStack[ContainerArcaneWorkbench.CRYSTAL_SLOT];
		for(int i=0; i<spellBooks.length; i++){
			spellBooks[i] = this.getStackInSlot(i);
		}
		ItemStack crystals = this.getStackInSlot(ContainerArcaneWorkbench.CRYSTAL_SLOT);
		ItemStack upgrade = this.getStackInSlot(ContainerArcaneWorkbench.UPGRADE_SLOT);
		
		// TODO: Implement this
		//if(MinecraftForge.EVENT_BUS.post(new SpellBindEvent(player))) return;

		// Since the workbench now accepts armour as well as wands, this check is needed.
		if(wand != null && wand.getItem() instanceof ItemWand){

			// Upgrades wand if necessary. Damage is copied, preserving remaining durability,
			// and also the entire NBT tag compound.
			if(upgrade != null){

				if(upgrade.getItem() == WizardryItems.arcane_tome){

					ItemStack newWand;

					switch(Tier.values()[upgrade.getItemDamage()]){

					case APPRENTICE: 
						if(((ItemWand)wand.getItem()).tier == Tier.BASIC){
							newWand = new ItemStack(WizardryUtilities.getWand(Tier.values()[upgrade.getItemDamage()], ((ItemWand)wand.getItem()).element));
							newWand.setTagCompound(wand.getTagCompound());
							// This needs to be done after copying the tag compound so the max damage for the new wand takes storage
							// upgrades into account.
							newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
							this.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, newWand);
							this.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
							player.addStat(WizardryAchievements.apprentice, 1);
						}
						break;

					case ADVANCED: 
						if(((ItemWand)wand.getItem()).tier == Tier.APPRENTICE){
							newWand = new ItemStack(WizardryUtilities.getWand(Tier.values()[upgrade.getItemDamage()], ((ItemWand)wand.getItem()).element));
							newWand.setTagCompound(wand.getTagCompound());
							newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
							this.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, newWand);
							this.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
						}
						break;

					case MASTER:
						if(((ItemWand)wand.getItem()).tier == Tier.ADVANCED){
							newWand = new ItemStack(WizardryUtilities.getWand(Tier.values()[upgrade.getItemDamage()], ((ItemWand)wand.getItem()).element));
							newWand.setTagCompound(wand.getTagCompound());
							newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
							this.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, newWand);
							this.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
							player.addStat(WizardryAchievements.master, 1);
						}
						break;

					default:
						break;
					}

					// This needs to happen so the charging works on the new wand, not the old one.
					wand = this.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);

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
							Spell[] newSpells = new Spell[5 + WandHelper.getUpgradeLevel(wand, WizardryItems.attunement_upgrade)];

							for(int i=0; i<newSpells.length; i++){
								// Prevents both NPEs and AIOOBEs
								newSpells[i] = i < spells.length && spells[i] != null ? spells[i] : Spells.none;
							}

							WandHelper.setSpells(wand, newSpells);

							int[] cooldown = WandHelper.getCooldowns(wand);
							int[] newCooldown = new int[5 + WandHelper.getUpgradeLevel(wand, WizardryItems.attunement_upgrade)];

							if(cooldown.length > 0){
								for(int i=0; i<cooldown.length; i++){
									newCooldown[i] = cooldown[i];
								}
							}

							WandHelper.setCooldowns(wand, newCooldown);
						}

						this.decrStackSize(ContainerArcaneWorkbench.UPGRADE_SLOT, 1);
						player.addStat(WizardryAchievements.special_upgrade, 1);

						if(WandHelper.getTotalUpgrades(wand) == Tier.MASTER.upgradeLimit){
							player.addStat(WizardryAchievements.max_out_wand, 1);
						}
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
			for(int i=0; i<spells.length; i++){
				if(spellBooks[i] != null && !(Spell.get(spellBooks[i].getItemDamage()).tier.level > ((ItemWand)wand.getItem()).tier.level)){
					spells[i] = Spell.get(spellBooks[i].getItemDamage());
				}
			}
			WandHelper.setSpells(wand, spells);

			// Charges wand by appropriate amount
			if(crystals != null){
				int chargeDepleted = wand.getItemDamage();
				//System.out.println("Charge depleted: " + chargeDepleted);
				//System.out.println("Crystals found: " + crystals.stackSize);
				if(crystals.stackSize * Constants.MANA_PER_CRYSTAL < chargeDepleted){
					//System.out.println("charging");
					wand.setItemDamage(chargeDepleted - crystals.stackSize * Constants.MANA_PER_CRYSTAL);
					this.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, crystals.stackSize);
				}else if(chargeDepleted != 0){
					//System.out.println((int)Math.ceil(((double)chargeDepleted)/50));
					this.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, (int)Math.ceil(((double)chargeDepleted)/Constants.MANA_PER_CRYSTAL));
					wand.setItemDamage(0);
				}
			}

			// Armour
		}else if(wand != null && wand.getItem() instanceof ItemWizardArmour){
			// Applies legendary upgrade
			if(upgrade != null && upgrade.getItem() == WizardryItems.armour_upgrade){
				if(!wand.hasTagCompound()){
					wand.setTagCompound(new NBTTagCompound());
				}
				if(!wand.getTagCompound().hasKey("legendary")){
					wand.getTagCompound().setBoolean("legendary", true);
					this.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
					player.addStat(WizardryAchievements.legendary);
				}
			}
			// Charges armour by appropriate amount
			if(crystals != null){
				int chargeDepleted = wand.getItemDamage();
				if(crystals.stackSize * Constants.MANA_PER_CRYSTAL < chargeDepleted){
					wand.setItemDamage(chargeDepleted - crystals.stackSize * Constants.MANA_PER_CRYSTAL);
					this.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, crystals.stackSize);
				}else if(chargeDepleted != 0){
					this.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, (int)Math.ceil(((double)chargeDepleted)/Constants.MANA_PER_CRYSTAL));
					wand.setItemDamage(0);
				}
			}

			// Scrolls
		}else if(wand != null && wand.getItem() == WizardryItems.blank_scroll){
			// Spells can only be bound to scrolls if the player has already cast them (prevents casting of master spells without getting a master wand)
			// This restriction does not apply in creative mode
			if(spellBooks[0] != null && (player.capabilities.isCreativeMode || (WizardData.get(player) != null
					&& WizardData.get(player).hasSpellBeenDiscovered(Spell.get(spellBooks[0].getItemDamage()))))
					&& crystals != null && crystals.stackSize * Constants.MANA_PER_CRYSTAL > Spell.get(spellBooks[0].getItemDamage()).cost){

				this.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, (int)Math.ceil(((double)Spell.get(spellBooks[0].getItemDamage()).cost)/Constants.MANA_PER_CRYSTAL));
				this.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, new ItemStack(WizardryItems.scroll, 1, spellBooks[0].getItemDamage()));
			}
		}
	}

}
