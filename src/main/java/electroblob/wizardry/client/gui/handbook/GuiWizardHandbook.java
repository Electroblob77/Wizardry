package electroblob.wizardry.client.gui.handbook;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.ClientProxy;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.client.gui.GuiButtonInvisible;
import electroblob.wizardry.client.gui.GuiButtonTurnPage;
import electroblob.wizardry.client.gui.GuiButtonTurnPage.Type;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.packet.PacketRequestAdvancementSync;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.*;

/**
 * GUI class for the wizard's handbook. Like any GUI class, this is instantiated each time the book is opened. As of
 * Wizardry 4.2, the handbook text is defined as a JSON file rather than a plain text file, and is loaded only on
 * resource pack reload, rather than every time the book is opened. This means all the data structures (sections, images,
 * etc.) are built before the GUI instance exists at all. However, since some things depend on positioning, these have to
 * be initialised on GUI creation. (Previously, everything was done on GUI load)
 *
 * @author Electroblob
 * @since Wizardry 1.0
 * @see Section
 * @see Contents
 * @see Image
 * @see CraftingRecipe
 */
public class GuiWizardHandbook extends GuiScreen {

	private static final ResourceLocation DEFAULT = new ResourceLocation(Wizardry.MODID, "texts/handbook_en_us.json");

	static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/gui/handbook/handbook.png");

	/** Global Gson instance for the handbook. */
	private static final Gson gson = new Gson();

	// Formatting markup

	static final char FORMAT_MARKER = '#';
	static final char HYPERLINK_MARKER = '@';

	static final String IMAGE_TAG = "image";
	static final String RECIPE_TAG = "recipe";
	static final String RULER_TAG = "ruler";

	static final Map<String, String> FORMAT_TAGS = new HashMap<>();

	// Dimension constants
	// Private constants are not relevant to book elements, package-protected ones are

	/** The dimensions of the rendered GUI area. */
	static final int GUI_WIDTH = 288, GUI_HEIGHT = 180;
	/** The dimensions of the GUI texture itself. */
	static final int TEXTURE_WIDTH = 512, TEXTURE_HEIGHT = 256;
	/** The dimensions of the area of a single page in which text can be drawn. */
	static final int PAGE_WIDTH = 120, PAGE_HEIGHT = 140;
	/** The distance of the text from the top outside corner of each page. */
	static final int TEXT_INSET_X = 17, TEXT_INSET_Y = 16;
	/** The distance of the buttons from the bottom outside corners of the GUI. */
	private static final int BUTTON_INSET_X = 22, BUTTON_INSET_Y = 13;
	/** The distance between adjacent buttons. */
	private static final int BUTTON_SPACING = 20;
	/** The distance of the page numbers from the bottom of the GUI. */
	private static final int PAGE_NUMBER_INSET = 22;

	// IDEA: Constant dimensions could be converted to JSON like the spell HUD ones

	// Global variables

	/**
	 * The <b>double-page</b> currently being viewed. Each double-page spread counts as a single page, with the inside
	 * of the front cover being page 0.
	 */
	private int currentPage = 0;
	/**
	 * The number of <b>single</b> pages currently in the book. This is calculated on GUI load based on visible sections.
	 */
	private int pageCount = 1; // Starts at 1 because the first single-page is the inside of the cover
	/**
	 * The <b>double-page</b> number where the bookmark is currently set, <b>relative to the section stored in
	 * {@link GuiWizardHandbook#bookmarkSection}</b>. Static because it persists when the book is closed.
	 */
	private static int bookmarkPage = 0;
	/**
	 * The key corresponding to the section in which the bookmark is currently set. Static because it persists when the
	 * book is closed. Storing a section means the bookmark doesn't change location when new sections are unlocked.
	 */
	private static String bookmarkSection;

	// Buttons
	private GuiButton bookmark, next, previous, nextSection, previousSection, menu;

	// Handbook content

	// As a general rule, I prefer to make static final fields lowercase if they're collections that change, because even
	// though the collection itself is constant, the stuff in it is not, so being lowercase highlights this difference.

	/**
	 * A map which stores all loaded section objects, including subsections. This gets wiped on resource pack reload and
	 * repopulated with mappings as specified by the handbook JSON file for the current language. The keys in the map
	 * correspond to the keys in the sections object in that file, and are sorted in that order.
	 */
	static final Map<String, Section> sections = new LinkedHashMap<>();

	/**
	 * A list which stores all loaded section objects, including subsections. This is an unmodifiable list view of the
	 * values in {@link GuiWizardHandbook#sections}, sorted in the same (page number) order. This exists only to allow
	 * sections to be accessed by ordinal index for the various navigation buttons, hence why it is private.
	 */
	private static List<Section> sectionList;

	/**
	 * A map which stores all loaded contents objects. This gets wiped on resource pack reload and repopulated with
	 * mappings as specified by the handbook JSON file for the current language. The keys in the map correspond to the
	 * id strings for the contents objects in that file. This map is not sorted.
	 */
	static final Map<String, Contents> contentsList = new HashMap<>();

	/**
	 * A map which stores all loaded hex colour values. This gets wiped on resource pack reload and repopulated with
	 * mappings as specified by the handbook JSON file for the current language. The keys in the map correspond to the
	 * keys in the colours object in that file. This map is not sorted.
	 */
	static final Map<String, Integer> colours = new HashMap<>();

	/**
	 * A map which stores all loaded image objects. This gets wiped on resource pack reload and repopulated with
	 * mappings as specified by the handbook JSON file for the current language. The keys in the map correspond to the
	 * keys in the images object in that file. This map is not sorted.
	 */
	static final Map<String, Image> images = new HashMap<>();

	/**
	 * A map which stores all loaded crafting recipe objects. This gets wiped on resource pack reload and repopulated
	 * with mappings as specified by the handbook JSON file for the current language. The keys in the map correspond to
	 * the keys in the recipes object in that file. This map is not sorted.
	 */
	static final Map<String, CraftingRecipe> recipes = new HashMap<>();

	/**
	 * Adds a format tag to the handbook. All occurrences of the given tag string preceded by a # will be replaced with
	 * the result of the given value string on GUI load. The value string, therefore, can be anything that should be
	 * input dynamically, as long as it does not change while the GUI is open. Examples include wizardry's version,
	 * the various element colours and the keys assigned to wizardry's controls.
	 * @param tag The tag string, as defined in the handbook JSON file, excluding the # character. Cannot include spaces.
	 * @param value The string to replace occurrences of the given format tag with. Can include spaces but not the # character.
	 */
	public static void addFormatTag(String tag, String value){
		FORMAT_TAGS.put(tag, value);
	}

	private static void initFormatTags(){

		addFormatTag("next_spell_key", ClientProxy.NEXT_SPELL.getDisplayName());
		addFormatTag("previous_spell_key", ClientProxy.PREVIOUS_SPELL.getDisplayName());
		addFormatTag("example_charging_loss", "" + (Constants.MANA_PER_CRYSTAL - 30));
		addFormatTag("mana_per_crystal", "" + Constants.MANA_PER_CRYSTAL);
		addFormatTag("novice_max_charge", "" + Tier.NOVICE.maxCharge);
		addFormatTag("apprentice_max_charge", "" + Tier.APPRENTICE.maxCharge);
		addFormatTag("advanced_max_charge", "" + Tier.ADVANCED.maxCharge);
		addFormatTag("master_max_charge", "" + Tier.MASTER.maxCharge);
		addFormatTag("version", Wizardry.VERSION);
		addFormatTag("mcversion", "1.12.2");

		addFormatTag("colour_novice", "\u00A77");
		addFormatTag("colour_apprentice", Tier.APPRENTICE.getFormattingCode());
		addFormatTag("colour_advanced", Tier.ADVANCED.getFormattingCode());
		addFormatTag("colour_master", Tier.MASTER.getFormattingCode());

		addFormatTag("colour_fire", Element.FIRE.getFormattingCode());
		addFormatTag("colour_ice", Element.ICE.getFormattingCode());
		addFormatTag("colour_lightning", Element.LIGHTNING.getFormattingCode());
		addFormatTag("colour_necromancy", Element.NECROMANCY.getFormattingCode());
		addFormatTag("colour_earth", Element.EARTH.getFormattingCode());
		addFormatTag("colour_sorcery", Element.SORCERY.getFormattingCode());
		addFormatTag("colour_healing", Element.HEALING.getFormattingCode());

		addFormatTag("colour_reset", "\u00A70");
	}

	// Helper methods

	/**
	 * Converts the given single page index to a double-page index. Inverse of
	 * {@link GuiWizardHandbook#doubleToSinglePage(int, boolean)}.
	 *
	 * @param singlePageIndex The single-page index, which is the same as the page numbers actually displayed.
	 * @return The corresponding double-page index.
	 */
	static int singleToDoublePage(int singlePageIndex){
		// Yes, this is trivial, but if I ever change the numbering it'll be useful. It's also more descriptive.
		return singlePageIndex / 2;
	}

	/**
	 * Converts the given double-page index to a single-page index. Inverse of
	 * {@link GuiWizardHandbook#singleToDoublePage(int)}.
	 *
	 * @param doublePageIndex The double-page index, as stored in {@link GuiWizardHandbook#currentPage}.
	 * @param rightHandPage   True to return the page on the right (1 greater), false for the left-hand page.
	 * @return The corresponding single-page index.
	 */
	static int doubleToSinglePage(int doublePageIndex, boolean rightHandPage){
		return rightHandPage ? doublePageIndex * 2 + 1 : doublePageIndex * 2;
	}

	/**
	 * Returns whether the given page index refers to a right-hand page or a left-hand page.
	 *
	 * @param page The single-page index, which is the same as the page number actually displayed.
	 * @return True if the given page index refers to a right-hand page, false if it is a left-hand page.
	 */
	static boolean isRightPage(int page){
		return page % 2 == 1;
	}

	// Drawing

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){

		int left = this.width / 2 - GUI_WIDTH / 2;
		int top = this.height / 2 - GUI_HEIGHT / 2;

		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		GlStateManager.color(1, 1, 1, 1);

		// Main background
		DrawingUtils.drawTexturedRect(left, top, 0, 0, GUI_WIDTH, GUI_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// First page background
		if(currentPage == 0){
			DrawingUtils.drawTexturedRect(left, top, 368, 0, GUI_WIDTH / 2, GUI_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			previous.visible = false;
			previousSection.visible = false; // Not worth testing if we're in the first section every frame
			menu.visible = false;
		}else{
			previous.visible = true;
			previousSection.visible = true;
			menu.visible = true;
		}

		// Last page background
		if(currentPage == singleToDoublePage(pageCount)){
			DrawingUtils.drawTexturedFlippedRect(left + GUI_WIDTH / 2, top, 368, 0, GUI_WIDTH / 2, GUI_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT, true, false);
			next.visible = false;
			nextSection.visible = false;
		}else{
			next.visible = true;
			nextSection.visible = true;
		}

		// Page numbers
		if(currentPage > 0){
			String pageNumber = "" + doubleToSinglePage(currentPage, false);
			this.fontRenderer.drawString(pageNumber, left + TEXT_INSET_X + PAGE_WIDTH / 2
					- fontRenderer.getStringWidth(pageNumber)/2, top + GUI_HEIGHT - PAGE_NUMBER_INSET, DrawingUtils.BLACK);
		}
		if(currentPage < singleToDoublePage(pageCount)){
			String pageNumber = "" + doubleToSinglePage(currentPage, true);
			this.fontRenderer.drawString(pageNumber, left + GUI_WIDTH - TEXT_INSET_X - PAGE_WIDTH / 2
					- fontRenderer.getStringWidth(pageNumber)/2, top + GUI_HEIGHT - PAGE_NUMBER_INSET, DrawingUtils.BLACK);
		}

		// Main content
		contentsList.values().forEach(c -> { if(c.section.isUnlocked()) c.draw(fontRenderer, currentPage, left, top); } );
		sections.values().forEach(s -> { if(s.isUnlocked()) s.draw(fontRenderer, currentPage, left, top); } );
		// These only get populated if the sections are unlocked so no checks are necessary
		images.values().forEach(i -> i.draw(fontRenderer, currentPage, left, top));
		recipes.values().forEach(r -> r.draw(fontRenderer, itemRender, currentPage, left, top));

		// Buttons
		super.drawScreen(mouseX, mouseY, partialTicks);

		// Bookmark
		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		if(currentPage == singleToDoublePage(sections.get(bookmarkSection).startPage) + bookmarkPage){
			// If the current page is the bookmarked page, the (invisible) bookmark button is disabled
			bookmark.visible = false;
			DrawingUtils.drawTexturedRect(left + 138, top, 299, 0, 11, 191, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}else{
			bookmark.visible = true;
			bookmark.x = left + (currentPage > singleToDoublePage(sections.get(bookmarkSection).startPage) + bookmarkPage ? 130 : 147);
			DrawingUtils.drawTexturedRect(bookmark.x, top,
					bookmark.isMouseOver() ? 310 : 288, 0, 11, 191, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		// Recipe tooltips
		recipes.values().forEach(r -> r.drawTooltips(this, fontRenderer, itemRender, currentPage, left, top, mouseX, mouseY));

	}

	// GUI Initialisation / Close

	@Override
	public void onResize(Minecraft minecraft, int width, int height){
		initGui();
	}

	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void initGui(){

		super.initGui();
		Keyboard.enableRepeatEvents(true);

		initFormatTags();

		final int left = this.width / 2 - GUI_WIDTH / 2;
		final int top = this.height / 2 - GUI_HEIGHT / 2;

		recipes.values().forEach(CraftingRecipe::load);

		int nextButtonId = 0;

		this.buttonList.clear();

		this.buttonList.add(next = new GuiButtonTurnPage(nextButtonId++, left + GUI_WIDTH - BUTTON_INSET_X - GuiButtonTurnPage.WIDTH,
				top + GUI_HEIGHT - BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.NEXT_PAGE, texture, TEXTURE_WIDTH, TEXTURE_HEIGHT));

		this.buttonList.add(previous = new GuiButtonTurnPage(nextButtonId++, left + BUTTON_INSET_X,
				top + GUI_HEIGHT - BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.PREVIOUS_PAGE, texture, TEXTURE_WIDTH, TEXTURE_HEIGHT));

		this.buttonList.add(nextSection = new GuiButtonTurnPage(nextButtonId++, left + GUI_WIDTH - BUTTON_INSET_X - GuiButtonTurnPage.WIDTH - BUTTON_SPACING,
				top + GUI_HEIGHT - BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.NEXT_SECTION, texture, TEXTURE_WIDTH, TEXTURE_HEIGHT));

		this.buttonList.add(previousSection = new GuiButtonTurnPage(nextButtonId++, left + BUTTON_INSET_X + BUTTON_SPACING,
				top + GUI_HEIGHT - BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.PREVIOUS_SECTION, texture, TEXTURE_WIDTH, TEXTURE_HEIGHT));

		this.buttonList.add(menu = new GuiButtonTurnPage(nextButtonId++, left + GUI_WIDTH/2 - 28,
				top + GUI_HEIGHT - BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.CONTENTS, texture, TEXTURE_WIDTH, TEXTURE_HEIGHT));

		this.buttonList.add(bookmark = new GuiButtonInvisible(nextButtonId++, left + 130, top + 172, 11, 19) {
			@Override
			public void playPressSound(SoundHandler soundHandler){
				soundHandler.playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.MISC_PAGE_TURN, 1));
			}
		});

		pageCount = 1;

		// Clears instances of all images and recipes
		images.values().forEach(Image::clearInstances);
		recipes.values().forEach(CraftingRecipe::clearInstances);

		// Formats all the unlocked sections in order
		for(Section section : sections.values()){
			if(section.isUnlocked()){
				pageCount = section.format(this.fontRenderer, pageCount, left, top);
				buttonList.addAll(section.getButtons());
			}
		}

		contentsList.values().forEach(c -> buttonList.addAll(c.getButtons()));

		this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.MISC_BOOK_OPEN, 1));
	}

	// JSON Parsing / Data Construction

	/**
	 * Called from preInit in the main mod class (via the proxies) to initialise the handbook (parses the JSON file
	 * and constructs the relevant data structures), and again on each resource reload (changing the language triggers
	 * a resource reload).
	 */
	public static void loadHandbookFile(IResourceManager manager){

		IResource handbookFile = getHandbookResource(manager);

		if(handbookFile != null){

			// Wipes all the maps before repopulating them
			images.clear();
			sections.clear();
			contentsList.clear();
			colours.clear();

			bookmarkSection = null; // Also need to wipe the reference to the old bookmarked section

			BufferedReader reader = new BufferedReader(new InputStreamReader(handbookFile.getInputStream()));

			JsonElement je = gson.fromJson(reader, JsonElement.class);
			JsonObject json = je.getAsJsonObject();

			JsonUtils.getJsonObject(json, "colours").entrySet().forEach(e -> colours.put(e.getKey(),
					Color.decode(e.getValue().getAsString()).getRGB()));

			// Repopulates the remaining maps
			Image.populate(images, json);
			CraftingRecipe.populate(recipes, json);
			Section.populate(sections, json);

			sectionList = Collections.unmodifiableList(new ArrayList<>(sections.values()));

			if(sections.isEmpty()){
				Wizardry.logger.warn("Handbook has no sections! Aborting loading...");
				return;
			}

			bookmarkSection = JsonUtils.getString(json, "bookmark_start_section");
			if(!sections.containsKey(bookmarkSection)) throw new JsonSyntaxException("Section with id " + bookmarkSection + " is undefined");
		}

		// The first resource load on startup is done before the packet handler is loaded
		if(WizardryPacketHandler.net != null) WizardryPacketHandler.net.sendToServer(new PacketRequestAdvancementSync.Message());
	}

	/**
	 * Retrieves the handbook JSON file for the current language and returns its IResource object. If a handbook file
	 * cannot be found for the current language, a message is printed to the console and the method attempts to retrieve
	 * the default file instead (English-US). If this file cannot be found, the resulting error is printed to the
	 * console and the method returns null.
	 *
	 * @param manager The resource manager instance to use.
	 * @return The handbook JSON file, as an IResource, or null if it was not found.
	 */
	private static IResource getHandbookResource(IResourceManager manager){

		// TODO: Implement resource pack stacking to allow addon mods and texture packs to add/overwrite content

		IResource handbookFile = null;

		try{
			handbookFile = manager.getResource(new ResourceLocation(Wizardry.MODID, "texts/handbook_"
					+ Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode() + ".json"));
		}catch(IOException e){

			Wizardry.logger.info("Wizard handbook JSON file missing for the current language (" + Minecraft.getMinecraft()
					.getLanguageManager().getCurrentLanguage() + "). Using default (English-US) instead.");

			try{
				handbookFile = manager.getResource(DEFAULT);
			}catch(IOException x){
				Wizardry.logger.error("Couldn't find file: " + DEFAULT + ". The file may be missing; please try re-downloading and reinstalling Wizardry.", x);
			}
		}

		return handbookFile;
	}

	// Controls

	@Override
	protected void actionPerformed(GuiButton button){

		if(button.enabled){

			if(button == next){
				if(currentPage < singleToDoublePage(pageCount)) currentPage++;

			}else if(button == previous){
				if(currentPage > 0) currentPage--;

			}else if(button == nextSection || button == previousSection){

				Section currentSection = null;

				for(Section section : sections.values()){
					// We always want this button to do something, and taking the right-hand page means it always does
					if(section.containsPage(doubleToSinglePage(currentPage, true))){
						currentSection = section;
						break;
					}
				}

				if(currentSection != null){

					List<Section> visibleSections = new ArrayList<>(sectionList);
					visibleSections.removeIf(s -> !s.isUnlocked());

					int index = visibleSections.indexOf(currentSection);

					if(button == nextSection && index + 1 < visibleSections.size()){
						currentPage = singleToDoublePage(visibleSections.get(index + 1).startPage);
					}else if(index > 0){
						currentPage = singleToDoublePage(visibleSections.get(index - 1).startPage);
					}
				}

			}else if(button == menu){
				currentPage = singleToDoublePage(sections.get("main_contents").startPage);

			}else if(button == bookmark && bookmarkSection != null){
				currentPage = singleToDoublePage(sections.get(bookmarkSection).startPage) + bookmarkPage;

			}else{
				if(button instanceof GuiButtonHyperlink.Internal){
					currentPage = singleToDoublePage(((GuiButtonHyperlink.Internal)button).target.startPage);
				}else if(button instanceof GuiButtonHyperlink.External){
					this.handleComponentClick(((GuiButtonHyperlink.External)button).link);
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException{
		if(mouseButton == 1){
			// Right-clicking of bookmark
			if(bookmark.mousePressed(this.mc, mouseX, mouseY)){

				this.selectedButton = bookmark;

				for(String key : sections.keySet()){
					// The bookmark is assumed to bookmark the left-hand page
					if(sections.get(key).containsPage(doubleToSinglePage(currentPage, false))) bookmarkSection = key;
				}

				bookmarkPage = currentPage - singleToDoublePage(sections.get(bookmarkSection).startPage);
			}
		}else{
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	// Overridden to make it public
	@Override
	public void renderToolTip(ItemStack stack, int x, int y){
		super.renderToolTip(stack, x, y);
	}

	@Override
	public boolean doesGuiPauseGame(){
		return Wizardry.settings.booksPauseGame;
	}


	public static void updateUnlockStatus(boolean showToasts, ResourceLocation... completedAdvancements){
		sections.values().forEach(s -> s.updateUnlockStatus(showToasts, completedAdvancements));
	}

}