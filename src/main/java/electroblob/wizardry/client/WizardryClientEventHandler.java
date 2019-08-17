package electroblob.wizardry.client;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockMagicLight;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.data.SpellEmitterData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.*;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Method;

/**
 * Event handler responsible for client-side only events, mostly rendering.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public final class WizardryClientEventHandler {

	private static final ResourceLocation sixthSenseTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/sixth_sense.png");
	private static final ResourceLocation sixthSenseOverlayTexture = new ResourceLocation(Wizardry.MODID, "textures/gui/sixth_sense_overlay.png");
	private static final ResourceLocation frostOverlayTexture = new ResourceLocation(Wizardry.MODID, "textures/gui/frost_overlay.png");
	private static final ResourceLocation blinkOverlayTexture = new ResourceLocation(Wizardry.MODID, "textures/gui/blink_overlay.png");
	private static final ResourceLocation pointerTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/pointer.png");
	private static final ResourceLocation targetPointerTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/target_pointer.png");

	/** The remaining time for which the blink screen overlay effect will be displayed in first-person. Since this is
	 * only for the first-person player (the instance of which is itself stored in a static variable), this can simply
	 * be stored statically here, rather than needing to be in {@code WizardData}. */
	private static int blinkEffectTimer;
	/** The number of ticks the blink effect lasts for. */
	private static final int BLINK_EFFECT_DURATION = 8;

	private static final Method unpressKey;

	static {
		unpressKey = ObfuscationReflectionHelper.findMethod(KeyBinding.class, "func_74505_d", void.class);
	}
	
	/** Starts the first person blink overlay effect. */
	public static void playBlinkEffect(){
		blinkEffectTimer = BLINK_EFFECT_DURATION;
	}
	
	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.player == Minecraft.getMinecraft().player){

			if(blinkEffectTimer > 0) blinkEffectTimer--;

			// Only seems to work here...
//			EntityLiving victim = Possession.getPossessee(Minecraft.getMinecraft().player);
//			if(victim != null && victim.getHeldItemMainhand().isEmpty()){
//				Minecraft.getMinecraft().player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
//			}

			// Reset shaders if their respective potions aren't active
			// This is a player so the potion effects are synced by vanilla
			if(Minecraft.getMinecraft().entityRenderer.getShaderGroup() != null){

				String activeShader = Minecraft.getMinecraft().entityRenderer.getShaderGroup().getShaderGroupName();

				if((activeShader.equals(SlowTime.SHADER.toString()) && !Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.slow_time))
						|| (activeShader.equals(SixthSense.SHADER.toString()) && !Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.sixth_sense))
						|| (activeShader.equals(Transience.SHADER.toString()) && !Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.transience))){

					if(activeShader.equals(SixthSense.SHADER.toString())
					|| activeShader.equals(Transience.SHADER.toString())) playBlinkEffect();

					Minecraft.getMinecraft().entityRenderer.stopUseShader();
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onRenderHandEvent(RenderHandEvent event){
		
		EntityLiving victim = Possession.getPossessee(Minecraft.getMinecraft().player);
		
		if(victim != null){
			
			victim.rotationYawHead = Minecraft.getMinecraft().player.rotationYaw;
			
			if(Minecraft.getMinecraft().player.getHeldItemMainhand().isEmpty()){
				event.setCanceled(true);
			}
		}
	}

	// This event is called every tick, not just when a movement key is pressed
	@SubscribeEvent
	public static void onInputUpdateEvent(InputUpdateEvent event){
		// Prevents the player moving when paralysed
		if(event.getEntityPlayer().isPotionActive(WizardryPotions.paralysis)){
			event.getMovementInput().moveForward = 0;
			event.getMovementInput().moveStrafe = 0;
			event.getMovementInput().jump = false;
			event.getMovementInput().sneak = false;
		}
	}

	@SubscribeEvent
	public static void onClientTickEvent(TickEvent.ClientTickEvent event){

		if(event.phase == TickEvent.Phase.END && !net.minecraft.client.Minecraft.getMinecraft().isGamePaused()){

			World world = net.minecraft.client.Minecraft.getMinecraft().world;

			if(world == null) return;

			for(TileEntity tileentity : world.loadedTileEntityList){
				if(tileentity instanceof TileEntityDispenser){
					if(DispenserCastingData.get((TileEntityDispenser)tileentity) != null){
						DispenserCastingData.get((TileEntityDispenser)tileentity).update();
					}
				}
			}

			SpellEmitterData.update(world);
		}
	}
	
	@SubscribeEvent
	public static void onMouseEvent(MouseEvent event){
		
		// Prevents the player looking around when paralysed
		if(Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.paralysis)
				&& Minecraft.getMinecraft().inGameHasFocus){
			event.setCanceled(true);
			Minecraft.getMinecraft().player.prevRotationYaw = 0;
			Minecraft.getMinecraft().player.prevRotationPitch = 0;
			Minecraft.getMinecraft().player.rotationYaw = 0;
			Minecraft.getMinecraft().player.rotationPitch = 0;
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
		
		if(blinkEffectTimer > 0){
			float f = ((float)Math.max(blinkEffectTimer - 2, 0))/BLINK_EFFECT_DURATION;
			event.setNewfov(event.getFov() + f * f * 0.7f);
		}
	}

	@SubscribeEvent
	public static void onDrawBlockHighlightEvent(DrawBlockHighlightEvent event){
		// Hide the block outline for magic light blocks unless the player can dispel them
		if(event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK
				&& event.getPlayer().world.getBlockState(event.getTarget().getBlockPos()).getBlock() instanceof BlockMagicLight){

			if((!(event.getPlayer().getHeldItemMainhand().getItem() instanceof ISpellCastingItem)
				&& !(event.getPlayer().getHeldItemOffhand().getItem() instanceof ISpellCastingItem))
				|| !ItemArtefact.isArtefactActive(event.getPlayer(), WizardryItems.charm_light)){

				event.setCanceled(true);
			}
		}
	}

	// Brute-force fix for crystals not showing up when a wizard is given a spell book in the trade GUI.
	@SubscribeEvent
	public static void onGuiDrawForegroundEvent(GuiContainerEvent.DrawForeground event){

		if(event.getGuiContainer() instanceof GuiMerchant){
			
			GuiMerchant gui = (GuiMerchant)event.getGuiContainer();
			// Note that gui.getMerchant() returns an NpcMerchant, not an EntityWizard.
			
			// Using == the specific item rather than instanceof because that's how trades do it.
			if(gui.inventorySlots.getSlot(0).getStack().getItem() == WizardryItems.spell_book
					|| gui.inventorySlots.getSlot(1).getStack().getItem() == WizardryItems.spell_book){
				
				for(MerchantRecipe trade : gui.getMerchant().getRecipes(Minecraft.getMinecraft().player)){
					if(trade.getItemToBuy().getItem() == WizardryItems.spell_book && trade.getSecondItemToBuy().isEmpty()){
						Slot slot = gui.inventorySlots.getSlot(2);
						// It still doesn't look quite right because the slot highlight is behind the item, but it'll do
						// until/unless I find a better solution.
						DrawingUtils.drawItemAndTooltip(gui, trade.getItemToSell(), slot.xPos, slot.yPos, event.getMouseX(), event.getMouseY(),
								gui.getSlotUnderMouse() == slot);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<EntityLivingBase> event){

		Minecraft mc = Minecraft.getMinecraft();
		WizardData data = WizardData.get(mc.player);
		RenderManager renderManager = event.getRenderer().getRenderManager();

		ItemStack wand = mc.player.getHeldItemMainhand();

		if(!(wand.getItem() instanceof ISpellCastingItem)){
			wand = mc.player.getHeldItemOffhand();
		}

		// Target selection pointer
		if(mc.player.isSneaking() && wand.getItem() instanceof ISpellCastingItem && WizardryUtilities.isLiving(event.getEntity())
				 && data != null && data.selectedMinion != null){
			
			// -> Moved this in here so it isn't called every tick
			RayTraceResult rayTrace = RayTracer.standardEntityRayTrace(mc.world, mc.player, 16, false);
			
			if(rayTrace != null && rayTrace.entityHit == event.getEntity()){

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder buffer = tessellator.getBuffer();

				GlStateManager.pushMatrix();

				GlStateManager.disableCull();
				GlStateManager.disableLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
				// Disabling depth test allows it to be seen through everything.
				GlStateManager.disableDepth();
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
				GlStateManager.enableDepth();

				GlStateManager.popMatrix();
			}
		}

		// Summoned creature selection pointer
		if(data != null && data.selectedMinion != null && data.selectedMinion.get() == event.getEntity()){

			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();

			GlStateManager.pushMatrix();

			GlStateManager.disableCull();
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			// Disabling depth test allows it to be seen through everything.
			GlStateManager.disableDepth();
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
			GlStateManager.enableDepth();

			GlStateManager.popMatrix();
		}

		// Sixth sense
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
			GlStateManager.enableDepth();

			GlStateManager.popMatrix();
		}
	}

	@SubscribeEvent
	public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event){

		if(event.getType() == RenderGameOverlayEvent.ElementType.HELMET){
			
			if(Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.sixth_sense)){
				
				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableAlpha();
				
				renderScreenOverlay(event, sixthSenseOverlayTexture);
				
				GlStateManager.enableAlpha();
				GlStateManager.color(1, 1, 1, 1);
			}
			
			if(Minecraft.getMinecraft().player.isPotionActive(WizardryPotions.frost)){
				
				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableAlpha();
				
				renderScreenOverlay(event, frostOverlayTexture);
				
				GlStateManager.enableAlpha();
				GlStateManager.color(1, 1, 1, 1);
			}
			
			if(blinkEffectTimer > 0){
				
				float alpha = ((float)blinkEffectTimer)/BLINK_EFFECT_DURATION;
				
				OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
				GlStateManager.color(1, 1, 1, alpha);
				GlStateManager.disableAlpha();
				
				renderScreenOverlay(event, blinkOverlayTexture);
				
				GlStateManager.enableAlpha();
				GlStateManager.color(1, 1, 1, 1);
			}
		}
	}
	
	private static void renderScreenOverlay(RenderGameOverlayEvent.Post event, ResourceLocation texture){
		
		GlStateManager.pushMatrix();

		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0.0D, (double)event.getResolution().getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
		buffer.pos((double)event.getResolution().getScaledWidth(), (double)event.getResolution().getScaledHeight(), -90.0D).tex(1.0D, 1.0D)
				.endVertex();
		buffer.pos((double)event.getResolution().getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
		buffer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
		
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();

		GlStateManager.popMatrix();
	}

}
