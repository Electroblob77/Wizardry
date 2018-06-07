package electroblob.wizardry.client;

import java.util.List;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.spell.None;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class GuiSpellDisplay extends Gui {

	private Minecraft mc;

	private static final ResourceLocation hudTexture = new ResourceLocation(Wizardry.MODID, "textures/gui/spell_hud.png");

	public GuiSpellDisplay(Minecraft par1Minecraft) {
		super();
		this.mc = par1Minecraft;
	}

	@SubscribeEvent
	public void draw(RenderGameOverlayEvent event){

		EntityPlayer player = this.mc.thePlayer;

		if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemWand){

			int width = event.resolution.getScaledWidth();
			int height = event.resolution.getScaledHeight();

			Spell spell = WandHelper.getCurrentSpell(player.getHeldItem());
			int cooldown = WandHelper.getCurrentCooldown(player.getHeldItem());

			float cooldownMultiplier = 1.0f - WandHelper.getUpgradeLevel(player.getHeldItem(), Wizardry.cooldownUpgrade)*Wizardry.COOLDOWN_REDUCTION_PER_LEVEL;

			if(player.isPotionActive(Wizardry.fontOfMana)){
				// Dividing by this rather than setting it takes upgrades and font of mana into account simultaneously
				cooldownMultiplier /= 2 + player.getActivePotionEffect(Wizardry.fontOfMana).getAmplifier();
			}

			// Coordinates of the top left corner of the HUD.
			int left = 0;
			int top = 0;
			boolean mirror = false;

			if(Wizardry.spellHUDPosition.equals("Bottom left")){
				left = 0;
				top = height-36;
			}else if(Wizardry.spellHUDPosition.equals("Top left")){
				left = 0;
				top = 0;
			}else if(Wizardry.spellHUDPosition.equals("Top right")){
				left = width-128;
				top = 0;
				mirror = true;
			}else if(Wizardry.spellHUDPosition.equals("Bottom right")){
				left = width-128;
				top = height-36;
				mirror = true;
			}

			boolean discovered = true;

			if(!player.capabilities.isCreativeMode && ExtendedPlayer.get(player) != null){
				discovered = ExtendedPlayer.get(player).hasSpellBeenDiscovered(spell);
			}

			if(event.type == RenderGameOverlayEvent.ElementType.TEXT){

				// Makes spells greyed out if they are in cooldown or if the player has the arcane jammer effect
				String colour = cooldown > 0 || player.isPotionActive(Wizardry.arcaneJammer) ? "\u00A78" : spell.element.colour;
				if(!discovered) colour = "\u00A79";
				String spellName = discovered ? spell.getDisplayName() : SpellGlyphData.getGlyphName(spell, player.worldObj);
				FontRenderer font = discovered ? this.mc.fontRenderer : this.mc.standardGalacticFontRenderer;

				int maxWidth = 90;

				if(font.getStringWidth(spellName) <= maxWidth){
					// Single line is rendered more centrally
					font.drawStringWithShadow(colour + spellName, mirror ? left+5 : left+41, top+13, 0xffffffff);

				}else{

					int lineNumber = 0;

					List lines = font.listFormattedStringToWidth(spellName, maxWidth);

					for(Object line : lines){
						if(line instanceof String){
							font.drawStringWithShadow(colour + (String)line, mirror ? left+5 : left+41, top+6 + 11*lineNumber, 0xffffffff);
						}
						lineNumber++;
					}
				}

			}else if(event.type == RenderGameOverlayEvent.ElementType.HOTBAR){

				GL11.glPushMatrix();
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				GL11.glColor3f(1.0f, 1.0f, 1.0f);
				this.mc.renderEngine.bindTexture(hudTexture);

				// Background of spell hud
				this.drawTexturedModalRect(left, top, 0, mirror ? 36 : 0, 128, 36);

				// Cooldown bar
				if(cooldown > 0){
					this.drawTexturedModalRect(mirror ? left+5 : left+41, height-8, 128, 6, 82, 6);

					int l = (int)(((double)(spell.cooldown * cooldownMultiplier - cooldown)
							/ (double)(spell.cooldown * cooldownMultiplier)) * 82);

					this.drawTexturedModalRect(mirror ? left+5 : left+41, height-8, 128, 0, l, 6);
				}

				// Spell illustration
				this.mc.renderEngine.bindTexture(discovered ? spell.icon : WizardryRegistry.none.icon);

				this.drawTexturedRect(mirror ? left+94 : left+2, top+2, 0, 0, 32, 32, 32, 32);

				GL11.glDisable(GL11.GL_BLEND);
				GL11.glPopMatrix();
			}
		}
	}

	/**
	 * Draws a textured rectangle, taking the size of the image and the bit needed into account. Client side only, of course.
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 */
	public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
	{
		float f = 1F / (float)textureWidth;
		float f1 = 1F / (float)textureHeight;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double)(x), (double)(y + height), 0, (double)((float)(u) * f), (double)((float)(v + height) * f1));
		tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(u + width) * f), (double)((float)(v + height) * f1));
		tessellator.addVertexWithUV((double)(x + width), (double)(y), 0, (double)((float)(u + width) * f), (double)((float)(v) * f1));
		tessellator.addVertexWithUV((double)(x), (double)(y), 0, (double)((float)(u) * f), (double)((float)(v) * f1));
		tessellator.draw();
	}

}
