package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ForestsCurse extends SpellAreaEffect {
	
	private static final int BASE_DAMAGE = 4;
	private static final int BASE_DURATION = 140;

	public ForestsCurse(){
		super("forests_curse", Tier.MASTER, Element.EARTH, SpellType.ATTACK, 75, 200, EnumAction.BOW, 5, SoundEvents.ENTITY_WITHER_SPAWN);
		this.soundValues(1, 1.1f, 0.2f);
	}
	
	@Override
	protected void affectEntity(World world, EntityLivingBase caster, EntityLivingBase target, SpellModifiers modifiers){
		
		if(!MagicDamage.isEntityImmune(DamageType.POISON, target) && WizardryUtilities.isLiving(target)){
			
			target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.POISON),
					BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY));
			
			int duration = (int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade));
			
			target.addPotionEffect(new PotionEffect(MobEffects.POISON, duration, 2));
			target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, duration, 2));
			target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, duration, 2));
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
