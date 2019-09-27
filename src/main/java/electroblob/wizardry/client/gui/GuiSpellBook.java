package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

public class GuiSpellBook extends GuiScreen {

	private int xSize, ySize;
	private ItemSpellBook book;
	private Spell spell;

	public GuiSpellBook(ItemStack stack){
		super();
		xSize = 288;
		ySize = 180;
		if(!(stack.getItem() instanceof ItemSpellBook)) throw new ClassCastException("Cannot create spell book GUI for item that does not extend ItemSpellBook!");
		this.book = (ItemSpellBook)stack.getItem();
		this.spell = Spell.byMetadata(stack.getItemDamage());
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int par1, int par2, float par3){

		int xPos = this.width / 2 - xSize / 2;
		int yPos = this.height / 2 - this.ySize / 2;

		EntityPlayer player = Minecraft.getMinecraft().player;

		boolean discovered = true;
		if(Wizardry.settings.discoveryMode && !player.isCreative() && WizardData.get(player) != null
				&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

		GlStateManager.color(1, 1, 1, 1); // Just in case

		// Draws spell illustration on opposite page, underneath the book so it shows through the hole.
		Minecraft.getMinecraft().renderEngine.bindTexture(discovered ? spell.getIcon() : Spells.none.getIcon());
		DrawingUtils.drawTexturedRect(xPos + 146, yPos + 20, 0, 0, 128, 128, 128, 128);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(book.getGuiTexture(spell));
		DrawingUtils.drawTexturedRect(xPos, yPos, 0, 0, xSize, ySize, xSize, 256);

		super.drawScreen(par1, par2, par3);

		if(discovered){
			this.fontRenderer.drawString(spell.getDisplayName(), xPos + 17, yPos + 15, 0);
			this.fontRenderer.drawString(spell.getType().getDisplayName(), xPos + 17, yPos + 26, 0x777777);
		}else{
			this.mc.standardGalacticFontRenderer.drawString(SpellGlyphData.getGlyphName(spell, player.world), xPos + 17,
					yPos + 15, 0);
			this.mc.standardGalacticFontRenderer.drawString(spell.getType().getDisplayName(), xPos + 17, yPos + 26,
					0x777777);
		}

		//this.fontRenderer.drawString("-------------------", xPos + 17, yPos + 35, 0);

		if(spell.getTier() == Tier.NOVICE){
			// Novice is usually white but this doesn't show up.
			this.fontRenderer.drawString("Tier: \u00A77" + Tier.NOVICE.getDisplayName(), xPos + 17, yPos + 45, 0);
		}else{
			this.fontRenderer.drawString("Tier: " + spell.getTier().getDisplayNameWithFormatting(), xPos + 17, yPos + 45, 0);
		}

		String element = "Element: " + spell.getElement().getFormattingCode() + spell.getElement().getDisplayName();
		if(!discovered) element = "Element: ?";
		this.fontRenderer.drawString(element, xPos + 17, yPos + 57, 0);

		String manaCost = "Mana Cost: " + spell.getCost();
		if(spell.isContinuous) manaCost = "Mana Cost: " + spell.getCost() + "/second";
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
		
		this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.MISC_BOOK_OPEN, 1));
	}

	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public boolean doesGuiPauseGame(){
		return Wizardry.settings.booksPauseGame;
	}

}
