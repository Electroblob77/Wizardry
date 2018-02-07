package electroblob.wizardry.constants;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum Element {

	/**
	 * The 'default' element, with {@link electroblob.wizardry.spell.MagicMissile MagicMissile} being its only spell.
	 */
	MAGIC(new Style().setColor(TextFormatting.GRAY), "simple", Wizardry.MODID), FIRE(
			new Style().setColor(TextFormatting.DARK_RED), "fire",
			Wizardry.MODID), ICE(new Style().setColor(TextFormatting.AQUA), "ice", Wizardry.MODID), LIGHTNING(
					new Style().setColor(TextFormatting.DARK_AQUA), "lightning",
					Wizardry.MODID), NECROMANCY(new Style().setColor(TextFormatting.DARK_PURPLE), "necromancy",
							Wizardry.MODID), EARTH(new Style().setColor(TextFormatting.DARK_GREEN), "earth",
									Wizardry.MODID), SORCERY(new Style().setColor(TextFormatting.GREEN), "sorcery",
											Wizardry.MODID), HEALING(new Style().setColor(TextFormatting.YELLOW),
													"healing", Wizardry.MODID);

	/** Display colour for this element */
	private final Style colour;
	/** Unlocalised name for this element */
	private final String unlocalisedName;
	/** The {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI) */
	private final ResourceLocation icon;

	private Element(Style colour, String name, String modid){
		this.colour = colour;
		this.unlocalisedName = name;
		this.icon = new ResourceLocation(modid, "textures/gui/element_icon_" + unlocalisedName + ".png");
	}

	/** Returns the translated display name of this element, without formatting. */
	@SideOnly(Side.CLIENT)
	public String getDisplayName(){
		return net.minecraft.client.resources.I18n.format("element." + getUnlocalisedName());
	}

	/** Returns the {@link Style} object representing the colour of this element. */
	public Style getColour(){
		return colour;
	}

	/** Returns the string formatting code which corresponds to the colour of this element. */
	public String getFormattingCode(){
		return colour.getFormattingCode();
	}

	/** Returns the translated display name for wizards of this element, shown in the trading GUI. */
	public ITextComponent getWizardName(){
		return new TextComponentTranslation("element." + getUnlocalisedName() + ".wizard");
	}

	/** Returns this element's unlocalised name. */
	public String getUnlocalisedName(){
		return unlocalisedName;
	}

	/** Returns the {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI). */
	public ResourceLocation getIcon(){
		return icon;
	}
}
