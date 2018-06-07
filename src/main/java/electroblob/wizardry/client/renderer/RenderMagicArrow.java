package electroblob.wizardry.client.renderer;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;

@SideOnly(Side.CLIENT)
public class RenderMagicArrow extends Render
{
	// This class can now be used to render any projectile; hence this field is no longer static or final.
    private ResourceLocation arrowTextures;
    private boolean blend;
    private boolean renderEnds;
    private double length = 8.0, width = 2.0;
    private int pixelsLong = 16, pixelsWide = 5;

    public RenderMagicArrow(ResourceLocation texture, boolean blend, double length, double width, int pixelsLong, int pixelsWide, boolean renderEnds){
    	this.arrowTextures = texture;
    	this.blend = blend;
    	this.renderEnds = renderEnds;
    	this.length = length;
    	this.width = width;
    	this.pixelsLong = pixelsLong;
    	this.pixelsWide = pixelsWide;
    }
    
    public void renderArrow(Entity entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.bindEntityTexture(entity);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        
        if(this.blend){
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        }
        
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        GL11.glRotatef(entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * par9 - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * par9, 0.0F, 0.0F, 1.0F);
        Tessellator tessellator = Tessellator.instance;
        float f2 = 0.0F;
        float f3 = pixelsLong / 32.0F;
        float f4 = 0.0F;
        float f5 = pixelsWide / 32.0F;
        float f6 = 0.0F;
        float f7 = 0.15625F;
        float f8 = (float)5 / 32.0F;
        float f9 = (float)10 / 32.0F;
        float f10 = 0.05625F;
        float f11 = 0.0f;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        //f11 = (float)par1EntityArrow.arrowShake - par9;
        if (f11 > 0.0F)
        {
            float f12 = -MathHelper.sin(f11 * 3.0F) * f11;
            GL11.glRotatef(f12, 0.0F, 0.0F, 1.0F);
        }

	        GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
	        GL11.glScalef(f10, f10, f10);
	        GL11.glTranslatef(-4.0F, 0.0F, 0.0F);
	        GL11.glNormal3f(f10, 0.0F, 0.0F);

	    if(renderEnds){
	    	// Ends
	        tessellator.startDrawingQuads();
	        tessellator.addVertexWithUV(-7.0D, -width, -width, (double)f6, (double)f8);
	        tessellator.addVertexWithUV(-7.0D, -width, width, (double)f7, (double)f8);
	        tessellator.addVertexWithUV(-7.0D, width, width, (double)f7, (double)f9);
	        tessellator.addVertexWithUV(-7.0D, width, -width, (double)f6, (double)f9);
	        tessellator.draw();
	        GL11.glNormal3f(-f10, 0.0F, 0.0F);
	        tessellator.startDrawingQuads();
	        tessellator.addVertexWithUV(-7.0D, width, -width, (double)f6, (double)f8);
	        tessellator.addVertexWithUV(-7.0D, width, width, (double)f7, (double)f8);
	        tessellator.addVertexWithUV(-7.0D, -width, width, (double)f7, (double)f9);
	        tessellator.addVertexWithUV(-7.0D, -width, -width, (double)f6, (double)f9);
	        tessellator.draw();
        }

        for (int i = 0; i < 4; ++i){
        	// Sides
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, f10);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-length, -width, 0.0D, (double)f2, (double)f4);
            tessellator.addVertexWithUV(length, -width, 0.0D, (double)f3, (double)f4);
            tessellator.addVertexWithUV(length, width, 0.0D, (double)f3, (double)f5);
            tessellator.addVertexWithUV(-length, width, 0.0D, (double)f2, (double)f5);
            tessellator.draw();
        }

        if(this.blend){
            GL11.glDisable(GL11.GL_BLEND);
        }
        
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    protected ResourceLocation getArrowTextures(Entity par1Entity)
    {
        return arrowTextures;
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.getArrowTextures(par1Entity);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.renderArrow(par1Entity, par2, par4, par6, par8, par9);
    }
}
