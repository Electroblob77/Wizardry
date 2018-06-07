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
public class EntitySparkFX extends EntityFX
{
	/** The name used to identify this particle. Uses the mod id to avoid any possible conflicts (Not that there would
	 * be any, but I may as well.) */
	public static final String NAME = Wizardry.MODID + "spark";
	
	private static final ResourceLocation sparkTextures = new ResourceLocation("wizardry:textures/particle/lightning_particles.png");
	
    public EntitySparkFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        super(par1World, par2, par4, par6, par8, par10, par12);
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.setParticleTextureIndex(rand.nextInt(8));
        this.setSize(0.02F, 0.02F);
        this.particleScale *= 1.4f;
        this.motionX = par8 * 0.20000000298023224D + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
        this.motionY = par10 * 0.20000000298023224D + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
        this.motionZ = par12 * 0.20000000298023224D + (double)((float)(Math.random() * 2.0D - 1.0D) * 0.02F);
        this.particleMaxAge = 3;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if(this.particleAge++ >= this.particleMaxAge)
        {
            this.setDead();
        }
    }
    
    /**
     * Public method to set private field particleTextureIndex.
     */
    public void setParticleTextureIndex(int par1)
    {
        this.particleTextureIndexY = par1;
    }
    
    @Override
    public int getFXLayer() {
    	// This can only be 0-3 or it will cause an ArrayIndexOutOfBoundsException in EffectRenderer.
        return 3;
    }
    
    // Overwitten from entityfx to bind the new texture.
    @Override
    public void renderParticle(Tessellator par1Tessellator, float partialTicks, float par3, float par4, float par5, float par6, float par7)
    {
    	GL11.glPushMatrix();
    	GL11.glDisable(GL11.GL_LIGHTING);
    	GL11.glEnable(GL11.GL_BLEND);
    	GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        
        RenderHelper.disableStandardItemLighting();

    	Minecraft.getMinecraft().renderEngine.bindTexture(sparkTextures);
        
        par1Tessellator.startDrawingQuads();
    	
        // Particles are now animated using a strip of 4 from left to right.
        float f6 = (float)this.particleAge / 4.0F;
        float f7 = f6 + 0.0624375F*4;
        float f8 = (float)this.particleTextureIndexY / 8.0f;
        float f9 = f8 + 0.0624375F*2;
        float f10 = 0.1F * this.particleScale;
        
        // Fix which gets proper values for interpPos so the particles don't appear to move.
        Entity player = Minecraft.getMinecraft().thePlayer;
        this.interpPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double)partialTicks;
   	 	this.interpPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double)partialTicks;
   	 	this.interpPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double)partialTicks;
        
        float f11 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float f12 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float f13 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        float f14 = 1.0F;
        
        par1Tessellator.setColorRGBA_F(this.particleRed * f14, this.particleGreen * f14, this.particleBlue * f14, this.particleAlpha);
        par1Tessellator.addVertexWithUV((double)(f11 - par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 - par5 * f10 - par7 * f10), (double)f7, (double)f9);
        par1Tessellator.addVertexWithUV((double)(f11 - par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 - par5 * f10 + par7 * f10), (double)f7, (double)f8);
        par1Tessellator.addVertexWithUV((double)(f11 + par3 * f10 + par6 * f10), (double)(f12 + par4 * f10), (double)(f13 + par5 * f10 + par7 * f10), (double)f6, (double)f8);
        par1Tessellator.addVertexWithUV((double)(f11 + par3 * f10 - par6 * f10), (double)(f12 - par4 * f10), (double)(f13 + par5 * f10 - par7 * f10), (double)f6, (double)f9);
        
        par1Tessellator.draw();
        
        GL11.glEnable(GL11.GL_LIGHTING);
    	GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    
    }
}
