package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.enchantment.EnchantmentMagicSword;
import electroblob.wizardry.enchantment.EnchantmentTimed;
import net.minecraft.enchantment.Enchantment;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Class responsible for defining, storing and registering all of wizardry's enchantments.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryEnchantments {

	// At the moment these enchantments generate on books in dungeon chests due to a bad bit of code (EnchantRandomly:49).
	// No idea how to fix this because I have no way of hooking into that code... removing the enchantments from the
	// registry works, but breaks everything else!

	// For the time being, a dynamic solution will have to do, i.e. intercept the book when it is generated and
	// reassign its enchantment.

	// All of these have custom classes, so the unlocalised name (referred to simply as 'name' for enchantments) is
	// dealt with inside those classes.
	public static final Enchantment magic_sword = null;
	public static final Enchantment magic_bow = null;
	public static final Enchantment flaming_weapon = null;
	public static final Enchantment freezing_weapon = null;

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Enchantment> event){
		event.getRegistry().register(new EnchantmentMagicSword().setRegistryName(Wizardry.MODID, "magic_sword"));
		event.getRegistry().register(new EnchantmentTimed().setRegistryName(Wizardry.MODID, "magic_bow"));
		event.getRegistry().register(new EnchantmentTimed().setRegistryName(Wizardry.MODID, "flaming_weapon"));
		event.getRegistry().register(new EnchantmentTimed().setRegistryName(Wizardry.MODID, "freezing_weapon"));
	}

}
