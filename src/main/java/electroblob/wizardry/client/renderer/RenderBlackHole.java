package electroblob.wizardry.client.renderer;

import java.util.ArrayList;
import java.util.Collections;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderBlackHole extends Render<EntityBlackHole> {

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/entity/dark_ray.png");
	private static final ResourceLocation texture2 = new ResourceLocation(Wizardry.MODID, "textures/entity/black_hole.png");

	public RenderBlackHole(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityBlackHole blackhole, double x, double y, double z, float fa, float fb) {

		GlStateManager.pushMatrix();

		GlStateManager.disableCull();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		RenderHelper.disableStandardItemLighting();

		GlStateManager.translate(x, y, z);

		//float pitch = (float) Math.toDegrees(Math.atan(y/(x*x+z*z)));
		//float yaw = (float) Math.toDegrees(Math.atan(x/z));

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();

		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		if(blackhole.ticksExisted < 10){
			GlStateManager.scale((float)blackhole.ticksExisted/10, (float)blackhole.ticksExisted/10, (float)blackhole.ticksExisted/10);
		}
		if(blackhole.ticksExisted > blackhole.lifetime - 10){
			GlStateManager.scale((float)(blackhole.lifetime-blackhole.ticksExisted)/10, (float)(blackhole.lifetime-blackhole.ticksExisted)/10, (float)(blackhole.lifetime-blackhole.ticksExisted)/10);
		}

		this.bindTexture(texture);

		// In theory this stuff should sort the rays into the correct render order based on the distance from
		// the 'camera' (i.e. the player's viewpoint)
		// In the end it was easier to do away with the openGL rotation because for some reason it produced
		// coordinates which were inconsistent with my calculated ones. Since I know those will give the desired
		// effect anyway, I just used them directly instead.

		ArrayList<RayHelper> rays = new ArrayList<RayHelper>(1);

		for(int j=0; j<30; j++){

			float scale = 3.0f;

			int a = blackhole.randomiser[j];
			int b = blackhole.randomiser2[j];

			int sliceAngle = 20 + a;

			double x1 = scale*Math.sin((blackhole.ticksExisted + 40*j)*(Math.PI/180));
			//double y1 = 0.7*Math.cos((blackhole.timer - 40*j)*(Math.PI/180))*j/10;
			double z1 = scale*Math.cos((blackhole.ticksExisted + 40*j)*(Math.PI/180));

			double x2 = scale*Math.sin((blackhole.ticksExisted + 40*j - sliceAngle)*(Math.PI/180));
			//double y2 = 0.7*Math.sin((blackhole.timer - 40*j)*(Math.PI/180))*j/10;
			double z2 = scale*Math.cos((blackhole.ticksExisted + 40*j - sliceAngle)*(Math.PI/180));

			double absoluteX = x1*Math.cos(31*b);
			double absoluteY = z1*Math.sin(31*a) + x1*Math.cos(31*a)*Math.sin(31*b);
			double absoluteZ = z1*Math.cos(31*a);

			double absoluteX2 = x2*Math.cos(31*b);
			double absoluteY2 = z2*Math.sin(31*a) + x2*Math.cos(31*a)*Math.sin(31*b);
			double absoluteZ2 = z2*Math.cos(31*a);
			/*
		        buffer.begin(0, DefaultVertexFormats.POSITION_TEX);

		        tessellator.setColorOpaque(255, 255, 255);
		        GL11.glPointSize(5);

		        tessellator.addVertex(absoluteX-x, 0, 0);
		        tessellator.addVertex(0, absoluteY-y, 0);
		        tessellator.addVertex(0, 0, absoluteZ-z);

		        tessellator.draw();
			 */
			rays.add(new RayHelper(j, absoluteX, absoluteY, absoluteZ, absoluteX2, absoluteY2, absoluteZ2, x, y, z));

		}

		Collections.sort(rays);

		for(RayHelper ray : rays){

			GlStateManager.pushMatrix();
			//GlStateManager.rotate(31*blackhole.randomiser[ray.ordinal], 1, 0, 0);
			//GlStateManager.rotate(31*blackhole.randomiser2[ray.ordinal], 0, 0, 1);

			buffer.begin(5, DefaultVertexFormats.POSITION_TEX);

			//tessellator.setColorRGBA(255, 255, 255, 0);
			buffer.pos(0, 0, 0).tex(0, 0).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();

			//tessellator.setColorRGBA(0, 0, 0, 255);
			buffer.pos(ray.x1, ray.y1, ray.z1).tex(1, 0).endVertex();
			buffer.pos(ray.x2, ray.y2, ray.z2).tex(1, 1).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();
		}

		GlStateManager.pushMatrix();

		/* Deprecated in favour of particle style method.
        GlStateManager.rotate(yaw, 0, 1, 0);
        // GL transformations are relative, hence only x rotation
        if(z < 0){
        	GlStateManager.rotate(pitch, 1, 0, 0);
        }else{
        	GlStateManager.rotate(-1*pitch, 1, 0, 0);
        }
		 */

		// Renders the aura effect

		// This counteracts the reverse rotation behaviour when in front f5 view. Vanilla now has this fix too.
		float yaw = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? this.renderManager.playerViewX : -this.renderManager.playerViewX;
		GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		this.bindTexture(texture2);

		buffer.pos(-0.4, 0.4, 0).tex(0, 0).endVertex();
		buffer.pos(0.4, 0.4, 0).tex(1, 0).endVertex();
		buffer.pos(0.4, -0.4, 0).tex(1, 1).endVertex();
		buffer.pos(-0.4, -0.4, 0).tex(0, 1).endVertex();

		tessellator.draw();

		GlStateManager.popMatrix();


		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlackHole entity) {
		return texture;
	}

}
