package electroblob.wizardry.enchantment;

import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.MagicDamage;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentProtection;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.DamageSource;

import java.util.function.Predicate;

/**
 * This class is for the various magic protection enchantments added by wizardry.
 */
// This was mostly copied from EnchantmentProtection, with the code cleaned up a lot (the vanilla one is awful java...)
public class EnchantmentMagicProtection extends Enchantment {

	/** The type of protection this enchantment gives. */
	public final EnchantmentMagicProtection.Type protectionType;

	public EnchantmentMagicProtection(Enchantment.Rarity rarity, EnchantmentMagicProtection.Type protectionType, EntityEquipmentSlot... slots){
		super(rarity, EnumEnchantmentType.ARMOR, slots);
		this.protectionType = protectionType;
	}

	// Who knew this was so complicated?
	// https://minecraft.gamepedia.com/Tutorials/Enchantment_mechanics#How_enchantments_are_chosen
	// https://minecraft.gamepedia.com/Enchanting/Levels <- This is what the results of the 2 methods below are compared to

	@Override
	public int getMinEnchantability(int enchantmentLevel){
		return this.protectionType.getMinimalEnchantability() + (enchantmentLevel - 1) * this.protectionType.getEnchantIncreasePerLevel();
	}

	@Override
	public int getMaxEnchantability(int enchantmentLevel){
		return this.getMinEnchantability(enchantmentLevel) + this.protectionType.getEnchantIncreasePerLevel();
	}

	@Override
	public int getMaxLevel(){
		return 4;
	}

	@Override
	public int calcModifierDamage(int level, DamageSource source){
		if(source.canHarmInCreative()) return 0;
		if(this.protectionType.protectsAgainst(source)) return this.protectionType.getProtectionMultiplier() * level;
		return 0;
	}

	@Override
	public String getName(){
		return "enchantment.ebwizardry:" + this.protectionType.getTypeName() + "_protection";
	}

	@Override
	public boolean canApplyTogether(Enchantment ench){
		if(ench instanceof EnchantmentMagicProtection){
			return false; // As per EnchantmentProtection, only feather falling can be applied with other protection types
		}else if(ench instanceof EnchantmentProtection){
			return ((EnchantmentProtection)ench).protectionType == EnchantmentProtection.Type.FALL;
		}else{
			return super.canApplyTogether(ench);
		}
	}

	public enum Type {

		MAGIC("magic", 1, 5, 8, s -> s instanceof IElementalDamage),
		FROST("frost", 2, 10, 8, s -> s instanceof IElementalDamage && ((IElementalDamage)s).getType() == MagicDamage.DamageType.FROST),
		SHOCK("shock", 2, 10, 8, s -> s instanceof IElementalDamage && ((IElementalDamage)s).getType() == MagicDamage.DamageType.SHOCK);
		// Fire already exists, and the other types aren't used enough to be worth having

		private final String typeName;
		private final int minEnchantability;
		private final int levelCost;
		// What the heck was levelCostSpan for? Removed since it's never used.
		private final Predicate<DamageSource> criteria;
		private final int protectionMultiplier;

		Type(String name, int protectionMultiplier, int minEnchantability, int perLevelEnchantability, Predicate<DamageSource> criteria){
			this.typeName = name;
			this.minEnchantability = minEnchantability;
			this.levelCost = perLevelEnchantability;
			this.criteria = criteria;
			this.protectionMultiplier = protectionMultiplier;
		}

		public String getTypeName(){
			return this.typeName;
		}

		public boolean protectsAgainst(DamageSource source){
			return criteria.test(source);
		}

		public int getMinimalEnchantability(){
			return this.minEnchantability;
		}

		public int getEnchantIncreasePerLevel(){
			return this.levelCost;
		}

		public int getProtectionMultiplier(){
			return protectionMultiplier;
		}
	}
}