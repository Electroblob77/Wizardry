package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityZombieSpawner;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class RenderZombieSpawner extends Render<EntityZombieSpawner> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/zombie_spawner.png");

	private static final Vec3d[] HIDDEN_BOX = WizardryUtilities.getVertices(new AxisAlignedBB(-1, 0, -1, 1, 2.1, 1));

	public RenderZombieSpawner(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityZombieSpawner entity, double x, double y, double z, float yaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GlStateManager.translate(x, y, z);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		// The visible bit
		GlStateManager.pushMatrix();

		int animationTicks = 10;
		float age = entity.ticksExisted + partialTicks;
		float s = age < animationTicks ? age/animationTicks : MathHelper.clamp((entity.lifetime - age) / animationTicks, 0, 1);
		s = (float)Math.pow(s, 0.4); // Smooths the animation

		GlStateManager.scale(s, s, s);
		GlStateManager.rotate(age, 0, 1, 0);

		this.bindTexture(TEXTURE);

		Vec3d[] vertices = WizardryUtilities.getVertices(entity.getEntityBoundingBox().offset(entity.getPositionVector().scale(-1)));

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		drawFace(buffer, vertices[0], vertices[1], vertices[3], vertices[2], 0, 0, 1, 1); // Top
		drawFace(buffer, vertices[1], vertices[0], vertices[2], vertices[3], 0, 0, 1, 1); // Bottom

		tessellator.draw();

		GlStateManager.popMatrix();

		// Hidden box
		GlStateManager.disableTexture2D();
		GlStateManager.colorMask(false, false, false, false); // Magically hide the zombies

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

		// Outside (hides everything)
		drawFaceColour(buffer, HIDDEN_BOX[6], HIDDEN_BOX[7], HIDDEN_BOX[2], HIDDEN_BOX[3], 0, 0, 0, 1); // South
		drawFaceColour(buffer, HIDDEN_BOX[5], HIDDEN_BOX[6], HIDDEN_BOX[1], HIDDEN_BOX[2], 0, 0, 0, 1); // East
		drawFaceColour(buffer, HIDDEN_BOX[4], HIDDEN_BOX[5], HIDDEN_BOX[0], HIDDEN_BOX[1], 0, 0, 0, 1); // North
		drawFaceColour(buffer, HIDDEN_BOX[7], HIDDEN_BOX[4], HIDDEN_BOX[3], HIDDEN_BOX[0], 0, 0, 0, 1); // West
		drawFaceColour(buffer, HIDDEN_BOX[5], HIDDEN_BOX[4], HIDDEN_BOX[6], HIDDEN_BOX[7], 0, 0, 0, 1); // Top

		tessellator.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableTexture2D();
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.popMatrix();
	}

	@Override
	public void renderMultipass(EntityZombieSpawner entity, double x, double y, double z, float yaw, float partialTicks){


	}

	@Override
	public boolean isMultipass(){
		return false;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityZombieSpawner entity){
		return TEXTURE;
	}

	private static void drawFace(BufferBuilder buffer, Vec3d topLeft, Vec3d topRight, Vec3d bottomLeft, Vec3d bottomRight, float u1, float v1, float u2, float v2){
		buffer.pos(topLeft.x, topLeft.y, topLeft.z).tex(u1, v1).endVertex();
		buffer.pos(topRight.x, topRight.y, topRight.z).tex(u2, v1).endVertex();
		buffer.pos(bottomRight.x, bottomRight.y, bottomRight.z).tex(u2, v2).endVertex();
		buffer.pos(bottomLeft.x, bottomLeft.y, bottomLeft.z).tex(u1, v2).endVertex();
	}

	private static void drawFaceColour(BufferBuilder buffer, Vec3d topLeft, Vec3d topRight, Vec3d bottomLeft, Vec3d bottomRight, float r, float g, float b, float a){
		buffer.pos(topLeft.x, topLeft.y, topLeft.z).color(r, g, b, a).endVertex();
		buffer.pos(topRight.x, topRight.y, topRight.z).color(r, g, b, a).endVertex();
		buffer.pos(bottomRight.x, bottomRight.y, bottomRight.z).color(r, g, b, a).endVertex();
		buffer.pos(bottomLeft.x, bottomLeft.y, bottomLeft.z).color(r, g, b, a).endVertex();
	}

}
