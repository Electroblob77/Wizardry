package electroblob.wizardry.client.particle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import net.minecraft.block.Block;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntityTornadoFX extends EntityDiggingFX
{
	/** The name used to identify this particle. Uses the mod id to avoid any possible conflicts (Not that there would
	 * be any, but I may as well.) */
	public static final String NAME = Wizardry.MODID + "tornado";
	
    private boolean gravity = false;
    private double angle;
    private double radius;
    private double speed;
    private double velX, velZ;
    private boolean fullBrightness = false;

    public EntityTornadoFX(World world, int maxAge, double originX, double originZ, double radius, double yPos, double velX, double velZ, Block block, int metadata, int side)
    {
        super(world, 0, 0, 0, 0, 0, 0, block, metadata, side);
        this.angle = this.rand.nextDouble() * Math.PI * 2;
    	double x = originX - Math.cos(angle)*radius;
    	double z = originZ + radius*Math.sin(angle);
    	this.radius = radius;
    	this.setPosition(x, yPos, z);
        //this.particleScale *= 0.75F;
        this.particleMaxAge = maxAge;
        this.noClip = true;
        // Grass has special treatment, since it has a colourised top but the rest is normal.
        if(block != Blocks.grass || side == 1) this.setColour(block.getRenderColor(side));
        
	    if(block.getLightValue() == 0){
	        this.particleRed *= 0.75;
	        this.particleGreen *= 0.75;
	        this.particleBlue *= 0.75;
        }else{
        	this.fullBrightness = true;
        }
	    
        speed = rand.nextDouble()*2 + 1;
        this.velX = velX;
        this.velZ = velZ;
    }
    
    public EntityTornadoFX(World world, double x, double y, double z, double vx, double vy, double vz, float red, float green, float blue)
    {
        super(world, x, y, z, vx, vy, vz, Blocks.dirt, 0);
        //this.particleScale *= 0.75F;
        this.particleMaxAge = 48 + this.rand.nextInt(12);
        this.noClip = true;
        particleRed = red;
        particleGreen = green;
        particleBlue = blue;
    }
    
    public void setColour(int par1)
    {
        float f = (float)((par1 & 16711680) >> 16) / 255.0F;
        float f1 = (float)((par1 & 65280) >> 8) / 255.0F;
        float f2 = (float)((par1 & 255) >> 0) / 255.0F;
        float f3 = 1.0F;
        this.setRBGColorF(f * f3, f1 * f3, f2 * f3);
    }

    /**
     * returns the bounding box for this entity
     */
    public AxisAlignedBB getBoundingBox()
    {
        return null;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }
        
        // This is in radians per tick...
        double omega = Math.signum(speed) * ((Math.PI*2)/20 - speed/(20*radius));

        // v = r times omega; therefore the normalised velocity vector needs to be r times the angle increment / 2 pi.
        this.angle += omega;
        
    	this.motionZ = radius * omega * Math.cos(angle);
    	this.motionX = radius * omega * Math.sin(angle);
        this.moveEntity(motionX + velX, 0, motionZ + velZ);
        
        if(this.gravity){
            this.motionY -= 0.05d;
        }

        if (this.particleAge > this.particleMaxAge / 2)
        {
            this.setAlphaF(1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
        }
        
    }
    
    @Override
    public int getBrightnessForRender(float par1)
    {
        return fullBrightness ? 15728880 : super.getBrightnessForRender(par1);
    }

    @Override
    public float getBrightness(float par1)
    {
        return fullBrightness ? 1.0F : super.getBrightness(par1);
    }
    
}
