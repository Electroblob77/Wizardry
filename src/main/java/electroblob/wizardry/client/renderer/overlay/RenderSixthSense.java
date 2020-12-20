package electroblob.wizardry.client.renderer.overlay;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.WizardryClientEventHandler;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@EventBusSubscriber(Side.CLIENT)
public class RenderSixthSense {

	private static final ResourceLocation SCREEN_OVERLAY_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_overlay.png");

	private static final ResourceLocation PASSIVE_MOB_MARKER_TEXTURE = 	new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_marker_passive.png");
	private static final ResourceLocation HOSTILE_MOB_MARKER_TEXTURE = 	new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_marker_hostile.png");
	private static final ResourceLocation PLAYER_MARKER_TEXTURE = 		new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_marker_player.png");

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event){

		if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET){

			if(Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.sixth_sense)){

				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableAlpha();

				WizardryClientEventHandler.renderScreenOverlay(event.getResolution(), SCREEN_OVERLAY_TEXTURE);

				GlStateManager.enableAlpha();
				GlStateManager.color(1, 1, 1, 1);
			}
		}
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<EntityLivingBase> event){

		Minecraft mc = Minecraft.getMinecraft();
		RenderManager renderManager = event.getRenderer().getRenderManager();

		if(mc.player.isPotionActive(WizardryPotions.sixth_sense) && !(event.getEntity() instanceof EntityArmorStand)
				&& event.getEntity() != mc.player && mc.player.getActivePotionEffect(WizardryPotions.sixth_sense) != null
				&& event.getEntity().getDistance(mc.player) < Spells.sixth_sense.getProperty(Spell.EFFECT_RADIUS).floatValue()
				* (1 + mc.player.getActivePotionEffect(WizardryPotions.sixth_sense).getAmplifier() * Constants.RANGE_INCREASE_PER_LEVEL)){

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			// Disabling depth test allows it to be seen through everything.
			GlStateManager.disableDepth();

			GlStateManager.translate(event.getX(), event.getY() + event.getEntity().height * 0.6, event.getZ());

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? renderManager.playerViewX : -renderManager.playerViewX;
			GlStateManager.rotate(180 - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

			GlStateManager.color(1, 1, 1, 1);

			ResourceLocation texture = PASSIVE_MOB_MARKER_TEXTURE;

			if(ItemArtefact.isArtefactActive(mc.player, WizardryItems.charm_sixth_sense)){
				if(event.getEntity() instanceof IMob) texture = HOSTILE_MOB_MARKER_TEXTURE;
				else if(event.getEntity() instanceof EntityPlayer) texture = PLAYER_MARKER_TEXTURE;
			}

			mc.renderEngine.bindTexture(texture);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(-0.6, 0.6, 0).tex(0, 0).endVertex();
			buffer.pos(0.6, 0.6, 0).tex(1, 0).endVertex();
			buffer.pos(0.6, -0.6, 0).tex(1, 1).endVertex();
			buffer.pos(-0.6, -0.6, 0).tex(0, 1).endVertex();

			tessellator.draw();

			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();

			GlStateManager.popMatrix();
		}

	}

}
