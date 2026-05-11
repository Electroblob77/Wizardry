package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemWandUpgrade extends Item {

	public ItemWandUpgrade(){
		super();
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return EnumRarity.UNCOMMON;
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

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc");
	}
}
