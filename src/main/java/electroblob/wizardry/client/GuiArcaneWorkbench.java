package electroblob.wizardry.client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.tileentity.SlotWizardry;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

public class GuiArcaneWorkbench extends GuiContainer {

	private GuiButton applyBtn;
	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/gui/arcane_workbench.png");

	private IInventory playerInventory;
	private IInventory arcaneWorkbenchInventory;

	private final int tooltipWidth = 164;

	public GuiArcaneWorkbench(InventoryPlayer invPlayer, TileEntityArcaneWorkbench entity) {
		super(new ContainerArcaneWorkbench(invPlayer, entity));
		this.playerInventory = invPlayer;
		this.arcaneWorkbenchInventory = entity;
		xSize = 176;
		ySize = 220;
	}

	@Override
	public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_){

		// Tests if there is a wand in the workbench and edits the positioning accordingly
		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack() && this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack().getItem() instanceof ItemWand){
			guiLeft = (this.width - this.xSize - tooltipWidth)/2;
			this.applyBtn.xPosition = (this.width - tooltipWidth)/2 + 48;
		}else{
			guiLeft = (this.width - this.xSize)/2;
			this.applyBtn.xPosition = this.width/2 + 48;
		}

		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack()){
			this.applyBtn.enabled = true;
		}else{
			this.applyBtn.enabled = false;
		}

		super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
	}

	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){

		this.fontRendererObj.drawString(this.arcaneWorkbenchInventory.hasCustomInventoryName() ? this.arcaneWorkbenchInventory.getInventoryName() : I18n.format(this.arcaneWorkbenchInventory.getInventoryName()), 8, 6, 4210752);
		this.fontRendererObj.drawString(this.playerInventory.hasCustomInventoryName() ? this.playerInventory.getInventoryName() : I18n.format(this.playerInventory.getInventoryName()), 8, this.ySize - 96 + 2, 4210752);

		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack() && this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack().getItem() instanceof ItemWand){

			ItemStack wand = this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack();

			this.fontRendererObj.drawStringWithShadow("\u00A7f" + wand.getDisplayName(), xSize + 6, 6, 0);
			this.fontRendererObj.drawStringWithShadow("\u00A77" + StatCollector.translateToLocal("container.arcaneWorkbench.mana") + " " + (wand.getMaxDamage() - wand.getItemDamage()) + "/" + wand.getMaxDamage(), xSize + 6, 20, 0);

			Spell[] spells = WandHelper.getSpells(wand);

			int y = 34;

			for(Spell spell : spells){

				boolean discovered = true;

				if(!this.mc.thePlayer.capabilities.isCreativeMode && ExtendedPlayer.get(this.mc.thePlayer) != null){
					discovered = ExtendedPlayer.get(this.mc.thePlayer).hasSpellBeenDiscovered(spell);
				}

				if(discovered){
					this.fontRendererObj.drawStringWithShadow(spell.getDisplayNameWithFormatting(), xSize + 16, y, 0);
				}else{
					this.mc.standardGalacticFontRenderer.drawStringWithShadow("\u00A79" + SpellGlyphData.getGlyphName(spell, this.mc.theWorld), xSize + 16, y, 0);
				}
				y += 10;
			}
			
			if(WandHelper.getTotalUpgrades(wand) > 0){
				
				this.fontRendererObj.drawStringWithShadow("\u00A7f" + StatCollector.translateToLocal("container.arcaneWorkbench.upgrades"), xSize + 6, y + 6, 0);
				
				int x = 0;
				y = guiTop + 50 + spells.length*10;
				// Wand upgrade tooltips
				for(Item item : WandHelper.getSpecialUpgrades()){
					
					int level = WandHelper.getUpgradeLevel(wand, item);
					
					if(level > 0){
						// No idea why this is -16 instead of +6, but it works!
						if(isPointInRegion(xSize -16 + x, y, 16, 16, mouseX + guiLeft, mouseY + guiTop)){
							ItemStack stack = new ItemStack(item, level);
							this.drawItemStackTooltip(stack, mouseX - guiLeft, mouseY - guiTop);
						}
						x += 18;
					}
				}
			}
		}
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {

		GL11.glColor4f(1F, 1F, 1F, 1F);
		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		// Main inventory
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		// Changing slots
		for(int i=0; i<ContainerArcaneWorkbench.CRYSTAL_SLOT; i++){
			Slot slot = this.inventorySlots.getSlot(i);
			if(slot.xDisplayPosition >=0 && slot.yDisplayPosition >= 0)
				this.drawTexturedModalRect(guiLeft + slot.xDisplayPosition - 10, guiTop + slot.yDisplayPosition - 10,
						64, 220, 36, 36);
		}

		// Tooltip only drawn if there is a wand
		if(this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getHasStack() && this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack().getItem() instanceof ItemWand){

			// Tooltip box
			drawTexturedModalRect(guiLeft + xSize, guiTop, xSize, 0, 256 - xSize - 4, ySize);
			drawTexturedModalRect(guiLeft + 252, guiTop, xSize + 4, 0, tooltipWidth - 2*(256 - xSize - 4), ySize);
			drawTexturedModalRect(guiLeft + xSize + tooltipWidth - (256 - xSize - 4), guiTop, xSize + 4, 0, 256 - xSize - 4, ySize);

			ItemStack wand = this.inventorySlots.getSlot(ContainerArcaneWorkbench.WAND_SLOT).getStack();

			Spell[] spells = WandHelper.getSpells(wand);

			int i=0;

			for(Spell spell : spells){

				boolean discovered = true;

				if(!this.mc.thePlayer.capabilities.isCreativeMode && ExtendedPlayer.get(this.mc.thePlayer) != null){
					discovered = ExtendedPlayer.get(this.mc.thePlayer).hasSpellBeenDiscovered(spell);
				}

				// Renders the little element icon if there is an element
				this.drawTexturedModalRect(guiLeft + xSize + 5, guiTop + 34 + 10*i++, discovered ? spell.element.ordinal()*8 : 0, 220, 8, 8);
			}

			int x = 0;
			int y = guiTop + 50 + spells.length*10;
			
			// Look how much shorter this is with the WandHelper class!
			for(Item item : WandHelper.getSpecialUpgrades()){
				
				int level = WandHelper.getUpgradeLevel(wand, item);
				
				if(level > 0){
					ItemStack stack = new ItemStack(item, level);
					this.itemRender.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, guiLeft + xSize + 6 + x, y);
					this.itemRender.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, guiLeft + xSize + 6 + x, y);
					x += 18;
				}
			}
		}

		Minecraft.getMinecraft().renderEngine.bindTexture(texture);

		for(Object object : this.inventorySlots.inventorySlots){
			if(object instanceof Slot){

				Slot slot = (Slot)object;

				// Tests whether I have added a background texture and if so actually renders it
				if(!slot.getHasStack() && slot instanceof SlotWizardry && ((SlotWizardry)slot).backgroundIndex > -1){
					// Taken directly from GuiContainer#func_146977_a, with a few changes.
					this.zLevel = 100.0F;
					itemRender.zLevel = 100.0F;
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_BLEND);
					this.drawTexturedModalRect(guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, ((SlotWizardry)slot).backgroundIndex*16, 240, 16, 16);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glEnable(GL11.GL_LIGHTING);
					itemRender.zLevel = 0.0F;
					this.zLevel = 0.0F;
				}
			}
		}
	}

	public void initGui()
	{
		this.mc.thePlayer.openContainer = this.inventorySlots;
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(this.applyBtn = new GuiButtonApply(0, this.width/2 + 48, this.height/2 + 3));
	}

	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	protected void actionPerformed(GuiButton par1GuiButton)
	{
		if (par1GuiButton.enabled)
		{
			if (par1GuiButton.id == 0)
			{
				// Packet building
				IMessage msg = new PacketControlInput.Message(0);
				WizardryPacketHandler.net.sendToServer(msg);
			}
		}
	}
	
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

}

