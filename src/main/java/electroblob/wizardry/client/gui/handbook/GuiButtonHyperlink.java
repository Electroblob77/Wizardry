package electroblob.wizardry.client.gui.handbook;

import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiButtonHyperlink extends GuiButton {

	public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

	/** Pulse period of links to new sections, in milliseconds. */
	private static final float PULSATION_PERIOD = 1500;

	final int indent;
	final List<String> lines;
	final int linesLeft;

	GuiButtonHyperlink(int id, int x, int y, FontRenderer font, String text, int indent, String suffix, int linesLeft, boolean rightPage){

		super(id, x, y, font.getStringWidth(text), font.FONT_HEIGHT, text);

		// Sometimes a link has punctuation or something after it that causes it to wrap onto a new line
		String linkWithSuffix = text + suffix;

		// If the string won't fit any words at the end of the current line, treat it as if we started a new line
		if(font.getStringWidth(linkWithSuffix.split("\\s")[0]) > GuiWizardHandbook.PAGE_WIDTH - indent){
			indent = 0;
			this.y += font.FONT_HEIGHT;
		}

		this.indent = indent; // Assigned here in case it was corrected above
		this.linesLeft = linesLeft;

		String line1 = font.listFormattedStringToWidth(linkWithSuffix, GuiWizardHandbook.PAGE_WIDTH - indent).get(0);
		// Without trim(), there will be at least 1 leading space due to the custom wrapping
		String remainder = linkWithSuffix.substring(line1.length()).trim();

		// ... then wrap the rest to the normal width.
		lines = new ArrayList<>();
		lines.add(line1);
		// Some links are only one line, if this wasn't checked they would cause a StackOverflowError
		if(!remainder.isEmpty()) lines.addAll(font.listFormattedStringToWidth(remainder, GuiWizardHandbook.PAGE_WIDTH));

		// Removes the suffix if it exists (ugly as heck, but it works)
		if(!suffix.isEmpty()){
			for(int i=lines.size()-1; i>=0; i--){
				String line = lines.get(i);
				if(suffix.endsWith(line)){
					lines.remove(i);
				}else if(line.endsWith(suffix)){
					lines.set(i, line.substring(0, line.length() - suffix.length()));
					break;
				}
			}
		}

		// Remove any lines that overflowed onto the next double-page
		if(rightPage){
			while(lines.size() > linesLeft) lines.remove(lines.size() - 1);
		}
	}

	public boolean isHovered(net.minecraft.client.gui.FontRenderer font, int mouseX, int mouseY){

		int i = 0;

		for(String line : lines){

			int l = x;
			if(i == 0) l += indent;

			int t = y + font.FONT_HEIGHT * i;

			if(i > linesLeft){
				l = l + GuiWizardHandbook.GUI_WIDTH - 2 * GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH;
				t -= GuiWizardHandbook.PAGE_HEIGHT - (GuiWizardHandbook.PAGE_HEIGHT % font.FONT_HEIGHT);
			}

			if(mouseX >= l && mouseY >= t && mouseX < l + font.getStringWidth(line) && mouseY < t + font.FONT_HEIGHT){
				return true;
			}

			i++;
		}

		return false;
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY){
		return this.enabled && this.visible && isHovered(minecraft.fontRenderer, mouseX, mouseY);
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){

		if(this.visible){

			this.hovered = isHovered(minecraft.fontRenderer, mouseX, mouseY);

			int i = 0;

			for(String line : lines){

				int l = x;
				if(i == 0) l += indent;

				int t = y + minecraft.fontRenderer.FONT_HEIGHT * i;

				if(i > linesLeft){
					l = l + GuiWizardHandbook.GUI_WIDTH - 2 * GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH;
					t -= GuiWizardHandbook.PAGE_HEIGHT - (GuiWizardHandbook.PAGE_HEIGHT % minecraft.fontRenderer.FONT_HEIGHT);
				}

				minecraft.fontRenderer.drawString(line, l, t, getColour());

				i++;
			}
		}
	}

	protected int getColour(){
		return hovered ? GuiWizardHandbook.colours.get("highlight") : GuiWizardHandbook.colours.get("hyperlink");
	}

	/**
	 * Creates a new hyperlink button from the given arguments, automatically differentiating between URLs and sections.
	 * @param x The x position of the button
	 * @param y The y position of the button
	 * @param font A reference to the FontRenderer object
	 * @param upToLink The paragraph (as a list of lines) up to the link, used to determine positioning and word wrap
	 * @param arguments The link arguments - that is, everything between the two @ signs, split by spaces
	 * @param suffix The text directly after the link, up to the first whitespace; used for word wrap. Usually this is
	 *               either empty or contains a single punctuation mark.
	 * @return The resulting button
	 * @throws IllegalArgumentException if the given argument array is empty or contains more than 2 arguments
	 * @throws JsonSyntaxException if the specified link target is not a URL or a valid section ID
	 */
	public static GuiButtonHyperlink create(int x, int y, FontRenderer font, List<String> upToLink, String[] arguments, String suffix, int linesLeft, boolean rightPage){

		if(arguments.length == 0 || arguments.length > 2) throw new IllegalArgumentException("Incorrect array length!");

		GuiButtonHyperlink button;

		if(arguments[0].matches(URL_REGEX)){

			button = new GuiButtonHyperlink.External(0, x, y, font, arguments[arguments.length - 1], arguments[0],
					font.getStringWidth(upToLink.get(upToLink.size() - 1)), suffix, linesLeft, rightPage);

		}else{

			Section target = GuiWizardHandbook.sections.get(arguments[0]);

			if(target == null) throw new JsonSyntaxException("Hyperlink points to nonexistent section id " + arguments[0]);

			button = new GuiButtonHyperlink.Internal(0, x, y, font, arguments[arguments.length - 1],
					target, font.getStringWidth(upToLink.get(upToLink.size() - 1)), suffix, linesLeft, rightPage);
		}

		return button;
	}

	static class Internal extends GuiButtonHyperlink {

		final Section target;

		Internal(int id, int x, int y, FontRenderer font, String text, Section target, int indent, String suffix, int linesLeft, boolean rightPage){
			super(id, x, y, font, text, indent, suffix, linesLeft, rightPage);
			this.target = target;
		}

		@Override
		public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY){
			if(!target.isUnlocked()) return false;
			return super.mousePressed(minecraft, mouseX, mouseY);
		}

		@Override
		public void playPressSound(SoundHandler soundHandler){
			soundHandler.playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.MISC_PAGE_TURN, 1));
		}

		@Override
		protected int getColour(){

			if(!target.isUnlocked()) return GuiWizardHandbook.colours.get("text");

			if(!hovered && target.isNew() && !Minecraft.getMinecraft().player.isCreative()){

				int c = GuiWizardHandbook.colours.get("new_section");
				int d = GuiWizardHandbook.colours.get("hyperlink");
				float f = (MathHelper.sin((Minecraft.getSystemTime() % PULSATION_PERIOD) / PULSATION_PERIOD * 2 * (float)Math.PI) + 1) / 2f;

				return DrawingUtils.mix(c, d, f);
			}

			return super.getColour();
		}

	}

	static class External extends GuiButtonHyperlink {

		final ITextComponent link;

		External(int id, int x, int y, FontRenderer font, String text, String url, int indent, String suffix, int linesLeft, boolean rightPage){
			super(id, x, y, font, text, indent, suffix, linesLeft, rightPage);
			this.link = new TextComponentString(text);
			link.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url)).setColor(TextFormatting.DARK_BLUE);
		}

	}

}
