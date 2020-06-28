package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.entity.construct.EntityRadiantTotem;
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

public class RenderRadiantTotem extends Render<EntityRadiantTotem> {

	private static final ResourceLocation FLARE_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/radiant_totem/flare.png");
	private static final ResourceLocation[] CUBE_TEXTURES = new ResourceLocation[14];

	static {
		for(int i = 0; i< CUBE_TEXTURES.length; i++) CUBE_TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/radiant_totem/cube_" + i + ".png");
	}

	public RenderRadiantTotem(RenderManager manager){
		super(manager);
	}

	@Nullable
	@Override
	protected ResourceLocation getEntityTexture(EntityRadiantTotem entity){
		return CUBE_TEXTURES[entity.ticksExisted % CUBE_TEXTURES.length];
	}

	@Override
	public void doRender(EntityRadiantTotem entity, double x, double y, double z, float entityYaw, float partialTicks){

		Tessellator tessellator = Tessellator.getInstance();

		GlStateManager.pushMatrix();

		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.depthMask(false);
		GlStateManager.disableCull();

		GlStateManager.translate(x, y + entity.height/2, z);

		float s = DrawingUtils.smoothScaleFactor(entity.lifetime, entity.ticksExisted, partialTicks, 10, 10);
		GlStateManager.scale(s, s, s);

		drawFlare(tessellator);
		drawCube(entity, tessellator, partialTicks);

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	private void drawFlare(Tessellator tessellator){

		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.pushMatrix();

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

		float r = 0.5f;

		buffer.pos(-r,  r, 0).tex(0, 0).endVertex();
		buffer.pos( r,  r, 0).tex(1, 0).endVertex();
		buffer.pos( r, -r, 0).tex(1, 1).endVertex();
		buffer.pos(-r, -r, 0).tex(0, 1).endVertex();

		tessellator.draw();

		GlStateManager.popMatrix();
	}

	private void drawCube(EntityRadiantTotem entity, Tessellator tessellator, float partialTicks){

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
