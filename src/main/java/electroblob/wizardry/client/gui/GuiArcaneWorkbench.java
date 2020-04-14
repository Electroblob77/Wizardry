package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.inventory.SlotBookList;
import electroblob.wizardry.item.*;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.util.ISpellSortable;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiArcaneWorkbench extends GuiContainer {

	public static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/gui/container/arcane_workbench.png");

	private static final int TOOLTIP_WIDTH = 144;
	private static final int TOOLTIP_BORDER = 6;
	private static final int LINE_SPACING_WIDE = 5;
	private static final int LINE_SPACING_NARROW = 1;

	private static final int BOOKSHELF_UI_WIDTH = ContainerArcaneWorkbench.BOOKSHELF_UI_WIDTH; // For conciseness

	/** We report the actual size of the GUI to Minecraft when a wand is in so JEI doesn't overdraw it.
	 * For calculations, we use the size without the tooltip, which is stored in this constant. */
	private static final int MAIN_GUI_WIDTH = 176;

	private static final int RUNE_LEFT = 38;
	private static final int RUNE_TOP = 22;
	private static final int RUNE_WIDTH = 100;
	private static final int RUNE_HEIGHT = 100;

	private static final int SCROLL_BAR_LEFT = 102;
	private static final int SCROLL_BAR_TOP = 34;
	private static final int SCROLL_BAR_WIDTH = 12;
	private static final int SCROLL_BAR_HEIGHT = 178;
	private static final int SCROLL_HANDLE_HEIGHT = 15;

	private static final int HALO_DIAMETER = 156;

	private static final int TEXTURE_WIDTH = 512;
	private static final int TEXTURE_HEIGHT = 512;

	private static final int ANIMATION_DURATION = 20;

	private InventoryPlayer playerInventory;
	private IInventory arcaneWorkbenchInventory;
	private ContainerArcaneWorkbench arcaneWorkbenchContainer;

	private GuiButton applyBtn;
	private GuiButton[] sortButtons = new GuiButton[3];

	private GuiTextField searchField;
	private boolean searchNeedsClearing;

	private final List<TooltipElement> tooltipElements = new ArrayList<>();

	private int animationTimer = 0;

	private float scroll = 0;
	private boolean scrolling = false;

	public GuiArcaneWorkbench(InventoryPlayer invPlayer, TileEntityArcaneWorkbench entity){
		super(new ContainerArcaneWorkbench(invPlayer, entity));
		this.arcaneWorkbenchContainer = (ContainerArcaneWorkbench)inventorySlots;
		this.playerInventory = invPlayer;
		this.arcaneWorkbenchInventory = entity;
		xSize = MAIN_GUI_WIDTH;
		ySize = 220;
	}

	@Override
	public void initGui(){

		this.mc.player.openContainer = this.inventorySlots;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		Keyboard.enableRepeatEvents(true);

		this.buttonList.clear();
		this.buttonList.add(this.applyBtn = new GuiButtonApply(0, this.width / 2 + 64, this.height / 2 + 3));
		this.buttonList.add(sortButtons[0] = new GuiButtonSpellSort(1, this.guiLeft - 44, this.guiTop + 8, ISpellSortable.SortType.TIER, arcaneWorkbenchContainer, this));
		this.buttonList.add(sortButtons[1] = new GuiButtonSpellSort(2, this.guiLeft - 31, this.guiTop + 8, ISpellSortable.SortType.ELEMENT, arcaneWorkbenchContainer, this));
		this.buttonList.add(sortButtons[2] = new GuiButtonSpellSort(3, this.guiLeft - 18, this.guiTop + 8, ISpellSortable.SortType.ALPHABETICAL, arcaneWorkbenchContainer, this));

		this.searchField = new GuiTextField(0, this.fontRenderer, this.guiLeft - 113, this.guiTop + 22, 104, this.fontRenderer.FONT_HEIGHT);
		this.searchField.setMaxStringLength(50);
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setVisible(true);
		this.searchField.setTextColor(16777215);
		this.searchField.setCanLoseFocus(false);
		this.searchField.setFocused(true);

		this.tooltipElements.add(new TooltipElementItemName(new Style().setColor(TextFormatting.WHITE), LINE_SPACING_WIDE));
		this.tooltipElements.add(new TooltipElementManaReadout(LINE_SPACING_WIDE));
		this.tooltipElements.add(new TooltipElementProgressionBar(LINE_SPACING_WIDE));
		this.tooltipElements.add(new TooltipElementSpellList(LINE_SPACING_WIDE));
		this.tooltipElements.add(new TooltipElementUpgradeList(LINE_SPACING_WIDE));

	}

	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	// Huh, didn't realise this method existed. Pretty neat.
	@Override
	public void updateScreen(){
		if(animationTimer > 0) animationTimer--;
		if(arcaneWorkbenchContainer.needsRefresh){
			arcaneWorkbenchContainer.refreshBookshelfSlots();
			arcaneWorkbenchContainer.needsRefresh = false;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){

		this.drawDefaultBackground();

		GlStateManager.color(1, 1, 1, 1); // Just in case

		boolean mouseHeld = Mouse.isButtonDown(0);

		if(!scrolling && mouseHeld && getMaxScrollRows() > 0 && isPointInRegion(SCROLL_BAR_LEFT,
				SCROLL_BAR_TOP, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, mouseX, mouseY)){
			scrolling = true;
		}

		if(!mouseHeld || getMaxScrollRows() == 0) scrolling = false;

		if(scrolling){
			scroll = MathHelper.clamp((float)(mouseY - SCROLL_BAR_TOP - SCROLL_HANDLE_HEIGHT/2 - guiTop)
					/(SCROLL_BAR_HEIGHT - SCROLL_HANDLE_HEIGHT), 0, 1);
			arcaneWorkbenchContainer.scrollTo((int)(getMaxScrollRows() * scroll + 0.5f));
		}

		Slot centreSlot = this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT);

		// Update the GUI dimensions based on whether there is a wand present and whether there are bookshelves

		xSize = MAIN_GUI_WIDTH;
		guiLeft = (this.width - MAIN_GUI_WIDTH) / 2;

		if(centreSlot.getHasStack() && centreSlot.getStack().getItem() instanceof IWorkbenchItem
				&& ((IWorkbenchItem)centreSlot.getStack().getItem()).showTooltip(centreSlot.getStack())){
			xSize += TOOLTIP_WIDTH;
		}

		if(arcaneWorkbenchContainer.hasBookshelves()){
			xSize += BOOKSHELF_UI_WIDTH;
			guiLeft -= BOOKSHELF_UI_WIDTH;
		}

		// Show/hide the relevant gui elements
		this.applyBtn.enabled = centreSlot.getHasStack();
		for(GuiButton button : this.sortButtons) button.visible = arcaneWorkbenchContainer.hasBookshelves();
		this.searchField.setVisible(arcaneWorkbenchContainer.hasBookshelves());

		super.drawScreen(mouseX, mouseY, partialTicks);

		// Required now, or item mouseover tooltips won't render.
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		// Coordinates of the top left corner of the main GUI
		int left = arcaneWorkbenchContainer.hasBookshelves() ? guiLeft + BOOKSHELF_UI_WIDTH : guiLeft;
		int top = guiTop;

		// Animation

		// Grey background
		DrawingUtils.drawTexturedRect(left + RUNE_LEFT, top + RUNE_TOP, MAIN_GUI_WIDTH + TOOLTIP_WIDTH, 0,
				RUNE_WIDTH, RUNE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Yellow 'halo'
		if(animationTimer > 0){

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

			int x = left + RUNE_LEFT + RUNE_WIDTH/2;
			int y = top + RUNE_TOP + RUNE_HEIGHT/2;

			float scale = (animationTimer + partialTicks)/ANIMATION_DURATION;
			scale = (float)(1 - Math.pow(1-scale, 1.4f)); // Makes it slower at the start and speed up
			GlStateManager.scale(scale, scale, 1);
			GlStateManager.translate(x/scale, y/scale, 0);

			DrawingUtils.drawTexturedRect(-HALO_DIAMETER /2, -HALO_DIAMETER /2, MAIN_GUI_WIDTH + TOOLTIP_WIDTH, RUNE_HEIGHT,
					HALO_DIAMETER, HALO_DIAMETER, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}

		// Main inventory
		DrawingUtils.drawTexturedRect(left, top, 0, 0, MAIN_GUI_WIDTH, ySize, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		float opacity = (animationTimer + partialTicks)/ANIMATION_DURATION;

		// Spell book slots (always use guiLeft and guiTop here regardless of bookshelf UI visibility
		for(int i = 0; i < ContainerArcaneWorkbench.CRYSTAL_SLOT; i++){

			Slot slot = this.inventorySlots.getSlot(i);

			if(slot.xPos >= 0 && slot.yPos >= 0){
				// Slot background
				DrawingUtils.drawTexturedRect(guiLeft + slot.xPos - 10, guiTop + slot.yPos - 10, 0, 220, 36, 36, TEXTURE_WIDTH, TEXTURE_HEIGHT);

				// Slot animation
				// IDEA: Somehow replace with intelligent check for whether the spell actually got applied
				if(animationTimer > 0 && slot.getHasStack()){

					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
					GlStateManager.color(1, 1, 1, opacity);

					DrawingUtils.drawTexturedRect(guiLeft + slot.xPos - 10, guiTop + slot.yPos - 10, 36, 220, 36, 36, TEXTURE_WIDTH, TEXTURE_HEIGHT);

					GlStateManager.color(1, 1, 1, 1);
					GlStateManager.disableBlend();
					GlStateManager.popMatrix();
				}
			}
		}

		// Crystal + upgrade slot animations
		if(animationTimer > 0){

			Slot crystals = this.inventorySlots.getSlot(ContainerArcaneWorkbench.CRYSTAL_SLOT);
			Slot upgrades = this.inventorySlots.getSlot(ContainerArcaneWorkbench.UPGRADE_SLOT);

			if(crystals.getHasStack()){

				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.color(1, 1, 1, opacity);

				DrawingUtils.drawTexturedRect(guiLeft + crystals.xPos - 8, guiTop + crystals.yPos - 8,
						MAIN_GUI_WIDTH + TOOLTIP_WIDTH + RUNE_WIDTH, 0, 32, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);

				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}

			if(upgrades.getHasStack()){

				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.color(1, 1, 1, opacity);

				DrawingUtils.drawTexturedRect(guiLeft + upgrades.xPos - 8, guiTop + upgrades.yPos - 8,
						MAIN_GUI_WIDTH + TOOLTIP_WIDTH + RUNE_WIDTH, 0, 32, 32, TEXTURE_WIDTH, TEXTURE_HEIGHT);

				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}

		// Bookshelf interface
		if(arcaneWorkbenchContainer.hasBookshelves()){

			DrawingUtils.drawTexturedRect(left - BOOKSHELF_UI_WIDTH, top, 0, 256, BOOKSHELF_UI_WIDTH, ySize, TEXTURE_WIDTH, TEXTURE_HEIGHT);

			// Scroll bar
			DrawingUtils.drawTexturedRect(left - BOOKSHELF_UI_WIDTH + SCROLL_BAR_LEFT,
					top + SCROLL_BAR_TOP + (int)(scroll * (SCROLL_BAR_HEIGHT - SCROLL_HANDLE_HEIGHT) + 0.5f),
					getMaxScrollRows() > 0 ? 0 : SCROLL_BAR_WIDTH, 476,
					SCROLL_BAR_WIDTH, SCROLL_HANDLE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		// Tooltip only drawn if there is a wand
		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getHasStack()){

			ItemStack stack = this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getStack();

			if(!(stack.getItem() instanceof IWorkbenchItem)){
				Wizardry.logger.warn("Invalid item in central slot of arcane workbench, how did that get there?!");
				return;
			}

			if(((IWorkbenchItem)stack.getItem()).showTooltip(stack)){

				int tooltipHeight = tooltipElements.stream().mapToInt(e -> e.getTotalHeight(stack)).sum()
						- tooltipElements.get(tooltipElements.size() - 1).spaceAfter; // Remove space after last element

				// Tooltip box
				DrawingUtils.drawTexturedRect(left + MAIN_GUI_WIDTH, top, MAIN_GUI_WIDTH, 0, TOOLTIP_WIDTH,
						TOOLTIP_BORDER + tooltipHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
				DrawingUtils.drawTexturedRect(left + MAIN_GUI_WIDTH, top + TOOLTIP_BORDER + tooltipHeight,
						MAIN_GUI_WIDTH, ySize - TOOLTIP_BORDER, TOOLTIP_WIDTH, TOOLTIP_BORDER, TEXTURE_WIDTH, TEXTURE_HEIGHT);

				int x = left + MAIN_GUI_WIDTH + TOOLTIP_BORDER;
				int y = top + TOOLTIP_BORDER;

				for(TooltipElement element : this.tooltipElements){
					y = element.drawBackgroundLayer(x, y, stack, partialTicks, mouseX, mouseY);
				}
			}
		}

		this.searchField.drawTextBox(); // Easier to do this last, then we don't need to re-bind the GUI texture twice

		GlStateManager.color(1, 1, 1, 1);

		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		// Fixes the bug that caused the slot highlight to render opaque. I don't know why it works, it just works!
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){

		GlStateManager.color(1, 1, 1, 1); // Just in case

		int left = arcaneWorkbenchContainer.hasBookshelves() ? BOOKSHELF_UI_WIDTH : 0;

		this.fontRenderer
				.drawString(this.arcaneWorkbenchInventory.hasCustomName() ? this.arcaneWorkbenchInventory.getName()
						: I18n.format(this.arcaneWorkbenchInventory.getName()), left + 8, 6, 4210752);
		this.fontRenderer.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
				: I18n.format(this.playerInventory.getName()), left + 8, this.ySize - 96 + 2, 4210752);

		if(arcaneWorkbenchContainer.hasBookshelves()){
			this.fontRenderer.drawString(I18n.format("container." + Wizardry.MODID + ":arcane_workbench.bookshelves"), 8, 6, 4210752);
		}

		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getHasStack()){

			ItemStack stack = this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getStack();

			if(!(stack.getItem() instanceof IWorkbenchItem)){
				Wizardry.logger.warn("Invalid item in central slot of arcane workbench, how did that get there?!");
				return;
			}

			if(((IWorkbenchItem)stack.getItem()).showTooltip(stack)){

				int x = left + MAIN_GUI_WIDTH + TOOLTIP_BORDER;
				int y = TOOLTIP_BORDER;

				for(TooltipElement element : this.tooltipElements){
					y = element.drawForegroundLayer(x, y, stack, mouseX, mouseY);
				}
			}
		}

		this.buttonList.forEach(b -> b.drawButtonForegroundLayer(mouseX - guiLeft, mouseY - guiTop));
	}

	// Controls

	@Override
	protected void actionPerformed(GuiButton button){

		if(button.enabled){

			if(button == applyBtn){
				// Packet building
				IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.APPLY_BUTTON);
				WizardryPacketHandler.net.sendToServer(msg);
				// Sound
				Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(
						WizardrySounds.BLOCK_ARCANE_WORKBENCH_SPELLBIND, 1));
				// Animation
				animationTimer = 20;
			}

			if(button instanceof GuiButtonSpellSort) this.arcaneWorkbenchContainer.setSortType(((GuiButtonSpellSort)button).sortType);
		}
	}

	private int getMaxScrollRows(){
		return Math.max(0, MathHelper.ceil((float)arcaneWorkbenchContainer.getActiveBookshelfSlots().size()
				/ ContainerArcaneWorkbench.BOOKSHELF_SLOTS_X) - ContainerArcaneWorkbench.BOOKSHELF_SLOTS_Y);
	}

	@Override
	public void handleMouseInput() throws IOException {

		super.handleMouseInput();
		int scrollDist = -Mouse.getEventDWheel();

		if(scrollDist != 0 && getMaxScrollRows() > 0){

			if(scrollDist > 0) this.scroll += 1f / getMaxScrollRows();
			if(scrollDist < 0) this.scroll -= 1f / getMaxScrollRows();

			scroll = MathHelper.clamp(scroll, 0, 1);

			arcaneWorkbenchContainer.scrollTo((int)(scroll * getMaxScrollRows() + 0.5f));
		}
	}

	@Override
	protected void handleMouseClick(Slot slot, int slotId, int mouseButton, ClickType type){

		searchNeedsClearing = true;

		// Click type behaves weirdly, don't use it! Query the item stack held by the cursor instead
		if(slot instanceof SlotBookList && ((SlotBookList)slot).hasDelegate() && playerInventory.getItemStack().isEmpty()){
			// If no item is currently being moved, divert book list slots to send the virtual slot through instead
			// See explanation in ContainerArcaneWorkbench
			Slot virtualSlot = ((SlotBookList)slot).getDelegate();
			super.handleMouseClick(virtualSlot, virtualSlot.slotNumber, mouseButton, type);
		}else{
			super.handleMouseClick(slot, slotId, mouseButton, type);
		}
		
		arcaneWorkbenchContainer.updateActiveBookshelfSlots();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {

		if(this.searchNeedsClearing){
			this.searchNeedsClearing = false;
			this.searchField.setText("");
		}

		if(this.searchField.textboxKeyTyped(typedChar, keyCode)){
			arcaneWorkbenchContainer.setSearchText(searchField.getText().toLowerCase(Locale.ROOT));
		}else{
			super.keyTyped(typedChar, keyCode);
		}
	}

	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(ContainerArcaneWorkbench.EMPTY_SLOT_CRYSTAL);
		event.getMap().registerSprite(ContainerArcaneWorkbench.EMPTY_SLOT_UPGRADE);
	}

	private abstract static class TooltipElement {

		private final TooltipElement[] children;
		private final int spaceAfter;

		public TooltipElement(int spaceAfter, TooltipElement... children){
			this.children = children;
			this.spaceAfter = spaceAfter;
		}

		// Externally-called methods

		/**
		 * Returns the height of this tooltip element and its children, with spacing, or 0 if it is not visible.
		 * @param stack The item stack currently in the central slot of the workbench
		 * @return The total height of this tooltip element and its children, including spacing
		 */
		public int getTotalHeight(ItemStack stack){
			if(!this.isVisible(stack)) return 0;
			int height = this.getHeight(stack);
			for(TooltipElement child : children) height += child.getTotalHeight(stack);
			return height + spaceAfter;
		}

		/**
		 * Draws the background layer of this tooltip element and all of its children.
		 * @param x The x-coordinate of the top left corner of this element
		 * @param y The y-coordinate of the top left corner of this element
		 * @param stack The item stack currently in the central slot of the workbench
		 * @param partialTicks The current partial tick time
		 * @param mouseX The current x-coordinate of the cursor
		 * @param mouseY The current y-coordinate of the cursor
		 * @return The y-coordinate at which the next tooltip element should start
		 */
		public int drawBackgroundLayer(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY){
			if(!this.isVisible(stack)) return y;
			this.drawBackground(x, y, stack, partialTicks, mouseX, mouseY);
			y += this.getHeight(stack);
			for(TooltipElement child : children) y = child.drawBackgroundLayer(x, y, stack, partialTicks, mouseX, mouseY);
			return y + spaceAfter;
		}

		/**
		 * Draws the foreground layer of this tooltip element and all of its children.
		 * @param x The x-coordinate of the top left corner of this element
		 * @param y The y-coordinate of the top left corner of this element
		 * @param stack The item stack currently in the central slot of the workbench
		 * @param mouseX The current x-coordinate of the cursor
		 * @param mouseY The current y-coordinate of the cursor
		 * @return The y-coordinate at which the next tooltip element should start
		 */
		public int drawForegroundLayer(int x, int y, ItemStack stack, int mouseX, int mouseY){
			if(!this.isVisible(stack)) return y;
			this.drawForeground(x, y, stack, mouseX, mouseY);
			y += this.getHeight(stack);
			for(TooltipElement child : children) y = child.drawForegroundLayer(x, y, stack, mouseX, mouseY);
			return y + spaceAfter;
		}

		// Abstract internal methods

		/**
		 * Returns whether this tooltip element should be shown.
		 * @param stack The item stack currently in the central slot of the workbench
		 * @return True if this element should be shown, false if not.
		 */
		protected abstract boolean isVisible(ItemStack stack);

		/**
		 * Returns the height of this tooltip element. This is for internal implementation.
		 * @param stack The item stack currently in the central slot of the workbench
		 * @return The height of this tooltip element, excluding children and spacing.
		 */
		protected abstract int getHeight(ItemStack stack);

		/**
		 * Draws the background layer of this tooltip element (excluding children). This is for internal implementation.
		 * @param x The x-coordinate of the top left corner of this element
		 * @param y The y-coordinate of the top left corner of this element
		 * @param stack The item stack currently in the central slot of the workbench
		 * @param partialTicks The current partial tick time
		 * @param mouseX The current x-coordinate of the cursor
		 * @param mouseY The current y-coordinate of the cursor
		 */
		protected abstract void drawBackground(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY);

		/**
		 * Draws the foreground layer of this tooltip element (excluding children). This is for internal implementation.
		 * @param x The x-coordinate of the top left corner of this element
		 * @param y The y-coordinate of the top left corner of this element
		 * @param stack The item stack currently in the central slot of the workbench
		 * @param mouseX The current x-coordinate of the cursor
		 * @param mouseY The current y-coordinate of the cursor
		 */
		protected abstract void drawForeground(int x, int y, ItemStack stack, int mouseX, int mouseY);

	}

	private class TooltipElementText extends TooltipElement {

		private final String text; // Can't change the language whilst in a GUI so we can just store the translated text
		private final Style style;

		public TooltipElementText(String text, Style style, int spaceAfter, TooltipElement... children){
			super(spaceAfter, children);
			this.text = text;
			this.style = style;
		}

		/** Returns the text for this element. */
		protected String getText(ItemStack stack){
			return text;
		}

		protected FontRenderer getFontRenderer(ItemStack stack){
			return fontRenderer;
		}

		protected int getColour(ItemStack stack){
			return 0;
		}

		@Override
		protected boolean isVisible(ItemStack stack){
			return true; // Always visible by default
		}

		@Override
		protected int getHeight(ItemStack stack){
			return getFontRenderer(stack).listFormattedStringToWidth(getText(stack), TOOLTIP_WIDTH - 2 * TOOLTIP_BORDER)
					.size() * getFontRenderer(stack).FONT_HEIGHT;
		}

		@Override
		protected void drawBackground(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY){
			// Nothing here because this element is only text!
		}

		@Override
		protected void drawForeground(int x, int y, ItemStack stack, int mouseX, int mouseY){
			for(String line : getFontRenderer(stack).listFormattedStringToWidth(getText(stack), TOOLTIP_WIDTH - 2 * TOOLTIP_BORDER)){
				getFontRenderer(stack).drawStringWithShadow(style.getFormattingCode() + line, x, y, getColour(stack));
				y += getFontRenderer(stack).FONT_HEIGHT;
			}
		}
	}

	private class TooltipElementItemName extends TooltipElementText {

		public TooltipElementItemName(Style style, int spaceAfter){
			super(null, style, spaceAfter);
		}

		@Override
		protected String getText(ItemStack stack){
			return stack.getDisplayName();
		}

	}

	private class TooltipElementManaReadout extends TooltipElementText {

		public TooltipElementManaReadout(int spaceAfter){
			super(null, new Style().setColor(TextFormatting.BLUE), spaceAfter);
		}

		@Override
		protected String getText(ItemStack stack){
			return I18n.format("container." + Wizardry.MODID + ":arcane_workbench.mana",
					((IManaStoringItem)stack.getItem()).getMana(stack),
					((IManaStoringItem)stack.getItem()).getManaCapacity(stack));
		}

		@Override
		protected boolean isVisible(ItemStack stack){
			return stack.getItem() instanceof IManaStoringItem && ((IManaStoringItem)stack.getItem()).showManaInWorkbench(mc.player, stack);
		}

	}

	private class TooltipElementProgressionBar extends TooltipElement {

		private static final int PROGRESSION_BAR_WIDTH = 131;
		private static final int PROGRESSION_BAR_HEIGHT = 3;

		public TooltipElementProgressionBar(int spaceAfter){
			super(spaceAfter);
		}

		@Override
		protected boolean isVisible(ItemStack stack){
			return stack.getItem() instanceof ItemWand && !Wizardry.settings.legacyWandLevelling;
		}

		@Override
		protected int getHeight(ItemStack stack){
			return fontRenderer.FONT_HEIGHT + LINE_SPACING_NARROW + PROGRESSION_BAR_HEIGHT;
		}

		@Override
		protected void drawBackground(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY){

			y += fontRenderer.FONT_HEIGHT + LINE_SPACING_NARROW;

			Tier tier = ((ItemWand)stack.getItem()).tier; // Only called if isVisible() returns true so this is ok

			float progressFraction = 1;

			if(tier != Tier.MASTER){
				progressFraction = (float)WandHelper.getProgression(stack) / Tier.values()[tier.level + 1].progression;
			}

			DrawingUtils.drawTexturedRect(x, y, MAIN_GUI_WIDTH, ySize + PROGRESSION_BAR_HEIGHT, PROGRESSION_BAR_WIDTH, PROGRESSION_BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			int width = (int)(PROGRESSION_BAR_WIDTH * progressFraction);
			DrawingUtils.drawTexturedRect(x, y, MAIN_GUI_WIDTH, ySize, width, PROGRESSION_BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
		}

		@Override
		protected void drawForeground(int x, int y, ItemStack stack, int mouseX, int mouseY){

			Tier tier = ((ItemWand)stack.getItem()).tier;

			fontRenderer.drawStringWithShadow(tier.getDisplayNameWithFormatting(), x, y, 0);

			if(tier != Tier.MASTER){
				Tier nextTier = Tier.values()[tier.level + 1];
				String s = TextFormatting.DARK_GRAY.toString() + nextTier.getDisplayName();
				if(WandHelper.getProgression(stack) >= nextTier.progression) s = nextTier.getDisplayNameWithFormatting();
				fontRenderer.drawStringWithShadow(s, x + TOOLTIP_WIDTH - TOOLTIP_BORDER * 2 - fontRenderer.getStringWidth(s), y, 0);
			}
		}

	}

	private class TooltipElementSpellList extends TooltipElement {

		public TooltipElementSpellList(int spaceAfter){
			super(spaceAfter, generateSpellEntries(8));
		}

		@Override
		protected boolean isVisible(ItemStack stack){
			return stack.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)stack.getItem()).showSpellsInWorkbench(mc.player, stack);
		}

		@Override
		protected int getHeight(ItemStack stack){
			return 0; // Doesn't have any height of its own
		}

		@Override
		public int drawBackgroundLayer(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY){
			// It's more efficient to do GL state changes once in here
			GlStateManager.enableBlend();
			y = super.drawBackgroundLayer(x, y, stack, partialTicks, mouseX, mouseY);
			GlStateManager.disableBlend();
			return y;
		}

		@Override
		protected void drawBackground(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY){
			// Has no background of its own
		}

		@Override
		protected void drawForeground(int x, int y, ItemStack stack, int mouseX, int mouseY){
			//Has no text of its own
		}
	}

	private TooltipElement[] generateSpellEntries(int count){
		TooltipElement[] entries = new TooltipElement[count];
		for(int i=0; i<count; i++) entries[i] = new TooltipElementSpellEntry(i);
		return entries;
	}

	private class TooltipElementSpellEntry extends TooltipElementText {

		private final int index;

		public TooltipElementSpellEntry(int index){
			super(null, new Style().setColor(TextFormatting.BLUE), LINE_SPACING_NARROW);
			this.index = index;
		}

		private Spell getSpell(ItemStack stack){

			ItemStack spellBook = inventorySlots.getSlot(index).getStack();

			if(!spellBook.isEmpty() && spellBook.getItem() instanceof ItemSpellBook){
				return Spell.byMetadata(spellBook.getMetadata());
			}else{
				return ((ISpellCastingItem)stack.getItem()).getSpells(stack)[index];
			}
		}

		private boolean shouldFlash(ItemStack stack){
			ItemStack spellBook = inventorySlots.getSlot(index).getStack();
			return !spellBook.isEmpty() && spellBook.getItem() instanceof ItemSpellBook
					&& Spell.byMetadata(spellBook.getMetadata()) != ((ISpellCastingItem)stack.getItem()).getSpells(stack)[index];
		}

		private float getAlpha(float partialTicks){
			return (MathHelper.sin(0.2f * (mc.player.ticksExisted + partialTicks)) + 1) / 4 + 0.5f;
		}

		@Override
		protected boolean isVisible(ItemStack stack){
			return stack.getItem() instanceof ISpellCastingItem
					&& index < ((ISpellCastingItem)stack.getItem()).getSpells(stack).length;
		}

		@Override
		protected FontRenderer getFontRenderer(ItemStack stack){
			return Wizardry.proxy.shouldDisplayDiscovered(getSpell(stack), null) ? super.getFontRenderer(stack)
					: mc.standardGalacticFontRenderer;
		}

		@Override
		protected int getColour(ItemStack stack){
			return shouldFlash(stack) ? DrawingUtils.makeTranslucent(0x000000, getAlpha(mc.getRenderPartialTicks()))
					: super.getColour(stack);
		}

		@Override
		protected String getText(ItemStack stack){

			Spell spell = getSpell(stack);

			if(Wizardry.proxy.shouldDisplayDiscovered(spell, null)){
				return spell.getDisplayNameWithFormatting();
			}else{
				return SpellGlyphData.getGlyphName(spell, mc.world);
			}
		}

		@Override
		protected void drawBackground(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY){

			Spell spell = getSpell(stack);

			Minecraft.getMinecraft().renderEngine.bindTexture(Wizardry.proxy.shouldDisplayDiscovered(spell, null)
					? spell.getElement().getIcon() : Element.MAGIC.getIcon());

			if(shouldFlash(stack)){
				GlStateManager.color(1, 1, 1, getAlpha(partialTicks));
			}

			// Renders the little element icon
			DrawingUtils.drawTexturedRect(x, y, 8, 8);

			GlStateManager.color(1, 1, 1, 1);

		}

		@Override
		protected void drawForeground(int x, int y, ItemStack stack, int mouseX, int mouseY){
			super.drawForeground(x + 11, y, stack, mouseX, mouseY);
		}
	}

	private class TooltipElementUpgradeList extends TooltipElementText {

		public TooltipElementUpgradeList(int spaceAfter){
			super(I18n.format("container." + Wizardry.MODID + ":arcane_workbench.upgrades"),
					new Style().setColor(TextFormatting.WHITE), spaceAfter, new TooltipElementUpgrades(0));
		}

		@Override
		protected int getHeight(ItemStack stack){
			return super.getHeight(stack) + LINE_SPACING_NARROW; // Gap between heading and upgrade icons
		}

		@Override
		protected boolean isVisible(ItemStack stack){
			return WandHelper.getTotalUpgrades(stack) > 0;
		}

	}

	private class TooltipElementUpgrades extends TooltipElement {

		private static final int ITEM_SIZE = 16;
		private static final int ITEM_SPACING = 2;

		public TooltipElementUpgrades(int spaceAfter){
			super(spaceAfter);
		}

		@Override
		protected boolean isVisible(ItemStack stack){
			return true; // Handled by parent
		}

		@Override
		protected int getHeight(ItemStack stack){
			int rows = 1 + (WandHelper.getTotalUpgrades(stack) * (ITEM_SIZE + ITEM_SPACING) - ITEM_SPACING)
					/ (TOOLTIP_WIDTH - TOOLTIP_BORDER * 2);
			return rows * (ITEM_SIZE + ITEM_SPACING) - ITEM_SPACING;
		}

		@Override
		protected void drawBackground(int x, int y, ItemStack stack, float partialTicks, int mouseX, int mouseY){

			GlStateManager.enableDepth();

			int x1 = 0;

			// Upgrades
			for(Item item : WandHelper.getSpecialUpgrades()){

				int level = WandHelper.getUpgradeLevel(stack, item);

				if(level > 0){

					ItemStack upgrade = new ItemStack(item, level);

					itemRender.renderItemAndEffectIntoGUI(upgrade, x + x1, y);
					itemRender.renderItemOverlayIntoGUI(fontRenderer, upgrade, x + x1, y, null);

					x1 += ITEM_SIZE + ITEM_SPACING;

					if(x1 + ITEM_SIZE > TOOLTIP_WIDTH - TOOLTIP_BORDER * 2){
						x1 = 0;
						y += ITEM_SIZE + ITEM_SPACING;
					}
				}
			}

			GlStateManager.disableDepth();
			GlStateManager.disableLighting(); // Whyyyyyy?
		}

		@Override
		protected void drawForeground(int x, int y, ItemStack stack, int mouseX, int mouseY){

			int x1 = 0;

			// Wand upgrade tooltips
			for(Item item : WandHelper.getSpecialUpgrades()){

				int level = WandHelper.getUpgradeLevel(stack, item);

				if(level > 0){
					// The javadoc for isPointInRegion is ambiguous; what it means is that the REGION is
					// relative to the GUI but the POINT isn't.
					if(isPointInRegion(x + x1, y, ITEM_SIZE, ITEM_SIZE, mouseX, mouseY)){
						ItemStack upgrade = new ItemStack(item, level);
						renderToolTip(upgrade, mouseX - guiLeft, mouseY - guiTop);
					}

					x1 += ITEM_SIZE + ITEM_SPACING;

					if(TOOLTIP_BORDER * 2 + x1 + ITEM_SIZE > TOOLTIP_WIDTH){
						x1 = 0;
						y += ITEM_SIZE + ITEM_SPACING;
					}

				}
			}

		}

	}

	private static class GuiButtonApply extends GuiButton {

		public GuiButtonApply(int id, int x, int y){
			super(id, x, y, 16, 16, I18n.format("container." + Wizardry.MODID + ":arcane_workbench.apply"));
		}

		@Override
		public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){

			// Whether the button is highlighted
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			int k = 72;
			int l = 220;
			//int colour = 14737632;

			if(this.enabled){
				if(this.hovered){
					k += this.width * 2;
					//colour = 16777120;
				}
			}else{
				k += this.width;
				//colour = 10526880;
			}

			DrawingUtils.drawTexturedRect(this.x, this.y, k, l, this.width, this.height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
			//this.drawCenteredString(minecraft.fontRenderer, this.displayString, this.x + this.width / 2,
			//		this.y + (this.height - 8) / 2, colour);
		}
	}

}