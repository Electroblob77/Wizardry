package electroblob.wizardry.client.gui.config;

import com.google.common.collect.Lists;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.gui.GuiSpellDisplay;
import electroblob.wizardry.client.gui.GuiSpellDisplay.Skin;
import electroblob.wizardry.registry.Spells;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiSelectString;
import net.minecraftforge.fml.client.config.IConfigElement;

import javax.annotation.Nullable;
import java.util.Map;

public class GuiSelectHUDSkin extends GuiSelectString {

	public GuiSelectHUDSkin(GuiScreen parentScreen, IConfigElement configElement, int slotIndex, Map<Object, String> selectableValues, Object currentValue, boolean enabled){
		super(parentScreen, configElement, slotIndex, selectableValues, currentValue, enabled);
	}
	
	@Override
	public void initGui(){
		super.initGui();
		setEntryListDimensions();
	}
	
	@Override
    protected void actionPerformed(GuiButton button){
		super.actionPerformed(button);
		setEntryListDimensions(); // Stops the entry list from resizing when a button is pressed
    }
	
	private void setEntryListDimensions(){
		this.entryList.setDimensions(150, height, 43, height-43);
		this.entryList.left = 10;
		this.entryList.maxEntryWidth = 120;
		this.entryList.headerPadding = 5;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		GlStateManager.disableLighting();
		
		if(this.currentValue instanceof String){
			
			this.drawString(this.fontRenderer, I18n.format("config." + Wizardry.MODID + ".spell_hud_skin.preview"), 170, 44, 0xffffff);
			
			int previewLeft = 170;
			int previewRight = width-10;
			int previewTop = 60;
			int previewBottom = height-43;
			
			this.drawGradientRect(previewLeft, previewTop, previewRight, previewBottom, 0x88000000, 0x88000000);
			
			int previewBorder = 10;

			Skin skin = GuiSpellDisplay.getSkin((String)this.currentValue);

			if(skin != null){

				float scale = Math.min((previewRight - previewLeft - 2 * previewBorder) / (float)skin.getWidth(),
						(previewBottom - previewTop - 2 * previewBorder) / (float)skin.getHeight());

				float x = (previewLeft + previewRight) / 2f - (skin.getWidth() * scale) / 2;
				float y = (previewBottom + previewTop) / 2f + (skin.getHeight() * scale) / 2;

				GlStateManager.pushMatrix();

				GlStateManager.scale(scale, scale, scale);

				skin.drawBackground((int)(x / scale), (int)(y / scale), false, false,
						Spells.magic_missile.getIcon(), 0.6f, false, false);

				skin.drawText((int)(x / scale), (int)(y / scale), false, false,
						Spells.none.getDisplayNameWithFormatting(),
						Spells.magic_missile.getDisplayNameWithFormatting(),
						Spells.none.getDisplayNameWithFormatting(), 0);

				GlStateManager.popMatrix();
			}

			Skin hovered = getHoveredSkin(mouseX, mouseY);

			if(hovered != null){
				this.drawToolTip(Lists.newArrayList("\u00A7a" + hovered.getName(), "\u00A7e" + hovered.getDescription()),
						mouseX, mouseY);
			}
		}
		
		GlStateManager.enableLighting();
	}
	
	/** Returns the skin corresponding to the list entry being hovered over, or null if there is none. */
	@Nullable
	private Skin getHoveredSkin(int mouseX, int mouseY){
		
		int index = this.entryList.getSlotIndexFromScreenCoords(mouseX, mouseY);
		
		if(index >= 0 && index <= this.entryList.listEntries.size() && mouseY <= this.entryList.bottom){
			
			Object object = entryList.getListEntry(index).getValue();
			
			if(object instanceof String){
				return GuiSpellDisplay.getSkin((String)object);
			}
		}
		
		return null;
	}
	
	@Override // Stops the world being visible behind the GUI when configuring from within a world
	public void drawWorldBackground(int tint){
		this.drawBackground(tint);
	}
	
}
