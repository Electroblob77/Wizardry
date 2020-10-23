package electroblob.wizardry.spell;

import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class FontOfMana extends SpellAreaEffect {

	public FontOfMana(){
		super("font_of_mana", SpellActions.POINT_UP, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		this.particleDensity(1.25f);
		this.targetAllies(true);
		this.alwaysSucceed(true);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(target instanceof EntityPlayer){ // Font of mana is only useful to players
			target.addPotionEffect(new PotionEffect(WizardryPotions.font_of_mana,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					(int)(getProperty(EFFECT_STRENGTH).intValue() + (modifiers.get(SpellModifiers.POTENCY) - 1) * 2)));
		}

		return true;
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z){
		float hue = world.rand.nextFloat() * 0.4f;
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.03, 0).time(50)
				.clr(1, 1 - hue, 0.6f + hue).spawn(world);
	}

	@SubscribeEvent(priority = EventPriority.LOW) // Doesn't really matter but there's no point processing it if casting is blocked
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Moved from ItemWand (quite why this wasn't done with modifiers before I don't know!)
		if(event.getCaster() != null && event.getCaster().isPotionActive(WizardryPotions.font_of_mana)){
			// Dividing by this rather than setting it takes upgrades and font of mana into account simultaneously
			event.getModifiers().set(WizardryItems.cooldown_upgrade, event.getModifiers().get(WizardryItems.cooldown_upgrade)
					/ (2 + event.getCaster().getActivePotionEffect(WizardryPotions.font_of_mana).getAmplifier()), false);
		}
	}
}