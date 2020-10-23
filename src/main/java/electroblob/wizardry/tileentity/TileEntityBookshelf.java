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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

public class TileEntityBookshelf extends TileEntityLockableLoot implements ITickable {

	/** NBT key for the boolean flag specifying if this bookshelf was generated naturally as part of a structure or not.
	 * This flag is set via {@link TileEntityBookshelf#markAsNatural(NBTTagCompound)}. */
	private static final String NATURAL_NBT_KEY = "NaturallyGenerated";
	/** When a non-spectating player comes within this distance of a naturally-generated bookshelf, it will automatically
	 * generate its loot if a loot table was set. This means the bookshelves do not incorrectly appear empty before a
	 * player looks inside. */ // Kind of a trade-off between not seeing them appear and not triggering from miles away
	private static final int LOOT_GEN_DISTANCE = 32; // Nobody is likely to be looking from more than 32 blocks away

	/** The inventory of the bookshelf. */
	private NonNullList<ItemStack> inventory;

	/** Whether this bookshelf was generated naturally as part of a structure. This determines whether the loot is
	 * automatically generated (and synced) when a non-spectating player comes within {@value LOOT_GEN_DISTANCE} blocks,
	 * so the bookshelf doesn't look empty until opened (loot is generated with that player in the context). */
	private boolean natural;
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

		// When a player gets near, generate the books so they can actually see them (if it was generated naturally)
		if(lootTable != null && natural){
			EntityPlayer player = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					LOOT_GEN_DISTANCE, false);
			if(player != null){
				natural = false; // It's a normal bookshelf now (unlikely to matter but you never know)
				fillWithLoot(player);
				sync();
			}
		}
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
		this.fillWithLoot(null);
		boolean wasEmpty = inventory.get(slot).isEmpty();
		super.setInventorySlotContents(slot, stack);
		// This must be done in the tile entity because containers only exist for player interaction, not hoppers etc.
		if(wasEmpty != stack.isEmpty()) this.sync();
		this.markDirty();
	}

	/** Sets the {@value NATURAL_NBT_KEY} flag to true in the given NBT tag compound, <b>if</b> the compound belongs to
	 * a bookshelf tile entity (more specifically, if it has an "id" tag matching the bookshelf TE's registry name). */
	public static void markAsNatural(NBTTagCompound nbt){
		if(nbt != null && nbt.getString("id").equals(TileEntity.getKey(TileEntityBookshelf.class).toString())){
			nbt.setBoolean(NATURAL_NBT_KEY, true);
		}
	}

	@Override
	public void fillWithLoot(@Nullable EntityPlayer player){
		if(world != null && world.getLootTableManager() != null) super.fillWithLoot(player); // IntelliJ is wrong, it can be null
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
	public void readFromNBT(NBTTagCompound nbt){

		super.readFromNBT(nbt);

		natural = nbt.getBoolean(NATURAL_NBT_KEY);

		if(!this.checkLootAndRead(nbt)){

			NBTTagList tagList = nbt.getTagList("Inventory", NBT.TAG_COMPOUND);

			for(int i = 0; i < tagList.tagCount(); i++){
				NBTTagCompound tag = tagList.getCompoundTagAt(i);
				byte slot = tag.getByte("Slot");
				if(slot >= 0 && slot < getSizeInventory()){
					setInventorySlotContents(slot, new ItemStack(tag));
				}
			}
		}

		if(nbt.hasKey("CustomName", NBT.TAG_STRING)) this.customName = nbt.getString("CustomName");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){

		super.writeToNBT(nbt);

		// Need to save this in case the block was generated but no players came near enough to trigger loot gen
		nbt.setBoolean(NATURAL_NBT_KEY, natural);

		if(!this.checkLootAndWrite(nbt)){

			NBTTagList itemList = new NBTTagList();

			for(int i = 0; i < getSizeInventory(); i++){
				ItemStack stack = getStackInSlot(i);
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte)i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}

			NBTExtras.storeTagSafely(nbt, "Inventory", itemList);
		}

		if(this.hasCustomName()) nbt.setString("CustomName", this.customName);

		return nbt;
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
