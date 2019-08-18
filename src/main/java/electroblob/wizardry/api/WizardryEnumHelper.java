package electroblob.wizardry.api;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellProperties;
import net.minecraft.util.text.Style;
import net.minecraftforge.common.util.EnumHelper;

/**
 * This class contains methods similar to those in {@link EnumHelper} specific to wizardry's enum types.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
public final class WizardryEnumHelper {

	// Make sure these are updated if the relevant constructors are updated!
	// Can't we do some kind of reflection to access them and generate these arrays?
	private static final Class[] TIER_ARGUMENTS = new Class[]{Integer.class, Integer.class, Integer.class, Style.class, String.class};
	private static final Class[] ELEMENT_ARGUMENTS = new Class[]{Style.class, String.class, String.class};
	private static final Class[] SPELL_TYPE_ARGUMENTS = new Class[]{String.class};
	private static final Class[] SPELL_CONTEXT_ARGUMENTS = new Class[]{String.class};

	/**
	 * Wrapper for the generic method {@link EnumHelper#addEnum(Class, String, Class[], Object...)} which is
	 * specifically for adding new tiers. Use this method in preference to the generic one in case the constructor
	 * parameters change for {@code Tier}.
	 * <p></p>
	 * As of version 4.2, wizardry now has partial support for externally-added tiers; you'll need to do some of the
	 * legwork yourself though.
	 *
	 * @param codeName The name of the enum constant in the code. This will be returned if you call toString() on the
	 *                 resulting enum constant; other than that it doesn't really make much difference.
	 * @param maxCharge The maximum charge for wands of this tier.
	 * @param upgradeLimit The maximum total number of special upgrades that can be applied to wands of this tier.
	 * @param weight The weight of this tier in the standard weighting.
	 * @param colour The colour of text associated with this tier, as a style object.
	 * @param name The unlocalised name of this tier, as used in translation keys.
	 * @return The resulting {@code Tier} enum constant.
	 */
	public static Tier addTier(String codeName, int maxCharge, int upgradeLimit, int weight, Style colour, String name){
		return EnumHelper.addEnum(Tier.class, codeName, TIER_ARGUMENTS, maxCharge, upgradeLimit, weight, colour, name);
	}

	/**
	 * Wrapper for the generic method {@link EnumHelper#addEnum(Class, String, Class[], Object...)} which is
	 * specifically for adding new elements. Use this method in preference to the generic one in case the constructor
	 * parameters change for {@code Element}.
	 * <p></p>
	 * As of version 4.2, wizardry now has full support for externally-added elements.
	 *
	 * @param codeName The name of the enum constant in the code. This will be returned if you call toString() on the
	 *                 resulting enum constant; other than that it doesn't really make much difference.
	 * @param colour The colour of text associated with this element, as a style object.
	 * @param name The unlocalised name of this element, as used in translation keys.
	 * @param modID The mod ID of the mod that added this element, for icon rendering purposes.
	 * @return The resulting {@code Element} enum constant.
	 */
	// This is the only one that needs the mod ID argument because elements are the only ones that have icons
	// For some reason Minecraft doesn't seem to care about the resource domain for lang files, it just pools them
	public static Element addElement(String codeName, Style colour, String name, String modID){
		return EnumHelper.addEnum(Element.class, codeName, ELEMENT_ARGUMENTS, colour, name, modID);
	}

	/**
	 * Wrapper for the generic method {@link EnumHelper#addEnum(Class, String, Class[], Object...)} which is
	 * specifically for adding new spell types. Use this method in preference to the generic one in case the constructor
	 * parameters change for {@code SpellType}.
	 * <p></p>
	 * As of version 4.2, wizardry now has full support for externally-added spell types.
	 *
	 * @param codeName The name of the enum constant in the code. This will be returned if you call toString() on the
	 *                 resulting enum constant; other than that it doesn't really make much difference.
	 * @param name The unlocalised name of this spell type, as used in translation keys.
	 * @return The resulting {@code SpellType} enum constant.
	 */
	public static SpellType addSpellType(String codeName, String name){
		return EnumHelper.addEnum(SpellType.class, codeName, SPELL_TYPE_ARGUMENTS, name);
	}

	/**
	 * Wrapper for the generic method {@link EnumHelper#addEnum(Class, String, Class[], Object...)} which is
	 * specifically for adding new spell contexts (for use in spell property JSON files). Use this method in preference
	 * to the generic one in case the constructor parameters change for {@code Context}.
	 *
	 * @param codeName The name of the enum constant in the code. This will be returned if you call toString() on the
	 *                 resulting enum constant; other than that it doesn't really make much difference.
	 * @param name The identifier for this spell context, as used in the JSON file.
	 * @return The resulting {@code Context} enum constant.
	 */
	public static SpellProperties.Context addSpellContext(String codeName, String name){
		return EnumHelper.addEnum(SpellProperties.Context.class, codeName, SPELL_CONTEXT_ARGUMENTS, name);
	}

}
