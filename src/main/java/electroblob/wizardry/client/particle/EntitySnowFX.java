package electroblob.wizardry.client.particle;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class EntitySnowFX extends EntityFX
{
	/** The name used to identify this particle. Uses the mod id to avoid any possible conflicts (Not that there would
	 * be any, but I may as well.) */
	public static final String NAME = Wizardry.MODID + "snow";
	
	private static final ResourceLocation snowTextures = new ResourceLocation("wizardry:textures/particle/snow_particles.png");
	
    public EntitySnowFX(World world, double x, double y, double z, double velX, double velY, double velZ)
    {
        super(world, x, y, z, velX, velY, velZ);
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.setParticleTextureIndex(rand.nextInt(8));
        this.setSize(0.02F, 0.02F);
        this.particleScale *= 0.6f;
        this.motionX = velX;
        this.motionY = velY;
        this.motionZ = velZ;
    	this.particleMaxAge = 40 + rand.nextInt(10);
    }
    
    public EntitySnowFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12, int maxAge){
    	this(par1World, par2, par4, par6, par8, par10, par12);
    	this.particleMaxAge = maxAge;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);

        if (this.particleMaxAge-- <= 0)
        {
            this.setDead();
        }
    }
    
    /**
     * Public method to set private field particleTextureIndex.
     */
    public void setParticleTextureIndex(int par1)
    {
        this.particleTextureIndexX = par1 % 4;
        this.particleTextureIndexY = par1 / 4;
    }
    
    @Override
    public int getFXLayer() {
    	// This can only be 0-3 or it will cause an ArrayIndexOutOfBoundsException in EffectRenderer.
        return 3;
    }
    
    // Overwitten from entityfx to bind the new texture.
    @Override
    public void renderParticle(Tessellator tessellator, float partialTicks, float par3, float par4, float par5, float par6, float par7)
    {
    	GL11.glPushMatrix();
        
    	// This stuff does the shading. The parameter does nothing apparently.
        int j = this.getBrightnessForRender(0);
        int k = j % 65536;
        int l = j / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k / 1.0F, (float)l / 1.0F);

        RenderHelper.disableStandardItemLighting();

    	Minecraft.getMinecraft().renderEngine.bindTexture(snowTextures);
        
        tessellator.startDrawingQuads();
    	
        float f6 = (float)this.particleTextureIndexX / 4.0F;
        float f7 = f6 + 0.0624375F*4;
        float f8 = (float)this.particleTextureIndexY / 4.0F;
        float f9 = f8 + 0.0624375F*4;
        float f10 = 0.1F * this.particleScale;

        if (this.particleIcon != null)
        {
            f6 = this.particleIcon.getMinU();
            f7 = this.particleIcon.getMaxU();
            f8 = this.particleIcon.getMinV();
            f9 = this.particleIcon.getMaxV();
        }
        
        // Fix which gets proper values for interpPos so the particles don't appear to move.
        Entity player = Minecraft.getMinecraft().thePlayer;
        this.interpPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
   	 	this.interpPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
   	 	this.interpPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;

        float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        float f14 = 1.0F;
        
        tessellator.setColorRGBA_F(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha);
        tessellator.addVertexWithUV((double)(f11 - par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 - par5 * f10 - par7 * f10), (double)f7, (double)f9);
        tessellator.addVertexWithUV((double)(f11 - par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 - par5 * f10 + par7 * f10), (double)f7, (double)f8);
        tessellator.addVertexWithUV((double)(f11 + par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 + par5 * f10 + par7 * f10), (double)f6, (double)f8);
        tessellator.addVertexWithUV((double)(f11 + par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 + par5 * f10 - par7 * f10), (double)f6, (double)f9);
        
        tessellator.draw();

        GL11.glPopMatrix();
    
    }
}
