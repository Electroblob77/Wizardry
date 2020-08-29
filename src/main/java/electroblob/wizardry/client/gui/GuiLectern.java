package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.client.gui.GuiButtonTurnPage.Type;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.packet.PacketLectern;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityLectern;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.ISpellSortable;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class GuiLectern extends GuiSpellInfo implements ISpellSortable {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/container/lectern.png");
	/** The distance of the page buttons from the bottom outside corners of the GUI. */
	private static final int PAGE_BUTTON_INSET_X = 22, PAGE_BUTTON_INSET_Y = 13;
	/** The distance between adjacent page turn buttons. */
	private static final int PAGE_BUTTON_SPACING = 20;
	/** The distance of the sort buttons from the top left corner of the GUI. */
	private static final int SORT_BUTTON_INSET_X = 96, SORT_BUTTON_INSET_Y = 20;
	/** The distance between adjacent sort buttons. */
	private static final int SORT_BUTTON_SPACING = 13;
	/** The distance of the spell buttons from the top outside corners of the GUI. */
	private static final int SPELL_BUTTON_INSET_X = 23, SPELL_BUTTON_INSET_Y = 44;
	/** The distance between adjacent spell buttons (in both x and y). */
	private static final int SPELL_BUTTON_SPACING = 38;

	private static final int SPELL_ROWS = 3, SPELL_COLUMNS = 3;
	public static final int SPELL_BUTTON_COUNT = SPELL_ROWS * SPELL_COLUMNS * 2; // x2 because there are 2 pages

	private static final int SEARCH_TOOLTIP_HOVER_TIME = 20;

	private static final Style TOOLTIP_SYNTAX = new Style().setColor(TextFormatting.YELLOW);
	private static final Style TOOLTIP_BODY = new Style().setColor(TextFormatting.WHITE);

	private final TileEntityLectern lectern;

	private GuiButton nextPageButton;
	private GuiButton prevPageButton;
	private GuiButton lastPageButton;
	private GuiButton firstPageButton;
	private GuiButton indexButton;
	private GuiButton locateButton;

	private GuiButton[] sortButtons = new GuiButton[3];
	private GuiButtonSpell[] spellButtons = new GuiButtonSpell[SPELL_BUTTON_COUNT];

	/** The spell currently being viewed, or null if the index is being viewed. */
	private Spell currentSpell;
	/** The available spells in nearby bookshelves; should not contain duplicates. */
	private List<Spell> availableSpells = new ArrayList<>();
	/** The spells matching the current search criteria, sorted according to the current sort settings. Will be a subset
	 * of {@link GuiLectern#availableSpells}. */
	private List<Spell> matchingSpells;

	private ISpellSortable.SortType sortType = ISpellSortable.SortType.TIER;
	private boolean sortDescending = false;

	private GuiTextField searchField;
	private boolean searchNeedsClearing;
	private int searchBarHoverTime;

	private int currentPage = 0;

	public GuiLectern(TileEntityLectern lectern){
		super(288, 180);
		this.lectern = lectern;
		this.currentSpell = lectern.currentSpell;
		this.setTextureSize(512, 512);
	}

	@Override
	public Spell getSpell(){
		return currentSpell;
	}

	@Override
	public ResourceLocation getTexture(){
		return TEXTURE;
	}

	@Override
	public SortType getSortType(){
		return sortType;
	}

	@Override
	public boolean isSortDescending(){
		return sortDescending;
	}

	private int getPageCount(){
		return MathHelper.ceil((float)matchingSpells.size() / SPELL_BUTTON_COUNT);
	}

	private Spell getSpellForButton(GuiButtonSpell button){
		return matchingSpells.get(currentPage * SPELL_BUTTON_COUNT + button.index);
	}

	@Override
	public void initGui(){

		super.initGui();

		final int left = this.width / 2 - this.xSize / 2;
		final int top = this.height / 2 - this.ySize / 2;

		int buttonID = 0;

		// Page buttons
		this.buttonList.add(nextPageButton = new GuiButtonTurnPage(buttonID++, left + xSize - PAGE_BUTTON_INSET_X - GuiButtonTurnPage.WIDTH,
				top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.NEXT_PAGE, TEXTURE, textureWidth, textureHeight));

		this.buttonList.add(prevPageButton = new GuiButtonTurnPage(buttonID++, left + PAGE_BUTTON_INSET_X,
				top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.PREVIOUS_PAGE, TEXTURE, textureWidth, textureHeight));

		this.buttonList.add(lastPageButton = new GuiButtonTurnPage(buttonID++, left + xSize - PAGE_BUTTON_INSET_X - GuiButtonTurnPage.WIDTH - PAGE_BUTTON_SPACING,
				top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.NEXT_SECTION, TEXTURE, textureWidth, textureHeight));

		this.buttonList.add(firstPageButton = new GuiButtonTurnPage(buttonID++, left + PAGE_BUTTON_INSET_X + PAGE_BUTTON_SPACING,
				top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.PREVIOUS_SECTION, TEXTURE, textureWidth, textureHeight));

		this.buttonList.add(indexButton = new GuiButtonTurnPage(buttonID++, left + xSize/2 - 23,
				top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT, Type.CONTENTS, TEXTURE, textureWidth, textureHeight));

		this.buttonList.add(locateButton = new GuiButtonLocateBook(buttonID++, left + xSize/2 - 34,
				top + ySize - PAGE_BUTTON_INSET_Y - GuiButtonTurnPage.HEIGHT));

		// Sort buttons
		for(SortType sortType : SortType.values()){
			this.buttonList.add(sortButtons[sortType.ordinal()] = new GuiButtonSpellSort(buttonID++,
					left + SORT_BUTTON_INSET_X + SORT_BUTTON_SPACING * sortType.ordinal(),
					top + SORT_BUTTON_INSET_Y, sortType, this, this));
		}

		// Spell buttons
		for(int i = 0; i < SPELL_BUTTON_COUNT; i++){

			int row = i % SPELL_COLUMNS;
			int column = (i / SPELL_COLUMNS) % SPELL_ROWS;

			int x = i < SPELL_BUTTON_COUNT/2 ? SPELL_BUTTON_INSET_X + row * SPELL_BUTTON_SPACING
					: xSize - SPELL_BUTTON_INSET_X - GuiButtonSpell.WIDTH - (2-row) * SPELL_BUTTON_SPACING;
			int y = SPELL_BUTTON_INSET_Y + column * SPELL_BUTTON_SPACING;

			this.buttonList.add(spellButtons[i] = new GuiButtonSpell(buttonID++, left + x, top + y, i));
		}

		this.searchField = new GuiTextField(0, this.fontRenderer, left + 157, top + 21, 106, this.fontRenderer.FONT_HEIGHT);
		this.searchField.setMaxStringLength(50);
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setVisible(true);
		this.searchField.setTextColor(16777215);
		this.searchField.setCanLoseFocus(false);
		this.searchField.setFocused(true);

		refreshAvailableSpells(); // Must be done last

	}

	@Override
	public void updateScreen(){
		super.updateScreen();
		if(searchBarHoverTime > 0 && searchBarHoverTime < SEARCH_TOOLTIP_HOVER_TIME) searchBarHoverTime++;
	}

	@Override
	public void onGuiClosed(){
		WizardryPacketHandler.net.sendToServer(new PacketLectern.Message(lectern.getPos(), currentSpell));
		super.onGuiClosed();
	}

	@Override
	protected void drawBackgroundLayer(int left, int top, int mouseX, int mouseY){
		if(currentSpell == Spells.none){
			drawIndexPage(left, top);
		}else{
			super.drawBackgroundLayer(left, top, mouseX, mouseY);
		}
	}

	@Override
	protected void drawForegroundLayer(int left, int top, int mouseX, int mouseY){

		if(currentSpell == Spells.none){
			this.fontRenderer.drawString(I18n.format("gui." + Wizardry.MODID + ":lectern.title"),
					left + 20, top + SORT_BUTTON_INSET_Y, 0);
		}else{
			super.drawForegroundLayer(left, top, mouseX, mouseY);
		}

		this.buttonList.forEach(b -> b.drawButtonForegroundLayer(mouseX, mouseY));

		// Search tooltip
		if(DrawingUtils.isPointInRegion(searchField.x, searchField.y, searchField.width, searchField.height, mouseX, mouseY)){
			if(searchBarHoverTime == 0){
				searchBarHoverTime++;
			}else if(searchBarHoverTime == SEARCH_TOOLTIP_HOVER_TIME){
				drawHoveringText(I18n.format("container." + Wizardry.MODID + ":arcane_workbench.search_tooltip",
						TOOLTIP_SYNTAX.getFormattingCode(), TOOLTIP_BODY.getFormattingCode()), mouseX, mouseY);
			}
		}else{
			searchBarHoverTime = 0;
		}
	}

	private void drawIndexPage(int left, int top){

		for(int i = 0; i < SPELL_BUTTON_COUNT; i++){

			int index = currentPage * SPELL_BUTTON_COUNT + i;
			Spell spell = index < matchingSpells.size() ? matchingSpells.get(index) : Spells.none;
			boolean discovered = Wizardry.proxy.shouldDisplayDiscovered(spell, null);

			Minecraft.getMinecraft().renderEngine.bindTexture(discovered ? spell.getIcon() : Spells.none.getIcon());

			int row = i % SPELL_COLUMNS;
			int column = (i / SPELL_COLUMNS) % SPELL_ROWS;

			int x = i < SPELL_BUTTON_COUNT/2 ? SPELL_BUTTON_INSET_X + row * SPELL_BUTTON_SPACING
					: xSize - SPELL_BUTTON_INSET_X - GuiButtonSpell.WIDTH - (2-row) * SPELL_BUTTON_SPACING;
			int y = SPELL_BUTTON_INSET_Y + column * SPELL_BUTTON_SPACING;

			DrawingUtils.drawTexturedRect(left + x + 1, top + y + 1, 0, 0, 32, 32, 32, 32);
		}

		mc.renderEngine.bindTexture(getTexture());
		DrawingUtils.drawTexturedRect(left, top, 0, 256, xSize, ySize, textureWidth, textureHeight);

		this.searchField.drawTextBox();

		GlStateManager.color(1, 1, 1, 1);
		mc.renderEngine.bindTexture(getTexture());

	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		searchNeedsClearing = true;
	}

	@Override
	protected void actionPerformed(GuiButton button){

		int lastPage = getPageCount() - 1;

		if(button == indexButton){
			currentSpell = Spells.none;
//			currentPage = 0;

		}else if(button == locateButton){
			// Close the GUI
			this.mc.player.connection.sendPacket(new CPacketCloseWindow(this.mc.player.openContainer.windowId));
			this.mc.displayGuiScreen(null);

			// Find the location(s) of the current spell's book(s) and highlight them
			for(IInventory bookshelf : BlockBookshelf.findNearbyBookshelves(lectern.getWorld(), lectern.getPos())){

				for(int i = 0; i < bookshelf.getSizeInventory(); i++){

					ItemStack stack = bookshelf.getStackInSlot(i);

					if(stack.getItem() instanceof ItemSpellBook){

						Spell spell = Spell.byMetadata(stack.getMetadata());

						if(spell == this.currentSpell){

							BlockPos pos = (((TileEntity)bookshelf).getPos());

							for(EnumFacing side : EnumFacing.VALUES){
								ParticleBuilder.create(ParticleBuilder.Type.BLOCK_HIGHLIGHT).pos(
												GeometryUtils.getFaceCentre(pos, side)
												.add(new Vec3d(side.getDirectionVec())
												.scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET)))
										.face(side).clr(0.9f, 0.5f, 0.8f).fade(0.7f, 0, 1).spawn(mc.world);
							}

							mc.world.playSound(pos, WizardrySounds.BLOCK_LECTERN_LOCATE_SPELL, SoundCategory.BLOCKS, 1, 0.7f, false);

							break; // This bookshelf has the spell, skip to the next bookshelf
						}
					}
				}
			}

		}else if(button == nextPageButton){
			if(currentPage < lastPage) currentPage++;

		}else if(button == prevPageButton){
			if(currentPage > 0) currentPage--;

		}else if(button == lastPageButton){
			currentPage = lastPage;

		}else if(button == firstPageButton){
			currentPage = 0;

		}else if(button instanceof GuiButtonSpell){
			currentSpell = getSpellForButton((GuiButtonSpell)button);

		}else if(button instanceof GuiButtonSpellSort){

			SortType sortType = (((GuiButtonSpellSort)button).sortType);

			if(this.sortType == sortType){
				this.sortDescending = !this.sortDescending;
			}else{
				this.sortType = sortType;
				this.sortDescending = false;
			}

			updateMatchingSpells();
		}

		updateButtonVisiblity();

	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if(this.searchNeedsClearing){
			this.searchNeedsClearing = false;
			this.searchField.setText("");
			this.currentPage = 0;
		}

		if(this.searchField.textboxKeyTyped(typedChar, keyCode)){
			this.currentPage = 0;
			updateMatchingSpells();
			updateButtonVisiblity();
		}else{
			super.keyTyped(typedChar, keyCode);
		}
	}

	private void updateButtonVisiblity(){

		if(currentSpell == Spells.none){

			this.searchField.setVisible(true);

			int lastPage = getPageCount() - 1;

			prevPageButton.visible = currentPage > 0;
			firstPageButton.visible = currentPage > 0;
			nextPageButton.visible = currentPage < lastPage;
			lastPageButton.visible = currentPage < lastPage;

			indexButton.visible = false;
			locateButton.visible = false;

			for(GuiButton button : sortButtons) button.visible = true;

			for(GuiButtonSpell button : spellButtons){
				button.visible = currentPage * SPELL_BUTTON_COUNT + button.index < matchingSpells.size();
			}

		}else{
			this.searchField.setVisible(false);
			this.buttonList.forEach(b -> b.visible = false); // Hide all buttons...
			indexButton.visible = true; // ... except the index button and locate button
			locateButton.visible = true;
		}
	}

	private void updateMatchingSpells(){
		matchingSpells = availableSpells.stream()
				.filter(s -> s.matches(searchField.getText().toLowerCase(Locale.ROOT)))
				.sorted(sortDescending ? sortType.comparator.reversed() : sortType.comparator)
				.collect(Collectors.toList());
	}

	// TODO: Config option to always display all spells when in creative
	/** Called on initialisation and whenever a bookshelf is added or removed, to update the list of spells. */
	public void refreshAvailableSpells(){

		availableSpells.clear();

		// No need to exclude the lectern TE because it's not an IInventory!
		for(IInventory bookshelf : BlockBookshelf.findNearbyBookshelves(lectern.getWorld(), lectern.getPos())){

			for(int i=0; i<bookshelf.getSizeInventory(); i++){

				ItemStack stack = bookshelf.getStackInSlot(i);

				if(stack.getItem() instanceof ItemSpellBook){
					Spell spell = Spell.byMetadata(stack.getMetadata());
					if(spell != Spells.none && !availableSpells.contains(spell)) availableSpells.add(spell);
				}
			}
		}

		if(!availableSpells.contains(currentSpell)) currentSpell = Spells.none; // TODO: Do we want this?

		updateMatchingSpells();
		updateButtonVisiblity();

	}

	private class GuiButtonLocateBook extends GuiButton {

		public GuiButtonLocateBook(int id, int x, int y){
			super(id, x, y, 12, 12, "");
		}

		@Override
		public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){

			if(this.visible){

				boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				GlStateManager.color(1, 1, 1, 1);
				minecraft.getTextureManager().bindTexture(TEXTURE);

				DrawingUtils.drawTexturedRect(this.x, this.y, flag ? width : 0, 184, width, height, textureWidth, textureHeight);
			}
		}

	}

	private class GuiButtonSpell extends GuiButtonInvisible {

		private static final int WIDTH = 34, HEIGHT = 34;

		private final int index;

		public GuiButtonSpell(int id, int x, int y, int index){
			super(id, x, y, WIDTH, HEIGHT);
			this.index = index;
		}

		@Override
		public void playPressSound(SoundHandler soundHandler){
			soundHandler.playSound(PositionedSoundRecord.getMasterRecord(WizardrySounds.MISC_PAGE_TURN, 1));
		}

		@Override
		public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){
			if(visible){
				super.drawButton(minecraft, mouseX, mouseY, partialTicks);
				if(hovered){
					mc.renderEngine.bindTexture(getTexture());
					DrawingUtils.drawTexturedRect(x, y, 40, 180, width, height, textureWidth, textureHeight);
				}
			}
		}

		@Override
		public void drawButtonForegroundLayer(int mouseX, int mouseY){
			if(visible && hovered){
				Spell spell = getSpellForButton(this);
				if(Wizardry.proxy.shouldDisplayDiscovered(spell, null)){
					drawHoveringText(Collections.singletonList(spell.getDisplayName()), mouseX, mouseY, fontRenderer);
				}else{
					drawHoveringText(Collections.singletonList(SpellGlyphData.getGlyphName(spell, mc.world)),
							mouseX, mouseY, mc.standardGalacticFontRenderer);
				}
			}
		}

	}

}
