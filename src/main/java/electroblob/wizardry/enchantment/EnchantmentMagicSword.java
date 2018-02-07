package electroblob.wizardry.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDamage;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

// This one is for imbued swords. The only reason this is separate is that the way vanilla is written allows me to hook
// into the damage increase for melee weapons, meaning I don't have to use events - always handy!
public class EnchantmentMagicSword extends EnchantmentDamage implements Imbuement {

	public EnchantmentMagicSword(){
		super(Enchantment.Rarity.COMMON, 0, EntityEquipmentSlot.MAINHAND);
		// Setting this to null stops the book appearing in the creative inventory
		this.type = null;
	}

	@Override
	public boolean canApply(ItemStack p_92089_1_){
		return false;
	}

	/**
	 * Returns the maximum level that the enchantment can have.
	 */
	// Here, enchantment level is the damage multiplier of the spell used to apply the enchantment, i.e. with an
	// non-sorcerer wand it is level 1, a basic sorcerer wand is level 2, and so on. Note that basic sorcerer wands
	// can't
	// cast the imbue weapon spell, so level 2 is actually for apprentice wands.
	@Override
	public int getMaxLevel(){
		return 4;
	}

	// Returns the number by which the damage should be increased (or something)
	@Override
	public float calcDamageByCreature(int p_152376_1_, EnumCreatureAttribute p_152376_2_){
		return (float)p_152376_1_ * 1.25F;
	}

	@Override
	public String getName(){
		return "enchantment." + this.getRegistryName();
	}

	@Override
	public boolean isAllowedOnBooks(){
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack){
		return false;
	}

}
