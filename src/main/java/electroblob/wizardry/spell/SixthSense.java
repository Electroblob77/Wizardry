package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class SixthSense extends Spell {

	/** A {@code ResourceLocation} representing the shader file used when under the effects of sixth sense. */
	public static final ResourceLocation SHADER = new ResourceLocation(Wizardry.MODID, "shaders/post/sixth_sense.json");

	public SixthSense(){
		super("sixth_sense", SpellActions.POINT_UP, false);
		addProperties(EFFECT_DURATION, EFFECT_RADIUS);
		soundValues(1, 1.1f, 0.2f);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		caster.addPotionEffect(new PotionEffect(WizardryPotions.sixth_sense,
				(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
				(int)((modifiers.get(WizardryItems.range_upgrade) - 1f) / Constants.RANGE_INCREASE_PER_LEVEL)));

		if(world.isRemote){
			Wizardry.proxy.loadShader(caster, SHADER);
			Wizardry.proxy.playBlinkEffect(caster);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@SubscribeEvent
	public static void onPotionAddedEvent(PotionEvent.PotionAddedEvent event){
		if(Wizardry.settings.blinkEffect && event.getEntity().world.isRemote && event.getPotionEffect().getPotion() == WizardryPotions.sixth_sense
				&& event.getEntity() instanceof EntityPlayer){
			Wizardry.proxy.loadShader((EntityPlayer)event.getEntity(), SHADER);
			Wizardry.proxy.playBlinkEffect((EntityPlayer)event.getEntity());
		}
	}

}
