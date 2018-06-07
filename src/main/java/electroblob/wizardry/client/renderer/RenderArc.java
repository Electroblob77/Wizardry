package electroblob.wizardry.client.renderer;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class RenderArc extends Render {

	private static final ResourceLocation[] textures = new ResourceLocation[16];
	
	public RenderArc(){
		for(int i=0;i<16;i++){
			textures[i] = new ResourceLocation("wizardry:textures/entity/arc_" + i + ".png");
		}
	}
	
	@Override
	public void doRender(Entity entity, double d0, double d1, double d2,
			float fa, float fb) {
		GL11.glPushMatrix();
        GL11.glTranslatef((float)d0, (float)d1, (float)d2);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); //This line fixes the weird brightness bug.
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        
		//System.out.println("Entity coords: " + entity.posX + ", " + entity.posY + ", " + entity.posZ);
		//System.out.println("doRender parameters: " + d0 + ", " + d1 + ", " + d2 + ", " + fa + ", " + fb);
		
		Tessellator tessellator = Tessellator.instance;
		
        if(entity instanceof EntityArc){
	        EntityArc arc = (EntityArc)entity;
	        bindTexture(textures[arc.textureIndex]); // This MUST be after the tessellator declaration and the gl stuff
	        
	        /**
	         * Note: A lot of the maths here works on similar triangles and the ratios between them, avoiding too much
	         * pythagoras and eliminating the need for any trig. Ratios are used for the positioning of the arc endpoints.
	         * Ratios are usually used swapping x and z because the triangles are rotated through 90 degrees.
	        */
	        
	        double dx = -d0;
	        double dy = -d1;
	        double dz = -d2;
	        
	        if(arc.x1 != 0){
		        dx = arc.x1 - arc.posX;// - d2/lengthOffsetRatio;
		        dy = arc.y1 - arc.posY + 0.3;
		        dz = arc.z1 - arc.posZ;// + d0/lengthOffsetRatio;
		        
		        
		        //The distance from caster to target
		        double arcLength = Math.sqrt(dz*dz+dx*dx);
		        
		        //The ratio between the length of the arc and the offset of the start point from the player's centre (which is always 0.3).
		        double lengthOffsetRatio = arcLength/0.3;
	
		        //EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		        
		        //double xViewDist = player.posX - d0;
		        //double yViewDist = player.posY + player.eyeHeight - d1;
		        //double zViewDist = player.posZ - d2;
		        
		        //double xzViewDist = Math.sqrt(xViewDist * xViewDist + zViewDist * zViewDist);
		        
		        //The angle above the horizontal that this particular player is viewing the arc from
		        //double viewAngle = Math.atan(yViewDist/xzViewDist);
		        
		        //Half the width of the arc
		        double arcWidth = 0.3d;
	        	
		        //Right hand side of vertical plane
		        tessellator.startDrawingQuads();
		        //Target end
		        tessellator.addVertexWithUV(0, -0.5, 0, 1, 1);
		        tessellator.addVertexWithUV(0, 0.5, 0, 1, 0);
		        //Caster end
		        tessellator.addVertexWithUV(dx, dy, dz, 0, 0);
		        tessellator.addVertexWithUV(dx, dy-1, dz, 0, 1);
		        tessellator.draw();
		        
		        //Left
		        tessellator.startDrawingQuads();
		        //Target end
		        tessellator.addVertexWithUV(0, -0.5, 0, 1, 1);
		        //Caster end
		        tessellator.addVertexWithUV(dx, dy-1, dz, 0, 1);
		        tessellator.addVertexWithUV(dx, dy, dz, 0, 0);
		        //Target end
		        tessellator.addVertexWithUV(0, 0.5, 0, 1, 0);
		        tessellator.draw();
		        
		        //Bottom of horizontal plane
		        tessellator.startDrawingQuads();
		        tessellator.addVertexWithUV((arcWidth/arcLength)*dz, 0, (-arcWidth/arcLength)*dx, 1, 1);
		        tessellator.addVertexWithUV(dx + (arcWidth/arcLength)*dz, dy-0.5, dz - (arcWidth/arcLength)*dx, 0, 1);
		        tessellator.addVertexWithUV(dx - (arcWidth/arcLength)*dz, dy-0.5, dz + (arcWidth/arcLength)*dx, 0, 0);
		        tessellator.addVertexWithUV((-arcWidth/arcLength)*dz, 0, (arcWidth/arcLength)*dx, 1, 0);
		        tessellator.draw();
		        
		        //Top
		        tessellator.startDrawingQuads();
		        tessellator.addVertexWithUV((arcWidth/arcLength)*dz, 0, (-arcWidth/arcLength)*dx, 1, 1);
		        tessellator.addVertexWithUV((-arcWidth/arcLength)*dz, 0, (arcWidth/arcLength)*dx, 1, 0);
		        tessellator.addVertexWithUV(dx - (arcWidth/arcLength)*dz, dy-0.5, dz + (arcWidth/arcLength)*dx, 0, 0);
		        tessellator.addVertexWithUV(dx + (arcWidth/arcLength)*dz, dy-0.5, dz - (arcWidth/arcLength)*dx, 0, 1);
		        tessellator.draw();
	        }
        }
        
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		if(entity instanceof EntityArc){
			return textures[((EntityArc)entity).textureIndex];
		}
		return null;
	}

}