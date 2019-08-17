package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemIdentificationScroll extends Item {

	public ItemIdentificationScroll(){
		super();
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return EnumRarity.UNCOMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag flag) {
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		if(WizardData.get(player) != null){

			WizardData data = WizardData.get(player);

			for(ItemStack stack1 : WizardryUtilities.getPrioritisedHotbarAndOffhand(player)){

				if(!stack1.isEmpty()){
					Spell spell = Spell.byMetadata(stack1.getItemDamage());
					if((stack1.getItem() instanceof ItemSpellBook || stack1.getItem() instanceof ItemScroll)
							&& !data.hasSpellBeenDiscovered(spell)){

						if(!MinecraftForge.EVENT_BUS.post(new DiscoverSpellEvent(player, spell,
								DiscoverSpellEvent.Source.IDENTIFICATION_SCROLL))){
							// Identification scrolls give the chat readout in creative mode, otherwise it looks like
							// nothing happens!
							data.discoverSpell(spell);
							player.playSound(WizardrySounds.MISC_DISCOVER_SPELL, 1.25f, 1);
							if(!player.isCreative()) stack.shrink(1);
							if(!world.isRemote) player.sendMessage(new TextComponentTranslation("spell.discover",
									spell.getNameForTranslationFormatted()));

							return new ActionResult<>(EnumActionResult.SUCCESS, stack);
						}
					}
				}
			}
			// If it found nothing to identify, it says so!
			if(!world.isRemote) player.sendMessage(
					new TextComponentTranslation("item." + Wizardry.MODID + ":identification_scroll.nothing_to_identify"));
		}

		return new ActionResult<>(EnumActionResult.FAIL, stack);
	}

}
