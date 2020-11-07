package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemPurifyingElixir extends Item {

	public ItemPurifyingElixir(){
		this.setMaxStackSize(1);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return EnumRarity.RARE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc");
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity){

		if(!world.isRemote){
			entity.curePotionEffects(stack);
		}else{

			ParticleBuilder.spawnHealParticles(world, entity);

			for(int i = 0; i < 20; i++){
				double x = entity.posX + world.rand.nextDouble() * 2 - 1;
				double y = entity.posY + entity.getEyeHeight() - 0.5 + world.rand.nextDouble();
				double z = entity.posZ + world.rand.nextDouble() * 2 - 1;
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.14, 0).clr(0x0f001b)
						.time(20 + world.rand.nextInt(12)).spawn(world);
				ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0x0f001b).spawn(world);
			}
		}

		world.playSound(entity.posX, entity.posY, entity.posZ, WizardrySounds.ITEM_PURIFYING_ELIXIR_DRINK, SoundCategory.PLAYERS, 1, 1, false);

		if(entity instanceof EntityPlayerMP){
			EntityPlayerMP entityplayermp = (EntityPlayerMP)entity;
			CriteriaTriggers.CONSUME_ITEM.trigger(entityplayermp, stack);
		}

		if(entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.isCreativeMode){
			stack.shrink(1);
		}

		return stack.isEmpty() ? new ItemStack(Items.GLASS_BOTTLE) : stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack){
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack){
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn){
		playerIn.setActiveHand(handIn);
		return new ActionResult<>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
	}
}
