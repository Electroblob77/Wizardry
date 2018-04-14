package electroblob.wizardry.client.renderer;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.entity.EntityArc;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

public class RenderArc extends Render<EntityArc> {

	public RenderArc(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityArc arc, double x, double y, double z, float viewPitch, float viewYaw) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		Tessellator tessellator = Tessellator.getInstance();

		/* Note: A lot of the maths here works on similar triangles and the ratios between them, avoiding too much
		 * pythagoras and eliminating the need for any trig. Ratios are used for the positioning of the arc endpoints.
		 * Ratios are usually used swapping x and z because the triangles are rotated through 90 degrees. */

		double dx = -x;
		double dy = -y;
		double dz = -z;

		if(arc.x1 != 0){

			dx = arc.x1 - arc.posX;
			dy = arc.y1 - arc.posY;
			dz = arc.z1 - arc.posZ;

			// The distance from origin to endpoint
			double arcLength = Math.sqrt(dx*dx+dy*dy+dz*dz);

			GL11.glTranslated(dx, dy, dz);

			// Math.atan2 computes within -180 to +180, rather than -90 to +90.
			float yaw = (float)(180d/Math.PI * Math.atan2(-dx, -dz));
			float pitch = (float)(180f/(float)Math.PI * Math.atan(dy/Math.sqrt(dz*dz+dx*dx)));

			GL11.glRotatef(yaw, 0, 1, 0);
			GL11.glRotatef(pitch, 1, 0, 0);

			// The direction of the arc drawn by the tessellator is always along the z axis and is rotated to the
			// correct orientation, that way there isn't a ton of trigonometry and the code is way neater.

			boolean freeEnd = arc.freeEnd;

			// == To be extracted as constants later ==
			float thickness = 0.04f; // Half the width of the outermost layer
			double maxSegmentLength = 0.6; // Max length of a segment, obviously
			double minSegmentLength = 0.2; // Min length of a segment
			double vertexDither = 0.15; // Max deviation in x or y axis from the centreline
			int maxForkSegments = 3; // Max number of segments a fork can have
			float forkChance = 0.3f; // Chance (as a fraction) that a vertex will have a fork
			int updateTime = 1; // Number of ticks to wait before the arc changes shape again

			int numberOfSegments = (int)Math.round(arcLength/maxSegmentLength); // Number of segments

			for(int layer=0; layer<3; layer++){

				double px=0, py=0, pz=0;
				// Creates a random from the arc's seed field + the number of ticks it has existed/the update period.
				// By using a seed, we can ensure the vertex positions and forks are identical a) for each layer, even
				// though they are rendered sequentially, and b) across many frames (and ticks, if updateTime > 1).
				Random random = new Random(arc.seed + arc.ticksExisted/updateTime);

				// numberOfSegments-1 because the last segment is handled separately.
				for(int i=0; i<numberOfSegments-1; i++){

					double px2 = (random.nextDouble()*2-1)*vertexDither; // Maybe use Gaussians?
					double py2 = (random.nextDouble()*2-1)*vertexDither;
					double pz2 = pz + arcLength/(double)numberOfSegments; // For now they are all the same length

					drawSegment(tessellator, layer, px, py, pz, px2, py2, pz2, thickness);

					// Forks
					if(random.nextFloat() < forkChance){

						double px3=px, py3=py, pz3=pz;

						for(int j=0; j<random.nextInt(maxForkSegments-1)+1; j++){
							// Forks set their centreline to the x/y coordinates of the vertex they originate from
							double px4 = px3 + (random.nextDouble()*2-1)*vertexDither;
							double py4 = py3 + (random.nextDouble()*2-1)*vertexDither;
							double pz4 = pz3 + minSegmentLength + random.nextDouble()*(maxSegmentLength - minSegmentLength);

							drawSegment(tessellator, layer, px3, py3, pz3, px4, py4, pz4, thickness*0.8f);

							// Forks of forks
							if(random.nextFloat() < forkChance){

								double px5 = px3 + (random.nextDouble()*2-1)*vertexDither;
								double py5 = py3 + (random.nextDouble()*2-1)*vertexDither;
								double pz5 = pz3 + minSegmentLength + random.nextDouble()*(maxSegmentLength - minSegmentLength);

								drawSegment(tessellator, layer, px3, py3, pz3, px5, py5, pz5, thickness*0.6f);
							}

							px3 = px4;
							py3 = py4;
							pz3 = pz4;
						}
					}

					px = px2;
					py = py2;
					pz = pz2;
				}

				// Last segment has a specific end position and cannot fork.
				double px2 = freeEnd ? (random.nextDouble()*2-1)*vertexDither : 0;
				double py2 = freeEnd ? (random.nextDouble()*2-1)*vertexDither : 0;
				drawSegment(tessellator, layer, px, py, pz, px2, py2, arcLength, thickness);

			}
		}

		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	/** Draws the given layer of a segment of the arc, from the point (x1, y1, z1) to the point (x2, y2, z2), with the given thickness. */
	private void drawSegment(Tessellator tessellator, int layer, double x1, double y1, double z1, double x2, double y2, double z2, float thickness){

		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		
		switch(layer){
		case 0:
			// TODO: Extract these colours as constants
			drawShearedBox(buffer, x1, y1, z1, x2, y2, z2, 0.25f*thickness, 255, 255, 255, 255);
			break;

		case 1:
			drawShearedBox(buffer, x1, y1, z1, x2, y2, z2, 0.6f*thickness, 200, 255, 255, 165);
			break;

		case 2:
			drawShearedBox(buffer, x1, y1, z1, x2, y2, z2, thickness, 50, 150, 255, 75);
			break;
		}
		
		tessellator.draw();
	}
	
	/** Draws a single box for one segment of the arc, from the point (x1, y1, z1) to the point (x2, y2, z2), with given width and colour. */
	private void drawShearedBox(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, float width, int r, int g, int b, int a){
		
		buffer.pos(x1-width, y1-width, z1).color(r, g, b, a).endVertex();
		buffer.pos(x2-width, y2-width, z2).color(r, g, b, a).endVertex();
		buffer.pos(x1-width, y1+width, z1).color(r, g, b, a).endVertex();
		buffer.pos(x2-width, y2+width, z2).color(r, g, b, a).endVertex();
		buffer.pos(x1+width, y1+width, z1).color(r, g, b, a).endVertex();
		buffer.pos(x2+width, y2+width, z2).color(r, g, b, a).endVertex();
		buffer.pos(x1+width, y1-width, z1).color(r, g, b, a).endVertex();
		buffer.pos(x2+width, y2-width, z2).color(r, g, b, a).endVertex();
		buffer.pos(x1-width, y1-width, z1).color(r, g, b, a).endVertex();
		buffer.pos(x2-width, y2-width, z2).color(r, g, b, a).endVertex();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityArc entity){
		return null;
	}

}