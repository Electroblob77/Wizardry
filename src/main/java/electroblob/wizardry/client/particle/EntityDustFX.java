package electroblob.wizardry.client.particle;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntityDustFX extends EntityFX
{
	/** The name used to identify this particle. Uses the mod id to avoid any possible conflicts (Not that there would
	 * be any, but I may as well.) */
	public static final String NAME = Wizardry.MODID + "dust";
	
	private final boolean shaded;
	
    public EntityDustFX(World par1World, double x, double y, double z, double par8, double par10, double par12, float r, float g, float b, boolean shaded)
    {
        super(par1World, x, y, z, par8, par10, par12);
        this.particleRed = r;
        this.particleGreen = g;
        this.particleBlue = b;
        this.setParticleTextureIndex(0);
        this.setSize(0.01F, 0.01F);
        this.particleScale *= this.rand.nextFloat() + 0.2F;
        this.motionX = par8;
        this.motionY = par10;
        this.motionZ = par12;
        this.particleMaxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
        this.shaded = shaded;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        //this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.particleMaxAge-- <= 0)
        {
            this.setDead();
        }
    }
    
    @Override
    public int getBrightnessForRender(float par1)
    {
        return shaded ? super.getBrightnessForRender(par1) : 15728880;
    }

    @Override
    public float getBrightness(float par1)
    {
    	return shaded ? super.getBrightness(par1) : 1.0F;
    }
}
