package electroblob.wizardry.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

public class GuiWizardHandbook extends GuiScreen {

	private int xSize, ySize;
	private int pageNumber = 0;

	private static final int PAGE_WIDTH = 120;
	/**
	 * The integer colour for black passed into the font renderer methods. This used to be 0 but that's now white for
	 * some reason, so I've made a it a constant in case it changes again.
	 */
	// I think this is actually ever-so-slightly lighter than pure black, but the difference is unnoticeable.
	private static final int BLACK = 1;

	public static final ResourceLocation regularHandbook = new ResourceLocation(Wizardry.MODID,
			"textures/gui/handbook.png");
	public static final ResourceLocation ore = new ResourceLocation(Wizardry.MODID, "textures/gui/ore_picture.png");
	public static final ResourceLocation crystal = new ResourceLocation(Wizardry.MODID,
			"textures/items/magic_crystal.png");
	public static final ResourceLocation workbenchGui = new ResourceLocation(Wizardry.MODID,
			"textures/gui/arcane_workbench.png");
	public static final ResourceLocation craftingGrids = new ResourceLocation(Wizardry.MODID,
			"textures/gui/handbook_recipes.png");

	private List<ArrayList<String>> text;
	private List<Section> sections;
	
	private static final List<Pair<ItemStack, NonNullList<NonNullList<ItemStack>>>> RECIPES = new ArrayList<>();

	private int guiPage, imagePage;

	public GuiWizardHandbook(){
		super();
		xSize = 288;
		ySize = 180;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float par3){

		int xPos = this.width / 2 - xSize / 2;
		int yPos = this.height / 2 - this.ySize / 2;

		// Tests for crafting recipes section
		if(pageNumber >= (sections.get(sections.size() - 1).pageNumber - 1) / 2
				&& pageNumber < (sections.get(sections.size() - 1).pageNumber - 1) / 2 + 4){
			Minecraft.getMinecraft().renderEngine.bindTexture(craftingGrids);
		}else{
			Minecraft.getMinecraft().renderEngine.bindTexture(regularHandbook);
		}

		WizardryUtilities.drawTexturedRect(xPos, yPos, 0, 0, xSize, ySize, xSize, 256);

		// Arcane workbench gui picture
		if(pageNumber == (this.guiPage - 1) / 2){
			Minecraft.getMinecraft().renderEngine.bindTexture(workbenchGui);
			this.drawTexturedModalRect(this.guiPage % 2 == 1 ? xPos + 17 : this.width / 2 + 7, yPos + 14, 28, 12, 120,
					118);
		}

		// Magic crystal and crystal ore images
		if(pageNumber == (this.imagePage - 1) / 2){

			Minecraft.getMinecraft().renderEngine.bindTexture(ore);
			WizardryUtilities.drawTexturedRect(this.imagePage % 2 == 1 ? xPos + 17 : this.width / 2 + 7, yPos + 80, 0,
					0, 64, 64, 64, 64);

			Minecraft.getMinecraft().renderEngine.bindTexture(crystal);
			drawTexturedStretchedRect(this.imagePage % 2 == 1 ? xPos + 17 + 64 : this.width / 2 + 7 + 62, yPos + 80, 0,
					0, 64, 64, 1, 1);

		}

		this.fontRenderer.drawString("" + (pageNumber * 2 + 1), xPos + xSize / 4 - 3, yPos + ySize - 20, 0);
		this.fontRenderer.drawString("" + (pageNumber * 2 + 2), xPos + 3 * xSize / 4 - 5, yPos + ySize - 20, 0);

		super.drawScreen(mouseX, mouseY, par3);

		int lineNumber = 0;

		if(pageNumber == 1){
			for(Section s : sections){
				s.drawContents();
			}
		}else{
			for(Section s : sections){
				s.hideButton();
			}
		}

		for(String paragraph : text.get(pageNumber * 2)){

			this.fontRenderer.drawSplitString(paragraph, xPos + 17,
					yPos + 14 + lineNumber * this.fontRenderer.FONT_HEIGHT, PAGE_WIDTH, BLACK);

			List<String> list = new ArrayList<String>(
					this.fontRenderer.listFormattedStringToWidth(paragraph, GuiWizardHandbook.PAGE_WIDTH));

			lineNumber += list.size();
		}

		lineNumber = 0;

		// Prevents crash when the last page is blank (and hence is not in the list of pages)
		if(text.size() > pageNumber * 2 + 1){
			for(String paragraph : text.get(pageNumber * 2 + 1)){

				// First page is centred
				if(pageNumber == 0){
					int startX = this.width / 2 + 7 + PAGE_WIDTH / 2
							- this.fontRenderer.getStringWidth(paragraph) / 2;
					this.fontRenderer.drawSplitString(paragraph, startX,
							yPos + 14 + lineNumber * this.fontRenderer.FONT_HEIGHT, PAGE_WIDTH, BLACK);
				}else{
					this.fontRenderer.drawSplitString(paragraph, this.width / 2 + 7,
							yPos + 14 + lineNumber * this.fontRenderer.FONT_HEIGHT, PAGE_WIDTH, BLACK);
				}

				List<String> list = new ArrayList<String>(
						this.fontRenderer.listFormattedStringToWidth(paragraph, GuiWizardHandbook.PAGE_WIDTH));

				lineNumber += list.size();
			}
		}

		// Which page of the recipes this is
		int recipePage = pageNumber - (sections.get(sections.size() - 1).pageNumber - 1) / 2;
		
		if(recipePage >= 0 && recipePage < 4){
			// 4 recipes per page, hence the recipePage*4
			this.renderCraftingRecipe(xPos + 23, yPos + 39, mouseX, mouseY, RECIPES.get(recipePage*4).getRight(), RECIPES.get(recipePage*4).getLeft());
			this.renderCraftingRecipe(xPos + 23, yPos + 98, mouseX, mouseY, RECIPES.get(recipePage*4+1).getRight(), RECIPES.get(recipePage*4+1).getLeft());
			this.renderCraftingRecipe(xPos + 156, yPos + 39, mouseX, mouseY, RECIPES.get(recipePage*4+2).getRight(), RECIPES.get(recipePage*4+2).getLeft());
			this.renderCraftingRecipe(xPos + 156, yPos + 98, mouseX, mouseY, RECIPES.get(recipePage*4+3).getRight(), RECIPES.get(recipePage*4+3).getLeft());

			// Tooltips are rendered after recipes to prevent tooltips on the left appearing behind items on the right.
			this.renderCraftingTooltips(xPos + 23, yPos + 39, mouseX, mouseY, RECIPES.get(recipePage*4).getRight(), RECIPES.get(recipePage*4).getLeft());
			this.renderCraftingTooltips(xPos + 23, yPos + 98, mouseX, mouseY, RECIPES.get(recipePage*4+1).getRight(), RECIPES.get(recipePage*4+1).getLeft());
			this.renderCraftingTooltips(xPos + 156, yPos + 39, mouseX, mouseY, RECIPES.get(recipePage*4+2).getRight(), RECIPES.get(recipePage*4+2).getLeft());
			this.renderCraftingTooltips(xPos + 156, yPos + 98, mouseX, mouseY, RECIPES.get(recipePage*4+3).getRight(), RECIPES.get(recipePage*4+3).getLeft());
		}

	}
	
	// TODO: In 1.12, this all needs redoing nicely. With the crafting system halfway through changing in 1.11.2, this
	// isn't worth doing until then.

	private void renderCraftingRecipe(int xPos, int yPos, int mouseX, int mouseY, NonNullList<NonNullList<ItemStack>> craftingGrid,
			ItemStack craftingResult){

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GlStateManager.enableLighting();
		itemRender.zLevel = 100.0F;

		for(int i = 0; i < craftingGrid.size(); i++){
			for(int j = 0; j < craftingGrid.get(i).size(); j++){
				ItemStack stack = craftingGrid.get(i).get(j);
				if(!stack.isEmpty()){
					itemRender.renderItemAndEffectIntoGUI(stack, xPos + 18 * i, yPos + 18 * j);
					itemRender.renderItemOverlays(this.fontRenderer, stack, xPos + 18 * i,
							yPos + 18 * j);
				}
			}
		}

		if(!craftingResult.isEmpty()){
			itemRender.renderItemAndEffectIntoGUI(craftingResult, xPos + 86, yPos + 18);
			itemRender.renderItemOverlays(this.fontRenderer, craftingResult, xPos + 86, yPos + 18);
		}

		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();

	}

	private void renderCraftingTooltips(int xPos, int yPos, int mouseX, int mouseY, NonNullList<NonNullList<ItemStack>> craftingGrid,
			ItemStack craftingResult){

		int guiLeft = this.width / 2 - xSize / 2;
		int guiTop = this.height / 2 - this.ySize / 2;

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		itemRender.zLevel = 0.0F;
		GlStateManager.disableLighting();

		for(int i = 0; i < craftingGrid.size(); i++){
			for(int j = 0; j < craftingGrid.get(i).size(); j++){
				ItemStack stack = craftingGrid.get(i).get(j);
				if(!stack.isEmpty()
						&& isPointInRegion(xPos + 18 * i, yPos + 18 * j, 16, 16, mouseX + guiLeft, mouseY + guiTop)){
					this.renderToolTip(stack, mouseX, mouseY);
				}
			}
		}

		if(!craftingResult.isEmpty() && isPointInRegion(xPos + 86, yPos + 18, 16, 16, mouseX + guiLeft, mouseY + guiTop)){
			this.renderToolTip(craftingResult, mouseX, mouseY);
		}

		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();

	}

	@Override
	public void initGui(){

		super.initGui();
		Keyboard.enableRepeatEvents(true);

		int nextButtonId = 0;

		this.buttonList.clear();
		this.buttonList.add(new GuiButtonTurnPage(nextButtonId++, this.width / 2 + this.xSize / 2 - 22 - 23,
				this.height / 2 + this.ySize / 2 - 10 - 13, true));
		this.buttonList.add(new GuiButtonTurnPage(nextButtonId++, this.width / 2 - this.xSize / 2 + 21,
				this.height / 2 + this.ySize / 2 - 10 - 13, false));

		text = new ArrayList<ArrayList<String>>(1);
		sections = new ArrayList<Section>(1);

		BufferedReader bufferedreader = null;

		String textFilepath = "wizardry:texts/handbook_"
				+ Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode() + ".txt";

		try{

			bufferedreader = new BufferedReader(new InputStreamReader(
					this.mc.getResourceManager().getResource(new ResourceLocation(textFilepath)).getInputStream(),
					Charsets.UTF_8));

		}catch (IOException e){

			Wizardry.logger.info(
					"Wizard handbook text file missing for the current language. Using default (English - US) instead.");

			textFilepath = "wizardry:texts/handbook_en_US.txt";

			try {

				bufferedreader = new BufferedReader(new InputStreamReader(
						this.mc.getResourceManager().getResource(new ResourceLocation(textFilepath)).getInputStream(),
						Charsets.UTF_8));

			} catch (IOException x){
				Wizardry.logger.error("Couldn't find file: wizardry:assets/texts/handbook_en_US.txt. The file may be"
						+ "missing; please try re-downloading and reinstalling Wizardry.", x);
			}
		}

		if(bufferedreader != null){

			try{

				String paragraph = bufferedreader.readLine();
				ArrayList<String> page = new ArrayList<String>(1);

				int linesPerPage = 16;

				int lineNumber = 0;

				while(paragraph != null){

					// System.out.println(paragraph);

					if(paragraph.contains("PAGEBREAK") || lineNumber >= linesPerPage){

						text.add(page);

						page = new ArrayList<String>(1);

						lineNumber = 0;

						if(paragraph.contains("PAGEBREAK")) paragraph = bufferedreader.readLine();

					}else if(paragraph.contains("LINEBREAK")){

						lineNumber++;

						page.add("");

						paragraph = bufferedreader.readLine();

					}else if(paragraph.contains("SECTION")){

						sections.add(
								new Section(paragraph.replace("SECTION ", ""), text.size() + 1, this.width / 2 + 7,
										this.height / 2 - this.ySize / 2 + 14
												+ (sections.size() + 2) * this.fontRenderer.FONT_HEIGHT,
										nextButtonId++));
						paragraph = bufferedreader.readLine();

					}else if(paragraph.contains("IMAGE")){

						if(paragraph.contains("WORKBENCH")){
							this.guiPage = text.size() + 1;
						}else if(paragraph.contains("CRYSTAL")){
							this.imagePage = text.size() + 1;
						}

						paragraph = bufferedreader.readLine();

					}else{

						paragraph = paragraph.replaceAll("NEXT_SPELL_KEY", Keyboard.getKeyName(ClientProxy.NEXT_SPELL.getKeyCode()));
						paragraph = paragraph.replaceAll("PREVIOUS_SPELL_KEY", Keyboard.getKeyName(ClientProxy.PREVIOUS_SPELL.getKeyCode()));
						paragraph = paragraph.replaceAll("MANA_PER_CRYSTAL_MINUS_30", "" + (Constants.MANA_PER_CRYSTAL - 30));
						paragraph = paragraph.replaceAll("MANA_PER_CRYSTAL", "" + Constants.MANA_PER_CRYSTAL);
						paragraph = paragraph.replaceAll("BASIC_MAX_CHARGE", "" + Tier.BASIC.maxCharge);
						paragraph = paragraph.replaceAll("APPRENTICE_MAX_CHARGE", "" + Tier.APPRENTICE.maxCharge);
						paragraph = paragraph.replaceAll("ADVANCED_MAX_CHARGE", "" + Tier.ADVANCED.maxCharge);
						paragraph = paragraph.replaceAll("MASTER_MAX_CHARGE", "" + Tier.MASTER.maxCharge);
						paragraph = paragraph.replaceAll("BASIC_COLOUR", "\u00A77");
						paragraph = paragraph.replaceAll("APPRENTICE_COLOUR", Tier.APPRENTICE.getFormattingCode());
						paragraph = paragraph.replaceAll("ADVANCED_COLOUR", Tier.ADVANCED.getFormattingCode());
						paragraph = paragraph.replaceAll("MASTER_COLOUR", Tier.MASTER.getFormattingCode());
						paragraph = paragraph.replaceAll("FIRE_COLOUR", Element.FIRE.getFormattingCode());
						paragraph = paragraph.replaceAll("ICE_COLOUR", Element.ICE.getFormattingCode());
						paragraph = paragraph.replaceAll("LIGHTNING_COLOUR", Element.LIGHTNING.getFormattingCode());
						paragraph = paragraph.replaceAll("NECROMANCY_COLOUR", Element.NECROMANCY.getFormattingCode());
						paragraph = paragraph.replaceAll("EARTH_COLOUR", Element.EARTH.getFormattingCode());
						paragraph = paragraph.replaceAll("SORCERY_COLOUR", Element.SORCERY.getFormattingCode());
						paragraph = paragraph.replaceAll("HEALING_COLOUR", Element.HEALING.getFormattingCode());
						paragraph = paragraph.replaceAll("RESET_COLOUR", "\u00A70");
						paragraph = paragraph.replaceAll("MCVERSION", "1.11.2");
						paragraph = paragraph.replaceAll("VERSION", Wizardry.VERSION);

						int linesInParagraph = this.fontRenderer
								.listFormattedStringToWidth(paragraph, GuiWizardHandbook.PAGE_WIDTH).size();

						// Ignores empty lines at the top of a page.
						if(paragraph.isEmpty() && lineNumber == 0){

							paragraph = bufferedreader.readLine();

							// Normal paragraph, all on one page
						}else if(lineNumber + linesInParagraph <= linesPerPage){

							page.add(paragraph);

							lineNumber += linesInParagraph;

							paragraph = bufferedreader.readLine();

							// Paragraphs split across two pages (or more?)
						}else{

							int linesInFirstPart = linesPerPage - lineNumber;

							String paragraphFirstPart = "";
							String paragraphLastPart = "";

							int i = 0;

							List<String> strings = this.fontRenderer.listFormattedStringToWidth(paragraph,
									GuiWizardHandbook.PAGE_WIDTH);

							for(Object s : strings){
								if(i < linesInFirstPart){
									paragraphFirstPart = paragraphFirstPart.concat((String)s + " ");
								}else{
									paragraphLastPart = paragraphLastPart.concat((String)s + " ");
								}
								i++;
							}

							// System.out.println("Paragraph crosses page boundary; string split into: \"" +
							// paragraphFirstPart + "\" and \"" + paragraphLastPart + "\"");

							page.add(paragraphFirstPart);

							lineNumber += linesInFirstPart;

							paragraph = paragraphLastPart;
						}
					}
				}

				text.add(page);

			}catch (IOException e){
				Wizardry.logger.error("Something went wrong reading file: " + textFilepath
						+ ". The file may be damaged;" + "please try re-downloading and reinstalling wizardry.", e);
			}
		}
	}

	private class Section {

		/** The integer text colour used for the section when it is moused over. Currently orange. */
		private static final int HIGHLIGHT_COLOUR = 0xdd4c1d;

		String name;
		int pageNumber;
		int x, y;
		int buttonId;

		Section(String name, int pageNumber, int x, int y, int id){
			this.name = name;
			this.pageNumber = pageNumber;
			this.x = x;
			this.y = y;
			this.buttonId = id;
			GuiWizardHandbook.this.buttonList.add(new GuiButtonInvisible(id, x, y, GuiWizardHandbook.PAGE_WIDTH,
					GuiWizardHandbook.this.fontRenderer.FONT_HEIGHT));
		}

		void hideButton(){
			GuiWizardHandbook.this.buttonList.get(buttonId).visible = false;
		}

		void drawContents(){

			GuiWizardHandbook.this.buttonList.get(buttonId).visible = true;

			GuiWizardHandbook.this.fontRenderer.drawString(name, x, y,
					GuiWizardHandbook.this.buttonList.get(buttonId).isMouseOver() ? HIGHLIGHT_COLOUR : BLACK);

			int nameWidth = GuiWizardHandbook.this.fontRenderer.getStringWidth(name);

			String dotsAndNumber = " " + this.pageNumber;

			while(GuiWizardHandbook.this.fontRenderer.getStringWidth(dotsAndNumber) < GuiWizardHandbook.PAGE_WIDTH
					- nameWidth - 2){
				dotsAndNumber = "." + dotsAndNumber;
			}

			GuiWizardHandbook.this.fontRenderer.drawString(dotsAndNumber, x + GuiWizardHandbook.PAGE_WIDTH
					- GuiWizardHandbook.this.fontRenderer.getStringWidth(dotsAndNumber), y, BLACK);
		}
	}

	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton par1GuiButton){

		if(par1GuiButton.enabled){
			if(par1GuiButton.id == 0){
				if(pageNumber < (text.size() - 1) / 2) pageNumber++;
			}else if(par1GuiButton.id == 1){
				if(pageNumber > 0) pageNumber--;
			}else{
				if(pageNumber == 1) pageNumber = (sections.get(par1GuiButton.id - 2).pageNumber - 1) / 2;
			}
		}
	}

	/**
	 * Args: left, top, width, height, pointX, pointY. Note: left, top are local to Gui, pointX, pointY are local to
	 * screen
	 */
	protected boolean isPointInRegion(int par1, int par2, int par3, int par4, int par5, int par6){
		int k1 = this.width / 2 - xSize / 2;
		int l1 = this.height / 2 - this.ySize / 2;
		par5 -= k1;
		par6 -= l1;
		return par5 >= par1 - 1 && par5 < par1 + par3 + 1 && par6 >= par2 - 1 && par6 < par2 + par4 + 1;
	}

	/**
	 * Draws a textured rectangle, stretching the section of the image to fit the size given.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted, expressed as a fraction of the
	 *        image width
	 * @param v The y position of the top left corner of the section of the image wanted, expressed as a fraction of the
	 *        image width
	 * @param finalWidth The width as rendered
	 * @param finalHeight The height as rendered
	 * @param width The width of the section, expressed as a fraction of the image width
	 * @param height The height of the section, expressed as a fraction of the image width
	 */
	public static void drawTexturedStretchedRect(int x, int y, int u, int v, int finalWidth, int finalHeight, int width,
			int height){

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos((x), y + finalHeight, 0).tex(u, v + height).endVertex();
		buffer.pos(x + finalWidth, y + finalHeight, 0).tex(u + width, v + height).endVertex();
		buffer.pos(x + finalWidth, (y), 0).tex(u + width, v).endVertex();
		buffer.pos((x), (y), 0).tex(u, v).endVertex();
		tessellator.draw();
	}
	
	private static NonNullList<NonNullList<ItemStack>> createGrid(){
		NonNullList<NonNullList<ItemStack>> grid = NonNullList.withSize(3, NonNullList.create());
		for(int i=0; i<3; i++){
			grid.set(i, NonNullList.withSize(3, ItemStack.EMPTY));
		}
		return grid;
	}
	
	/** Called from init() in the main mod class to initialise the recipes for display in the handbook. */
	public static void initDisplayRecipes(){
		
		NonNullList<NonNullList<ItemStack>> craftingGrid;
		ItemStack craftingResult;
		
		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(Items.GOLD_NUGGET));
		craftingGrid.get(1).set(0, new ItemStack(Blocks.CARPET, 1, 10));
		craftingGrid.get(2).set(0, new ItemStack(Items.GOLD_NUGGET));
		craftingGrid.get(0).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(1, new ItemStack(Blocks.LAPIS_BLOCK));
		craftingGrid.get(2).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(0).set(2, new ItemStack(Blocks.STONE));
		craftingGrid.get(1).set(2, new ItemStack(Blocks.STONE));
		craftingGrid.get(2).set(2, new ItemStack(Blocks.STONE));
		craftingResult = new ItemStack(WizardryBlocks.arcane_workbench);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(2).set(0, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(1, new ItemStack(Items.STICK));
		craftingGrid.get(0).set(2, new ItemStack(Items.GOLD_NUGGET));
		craftingResult = new ItemStack(WizardryItems.magic_wand);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(1).set(0, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(0).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(1, new ItemStack(Items.BOOK));
		craftingGrid.get(1).set(2, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(2).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingResult = new ItemStack(WizardryItems.spell_book, 1, 1);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(Items.BOOK));
		craftingGrid.get(1).set(0, new ItemStack(WizardryItems.magic_crystal));
		craftingResult = new ItemStack(WizardryItems.wizard_handbook);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(WizardryBlocks.crystal_flower));
		craftingResult = new ItemStack(WizardryItems.magic_crystal, 2);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(0, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(2).set(0, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(0).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(1, new ItemStack(Items.GLASS_BOTTLE));
		craftingGrid.get(2).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(0).set(2, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(2, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(2).set(2, new ItemStack(WizardryItems.magic_crystal));
		craftingResult = new ItemStack(WizardryItems.mana_flask);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(1).set(0, new ItemStack(Blocks.STONE));
		craftingGrid.get(0).set(1, new ItemStack(Blocks.STONE));
		craftingGrid.get(1).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(2, new ItemStack(Blocks.STONE));
		craftingGrid.get(2).set(1, new ItemStack(Blocks.STONE));
		craftingResult = new ItemStack(WizardryBlocks.transportation_stone, 2);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(1).set(0, new ItemStack(Items.STRING));
		craftingGrid.get(0).set(1, new ItemStack(Items.STRING));
		craftingGrid.get(1).set(1, new ItemStack(WizardryItems.magic_crystal));
		craftingGrid.get(1).set(2, new ItemStack(Items.STRING));
		craftingGrid.get(2).set(1, new ItemStack(Items.STRING));
		craftingResult = new ItemStack(WizardryItems.magic_silk, 2);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(1).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(0).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingResult = new ItemStack(WizardryItems.wizard_hat);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(0).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(1).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(0).set(2, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(1).set(2, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(2, new ItemStack(WizardryItems.magic_silk));
		craftingResult = new ItemStack(WizardryItems.wizard_robe);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(1).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(0).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(0).set(2, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(2, new ItemStack(WizardryItems.magic_silk));
		craftingResult = new ItemStack(WizardryItems.wizard_leggings);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		craftingGrid = createGrid();
		craftingGrid.get(0).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(0, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(0).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingGrid.get(2).set(1, new ItemStack(WizardryItems.magic_silk));
		craftingResult = new ItemStack(WizardryItems.wizard_boots);
		RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));

		if(Wizardry.settings.useAlternateScrollRecipe){
			craftingGrid = createGrid();
			craftingGrid.get(0).set(0, new ItemStack(Items.PAPER));
			craftingGrid.get(1).set(0, new ItemStack(Items.STRING));
			craftingGrid.get(2).set(0, new ItemStack(WizardryItems.magic_crystal));
			craftingResult = new ItemStack(WizardryItems.blank_scroll);
			RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));
		}else{
			craftingGrid = createGrid();
			craftingGrid.get(0).set(0, new ItemStack(Items.PAPER));
			craftingGrid.get(1).set(0, new ItemStack(Items.STRING));
			craftingResult = new ItemStack(WizardryItems.blank_scroll);
			RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));
		}

		if(Wizardry.settings.firebombIsCraftable){
			craftingGrid = createGrid();
			craftingGrid.get(0).set(0, new ItemStack(Items.BLAZE_POWDER));
			craftingGrid.get(1).set(0, new ItemStack(Items.BLAZE_POWDER));
			craftingGrid.get(0).set(1, new ItemStack(Items.GLASS_BOTTLE));
			craftingGrid.get(1).set(1, new ItemStack(Items.GUNPOWDER));
			craftingResult = new ItemStack(WizardryItems.firebomb, 3);
			RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));
		}

		if(Wizardry.settings.poisonBombIsCraftable){
			craftingGrid = createGrid();
			craftingGrid.get(0).set(0, new ItemStack(Items.SPIDER_EYE));
			craftingGrid.get(1).set(0, new ItemStack(Items.SPIDER_EYE));
			craftingGrid.get(0).set(1, new ItemStack(Items.GLASS_BOTTLE));
			craftingGrid.get(1).set(1, new ItemStack(Items.GUNPOWDER));
			craftingResult = new ItemStack(WizardryItems.poison_bomb, 3);
			RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));
		}

		if(Wizardry.settings.smokeBombIsCraftable){
			craftingGrid = createGrid();
			craftingGrid.get(0).set(0, new ItemStack(Items.COAL));
			craftingGrid.get(1).set(0, new ItemStack(Items.COAL));
			craftingGrid.get(0).set(1, new ItemStack(Items.GLASS_BOTTLE));
			craftingGrid.get(1).set(1, new ItemStack(Items.GUNPOWDER));
			craftingResult = new ItemStack(WizardryItems.smoke_bomb, 3);
			RECIPES.add(ImmutablePair.of(craftingResult, craftingGrid));
		}
	}

}