package electroblob.wizardry.client.gui;

import java.util.List;

import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GuiSpellDisplay extends Gui {

	private Minecraft mc;

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/gui/spell_hud.png");

	// Measurements
	/** Width of the used portion of the HUD texture */ 				private static final int HUD_WIDTH = 128;
	/** Height of the used portion of the HUD texture */				private static final int HUD_HEIGHT = 64;
	/** Distance (in x and y) of the spell icon from the screen edge */ private static final int SPELL_ICON_INSET = 2;
	/** Line spacing for the spell name (when on two lines) */			private static final int TEXT_LINE_SPACING = 1;
	/** Distance in x from the edge of the screen to the spell name */	private static final int TEXT_INSET_X = 42;
	/** Distance in x of the cooldown bar from the screen edge */		private static final int COOLDOWN_BAR_INSET_X = 42;
	/** Length of the cooldown bar portion of the HUD texture */		private static final int COOLDOWN_BAR_LENGTH = 79;
	/** Height of the cooldown bar portion of the HUD texture */		private static final int COOLDOWN_BAR_HEIGHT = 5;
	/** Width and height of the spell icon (very unlikely to change!) */private static final int SPELL_ICON_SIZE = 32;
	
	public GuiSpellDisplay(Minecraft minecraft){
		super();
		this.mc = minecraft;
	}

	@SubscribeEvent
	public void draw(RenderGameOverlayEvent event){

		EntityPlayer player = this.mc.player;

		// If the player has a wand in each hand, only displays for the one in the main hand.

		ItemStack wand = player.getHeldItemMainhand();

		if(!(wand.getItem() instanceof ItemWand)){
			wand = player.getHeldItemOffhand();
			// If the player isn't holding a wand, then nothing else needs to be done.
			if(!(wand.getItem() instanceof ItemWand)) return;
		}

		int width = event.getResolution().getScaledWidth();
		int height = event.getResolution().getScaledHeight();

		Spell spell = WandHelper.getCurrentSpell(wand);
		int cooldown = WandHelper.getCurrentCooldown(wand);

		float cooldownMultiplier = 1.0f - WandHelper.getUpgradeLevel(wand, WizardryItems.cooldown_upgrade) * Constants.COOLDOWN_REDUCTION_PER_LEVEL;

		if(player.isPotionActive(WizardryPotions.font_of_mana)){
			// Dividing by this rather than setting it takes upgrades and font of mana into account simultaneously
			cooldownMultiplier /= 2 + player.getActivePotionEffect(WizardryPotions.font_of_mana).getAmplifier();
		}

		boolean flipX = Wizardry.settings.spellHUDPosition.flipX;
		boolean flipY = Wizardry.settings.spellHUDPosition.flipY;

		boolean discovered = true;

		if(!player.capabilities.isCreativeMode && WizardData.get(player) != null){
			discovered = WizardData.get(player).hasSpellBeenDiscovered(spell);
		}

		if(event.getType() == RenderGameOverlayEvent.ElementType.TEXT){

			// Makes spells greyed out if they are in cooldown or if the player has the arcane jammer effect
			String colour = cooldown > 0 || player.isPotionActive(WizardryPotions.arcane_jammer) ? "\u00A78" : spell.element.getFormattingCode();
			if(!discovered) colour = "\u00A79";
			String spellName = discovered ? spell.getDisplayName() : SpellGlyphData.getGlyphName(spell, player.world);
			FontRenderer font = discovered ? this.mc.fontRenderer : this.mc.standardGalacticFontRenderer;

			int maxWidth = HUD_WIDTH - TEXT_INSET_X;
			int stringWidth = font.getStringWidth(spellName);
			int iconMidpoint = (SPELL_ICON_SIZE + 2*SPELL_ICON_INSET)/2; // Should be 18

			// TODO: Make a 'scrolling' animation when changing spells, using the text alpha channel to fade it in
			// and out.
			if(stringWidth <= maxWidth){
				//GL11.glPushMatrix();
                //GL11.glEnable(GL11.GL_BLEND);
                //OpenGlHelper.glBlendFunc(770, 771, 1, 0); // Taken from GuiInGame. TODO: Replace with OpenGL caps.
				// Single line is rendered more centrally
				font.drawStringWithShadow(colour + spellName, flipX ? width - TEXT_INSET_X - stringWidth : TEXT_INSET_X,
						// The text is an odd number of pixels high so we need to subtract an extra 1 when at the bottom
						flipY ? iconMidpoint - font.FONT_HEIGHT/2 : height - iconMidpoint - font.FONT_HEIGHT/2 - 1, 0xffffffff);
                //GL11.glDisable(GL11.GL_BLEND);
                //GL11.glPopMatrix();
			}else{

				int lineNumber = 0;

				List<String> lines = font.listFormattedStringToWidth(spellName, maxWidth);

				for(String line : lines){
					int lineWidth = font.getStringWidth((String)line);
					font.drawStringWithShadow(colour + (String)line, flipX ? width - TEXT_INSET_X - lineWidth : TEXT_INSET_X,
							// This time there are two lines so we need to subtract the full line height, and an extra 1 for the spacing 
							(flipY ? iconMidpoint - font.FONT_HEIGHT - 1 : height - iconMidpoint - font.FONT_HEIGHT - 1)
							// Note that there should only ever be two lines maximum
							+ lineNumber*(font.FONT_HEIGHT + TEXT_LINE_SPACING), 0xffffffff);
					lineNumber++;
				}
			}

		}else if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR){

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1, 1, 1);

			// Spell illustration - this is now done first so it is behind the HUD texture
			this.mc.renderEngine.bindTexture(discovered ? spell.getIcon() : Spells.none.getIcon());

			WizardryUtilities.drawTexturedRect(flipX ? width - SPELL_ICON_INSET - SPELL_ICON_SIZE : SPELL_ICON_INSET,
					flipY ? SPELL_ICON_INSET : height - SPELL_ICON_INSET - SPELL_ICON_SIZE, 0, 0, 32, 32, 32, 32);
			
			this.mc.renderEngine.bindTexture(texture);

			// Background of spell hud
			WizardryUtilities.drawTexturedFlippedRect(flipX ? width-HUD_WIDTH : 0, flipY ? 0 : height-HUD_HEIGHT,
					// The 128 here is a uv value, not a dimension, and hence is left as a hardcoded number.
					player.capabilities.isCreativeMode ? 128 : 0, 0, HUD_WIDTH, HUD_HEIGHT, 256, 256, flipX, flipY);

			// Cooldown bar
			if(!player.capabilities.isCreativeMode && cooldown > 0){

				int l = (int)(((double)(spell.cooldown * cooldownMultiplier - cooldown)
						/ (double)(spell.cooldown * cooldownMultiplier)) * COOLDOWN_BAR_LENGTH);
				// Likewise, the 64 here is a uv value and therefore left as a hardcoded number.
				WizardryUtilities.drawTexturedFlippedRect(flipX ? width - COOLDOWN_BAR_INSET_X - COOLDOWN_BAR_LENGTH : COOLDOWN_BAR_INSET_X,
						flipY ? 1 : height-1-COOLDOWN_BAR_HEIGHT, 0, 64, l, COOLDOWN_BAR_HEIGHT, 256, 256, false, flipY);
			}

			GlStateManager.popMatrix();
			
			// Blend needs to be left enabled here because otherwise the hotbar becomes opaque
		}
	}

}
