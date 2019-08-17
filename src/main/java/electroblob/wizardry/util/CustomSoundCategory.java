package electroblob.wizardry.util;

import com.google.common.collect.Maps;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

/**
 * Add a new CONSTANT and reference name to net.minecraft.util.SoundCategory
 *
 * This allows the display of a volume control in the "Music & Sound Options" dialog.
 * Unfortunately the GuiScreenOptionsSounds dialog does not auto size
 * properly and move the Done button lower on the screen.
 *
 * To initialize the class create an instance during FMLPreInitializationEvent in
 * the file with the @Mod annotation or your common proxy class.
 *
 * Usage example: static final SoundCategory SC_MXTUNE = MODSoundCategory.add("MXTUNE");
 *
 * The language file key is "soundCategory.mxtune"
 * The game settings "options.txt" key is "soundCategory_mxtune"
 *
 * To use the MXTUNE enum constant in code it must be referenced by name because
 * SoundCategory.MXTUNE does not exist at compile time.
 *   e.g. SoundCategory.getByName("mxtune");
 *
 * @author Paul Boese aka Aeronica (modified for 1.12.2 and for conciseness/clarity by Electroblob)
 * @see <a href=http://www.minecraftforge.net/forum/topic/42439-adding-additional-soundcategorys/>
 * www.minecraftforge.net/forum/topic/42439-adding-additional-soundcategorys/</a>
 */
public final class CustomSoundCategory {

	private static final String SRG_soundLevels = "field_186714_aM";
	private static final String SRG_SOUND_CATEGORIES = "field_187961_k";
	// >> Electroblob: Don't know why this was instantiated at all, surely it's a static helper class?

	private CustomSoundCategory(){}

	/**
	 * Adds a new custom sound category, performing the necessary changes to GameSettings and
	 *
	 * @param name A unique name for the sound category
	 * @return The resulting SoundCategory object
	 * @throws IllegalArgumentException if name is not unique
	 */
	public static SoundCategory add(String name){

		Map<String, SoundCategory> SOUND_CATEGORIES;

		String constantName;
		String referenceName;
		SoundCategory soundCategory;
		// >> Electroblob: Constructors were unnecessary since strings are immutable
		constantName = name.toUpperCase().replace(" ", "");
		referenceName = constantName.toLowerCase();
		// >> Electroblob: Removed array surrounding varargs argument
		soundCategory =  EnumHelper.addEnum(SoundCategory.class , constantName, new Class[]{String.class}, referenceName);
		SOUND_CATEGORIES = ObfuscationReflectionHelper.getPrivateValue(SoundCategory.class, SoundCategory.VOICE ,"SOUND_CATEGORIES", SRG_SOUND_CATEGORIES);
		if (SOUND_CATEGORIES.containsKey(referenceName))
			// >> Electroblob: changed from Error to IllegalArgumentException
			throw new IllegalArgumentException("Clash in Sound Category name pools! Cannot insert " + constantName);
		SOUND_CATEGORIES.put(referenceName, soundCategory);
		if (FMLLaunchHandler.side() == Side.CLIENT) setSoundLevels();

		return soundCategory;
	}

	/** Game sound level options settings only exist on the client side */
	@SideOnly(Side.CLIENT)
	private static void setSoundLevels(){
		// SoundCategory now contains 'name' sound category so build a new map
		// >> Electroblob: Converted to local variable
		Map<SoundCategory, Float> soundLevels = Maps.newEnumMap(SoundCategory.class);
		// Replace the map in the GameSettings.class
		// >> Electroblob: Fully qualified names, because this class gets loaded on both sides
		ObfuscationReflectionHelper.setPrivateValue(net.minecraft.client.settings.GameSettings.class,
				net.minecraft.client.Minecraft.getMinecraft().gameSettings, soundLevels,
				"soundLevels", SRG_soundLevels);
	}

}