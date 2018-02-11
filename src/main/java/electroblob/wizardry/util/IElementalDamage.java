package electroblob.wizardry.util;

import electroblob.wizardry.registry.WizardryAchievements;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * This interface allows {@link MagicDamage} and {@link IndirectMagicDamage} to both be treated as instances of a single
 * type so that the damage type field can be accessed, rather than having to deal with each of them separately, which
 * would be inefficient and cumbersome (the latter of those classes cannot extend the former because they both need to
 * extend different subclasses of {@link net.minecraft.util.DamageSource DamageSource}).
 * 
 * @since Wizardry 1.1
 * @author Electroblob
 */
@Mod.EventBusSubscriber
public interface IElementalDamage {

	DamageType getType();

	boolean isRetaliatory();

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource() instanceof IElementalDamage){
			if(MagicDamage.isEntityImmune(((IElementalDamage)event.getSource()).getType(), event.getEntity())){
				event.setCanceled(true);
				// I would have liked to have done the 'resist' chat message here, but I overlooked the fact that I
				// would need an instance of the spell to get its display name!
				return;
			}
			// One convenient side effect of the new damage type system is that I can get rid of all the places where
			// creepers are charged and just put them here under shock damage - this is precisely the sort of
			// repetitive code I was trying to get rid of, since errors can (and did!) occur.
			if(event.getEntityLiving() instanceof EntityCreeper
					&& !((EntityCreeper)event.getEntityLiving()).getPowered()
					&& ((IElementalDamage)event.getSource()).getType() == DamageType.SHOCK){
				// Charges creepers when they are hit by shock damage
				WizardryUtilities.chargeCreeper((EntityCreeper)event.getEntityLiving());
				// Gives the player that caused the shock damage the 'It's Gonna Blow' achievement
				if(event.getSource().getTrueSource() instanceof EntityPlayer){
					((EntityPlayer)event.getSource().getTrueSource()).addStat(WizardryAchievements.charge_creeper);
				}
			}
		}
	}
}