package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ForestsCurse extends SpellAreaEffect {

	public ForestsCurse(){
		super("forests_curse", SpellActions.POINT_UP);
		this.soundValues(1, 1.1f, 0.2f);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
	}
	
	@Override
	protected void affectEntity(World world, EntityLivingBase caster, EntityLivingBase target, SpellModifiers modifiers){
		
		if(!MagicDamage.isEntityImmune(DamageType.POISON, target) && WizardryUtilities.isLiving(target)){
			
			target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.POISON),
					getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

			int bonusAmplifier = SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));
			int duration = (int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
			int amplifier = (int)(getProperty(EFFECT_STRENGTH).floatValue() + bonusAmplifier);

			target.addPotionEffect(new PotionEffect(MobEffects.POISON, duration, amplifier));
			target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, amplifier));
			target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, duration, amplifier));
		}
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z){
		
		y += 2; // Moves the particles up to the caster's head level
		
		float brightness = world.rand.nextFloat() / 4;
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).vel(0, -0.2, 0)
		.clr(0.05f + brightness, 0.2f + brightness, 0).spawn(world);
		
		brightness = world.rand.nextFloat() / 4;
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.05, 0).time(50)
		.clr(0.1f + brightness, 0.2f + brightness, 0).spawn(world);
		
		ParticleBuilder.create(Type.LEAF).pos(x, y, z).vel(0, -0.01, 0).time(40 + world.rand.nextInt(12)).spawn(world);
	}

}
