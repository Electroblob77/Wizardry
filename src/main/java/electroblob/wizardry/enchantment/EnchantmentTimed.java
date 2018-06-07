package electroblob.wizardry.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.item.ItemStack;

// This one is for everything other than imbued swords.
public class EnchantmentTimed extends Enchantment {

	public EnchantmentTimed(int id) {
		// Setting enchantment type to null stops the book appearing in the creative inventory
		super(id, 0, null);
	}
	
	@Override
	public boolean canApply(ItemStack p_92089_1_){
        return false;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
	// Here, enchantment level is the damage multiplier of the spell used to apply the enchantment, i.e. with an
	// non-sorcerer wand it is level 1, an apprentice sorcerer wand is level 2, and so on. Note that basic sorcerer wands can't
	// cast the imbue weapon spell, so level 2 is actually for apprentice wands.
    @Override
    public int getMaxLevel()
    {
        return 4;
    }
    
    @Override
    public boolean isAllowedOnBooks(){
    	return false;
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
    	return false;
    }
    
}
