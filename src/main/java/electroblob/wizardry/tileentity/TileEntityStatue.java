package electroblob.wizardry.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityStatue extends TileEntity {

	public EntityLiving creature;
	private NBTTagCompound entityCompound;
	private String entityName;
	private float entityYawHead;
	private float entityYawOffset;
	public boolean isIce;
	private int timer;
	private int lifetime = 600;
	
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
	}
	
	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}
	
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox(){
		
        AxisAlignedBB bb = INFINITE_EXTENT_AABB;
        Block type = getBlockType();
        // Allows the renderer to render the entity even when the bottom block is not visible.
        // Only needed when position = 1 because that is the only time it renders the entity.
        if (this.position == 1 && this.creature != null){
        	// Now uses the entity's bounding box
        	bb = this.creature.boundingBox;
        	//bb = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + this.parts, zCoord + 1);
        
        }else if(type != null){
        	
            AxisAlignedBB cbb = getBlockType().getCollisionBoundingBoxFromPool(worldObj, xCoord, yCoord, zCoord);
            if (cbb != null)
            {
                bb = cbb;
            }
        }
        return bb;
    }
	
	@Override
	public void updateEntity(){
		
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
			if(this.worldObj.getBlockLightValue(this.xCoord, this.yCoord, this.zCoord) < this.worldObj.rand.nextInt(12) - 3){
				// This is all that is needed because destroyBlock invokes the breakBlock function in BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				this.worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
			}
		}
		
		// Breaks the block after 30 secs
		if(!this.worldObj.isRemote && this.timer > lifetime && this.isIce){
			if(this.position == 1){
				// This is all that is needed because destroyBlock invokes the breakBlock function in BlockPetrifiedStone
				// and that function handles all the spawning and stuff.
				this.worldObj.func_147480_a(xCoord, yCoord, zCoord, false);
			}
		}
		
		// Sends information about the petrified creature to the client side so it can be rendered (Stuck in the ice! :D)
		/*
		if(!this.worldObj.isRemote && this.isIce && this.position == 1){
			if(this.petrifiedCreature != null){
				
				// Packet building
            	ByteArrayOutputStream bos = new ByteArrayOutputStream(20);
            	DataOutputStream outputStream = new DataOutputStream(bos);
            	try {
        	        	outputStream.writeInt(4);
        	        	outputStream.writeInt(xCoord);
        	        	outputStream.writeInt(yCoord);
        	        	outputStream.writeInt(zCoord);
            	        outputStream.writeInt(EntityList.getEntityID(petrifiedCreature));
            	} catch (Exception ex) {
            	        ex.printStackTrace();
            	}

            	Packet250CustomPayload packet = new Packet250CustomPayload();
            	packet.channel = "WizardryMod";
            	packet.data = bos.toByteArray();
            	packet.length = bos.size();
            	
            	PacketDispatcher.sendPacketToAllPlayers(packet);
			}
		}
		*/
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
    public void writeToNBT(NBTTagCompound tagCompound) {
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
    }
    
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
}
