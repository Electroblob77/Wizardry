package electroblob.wizardry.tileentity;

import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;
import java.util.Arrays;

public class TileEntityImbuementAltar extends TileEntity implements ITickable {

	private static final int IMBUEMENT_DURATION = 140;

	private ItemStack stack;
	private int imbuementTimer;
	private Element displayElement;

	public TileEntityImbuementAltar(){
		stack = ItemStack.EMPTY;
	}

	public void setStack(ItemStack stack){
		this.stack = stack;
		checkRecipe();
	}

	public void checkRecipe(){

		if(getResult().isEmpty()){
			imbuementTimer = 0;
		}else if(imbuementTimer == 0){
			imbuementTimer = 1;
		}else{
			return; // Don't sync if nothing changed
		}

		world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3); // Sync
	}

	public ItemStack getStack(){
		return stack;
	}

	@Override
	public void update(){

		if(imbuementTimer > 0){

			if(imbuementTimer == 1){ // Has to be done here because of syncing
				world.playSound(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
						WizardrySounds.BLOCK_IMBUEMENT_ALTAR_IMBUE, SoundCategory.BLOCKS, 1, 1, false);
			}

			ItemStack result = getResult();

			if(result.isEmpty()){
				imbuementTimer = 0;

			}else{

				if(imbuementTimer++ >= IMBUEMENT_DURATION){
					this.stack = result;
					consumeReceptacleContents();
					imbuementTimer = 0;
					displayElement = null;
				}

				if(world.isRemote && world.rand.nextInt(2) == 0){

					Element[] elements = getReceptacleElements();

					Vec3d centre = GeometryUtils.getCentre(this.pos.up());

					for(int i = 0; i < elements.length; i++){

						if(elements[i] == null) continue;

						Vec3d offset = new Vec3d(EnumFacing.byHorizontalIndex(i).getDirectionVec());
						Vec3d vec = GeometryUtils.getCentre(this.pos).add(0, 0.3, 0).add(offset.scale(0.7));

						int[] colours = BlockReceptacle.PARTICLE_COLOURS.get(elements[i]);

						ParticleBuilder.create(Type.DUST, world.rand, vec.x, vec.y, vec.z, 0.1, false)
								.vel(centre.subtract(vec).scale(0.02)).clr(colours[1]).fade(colours[2]).time(50).spawn(world);
					}
				}
			}
		}
	}

	/** Returns the element to use for the visual ray effect colours, or null if they should not be displayed. */
	public Element getDisplayElement(){
		return displayElement;
	}

	/** Returns how complete the current action is (from 0 to 1), or 0 if no action is being performed. */
	public float getImbuementProgress(){
		return (float)imbuementTimer / IMBUEMENT_DURATION;
	}

	private ItemStack getResult(){

		if(stack.getItem() instanceof ItemWizardArmour && ((ItemWizardArmour)stack.getItem()).element == null){

			Element[] elements = getReceptacleElements();

			if(Arrays.stream(elements).distinct().count() == 1 && elements[0] != null){ // All the same element

				ItemStack result = new ItemStack(WizardryItems.getArmour(elements[0], ((ItemWizardArmour)stack.getItem()).armorType));
				displayElement = elements[0];

				result.setTagCompound(stack.getTagCompound());
				((IManaStoringItem)result.getItem()).setMana(result, ((ItemWizardArmour)stack.getItem()).getMana(stack));

				return result;
			}
		}

		if((stack.getItem() == WizardryItems.magic_crystal || stack.getItem() == Item.getItemFromBlock(WizardryBlocks.crystal_block))
				&& stack.getMetadata() == 0){

			Element[] elements = getReceptacleElements();

			if(Arrays.stream(elements).distinct().count() == 1 && elements[0] != null){ // All the same element
				displayElement = elements[0];
				return new ItemStack(stack.getItem(), stack.getCount(), elements[0].ordinal());
			}
		}

		displayElement = null;
		return ItemStack.EMPTY;
	}

	/** Returns the elements of the 4 adjacent receptacles, in SWNE order. Null means an empty or missing receptacle. */
	private Element[] getReceptacleElements(){

		Element[] elements = new Element[4];

		for(EnumFacing side : EnumFacing.HORIZONTALS){

			TileEntity tileEntity = world.getTileEntity(pos.offset(side));

			if(tileEntity instanceof TileEntityReceptacle){
				elements[side.getHorizontalIndex()] = ((TileEntityReceptacle)tileEntity).getElement();
			}else{
				elements[side.getHorizontalIndex()] = null;
			}
		}

		return elements;
	}

	/** Empties the 4 adjacent receptacles. */
	private void consumeReceptacleContents(){

		for(EnumFacing side : EnumFacing.HORIZONTALS){

			TileEntity tileEntity = world.getTileEntity(pos.offset(side));

			if(tileEntity instanceof TileEntityReceptacle){
				((TileEntityReceptacle)tileEntity).setElement(null);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);
		NBTTagCompound itemTag = new NBTTagCompound();
		stack.writeToNBT(itemTag);
		nbt.setTag("item", itemTag);
		nbt.setInteger("imbuementTimer", imbuementTimer);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);
		NBTTagCompound itemTag = nbt.getCompoundTag("item");
		this.stack = new ItemStack(itemTag);
		this.imbuementTimer = nbt.getInteger("imbuementTimer");
	}

	@Override
	public NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		return new SPacketUpdateTileEntity(pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt){
		readFromNBT(pkt.getNbtCompound());
	}

}
