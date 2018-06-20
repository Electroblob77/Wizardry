package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class Transience extends Spell {

	public Transience(){
		super("transience", Tier.ADVANCED, Element.HEALING, SpellType.BUFF, 50, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!caster.isPotionActive(WizardryPotions.transience)){
			if(!world.isRemote){
				caster.addPotionEffect(new PotionEffect(WizardryPotions.transience,
						(int)(400 * modifiers.get(WizardryItems.duration_upgrade)), 0));
				caster.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY,
						(int)(400 * modifiers.get(WizardryItems.duration_upgrade)), 0, false, false));
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
			}
			return true;
		}
		return false;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource() != null){
			// Prevents all blockable damage while transience is active
			if(event.getEntityLiving().isPotionActive(WizardryPotions.transience)
					&& !event.getSource().isUnblockable()){
				event.setCanceled(true);
			}
			// Prevents transient entities from causing any damage
			if(event.getSource().getTrueSource() instanceof EntityLivingBase
					&& ((EntityLivingBase)event.getSource().getTrueSource()).isPotionActive(WizardryPotions.transience)){
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockPlaceEvent(BlockEvent.PlaceEvent event){
		// Prevents transient players from placing blocks
		if(event.getPlayer().isPotionActive(WizardryPotions.transience)){
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public static void onBlockBreakEvent(BlockEvent.BreakEvent event){
		// Prevents transient players from breaking blocks
		if(event.getPlayer().isPotionActive(WizardryPotions.transience)){
			event.setCanceled(true);
			return;
		}
	}

}
