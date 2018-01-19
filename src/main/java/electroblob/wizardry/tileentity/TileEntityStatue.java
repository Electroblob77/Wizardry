package electroblob.wizardry.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityStatue extends TileEntity implements ITickable {

	public EntityLiving creature;
	private NBTTagCompound entityCompound;
	private String entityName;
	private float entityYawHead;
	private float entityYawOffset;
	public boolean isIce;
	private int timer;
	private int lifetime = 600;
	/** <b>[Client-side]</b> Keeps track of the destroy stage for the block associated with this tile entity
	 * for rendering purposes. Will be 0 if position is not 1. */
	public int destroyStage;
	
	public TileEntityStatue(){

	}
	
	public TileEntityStatue(boolean isIce){
		this.isIce = isIce;
		this.timer = 0;
	}
	
	/** The number of stone blocks that this petrified creature is made of.*/
	public int parts;
	
	/** The position within the petrified creature this particular tileentity holds.
	 * 1 is at the bottom.*/
	public int position = 1;
	
	public void setCreatureAndPart(EntityLiving entity, int position, int parts){
		this.creature = entity;
		this.position = position;
		this.parts = parts;
		// Aligns the entity with the block so the render bounding box works correctly, and also for visual effect when
		// broken out.
		if(position == 1) creature.setPosition(this.getPos().getX() + 0.5, this.getPos().getY(), this.getPos().getZ() + 0.5);
	}
	
	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}
	
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
		
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        Block type = getBlockType();
        // Allows the renderer to render the entity even when the bottom block is not visible.
        // Was only done for position 1, now done for all positions so the breaking animation works properly.
        if(this.creature != null){
        	// Now uses the entity's bounding box
        	bb = this.creature.getRenderBoundingBox();
        	//bb = new AxisAlignedBB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + this.parts, zCoord + 1);
        
        }else if(type != null){
        	
            AxisAlignedBB cbb = this.getWorld().getBlockState(pos).getBoundingBox(worldObj, pos);
            if (cbb != null)
            {
                bb = cbb;
            }
        }
        return bb;
    }
	
	@Override
	public boolean canRenderBreaking(){
		return true;
	}
	
	@Override
	public void update(){
		
		this.timer++;
		
		//System.out.println(entityName);
		if(this.creature == null && entityName != null){
			this.creature = (EntityLiving) EntityList.createEntityByName(this.entityName, this.worldObj);
			if(this.creature != null){
				this.creature.readFromNBT(entityCompound);
				this.creature.rotationYawHead = this.entityYawHead;
				this.creature.renderYawOffset = this.entityYawOffset;
			}
		}
		
		//System.out.println("Coords: " + this.xCoord + ", " + this.yCoord + ", " + this.zCoord);
		// Breaks the block at light levels of 7 or below, with a higher chance the lower the light level.
		// The chance is (8 - light level)/12, so at light 0 the chance is 3/4 and at light 7 the chance is 1/12.
		if(!this.worldObj.isRemote && this.timer % 200 == 0 && this.timer > lifetime && !this.isIce && this.position == 1){
			// TESTME: There are about 10 different light-related methods in world now... is this the right one?
			if(this.worldObj.getLight(pos) < this.worldObj.rand.nextInt(12) - 3){
				// This is all that is needed because destroyBlock invokes the breakBlock function in BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				this.worldObj.destroyBlock(pos, false);
			}
		}
		
		// Breaks the block after 30 secs
		if(!this.worldObj.isRemote && this.timer > lifetime && this.isIce){
			if(this.position == 1){
				// This is all that is needed because destroyBlock invokes the breakBlock function in BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				this.worldObj.destroyBlock(pos, false);
			}
		}
	}
	
	@Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        position = tagCompound.getInteger("position");
        parts = tagCompound.getInteger("parts");
        entityCompound = tagCompound.getCompoundTag("entity");
        entityName = tagCompound.getString("entityName");
        timer = tagCompound.getInteger("timer");
        lifetime = tagCompound.getInteger("lifetime");
        isIce = tagCompound.getBoolean("isIce");
        entityYawHead = tagCompound.getFloat("entityYawHead");
        entityYawOffset = tagCompound.getFloat("entityYawOffset");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("position", position);
        tagCompound.setInteger("parts", parts);
        entityCompound = new NBTTagCompound();
        if(creature != null){
        	creature.writeToNBT(entityCompound);
            tagCompound.setString("entityName", EntityList.getEntityString(creature));
            tagCompound.setFloat("entityYawHead", creature.rotationYawHead);
            tagCompound.setFloat("entityYawOffset", creature.renderYawOffset);
        }
        tagCompound.setTag("entity", entityCompound);
        tagCompound.setInteger("timer", timer);
        tagCompound.setInteger("lifetime", lifetime);
        tagCompound.setBoolean("isIce", isIce);
        
		return tagCompound;
    }
    
    @Override
	public final NBTTagCompound getUpdateTag(){
		return this.writeToNBT(new NBTTagCompound());
	}
    
    @Override
	public SPacketUpdateTileEntity getUpdatePacket(){
		NBTTagCompound tag = new NBTTagCompound();
		writeToNBT(tag);
		return new SPacketUpdateTileEntity(pos, 1, tag);
	}

    @Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound tag = pkt.getNbtCompound();
		readFromNBT(tag);
	}
}
