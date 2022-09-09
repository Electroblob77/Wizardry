package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemArcaneTome extends Item {

	private final EnumRarity rarity;
	private final Tier tier;

	public ItemArcaneTome(EnumRarity rarity, Tier tier){
		super();
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.WIZARDRY);
		this.rarity = rarity;
		this.tier = tier;
	}

	public Tier getTier() { return tier; }

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){ return true; }

	@Override
	public EnumRarity getRarity(ItemStack stack){ return rarity; }

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag showAdvanced){
		tooltip.add(tier.getDisplayNameWithFormatting());
		Tier tier2 = Tier.values()[tier.ordinal() - 1];

		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + Wizardry.MODID + ":arcane_tome.desc",
				tier2.getDisplayNameWithFormatting() + "\u00A77", tier.getDisplayNameWithFormatting() + "\u00A77");
	}

}
