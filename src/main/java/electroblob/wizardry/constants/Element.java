package electroblob.wizardry.constants;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public enum Element implements IStringSerializable {

	/** The 'default' element, with {@link electroblob.wizardry.registry.Spells#magic_missile magic missile} being its
	 * only spell. */
	MAGIC(new Style().setColor(TextFormatting.GRAY), "magic"),
	FIRE(new Style().setColor(TextFormatting.DARK_RED), "fire"),
	ICE(new Style().setColor(TextFormatting.AQUA), "ice"),
	LIGHTNING(new Style().setColor(TextFormatting.DARK_AQUA), "lightning"),
	NECROMANCY(new Style().setColor(TextFormatting.DARK_PURPLE), "necromancy"),
	EARTH(new Style().setColor(TextFormatting.DARK_GREEN), "earth"),
	SORCERY(new Style().setColor(TextFormatting.GREEN), "sorcery"),
	HEALING(new Style().setColor(TextFormatting.YELLOW), "healing");

	/** Display colour for this element */
	private final Style colour;
	/** Unlocalised name for this element */
	private final String unlocalisedName;
	/** The {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI) */
	private final ResourceLocation icon;

	private String modid;
	/** true if this element should have worldgen structures generated (Obelisk, Shrine) */
	private final boolean worldgen;
	/** true if evil/good wizards of this element should naturally spawn */
	private final boolean wizards;

	Element(Style colour, String name){
		this(colour, name, Wizardry.MODID, true, true);
	}

	Element(Style colour, String name, String modid, boolean worldgen, boolean wizards){
		this.colour = colour;
		this.unlocalisedName = name;
		this.icon = new ResourceLocation(modid, "textures/gui/container/element_icon_" + unlocalisedName + ".png");
		this.modid = modid;
		this.worldgen = worldgen;
		this.wizards = wizards;
	}

	/** Returns the element with the given name, or throws an {@link java.lang.IllegalArgumentException} if no such
	 * element exists. */
	public static Element fromName(String name){

		for(Element element : values()){
			if(element.unlocalisedName.equals(name)) return element;
		}

		throw new IllegalArgumentException("No such element with unlocalised name: " + name);
	}

	/** Same as {@link Element#fromName(String)}, but returns the given fallback instead of throwing an exception if no
	 * element matches the given string. */
	@Nullable
	public static Element fromName(String name, @Nullable Element fallback){

		for(Element element : values()){
			if(element.unlocalisedName.equals(name)) return element;
		}

		return fallback;
	}

	/** Returns the translated display name of this element, without formatting. */
	public String getDisplayName(){
		return Wizardry.proxy.translate("element." + getName());
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
		return new TextComponentTranslation("element." + getName() + ".wizard");
	}

	/** Returns this element's unlocalised name. Also used as the serialised string in block properties. */
	@Override
	public String getName(){
		return unlocalisedName;
	}

	/** Returns the {@link ResourceLocation} for this element's 8x8 icon (displayed in the arcane workbench GUI). */
	public ResourceLocation getIcon(){
		return icon;
	}

	public boolean hasWorldgen() { return worldgen; }

	public boolean hasWizards() { return wizards; }

	public String getModid() { return modid; }
}
