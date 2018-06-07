package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.tileentity.TileEntityMagicLight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderMagicLight extends TileEntitySpecialRenderer{

	private static final ResourceLocation texture = new ResourceLocation("wizardry:textures/entity/light_ray.png");
	private static final ResourceLocation texture2 = new ResourceLocation("wizardry:textures/entity/light_aura.png");
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {

		GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glDisable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        RenderHelper.disableStandardItemLighting();
        
		if(tileentity instanceof TileEntityMagicLight){
			
			TileEntityMagicLight timerentity = (TileEntityMagicLight)tileentity;

			GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);

			if(timerentity.timer < 10){
				GL11.glScalef((float)timerentity.timer/10, (float)timerentity.timer/10, (float)timerentity.timer/10);
			}
			if(timerentity.timer > timerentity.maxTimer-10){
				GL11.glScalef((float)(timerentity.maxTimer-timerentity.timer)/10, (float)(timerentity.maxTimer-timerentity.timer)/10, (float)(timerentity.maxTimer-timerentity.timer)/10);
			}
			
			// Renders the aura effect
			
	        Tessellator tessellator = Tessellator.instance;
	        
	        GL11.glPushMatrix();
	        
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

	        // This counteracts the reverse rotation behaviour when in front f5 view.
	        // Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
	        float yaw = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? RenderManager.instance.playerViewX : -RenderManager.instance.playerViewX;
	        GL11.glRotatef(180.0F - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
	        GL11.glRotatef(yaw, 1.0F, 0.0F, 0.0F);
	        
	        tessellator.startDrawingQuads();

			this.bindTexture(texture2);
	        
	        tessellator.addVertexWithUV(-0.6, 0.6, 0, 0, 0);
	        tessellator.addVertexWithUV(0.6, 0.6, 0, 1, 0);
	        tessellator.addVertexWithUV(0.6, -0.6, 0, 1, 1);
	        tessellator.addVertexWithUV(-0.6, -0.6, 0, 0, 1);
	        
	        tessellator.draw();
	        
	        GL11.glPopMatrix();
	        
	        // Renders the rays
	        
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
			
			this.bindTexture(texture);
			
			for(int j=0; j<30; j++){
				
				int sliceAngle = 20 + timerentity.randomiser[j];
				float scale = 0.5f;
				
				GL11.glPushMatrix();
				
				GL11.glRotatef(31*timerentity.randomiser[j], 1, 0, 0);
				GL11.glRotatef(31*timerentity.randomiser2[j], 0, 0, 1);
				
				tessellator.startDrawing(5);

		        tessellator.setColorOpaque_I(16777215);
		        tessellator.addVertexWithUV(0, 0, 0, 0, 0);
		        tessellator.addVertexWithUV(0, 0, 0, 0, 1);
		        
		        double x1 = scale*Math.sin((timerentity.timer + 40*j)*(Math.PI/180));
		        //double y1 = 0.7*Math.cos((timerentity.timer - 40*j)*(Math.PI/180))*j/10;
		        double z1 = scale*Math.cos((timerentity.timer + 40*j)*(Math.PI/180));

		        double x2 = scale*Math.sin((timerentity.timer + 40*j - sliceAngle)*(Math.PI/180));
		        //double y2 = 0.7*Math.sin((timerentity.timer - 40*j)*(Math.PI/180))*j/10;
		        double z2 = scale*Math.cos((timerentity.timer + 40*j - sliceAngle)*(Math.PI/180));
		        
		        tessellator.setColorRGBA(0, 0, 0, 255);
		        tessellator.addVertexWithUV(x1, 0, z1, 1, 0);
		        tessellator.addVertexWithUV(x2, 0, z2, 1, 1);
		
		        tessellator.draw();
		        
		        GL11.glPopMatrix();
			}
		}
		
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        RenderHelper.enableStandardItemLighting();
        
		GL11.glPopMatrix();
	}

}
