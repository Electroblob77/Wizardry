package electroblob.wizardry.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;

public class TileEntityArcaneWorkbench extends TileEntity implements IInventory {

	private ItemStack[] inv;
	public float timer;
	public int yTimer;
	public int yOffset;

	public TileEntityArcaneWorkbench(){
		inv = new ItemStack[ContainerArcaneWorkbench.UPGRADE_SLOT + 1];
		timer = 0;
		yTimer = 0;
		yOffset = 300;
	}

	@Override
	public void updateEntity(){
		
		ItemStack itemstack = this.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);
		
		// Decrements wand damage (increases mana) every 1.5 seconds if it has a condenser upgrade
		if(itemstack != null && itemstack.getItem() instanceof ItemWand && !this.worldObj.isRemote && itemstack.isItemDamaged()
				&& this.worldObj.getWorldTime() % Wizardry.CONDENSER_TICK_INTERVAL == 0){
			// If the upgrade level is 0, this does nothing anyway.
			itemstack.setItemDamage(itemstack.getItemDamage() - WandHelper.getUpgradeLevel(itemstack, Wizardry.condenserUpgrade));
		}
		
		if(yOffset > 0){
			yTimer--;
		}else{
			yTimer++;
		}
		if(timer < 359){
			timer++;
		}else{
			timer = 0;
		}
		yOffset += yTimer;
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
	public ItemStack getStackInSlotOnClosing(int slot) {
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
	public String getInventoryName() {
		return "container.arcaneWorkbench";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this &&
				player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openInventory() {

	}

	@Override
	public void closeInventory() {

	}

	@Override
	public boolean isItemValidForSlot(int slotNumber, ItemStack itemstack) {
		
		if(itemstack == null) return true;
		
		if(slotNumber >= 0 && slotNumber < ContainerArcaneWorkbench.CRYSTAL_SLOT){
			return itemstack.getItem() == Wizardry.spellBook;
			
		}else if(slotNumber == ContainerArcaneWorkbench.CRYSTAL_SLOT){
			return itemstack.getItem() == Wizardry.magicCrystal;
			
		}else if(slotNumber == ContainerArcaneWorkbench.WAND_SLOT){
			return itemstack != null && (itemstack.getItem() instanceof ItemWand
					|| itemstack.getItem() instanceof ItemWizardArmour
					|| itemstack.getItem() == Wizardry.blankScroll);
			
		}else if(slotNumber == ContainerArcaneWorkbench.UPGRADE_SLOT){
			return itemstack.getItem() == Wizardry.arcaneTome
					|| itemstack.getItem() == Wizardry.condenserUpgrade
					|| itemstack.getItem() == Wizardry.siphonUpgrade
					|| itemstack.getItem() == Wizardry.storageUpgrade
					|| itemstack.getItem() == Wizardry.rangeUpgrade
					|| itemstack.getItem() == Wizardry.durationUpgrade
					|| itemstack.getItem() == Wizardry.cooldownUpgrade
					|| itemstack.getItem() == Wizardry.blastUpgrade
					|| itemstack.getItem() == Wizardry.attunementUpgrade
					|| itemstack.getItem() == Wizardry.armourUpgrade;
		}
		
		return true;
		
	}
	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inv.length) {
				inv[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}

		timer = tagCompound.getFloat("timer");
		yTimer = tagCompound.getInteger("yTimer");
		yOffset = tagCompound.getInteger("yOffset");
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
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
		tagCompound.setFloat("timer", timer);
		tagCompound.setInteger("yTimer", yTimer);
		tagCompound.setInteger("yOffset", yOffset);
	}

	// Not sure why there were super calls here in the first place.
	@Override
	public Packet getDescriptionPacket() {
		//S35PacketUpdateTileEntity packet = (S35PacketUpdateTileEntity) super.getDescriptionPacket();
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		//super.onDataPacket(net, pkt);
		NBTTagCompound tag = pkt.func_148857_g();
		readFromNBT(tag);
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox()
	{
		AxisAlignedBB bb = INFINITE_EXTENT_AABB;
		Block type = getBlockType();
		if (type == Wizardry.arcaneWorkbench)
		{
			bb = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
		}
		else if (type != null)
		{
			AxisAlignedBB cbb = getBlockType().getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord);
			if (cbb != null)
			{
				bb = cbb;
			}
		}
		return bb;
	}

}
