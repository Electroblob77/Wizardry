package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants.NBT;

public class TileEntityBookshelf extends TileEntity implements IInventory, ITickable {

	/** The inventory of the bookshelf. */
	private NonNullList<ItemStack> inventory;

	public TileEntityBookshelf(){
		inventory = NonNullList.withSize(BlockBookshelf.SLOT_COUNT, ItemStack.EMPTY);
	}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		this.world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public void update(){
		// Nothing here for now
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

		if(previous.isEmpty() != stack.isEmpty()) this.sync();
		
		if(!stack.isEmpty() && stack.getCount() > getInventoryStackLimit()){
			stack.setCount(getInventoryStackLimit());
		}

	}

	@Override
	public String getName(){
		return "container." + Wizardry.MODID + ":bookshelf";
	}

	@Override
	public boolean hasCustomName(){
		return false;
	}

	@Override
	public ITextComponent getDisplayName(){
		return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());
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
		return itemstack.isEmpty() || itemstack.getItem() instanceof ItemSpellBook; // TODO: Add a whitelist
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
		Wizardry.proxy.notifyBookshelfChange(world, pos);
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
