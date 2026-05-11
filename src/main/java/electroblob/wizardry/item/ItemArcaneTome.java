package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemArcaneTome extends Item {

	public ItemArcaneTome(){
		super();
		setHasSubtypes(true);
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){
		if(tab == WizardryTabs.WIZARDRY || tab == CreativeTabs.SEARCH){ // Don't use isInCreativeTab here.
			for(int i = 1; i < Tier.values().length; i++){
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		switch(this.getDamage(stack)){
		case 1:
			return EnumRarity.UNCOMMON;
		case 2:
			return EnumRarity.RARE;
		case 3:
			return EnumRarity.EPIC;
		}
		return EnumRarity.COMMON;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ){
		if(player.isSneaking()){
			IBlockState state = world.getBlockState(pos);
			if(state.getBlock() instanceof BlockBookshelf){
				if(state.getBlock().onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ)){
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.PASS;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag showAdvanced){

		if(stack.getItemDamage() < 1){
			return; // If something's up with the metadata it will display a 'generic' tome of arcana with no info
		}

		Tier tier = Tier.values()[stack.getItemDamage()];
		Tier tier2 = Tier.values()[stack.getItemDamage() - 1];

		tooltip.add(tier.getDisplayNameWithFormatting());
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc",
				tier2.getDisplayNameWithFormatting() + "\u00A77", tier.getDisplayNameWithFormatting() + "\u00A77");
	}

}
