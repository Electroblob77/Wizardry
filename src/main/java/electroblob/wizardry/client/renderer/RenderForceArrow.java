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
public class RenderForceArrow extends Render
{
    private static final ResourceLocation arrowTextures = new ResourceLocation("wizardry:textures/entity/force_arrow.png");

    public RenderForceArrow(){
    	
    }
    
    public void renderArrow(Entity par1EntityArrow, double par2, double par4, double par6, float par8, float par9)
    {
        this.bindEntityTexture(par1EntityArrow);
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
        GL11.glRotatef(par1EntityArrow.prevRotationYaw + (par1EntityArrow.rotationYaw - par1EntityArrow.prevRotationYaw) * par9 - 90.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(par1EntityArrow.prevRotationPitch + (par1EntityArrow.rotationPitch - par1EntityArrow.prevRotationPitch) * par9, 0.0F, 0.0F, 1.0F);
        GL11.glRotatef(180, 0, 1, 0);
        Tessellator tessellator = Tessellator.instance;
        float pixel = 1.0f/32.0f;
        float u1 = 0.0f;
        float u2 = pixel*14;
        float v1 = 0.0f;
        float v2 = pixel*7;
        float u3 = pixel*16;
        float u4 = 1.0f;
        float v3 = 0.0f;
        float v4 = pixel*16;
        float u5 = 0.0f;
        float u6 = pixel*7;
        float v5 = pixel*25;
        float v6 = 1.0f;
        float scale = 0.05625F;
        float f11 = 0.0f;
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        //f11 = (float)par1EntityArrow.arrowShake - par9;
        if (f11 > 0.0F)
        {
            float f12 = -MathHelper.sin(f11 * 3.0F) * f11;
            GL11.glRotatef(f12, 0.0F, 0.0F, 1.0F);
        }
        
        scale*=0.8f;

	    GL11.glRotatef(45.0F, 1.0F, 0.0F, 0.0F);
	    GL11.glScalef(scale, scale, scale);
	    GL11.glTranslatef(-4.0F, 0.0F, 0.0F);

	    tessellator.startDrawingQuads();
	    tessellator.addVertexWithUV(-5, 3.5, -3.5, (double)u5, (double)v5);
	    tessellator.addVertexWithUV(-5, 3.5, 3.5, (double)u6, (double)v5);
	    tessellator.addVertexWithUV(-5, -3.5, 3.5, (double)u6, (double)v6);
	    tessellator.addVertexWithUV(-5, -3.5, -3.5, (double)u5, (double)v6);
	    tessellator.draw();
	    
        for(int i=0; i<5; i++){
        	GL11.glColor4f(1, 1, 1, 1 - i*0.2f);
        	double j = i + ((double)par1EntityArrow.ticksExisted%3)/3;
        	double width = 2.0d + (Math.sqrt(j*2)-0.6)*2;
		    GL11.glNormal3f(scale, 0.0F, 0.0F);
		    tessellator.startDrawingQuads();
		    tessellator.addVertexWithUV(-10 + j*4, -width, -width, (double)u3, (double)v3);
		    tessellator.addVertexWithUV(-10 + j*4, -width, width, (double)u4, (double)v3);
		    tessellator.addVertexWithUV(-10 + j*4, width, width, (double)u4, (double)v4);
		    tessellator.addVertexWithUV(-10 + j*4, width, -width, (double)u3, (double)v4);
		    tessellator.draw();
		    GL11.glNormal3f(-scale, 0.0F, 0.0F);
		    tessellator.startDrawingQuads();
		    tessellator.addVertexWithUV(-10 + j*4, width, -width, (double)u3, (double)v3);
		    tessellator.addVertexWithUV(-10 + j*4, width, width, (double)u4, (double)v3);
		    tessellator.addVertexWithUV(-10 + j*4, -width, width, (double)u4, (double)v4);
		    tessellator.addVertexWithUV(-10 + j*4, -width, -width, (double)u3, (double)v4);
		    tessellator.draw();
        }

    	GL11.glColor4f(1, 1, 1, 1);

        for (int i = 0; i < 4; ++i)
        {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, scale);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(-10.0D, -4.0D, 0.0D, (double)u1, (double)v1);
            tessellator.addVertexWithUV(10.0D, -4.0D, 0.0D, (double)u2, (double)v1);
            tessellator.addVertexWithUV(10.0D, 4.0D, 0.0D, (double)u2, (double)v2);
            tessellator.addVertexWithUV(-10.0D, 4.0D, 0.0D, (double)u1, (double)v2);
            tessellator.draw();
        }

        GL11.glDisable(GL11.GL_BLEND);
        
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
