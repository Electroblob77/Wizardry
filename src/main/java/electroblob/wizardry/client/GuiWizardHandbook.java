package electroblob.wizardry.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiWizardHandbook extends GuiScreen {

	private GuiButtonNextPage nextPageBtn, prevPageBtn;
	private int xSize, ySize;
	private int pageNumber = 0;

	private static final int pageWidth = 120;

	public static final ResourceLocation regularHandbook = new ResourceLocation(Wizardry.MODID, "textures/gui/handbook.png");
	public static final ResourceLocation ore = new ResourceLocation(Wizardry.MODID, "textures/gui/ore_picture.png");
	public static final ResourceLocation crystal = new ResourceLocation(Wizardry.MODID, "textures/items/magic_crystal.png");
	public static final ResourceLocation workbenchGui = new ResourceLocation(Wizardry.MODID, "textures/gui/arcane_workbench.png");
	public static final ResourceLocation craftingGrids = new ResourceLocation(Wizardry.MODID, "textures/gui/handbook_recipes.png");

	private ArrayList<ArrayList<String>> text;
	private ArrayList<Section> sections;
	
	private int guiPage, imagePage;

	public GuiWizardHandbook() {
		super();
		xSize = 288;
		ySize = 180;
	}

	/**
	 * Draws the screen and all the components in it.
	 */
	public void drawScreen(int mouseX, int mouseY, float par3){
		
		int xPos = this.width/2 - xSize/2;
		int yPos = this.height/2 - this.ySize/2;

		// Tests for crafting recipes section
		if(pageNumber >= (sections.get(sections.size()-1).pageNumber-1)/2 && pageNumber < (sections.get(sections.size()-1).pageNumber-1)/2 + 4){
			Minecraft.getMinecraft().renderEngine.bindTexture(craftingGrids);
		}else{
			Minecraft.getMinecraft().renderEngine.bindTexture(regularHandbook);
		}

		drawTexturedRect(xPos, yPos, 0, 0, xSize, ySize, xSize, 256);
		
		// Arcane workbench gui picture
		if(pageNumber == (this.guiPage-1)/2){
			Minecraft.getMinecraft().renderEngine.bindTexture(workbenchGui);
			this.drawTexturedModalRect(this.guiPage % 2 == 1 ? xPos + 17 : this.width/2 + 7, yPos + 14, 28, 12, 120, 118);
		}
		
		// Magic crystal and crystal ore images
		if(pageNumber == (this.imagePage-1)/2){
			
			Minecraft.getMinecraft().renderEngine.bindTexture(ore);
			drawTexturedRect(this.imagePage % 2 == 1 ? xPos + 17 : this.width/2 + 7, yPos + 80, 0, 0, 64, 64, 64, 64);
			
			Minecraft.getMinecraft().renderEngine.bindTexture(crystal);
			drawTexturedStretchedRect(this.imagePage % 2 == 1 ? xPos + 17 + 64 : this.width/2 + 7 + 62, yPos + 80, 0, 0, 64, 64, 1, 1);

		}

		this.fontRendererObj.drawString("" + (pageNumber*2 + 1), xPos + xSize/4 - 3, yPos + ySize - 20, 0);
		this.fontRendererObj.drawString("" + (pageNumber*2 + 2), xPos + 3*xSize/4 - 5, yPos + ySize - 20, 0);

		super.drawScreen(mouseX, mouseY, par3);

		int lineNumber = 0;
		
		if(pageNumber == 1){
			int row = 2;
			for(Section s : sections){
				s.drawContents();
				row++;
			}
		}else{
			for(Section s : sections){
				s.hideButton();
			}
		}

		for(String paragraph : text.get(pageNumber*2)){

			this.fontRendererObj.drawSplitString(paragraph, xPos + 17, yPos + 14 + lineNumber*this.fontRendererObj.FONT_HEIGHT, pageWidth, 0);

			ArrayList list = new ArrayList(this.fontRendererObj.listFormattedStringToWidth(paragraph, this.pageWidth));

			lineNumber += list.size();
		}

		lineNumber = 0;

		// Prevents crash when the last page is blank (and hence is not in the list of pages)
		if(text.size() > pageNumber*2 + 1){
			for(String paragraph : text.get(pageNumber*2 + 1)){

				// First page is centred
				if(pageNumber == 0){
					int startX = this.width/2 + 7 + pageWidth/2 - this.fontRendererObj.getStringWidth(paragraph)/2;
					this.fontRendererObj.drawSplitString(paragraph, startX, yPos + 14 + lineNumber*this.fontRendererObj.FONT_HEIGHT, pageWidth, 0);
				}else{
					this.fontRendererObj.drawSplitString(paragraph, this.width/2 + 7, yPos + 14 + lineNumber*this.fontRendererObj.FONT_HEIGHT, pageWidth, 0);
				}
				
				ArrayList list = new ArrayList(this.fontRendererObj.listFormattedStringToWidth(paragraph, this.pageWidth));
	
				lineNumber += list.size();
			}
		}

		/*
		for(int i=0;i<(Math.min(text[pageNumber].length, 14));i++){
			this.fontRendererObj.drawString(text[pageNumber][i], xPos + 16, yPos + 14 + i*10, 0);
		}
		for(int i=0;i<(Math.min(text[pageNumber].length - 14, 14));i++){
			this.fontRendererObj.drawString(text[pageNumber][i+14], this.width/2 + 5, yPos + 14 + i*10, 0);
		}
		 */
		ItemStack[][] craftingGrid;
		ItemStack craftingResult;

		// Tooltips are rendered after recipes to prevent tooltips on the left appearing behind items on the right.
		if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2){

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Items.gold_nugget);
			craftingGrid[1][0] = new ItemStack(Blocks.carpet, 1, 10);
			craftingGrid[2][0] = new ItemStack(Items.gold_nugget);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Blocks.lapis_block);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][2] = new ItemStack(Blocks.stone);
			craftingGrid[1][2] = new ItemStack(Blocks.stone);
			craftingGrid[2][2] = new ItemStack(Blocks.stone);
			craftingResult = new ItemStack(Wizardry.arcaneWorkbench);
			this.renderCraftingRecipe(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[2][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Items.stick);
			craftingGrid[0][2] = new ItemStack(Items.gold_nugget);
			craftingResult = new ItemStack(Wizardry.magicWand);
			this.renderCraftingRecipe(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[1][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Items.book);
			craftingGrid[1][2] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicCrystal);
			craftingResult = new ItemStack(Wizardry.spellBook, 1, 1);
			this.renderCraftingRecipe(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Items.book);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicCrystal);
			craftingResult = new ItemStack(Wizardry.wizardHandbook);
			this.renderCraftingRecipe(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

		}else if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2 + 1){

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.crystalFlower);
			craftingResult = new ItemStack(Wizardry.magicCrystal, 2);
			this.renderCraftingRecipe(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Items.glass_bottle);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][2] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][2] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[2][2] = new ItemStack(Wizardry.magicCrystal);
			craftingResult = new ItemStack(Wizardry.manaFlask);
			this.renderCraftingRecipe(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[1][0] = new ItemStack(Blocks.stone);
			craftingGrid[0][1] = new ItemStack(Blocks.stone);
			craftingGrid[1][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][2] = new ItemStack(Blocks.stone);
			craftingGrid[2][1] = new ItemStack(Blocks.stone);
			craftingResult = new ItemStack(Wizardry.transportationStone, 2);
			this.renderCraftingRecipe(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[1][0] = new ItemStack(Items.string);
			craftingGrid[0][1] = new ItemStack(Items.string);
			craftingGrid[1][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][2] = new ItemStack(Items.string);
			craftingGrid[2][1] = new ItemStack(Items.string);
			craftingResult = new ItemStack(Wizardry.magicSilk, 2);
			this.renderCraftingRecipe(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

		}else if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2 + 2){

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardHat);
			this.renderCraftingRecipe(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][2] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][2] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][2] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardRobe);
			this.renderCraftingRecipe(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][2] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][2] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardLeggings);
			this.renderCraftingRecipe(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardBoots);
			this.renderCraftingRecipe(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

		}else if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2 + 3){

			if(Wizardry.useAlternateScrollRecipe){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.paper);
				craftingGrid[1][0] = new ItemStack(Items.string);
				craftingGrid[2][0] = new ItemStack(Wizardry.magicCrystal);
				craftingResult = new ItemStack(Wizardry.blankScroll);
				this.renderCraftingRecipe(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);
			}else{
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.paper);
				craftingGrid[1][0] = new ItemStack(Items.string);
				craftingResult = new ItemStack(Wizardry.blankScroll);
				this.renderCraftingRecipe(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);
			}
			
			if(Wizardry.firebombIsCraftable){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.blaze_powder);
				craftingGrid[1][0] = new ItemStack(Items.blaze_powder);
				craftingGrid[0][1] = new ItemStack(Items.glass_bottle);
				craftingGrid[1][1] = new ItemStack(Items.gunpowder);
				craftingResult = new ItemStack(Wizardry.firebomb, 3);
				this.renderCraftingRecipe(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);
			}

			if(Wizardry.poisonBombIsCraftable){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.spider_eye);
				craftingGrid[1][0] = new ItemStack(Items.spider_eye);
				craftingGrid[0][1] = new ItemStack(Items.glass_bottle);
				craftingGrid[1][1] = new ItemStack(Items.gunpowder);
				craftingResult = new ItemStack(Wizardry.poisonBomb, 3);
				this.renderCraftingRecipe(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);
			}

			if(Wizardry.smokeBombIsCraftable){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.coal);
				craftingGrid[1][0] = new ItemStack(Items.coal);
				craftingGrid[0][1] = new ItemStack(Items.glass_bottle);
				craftingGrid[1][1] = new ItemStack(Items.gunpowder);
				craftingResult = new ItemStack(Wizardry.smokeBomb, 3);
				this.renderCraftingRecipe(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);
			}
			
		}

		if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2){

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Items.gold_nugget);
			craftingGrid[1][0] = new ItemStack(Blocks.carpet, 1, 10);
			craftingGrid[2][0] = new ItemStack(Items.gold_nugget);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Blocks.lapis_block);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][2] = new ItemStack(Blocks.stone);
			craftingGrid[1][2] = new ItemStack(Blocks.stone);
			craftingGrid[2][2] = new ItemStack(Blocks.stone);
			craftingResult = new ItemStack(Wizardry.arcaneWorkbench);
			this.renderCraftingTooltips(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[2][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Items.stick);
			craftingGrid[0][2] = new ItemStack(Items.gold_nugget);
			craftingResult = new ItemStack(Wizardry.magicWand);
			this.renderCraftingTooltips(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[1][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Items.book);
			craftingGrid[1][2] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicCrystal);
			craftingResult = new ItemStack(Wizardry.spellBook, 1, 1);
			this.renderCraftingTooltips(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Items.book);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicCrystal);
			craftingResult = new ItemStack(Wizardry.wizardHandbook);
			this.renderCraftingTooltips(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

		}else if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2 + 1){

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.crystalFlower);
			craftingResult = new ItemStack(Wizardry.magicCrystal, 2);
			this.renderCraftingTooltips(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][1] = new ItemStack(Items.glass_bottle);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[0][2] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][2] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[2][2] = new ItemStack(Wizardry.magicCrystal);
			craftingResult = new ItemStack(Wizardry.manaFlask);
			this.renderCraftingTooltips(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[1][0] = new ItemStack(Blocks.stone);
			craftingGrid[0][1] = new ItemStack(Blocks.stone);
			craftingGrid[1][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][2] = new ItemStack(Blocks.stone);
			craftingGrid[2][1] = new ItemStack(Blocks.stone);
			craftingResult = new ItemStack(Wizardry.transportationStone, 2);
			this.renderCraftingTooltips(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[1][0] = new ItemStack(Items.string);
			craftingGrid[0][1] = new ItemStack(Items.string);
			craftingGrid[1][1] = new ItemStack(Wizardry.magicCrystal);
			craftingGrid[1][2] = new ItemStack(Items.string);
			craftingGrid[2][1] = new ItemStack(Items.string);
			craftingResult = new ItemStack(Wizardry.magicSilk, 2);
			this.renderCraftingTooltips(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

		}else if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2 + 2){

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardHat);
			this.renderCraftingTooltips(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][2] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][2] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][2] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardRobe);
			this.renderCraftingTooltips(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[1][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][2] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][2] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardLeggings);
			this.renderCraftingTooltips(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);

			craftingGrid = new ItemStack[3][3];
			craftingGrid[0][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][0] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[0][1] = new ItemStack(Wizardry.magicSilk);
			craftingGrid[2][1] = new ItemStack(Wizardry.magicSilk);
			craftingResult = new ItemStack(Wizardry.wizardBoots);
			this.renderCraftingTooltips(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);

		}else if(pageNumber == (sections.get(sections.size()-1).pageNumber-1)/2 + 3){

			if(Wizardry.useAlternateScrollRecipe){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.paper);
				craftingGrid[1][0] = new ItemStack(Items.string);
				craftingGrid[2][0] = new ItemStack(Wizardry.magicCrystal);
				craftingResult = new ItemStack(Wizardry.blankScroll);
				this.renderCraftingTooltips(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);
			}else{
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.paper);
				craftingGrid[1][0] = new ItemStack(Items.string);
				craftingResult = new ItemStack(Wizardry.blankScroll);
				this.renderCraftingTooltips(xPos + 23, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);
			}
			
			if(Wizardry.firebombIsCraftable){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.blaze_powder);
				craftingGrid[1][0] = new ItemStack(Items.blaze_powder);
				craftingGrid[0][1] = new ItemStack(Items.glass_bottle);
				craftingGrid[1][1] = new ItemStack(Items.gunpowder);
				craftingResult = new ItemStack(Wizardry.firebomb, 3);
				this.renderCraftingTooltips(xPos + 23, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);
			}

			if(Wizardry.poisonBombIsCraftable){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.spider_eye);
				craftingGrid[1][0] = new ItemStack(Items.spider_eye);
				craftingGrid[0][1] = new ItemStack(Items.glass_bottle);
				craftingGrid[1][1] = new ItemStack(Items.gunpowder);
				craftingResult = new ItemStack(Wizardry.poisonBomb, 3);
				this.renderCraftingTooltips(xPos + 156, yPos + 39, mouseX, mouseY, craftingGrid, craftingResult);
			}

			if(Wizardry.smokeBombIsCraftable){
				craftingGrid = new ItemStack[3][3];
				craftingGrid[0][0] = new ItemStack(Items.coal);
				craftingGrid[1][0] = new ItemStack(Items.coal);
				craftingGrid[0][1] = new ItemStack(Items.glass_bottle);
				craftingGrid[1][1] = new ItemStack(Items.gunpowder);
				craftingResult = new ItemStack(Wizardry.smokeBomb, 3);
				this.renderCraftingTooltips(xPos + 156, yPos + 98, mouseX, mouseY, craftingGrid, craftingResult);
			}
			
		}
	}

	private void renderCraftingRecipe(int xPos, int yPos, int mouseX, int mouseY, ItemStack[][] craftingGrid, ItemStack craftingResult) {

		int guiLeft = this.width/2 - xSize/2;
		int guiTop = this.height/2 - this.ySize/2;

		GL11.glPushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glEnable(GL11.GL_LIGHTING);
		itemRender.zLevel = 100.0F;

		for(int i=0; i<craftingGrid.length; i++){
			for(int j=0; j<craftingGrid[i].length; j++){
				if(craftingGrid[i][j] != null){
					itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), craftingGrid[i][j], xPos + 18*i, yPos + 18*j);
					itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), craftingGrid[i][j], xPos + 18*i, yPos + 18*j);
				}
			}
		}

		if(craftingResult != null){
			itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), craftingResult, xPos + 86, yPos + 18);
			itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), craftingResult, xPos + 86, yPos + 18);
		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();

	}

	private void renderCraftingTooltips(int xPos, int yPos, int mouseX, int mouseY, ItemStack[][] craftingGrid, ItemStack craftingResult) {

		int guiLeft = this.width/2 - xSize/2;
		int guiTop = this.height/2 - this.ySize/2;

		GL11.glPushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		itemRender.zLevel = 0.0F;        
		GL11.glDisable(GL11.GL_LIGHTING);

		for(int i=0; i<craftingGrid.length; i++){
			for(int j=0; j<craftingGrid[i].length; j++){
				if(craftingGrid[i][j] != null && isPointInRegion(xPos + 18*i, yPos + 18*j, 16, 16, mouseX + guiLeft, mouseY + guiTop)){
					this.drawItemStackTooltip(craftingGrid[i][j], mouseX, mouseY);
				}
			}
		}

		if(craftingResult != null && isPointInRegion(xPos + 86, yPos + 18, 16, 16, mouseX + guiLeft, mouseY + guiTop)){
			this.drawItemStackTooltip(craftingResult, mouseX, mouseY);
		}

		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableStandardItemLighting();

	}

	@Override
	public void initGui(){

		super.initGui();
		Keyboard.enableRepeatEvents(true);

		int nextButtonId = 0;

		this.buttonList.clear();
		this.buttonList.add(this.nextPageBtn = new GuiButtonNextPage(nextButtonId++, this.width/2 + this.xSize/2 - 22 - 23, this.height/2 + this.ySize/2 - 10 - 13, true));
		this.buttonList.add(this.prevPageBtn = new GuiButtonNextPage(nextButtonId++, this.width/2 - this.xSize/2 + 21, this.height/2 + this.ySize/2 - 10 - 13, false));

		text = new ArrayList<ArrayList<String>>(1);
		sections = new ArrayList<Section>(1);
		
		BufferedReader bufferedreader = null;
		
		String textFilepath = "wizardry:texts/handbook_" + Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode() + ".txt";
		
		try {
			
			bufferedreader = new BufferedReader(new InputStreamReader(this.mc.getResourceManager().getResource(new ResourceLocation(textFilepath)).getInputStream(), Charsets.UTF_8));

		} catch (IOException e){
			
			System.out.println("Warning: wizard handbook text file missing for the current language. Using default (English - US) instead.");

			textFilepath = "wizardry:texts/handbook_en_US.txt";
			
			try {
				
				bufferedreader = new BufferedReader(new InputStreamReader(this.mc.getResourceManager().getResource(new ResourceLocation(textFilepath)).getInputStream(), Charsets.UTF_8));

			} catch (IOException x){
				System.err.println("Couldn't find file: wizardry:assets/texts/handbook_en_US.txt. The file may be missing; please try re-downloading and reinstalling wizardry.");
				x.printStackTrace();
			}
		}
		
		if(bufferedreader != null){
			
			try {
				
				String paragraph = bufferedreader.readLine();
				ArrayList<String> page = new ArrayList<String>(1);
	
				int linesPerPage = 16;
	
				int lineNumber = 0;
	
				while(paragraph != null){
	
					//System.out.println(paragraph);
	
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
	
						sections.add(new Section(paragraph.replace("SECTION ", ""), text.size() + 1, this.width/2 + 7,
								this.height/2 - this.ySize/2 + 14 + (sections.size()+2)*this.fontRendererObj.FONT_HEIGHT, nextButtonId++));
						paragraph = bufferedreader.readLine();
	
					}else if(paragraph.contains("IMAGE")){
						
						if(paragraph.contains("WORKBENCH")){
							this.guiPage = text.size() + 1;
						}else if(paragraph.contains("CRYSTAL")){
							this.imagePage = text.size() + 1;
						}
						
						paragraph = bufferedreader.readLine();
						
					}else{
	
						paragraph = paragraph.replaceAll("NEXT_SPELL_KEY", Keyboard.getKeyName(ClientProxy.nextSpell.getKeyCode()));
						paragraph = paragraph.replaceAll("PREVIOUS_SPELL_KEY", Keyboard.getKeyName(ClientProxy.previousSpell.getKeyCode()));
						paragraph = paragraph.replaceAll("MANA_PER_CRYSTAL_MINUS_30", "" + (Wizardry.MANA_PER_CRYSTAL - 30));
						paragraph = paragraph.replaceAll("MANA_PER_CRYSTAL", "" + Wizardry.MANA_PER_CRYSTAL);
						paragraph = paragraph.replaceAll("BASIC_MAX_CHARGE", "" + EnumTier.BASIC.maxCharge);
						paragraph = paragraph.replaceAll("APPRENTICE_MAX_CHARGE", "" + EnumTier.APPRENTICE.maxCharge);
						paragraph = paragraph.replaceAll("ADVANCED_MAX_CHARGE", "" + EnumTier.ADVANCED.maxCharge);
						paragraph = paragraph.replaceAll("MASTER_MAX_CHARGE", "" + EnumTier.MASTER.maxCharge);
						paragraph = paragraph.replaceAll("BASIC_COLOUR", "\u00A77");
						paragraph = paragraph.replaceAll("APPRENTICE_COLOUR", EnumTier.APPRENTICE.colour);
						paragraph = paragraph.replaceAll("ADVANCED_COLOUR", EnumTier.ADVANCED.colour);
						paragraph = paragraph.replaceAll("MASTER_COLOUR", EnumTier.MASTER.colour);
						paragraph = paragraph.replaceAll("FIRE_COLOUR", EnumElement.FIRE.colour);
						paragraph = paragraph.replaceAll("ICE_COLOUR", EnumElement.ICE.colour);
						paragraph = paragraph.replaceAll("LIGHTNING_COLOUR", EnumElement.LIGHTNING.colour);
						paragraph = paragraph.replaceAll("NECROMANCY_COLOUR", EnumElement.NECROMANCY.colour);
						paragraph = paragraph.replaceAll("EARTH_COLOUR", EnumElement.EARTH.colour);
						paragraph = paragraph.replaceAll("SORCERY_COLOUR", EnumElement.SORCERY.colour);
						paragraph = paragraph.replaceAll("HEALING_COLOUR", EnumElement.HEALING.colour);
						paragraph = paragraph.replaceAll("RESET_COLOUR", "\u00A70");
						paragraph = paragraph.replaceAll("VERSION", Wizardry.VERSION);
	
						int linesInParagraph = this.fontRendererObj.listFormattedStringToWidth(paragraph, this.pageWidth).size();
	
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
	
							List strings = this.fontRendererObj.listFormattedStringToWidth(paragraph, this.pageWidth);
	
							for(Object s : strings){
								if(i < linesInFirstPart){
									paragraphFirstPart = paragraphFirstPart.concat((String)s + " ");
								}else{
									paragraphLastPart = paragraphLastPart.concat((String)s + " ");
								}
								i++;
							}
	
							//System.out.println("Paragraph crosses page boundary; string split into: \"" + paragraphFirstPart + "\" and \"" + paragraphLastPart + "\"");
	
							page.add(paragraphFirstPart);
	
							lineNumber += linesInFirstPart;
	
							paragraph = paragraphLastPart;
						}
					}
				}
	
				text.add(page);
	
			} catch (IOException e){
				System.err.println("Something went wrong reading file: " + textFilepath + ". The file may be damaged; please try re-downloading and reinstalling wizardry.");
				e.printStackTrace();
			}
		}
		/*
		for(ArrayList<String> a : text){
			for(String s : a){
				System.out.println(s);
			}
		}
		*/
	}

	private class Section {

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
			GuiWizardHandbook.this.buttonList.add(new GuiButtonInvisible(id, x, y, GuiWizardHandbook.this.pageWidth, GuiWizardHandbook.this.fontRendererObj.FONT_HEIGHT));
		}

		void hideButton(){
			((GuiButton)GuiWizardHandbook.this.buttonList.get(buttonId)).visible = false;
		}

		void drawContents(){
			
			((GuiButton)GuiWizardHandbook.this.buttonList.get(buttonId)).visible = true;

			GuiWizardHandbook.this.fontRendererObj.drawString(name, x, y, ((GuiButton)GuiWizardHandbook.this.buttonList.get(buttonId)).func_146115_a() ? 0xdd4c1d : 0);

			int nameWidth = GuiWizardHandbook.this.fontRendererObj.getStringWidth(name);

			String dotsAndNumber = " " + this.pageNumber;

			while(GuiWizardHandbook.this.fontRendererObj.getStringWidth(dotsAndNumber) < GuiWizardHandbook.pageWidth - nameWidth - 2){
				dotsAndNumber = "." + dotsAndNumber;
			}

			GuiWizardHandbook.this.fontRendererObj.drawString(dotsAndNumber, x + GuiWizardHandbook.pageWidth - GuiWizardHandbook.this.fontRendererObj.getStringWidth(dotsAndNumber), y, 0);
		}
	}

	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(GuiButton par1GuiButton){

		if(par1GuiButton.enabled){
			if(par1GuiButton.id == 0){
				if(pageNumber < (text.size()-1)/2) pageNumber++;
			}else if(par1GuiButton.id == 1){
				if(pageNumber > 0) pageNumber--;
			}else{
				if(pageNumber == 1) pageNumber = (sections.get(par1GuiButton.id - 2).pageNumber-1)/2;
			}
		}
	}

	/**
	 * Args: left, top, width, height, pointX, pointY. Note: left, top are local to Gui, pointX, pointY are local to
	 * screen
	 */
	protected boolean isPointInRegion(int par1, int par2, int par3, int par4, int par5, int par6)
	{
		int k1 = this.width/2 - xSize/2;
		int l1 = this.height/2 - this.ySize/2;
		par5 -= k1;
		par6 -= l1;
		return par5 >= par1 - 1 && par5 < par1 + par3 + 1 && par6 >= par2 - 1 && par6 < par2 + par4 + 1;
	}

	protected void drawItemStackTooltip(ItemStack par1ItemStack, int par2, int par3)
	{
		List list = par1ItemStack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);

		for (int k = 0; k < list.size(); ++k)
		{
			if (k == 0)
			{
				list.set(k, par1ItemStack.getRarity().rarityColor + (String)list.get(k));
			}
			else
			{
				list.set(k, EnumChatFormatting.GRAY + (String)list.get(k));
			}
		}

		FontRenderer font = par1ItemStack.getItem().getFontRenderer(par1ItemStack);
		drawHoveringText(list, par2, par3, (font == null ? fontRendererObj : font));
	}

	protected void drawHoveringText(List par1List, int par2, int par3, FontRenderer font)
	{
		if (!par1List.isEmpty())
		{
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;
			Iterator iterator = par1List.iterator();

			while (iterator.hasNext())
			{
				String s = (String)iterator.next();
				int l = font.getStringWidth(s);

				if (l > k)
				{
					k = l;
				}
			}

			int i1 = par2 + 12;
			int j1 = par3 - 12;
			int k1 = 8;

			if (par1List.size() > 1)
			{
				k1 += 2 + (par1List.size() - 1) * 10;
			}

			if (i1 + k > this.width)
			{
				i1 -= 28 + k;
			}

			if (j1 + k1 + 6 > this.height)
			{
				j1 = this.height - k1 - 6;
			}

			this.zLevel = 300.0F;
			itemRender.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

			for(int k2 = 0; k2 < par1List.size(); ++k2){
				
				String s1 = (String)par1List.get(k2);
				font.drawStringWithShadow(s1, i1, j1, -1);
	
				if(k2 == 0){
					j1 += 2;
				}
			
				j1 += 10;
			}

			this.zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	/**
	 * Draws a textured rectangle, taking the size of the image and the bit needed into account.
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 */
	public void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
	{
		float f = 1F / (float)textureWidth;
		float f1 = 1F / (float)textureHeight;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double)(x), (double)(y + height), 0, (double)((float)(u) * f), (double)((float)(v + height) * f1));
		tessellator.addVertexWithUV((double)(x + width), (double)(y + height), 0, (double)((float)(u + width) * f), (double)((float)(v + height) * f1));
		tessellator.addVertexWithUV((double)(x + width), (double)(y), 0, (double)((float)(u + width) * f), (double)((float)(v) * f1));
		tessellator.addVertexWithUV((double)(x), (double)(y), 0, (double)((float)(u) * f), (double)((float)(v) * f1));
		tessellator.draw();
	}
	
	/**
	 * Draws a textured rectangle, stretching the section of the image to fit the size given.
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted, expressed as a fraction of the image width
	 * @param v The y position of the top left corner of the section of the image wanted, expressed as a fraction of the image width
	 * @param finalWidth The width as rendered
	 * @param finalHeight The height as rendered
	 * @param width The width of the section, expressed as a fraction of the image width
	 * @param height The height of the section, expressed as a fraction of the image width
	 */
	public void drawTexturedStretchedRect(int x, int y, int u, int v, int finalWidth, int finalHeight, int width, int height)
	{
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double)(x), (double)(y + finalHeight), 0, u, v + height);
		tessellator.addVertexWithUV((double)(x + finalWidth), (double)(y + finalHeight), 0, u + width, v + height);
		tessellator.addVertexWithUV((double)(x + finalWidth), (double)(y), 0, u + width, v);
		tessellator.addVertexWithUV((double)(x), (double)(y), 0, u, v);
		tessellator.draw();
	}

}

