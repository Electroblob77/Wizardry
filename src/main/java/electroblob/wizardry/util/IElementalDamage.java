package electroblob.wizardry.util;

import electroblob.wizardry.util.MagicDamage.DamageType;

/** This interface allows {@link MagicDamage} and {@link IndirectMagicDamage} to both be treated as instances of a
 * single type so that the damage type field can be accessed, rather than having to deal with each of them separately,
 * which would be inefficient and cumbersome (the latter of those classes cannot extend the former because they both
 * need to extend different subclasses of {@link net.minecraft.util.DamageSource DamageSource}).
 * @since Wizardry 1.1
 * @author Electroblob */
public interface IElementalDamage {
	
	DamageType getType();
	
	boolean isRetaliatory();
	
}