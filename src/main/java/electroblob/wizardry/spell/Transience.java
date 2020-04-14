package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class Transience extends Spell {

	/** A {@code ResourceLocation} representing the shader file used when under the effects of transience. */
	public static final ResourceLocation SHADER = new ResourceLocation(Wizardry.MODID, "shaders/post/transience.json");

	public Transience(){
		super("transience", EnumAction.BOW, false);
		addProperties(EFFECT_DURATION);
	}

	@Override
	public boolean requiresPacket(){
		return true;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(world.isRemote){
			Wizardry.proxy.loadShader(caster, SHADER);
			Wizardry.proxy.playBlinkEffect(caster);
		}

		if(!caster.isPotionActive(WizardryPotions.transience)){

			if(!world.isRemote){

				int duration = (int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));

				caster.addPotionEffect(new PotionEffect(WizardryPotions.transience, duration, 0));
				caster.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, duration, 0, false, false));

				this.playSound(world, caster, ticksInUse, duration, modifiers);
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
					&& event.getSource() != DamageSource.OUT_OF_WORLD){
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
	public static void onPlayerInteractEvent(PlayerInteractEvent event){
		// Prevents transient players from interacting with the world in any way
		if(event.isCancelable() && event.getEntityPlayer().isPotionActive(WizardryPotions.transience)){
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPotionAddedEvent(PotionEvent.PotionAddedEvent event){
		if(event.getEntity().world.isRemote && event.getPotionEffect().getPotion() == WizardryPotions.transience
				&& event.getEntity() instanceof EntityPlayer){
			Wizardry.proxy.loadShader((EntityPlayer)event.getEntity(), SHADER);
			Wizardry.proxy.playBlinkEffect((EntityPlayer)event.getEntity());
		}
	}

}
