package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlindingFlash extends SpellAreaEffect {

	public BlindingFlash(){
		super("blinding_flash", SpellActions.POINT_UP, false);
		this.alwaysSucceed(true);
		addProperties(EFFECT_DURATION); // No effect strength, you're either blinded or you're not!
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target)){
			int duration = (int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
			target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, duration, 0));
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		if(caster != null) origin = origin.add(0, caster.height + 1, 0);
		ParticleBuilder.create(Type.SPHERE).pos(origin).scale((float)radius * 0.8f).spawn(world);
	}
}
