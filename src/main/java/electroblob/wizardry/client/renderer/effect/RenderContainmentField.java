package electroblob.wizardry.client.renderer.effect;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.potion.PotionContainment;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.GeometryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(Side.CLIENT)
public class RenderContainmentField {

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[8];

	private static final float ANIMATION_SPEED = 0.004f;
	private static final float FADE_DISTANCE_SQUARED = 15;

	static {
		for(int i = 0; i< TEXTURES.length; i++){
			TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/environment/containment_field_" + i + ".png");
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(RenderWorldLastEvent event){

		EntityPlayer player = Minecraft.getMinecraft().player;

		if(player.isPotionActive(WizardryPotions.containment)){

			Vec3d centre = GeometryUtils.getCentre(NBTUtil.getPosFromTag(player.getEntityData().getCompoundTag(PotionContainment.ENTITY_TAG)));
			float r = PotionContainment.getContainmentDistance(player.getActivePotionEffect(WizardryPotions.containment).getAmplifier());

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.shadeModel(GL11.GL_SMOOTH); // Allows the vertex colours to produce a gradient
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			// Enables tiling (Also used for guardian beam, beacon beam and ender crystal beam)
			GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
			GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

			// Translate the texture in the 2D space (like the creeper charge layer)
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.loadIdentity();
			float distance = (player.ticksExisted + event.getPartialTicks()) * ANIMATION_SPEED;
			GlStateManager.translate(distance, distance, 0);
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);

			GlStateManager.color(1, 1, 1, 1);

			Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURES[(player.ticksExisted % (TEXTURES.length * 2))/2]);

			Tessellator tessellator = Tessellator.getInstance();

			// The aim here is to have only the bits of the containment field near the player be visible (if any).
			// There's probably a really neat, advanced way of doing this with OpenGL (stencil buffer...?) but in the
			// absence of OpenGL skills, I've devised a cheat which kinda achieves the effect we want:

			// - Split each face of the cube into four, where the split point is taken orthogonal to the player (the
			//   player is taken as (0, 0, 0) for simplicity, unlike most render classes where the entire thing is
			//   translated first)
			// - Then have the alpha value for each vertex change based on its distance from the player
			// - N.B. OpenGL renders quads as two triangles, with the 1st and 3rd vertices forming the line between them.
			//   This causes weirdness in the interpolation since the interpolation isn't taking the whole square into
			//   account. The order of the vertices below ensures that the seams between the triangles form an X shape
			//   on each of the faces, meaning that you always see a square shape appear rather than a random polygon.

			Vec3d relative = centre.subtract(player.getPositionEyes(event.getPartialTicks())).add(0, player.getEyeHeight(), 0);

			double x1 = relative.x - r;
			double y1 = relative.y - r;
			double z1 = relative.z - r;
			double x2 = relative.x + r;
			double y2 = relative.y + r;
			double z2 = relative.z + r;

			float alpha = Math.min(1, player.getActivePotionEffect(WizardryPotions.containment).getDuration() / 40f);

			BufferBuilder buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			// The following code is mostly an incomprehensible mess of numbers, but unfortunately without a lot of
			// fancy vector stuff it has to be like this, and besides, this is faster!

			// Bottom
			drawVertex(buffer, x1, y1, z1, 0, 0, alpha);
			drawVertex(buffer, x1, y1, 0, 0, -(float)z1, alpha);
			drawVertex(buffer, 0, y1, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, 0, y1, z1, -(float)x1, 0, alpha);

			drawVertex(buffer, 0, y1, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, x2, y1, 0, 2*r, -(float)z1, alpha);
			drawVertex(buffer, x2, y1, z1, 2*r, 0, alpha);
			drawVertex(buffer, 0, y1, z1, -(float)x1, 0, alpha);

			drawVertex(buffer, x1, y1, z2, 0, 2*r, alpha);
			drawVertex(buffer, 0, y1, z2, -(float)x1, 2*r, alpha);
			drawVertex(buffer, 0, y1, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, x1, y1, 0, 0, -(float)z1, alpha);

			drawVertex(buffer, 0, y1, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, 0, y1, z2, -(float)x1, 2*r, alpha);
			drawVertex(buffer, x2, y1, z2, 2*r, 2*r, alpha);
			drawVertex(buffer, x2, y1, 0, 2*r, -(float)z1, alpha);

			// Top
			drawVertex(buffer, x1, y2, z1, 0, 0, alpha);
			drawVertex(buffer, 0, y2, z1, -(float)x1, 0, alpha);
			drawVertex(buffer, 0, y2, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, x1, y2, 0, 0, -(float)z1, alpha);

			drawVertex(buffer, x2, y2, z1, 2*r, 0, alpha);
			drawVertex(buffer, x2, y2, 0, 2*r, -(float)z1, alpha);
			drawVertex(buffer, 0, y2, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, 0, y2, z1, -(float)x1, 0, alpha);

			drawVertex(buffer, 0, y2, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, 0, y2, z2, -(float)x1, 2*r, alpha);
			drawVertex(buffer, x1, y2, z2, 0, 2*r, alpha);
			drawVertex(buffer, x1, y2, 0, 0, -(float)z1, alpha);

			drawVertex(buffer, 0, y2, 0, -(float)x1, -(float)z1, alpha);
			drawVertex(buffer, x2, y2, 0, 2*r, -(float)z1, alpha);
			drawVertex(buffer, x2, y2, z2, 2*r, 2*r, alpha);
			drawVertex(buffer, 0, y2, z2, -(float)x1, 2*r, alpha);

			// North
			drawVertex(buffer, x1, y1, z1, 0, 0, alpha);
			drawVertex(buffer, 0, y1, z1, -(float)x1, 0, alpha);
			drawVertex(buffer, 0, 0, z1, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, x1, 0, z1, 0, -(float)y1, alpha);

			drawVertex(buffer, x2, y1, z1, 2*r, 0, alpha);
			drawVertex(buffer, x2, 0, z1, 2*r, -(float)y1, alpha);
			drawVertex(buffer, 0, 0, z1, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, 0, y1, z1, -(float)x1, 0, alpha);

			drawVertex(buffer, 0, 0, z1, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, 0, y2, z1, -(float)x1, 2*r, alpha);
			drawVertex(buffer, x1, y2, z1, 0, 2*r, alpha);
			drawVertex(buffer, x1, 0, z1, 0, -(float)y1, alpha);

			drawVertex(buffer, 0, 0, z1, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, x2, 0, z1, 2*r, -(float)y1, alpha);
			drawVertex(buffer, x2, y2, z1, 2*r, 2*r, alpha);
			drawVertex(buffer, 0, y2, z1, -(float)x1, 2*r, alpha);

			// South
			drawVertex(buffer, x1, y1, z2, 0, 0, alpha);
			drawVertex(buffer, x1, 0, z2, 0, -(float)y1, alpha);
			drawVertex(buffer, 0, 0, z2, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, 0, y1, z2, -(float)x1, 0, alpha);

			drawVertex(buffer, 0, 0, z2, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, x2, 0, z2, 2*r, -(float)y1, alpha);
			drawVertex(buffer, x2, y1, z2, 2*r, 0, alpha);
			drawVertex(buffer, 0, y1, z2, -(float)x1, 0, alpha);

			drawVertex(buffer, x1, y2, z2, 0, 2*r, alpha);
			drawVertex(buffer, 0, y2, z2, -(float)x1, 2*r, alpha);
			drawVertex(buffer, 0, 0, z2, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, x1, 0, z2, 0, -(float)y1, alpha);

			drawVertex(buffer, 0, 0, z2, -(float)x1, -(float)y1, alpha);
			drawVertex(buffer, 0, y2, z2, -(float)x1, 2*r, alpha);
			drawVertex(buffer, x2, y2, z2, 2*r, 2*r, alpha);
			drawVertex(buffer, x2, 0, z2, 2*r, -(float)y1, alpha);

			// West
			drawVertex(buffer, x1, y1, z1, 0, 0, alpha);
			drawVertex(buffer, x1, 0, z1, 0, -(float)y1, alpha);
			drawVertex(buffer, x1, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x1, y1, 0, -(float)z1, 0, alpha);

			drawVertex(buffer, x1, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x1, 0, z2, 2*r, -(float)y1, alpha);
			drawVertex(buffer, x1, y1, z2, 2*r, 0, alpha);
			drawVertex(buffer, x1, y1, 0, -(float)z1, 0, alpha);

			drawVertex(buffer, x1, y2, z1, 0, 2*r, alpha);
			drawVertex(buffer, x1, y2, 0, -(float)z1, 2*r, alpha);
			drawVertex(buffer, x1, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x1, 0, z1, 0, -(float)y1, alpha);

			drawVertex(buffer, x1, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x1, y2, 0, -(float)z1, 2*r, alpha);
			drawVertex(buffer, x1, y2, z2, 2*r, 2*r, alpha);
			drawVertex(buffer, x1, 0, z2, 2*r, -(float)y1, alpha);

			// East
			drawVertex(buffer, x2, y1, z1, 0, 0, alpha);
			drawVertex(buffer, x2, y1, 0, -(float)z1, 0, alpha);
			drawVertex(buffer, x2, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x2, 0, z1, 0, -(float)y1, alpha);

			drawVertex(buffer, x2, y1, z2, 2*r, 0, alpha);
			drawVertex(buffer, x2, 0, z2, 2*r, -(float)y1, alpha);
			drawVertex(buffer, x2, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x2, y1, 0, -(float)z1, 0, alpha);

			drawVertex(buffer, x2, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x2, y2, 0, -(float)z1, 2*r, alpha);
			drawVertex(buffer, x2, y2, z1, 0, 2*r, alpha);
			drawVertex(buffer, x2, 0, z1, 0, -(float)y1, alpha);

			drawVertex(buffer, x2, 0, 0, -(float)z1, -(float)y1, alpha);
			drawVertex(buffer, x2, 0, z2, 2*r, -(float)y1, alpha);
			drawVertex(buffer, x2, y2, z2, 2*r, 2*r, alpha);
			drawVertex(buffer, x2, y2, 0, -(float)z1, 2*r, alpha);

			tessellator.draw();

			// Undoes the texture translation
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.loadIdentity();
			GlStateManager.matrixMode(GL11.GL_MODELVIEW);

			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
		}
	}

	private static void drawVertex(BufferBuilder buffer, double x, double y, double z, float u, float v, float alpha){
		float f = MathHelper.clamp(1 - (float)(x*x + y*y + z*z) / FADE_DISTANCE_SQUARED, 0, 1);
		buffer.pos(x, y, z).tex(u, v).color(1, 1, 1, f * alpha).endVertex();
	}

}
