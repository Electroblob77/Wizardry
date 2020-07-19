package electroblob.wizardry.client.renderer.effect;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockReceptacle;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.misc.DonationPerksHandler;
import electroblob.wizardry.util.GeometryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber
public class RenderDonationPerks {

	private static final ResourceLocation FLARE_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/totem/flare.png");
	private static final ResourceLocation[] CUBE_TEXTURES = new ResourceLocation[14];

	private static final float BOBBING_PERIOD = 25;
	private static final float ROTATION_PERIOD = 60;
	private static final double ROTATION_RADIUS = 0.7;
	private static final double HEIGHT_FRACTION = 0.8;
	private static final double BOBBING_DISTANCE = 0.2;
	private static final double FOLLOW_DISTANCE = 1.4;

	static {
		for(int i = 0; i< CUBE_TEXTURES.length; i++) CUBE_TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/totem/cube_" + i + ".png");
	}

	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Post event){

		EntityPlayer player = event.getEntityPlayer();
		float partialTicks = event.getPartialRenderTick();

		Element element = DonationPerksHandler.getElement(player);

		if(element != null){

			int c1 = BlockReceptacle.PARTICLE_COLOURS.get(element)[0];
			int r1 = (c1 & 0xFF0000) >> 16;
			int g1 = (c1 & 0xFF00) >> 8;
			int b1 = (c1 & 0xFF);

			int c2 = BlockReceptacle.PARTICLE_COLOURS.get(element)[1];
			int r2 = (c2 & 0xFF0000) >> 16;
			int g2 = (c2 & 0xFF00) >> 8;
			int b2 = (c2 & 0xFF);

			Tessellator tessellator = Tessellator.getInstance();

			GlStateManager.pushMatrix();

			GlStateManager.enableBlend();
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GlStateManager.depthMask(false);
			GlStateManager.disableCull();

			double x1 = player.chasingPosX - player.posX;
			double y1 = player.chasingPosY - player.posY;
			double z1 = player.chasingPosZ - player.posZ;

			double x2 = player.prevChasingPosX - player.prevPosX;
			double y2 = player.prevChasingPosY - player.prevPosY;
			double z2 = player.prevChasingPosZ - player.prevPosZ;

			double dx = x2 + (x1 - x2) * partialTicks;
			double dy = y2 + (y1 - y2) * partialTicks;
			double dz = z2 + (z1 - z2) * partialTicks;

			float t = player.ticksExisted + partialTicks;
			float hMoveFraction = MathHelper.clamp((float)(dx * dx + dz * dz) / 0.3f, 0, 1);
			float vMoveFraction = MathHelper.clamp((float)(dy * dy) / 0.3f, 0, 1);

			double x = event.getX() + ROTATION_RADIUS * MathHelper.sin(t / ROTATION_PERIOD) * (1 - hMoveFraction) + FOLLOW_DISTANCE * dx;
			double y = event.getY() + HEIGHT_FRACTION * player.height + BOBBING_DISTANCE * MathHelper.sin(t / BOBBING_PERIOD) * (1 - vMoveFraction) + FOLLOW_DISTANCE * dy;
			double z = event.getZ() + ROTATION_RADIUS * MathHelper.cos(t / ROTATION_PERIOD) * (1 - hMoveFraction) + FOLLOW_DISTANCE * dz;
			GlStateManager.translate(x, y, z);

			GlStateManager.scale(0.4, 0.4, 0.4);

			// Makes the colour add to the colour of the texture pixels, rather than the default multiplying
			GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_ADD);
			GlStateManager.color(r1/255f, g1/255f, b1/255f);
			drawFlare(tessellator);

			// Reverses the colour addition change
			GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE);
			GlStateManager.color(r2/255f, g2/255f, b2/255f);
			drawCube(player, tessellator, partialTicks);

			GlStateManager.color(1, 1, 1,1);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.depthMask(true);
			GlStateManager.enableCull();

			GlStateManager.popMatrix();
		}

	}

	private static void drawFlare(Tessellator tessellator){

		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.pushMatrix();

		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		Minecraft.getMinecraft().renderEngine.bindTexture(FLARE_TEXTURE);

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

	private static void drawCube(EntityPlayer player, Tessellator tessellator, float partialTicks){

		BufferBuilder buffer = tessellator.getBuffer();

		GlStateManager.pushMatrix();

		float age = player.ticksExisted + partialTicks;
		float rotationSpeed = 2;

		GlStateManager.rotate(age * rotationSpeed/2, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
		GlStateManager.rotate(age * rotationSpeed, 0.0F, 1.0F, 0.0F);

		GlStateManager.scale(0.5, 0.5, 0.5);

		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

		Minecraft.getMinecraft().renderEngine.bindTexture(CUBE_TEXTURES[player.ticksExisted % CUBE_TEXTURES.length]);

		Vec3d[] vertices = GeometryUtils.getVertices(new AxisAlignedBB(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5));

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
