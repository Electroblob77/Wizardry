package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.enchantment.EnchantmentMagicProtection;
import electroblob.wizardry.enchantment.EnchantmentMagicSword;
import electroblob.wizardry.enchantment.EnchantmentTimed;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

/**
 * Class responsible for defining, storing and registering all of wizardry's enchantments.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryEnchantments {

	private WizardryEnchantments(){} // No instances!

	// At the moment these enchantments generate on books in dungeon chests due to a bad bit of code (EnchantRandomly:49).
	// No idea how to fix this because I have no way of hooking into that code... removing the enchantments from the
	// registry works, but breaks everything else!

	// For the time being, a dynamic solution will have to do, i.e. intercept the book when it is generated and
	// reassign its enchantment.

	// All of these have custom classes, so the unlocalised name (referred to simply as 'name' for enchantments) is
	// dealt with inside those classes.

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder(){ return null; }
	
	public static final Enchantment magic_sword = placeholder();
	public static final Enchantment magic_bow = placeholder();
	public static final Enchantment flaming_weapon = placeholder();
	public static final Enchantment freezing_weapon = placeholder();

	public static final Enchantment magic_protection = placeholder();
	public static final Enchantment frost_protection = placeholder();
	public static final Enchantment shock_protection = placeholder();

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Enchantment> event){

		event.getRegistry().register(new EnchantmentMagicSword().setRegistryName(Wizardry.MODID, "magic_sword"));
		event.getRegistry().register(new EnchantmentTimed().setRegistryName(Wizardry.MODID, "magic_bow"));
		event.getRegistry().register(new EnchantmentTimed().setRegistryName(Wizardry.MODID, "flaming_weapon"));
		event.getRegistry().register(new EnchantmentTimed().setRegistryName(Wizardry.MODID, "freezing_weapon"));

		event.getRegistry().register(new EnchantmentMagicProtection(Enchantment.Rarity.UNCOMMON, EnchantmentMagicProtection.Type.MAGIC, WizardryUtilities.ARMOUR_SLOTS).setRegistryName(Wizardry.MODID, "magic_protection"));
		event.getRegistry().register(new EnchantmentMagicProtection(Enchantment.Rarity.RARE, EnchantmentMagicProtection.Type.FROST, WizardryUtilities.ARMOUR_SLOTS).setRegistryName(Wizardry.MODID, "frost_protection"));
		event.getRegistry().register(new EnchantmentMagicProtection(Enchantment.Rarity.RARE, EnchantmentMagicProtection.Type.SHOCK, WizardryUtilities.ARMOUR_SLOTS).setRegistryName(Wizardry.MODID, "shock_protection"));
	}

}
