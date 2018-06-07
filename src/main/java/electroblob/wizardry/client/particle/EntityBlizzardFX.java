package electroblob.wizardry.client.particle;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntityBlizzardFX extends EntitySnowFX
{
	/** The name used to identify this particle. Uses the mod id to avoid any possible conflicts (Not that there would
	 * be any, but I may as well.) */
	public static final String NAME = Wizardry.MODID + "blizzard";
	
    private boolean gravity = false;
    private double angle;
    private double radius;
    private double speed;

    public EntityBlizzardFX(World world, float red, float green, float blue, int maxAge, double originX, double originZ, double radius, double yPos)
    {
        super(world, 0, 0, 0, 0, 0, 0, maxAge);
        this.angle = this.rand.nextDouble() * Math.PI * 2;
    	double x = originX - Math.cos(angle)*radius;
    	double z = originZ + radius*Math.sin(angle);
    	this.radius = radius;
    	this.setPosition(x, yPos, z);
        //this.particleScale *= 0.75F;
        this.noClip = true;
        particleRed = red;
        particleGreen = green;
        particleBlue = blue;
        if(rand.nextBoolean()){
        	speed = rand.nextDouble()*2 + 1;
        }else{
        	speed = rand.nextDouble()*-2 - 1;
        }
    }
    
    public EntityBlizzardFX(World world, double x, double y, double z, double vx, double vy, double vz, float red, float green, float blue)
    {
        super(world, x, y, z, vx, vy, vz);
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

    public void renderParticle(Tessellator par1Tessellator, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        if (this.particleAge < this.particleMaxAge / 3 || (this.particleAge + this.particleMaxAge) / 3 % 2 == 0)
        {
            super.renderParticle(par1Tessellator, par2, par3, par4, par5, par6, par7);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
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
        double omega = (Math.PI*2)/20 - speed/(20*radius);

        if(speed < 0){
        	omega = -(Math.PI*2)/20 + speed/(20*radius);
        }
        
        // v = r times omega; therefore the normalised velocity vector needs to be r times the angle increment / 2 pi.
        this.angle += omega;
        
    	this.motionZ = radius * omega * Math.cos(angle);
    	this.motionX = radius * omega * Math.sin(angle);
        this.moveEntity(motionX, motionY, motionZ);
        
        if(this.gravity){
            this.motionY -= 0.05d;
        }

        if (this.particleAge > this.particleMaxAge / 2)
        {
            this.setAlphaF(1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);
        }
        
    }
/*
    @Override
    public int getBrightnessForRender(float par1)
    {
        return 15728880;
    }

    @Override
    public float getBrightness(float par1)
    {
        return 1.0F;
    }
    */
}
