package electroblob.wizardry.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityMeteor extends EntityFallingBlock
{
    /** How long the block has been falling for. */
    public int fallTime;
    
    /** The entity blast multiplier. Only some projectiles cause a blast, which is why this isn't in EntityMagicProjectile. */
	public float blastMultiplier;

    public EntityMeteor(World par1World)
    {
        super(par1World);
    }

    public EntityMeteor(World par1World, double par2, double par4, double par6, Block block, float blastMultiplier)
    {
        super(par1World, par2, par4, par6, block);
        this.setSize(0.98F, 0.98F);
        this.yOffset = this.height / 2.0F;
        this.setPosition(par2, par4, par6);
        this.motionX = 0.0D;
        this.motionY = -1.0D;
        this.motionZ = 0.0D;
        this.prevPosX = par2;
        this.prevPosY = par4;
        this.prevPosZ = par6;
        this.setFire(200);
        this.blastMultiplier = blastMultiplier;
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    protected void entityInit() {}

    /**
     * Returns true if other Entities should be prevented from moving through this Entity.
     */
    public boolean canBeCollidedWith()
    {
        return !this.isDead;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate(){
    	
    	if(this.ticksExisted % 16 == 1 && worldObj.isRemote){
    		Wizardry.proxy.playMovingSound(this, "wizardry:flameray", 3.0f, 1.0f, false);
		}
    	/*
    	if(worldObj.isRemote){
    		System.out.println("Client: " + this.posX + ", " + this.posY + ", " + this.posZ);
    	}else{
    		System.out.println("Server: " + this.posX + ", " + this.posY + ", " + this.posZ);
    	}
    	*/
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        ++this.fallTime;
        this.motionY -= 0.1d; //0.03999999910593033D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (!this.worldObj.isRemote)
        {
            int i = MathHelper.floor_double(this.posX);
            int j = MathHelper.floor_double(this.posY);
            int k = MathHelper.floor_double(this.posZ);
            
            if (this.onGround)
            {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
                this.motionY *= -0.5D;
                this.worldObj.createExplosion(this, this.posX, this.posY, this.posZ, 2.0f*blastMultiplier, true);
            	for(int i1=-3; i1<4; i1++){
					for(int j1=-3; j1<4; j1++){
						int y = WizardryUtilities.getNearestFloorLevelB(this.worldObj, (int)this.posX + i1, (int)this.posY, (int)this.posZ + j1, 7);
						//System.out.println(y);
						double dist = this.getDistance((int)this.posX + i1, y, (int)this.posZ + j1);
						// Randomised with weighting so that the nearer the block the more likely it is to be set on fire.
						if(y != -1 && rand.nextInt((int)dist*2 + 1) < 3 && dist < 4){
								this.worldObj.setBlock((int)this.posX + i1, y, (int)this.posZ + j1, Blocks.fire);
						}
					}
				}
                this.setDead();
            }
        }
    }

    /**
     * Called when the mob is falling. Calculates and applies fall damage.
     */
    protected void fall(float par1)
    {
        
    }
    
    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    /*
    protected void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setByte("Tile", (byte)this.blockID);
        par1NBTTagCompound.setInteger("TileID", this.blockID);
        par1NBTTagCompound.setByte("Time", (byte)this.fallTime);
    }
	*/
    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    /*
    protected void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        if (par1NBTTagCompound.hasKey("TileID"))
        {
            this.blockID = par1NBTTagCompound.getInteger("TileID");
        }
        else
        {
            this.blockID = par1NBTTagCompound.getByte("Tile") & 255;
        }

        this.fallTime = par1NBTTagCompound.getByte("Time") & 255;

        if (this.blockID == 0)
        {
            this.blockID = Block.stone.blockID;
        }
    }
*/
    @SideOnly(Side.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    @SideOnly(Side.CLIENT)
    public World getWorld()
    {
        return this.worldObj;
    }

    @SideOnly(Side.CLIENT)

    /**
     * Return whether this entity should be rendered as on fire.
     */
    public boolean canRenderOnFire()
    {
        return true;
    }

	/**
     * Checks using a Vec3d to determine if this entity is within range of that vector to be rendered. Args: vec3D
     */
    public boolean isInRangeToRenderVec3D(Vec3 par1Vec3)
    {
        return true;
    }

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
    	super.readEntityFromNBT(nbttagcompound);
        blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("blastMultiplier", blastMultiplier);
	}
	
}
