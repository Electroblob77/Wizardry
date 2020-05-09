package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticleVine extends ParticleTargeted {

	/** Half the width of the vine. */
	private static final float THICKNESS = 0.02f;
	private static final float LEAF_SPACING = 0.5f;
	private static final float SEGMENT_LENGTH = 1;

	private static final ResourceLocation STEM_TEXTURE = new ResourceLocation(Wizardry.MODID, "particle/vine");
	private static final ResourceLocation[] LEAF_TEXTURES = generateTextures("vine_leaf", 5);

	public ParticleVine(World world, double x, double y, double z){
		super(world, x, y, z, STEM_TEXTURE);
		//this.setRBGColorF(1, 1, 1);
		this.setMaxAge(0);
		this.particleScale = 1;
		this.setRBGColorF(0.2f, 0.65f, 0f);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ){
		// When using FX layer 1 the BufferBuilder is already drawing... but in the wrong mode :/
		Tessellator.getInstance().draw();
		super.renderParticle(buffer, viewer, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

	@Override
	protected boolean shouldApplyOriginOffset(){
		return false;
	}

	@Override
	protected void draw(Tessellator tessellator, double length, float partialTicks){

		random.setSeed(seed); // Reset the random so we get the same sequence of numbers each frame
		
		float scale = this.particleScale;

		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		// Hmmmm we can't get the texture to tile using OpenGL texture space because it's on a sprite sheet...
		// Solution: Draw loads of boxes. Simple!
		// (Since there aren't going to be that many of these particles around we could have not used sprite sheets
		// and done the OpenGL texture space thing, but this is kinda easier)
		// Everything is drawn back-to-front so it looks like the vine is growing from the origin, not the endpoint
		int i = 0;
		while(i + SEGMENT_LENGTH < length){
			drawShearedBox(tessellator, 0, 0, length-i, 0, 0, length-i-SEGMENT_LENGTH, THICKNESS * scale,
					particleRed, particleGreen, particleBlue, particleAlpha);
			i += SEGMENT_LENGTH;
		}

		drawShearedBox(tessellator, 0, 0, length-i, 0, 0, 0, THICKNESS * scale,
				particleRed, particleGreen, particleBlue, particleAlpha);

		for(double l=length; l>0; l-=LEAF_SPACING){

			GlStateManager.pushMatrix();

			GlStateManager.rotate(random.nextInt(4) * 90, 0, 0, 1);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			TextureAtlasSprite leaf = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(
					LEAF_TEXTURES[random.nextInt(LEAF_TEXTURES.length)].toString());

			float w = 16 * THICKNESS * scale;
			float u1 = leaf.getMinU();
			float u2 = leaf.getMaxU();
			float v1 = leaf.getMinV();
			float v2 = leaf.getMaxV();

			float colourVariation = 0.3f;

			float r = MathHelper.clamp(particleRed + (random.nextFloat() - 0.5f) * colourVariation, 0, 1);
			float g = MathHelper.clamp(particleGreen + (random.nextFloat() - 0.5f) * colourVariation, 0, 1);
			float b = MathHelper.clamp(particleBlue + (random.nextFloat() - 0.5f) * colourVariation, 0, 1);

			buffer.pos(0, 0, l).tex(u1, v1).color(r, g, b, particleAlpha).endVertex();
			buffer.pos(w, 0, l).tex(u2, v1).color(r, g, b, particleAlpha).endVertex();
			buffer.pos(w, w, l).tex(u2, v2).color(r, g, b, particleAlpha).endVertex();
			buffer.pos(0, w, l).tex(u1, v2).color(r, g, b, particleAlpha).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();
		}

		// Makes the rain go weird
		//GlStateManager.enableLighting();
	}
	
	/** Draws a single box for one segment of the arc, from the point (x1, y1, z1) to the point (x2, y2, z2), with given width and colour. */
	private void drawShearedBox(Tessellator tessellator, double x1, double y1, double z1, double x2, double y2, double z2, float width, float r, float g, float b, float a){

		float u1 = particleTexture.getMinU();
		float u2 = u1 + (particleTexture.getMaxU() - u1) * (float)(z1-z2)/SEGMENT_LENGTH;
		float v1 = particleTexture.getMinV();
		float dv = particleTexture.getMaxV() - v1;
		// width * 8 gives the total 'circumference' of the box
		float v2 = v1 + dv * 0.0625f;
		float v3 = v1 + dv * 0.125f;
		float v4 = v1 + dv * 0.1875f;
		float v5 = v1 + dv * 0.25f;

		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

		buffer.pos(x1-width, y1-width, z1).tex(u1, v1).color(r, g, b, a).endVertex();
		buffer.pos(x2-width, y2-width, z2).tex(u2, v1).color(r, g, b, a).endVertex();
		buffer.pos(x1-width, y1+width, z1).tex(u1, v2).color(r, g, b, a).endVertex();
		buffer.pos(x2-width, y2+width, z2).tex(u2, v2).color(r, g, b, a).endVertex();
		buffer.pos(x1+width, y1+width, z1).tex(u1, v3).color(r, g, b, a).endVertex();
		buffer.pos(x2+width, y2+width, z2).tex(u2, v3).color(r, g, b, a).endVertex();
		buffer.pos(x1+width, y1-width, z1).tex(u1, v4).color(r, g, b, a).endVertex();
		buffer.pos(x2+width, y2-width, z2).tex(u2, v4).color(r, g, b, a).endVertex();
		buffer.pos(x1-width, y1-width, z1).tex(u1, v5).color(r, g, b, a).endVertex();
		buffer.pos(x2-width, y2-width, z2).tex(u2, v5).color(r, g, b, a).endVertex();

		tessellator.draw();
	}

	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){

		event.getMap().registerSprite(STEM_TEXTURE);

		for(ResourceLocation texture : LEAF_TEXTURES){
			event.getMap().registerSprite(texture);
		}
	}

}
