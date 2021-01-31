package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class ItemManaFlask extends Item {

	public enum Size {

		SMALL(75, 25, EnumRarity.COMMON),
		MEDIUM(350, 40, EnumRarity.COMMON),
		LARGE(1400, 60, EnumRarity.RARE);

		public int capacity;
		public int useDuration;
		public EnumRarity rarity;

		Size(int capacity, int useDuration, EnumRarity rarity){
			this.capacity = capacity;
			this.useDuration = useDuration;
			this.rarity = rarity;
		}
	}

	public final Size size;

	public ItemManaFlask(Size size){
		super();
		this.size = size;
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setMaxStackSize(16);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return size.rarity;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn){
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + Wizardry.MODID + ":mana_flask.desc", size.capacity);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack){
		return EnumAction.BLOCK;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack){
		return size.useDuration;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack flask = player.getHeldItem(hand);

		List<ItemStack> stacks = InventoryUtils.getPrioritisedHotbarAndOffhand(player);
		stacks.addAll(player.inventory.armorInventory); // player#getArmorInventoryList() only returns an Iterable

		if(stacks.stream().anyMatch(s -> s.getItem() instanceof IManaStoringItem && !((IManaStoringItem)s.getItem()).isManaFull(s))){

			if(player.capabilities.isCreativeMode){
				findAndChargeItem(flask, player);
			}else{
				player.setActiveHand(hand);
			}

			return new ActionResult<>(EnumActionResult.SUCCESS, flask);

		}else{
			return new ActionResult<>(EnumActionResult.FAIL, flask);
		}
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase player, int count){
		if(player.world.isRemote){
			float f = count/(float)getMaxItemUseDuration(stack);
			Vec3d pos = player.getPositionEyes(0).subtract(0, 0.2, 0).add(player.getLookVec().scale(0.6));
			Vec3d delta = new Vec3d(0, 0.2 * f, 0).rotatePitch(count * 0.5f).rotateYaw((float)Math.toRadians(90 - player.rotationYawHead));
			ParticleBuilder.create(Type.DUST).pos(pos.add(delta)).vel(delta.scale(0.2)).time(12 + player.world.rand.nextInt(6))
					.clr(1, 1, 0.65f).fade(0.7f, 0, 1).spawn(player.world);
		}
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity){

		if(entity instanceof EntityPlayer){
			findAndChargeItem(stack, (EntityPlayer)entity);
		}

		return stack;
	}

	private void findAndChargeItem(ItemStack stack, EntityPlayer player){

		List<ItemStack> stacks = InventoryUtils.getPrioritisedHotbarAndOffhand(player);
		stacks.addAll(player.inventory.armorInventory); // player#getArmorInventoryList() only returns an Iterable

		// Find the chargeable item with the least mana
		ItemStack toCharge = stacks.stream()
				.filter(s -> s.getItem() instanceof IManaStoringItem && !((IManaStoringItem)s.getItem()).isManaFull(s))
				.min(Comparator.comparingDouble(s -> ((IManaStoringItem)s.getItem()).getFullness(s))).orElse(null);

		if(toCharge != null){

			((IManaStoringItem)toCharge.getItem()).rechargeMana(toCharge, size.capacity);

			EntityUtils.playSoundAtPlayer(player, WizardrySounds.ITEM_MANA_FLASK_USE, 1, 1);
			EntityUtils.playSoundAtPlayer(player, WizardrySounds.ITEM_MANA_FLASK_RECHARGE, 0.7f, 1.1f);

			if(!player.isCreative()) stack.shrink(1);
			player.getCooldownTracker().setCooldown(this, 20);
		}
	}

}
