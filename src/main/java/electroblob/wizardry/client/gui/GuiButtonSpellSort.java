package electroblob.wizardry.client.gui;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.util.ISpellSortable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class GuiButtonSpellSort extends GuiButton {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/gui/spell_sort_buttons.png");
	private static final int TEXTURE_WIDTH = 32;
	private static final int TEXTURE_HEIGHT = 32;

	public final ISpellSortable.SortType sortType;

	private final ISpellSortable sortable;
	private final GuiScreen parent;

	public GuiButtonSpellSort(int id, int x, int y, ISpellSortable.SortType sortType, ISpellSortable sortable, GuiScreen parent){
		super(id, x, y, 10, 10, I18n.format("container." + Wizardry.MODID + ":arcane_workbench.sort_" + sortType.name));
		this.sortType = sortType;
		this.sortable = sortable;
		this.parent = parent;
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY, float partialTicks){

		if(this.visible){

			// Whether the button is highlighted
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			int k = 0;
			int l = this.sortType.ordinal() * this.height;

			if(sortType == sortable.getSortType()){
				k += this.width;
				if(sortable.isSortDescending()) k += this.width;
			}

			parent.mc.getTextureManager().bindTexture(TEXTURE);
			DrawingUtils.drawTexturedRect(this.x, this.y, k, l, this.width, this.height, TEXTURE_WIDTH, TEXTURE_HEIGHT);

		}
	}

	@Override
	public void drawButtonForegroundLayer(int mouseX, int mouseY){
		if(hovered) parent.drawHoveringText(this.displayString, mouseX, mouseY);
	}

}
