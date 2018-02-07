package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ForestsCurse extends Spell {

	public ForestsCurse(){
		super(Tier.MASTER, 75, Element.EARTH, "forests_curse", SpellType.ATTACK, 200, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
				5.0d * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target)
					&& !MagicDamage.isEntityImmune(DamageType.POISON, target)){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.POISON),
						4.0f * modifiers.get(SpellModifiers.DAMAGE));
				target.addPotionEffect(new PotionEffect(MobEffects.POISON,
						(int)(140 * modifiers.get(WizardryItems.duration_upgrade)), 2));
				target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,
						(int)(140 * modifiers.get(WizardryItems.duration_upgrade)), 2));
				target.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS,
						(int)(140 * modifiers.get(WizardryItems.duration_upgrade)), 2));
			}
		}

		if(world.isRemote){
			for(int i = 0; i < 50 * modifiers.get(WizardryItems.blast_upgrade); i++){
				double radius = (1 + world.rand.nextDouble() * 4) * modifiers.get(WizardryItems.blast_upgrade);
				double angle = world.rand.nextDouble() * Math.PI * 2;
				float brightness = world.rand.nextFloat() / 4;
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
						caster.posX + radius * Math.cos(angle), WizardryUtilities.getPlayerEyesPos(caster) + 0.5,
						caster.posZ + radius * Math.sin(angle), 0, -0.2, 0, 0, 0.05f + brightness, 0.2f + brightness,
						0.0f);
				brightness = world.rand.nextFloat() / 4;
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world,
						caster.posX + radius * Math.cos(angle), WizardryUtilities.getPlayerEyesPos(caster) + 0.5,
						caster.posZ + radius * Math.sin(angle), 0, -0.05, 0, 50, 0.1f + brightness, 0.2f + brightness,
						0.0f);
				Wizardry.proxy.spawnParticle(WizardryParticleType.LEAF, world, caster.posX + radius * Math.cos(angle),
						WizardryUtilities.getPlayerEyesPos(caster) + 0.5, caster.posZ + radius * Math.sin(angle), 0,
						-0.01, 0, 40 + world.rand.nextInt(12));

			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SPAWN, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

}
