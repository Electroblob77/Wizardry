package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiBookshelf extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/bookshelf.png");
	/** The player inventory bound to this GUI. */
	private final InventoryPlayer playerInventory;
	/** The inventory contained within the corresponding bookshelf. */
	public IInventory bookshelfInventory;

	public GuiBookshelf(InventoryPlayer playerInv, TileEntityBookshelf bookshelfInv){
		super(new ContainerBookshelf(playerInv, bookshelfInv));
		this.playerInventory = playerInv;
		this.bookshelfInventory = bookshelfInv;
		this.allowUserInput = false;
		this.ySize = 148;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

    @Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY){
		String s = this.bookshelfInventory.getDisplayName().getUnformattedText();
		this.fontRenderer.drawString(s, 8, 6, 4210752);
		this.fontRenderer.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
	}

    @Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
	}

}