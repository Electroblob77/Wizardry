package electroblob.wizardry.client.gui.handbook;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.JsonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Instances of this class represent tables of contents in the wizard's handbook. Each {@link Section} can have a
 * single table of contents, which can reference any other sections in the handbook (though it is normal to list
 * top-level sections in a main contents and have subsections listed in their respective parent sections' contents).
 *
 * This class handles JSON parsing, formatting and drawing of the contents itself, working on a line-by-line basis
 * (as opposed to sections, which work on a page-by-page basis). It also stores its own list of buttons.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
class Contents {

	// Final fields are mandatory, the rest are optional
	final String id;
	final Section section;
	private boolean hyperlinks = true;
	private boolean pageNumbers = true;
	private String separator = ".";
	// Derived fields, not specifically defined in JSON
	private int startPage;
	private int startLine;
	private final List<List<GuiButton>> buttons;

	private final List<Section> entries;

	private List<Section> visibleEntries;

	private Contents(String id, Section section){
		this.id = id;
		this.section = section;
		this.entries = new ArrayList<>();
		this.buttons = new ArrayList<>();
		this.visibleEntries = new ArrayList<>();
	}

	/** Returns an unmodifiable, flattened collection of all the buttons in this contents. */
	Collection<GuiButton> getButtons(){
		return WizardryUtilities.flatten(buttons);
	}

	void addEntry(Section section){
		entries.add(section);
	}

	/**
	 * Draws this contents for the given double-page spread and shows/hides buttons accordingly. Will draw nothing
	 * if the given page is outside of this contents.
	 *
	 * @param font       The font renderer object.
	 * @param doublePage The index of the <b>double-page</b> to be drawn.
	 * @param left       The x coordinate of the left side of the GUI.
	 * @param top        The y coordinate of the top of the GUI.
	 */
	void draw(FontRenderer font, int doublePage, int left, int top){

		// Show/hide buttons

		int i = 0;

		for(List<GuiButton> list : buttons){
			final int i1 = i++;
			list.forEach(b -> b.visible = GuiWizardHandbook.singleToDoublePage(startPage + i1) == doublePage);
		}

		if(!pageNumbers) return; // No page numbers means only the buttons are drawn

		// FONT_HEIGHT may change between fonts, so this is calculated here. With the default font it's 14.
		final int maxLineNumber = GuiWizardHandbook.PAGE_HEIGHT / font.FONT_HEIGHT;

		int leftIndex = GuiWizardHandbook.doubleToSinglePage(doublePage, false);
		// Relative indices of the pages to be rendered - often these will be outside the section entirely
		int[] visiblePages = {leftIndex - startPage, leftIndex - startPage + 1};

		for(int page : visiblePages){

			if(page >= 0 && page < visibleEntries.size() / maxLineNumber + 1){

				int x = left + (GuiWizardHandbook.isRightPage(startPage + page) ? GuiWizardHandbook.GUI_WIDTH - GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH : GuiWizardHandbook.TEXT_INSET_X);
				int y = top + GuiWizardHandbook.TEXT_INSET_Y + startLine * font.FONT_HEIGHT;

				for(Section entry : this.visibleEntries){

					if(entry.isUnlocked()){

						int nameWidth = font.getStringWidth(entry.title);

						String dotsAndNumber = " " + entry.startPage;

						while(font.getStringWidth(dotsAndNumber) < GuiWizardHandbook.PAGE_WIDTH - nameWidth - 2){
							dotsAndNumber = separator + dotsAndNumber;
						}

						font.drawString(dotsAndNumber, x + GuiWizardHandbook.PAGE_WIDTH - font.getStringWidth(dotsAndNumber), y, DrawingUtils.BLACK, false);

						if(!hyperlinks) font.drawString(entry.title, x, y, DrawingUtils.BLACK, false);

						y += font.FONT_HEIGHT;
					}
				}
			}
		}
	}

	/**
	 * Called on GUI load to format the section and all subsections, contents tables and other elements. Does not
	 * perform any actual drawing.
	 *
	 * @param font      The font renderer object, for measurement purposes.
	 * @param startPage The index of the first page (single side, not double-page) of this section.
	 * @param startLine The index of the first line of this contents.
	 * @param left      The x coordinate of the left side of the GUI.
	 * @param top       The y coordinate of the top of the GUI.
	 * @return The number of lines this contents takes up.
	 * @throws JsonSyntaxException if at any point the formatting is found to be invalid.
	 */
	int format(FontRenderer font, int startPage, int startLine, int left, int top){

		this.buttons.clear();

		this.visibleEntries = new ArrayList<>(entries); // Need to copy the collection first!

		this.visibleEntries.removeIf(s -> !s.isUnlocked());

		if(hyperlinks){

			// FONT_HEIGHT may change between fonts, so this is calculated here. With the default font it's 14.
			final int maxLineNumber = GuiWizardHandbook.PAGE_HEIGHT / font.FONT_HEIGHT;

			this.startPage = startPage;
			this.startLine = startLine;

			List<GuiButton> list = new ArrayList<>(maxLineNumber);

			for(Section entry : this.visibleEntries){

				int x = GuiWizardHandbook.isRightPage(startPage) ? left + GuiWizardHandbook.GUI_WIDTH - GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH : left + GuiWizardHandbook.TEXT_INSET_X;
				int y = top + GuiWizardHandbook.TEXT_INSET_Y + startLine * font.FONT_HEIGHT;

				list.add(new GuiButtonHyperlink.Internal(0, x, y, font, entry.title, entry, 0, "", maxLineNumber-startLine, GuiWizardHandbook.isRightPage(startPage)));

				startLine++;

				if(startLine == maxLineNumber){
					startLine = 0;
					startPage++;
					buttons.add(list);
					list = new ArrayList<>(maxLineNumber); // If there are no more entries this will be discarded anyway
				}
			}

			buttons.add(list);
		}

		// Returning this is kind of trivial at the moment but if we ever wanted to add a header or something,
		// it would be more useful.
		return visibleEntries.size();
	}

	/**
	 * Parses the given JSON object and constructs a new {@code Contents} from it, setting all the relevant fields
	 * and references.
	 *
	 * @param parent The parent section for this contents.
	 * @param json A JSON object representing the contents to be constructed. This must contain at least an "id"
	 *             string.
	 * @return The resulting {@code Contents} object.
	 * @throws JsonSyntaxException if at any point the JSON object is found to be invalid.
	 */
	static Contents fromJson(Section parent, JsonObject json){

		Contents contents = new Contents(JsonUtils.getString(json, "id"), parent);

		contents.hyperlinks = JsonUtils.getBoolean(json, "hyperlinks", true);
		contents.pageNumbers = JsonUtils.getBoolean(json, "page_numbers", true);
		contents.separator = JsonUtils.getString(json, "separator", ".");

		return contents;
	}
}
