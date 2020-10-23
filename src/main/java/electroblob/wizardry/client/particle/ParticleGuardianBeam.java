package electroblob.wizardry.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class ParticleGuardianBeam extends ParticleTargeted {

	/** Half the width of the beam. */
	private static final float THICKNESS = 0.15f;

	private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft:textures/entity/guardian_beam.png");

	public ParticleGuardianBeam(World world, double x, double y, double z){
		super(world, x, y, z); // Does't use TextureAtlasSprite
		this.setRBGColorF(1, 1, 1);
		this.setMaxAge(3);
		this.particleScale = 1;
	}

	@Override
	public int getFXLayer(){
		return 3;
	}

	@Override
	protected void draw(Tessellator tessellator, double length, float partialTicks){

		float scale = this.particleScale;

		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		GlStateManager.pushMatrix();
		// Enables texture tiling (Also used for guardian beam, beacon beam and ender crystal beam, amongst others)
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		GlStateManager.rotate(Minecraft.getMinecraft().player.ticksExisted + partialTicks, 0, 0, 1);

		Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		float t = THICKNESS * scale;
		float v1 = 2 * (particleAge + partialTicks) / particleMaxAge;
		float v2 = v1 + (float)length * 2; // Multiply by 2 to make it squished like it is in vanilla

		buffer.pos(-t, 0, 0)   .tex(0,   v1)  .color(particleRed, particleGreen, particleBlue, 1).endVertex();
		buffer.pos( t, 0, 0)   .tex(0.5, v1)  .color(particleRed, particleGreen, particleBlue, 1).endVertex();
		buffer.pos( t, 0, length).tex(0.5, v2).color(particleRed, particleGreen, particleBlue, 1).endVertex();
		buffer.pos(-t, 0, length).tex(0,   v2).color(particleRed, particleGreen, particleBlue, 1).endVertex();

		buffer.pos(0, -t, 0)   .tex(0,   v1)  .color(particleRed, particleGreen, particleBlue, 1).endVertex();
		buffer.pos(0,  t, 0)   .tex(0.5, v1)  .color(particleRed, particleGreen, particleBlue, 1).endVertex();
		buffer.pos(0,  t, length).tex(0.5, v2).color(particleRed, particleGreen, particleBlue, 1).endVertex();
		buffer.pos(0, -t, length).tex(0,   v2).color(particleRed, particleGreen, particleBlue, 1).endVertex();

		tessellator.draw();

//		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
//		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		GlStateManager.popMatrix();

		// Makes the rain go weird
		//GlStateManager.enableLighting();
	}

}
