package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

/** Abstract base class for both {@link GuiSpellBook} and {@link GuiLectern}. Centralises code common to both
 * those classes. */
public abstract class GuiSpellInfo extends GuiScreen {

	protected static final String TRANSLATION_KEY_PREFIX = "gui." + Wizardry.MODID + ":spell_book";

	protected final int xSize, ySize;
	protected int textureWidth = 512;
	protected int textureHeight = 256;

	public GuiSpellInfo(int xSize, int ySize){
		super();
		this.xSize = xSize;
		this.ySize = ySize;
	}

	protected void setTextureSize(int width, int height){
		this.textureWidth = width;
		this.textureHeight = height;
	}

	/** Returns the spell to be displayed by this GUI. */
	public abstract Spell getSpell();

	/** Returns the main texture for the background of this GUI. */
	public abstract ResourceLocation getTexture();

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		int left = this.width/2 - xSize/2;
		int top = this.height/2 - this.ySize/2;
		this.drawDefaultBackground();
		this.drawBackgroundLayer(left, top, mouseX, mouseY);
		super.drawScreen(mouseX, mouseY, partialTicks); // Just draws the buttons
		this.drawForegroundLayer(left, top, mouseX, mouseY);
	}

	/**
	 * Draws the background of the spell info GUI. This is called before buttons are drawn.
	 * @param left The x-coordinate of the left-hand edge of the GUI
	 * @param top The y-coordinate of the top edge of the GUI
	 * @param mouseX The current x position of the mouse pointer
	 * @param mouseY The current y position of the mouse pointer
	 */
	protected void drawBackgroundLayer(int left, int top, int mouseX, int mouseY){

		boolean discovered = Wizardry.proxy.shouldDisplayDiscovered(getSpell(), null);

		GlStateManager.color(1, 1, 1, 1); // Just in case

		// Draws spell illustration on opposite page, underneath the book so it shows through the hole.
		Minecraft.getMinecraft().renderEngine.bindTexture(discovered ? getSpell().getIcon() : Spells.none.getIcon());
		DrawingUtils.drawTexturedRect(left + 146, top + 20, 0, 0, 128, 128, 128, 128);

		Minecraft.getMinecraft().renderEngine.bindTexture(getTexture());
		DrawingUtils.drawTexturedRect(left, top, 0, 0, xSize, ySize, textureWidth, textureHeight);
	}

	/**
	 * Draws the foreground of the spell info GUI. This is called after buttons are drawn.
	 * @param left The x-coordinate of the left-hand edge of the GUI
	 * @param top The y-coordinate of the top edge of the GUI
	 * @param mouseX The current x position of the mouse pointer
	 * @param mouseY The current y position of the mouse pointer
	 */
	protected void drawForegroundLayer(int left, int top, int mouseX, int mouseY){

		boolean discovered = Wizardry.proxy.shouldDisplayDiscovered(getSpell(), null);

		if(discovered){
			this.fontRenderer.drawString(getSpell().getDisplayName(), left + 17, top + 15, 0);
			this.fontRenderer.drawString(getSpell().getType().getDisplayName(), left + 17, top + 26, 0x777777);
		}else{
			this.mc.standardGalacticFontRenderer.drawString(SpellGlyphData.getGlyphName(getSpell(), mc.world), left + 17,
					top + 15, 0);
			this.mc.standardGalacticFontRenderer.drawString(getSpell().getType().getDisplayName(), left + 17, top + 26,
					0x777777);
		}

		// Novice is usually white but this doesn't show up
		String tier = I18n.format(TRANSLATION_KEY_PREFIX + ".tier", getSpell().getTier() == Tier.NOVICE ?
				"\u00A77" + getSpell().getTier().getDisplayName() : getSpell().getTier().getDisplayNameWithFormatting());
		this.fontRenderer.drawString(tier, left + 17, top + 45, 0);

		String element = I18n.format(TRANSLATION_KEY_PREFIX + ".element", getSpell().getElement().getFormattingCode() + getSpell().getElement().getDisplayName());
		if(!discovered) element = I18n.format(TRANSLATION_KEY_PREFIX + ".element_undiscovered");
		this.fontRenderer.drawString(element, left + 17, top + 57, 0);

		String manaCost = I18n.format(TRANSLATION_KEY_PREFIX + ".mana_cost", getSpell().getCost());
		if(getSpell().isContinuous) manaCost = I18n.format(TRANSLATION_KEY_PREFIX + ".mana_cost_continuous", getSpell().getCost());
		if(!discovered) manaCost = I18n.format(TRANSLATION_KEY_PREFIX + ".mana_cost_undiscovered");
		this.fontRenderer.drawString(manaCost, left + 17, top + 69, 0);

		if(discovered){
			this.fontRenderer.drawSplitString(getSpell().getDescription(), left + 17, top + 83, 118, 0);
		}else{
			this.mc.standardGalacticFontRenderer.drawSplitString(
					SpellGlyphData.getGlyphDescription(getSpell(), mc.world), left + 17, top + 83, 118, 0);
		}
	}

	@Override
	public void initGui(){
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.MISC_BOOK_OPEN, 1));
	}

	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public boolean doesGuiPauseGame(){
		return Wizardry.settings.booksPauseGame;
	}

}
