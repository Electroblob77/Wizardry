package electroblob.wizardry.client;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Flight;
import electroblob.wizardry.spell.ShadowWard;
import electroblob.wizardry.spell.Shield;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Event handler responsible for all client-side only events, mostly rendering.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public final class WizardryClientEventHandler {

	private static final ResourceLocation shieldTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/shield.png");
	private static final ResourceLocation wingTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/wing.png");
	private static final ResourceLocation shadowWardTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/shadow_ward.png");
	private static final ResourceLocation sixthSenseTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/sixth_sense.png");
	private static final ResourceLocation sixthSenseOverlayTexture = new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_overlay.png");
	private static final ResourceLocation frostOverlayTexture = new ResourceLocation(Wizardry.MODID, "textures/gui/frost_overlay.png");
	private static final ResourceLocation pointerTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/pointer.png");
	private static final ResourceLocation targetPointerTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/target_pointer.png");

	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(ContainerArcaneWorkbench.EMPTY_SLOT_CRYSTAL);
		event.getMap().registerSprite(ContainerArcaneWorkbench.EMPTY_SLOT_UPGRADE);
	}

	// Shift-scrolling to change spells
	@SubscribeEvent
	public static void onMouseEvent(MouseEvent event){

		EntityPlayer player = Minecraft.getMinecraft().player;
		ItemStack wand = player.getHeldItemMainhand();

		if(!(wand.getItem() instanceof ItemWand)){
			wand = player.getHeldItemOffhand();
			// If the player isn't holding a wand, then nothing else needs to be done.
			if(!(wand.getItem() instanceof ItemWand)) return;
		}

		if(Minecraft.getMinecraft().inGameHasFocus && !wand.isEmpty() && event.getDwheel() != 0 && player.isSneaking()
				&& Wizardry.settings.enableShiftScrolling){

			event.setCanceled(true);

			if(event.getDwheel() > 0){
				// Packet building
				IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.PREVIOUS_SPELL_KEY);
				WizardryPacketHandler.net.sendToServer(msg);

			}else if(event.getDwheel() < 0){
				// Packet building
				IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.NEXT_SPELL_KEY);
				WizardryPacketHandler.net.sendToServer(msg);
			}
		}
	}

	@SubscribeEvent
	public static void onFOVUpdateEvent(FOVUpdateEvent event){

		// Bow zoom. Taken directly from AbstractClientPlayer so it works exactly like vanilla.
		if(event.getEntity().isHandActive() && event.getEntity().getActiveItemStack().getItem() instanceof ItemSpectralBow){

			int maxUseTicks = event.getEntity().getItemInUseMaxCount();

			float maxUseSeconds = (float)maxUseTicks / 20.0F;

			if(maxUseSeconds > 1.0F){
				maxUseSeconds = 1.0F;
			}else{
				maxUseSeconds = maxUseSeconds * maxUseSeconds;
			}

			event.setNewfov(event.getFov() * 1.0F - maxUseSeconds * 0.15F);
		}
	}

	// Third person
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderPlayerEvent.Post event){
		renderShieldIfActive(event.getEntityPlayer());
		renderWingsIfActive(event.getEntityPlayer(), event.getPartialRenderTick());
		renderShadowWardIfActive(event.getEntityPlayer());
	}

	// First person
	@SubscribeEvent
	public static void onRenderWorldLastEvent(RenderWorldLastEvent event){
		// Now only fires in first person.
		if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0){
			renderShieldFirstPerson(Minecraft.getMinecraft().player);
			renderShadowWardFirstPerson(Minecraft.getMinecraft().player);
		}
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<EntityLivingBase> event){

		Minecraft mc = Minecraft.getMinecraft();
		WizardData properties = WizardData.get(mc.player);
		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(mc.world, mc.player, 16);
		RenderManager renderManager = event.getRenderer().getRenderManager();

		ItemStack wand = mc.player.getHeldItemMainhand();

		if(!(wand.getItem() instanceof ItemWand)){
			wand = mc.player.getHeldItemOffhand();
		}

		// Target selection pointer
		if(mc.player.isSneaking() && wand.getItem() instanceof ItemWand && rayTrace != null && !(event.getEntity() instanceof EntityArmorStand)
				&& rayTrace.entityHit == event.getEntity() && properties != null && properties.selectedMinion != null){

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			// Disabling depth test allows it to be seen through everything.
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GlStateManager.color(1, 1, 1, 1);

			GlStateManager.translate(event.getX(), event.getY() + event.getEntity().height + 0.5, event.getZ());

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? renderManager.playerViewX : -renderManager.playerViewX;
			GlStateManager.rotate(180 - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			mc.renderEngine.bindTexture(targetPointerTexture);

			buffer.pos(-0.2, 0.24, 0).tex(0, 0).endVertex();
			buffer.pos(0.2, 0.24, 0).tex(9f / 16f, 0).endVertex();
			buffer.pos(0.2, -0.24, 0).tex(9f / 16f, 11f / 16f).endVertex();
			buffer.pos(-0.2, -0.24, 0).tex(0, 11f / 16f).endVertex();

			tessellator.draw();

			GlStateManager.enableCull();
			GlStateManager.enableLighting();
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GlStateManager.popMatrix();
		}

		// Summoned creature selection pointer
		if(properties != null && properties.selectedMinion != null && properties.selectedMinion.get() == event.getEntity()){

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			// Disabling depth test allows it to be seen through everything.
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GlStateManager.color(1, 1, 1, 1);

			GlStateManager.translate(event.getX(), event.getY() + event.getEntity().height + 0.5, event.getZ());

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? renderManager.playerViewX : -renderManager.playerViewX;
			GlStateManager.rotate(180 - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			mc.renderEngine.bindTexture(pointerTexture);

			buffer.pos(-0.2, 0.24, 0).tex(0, 0).endVertex();
			buffer.pos(0.2, 0.24, 0).tex(9f / 16f, 0).endVertex();
			buffer.pos(0.2, -0.24, 0).tex(9f / 16f, 11f / 16f).endVertex();
			buffer.pos(-0.2, -0.24, 0).tex(0, 11f / 16f).endVertex();

			tessellator.draw();

			GlStateManager.enableCull();
			GlStateManager.enableLighting();
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GlStateManager.popMatrix();
		}

		// Sixth sense
		if(mc.player.isPotionActive(WizardryPotions.sixth_sense) && !(event.getEntity() instanceof EntityArmorStand) && event.getEntity() != mc.player
				&& mc.player.getActivePotionEffect(WizardryPotions.sixth_sense) != null && event.getEntity().getDistance(mc.player) < 20
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
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			GlStateManager.translate(event.getX(), event.getY() + event.getEntity().height * 0.6, event.getZ());

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? renderManager.playerViewX : -renderManager.playerViewX;
			GlStateManager.rotate(180 - renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			mc.renderEngine.bindTexture(sixthSenseTexture);

			buffer.pos(-0.6, 0.6, 0).tex(0, 0).endVertex();
			buffer.pos(0.6, 0.6, 0).tex(1, 0).endVertex();
			buffer.pos(0.6, -0.6, 0).tex(1, 1).endVertex();
			buffer.pos(-0.6, -0.6, 0).tex(0, 1).endVertex();

			tessellator.draw();

			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GlStateManager.popMatrix();
		}
	}

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event){

		if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET
				&& Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.sixth_sense)){

			GlStateManager.pushMatrix();

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableAlpha();
			Minecraft.getMinecraft().renderEngine.bindTexture(sixthSenseOverlayTexture);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(0.0D, (double)event.getResolution().getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
			buffer.pos((double)event.getResolution().getScaledWidth(), (double)event.getResolution().getScaledHeight(), -90.0D).tex(1.0D, 1.0D)
					.endVertex();
			buffer.pos((double)event.getResolution().getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
			buffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
			tessellator.draw();

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GlStateManager.enableAlpha();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.popMatrix();
		}

		if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET && Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.frost)){

			GlStateManager.pushMatrix();

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableAlpha();
			Minecraft.getMinecraft().renderEngine.bindTexture(frostOverlayTexture);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(0.0D, (double)event.getResolution().getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
			buffer.pos((double)event.getResolution().getScaledWidth(), (double)event.getResolution().getScaledHeight(), -90.0D).tex(1.0D, 1.0D)
					.endVertex();
			buffer.pos((double)event.getResolution().getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
			buffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();

			tessellator.draw();
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GlStateManager.enableAlpha();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.popMatrix();
		}
	}

	// FIXME: Something in here is making the first person shadow ward rather translucent.
	private static void renderShadowWardFirstPerson(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getActiveItemStack();
		if(WizardData.get(entityplayer) != null && WizardData.get(entityplayer).currentlyCasting() instanceof ShadowWard
				|| (entityplayer.isHandActive() && wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand
						&& WandHelper.getCurrentSpell(wand) instanceof ShadowWard)){

			GlStateManager.pushMatrix();

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableLighting();
			GlStateManager.disableAlpha();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GlStateManager.translate(0, 1.2, 0);
			GlStateManager.rotate(-entityplayer.rotationYaw, 0, 1, 0);
			GlStateManager.rotate(entityplayer.rotationPitch, 1, 0, 0);

			Minecraft.getMinecraft().renderEngine.bindTexture(shadowWardTexture);

			GlStateManager.pushMatrix();

			GlStateManager.translate(0, 0, 1.2);
			GlStateManager.rotate(entityplayer.world.getWorldTime() * -2, 0, 0, 1);
			GlStateManager.scale(1.1, 1.1, 1.1);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
			buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();
			buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
			buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();

			tessellator.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
			buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();
			buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
			buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();

			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableLighting();
			GlStateManager.disableBlend();

			GlStateManager.popMatrix();

		}
	}

	private static void renderShadowWardIfActive(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getActiveItemStack();
		if(WizardData.get(entityplayer).currentlyCasting() instanceof ShadowWard
				|| (entityplayer.isHandActive() && wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand
						&& WandHelper.getCurrentSpell(wand) instanceof ShadowWard)){

			GlStateManager.pushMatrix();

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate(-entityplayer.renderYawOffset, 0, 1, 0);

			Minecraft.getMinecraft().renderEngine.bindTexture(shadowWardTexture);

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.translate(0, 1.2, 0);
			GlStateManager.rotate(entityplayer.world.getWorldTime() * -2, 0, 0, 1);
			GlStateManager.scale(1.1, 1.1, 1.1);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
			buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();
			buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
			buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();

			tessellator.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(-0.5, 0.5, -0.5).tex(0, 0).endVertex();
			buffer.pos(-0.5, -0.5, -0.5).tex(0, 1).endVertex();
			buffer.pos(0.5, -0.5, -0.5).tex(1, 1).endVertex();
			buffer.pos(0.5, 0.5, -0.5).tex(1, 0).endVertex();

			tessellator.draw();

			GlStateManager.enableLighting();
			GlStateManager.disableBlend();

			GlStateManager.popMatrix();

		}
	}

	private static void renderWingsIfActive(EntityPlayer entityplayer, float partialTickTime){
		ItemStack wand = entityplayer.getActiveItemStack();
		if(WizardData.get(entityplayer).currentlyCasting() instanceof Flight
				|| (entityplayer.isHandActive() && wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand
						&& WandHelper.getCurrentSpell(wand) instanceof Flight)){

			GlStateManager.pushMatrix();

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			// GlStateManager.rotate(-entityplayer.rotationYawHead, 0, 1, 0);
			GlStateManager.rotate(-entityplayer.renderYawOffset, 0, 1, 0);
			// GlStateManager.rotate(180, 1, 0, 0);

			Minecraft.getMinecraft().renderEngine.bindTexture(wingTexture);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.pushMatrix();

			GlStateManager.translate(0.1, 0.4, -0.15);
			GlStateManager.rotate(20 + 20 * (float)Math.sin(entityplayer.world.getWorldTime() * 0.3), 0, 1, 0);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();

			tessellator.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();

			GlStateManager.translate(-0.1, 0.4, -0.15);
			GlStateManager.rotate(-200 - 20 * (float)Math.sin(entityplayer.world.getWorldTime() * 0.3), 0, 1, 0);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();

			tessellator.draw();

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

			buffer.pos(0, 2, 0).tex(0, 0).endVertex();
			buffer.pos(0, 0, 0).tex(0, 1).endVertex();
			buffer.pos(2, 0, 0).tex(1, 1).endVertex();
			buffer.pos(2, 2, 0).tex(1, 0).endVertex();

			tessellator.draw();

			GlStateManager.popMatrix();

			GlStateManager.enableLighting();
			GlStateManager.disableBlend();

			GlStateManager.popMatrix();
		}
	}

	private static void renderShieldFirstPerson(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getActiveItemStack();
		if(WizardData.get(entityplayer) != null && WizardData.get(entityplayer).shield != null
				&& (WizardData.get(entityplayer).currentlyCasting() instanceof Shield
						|| (entityplayer.isHandActive() && wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand
								&& WandHelper.getCurrentSpell(wand) instanceof Shield))){

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GlStateManager.translate(0, 1.4, 0);

			GlStateManager.rotate(-entityplayer.rotationYaw, 0, 1, 0);
			GlStateManager.rotate(entityplayer.rotationPitch, 1, 0, 0);

			GlStateManager.translate(0, 0, 0.8);

			Tessellator tessellator = Tessellator.getInstance();

			Minecraft.getMinecraft().renderEngine.bindTexture(shieldTexture);

			renderShield(tessellator);

			GlStateManager.enableLighting();

			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			// RenderHelper.enableStandardItemLighting();

			GlStateManager.popMatrix();
		}
	}

	private static void renderShieldIfActive(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getActiveItemStack();
		if(WizardData.get(entityplayer).shield != null && (WizardData.get(entityplayer).currentlyCasting() instanceof Shield
				|| (entityplayer.isHandActive() && wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand
						&& WandHelper.getCurrentSpell(wand) instanceof Shield))){

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			// For some reason, the old blend function (GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA) caused the inner
			// edges to appear black, so I have changed it to this, which looks very slightly different.
			GlStateManager.blendFunc(GL11.GL_ONE, GL11.GL_SRC_ALPHA);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GlStateManager.translate(0, 1.3, 0);

			// GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate(-entityplayer.renderYawOffset, 0, 1, 0);
			// GlStateManager.rotate(-entityplayer.rotationPitch, 1, 0, 0);

			GlStateManager.translate(0, 0, 0.8);

			Tessellator tessellator = Tessellator.getInstance();

			Minecraft.getMinecraft().renderEngine.bindTexture(shieldTexture);

			renderShield(tessellator);

			GlStateManager.enableLighting();

			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.enableCull();
			GlStateManager.disableBlend();
			// RenderHelper.enableStandardItemLighting();

			GlStateManager.popMatrix();
		}
	}

	private static void renderShield(Tessellator tessellator){

		BufferBuilder buffer = tessellator.getBuffer();

		double widthOuter = 0.6d;
		double heightOuter = 0.7d;
		double widthInner = 0.3d;
		double heightInner = 0.4d;
		double depth = 0.2d;

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

		buffer.pos(-widthOuter, heightInner, -depth).tex(0, 0.2).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(-widthInner, heightOuter, -depth).tex(0.2, 0).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();

		buffer.pos(widthInner, heightOuter, -depth).tex(0.8, 0).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, heightInner, 0).tex(0.8, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthOuter, heightInner, -depth).tex(1, 0.2).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, heightInner, 0).tex(0.8, 0.2).color(200, 200, 255, 255).endVertex();

		buffer.pos(widthOuter, -heightInner, -depth).tex(1, 0.8).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, -heightInner, 0).tex(0.8, 0.8).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthInner, -heightOuter, -depth).tex(0.8, 1).color(0, 0, 0, 255).endVertex();
		buffer.pos(widthInner, -heightInner, 0).tex(0.8, 0.8).color(200, 200, 255, 255).endVertex();

		buffer.pos(-widthInner, -heightOuter, -depth).tex(0.2, 1).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, -heightInner, 0).tex(0.2, 0.8).color(200, 200, 255, 255).endVertex();
		buffer.pos(-widthOuter, -heightInner, -depth).tex(0, 0.8).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, -heightInner, 0).tex(0.2, 0.8).color(200, 200, 255, 255).endVertex();

		buffer.pos(-widthOuter, heightInner, -depth).tex(0, 0.2).color(0, 0, 0, 255).endVertex();
		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();

		tessellator.draw();

		buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);

		buffer.pos(-widthInner, heightInner, 0).tex(0.2, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthInner, heightInner, 0).tex(0.8, 0.2).color(200, 200, 255, 255).endVertex();
		buffer.pos(-widthInner, -heightInner, 0).tex(0.2, 0.8).color(200, 200, 255, 255).endVertex();
		buffer.pos(widthInner, -heightInner, 0).tex(0.8, 0.8).color(200, 200, 255, 255).endVertex();

		tessellator.draw();
	}

}
