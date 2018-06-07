package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import electroblob.wizardry.entity.construct.EntityBubble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderBubble extends Render {
	
    private static final ResourceLocation particleTextures = new ResourceLocation("textures/particle/particles.png");
    private static final ResourceLocation darkOrbTexture = new ResourceLocation("wizardry:textures/entity/dark_orb.png");

	public RenderBubble() {
		
	}

	@Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9)
    {
		if(entity instanceof EntityBubble){
			
	        GL11.glPushMatrix();
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	        
	        float yOffset = 0;
	        
	        if(entity.riddenByEntity != null){
	        	yOffset = entity.riddenByEntity.height/2;
	        }
	        
	        GL11.glTranslatef((float)par2, (float)par4 + yOffset, (float)par6);
	        
	        this.bindTexture(((EntityBubble)entity).isDarkOrb ? darkOrbTexture : particleTextures);
	        float f6 = 1.0F;
	        float f7 = 0.5F;
	        float f8 = 0.5F;
	        
	        if(((EntityBubble)entity).isDarkOrb){
	        	GL11.glDisable(GL11.GL_LIGHTING);
		        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
	        }else{
		        int j = entity.getBrightnessForRender(par9);
		        int k = j % 65536;
		        int l = j / 65536;
		        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k / 1.0F, (float)l / 1.0F);
	        }
	        
	        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	        
	        // This counteracts the reverse rotation behaviour when in front f5 view.
	        // Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
	        float yaw = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? this.renderManager.playerViewX : -this.renderManager.playerViewX;
	        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(yaw, 1.0F, 0.0F, 0.0F);
	        
	        float f11 = 3.0F;
	        GL11.glScalef(f11, f11, f11);
	        
	        double pixelwidth = (1.0d/128);
	        
	        Tessellator tessellator = Tessellator.instance;
	        tessellator.startDrawingQuads();
	        //tessellator.setColorRGBA_I(k1, 128);
	        tessellator.setNormal(0.0F, 1.0F, 0.0F);
	        
	        if(((EntityBubble)entity).isDarkOrb){
		        tessellator.addVertexWithUV((double)(0.0F - f7), (double)(0.0F - f8), 0.0D, 0, 1);
		        tessellator.addVertexWithUV((double)(f6 - f7), (double)(0.0F - f8), 0.0D, 1, 1);
		        tessellator.addVertexWithUV((double)(f6 - f7), (double)(1.0F - f8), 0.0D, 1, 0);
		        tessellator.addVertexWithUV((double)(0.0F - f7), (double)(1.0F - f8), 0.0D, 0, 0);
		    }else{
		        tessellator.addVertexWithUV((double)(0.0F - f7), (double)(0.0F - f8), 0.0D, pixelwidth, pixelwidth * 24);
		        tessellator.addVertexWithUV((double)(f6 - f7), (double)(0.0F - f8), 0.0D, pixelwidth*8, pixelwidth * 24);
		        tessellator.addVertexWithUV((double)(f6 - f7), (double)(1.0F - f8), 0.0D, pixelwidth*8, pixelwidth * 17);
		        tessellator.addVertexWithUV((double)(0.0F - f7), (double)(1.0F - f8), 0.0D, pixelwidth, pixelwidth * 17);
		    }
	        
	        tessellator.draw();
	        
	        GL11.glDisable(GL11.GL_BLEND);
	        if(((EntityBubble)entity).isDarkOrb){
	        	GL11.glEnable(GL11.GL_LIGHTING);
	        }
	        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	        GL11.glPopMatrix();
		}
    }

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
