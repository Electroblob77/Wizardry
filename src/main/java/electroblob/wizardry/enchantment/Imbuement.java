package electroblob.wizardry.enchantment;

/** Interface for temporary enchantments that last for a certain duration ('imbuements'). This interface allows
 * {@link EnchantmentMagicSword} and {@link EnchantmentTimed} to both be treated as instances of a
 * single type, rather than having to deal with each of them separately,
 * which would be inefficient and cumbersome (the former of those classes cannot extend the latter because they both
 * need to extend different subclasses of {@link net.minecraft.enchantment.Enchantment}).
 * @since Wizardry 1.2 */
public interface Imbuement {

}
