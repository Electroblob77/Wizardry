package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.util.NBTExtras;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

public class TileEntityBookshelf extends TileEntityLockableLoot implements ITickable {

	/** The inventory of the bookshelf. */
	private NonNullList<ItemStack> inventory;

	private boolean doNotSync;

	public TileEntityBookshelf(){
		inventory = NonNullList.withSize(BlockBookshelf.SLOT_COUNT, ItemStack.EMPTY);
		// Prevent sync() happening when loading from NBT or weirdness ensues when loading a world
		// Normally I'd pass this as a flag to setInventorySlotContents but we can't change the method signature
		this.doNotSync = true;
	}

	/** Called to manually sync the tile entity with clients. */
	public void sync(){
		if(!this.doNotSync)
			this.world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
	}

	@Override
	public void update(){
		this.doNotSync = false;
		// Nothing here for now
	}

	@Override
	public int getSizeInventory(){
		return inventory.size();
	}

	// Still better to override these three because then we can sync only when necessary

	@Override
	public ItemStack decrStackSize(int slot, int amount){

		this.fillWithLoot(null);

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

		this.fillWithLoot(null);

		ItemStack stack = getStackInSlot(slot);

		if(!stack.isEmpty()){
			setInventorySlotContents(slot, ItemStack.EMPTY);
		}

		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack){
		boolean wasEmpty = inventory.get(slot).isEmpty();
		super.setInventorySlotContents(slot, stack);
		// This must be done in the tile entity because containers only exist for player interaction, not hoppers etc.
		if(wasEmpty != stack.isEmpty()) this.sync();

	}

	@Override
	public void fillWithLoot(@Nullable EntityPlayer player){
		if(world.getLootTableManager() != null) super.fillWithLoot(player); // IntelliJ is wrong, it can be null
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
	public boolean isItemValidForSlot(int slotNumber, ItemStack stack){
		return stack.isEmpty() || ContainerBookshelf.isBook(stack);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound){

		super.readFromNBT(tagCompound);

		if(!this.checkLootAndRead(tagCompound)){
			// TODO: Replace with ItemStackHelper#loadAllItems
			NBTTagList tagList = tagCompound.getTagList("Inventory", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++){
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				byte slot = tag.getByte("Slot");
				if(slot >= 0 && slot < getSizeInventory()){
					setInventorySlotContents(slot, new ItemStack(tag));
				}
			}
		}

		if(tagCompound.hasKey("CustomName", NBT.TAG_STRING)) this.customName = tagCompound.getString("CustomName");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound){

		super.writeToNBT(tagCompound);

		if(!this.checkLootAndWrite(tagCompound)){

			// TODO: Replace with ItemStackHelper#saveAllItems

			NBTTagList itemList = new NBTTagList();

			for(int i = 0; i < getSizeInventory(); i++){
				ItemStack stack = getStackInSlot(i);
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}

			NBTExtras.storeTagSafely(tagCompound, "Inventory", itemList);
		}

		if(this.hasCustomName()) tagCompound.setString("CustomName", this.customName);

		return tagCompound;
	}

	@Override
	public final NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
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

	@Override
	protected NonNullList<ItemStack> getItems(){
		return inventory;
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player){
		this.fillWithLoot(player);
		return new ContainerBookshelf(playerInventory, this);
	}

	@Override
	public String getGuiID(){
		return Wizardry.MODID + ":bookshelf";
	}

}
