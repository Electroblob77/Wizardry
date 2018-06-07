package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public class RenderFirestorm extends Render {
	
    private final ResourceLocation texture;
    private float scale = 1.0f;
    
	public RenderFirestorm(ResourceLocation texture, float scale) {
		this.texture = texture;
		this.scale = scale;
	}

	@Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9){
		
        GL11.glPushMatrix();
        
        GL11.glTranslatef((float)par2, (float)par4, (float)par6);
		
		GL11.glRotatef(-entity.rotationYaw, 0, 1, 0);
		GL11.glRotatef(entity.rotationPitch, 1, 0, 0);
        
		GL11.glPushMatrix();

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        this.bindTexture(texture);
        float f6 = 1.0F;
        float f7 = 0.5F;
        float f8 = 0.5F;
        
        //GL11.glRotatef(-90, 1, 0, 0);
        
        GL11.glScalef(scale, scale, scale);
        
        Tessellator tessellator = Tessellator.instance;
        
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        
	    tessellator.addVertexWithUV((double)(0.0F - f7), (double)(0.0F - f8), 0.01, 0, 1);
	    tessellator.addVertexWithUV((double)(f6 - f7), (double)(0.0F - f8), 0.01, 1, 1);
	    tessellator.addVertexWithUV((double)(f6 - f7), (double)(1.0F - f8), 0.01, 1, 0);
	    tessellator.addVertexWithUV((double)(0.0F - f7), (double)(1.0F - f8), 0.01, 0, 0);
        
        tessellator.draw();
        
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        
	    tessellator.addVertexWithUV((double)(0.0F - f7), (double)(0.0F - f8), 0.01, 0, 1);
	    tessellator.addVertexWithUV((double)(0.0F - f7), (double)(1.0F - f8), 0.01, 0, 0);
	    tessellator.addVertexWithUV((double)(f6 - f7), (double)(1.0F - f8), 0.01, 1, 0);
	    tessellator.addVertexWithUV((double)(f6 - f7), (double)(0.0F - f8), 0.01, 1, 1);
        
        tessellator.draw();
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        //GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        
        GL11.glPopMatrix();
        
        // Fire
        
        GL11.glDisable(GL11.GL_LIGHTING);
        IIcon icon = Blocks.fire.getFireIcon(0);
        IIcon icon1 = Blocks.fire.getFireIcon(1);
        
        int sides = 16;
        float height = 2.0f;
        
        for(int k=0; k<sides; k++){
	        
	        GL11.glPushMatrix();
	        //GL11.glTranslatef((float)par2, (float)par4 + 0.05f, (float)par6);

	        GL11.glScalef(0.6f, 0.6f, 0.6f);
	        GL11.glRotatef(-90, 1, 0, 0);
	        
	        float f2 = 0.5F;
	        float f3 = 0.0F;
	        float f4 = 0.2f;
	        float f5 = (float)(entity.posY - entity.boundingBox.minY);
	        //GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
	        //GL11.glTranslatef(0.0F, 0.0F, -0.3F + (float)((int)f4) * 0.02F);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        float f61 = 0.0F;
	        int i = 0;
	        
	        GL11.glRotatef((360f/(float)sides)*k, 0, 1, 0);
	        GL11.glTranslatef(0, 0, -2.3f);
	        
	        tessellator.startDrawingQuads();
	
	        while (f4 > 0.0F)
	        {
	            IIcon icon2 = i % 2 == 0 ? icon : icon1;
	            this.bindTexture(TextureMap.locationBlocksTexture);
	            float f71 = icon2.getMinU();
	            float f81 = icon2.getMinV();
	            float f9 = icon2.getMaxU();
	            float f10 = icon2.getMaxV();
	
	            if (i / 2 % 2 == 0)
	            {
	                float f11 = f9;
	                f9 = f71;
	                f71 = f11;
	            }
	
	            tessellator.addVertexWithUV((double)(f2 - f3), (double)(0.0F - f5), (double)f61, (double)f9, (double)f10);
	            tessellator.addVertexWithUV((double)(-f2 - f3), (double)(0.0F - f5), (double)f61, (double)f71, (double)f10);
	            tessellator.addVertexWithUV((double)(-f2 - f3), (double)(height - f5), (double)f61, (double)f71, (double)f81);
	            tessellator.addVertexWithUV((double)(f2 - f3), (double)(height - f5), (double)f61, (double)f9, (double)f81);
	            f4 -= 0.45F;
	            f5 -= 0.45F;
	            f2 *= 0.9F;
	            f61 += 0.03F;
	            ++i;
	        }
	
	        tessellator.draw();
	        GL11.glPopMatrix();
        }

        for(int k=0; k<sides; k++){
	        
	        GL11.glPushMatrix();
	        //GL11.glTranslatef((float)par2, (float)par4 + 0.05f, (float)par6);

	        GL11.glScalef(0.6f, 0.6f, 0.6f);
	        GL11.glRotatef(-90, 1, 0, 0);
	        
	        float f2 = 0.5F;
	        float f3 = 0.0F;
	        float f4 = 0.2f;
	        float f5 = (float)(entity.posY - entity.boundingBox.minY);
	        //GL11.glRotatef(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
	        //GL11.glTranslatef(0.0F, 0.0F, -0.3F + (float)((int)f4) * 0.02F);
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        float f61 = 0.0F;
	        int i = 0;
	        
	        GL11.glRotatef((360f/(float)sides)*k, 0, 1, 0);
	        GL11.glTranslatef(0, 0, 2.3f);
	        
	        tessellator.startDrawingQuads();
	
	        while (f4 > 0.0F)
	        {
	            IIcon icon2 = i % 2 == 0 ? icon : icon1;
	            this.bindTexture(TextureMap.locationBlocksTexture);
	            float f71 = icon2.getMinU();
	            float f81 = icon2.getMinV();
	            float f9 = icon2.getMaxU();
	            float f10 = icon2.getMaxV();
	
	            if (i / 2 % 2 == 0)
	            {
	                float f11 = f9;
	                f9 = f71;
	                f71 = f11;
	            }
	
	            tessellator.addVertexWithUV((double)(f2 - f3), (double)(0.0F - f5), (double)f61, (double)f9, (double)f10);
	            tessellator.addVertexWithUV((double)(-f2 - f3), (double)(0.0F - f5), (double)f61, (double)f71, (double)f10);
	            tessellator.addVertexWithUV((double)(-f2 - f3), (double)(height - f5), (double)f61, (double)f71, (double)f81);
	            tessellator.addVertexWithUV((double)(f2 - f3), (double)(height - f5), (double)f61, (double)f9, (double)f81);
	            f4 -= 0.45F;
	            f5 -= 0.45F;
	            f2 *= 0.9F;
	            f61 += 0.03F;
	            ++i;
	        }
	
	        tessellator.draw();
	        GL11.glPopMatrix();
        }
        
        GL11.glEnable(GL11.GL_LIGHTING);
        
        GL11.glPopMatrix();
    }

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
