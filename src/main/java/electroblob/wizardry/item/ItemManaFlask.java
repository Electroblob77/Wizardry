package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

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

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn){
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + Wizardry.MODID + ":mana_flask.desc", size.capacity);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack flask = player.getHeldItem(hand);

		List<ItemStack> stacks = WizardryUtilities.getPrioritisedHotbarAndOffhand(player);
		stacks.addAll(player.inventory.armorInventory); // player#getArmorInventoryList() only returns an Iterable

		// Find the chargeable item with the least mana
		ItemStack toCharge = stacks.stream()
				.filter(s -> s.getItem() instanceof IManaStoringItem && !((IManaStoringItem)s.getItem()).isManaFull(s))
				.min(Comparator.comparingDouble(s -> ((IManaStoringItem)s.getItem()).getFullness(s))).orElse(null);

		if(toCharge != null){

			((IManaStoringItem)toCharge.getItem()).rechargeMana(toCharge, size.capacity);

			WizardryUtilities.playSoundAtPlayer(player, WizardrySounds.ITEM_MANA_FLASK_USE, 1, 1);
			WizardryUtilities.playSoundAtPlayer(player, WizardrySounds.ITEM_MANA_FLASK_RECHARGE, 0.7f, 1.1f);

			if(!player.isCreative()) flask.shrink(1);
			player.getCooldownTracker().setCooldown(this, 20);

			return new ActionResult<>(EnumActionResult.SUCCESS, flask);

		}else{
			return new ActionResult<>(EnumActionResult.FAIL, flask);
		}
	}
}
