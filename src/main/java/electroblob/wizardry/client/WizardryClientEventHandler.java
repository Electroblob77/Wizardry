package electroblob.wizardry.client;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.item.ItemSpectralArmour;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Flight;
import electroblob.wizardry.spell.ShadowWard;
import electroblob.wizardry.spell.Shield;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;

/** Event handler responsible for all client-side only events, mostly rendering.
 * @since Wizardry 1.0 */
public class WizardryClientEventHandler {

	private static final ResourceLocation shieldTexture = new ResourceLocation("wizardry:textures/entity/shield.png");
	private static final ResourceLocation wingTexture = new ResourceLocation("wizardry:textures/entity/wing.png");
	private static final ResourceLocation shadowWardTexture = new ResourceLocation("wizardry:textures/entity/shadow_ward.png");
	private static final ResourceLocation sixthSenseTexture = new ResourceLocation("wizardry:textures/entity/sixth_sense.png");
	private static final ResourceLocation sixthSenseOverlayTexture = new ResourceLocation("wizardry:textures/gui/sixth_sense_overlay.png");
	private static final ResourceLocation frostOverlayTexture = new ResourceLocation("wizardry:textures/gui/frost_overlay.png");
	private static final ResourceLocation pointerTexture = new ResourceLocation("wizardry:textures/entity/pointer.png");
	private static final ResourceLocation targetPointerTexture = new ResourceLocation("wizardry:textures/entity/target_pointer.png");

	@SubscribeEvent
	public void onRenderPlayerArmourEvent(RenderPlayerEvent.SetArmorModel event){

		ItemStack stack = event.entityPlayer.inventory.armorInventory[event.slot];

		if(stack != null && stack.getItem() instanceof ItemSpectralArmour){
			GL11.glEnable(GL11.GL_BLEND);
		}
	}

	// Shift-scrolling to change spells
	@SubscribeEvent
	public void onMouseEvent(MouseEvent event){
		if(Minecraft.getMinecraft().inGameHasFocus && Minecraft.getMinecraft().thePlayer.getHeldItem() != null
				&& Minecraft.getMinecraft().thePlayer.getHeldItem().getItem() instanceof ItemWand && event.dwheel != 0
				&& Minecraft.getMinecraft().thePlayer.isSneaking() && Wizardry.enableShiftScrolling){

			event.setCanceled(true);

			if(event.dwheel > 0){
				// Packet building
				IMessage msg = new PacketControlInput.Message(2);
				WizardryPacketHandler.net.sendToServer(msg);

			}else if(event.dwheel < 0){
				// Packet building
				IMessage msg = new PacketControlInput.Message(1);
				WizardryPacketHandler.net.sendToServer(msg);
			}
		}
	}

	@SubscribeEvent
	public void onFOVUpdateEvent(FOVUpdateEvent event){

		// Bow zoom. Taken directly from EntityPlayerSP so it works exactly like vanilla.
		if (event.entity.isUsingItem() && event.entity.getItemInUse().getItem() instanceof ItemSpectralBow)
		{
			int i = event.entity.getItemInUseDuration();
			float f1 = (float)i / 20.0F;

			if (f1 > 1.0F)
			{
				f1 = 1.0F;
			}
			else
			{
				f1 *= f1;
			}

			event.newfov *= 1.0F - f1 * 0.15F;
		}
	}

	// Third person
	@SubscribeEvent
	public void onRenderPlayerEvent(RenderPlayerEvent.Specials.Post event){
		renderShieldIfActive(event.entityPlayer);
		renderWingsIfActive(event.entityPlayer, event.partialRenderTick);
		renderShadowWardIfActive(event.entityPlayer);
	}

	// First person
	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent event){
		// Now only fires in first person.
		if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0){
			renderShieldFirstPerson(Minecraft.getMinecraft().thePlayer);
			renderShadowWardFirstPerson(Minecraft.getMinecraft().thePlayer);
		}
	}

	@SubscribeEvent
	public void onRenderLivingEvent(RenderLivingEvent.Post event){
		/*
		// Frost effect
		if(event.entity.isPotionActive(Wizardry.frost)){

			GL11.glPushMatrix();
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			float someScalingFactor = 0.0625f;

			float yaw = event.entity.prevRotationYaw;
			
			//int brightness = event.entity.getBrightnessForRender(0);

			//int j = brightness % 65536;
			//int k = brightness / 65536;
			//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
			//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			
			
			GL11.glTranslated(event.x, event.y, event.z);

			GL11.glRotatef(-yaw + 180, 0F, 1F, 0F);

			Render render = RenderManager.instance.getEntityRenderObject(event.entity);

			RendererLivingEntity renderliving = event.renderer;

			// Reflection

			try {
				
				Timer timer = ReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer");
				
				// Chooses the appropriate zombie model, normal or villager
				// Fixed by moving before the model fields are accessed
				if(render instanceof RenderZombie && event.entity instanceof EntityZombie){
					// The second argument is never used...
					ReflectionHelper.findMethod(RenderZombie.class, (RenderZombie)render, new String[]{"func_82427_a"}, EntityZombie.class)
					.invoke(renderliving, (EntityZombie)event.entity);
				}
				
				// Turns out that java automatically infers the type parameter T in this method from the type
				// I am assigning the returned value to. Neat!
				ModelBase mainModel = ReflectionHelper.getPrivateValue(RendererLivingEntity.class, renderliving, "mainModel");
				
				mainModel.isRiding = event.entity.isRiding();
				mainModel.isChild = event.entity.isChild();

				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glScalef(-1.0F, -1.0F, 1.0F);

				// The second argument is never used...
				ReflectionHelper.findMethod(RendererLivingEntity.class, renderliving, new String[]{"preRenderCallback"}, EntityLivingBase.class, float.class)
				.invoke(renderliving, event.entity, someScalingFactor);

				// Why is this -1.5f? No idea!
				GL11.glTranslatef(0, -1.5f, 0);
				
				float f6 = event.entity.prevLimbSwingAmount + (event.entity.limbSwingAmount - event.entity.prevLimbSwingAmount) * timer.renderPartialTicks;
	            float f7 = event.entity.limbSwing - event.entity.limbSwingAmount * (1.0F - timer.renderPartialTicks);

	            if (event.entity.isChild())
	            {
	                f7 *= 3.0F;
	            }

	            if (f6 > 1.0F)
	            {
	                f6 = 1.0F;
	            }
	            
	            mainModel.setLivingAnimations(event.entity, f7, f6, timer.renderPartialTicks);

				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glColor4f(0.5f, 0.7f, 1, 0.5f);

				mainModel.render(event.entity, f7, f6, 0, 0, 0, someScalingFactor);

				GL11.glDepthMask(true);

			// 'Pokemon' exception handling... Because why not?!
			} catch (Exception e) {
				System.err.println("Something went very wrong! Error while rendering frost effect:");
				e.printStackTrace();
			}

			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glPopMatrix();
		}
		*/
		
		Minecraft mc = Minecraft.getMinecraft();
		ExtendedPlayer properties = ExtendedPlayer.get(mc.thePlayer);
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(mc.theWorld, mc.thePlayer, 16);
		
		// Target selection pointer
		if(mc.thePlayer.isSneaking() && mc.thePlayer.getHeldItem() != null
				&& mc.thePlayer.getHeldItem().getItem() instanceof ItemWand && rayTrace != null
				&& rayTrace.entityHit instanceof EntityLivingBase && rayTrace.entityHit == event.entity
				&& properties != null && properties.selectedMinion != null){
			
			Tessellator tessellator = Tessellator.instance;

			GL11.glPushMatrix();

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			// Disabling depth test allows it to be seen through everything.
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glColor4f(1, 1, 1, 1);

			GL11.glTranslated(event.x, event.y + event.entity.height + 0.5, event.z);

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? RenderManager.instance.playerViewX : -RenderManager.instance.playerViewX;
			GL11.glRotatef(180 - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(yaw, 1.0F, 0.0F, 0.0F);

			tessellator.startDrawingQuads();

			mc.renderEngine.bindTexture(targetPointerTexture);

			tessellator.addVertexWithUV(-0.2, 0.24, 0, 0, 0);
			tessellator.addVertexWithUV(0.2, 0.24, 0, 9f/16f, 0);
			tessellator.addVertexWithUV(0.2, -0.24, 0, 9f/16f, 11f/16f);
			tessellator.addVertexWithUV(-0.2, -0.24, 0, 0, 11f/16f);

			tessellator.draw();

			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glPopMatrix();
		}

		// Summoned creature selection pointer
		if(properties != null && properties.selectedMinion != null && properties.selectedMinion.get() == event.entity){
			
			Tessellator tessellator = Tessellator.instance;

			GL11.glPushMatrix();

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			// Disabling depth test allows it to be seen through everything.
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glColor4f(1, 1, 1, 1);

			GL11.glTranslated(event.x, event.y + event.entity.height + 0.5, event.z);

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? RenderManager.instance.playerViewX : -RenderManager.instance.playerViewX;
			GL11.glRotatef(180 - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(yaw, 1.0F, 0.0F, 0.0F);

			tessellator.startDrawingQuads();

			mc.renderEngine.bindTexture(pointerTexture);

			tessellator.addVertexWithUV(-0.2, 0.24, 0, 0, 0);
			tessellator.addVertexWithUV(0.2, 0.24, 0, 9f/16f, 0);
			tessellator.addVertexWithUV(0.2, -0.24, 0, 9f/16f, 11f/16f);
			tessellator.addVertexWithUV(-0.2, -0.24, 0, 0, 11f/16f);

			tessellator.draw();

			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glPopMatrix();
		}
		
		// Sixth sense
		if(mc.thePlayer.isPotionActive(Wizardry.sixthSense) && event.entity != mc.thePlayer
				&& mc.thePlayer.getActivePotionEffect(Wizardry.sixthSense) != null
				&& event.entity.getDistanceToEntity(mc.thePlayer) < 20*(1+mc.thePlayer.getActivePotionEffect(Wizardry.sixthSense).getAmplifier()*Wizardry.RANGE_INCREASE_PER_LEVEL)){

			Tessellator tessellator = Tessellator.instance;

			GL11.glPushMatrix();

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			// Disabling depth test allows it to be seen through everything.
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			GL11.glTranslated(event.x, event.y + event.entity.height * 0.6, event.z);

			// This counteracts the reverse rotation behaviour when in front f5 view.
			// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
			float yaw = mc.gameSettings.thirdPersonView == 2 ? RenderManager.instance.playerViewX : -RenderManager.instance.playerViewX;
			GL11.glRotatef(180 - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(yaw, 1.0F, 0.0F, 0.0F);

			tessellator.startDrawingQuads();

			mc.renderEngine.bindTexture(sixthSenseTexture);

			tessellator.addVertexWithUV(-0.6, 0.6, 0, 0, 0);
			tessellator.addVertexWithUV(0.6, 0.6, 0, 1, 0);
			tessellator.addVertexWithUV(0.6, -0.6, 0, 1, 1);
			tessellator.addVertexWithUV(-0.6, -0.6, 0, 0, 1);

			tessellator.draw();

			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			GL11.glPopMatrix();
		}
	}

	@SubscribeEvent
	public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event){
		if(event.type == RenderGameOverlayEvent.ElementType.HELMET
				&& Minecraft.getMinecraft().thePlayer.isPotionActive(Wizardry.sixthSense)){

			GL11.glPushMatrix();

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			Minecraft.getMinecraft().renderEngine.bindTexture(sixthSenseOverlayTexture);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(0.0D, (double)event.resolution.getScaledHeight(), -90.0D, 0.0D, 1.0D);
			tessellator.addVertexWithUV((double)event.resolution.getScaledWidth(), (double)event.resolution.getScaledHeight(), -90.0D, 1.0D, 1.0D);
			tessellator.addVertexWithUV((double)event.resolution.getScaledWidth(), 0.0D, -90.0D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
			tessellator.draw();
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			GL11.glPopMatrix();
		}

		if(event.type == RenderGameOverlayEvent.ElementType.HELMET
				&& Minecraft.getMinecraft().thePlayer.isPotionActive(Wizardry.frost)){

			GL11.glPushMatrix();

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			Minecraft.getMinecraft().renderEngine.bindTexture(frostOverlayTexture);
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(0.0D, (double)event.resolution.getScaledHeight(), -90.0D, 0.0D, 1.0D);
			tessellator.addVertexWithUV((double)event.resolution.getScaledWidth(), (double)event.resolution.getScaledHeight(), -90.0D, 1.0D, 1.0D);
			tessellator.addVertexWithUV((double)event.resolution.getScaledWidth(), 0.0D, -90.0D, 1.0D, 0.0D);
			tessellator.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
			tessellator.draw();
			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			GL11.glPopMatrix();
		}
	}

	private static void renderShadowWardFirstPerson(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getHeldItem();
		if(ExtendedPlayer.get(entityplayer).currentlyCasting() instanceof ShadowWard || (entityplayer.isUsingItem() && wand != null && wand.getItemDamage() < wand.getMaxDamage()
				&& wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof ShadowWard)){

			GL11.glPushMatrix();

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GL11.glRotatef(-1*entityplayer.rotationYaw, 0, 1, 0);
			GL11.glRotatef(entityplayer.rotationPitch, 1, 0, 0);
			Minecraft.getMinecraft().renderEngine.bindTexture(shadowWardTexture);
			Tessellator tessellator = Tessellator.instance;

			GL11.glPushMatrix();

			GL11.glTranslated(0, -0.3, 1.2);
			GL11.glRotated(entityplayer.worldObj.getWorldTime()*-2, 0, 0, 1);
			GL11.glScaled(1.1, 1.1, 1.1);

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(-0.5, 0.5, -0.5, 0, 0);
			tessellator.addVertexWithUV(0.5, 0.5, -0.5, 1, 0);
			tessellator.addVertexWithUV(0.5, -0.5, -0.5, 1, 1);
			tessellator.addVertexWithUV(-0.5, -0.5, -0.5, 0, 1);

			tessellator.draw();

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(-0.5, 0.5, -0.5, 0, 0);
			tessellator.addVertexWithUV(-0.5, -0.5, -0.5, 0, 1);
			tessellator.addVertexWithUV(0.5, -0.5, -0.5, 1, 1);
			tessellator.addVertexWithUV(0.5, 0.5, -0.5, 1, 0);

			tessellator.draw();

			GL11.glPopMatrix();

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();

		}
	}

	private static void renderShadowWardIfActive(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getHeldItem();
		if(ExtendedPlayer.get(entityplayer).currentlyCasting() instanceof ShadowWard || (entityplayer.isUsingItem() && wand != null
				&& wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof ShadowWard)){

			GL11.glPushMatrix();

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GL11.glRotatef(180, 0, 1, 0);
			//GL11.glRotatef(entityplayer.rotationYaw, 0, 1, 0);
			//GL11.glRotatef(-entityplayer.rotationPitch, 1, 0, 0);
			Minecraft.getMinecraft().renderEngine.bindTexture(shadowWardTexture);
			Tessellator tessellator = Tessellator.instance;

			GL11.glPushMatrix();

			// was 0, -0.3, 1.2
			GL11.glTranslated(0, 0, 1.2);
			GL11.glRotated(entityplayer.worldObj.getWorldTime()*-2, 0, 0, 1);
			GL11.glScaled(1.1, 1.1, 1.1);

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(-0.5, 0.5, -0.5, 0, 0);
			tessellator.addVertexWithUV(0.5, 0.5, -0.5, 1, 0);
			tessellator.addVertexWithUV(0.5, -0.5, -0.5, 1, 1);
			tessellator.addVertexWithUV(-0.5, -0.5, -0.5, 0, 1);

			tessellator.draw();

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(-0.5, 0.5, -0.5, 0, 0);
			tessellator.addVertexWithUV(-0.5, -0.5, -0.5, 0, 1);
			tessellator.addVertexWithUV(0.5, -0.5, -0.5, 1, 1);
			tessellator.addVertexWithUV(0.5, 0.5, -0.5, 1, 0);

			tessellator.draw();

			GL11.glPopMatrix();

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();

		}
	}

	private static void renderWingsIfActive(EntityPlayer entityplayer, float partialTickTime){
		ItemStack wand = entityplayer.getHeldItem();
		if(ExtendedPlayer.get(entityplayer).currentlyCasting() instanceof Flight || (entityplayer.isUsingItem() && wand != null
				&& wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof Flight)){

			GL11.glPushMatrix();

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GL11.glRotatef(-entityplayer.rotationYawHead, 0, 1, 0);
			GL11.glRotatef(entityplayer.rotationYaw, 0, 1, 0);
			GL11.glRotatef(180, 1, 0, 0);

			Minecraft.getMinecraft().renderEngine.bindTexture(wingTexture);
			Tessellator tessellator = Tessellator.instance;

			GL11.glPushMatrix();

			GL11.glTranslated(0.1, -1.0, -0.15);
			GL11.glRotatef(20 + 20*(float)Math.sin(entityplayer.worldObj.getWorldTime()*0.3), 0, 1, 0);

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(0, 2, 0, 0, 0);
			tessellator.addVertexWithUV(2, 2, 0, 1, 0);
			tessellator.addVertexWithUV(2, 0, 0, 1, 1);
			tessellator.addVertexWithUV(0, 0, 0, 0, 1);

			tessellator.draw();

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(0, 2, 0, 0, 0);
			tessellator.addVertexWithUV(0, 0, 0, 0, 1);
			tessellator.addVertexWithUV(2, 0, 0, 1, 1);
			tessellator.addVertexWithUV(2, 2, 0, 1, 0);

			tessellator.draw();

			GL11.glPopMatrix();

			GL11.glPushMatrix();

			GL11.glTranslated(-0.1, -1.0, -0.15);
			GL11.glRotatef(-200 - 20*(float)Math.sin(entityplayer.worldObj.getWorldTime()*0.3), 0, 1, 0);

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(0, 2, 0, 0, 0);
			tessellator.addVertexWithUV(2, 2, 0, 1, 0);
			tessellator.addVertexWithUV(2, 0, 0, 1, 1);
			tessellator.addVertexWithUV(0, 0, 0, 0, 1);

			tessellator.draw();

			tessellator.startDrawingQuads();

			tessellator.addVertexWithUV(0, 2, 0, 0, 0);
			tessellator.addVertexWithUV(0, 0, 0, 0, 1);
			tessellator.addVertexWithUV(2, 0, 0, 1, 1);
			tessellator.addVertexWithUV(2, 2, 0, 1, 0);

			tessellator.draw();

			GL11.glPopMatrix();

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);

			GL11.glPopMatrix();
		}
	}

	private static void renderShieldFirstPerson(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getHeldItem();
		if(ExtendedPlayer.get(entityplayer).shield != null && (ExtendedPlayer.get(entityplayer).currentlyCasting() instanceof Shield || (entityplayer.isUsingItem() && wand != null
				&& wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof Shield))){

			double x = entityplayer.posX;
			double y = entityplayer.posY;
			double z = entityplayer.posZ;

			EntityShield shield = (EntityShield)ExtendedPlayer.get(entityplayer).shield;

			GL11.glPushMatrix();

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GL11.glTranslated(0, -0.3, 0);

			GL11.glRotatef(-1*entityplayer.rotationYaw, 0, 1, 0);
			GL11.glRotatef(entityplayer.rotationPitch, 1, 0, 0);

			GL11.glTranslated(0, 0, 0.8);

			Tessellator tessellator = Tessellator.instance;

			Minecraft.getMinecraft().renderEngine.bindTexture(shieldTexture);

			renderShield(tessellator, -1);

			// Enchantment effect
			/*
	        GL11.glPushMatrix();

            GL11.glDisable(GL11.GL_LIGHTING);
            //this.bindTexture(RES_ITEM_GLINT);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            float f7 = 0.76F;
            GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
            float f8 = 0.125F;
            //GL11.glScalef(f8, f8, f8);
            float f9 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 0.8f;
            //GL11.glTranslatef(f9, 0.0F, 0.0F);
            //GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);

            //this.renderShield(tessellator, f9);

            GL11.glPopMatrix();
			 */
			GL11.glEnable(GL11.GL_LIGHTING);

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			//RenderHelper.enableStandardItemLighting();

			GL11.glPopMatrix();
		}
	}

	private static void renderShieldIfActive(EntityPlayer entityplayer){
		ItemStack wand = entityplayer.getHeldItem();
		if(ExtendedPlayer.get(entityplayer).shield != null && (ExtendedPlayer.get(entityplayer).currentlyCasting() instanceof Shield || (entityplayer.isUsingItem() && wand != null
				&& wand.getItemDamage() < wand.getMaxDamage() && wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof Shield))){

			double x = entityplayer.posX;
			double y = entityplayer.posY;
			double z = entityplayer.posZ;

			EntityShield shield = (EntityShield)ExtendedPlayer.get(entityplayer).shield;

			GL11.glPushMatrix();

			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_SRC_ALPHA);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glDisable(GL11.GL_LIGHTING);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

			GL11.glTranslated(0, 0, 0);

			GL11.glRotatef(180, 0, 1, 0);
			//GL11.glRotatef(entityplayer.rotationYaw, 0, 1, 0);
			//GL11.glRotatef(-entityplayer.rotationPitch, 1, 0, 0);

			GL11.glTranslated(0, 0, 0.8);

			Tessellator tessellator = Tessellator.instance;

			Minecraft.getMinecraft().renderEngine.bindTexture(shieldTexture);

			renderShield(tessellator, -1);

			// Enchantment effect
			/*
	        GL11.glPushMatrix();

            GL11.glDisable(GL11.GL_LIGHTING);
            //this.bindTexture(RES_ITEM_GLINT);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            float f7 = 0.76F;
            GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
            float f8 = 0.125F;
            //GL11.glScalef(f8, f8, f8);
            float f9 = (float)(Minecraft.getSystemTime() % 3000L) / 3000.0F * 0.8f;
            //GL11.glTranslatef(f9, 0.0F, 0.0F);
            //GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);

            //this.renderShield(tessellator, f9);

            GL11.glPopMatrix();
			 */
			GL11.glEnable(GL11.GL_LIGHTING);

			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			//RenderHelper.enableStandardItemLighting();

			GL11.glPopMatrix();
		}
	}

	private static void renderShield(Tessellator tessellator, float textureOffset){

		double widthOuter = 0.6d;
		double heightOuter = 0.7d;
		double widthInner = 0.3d;
		double heightInner = 0.4d;
		double depth = 0.2d;

		double textureSection = 1.0d;
		double textureU = 0.0d;

		if(textureOffset != -1){
			textureSection = 0.2d;
			textureU = textureOffset;
		}

		tessellator.startDrawing(5);

		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(-widthOuter, heightOuter - 0.3, -depth, 0, 0);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);
		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(-widthOuter + 0.3, heightOuter, -depth, 0, 0);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);

		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(widthOuter - 0.3, heightOuter, -depth, 1, 0);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(widthInner, heightInner, 0, textureU + textureSection, 0.2);
		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(widthOuter, heightOuter - 0.3, -depth, 1, 0);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(widthInner, heightInner, 0, textureU + textureSection, 0.2);

		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(widthOuter, -heightOuter + 0.3, -depth, 1, 1);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(widthInner, -heightInner, 0, textureU + textureSection, 0.8);
		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(widthOuter - 0.3, -heightOuter, -depth, 1, 1);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(widthInner, -heightInner, 0, textureU + textureSection, 0.8);

		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(-widthOuter + 0.3, -heightOuter, -depth, 0, 1);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(-widthInner, -heightInner, 0, textureU, 0.8);
		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(-widthOuter, -heightOuter + 0.3, -depth, 0, 1);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(-widthInner, -heightInner, 0, textureU, 0.8);

		tessellator.setColorRGBA(0, 0, 0, 255);
		tessellator.addVertexWithUV(-widthOuter, heightOuter - 0.3, -depth, 0, 0);
		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);

		tessellator.draw();

		tessellator.startDrawing(5);

		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(-widthInner, heightInner, 0, textureU, 0.2);

		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(widthInner, heightInner, 0, textureU + textureSection, 0.2);

		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(-widthInner, -heightInner, 0, textureU, 0.8);

		tessellator.setColorOpaque(200, 200, 255);
		tessellator.addVertexWithUV(widthInner, -heightInner, 0, textureU + textureSection, 0.8);

		tessellator.draw();
	}

}
