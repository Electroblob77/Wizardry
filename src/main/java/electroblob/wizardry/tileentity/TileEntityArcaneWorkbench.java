package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.item.ItemCrystal;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.WandHelper;
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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Set;

public class TileEntityArcaneWorkbench extends TileEntity implements IInventory, ITickable {

	/** The inventory of the arcane workbench. */
	private NonNullList<ItemStack> inventory;
	/** Controls the rotating rune and floating wand animations. */
	public float timer = 0;

	private boolean doNotSync;

	public TileEntityArcaneWorkbench(){
		inventory = NonNullList.withSize(ContainerArcaneWorkbench.UPGRADE_SLOT + 1, ItemStack.EMPTY);
		// Prevent sync() happening when loading from NBT the first time or weirdness ensues when loading a world
		// Normally I'd pass this as a flag to setInventorySlotContents but we can't change the method signature
		this.doNotSync = true;
	}

	@Override
	public void onLoad(){
		timer = 0;
	}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		if(!doNotSync) this.world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public void update(){

		this.doNotSync = false;

		ItemStack stack = this.getStackInSlot(ContainerArcaneWorkbench.CENTRE_SLOT);

		// Decrements wand damage (increases mana) every 1.5 seconds if it has a condenser upgrade
		if(stack.getItem() instanceof IManaStoringItem && !this.world.isRemote && !((IManaStoringItem)stack.getItem()).isManaFull(stack)
				&& this.world.getTotalWorldTime() % electroblob.wizardry.constants.Constants.CONDENSER_TICK_INTERVAL == 0){
			// If the upgrade level is 0, this does nothing anyway.
			((IManaStoringItem)stack.getItem()).rechargeMana(stack, WandHelper.getUpgradeLevel(stack, WizardryItems.condenser_upgrade));
		}

		// The server doesn't care what these are, and there's no need for them to be synced or saved.
		if(this.world.isRemote){
			timer++;
		}
	}

	@Override
	public int getSizeInventory(){
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot){
		return inventory.get(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount){
		
		ItemStack stack = getStackInSlot(slot);
		
		if(!stack.isEmpty()){
			if(stack.getCount() <= amount){
				setInventorySlotContents(slot, ItemStack.EMPTY);
			}else{
				stack = stack.splitStack(amount);
				if(stack.getCount() == 0){
					setInventorySlotContents(slot, ItemStack.EMPTY);
				}
			}
			this.markDirty();
		}
		
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot){
		
		ItemStack stack = getStackInSlot(slot);
		
		if(!stack.isEmpty()){
			setInventorySlotContents(slot, ItemStack.EMPTY);
		}
		
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack){

		ItemStack previous = inventory.set(slot, stack);

		// Only the central slot affects the in-world rendering, so only sync if that changes
		// This must be done in the tile entity because containers only exist for player interaction, not hoppers etc.
		if(slot == ContainerArcaneWorkbench.CENTRE_SLOT && previous.isEmpty() != stack.isEmpty()) this.sync();
		
		if(!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()){
			stack.setCount(getInventoryStackLimit());
		}
	}

	@Override
	public String getName(){
		return "container." + Wizardry.MODID + ":arcane_workbench";
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public int getInventoryStackLimit(){
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player){
		return world.getTileEntity(pos) == this && player.getDistanceSqToCenter(pos) < 64;
	}

	@Override
	public void openInventory(EntityPlayer player){

	}

	@Override
	public void closeInventory(EntityPlayer player){

	}

	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemstack){

		if(itemstack == ItemStack.EMPTY) return true;

		if(slotNumber >= 0 && slotNumber < ContainerArcaneWorkbench.CRYSTAL_SLOT){

			if(!(itemstack.getItem() instanceof ItemSpellBook)) return false;

			ItemStack centreStack = getStackInSlot(ContainerArcaneWorkbench.CENTRE_SLOT);

			if(centreStack.getItem() instanceof IWorkbenchItem){
				int spellSlots = ((IWorkbenchItem)centreStack.getItem()).getSpellSlotCount(centreStack);
				return slotNumber < spellSlots;
			}

			return false;

		}else if(slotNumber == ContainerArcaneWorkbench.CRYSTAL_SLOT){
			return itemstack.getItem() instanceof ItemCrystal;

		}else if(slotNumber == ContainerArcaneWorkbench.CENTRE_SLOT){
			return itemstack.getItem() instanceof IWorkbenchItem;

		}else if(slotNumber == ContainerArcaneWorkbench.UPGRADE_SLOT){
			Set<Item> upgrades = new HashSet<>(WandHelper.getSpecialUpgrades());
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
		for(int i = 0; i < tagList.tagCount(); i++){
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if(slot >= 0 && slot < getSizeInventory()){
				setInventorySlotContents(slot, new ItemStack(tag));
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){

		super.writeToNBT(tagCompound);

		NBTTagList itemList = new NBTTagList();
		for(int i = 0; i < getSizeInventory(); i++){
			ItemStack stack = getStackInSlot(i);
			NBTTagCompound tag = new NBTTagCompound();
			tag.setByte("Slot", (byte)i);
			stack.writeToNBT(tag);
			itemList.appendTag(tag);
		}

		NBTExtras.storeTagSafely(tagCompound, "Inventory", itemList);
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
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		AxisAlignedBB bb = INFINITE_EXTENT_AABB;
		Block type = getBlockType();
		if(type == WizardryBlocks.arcane_workbench){
			bb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
		}else if(type != null){
			AxisAlignedBB cbb = this.getWorld().getBlockState(pos).getBoundingBox(world, pos);
			if(cbb != null){
				bb = cbb;
			}
		}
		return bb;
	}

	// What are all these for?

	@Override
	public int getField(int id){
		return 0;
	}

	@Override
	public void setField(int id, int value){

	}

	@Override
	public int getFieldCount(){
		return 0;
	}

	@Override
	public void clear(){
		for(int i = 0; i < getSizeInventory(); i++){
			setInventorySlotContents(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean isEmpty(){
		for(int i = 0; i < getSizeInventory(); i++){
			if(!getStackInSlot(i).isEmpty()){
				return false;
			}
		}
		return true;
	}

}
