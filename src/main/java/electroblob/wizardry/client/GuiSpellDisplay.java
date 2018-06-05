package electroblob.wizardry.client;

import java.util.List;

import electroblob.wizardry.Settings.GuiPosition;
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

	private static final ResourceLocation hudTexture = new ResourceLocation(Wizardry.MODID, "textures/gui/spell_hud.png");

	public GuiSpellDisplay(Minecraft par1Minecraft){
		super();
		this.mc = par1Minecraft;
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

		// Coordinates of the top left corner of the HUD.
		int left = 0;
		int top = 0;
		boolean mirror = false;

		if(Wizardry.settings.spellHUDPosition == GuiPosition.BOTTOM_LEFT){
			left = 0;
			top = height - 36;
		}else if(Wizardry.settings.spellHUDPosition == GuiPosition.TOP_LEFT){
			left = 0;
			top = 0;
		}else if(Wizardry.settings.spellHUDPosition == GuiPosition.TOP_RIGHT){
			left = width - 128;
			top = 0;
			mirror = true;
		}else if(Wizardry.settings.spellHUDPosition == GuiPosition.BOTTOM_RIGHT){
			left = width - 128;
			top = height - 36;
			mirror = true;
		}

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

			int maxWidth = 90;

			if(font.getStringWidth(spellName) <= maxWidth){
				// Single line is rendered more centrally
				font.drawStringWithShadow(colour + spellName, mirror ? left + 5 : left + 41, top + 13, 0xffffffff);

			}else{

				int lineNumber = 0;

				List<String> lines = font.listFormattedStringToWidth(spellName, maxWidth);

				for(Object line : lines){
					if(line instanceof String){
						font.drawStringWithShadow(colour + (String)line, mirror ? left + 5 : left + 41, top + 6 + 11 * lineNumber, 0xffffffff);
					}
					lineNumber++;
				}
			}

		}else if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR){

			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1, 1, 1);

			this.mc.renderEngine.bindTexture(hudTexture);

			// Background of spell hud
			this.drawTexturedModalRect(left, top, 0, mirror ? 36 : 0, 128, 36);

			// Cooldown bar
			if(cooldown > 0){
				this.drawTexturedModalRect(mirror ? left + 5 : left + 41, top + 28, 128, 6, 82, 6);

				int l = (int)(((double)(spell.cooldown * cooldownMultiplier - cooldown) / (double)(spell.cooldown * cooldownMultiplier)) * 82);

				this.drawTexturedModalRect(mirror ? left + 5 : left + 41, top + 28, 128, 0, l, 6);
			}

			// Spell illustration
			this.mc.renderEngine.bindTexture(discovered ? spell.getIcon() : Spells.none.getIcon());

			WizardryUtilities.drawTexturedRect(mirror ? left + 94 : left + 2, top + 2, 0, 0, 32, 32, 32, 32);
			
			// Blend needs to be left enabled here because otherwise the hotbar becomes opaque
		}
	}

}
