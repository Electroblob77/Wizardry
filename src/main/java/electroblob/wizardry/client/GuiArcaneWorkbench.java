package electroblob.wizardry.client;

import org.lwjgl.input.Keyboard;

import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class GuiArcaneWorkbench extends GuiContainer {

	private GuiButton applyBtn;
	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/gui/arcane_workbench.png");

	private IInventory playerInventory;
	private IInventory arcaneWorkbenchInventory;

	private final int tooltipWidth = 164;

	public GuiArcaneWorkbench(InventoryPlayer invPlayer, TileEntityArcaneWorkbench entity){
		super(new ContainerArcaneWorkbench(invPlayer, entity));
		this.playerInventory = invPlayer;
		this.arcaneWorkbenchInventory = entity;
		xSize = 176;
		ySize = 220;
	}

	@Override
	public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_){

		// Tests if there is a wand in the workbench and edits the positioning accordingly
		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack() && this.inventorySlots
				.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack().getItem() instanceof ItemWand){
			guiLeft = (this.width - this.xSize - tooltipWidth) / 2;
			this.applyBtn.xPosition = (this.width - tooltipWidth) / 2 + 48;
		}else{
			guiLeft = (this.width - this.xSize) / 2;
			this.applyBtn.xPosition = this.width / 2 + 48;
		}

		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack()){
			this.applyBtn.enabled = true;
		}else{
			this.applyBtn.enabled = false;
		}

		super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY){

		GlStateManager.pushAttrib();

		GlStateManager.color(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		// Main inventory
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		// Changing slots
		for(int i = 0; i < ContainerArcaneWorkbench.CRYSTAL_SLOT; i++){
			Slot slot = this.inventorySlots.getSlot(i);
			if(slot.xPos >= 0 && slot.yPos >= 0)
				this.drawTexturedModalRect(guiLeft + slot.xPos - 10, guiTop + slot.yPos - 10, 0, 220, 36, 36);
		}

		// Tooltip only drawn if there is a wand
		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack() && this.inventorySlots
				.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack().getItem() instanceof ItemWand){

			// Tooltip box
			drawTexturedModalRect(guiLeft + xSize, guiTop, xSize, 0, 256 - xSize - 4, ySize);
			drawTexturedModalRect(guiLeft + 252, guiTop, xSize + 4, 0, tooltipWidth - 2 * (256 - xSize - 4), ySize);
			drawTexturedModalRect(guiLeft + xSize + tooltipWidth - (256 - xSize - 4), guiTop, xSize + 4, 0,
					256 - xSize - 4, ySize);

			ItemStack wand = this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack();

			Spell[] spells = WandHelper.getSpells(wand);

			int i = 0;

			for(Spell spell : spells){

				boolean discovered = true;

				if(!this.mc.player.capabilities.isCreativeMode && WizardData.get(this.mc.player) != null){
					discovered = WizardData.get(this.mc.player).hasSpellBeenDiscovered(spell);
				}
				// As of Wizardry 1.2, the icons have been split off into their own texture files to allow for add-on
				// mods to add their own.
				Minecraft.getMinecraft().renderEngine
						.bindTexture(discovered ? spell.element.getIcon() : Element.MAGIC.getIcon());

				// Renders the little element icon
				WizardryUtilities.drawTexturedRect(guiLeft + xSize + 5, guiTop + 34 + 10 * i++, 8, 8);
			}

			int x = 0;
			int y = guiTop + 50 + spells.length * 10;

			// Look how much shorter this is with the WandHelper class!
			for(Item item : WandHelper.getSpecialUpgrades()){

				int level = WandHelper.getUpgradeLevel(wand, item);

				if(level > 0){
					ItemStack stack = new ItemStack(item, level);
					GlStateManager.enableDepth();
					this.itemRender.renderItemAndEffectIntoGUI(stack, guiLeft + xSize + 6 + x, y);
					this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, stack, guiLeft + xSize + 6 + x, y,
							null);
					x += 18;
					GlStateManager.disableDepth();
				}
			}
		}

		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		// Fixes the bug that caused the slot hightlight to render opaque. I don't know why it works, it just works!
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();

		GlStateManager.popAttrib();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){

		this.fontRendererObj
				.drawString(this.arcaneWorkbenchInventory.hasCustomName() ? this.arcaneWorkbenchInventory.getName()
						: I18n.format(this.arcaneWorkbenchInventory.getName()), 8, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.hasCustomName() ? this.playerInventory.getName()
				: I18n.format(this.playerInventory.getName()), 8, this.ySize - 96 + 2, 4210752);

		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack() && this.inventorySlots
				.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack().getItem() instanceof ItemWand){

			ItemStack wand = this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack();

			this.fontRendererObj.drawStringWithShadow("\u00A7f" + wand.getDisplayName(), xSize + 6, 6, 0);
			this.fontRendererObj.drawStringWithShadow(
					"\u00A77" + I18n.format("container.wizardry:arcane_workbench.mana") + " "
							+ (wand.getMaxDamage() - wand.getItemDamage()) + "/" + wand.getMaxDamage(),
					xSize + 6, 20, 0);

			Spell[] spells = WandHelper.getSpells(wand);

			int y = 34;

			for(Spell spell : spells){

				boolean discovered = true;

				if(!this.mc.player.capabilities.isCreativeMode && WizardData.get(this.mc.player) != null){
					discovered = WizardData.get(this.mc.player).hasSpellBeenDiscovered(spell);
				}

				if(discovered){
					this.fontRendererObj.drawStringWithShadow(spell.getDisplayNameWithFormatting(), xSize + 16, y, 0);
				}else{
					this.mc.standardGalacticFontRenderer.drawStringWithShadow(
							"\u00A79" + SpellGlyphData.getGlyphName(spell, this.mc.world), xSize + 16, y, 0);
				}
				y += 10;
			}

			if(WandHelper.getTotalUpgrades(wand) > 0){

				this.fontRendererObj.drawStringWithShadow(
						"\u00A7f" + I18n.format("container.wizardry:arcane_workbench.upgrades"), xSize + 6, y + 6, 0);

				int x = 0;
				y = 50 + spells.length * 10;
				// Wand upgrade tooltips
				for(Item item : WandHelper.getSpecialUpgrades()){

					int level = WandHelper.getUpgradeLevel(wand, item);

					if(level > 0){
						// The javadoc for isPointInRegion is ambiguous; what it means is that the REGION is
						// relative to the GUI but the POINT isn't.
						if(isPointInRegion(xSize + 6 + x, y, 16, 16, mouseX, mouseY)){
							ItemStack stack = new ItemStack(item, level);
							this.renderToolTip(stack, mouseX - guiLeft, mouseY - guiTop);
						}
						x += 18;
					}
				}
			}
		}
	}

	@Override
	public void initGui(){
		this.mc.player.openContainer = this.inventorySlots;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(this.applyBtn = new GuiButtonApply(0, this.width / 2 + 48, this.height / 2 + 3));
	}

	@Override
	public void onGuiClosed(){
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button){
		if(button.enabled){
			if(button.id == 0){
				// Packet building
				IMessage msg = new PacketControlInput.Message(PacketControlInput.ControlType.APPLY_BUTTON);
				WizardryPacketHandler.net.sendToServer(msg);
			}
		}
	}

}