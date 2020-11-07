package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class InvigoratingPresence extends SpellAreaEffect {

	public InvigoratingPresence(){
		super("invigorating_presence", SpellActions.POINT_UP, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		this.alwaysSucceed(true);
		this.targetAllies(true);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		int bonusAmplifier = SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));

		target.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,
				(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
				getProperty(EFFECT_STRENGTH).intValue() + bonusAmplifier));

		return true;
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z){
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.03, 0).time(50).clr(1, 0.2f, 0.2f).spawn(world);
	}

	@Override
	protected String getTranslationKey(){
		return Wizardry.tisTheSeason ? super.getTranslationKey() + "_festive" : super.getTranslationKey();
	}

}
