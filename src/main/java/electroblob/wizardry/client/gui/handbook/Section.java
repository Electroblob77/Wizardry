package electroblob.wizardry.client.gui.handbook;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Instances of this class represent sections in the wizard's handbook. As of wizardry 4.2, this class handles
 * everything within the section itself, including JSON parsing, unlock triggers and drawing the actual rawText.
 * Sections may now also be nested and have other elements within them, such as images and a table of contents, a
 * behaviour which is also handled within this class.
 * <p>
 * The formatting of the book is now done 'dynamically' - that is, the exact positions and page numbers of
 * sections, images and so on are determined on GUI load and depend on which of the previous sections have been
 * unlocked, amongst other factors. This means that all of the unlocked sections must be formatted in order on GUI
 * load, so that each section knows the previous section's length and therefore where to start.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
// Because these are now generated on resource pack reload (not on handbook open, as before), this class must now
// be static
class Section {

	// Final fields are mandatory (none here though), the rest are optional
	String title;
	private String[] rawText;
	private Contents contents;
	private Advancement[] triggers;
	private Map<String, Section> subsections;
	private boolean centreX, centreY;

	// Derived fields, not explicitly defined in JSON
	/** The <b>single-page</b> index of the first page of this section. */
	int startPage;
	private final List<List<GuiButton>> buttons;
	/**
	 * A list of <b>single</b> pages, which are themselves lists of paragraphs (each paragraph is a single
	 * string which may include line breaks and other escape characters).
	 */
	private final List<List<String>> pages;

	private Section(){
		this.buttons = new ArrayList<>();
		this.pages = new ArrayList<>();
		this.subsections = new LinkedHashMap<>();
	}

	Collection<GuiButton> getButtons(){
		return WizardryUtilities.flatten(buttons);
	}

	/**
	 * Returns true if the given page is within this section, false if not.
	 */
	boolean containsPage(int page){
		return startPage <= page && startPage + pages.size() > page;
	}

	/**
	 * Returns true if this section is unlocked for the client player, false if not. Always returns true if
	 * handbook progression is disabled in the config.
	 */
	boolean isUnlocked(){

		if(!Wizardry.settings.handbookProgression) return true; // Always unlocked if handbook progression is off

		// A section is automatically unlocked if one of its subsections is unlocked
		for(Section subsection : subsections.values()){
			if(subsection.isUnlocked()) return true;
		}

		if(triggers == null) return true; // If no triggers were defined, the section is unlocked from the start

		for(Advancement trigger : triggers){

			try{

				// TESTME: Is this properly synced or do we need to do that as well?
				Map<Advancement, AdvancementProgress> advancements = (Map)GuiWizardHandbook.ADVANCEMENT_TO_PROGRESS.get(Minecraft.getMinecraft().player.connection.getAdvancementManager());

				if(advancements.get(trigger).isDone()){
					return true;
				}

			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * Actually draws the contents of the given section for the given double-page spread. Will do nothing if the
	 * given page is outside of this section.
	 *
	 * @param font       The font renderer object.
	 * @param doublePage The index of the <b>double-page</b> to be drawn.
	 * @param left       The x coordinate of the left side of the GUI.
	 * @param top        The y coordinate of the top of the GUI.
	 */
	// This method is supposed to be 'idiot-proof' in the sense that the code calling it need not check whether the
	// section actually needs drawing, so it can just dumbly call draw(...) for all the sections in order.
	void draw(FontRenderer font, int doublePage, int left, int top){

		// Show/hide buttons

		int i = 0;

		for(List<GuiButton> list : buttons){
			final int i1 = i++;
			list.forEach(b -> b.visible = GuiWizardHandbook.singleToDoublePage(startPage + i1) == doublePage);
		}

		int leftIndex = GuiWizardHandbook.doubleToSinglePage(doublePage, false);
		// Relative indices of the pages to be rendered - often these will be outside the section entirely
		int[] visiblePages = {leftIndex - startPage, leftIndex - startPage + 1};

		for(int page : visiblePages){

			if(page >= 0 && page < pages.size()){

				List<String> lines = pages.get(page);

				int x = left + (GuiWizardHandbook.isRightPage(startPage + page) ? GuiWizardHandbook.GUI_WIDTH - GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH : GuiWizardHandbook.TEXT_INSET_X);
				int y = top + GuiWizardHandbook.TEXT_INSET_Y;
				if(centreY) y += GuiWizardHandbook.PAGE_HEIGHT / 2 - lines.size() / 2 * font.FONT_HEIGHT;

				for(String line : lines){

					int lx = centreX ? x + GuiWizardHandbook.PAGE_WIDTH / 2 - font.getStringWidth(line) / 2 : x;

					font.drawString(line, lx, y, DrawingUtils.BLACK, false);

					y += font.FONT_HEIGHT;
				}
			}
		}
	}

	/**
	 * Called on GUI load to format the section, contents tables and other elements, <b>excluding</b> subsections.
	 * Does not perform any actual drawing.
	 *
	 * @param font      The font renderer object, for measurement purposes.
	 * @param startPage The index of the first page (single side, not double-page) of this section.
	 * @param left      The x coordinate of the left side of the GUI.
	 * @param top       The y coordinate of the top of the GUI.
	 * @return The <b>single-page</b> index of the next blank page after the end of this section.
	 * @throws JsonSyntaxException if at any point the formatting is found to be invalid.
	 */
	int format(FontRenderer font, int startPage, int left, int top){

		this.buttons.clear();
		this.pages.clear();

		// FONT_HEIGHT may change between fonts, so this is calculated here. With the default font it's 14.
		final int maxLineNumber = GuiWizardHandbook.PAGE_HEIGHT / font.FONT_HEIGHT;

		this.startPage = startPage;

		// First everything is added to a single list of lines, then it is split into pages.
		List<String> lines = new ArrayList<>();

		// Adds the header if present
		if(!this.title.isEmpty()){
			lines.add(this.title);
			lines.add(GuiWizardHandbook.rulerText);
		}

		// Adds space for the contents if it exists
		if(this.contents != null){
			lines.addAll(Collections.nCopies(this.contents.format(font, startPage, lines.size(), left, top), ""));
			// Line break between contents and first paragraph
			if((lines.size() % maxLineNumber) != 0) lines.add("");
		}

		if(this.rawText != null){
			// Paragraphs are defined as a JSON list because it makes it easier to arrange them properly across pages
			// - using multiple line breaks would mean having to find and remove them when at the top of a page.
			for(String paragraph : this.rawText){

				// (lines.size() % maxLineNumber) gives the number of lines on the current page
				// (lines.size() / maxLineNumber) gives the index of the current page minus the value of startPage

				// Formats the paragraph

				String raw = paragraph; // For error messages

				// Images (images must be separate paragraphs)

				if(paragraph.startsWith(GuiWizardHandbook.FORMAT_MARKER + GuiWizardHandbook.IMAGE_TAG)){

					String[] arguments = paragraph.split("\\s", 2);

					if(arguments.length < 2) throw new JsonSyntaxException("Missing image name in string "
							+ StringUtils.abbreviate(raw, 50));

					Image image = GuiWizardHandbook.images.get(arguments[1]);
					if(image == null) throw new JsonSyntaxException("Image with id " + arguments[1] + "is undefined");

					// Starts a new page if the image will not fit on the current one
					if((lines.size() % maxLineNumber) * font.FONT_HEIGHT + image.getHeight(font) > GuiWizardHandbook.PAGE_HEIGHT){
						// Remaining number of lines on the page
						lines.addAll(Collections.nCopies(maxLineNumber - (lines.size() % maxLineNumber), ""));
					}

					if(image.getWidth() > GuiWizardHandbook.PAGE_WIDTH) Wizardry.logger.warn("Image with id " + arguments[1]
							+ "has a width (" + image.getWidth() + ") greater than the maximum page width (" + GuiWizardHandbook.PAGE_WIDTH
							+ "), it will extend beyond the page area.");

					if(image.getHeight(font) > GuiWizardHandbook.PAGE_HEIGHT) Wizardry.logger.warn("Image with id " + arguments[1]
							+ "has a height (" + image.getHeight(font) + ") greater than the maximum page height (" + GuiWizardHandbook.PAGE_HEIGHT
							+ "), it will extend beyond the page area.");

					int page = startPage + (lines.size() / maxLineNumber);

					image.addInstance(page, GuiWizardHandbook.PAGE_WIDTH / 2 - image.getWidth() / 2
							+ (GuiWizardHandbook.isRightPage(page) ? GuiWizardHandbook.GUI_WIDTH - GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH : GuiWizardHandbook.TEXT_INSET_X),
							GuiWizardHandbook.TEXT_INSET_Y + (lines.size() % maxLineNumber) * font.FONT_HEIGHT);

					// Height of the image in lines, rounded up
					// Uses a single space instead of an empty string so that the page trimming doesn't remove them
					lines.addAll(Collections.nCopies(image.getHeight(font) / font.FONT_HEIGHT, " "));
					lines.add(""); // The last one is removable though, since it's actually extra space

				// Recipes (recipes must be separate paragraphs)
				}else if(paragraph.startsWith(GuiWizardHandbook.FORMAT_MARKER + GuiWizardHandbook.RECIPE_TAG)){

					String[] arguments = paragraph.split("\\s", 2);

					if(arguments.length < 2) throw new JsonSyntaxException("Missing recipe name in string "
							+ StringUtils.abbreviate(raw, 50));

					CraftingRecipe recipe = GuiWizardHandbook.recipes.get(arguments[1]);
					if(recipe == null) throw new JsonSyntaxException("Recipe with id " + arguments[1] + "is undefined");

					// Starts a new page if the recipe will not fit on the current one
					if((lines.size() % maxLineNumber) * font.FONT_HEIGHT + CraftingRecipe.HEIGHT > GuiWizardHandbook.PAGE_HEIGHT){
						// Remaining number of lines on the page
						lines.addAll(Collections.nCopies(maxLineNumber - (lines.size() % maxLineNumber), ""));
					}

					int page = startPage + (lines.size() / maxLineNumber);

					recipe.addInstance(page, GuiWizardHandbook.PAGE_WIDTH / 2 - CraftingRecipe.WIDTH / 2
									+ (GuiWizardHandbook.isRightPage(page) ? GuiWizardHandbook.GUI_WIDTH - GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH : GuiWizardHandbook.TEXT_INSET_X),
							GuiWizardHandbook.TEXT_INSET_Y + (lines.size() % maxLineNumber) * font.FONT_HEIGHT);

					// Height of the recipe in lines, rounded up
					// Uses a single space instead of an empty string so that the page trimming doesn't remove them
					lines.addAll(Collections.nCopies(CraftingRecipe.HEIGHT / font.FONT_HEIGHT, " "));
					// This time we're not adding an extra space because it's not really needed

				}else{ // All other paragraphs

					// Hyperlinks

					int linkStart;

					while((linkStart = paragraph.indexOf(GuiWizardHandbook.HYPERLINK_MARKER)) > -1){ // Ooh an assignment and a comparison in one...

						int linkEnd = paragraph.indexOf(GuiWizardHandbook.HYPERLINK_MARKER, linkStart + 1);

						if(linkEnd < 0) throw new JsonSyntaxException("Un-closed hyperlink marker in string "
								+ StringUtils.abbreviate(raw, 50));

						List<String> upToLink = font.listFormattedStringToWidth(paragraph.substring(0, linkStart), GuiWizardHandbook.PAGE_WIDTH);

						String linkRaw = paragraph.substring(linkStart, linkEnd + 1);
						String[] arguments = paragraph.substring(linkStart + 1, linkEnd).split("\\s", 2);
						String suffix = paragraph.substring(linkEnd).split("\\s", 2)[0];

						// The index of the single page currently being formatted, relative to the section
						int pageRelative = (lines.size() + upToLink.size() - 1) / maxLineNumber;
						// The overall index of the single page currently being formatted
						int page = startPage + pageRelative;

						int x = GuiWizardHandbook.isRightPage(page) ? left + GuiWizardHandbook.GUI_WIDTH - GuiWizardHandbook.TEXT_INSET_X - GuiWizardHandbook.PAGE_WIDTH : left + GuiWizardHandbook.TEXT_INSET_X;
						int y = top + GuiWizardHandbook.TEXT_INSET_Y + (((lines.size() + upToLink.size() - 1) % maxLineNumber)) * font.FONT_HEIGHT;

						// Adds any missing sub-lists
						while(this.buttons.size() <= pageRelative){
							this.buttons.add(new ArrayList<>());
						}

						// The button id only does what you use it for, so we're just not using it at all.
						this.buttons.get(pageRelative).add(GuiButtonHyperlink.create(x, y, font, upToLink, arguments, suffix));

						// The link button should exactly overlay the display rawText in the main string
						// If the link has no display rawText specified, it displays the unformatted target string
						paragraph = paragraph.replace(linkRaw, arguments[arguments.length - 1]);
					}

					// Formatting
					for(Map.Entry<String, String> entry : GuiWizardHandbook.FORMAT_TAGS.entrySet()){
						paragraph = paragraph.replace(GuiWizardHandbook.FORMAT_MARKER + entry.getKey(), entry.getValue());
					}

					lines.addAll(font.listFormattedStringToWidth(paragraph, GuiWizardHandbook.PAGE_WIDTH));
				}

				// Line break between paragraphs (the last one will just be deleted later)
				if((lines.size() % maxLineNumber) != 0) lines.add("");
			}
		}

		// Splits lines into pages

		List<String> page = new ArrayList<>();
		pages.add(page);

		while(!lines.isEmpty()){

			if(page.size() == maxLineNumber){
				// Removes blank lines at the end of the page
				while(page.get(page.size() - 1).isEmpty()) page.remove(page.size() - 1);
				// Adds a new page
				pages.add(page = new ArrayList<>());
			}

			String line = lines.remove(0);

			// Prevents blank lines at the start of the page
			if(!page.isEmpty() || !line.isEmpty()) page.add(line);
		}

		return startPage + pages.size();
	}

	/**
	 * Parses the given JSON object and constructs a new {@code Section} from it, setting all the relevant fields
	 * and references. This method converts the JSON object to a {@code Section} object and retrieves any resources;
	 * the section is not formatted in any way until GUI load, in {@link Section#format(FontRenderer, int, int, int)}.
	 *
	 * @param json A JSON object representing the section to be constructed. This must contain at least a "title"
	 *             string.
	 * @return The resulting {@code Section} object.
	 * @throws JsonSyntaxException if at any point the JSON object is found to be invalid.
	 */
	static Section fromJson(JsonObject json){

		Section section = new Section();

		section.title = JsonUtils.getString(json, "title", "");

		if(JsonUtils.hasField(json, "include_in_contents")){

			String id = JsonUtils.getString(json, "include_in_contents");

			Contents belongsTo = GuiWizardHandbook.contentsList.get(id);

			if(belongsTo == null){
				throw new JsonSyntaxException("Expected include_in_contents to be the id of a previously defined contents, but no contents with the id " + id + " exists yet.");
			}else{
				belongsTo.addEntry(section);
			}
		}

		if(JsonUtils.hasField(json, "contents")){
			section.contents = Contents.fromJson(JsonUtils.getJsonObject(json, "contents"));
			GuiWizardHandbook.contentsList.put(section.contents.id, section.contents);
		}

		if(JsonUtils.hasField(json, "text")){
			// TESTME: Does this always preserve order?
			section.rawText = Streams.stream(JsonUtils.getJsonArray(json, "text"))
					.map(e -> JsonUtils.getString(e, "element of array rawText"))
					.toArray(String[]::new);
		}

		// TODO: Triggers

		if(JsonUtils.hasField(json, "centre")){
			JsonObject centre = JsonUtils.getJsonObject(json,"centre");
			section.centreX = JsonUtils.getBoolean(centre, "x", false);
			section.centreY = JsonUtils.getBoolean(centre, "y", false);
		}

		// The only benefit of having subsections (other than logical grouping) is that the parent section can
		// automatically be unlocked if one of the subsections is.
		if(JsonUtils.hasField(json, "sections")){
			populate(section.subsections, json);
		}

		return section;
	}

	static void populate(Map<String, Section> map, JsonObject json){

		JsonObject sectionsObject = JsonUtils.getJsonObject(json, "sections");

		// Need to iterate over these since we don't know what they're called or how many there are
		for(Map.Entry<String, JsonElement> entry : sectionsObject.entrySet()){

			String key = entry.getKey(); // Find out what each element is called, this will be the sections map key

			Section section = fromJson(entry.getValue().getAsJsonObject());
			map.put(key, section);
			map.putAll(section.subsections);
		}
	}

	public void onAdvancement(EntityPlayer player, Advancement advancement){
		Minecraft minecraft = Minecraft.getMinecraft();
		//System.out.println(title);
		if(triggers != null && Arrays.asList(triggers).contains(advancement) && player == minecraft.player){
			//minecraft.getToastGui().drawToast(IToast);

			// Debug
			//System.out.println(title);
		}
	}
}
