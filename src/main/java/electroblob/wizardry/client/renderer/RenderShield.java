package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.entity.EntityShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class RenderShield extends Render{

	private static final ResourceLocation texture = new ResourceLocation("wizardry:textures/entity/shield.png");
	private static final ResourceLocation RES_ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
	
	@Override
	public void doRender(Entity entity, double x, double y, double z,
			float fa, float fb) {
		if(entity instanceof EntityShield){
			
			EntityShield shield = (EntityShield)entity;
		
			GL11.glPushMatrix();
	
	        GL11.glDisable(GL11.GL_CULL_FACE);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
	        GL11.glShadeModel(GL11.GL_SMOOTH);
	        //GL11.glDisable(GL11.GL_LIGHTING);
	        if(Minecraft.getMinecraft().renderViewEntity != shield.player.get() || Minecraft.getMinecraft().gameSettings.thirdPersonView != 0){
		        GL11.glDisable(GL11.GL_LIGHTING);
	        }
        	OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
	        //RenderHelper.disableStandardItemLighting();
	
			GL11.glTranslated(x, y + 0.3, z);
			GL11.glRotatef(-1*shield.rotationYaw, 0, 1, 0);
			GL11.glRotatef(shield.rotationPitch, 1, 0, 0);
			
	        Tessellator tessellator = Tessellator.instance;
			
			this.bindTexture(texture);
			
			this.renderShield(tessellator, -1);
	        
	        // Enchantment effect

	        GL11.glPushMatrix();
	        
            GL11.glDisable(GL11.GL_LIGHTING);
            this.bindTexture(RES_ITEM_GLINT);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            float f7 = 0.76F;
            GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
            float f8 = 0.125F;
            //GL11.glScalef(f8, f8, f8);
            float f9 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 0.8f;
            //GL11.glTranslatef(f9, 0.0F, 0.0F);
            //GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
            
            //this.renderShield(tessellator, f9);
		
            GL11.glPopMatrix();
            
	        GL11.glEnable(GL11.GL_LIGHTING);
            
	        GL11.glShadeModel(GL11.GL_FLAT);
	        GL11.glEnable(GL11.GL_CULL_FACE);
	        GL11.glDisable(GL11.GL_BLEND);
	        //RenderHelper.enableStandardItemLighting();
	        
			GL11.glPopMatrix();
		}
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}
	
	private void renderShield(Tessellator tessellator, float textureOffset){
		
		double widthOuter = 0.6d;
		double heightOuter = 0.7d;
		double widthInner = 0.3d;
		double heightInner = 0.4d;
		double depth = 0.2d;
		
		double textureSection = 1.0d;
		double textureU = 0.0d;
		
		if(textureOffset != -1){
			textureSection = 0.2d;
			textureU = textureOffset;
		}
		
		tessellator.startDrawing(5);
        
        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(-widthOuter, heightOuter - 0.3, -depth, 0, 0);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);
        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(-widthOuter + 0.3, heightOuter, -depth, 0, 0);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);

        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(widthOuter - 0.3, heightOuter, -depth, 1, 0);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(widthInner, heightInner, 0, textureU + textureSection, 0.2);
        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(widthOuter, heightOuter - 0.3, -depth, 1, 0);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(widthInner, heightInner, 0, textureU + textureSection, 0.2);

        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(widthOuter, -heightOuter + 0.3, -depth, 1, 1);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(widthInner, -heightInner, 0, textureU + textureSection, 0.8);
        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(widthOuter - 0.3, -heightOuter, -depth, 1, 1);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(widthInner, -heightInner, 0, textureU + textureSection, 0.8);

        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(-widthOuter + 0.3, -heightOuter, -depth, 0, 1);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(-widthInner, -heightInner, 0, textureU, 0.8);
        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(-widthOuter, -heightOuter + 0.3, -depth, 0, 1);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(-widthInner, -heightInner, 0, textureU, 0.8);

        tessellator.setColorRGBA(0, 0, 0, 255);
        tessellator.addVertexWithUV(-widthOuter, heightOuter - 0.3, -depth, 0, 0);
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);

        tessellator.draw();
        
        tessellator.startDrawing(5);
        
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);

        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(widthInner, heightInner, 0, textureU + textureSection, 0.2);
        
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(-widthInner, -heightInner, 0, textureU, 0.8);
        
        tessellator.setColorOpaque(200, 200, 255);
        tessellator.addVertexWithUV(widthInner, -heightInner, 0, textureU + textureSection, 0.8);

        tessellator.draw();
	}

}
