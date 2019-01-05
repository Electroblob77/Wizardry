package electroblob.wizardry.client.gui;

import org.lwjgl.input.Keyboard;

import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiSpellBook extends GuiScreen {

	private int xSize, ySize;
	private Spell spell;

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/gui/spellbook.png");

	public GuiSpellBook(Spell spell){
		super();
		xSize = 288;
		ySize = 180;
		this.spell = spell;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3){

		int xPos = this.width / 2 - xSize / 2;
		int yPos = this.height / 2 - this.ySize / 2;

		EntityPlayer player = Minecraft.getMinecraft().player;

		boolean discovered = true;
		if(Wizardry.settings.discoveryMode && !player.capabilities.isCreativeMode && WizardData.get(player) != null
				&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

		// Draws spell illustration on opposite page, underneath the book so it shows through the hole.
		Minecraft.getMinecraft().renderEngine.bindTexture(discovered ? spell.getIcon() : Spells.none.getIcon());
		DrawingUtils.drawTexturedRect(xPos + 146, yPos + 20, 0, 0, 128, 128, 128, 128);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(textures.get(spell.tier));
		DrawingUtils.drawTexturedRect(xPos, yPos, 0, 0, xSize, ySize, xSize, 256);

		super.drawScreen(par1, par2, par3);

		if(discovered){
			this.fontRenderer.drawString(spell.getDisplayName(), xPos + 17, yPos + 15, 0);
			this.fontRenderer.drawString(spell.type.getDisplayName(), xPos + 17, yPos + 26, 0x777777);
		}else{
			this.mc.standardGalacticFontRenderer.drawString(SpellGlyphData.getGlyphName(spell, player.world), xPos + 17,
					yPos + 15, 0);
			this.mc.standardGalacticFontRenderer.drawString(spell.type.getDisplayName(), xPos + 17, yPos + 26,
					0x777777);
		}

		this.fontRenderer.drawString("-------------------", xPos + 17, yPos + 35, 0);

		if(spell.tier == Tier.BASIC){
			// Basic is usually white but this doesn't show up.
			this.fontRenderer.drawString("Tier: \u00A77" + Tier.BASIC.getDisplayName(), xPos + 17, yPos + 45, 0);
		}else{
			this.fontRenderer.drawString("Tier: " + spell.tier.getDisplayNameWithFormatting(), xPos + 17, yPos + 45, 0);
		}

		String element = "Element: " + spell.element.getFormattingCode() + spell.element.getDisplayName();
		if(!discovered) element = "Element: ?";
		this.fontRenderer.drawString(element, xPos + 17, yPos + 57, 0);

		String manaCost = "Mana Cost: " + spell.cost;
		if(spell.isContinuous) manaCost = "Mana Cost: " + spell.cost + "/second";
		if(!discovered) manaCost = "Mana Cost: ?";
		this.fontRenderer.drawString(manaCost, xPos + 17, yPos + 69, 0);

		if(discovered){
			this.fontRenderer.drawSplitString(spell.getDescription(), xPos + 17, yPos + 83, 118, 0);
		}else{
			this.mc.standardGalacticFontRenderer.drawSplitString(
					SpellGlyphData.getGlyphDescription(spell, player.world), xPos + 17, yPos + 83, 118, 0);
		}
	}

	public void initGui(){
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
	}

	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
}
