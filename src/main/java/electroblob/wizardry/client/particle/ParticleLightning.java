package electroblob.wizardry.client.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;

public class ParticleLightning extends ParticleTargeted {
	
	/** Half the width of the outermost layer. */
	private static final float THICKNESS = 0.04f;
	/** Maximum length of a segment. */
	private static final double MAX_SEGMENT_LENGTH = 0.6;
	/** Minimum length of a segment. */
	private static final double MIN_SEGMENT_LENGTH = 0.2;
	/** Maximum deviation (in x or y, as drawn before transformations) from the centreline. */
	private static final double VERTEX_JITTER = 0.15;
	/** Maximum number of segments a fork can have before ending. */
	private static final int MAX_FORK_SEGMENTS = 3;
	/** Probability (as a fraction) that a vertex will have a fork. */
	private static final float FORK_CHANCE = 0.3f;
	/** Number of ticks to wait before the arc changes shape again. */
	private static final int UPDATE_PERIOD = 1;

	/** A random long value used by the renderer as a seed to generate its vertices from, ensuring they remain the same
	 * across multiple frames. Not synced. */
	public final long seed;
	
	public ParticleLightning(World world, double x, double y, double z){
		super(world, x, y, z); // Does not have a texture!
		seed = this.rand.nextLong();
		this.setRBGColorF(0.2f, 0.6f, 1); // Default blue colour
		this.setMaxAge(3);
		this.particleScale = 1;
	}
	
	@Override
	public int getFXLayer(){
		return 3;
	}
	
	@Override
	protected void draw(Tessellator tessellator, double length, float partialTicks){
		
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		// The direction of the arc drawn by the tessellator is always along the z axis and is rotated to the
		// correct orientation, that way there isn't a ton of trigonometry and the code is way neater.

		boolean freeEnd = this.target == null;
		
		int numberOfSegments = (int)Math.round(length/MAX_SEGMENT_LENGTH); // Number of segments

		for(int layer=0; layer<3; layer++){

			double px=0, py=0, pz=0;
			// Creates a random from the arc's seed field + the number of ticks it has existed/the update period.
			// By using a seed, we can ensure the vertex positions and forks are identical a) for each layer, even
			// though they are rendered sequentially, and b) across many frames (and ticks, if updateTime > 1).
			Random random = new Random(this.seed + this.particleAge/UPDATE_PERIOD);

			// numberOfSegments-1 because the last segment is handled separately.
			for(int i=0; i<numberOfSegments-1; i++){

				double px2 = (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale;
				double py2 = (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale;
				double pz2 = pz + length/(double)numberOfSegments; // For now they are all the same length

				drawSegment(tessellator, layer, px, py, pz, px2, py2, pz2, THICKNESS*particleScale);

				// Forks
				if(random.nextFloat() < FORK_CHANCE){

					double px3=px, py3=py, pz3=pz;

					for(int j=0; j<random.nextInt(MAX_FORK_SEGMENTS-1)+1; j++){
						// Forks set their centreline to the x/y coordinates of the vertex they originate from
						double px4 = px3 + (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale;
						double py4 = py3 + (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale;
						double pz4 = pz3 + MIN_SEGMENT_LENGTH + random.nextDouble()*(MAX_SEGMENT_LENGTH - MIN_SEGMENT_LENGTH);

						drawSegment(tessellator, layer, px3, py3, pz3, px4, py4, pz4, THICKNESS*0.8f*particleScale);

						// Forks of forks
						if(random.nextFloat() < FORK_CHANCE){

							double px5 = px3 + (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale;
							double py5 = py3 + (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale;
							double pz5 = pz3 + MIN_SEGMENT_LENGTH + random.nextDouble()*(MAX_SEGMENT_LENGTH - MIN_SEGMENT_LENGTH);

							drawSegment(tessellator, layer, px3, py3, pz3, px5, py5, pz5, THICKNESS*0.6f*particleScale);
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
			double px2 = freeEnd ? (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale : 0;
			double py2 = freeEnd ? (random.nextDouble()*2-1)*VERTEX_JITTER*particleScale : 0;
			drawSegment(tessellator, layer, px, py, pz, px2, py2, length, THICKNESS*particleScale);

		}

		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
	}
	
	/** Draws the given layer of a segment of the arc, from the point (x1, y1, z1) to the point (x2, y2, z2), with the given thickness. */
	private void drawSegment(Tessellator tessellator, int layer, double x1, double y1, double z1, double x2, double y2, double z2, float thickness){

		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
		
		switch(layer){
		
		case 0:
			drawShearedBox(buffer, x1, y1, z1, x2, y2, z2, 0.25f*thickness, 1, 1, 1, 1);
			break;

		case 1:
			drawShearedBox(buffer, x1, y1, z1, x2, y2, z2, 0.6f*thickness, (particleRed + 1)/2, (particleGreen + 1)/2,
					(particleBlue + 1)/2, 0.65f);
			break;

		case 2:
			drawShearedBox(buffer, x1, y1, z1, x2, y2, z2, thickness, particleRed, particleGreen, particleBlue, 0.3f);
			break;
		}
		
		tessellator.draw();
	}
	
	/** Draws a single box for one segment of the arc, from the point (x1, y1, z1) to the point (x2, y2, z2), with given width and colour. */
	private void drawShearedBox(BufferBuilder buffer, double x1, double y1, double z1, double x2, double y2, double z2, float width, float r, float g, float b, float a){
		
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

}
