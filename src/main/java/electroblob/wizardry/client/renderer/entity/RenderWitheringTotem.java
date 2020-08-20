package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.entity.construct.EntityWitheringTotem;
import electroblob.wizardry.util.GeometryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderWitheringTotem extends Render<EntityWitheringTotem> {

	private static final ResourceLocation FLARE_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/totem/flare.png");
	private static final ResourceLocation[] CUBE_TEXTURES = new ResourceLocation[14];

	static {
		for(int i = 0; i< CUBE_TEXTURES.length; i++) CUBE_TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/totem/cube_" + i + ".png");
	}

	public RenderWitheringTotem(RenderManager manager){
		super(manager);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityWitheringTotem entity){
		return CUBE_TEXTURES[entity.ticksExisted % CUBE_TEXTURES.length];
	}

	@Override
	public void doRender(EntityWitheringTotem entity, double x, double y, double z, float entityYaw, float partialTicks){

		Tessellator tessellator = Tessellator.getInstance();

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.depthMask(false);
		GlStateManager.disableCull();

		GlStateManager.translate(x, y + entity.height/2, z);

		float charge = entity.getHealthDrained() / 50f;

		float s = DrawingUtils.smoothScaleFactor(entity.lifetime, entity.ticksExisted, partialTicks, 10, 10);
		s *= 1 + charge * 0.3f; // Gets bigger the more health it drains
		GlStateManager.scale(s, s, s);

		drawFlare(charge, tessellator);
		drawCube(entity, tessellator, partialTicks);

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	private void drawFlare(float redness, Tessellator tessellator){

		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.pushMatrix();

		int c = DrawingUtils.mix(0xb333e6, 0xff0044, redness);
		int r = c >> 16 & 255;
		int g = c >> 8 & 255;
		int b = c & 255;

		// Makes the colour add to the colour of the texture pixels, rather than the default multiplying
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);
		GlStateManager.color(r/255f, g/255f, b/255f); // Gets redder the more health it drains
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		bindTexture(FLARE_TEXTURE);

		// This counteracts the reverse rotation behaviour when in front f5 view.
		// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
		float yaw = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2
				? Minecraft.getMinecraft().getRenderManager().playerViewX
				: -Minecraft.getMinecraft().getRenderManager().playerViewX;
		GlStateManager.rotate(180.0F - Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		float radius = 0.5f;

		buffer.pos(-radius,  radius, 0).tex(0, 0).endVertex();
		buffer.pos( radius,  radius, 0).tex(1, 0).endVertex();
		buffer.pos( radius, -radius, 0).tex(1, 1).endVertex();
		buffer.pos(-radius, -radius, 0).tex(0, 1).endVertex();

		tessellator.draw();

		// Reverses the colour addition change
		GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.popMatrix();
	}

	private void drawCube(EntityWitheringTotem entity, Tessellator tessellator, float partialTicks){

		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.pushMatrix();

		float age = entity.ticksExisted + partialTicks;
		float rotationSpeed = 2;

		GlStateManager.rotate(age * rotationSpeed/2, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
		GlStateManager.rotate(age * rotationSpeed, 0.0F, 1.0F, 0.0F);

		GlStateManager.scale(0.5, 0.5, 0.5);

		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		bindEntityTexture(entity);

		Vec3d[] vertices = GeometryUtils.getVertices(entity.getEntityBoundingBox().offset(entity.getPositionVector()
				.add(0, entity.height/2, 0).scale(-1)));

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		// Outside
		drawFace(buffer, vertices[0], vertices[1], vertices[3], vertices[2], 0.5f, 0, 0.75f, 0.5f); // Bottom
		drawFace(buffer, vertices[6], vertices[7], vertices[2], vertices[3], 0.75f, 0.5f, 1,    1); // South
		drawFace(buffer, vertices[5], vertices[6], vertices[1], vertices[2], 0, 0.5f, 0.25f,    1); // East
		drawFace(buffer, vertices[4], vertices[5], vertices[0], vertices[1], 0.25f, 0.5f, 0.5f, 1); // North
		drawFace(buffer, vertices[7], vertices[4], vertices[3], vertices[0], 0.5f, 0.5f, 0.75f, 1); // West
		drawFace(buffer, vertices[5], vertices[4], vertices[6], vertices[7], 0.25f, 0, 0.5f, 0.5f); // Top

		tessellator.draw();

		GlStateManager.popMatrix();
	}

	private static void drawFace(BufferBuilder buffer, Vec3d topLeft, Vec3d topRight, Vec3d bottomLeft, Vec3d bottomRight, float u1, float v1, float u2, float v2){
		buffer.pos(topLeft.x, topLeft.y, topLeft.z).tex(u1, v1).endVertex();
		buffer.pos(topRight.x, topRight.y, topRight.z).tex(u2, v1).endVertex();
		buffer.pos(bottomRight.x, bottomRight.y, bottomRight.z).tex(u2, v2).endVertex();
		buffer.pos(bottomLeft.x, bottomLeft.y, bottomLeft.z).tex(u1, v2).endVertex();
	}

}
