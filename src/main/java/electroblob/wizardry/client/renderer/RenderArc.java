package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityArc;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderArc extends Render<EntityArc> {

	private static final ResourceLocation[] textures = new ResourceLocation[16];

	public RenderArc(RenderManager renderManager){
		super(renderManager);
		for(int i=0;i<16;i++){
			textures[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/arc_" + i + ".png");
		}
	}

	@Override
	public void doRender(EntityArc arc, double d0, double d1, double d2,
			float fa, float fb) {
		GlStateManager.pushMatrix();
		GlStateManager.translate((float)d0, (float)d1, (float)d2);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); //This line fixes the weird brightness bug.
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		//System.out.println("Entity coords: " + entity.posX + ", " + entity.posY + ", " + entity.posZ);
		//System.out.println("doRender parameters: " + d0 + ", " + d1 + ", " + d2 + ", " + fa + ", " + fb);

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();

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
			//double lengthOffsetRatio = arcLength/0.3;

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
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			//Target end
			buffer.pos(0, -0.5, 0).tex(1, 1).endVertex();
			buffer.pos(0, 0.5, 0).tex(1, 0).endVertex();
			//Caster end
			buffer.pos(dx, dy, dz).tex(0, 0).endVertex();
			buffer.pos(dx, dy-1, dz).tex(0, 1).endVertex();
			tessellator.draw();

			//Left
			buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
			//Target end
			buffer.pos(0, -0.5, 0).tex(1, 1).endVertex();
			//Caster end
			buffer.pos(dx, dy-1, dz).tex(0, 1).endVertex();
			buffer.pos(dx, dy, dz).tex(0, 0).endVertex();
			//Target end
			buffer.pos(0, 0.5, 0).tex(1, 0).endVertex();
			tessellator.draw();

			//Bottom of horizontal plane
			buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
			buffer.pos((arcWidth/arcLength)*dz, 0, (-arcWidth/arcLength)*dx).tex(1, 1).endVertex();
			buffer.pos(dx + (arcWidth/arcLength)*dz, dy-0.5, dz - (arcWidth/arcLength)*dx).tex(0, 1).endVertex();
			buffer.pos(dx - (arcWidth/arcLength)*dz, dy-0.5, dz + (arcWidth/arcLength)*dx).tex(0, 0).endVertex();
			buffer.pos((-arcWidth/arcLength)*dz, 0, (arcWidth/arcLength)*dx).tex(1, 0).endVertex();
			tessellator.draw();

			//Top
			buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
			buffer.pos((arcWidth/arcLength)*dz, 0, (-arcWidth/arcLength)*dx).tex(1, 1).endVertex();
			buffer.pos((-arcWidth/arcLength)*dz, 0, (arcWidth/arcLength)*dx).tex(1, 0).endVertex();
			buffer.pos(dx - (arcWidth/arcLength)*dz, dy-0.5, dz + (arcWidth/arcLength)*dx).tex(0, 0).endVertex();
			buffer.pos(dx + (arcWidth/arcLength)*dz, dy-0.5, dz - (arcWidth/arcLength)*dx).tex(0, 1).endVertex();
			tessellator.draw();
		}

		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityArc entity) {
		return textures[entity.textureIndex];
	}

}