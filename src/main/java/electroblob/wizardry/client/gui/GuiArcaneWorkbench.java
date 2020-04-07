package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.inventory.SlotBookList;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.util.ISpellSortable;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Locale;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiArcaneWorkbench extends GuiContainer {

	private GuiButton applyBtn;
	public static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/gui/arcane_workbench.png");

	private InventoryPlayer playerInventory;
	private IInventory arcaneWorkbenchInventory;
	private ContainerArcaneWorkbench arcaneWorkbenchContainer;

	private static final int TOOLTIP_WIDTH = 144;
	private static final int TOOLTIP_TEXT_INSET = 6;

	private static final int BOOKSHELF_UI_WIDTH = 122;

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

	private static final int PROGRESSION_BAR_WIDTH = 152;
	private static final int PROGRESSION_BAR_HEIGHT = 3;

	private static final int HALO_DIAMETER = 156;

	private static final int TEXTURE_WIDTH = 512;
	private static final int TEXTURE_HEIGHT = 512;

	private int animationTimer = 0;
	private static final int ANIMATION_DURATION = 20;

	private GuiTextField searchField;
	private boolean searchNeedsClearing;

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
		this.buttonList.add(new GuiButtonSpellSort(1, this.guiLeft - 44, this.guiTop + 8, ISpellSortable.SortType.TIER, arcaneWorkbenchContainer, this));
		this.buttonList.add(new GuiButtonSpellSort(2, this.guiLeft - 31, this.guiTop + 8, ISpellSortable.SortType.ELEMENT, arcaneWorkbenchContainer, this));
		this.buttonList.add(new GuiButtonSpellSort(3, this.guiLeft - 18, this.guiTop + 8, ISpellSortable.SortType.ALPHABETICAL, arcaneWorkbenchContainer, this));

		this.searchField = new GuiTextField(0, this.fontRenderer, this.guiLeft - 113, this.guiTop + 22, 104, this.fontRenderer.FONT_HEIGHT);
		this.searchField.setMaxStringLength(50);
		this.searchField.setEnableBackgroundDrawing(false);
		this.searchField.setVisible(true);
		this.searchField.setTextColor(16777215);
		this.searchField.setCanLoseFocus(false);
		this.searchField.setFocused(true);

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
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){

		this.drawDefaultBackground();

		GlStateManager.color(1, 1, 1, 1); // Just in case

		boolean mouseHeld = Mouse.isButtonDown(0);

		if(!scrolling && mouseHeld && getMaxScrollRows() > 0 && isPointInRegion(SCROLL_BAR_LEFT - BOOKSHELF_UI_WIDTH,
				SCROLL_BAR_TOP, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, mouseX, mouseY)){
			scrolling = true;
		}

		if(!mouseHeld || getMaxScrollRows() == 0) scrolling = false;

		if(scrolling){
			scroll = MathHelper.clamp((float)(mouseY - SCROLL_BAR_TOP - SCROLL_HANDLE_HEIGHT/2 - guiTop)
					/(SCROLL_BAR_HEIGHT - SCROLL_HANDLE_HEIGHT), 0, 1);
			arcaneWorkbenchContainer.scrollTo((int)(getMaxScrollRows() * scroll + 0.5f));
		}

		Slot slot = this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT);

		// Tests if there is a wand in the workbench and edits the positioning accordingly
		if(slot.getHasStack() && slot.getStack().getItem() instanceof IWorkbenchItem
				&& ((IWorkbenchItem)slot.getStack().getItem()).showTooltip(slot.getStack())){
			xSize = MAIN_GUI_WIDTH + TOOLTIP_WIDTH;
//			guiLeft = (this.width - this.xSize) / 2;
//			this.applyBtn.x = (this.width - TOOLTIP_WIDTH) / 2 + 64;
		}else{
			xSize = MAIN_GUI_WIDTH;
//			guiLeft = (this.width - this.xSize) / 2;
//			this.applyBtn.x = this.width / 2 + 64;
		}

		this.applyBtn.enabled = slot.getHasStack();

		super.drawScreen(mouseX, mouseY, partialTicks);

		// Required now, or item mouseover tooltips won't render.
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		// Animation

		// Grey background
		DrawingUtils.drawTexturedRect(guiLeft + RUNE_LEFT, guiTop + RUNE_TOP, MAIN_GUI_WIDTH + TOOLTIP_WIDTH, 0,
				RUNE_WIDTH, RUNE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Yellow 'halo'
		if(animationTimer > 0){

			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

			int x = guiLeft + RUNE_LEFT + RUNE_WIDTH/2;
			int y = guiTop + RUNE_TOP + RUNE_HEIGHT/2;

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
		DrawingUtils.drawTexturedRect(guiLeft, guiTop, 0, 0, MAIN_GUI_WIDTH, ySize, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		float opacity = (animationTimer + partialTicks)/ANIMATION_DURATION;

		// Changing slots
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
		DrawingUtils.drawTexturedRect(guiLeft - BOOKSHELF_UI_WIDTH, guiTop, 0, 256, BOOKSHELF_UI_WIDTH, ySize, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Scroll bar
		DrawingUtils.drawTexturedRect(guiLeft - BOOKSHELF_UI_WIDTH + SCROLL_BAR_LEFT,
				guiTop + SCROLL_BAR_TOP + (int)(scroll * (SCROLL_BAR_HEIGHT - SCROLL_HANDLE_HEIGHT) + 0.5f),
				getMaxScrollRows() > 0 ? 0 : SCROLL_BAR_WIDTH, 476,
				SCROLL_BAR_WIDTH, SCROLL_HANDLE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		// Tooltip only drawn if there is a wand
		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getHasStack()){

			ItemStack stack = this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getStack();

			if(!(stack.getItem() instanceof IWorkbenchItem)){
				Wizardry.logger.warn("Invalid item in central slot of arcane workbench, how did that get there?!");
				return;
			}

			if(((IWorkbenchItem)stack.getItem()).showTooltip(stack)){

				// Tooltip box
				DrawingUtils.drawTexturedRect(guiLeft + MAIN_GUI_WIDTH, guiTop, MAIN_GUI_WIDTH, 0, TOOLTIP_WIDTH, ySize, TEXTURE_WIDTH, TEXTURE_HEIGHT);

				int y = guiTop + 20;

				if(stack.getItem() instanceof IManaStoringItem && ((IManaStoringItem)stack.getItem()).showManaInWorkbench(this.mc.player, stack)){
					y += 14;
				}

				// Progression bar
				if(stack.getItem() instanceof ItemWand && !Wizardry.settings.legacyWandLevelling){

					y += 10;

					Tier tier = ((ItemWand)stack.getItem()).tier;

					float progressFraction = 1;

					if(tier != Tier.MASTER){
						progressFraction = (float)WandHelper.getProgression(stack) / Tier.values()[tier.level + 1].progression;
					}

					DrawingUtils.drawTexturedRect(guiLeft + MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET, y, MAIN_GUI_WIDTH, ySize + PROGRESSION_BAR_HEIGHT, PROGRESSION_BAR_WIDTH, PROGRESSION_BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
					int width = (int)(PROGRESSION_BAR_WIDTH * progressFraction);
					DrawingUtils.drawTexturedRect(guiLeft + MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET, y, MAIN_GUI_WIDTH, ySize, width, PROGRESSION_BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);

					y += 8;
				}

				if(stack.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)stack.getItem()).showSpellsInWorkbench(this.mc.player, stack)){

					Spell[] spells = ((ISpellCastingItem)stack.getItem()).getSpells(stack);

					GlStateManager.enableBlend();

					for(Spell spell : spells){

						boolean discovered = true;

						if(!this.mc.player.isCreative() && WizardData.get(this.mc.player) != null){
							discovered = WizardData.get(this.mc.player).hasSpellBeenDiscovered(spell);
						}
						// As of Wizardry 1.2, the icons have been split off into their own texture files to allow for add-on
						// mods to add their own.
						Minecraft.getMinecraft().renderEngine
								.bindTexture(discovered ? spell.getElement().getIcon() : Element.MAGIC.getIcon());

						// Renders the little element icon
						DrawingUtils.drawTexturedRect(guiLeft + MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET - 1, y, 8, 8);

						y += 10;
					}
				}

				GlStateManager.disableBlend();

				int x = 0;
				y += 16;

				// Look how much shorter this is with the WandHelper class!
				for(Item item : WandHelper.getSpecialUpgrades()){

					int level = WandHelper.getUpgradeLevel(stack, item);

					if(level > 0){
						ItemStack stack1 = new ItemStack(item, level);
						GlStateManager.enableDepth();
						this.itemRender.renderItemAndEffectIntoGUI(stack1, guiLeft + MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET + x, y);
						this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, stack1, guiLeft + MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET + x, y,
								null);
						x += 18;
						GlStateManager.disableDepth();
						GlStateManager.disableLighting(); // Whyyyyyy?
					}
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

		this.fontRenderer
				.drawString(this.arcaneWorkbenchInventory.hasCustomName() ? this.arcaneWorkbenchInventory.getName()
						: I18n.format(this.arcaneWorkbenchInventory.getName()), 8, 6, 4210752);
		this.fontRenderer.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
				: I18n.format(this.playerInventory.getName()), 8, this.ySize - 96 + 2, 4210752);
		this.fontRenderer.drawString(I18n.format("container." + Wizardry.MODID + ":arcane_workbench.bookshelves"), 8 - BOOKSHELF_UI_WIDTH, 6, 4210752);

		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getHasStack()){

			ItemStack stack = this.inventorySlots.getSlot(ContainerArcaneWorkbench.CENTRE_SLOT).getStack();

			if(!(stack.getItem() instanceof IWorkbenchItem)){
				Wizardry.logger.warn("Invalid item in central slot of arcane workbench, how did that get there?!");
				return;
			}

			if(((IWorkbenchItem)stack.getItem()).showTooltip(stack)){

				int y = 6;

				this.fontRenderer.drawStringWithShadow("\u00A7f" + stack.getDisplayName(), MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET, y, 0);

				if(stack.getItem() instanceof IManaStoringItem && ((IManaStoringItem)stack.getItem()).showManaInWorkbench(this.mc.player, stack)){
					y += 14;
					this.fontRenderer.drawStringWithShadow(
							"\u00A77" + I18n.format("container." + Wizardry.MODID + ":arcane_workbench.mana")
									+ " " + ((IManaStoringItem)stack.getItem()).getMana(stack) + "/"
									+ ((IManaStoringItem)stack.getItem()).getManaCapacity(stack),
							MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET, y, 0);
				}

				// Progression tier text
				if(stack.getItem() instanceof ItemWand && !Wizardry.settings.legacyWandLevelling){

					y += 14;

					Tier tier = ((ItemWand)stack.getItem()).tier;

					this.fontRenderer.drawStringWithShadow(tier.getDisplayNameWithFormatting(), MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET, y, 0);

					if(tier != Tier.MASTER){
						Tier nextTier = Tier.values()[tier.level + 1];
						String s = TextFormatting.DARK_GRAY.toString() + nextTier.getDisplayName();
						if(WandHelper.getProgression(stack) >= nextTier.progression) s = nextTier.getDisplayNameWithFormatting();
						this.fontRenderer.drawStringWithShadow(s, MAIN_GUI_WIDTH + TOOLTIP_WIDTH
								- TOOLTIP_TEXT_INSET - fontRenderer.getStringWidth(s), y, 0);
					}

					y += 4;
				}

				y += 14;

				if(stack.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)stack.getItem()).showSpellsInWorkbench(this.mc.player, stack)){

					Spell[] spells = ((ISpellCastingItem)stack.getItem()).getSpells(stack);

					for(Spell spell : spells){

						boolean discovered = true;

						if(!this.mc.player.isCreative() && WizardData.get(this.mc.player) != null){
							discovered = WizardData.get(this.mc.player).hasSpellBeenDiscovered(spell);
						}

						if(discovered){
							this.fontRenderer.drawStringWithShadow(spell.getDisplayNameWithFormatting(), MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET + 10, y, 0);
						}else{
							this.mc.standardGalacticFontRenderer.drawStringWithShadow(
									"\u00A79" + SpellGlyphData.getGlyphName(spell, this.mc.world), MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET + 10, y, 0);
						}
						y += 10;
					}
				}

				if(WandHelper.getTotalUpgrades(stack) > 0){

					y += 6;

					this.fontRenderer.drawStringWithShadow("\u00A7f" + I18n.format("container."
							+ Wizardry.MODID + ":arcane_workbench.upgrades"), MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET, y, 0);

					int x = 0;
					y += 10;

					// Wand upgrade tooltips
					for(Item item : WandHelper.getSpecialUpgrades()){

						int level = WandHelper.getUpgradeLevel(stack, item);

						if(level > 0){
							// The javadoc for isPointInRegion is ambiguous; what it means is that the REGION is
							// relative to the GUI but the POINT isn't.
							if(isPointInRegion(MAIN_GUI_WIDTH + TOOLTIP_TEXT_INSET + x, y, 16, 16, mouseX, mouseY)){
								ItemStack stack1 = new ItemStack(item, level);
								this.renderToolTip(stack1, mouseX - guiLeft, mouseY - guiTop);
							}
							x += 18;
						}
					}
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
	public void handleMouseInput() throws IOException{

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

	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(ContainerArcaneWorkbench.EMPTY_SLOT_CRYSTAL);
		event.getMap().registerSprite(ContainerArcaneWorkbench.EMPTY_SLOT_UPGRADE);
	}

}