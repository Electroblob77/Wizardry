package electroblob.wizardry.item;

import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemManaFlask extends Item {

	public enum Size {

		SMALL(75, EnumRarity.COMMON),
		MEDIUM(700, EnumRarity.COMMON),
		LARGE(1400, EnumRarity.RARE);

		public int capacity;
		public EnumRarity rarity;

		Size(int capacity, EnumRarity rarity){
			this.capacity = capacity;
			this.rarity = rarity;
		}
	}

	public final Size size;

	public ItemManaFlask(Size size){
		super();
		this.size = size;
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return size.rarity;
	}
}
