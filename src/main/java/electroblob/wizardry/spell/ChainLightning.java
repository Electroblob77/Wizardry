package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class ChainLightning extends SpellRay {

	private static final float PRIMARY_DAMAGE = 10;
	private static final float SECONDARY_DAMAGE = 8;
	private static final float TERTIARY_DAMAGE = 6;

	private static final double PRIMARY_RANGE = 10;
	private static final double SECONDARY_RANGE = 5;
	private static final double TERTIARY_RANGE = 5;

	private static final int SECONDARY_MAX_TARGETS = 5;
	private static final int TERTIARY_MAX_TARGETS = 2; // This is per secondary target, giving 10 in total

	public ChainLightning(){
		super("chain_lightning", Tier.ADVANCED, Element.LIGHTNING, SpellType.ATTACK, 25, 50, false, PRIMARY_RANGE, null);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){

		// Anything can be attacked with the initial arc, because the player has control over where it goes. If they
		// hit a minion or an ally, it's their problem!
		if(WizardryUtilities.isLiving(target)){

			electrocute(world, caster, caster, target, PRIMARY_DAMAGE * modifiers.get(SpellModifiers.POTENCY));

			// Secondary chaining effect
			List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(SECONDARY_RANGE,
					target.posX, target.posY + target.height / 2, target.posZ, world);

			secondaryTargets.remove(target);
			secondaryTargets.removeIf(e -> !WizardryUtilities.isLiving(e));
			secondaryTargets.removeIf(e -> !WizardryUtilities.isValidTarget(caster, e));
			if(secondaryTargets.size() > SECONDARY_MAX_TARGETS) secondaryTargets = secondaryTargets.subList(0, SECONDARY_MAX_TARGETS);

			for(EntityLivingBase secondaryTarget : secondaryTargets){

				electrocute(world, caster, target, secondaryTarget,
						SECONDARY_DAMAGE * modifiers.get(SpellModifiers.POTENCY));

				// Tertiary chaining effect

				List<EntityLivingBase> tertiaryTargets = WizardryUtilities.getEntitiesWithinRadius(TERTIARY_RANGE,
						secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height / 2,
						secondaryTarget.posZ, world);

				tertiaryTargets.remove(target);
				tertiaryTargets.removeAll(secondaryTargets);
				tertiaryTargets.removeIf(e -> !WizardryUtilities.isLiving(e));
				tertiaryTargets.removeIf(e -> !WizardryUtilities.isValidTarget(caster, e));
				if(tertiaryTargets.size() > TERTIARY_MAX_TARGETS) tertiaryTargets = tertiaryTargets.subList(0, TERTIARY_MAX_TARGETS);

				for(EntityLivingBase tertiaryTarget : tertiaryTargets){
					electrocute(world, caster, secondaryTarget, tertiaryTarget,
							TERTIARY_DAMAGE * modifiers.get(SpellModifiers.POTENCY));
				}
			}

			return true;
		}

		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	private void electrocute(World world, Entity caster, Entity origin, Entity target, float damage){

		if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
			if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
					this.getNameForTranslationFormatted()));
		}else{
			target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), damage);
		}

		if(world.isRemote){
			
			ParticleBuilder.create(Type.LIGHTNING).entity(caster)
			.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
			
			ParticleBuilder.spawnShockParticles(world, target.posX, target.getEntityBoundingBox().minY + target.height/2, target.posZ);
		}

		target.playSound(WizardrySounds.SPELL_SPARK, 1, 1.5f + 0.4f * world.rand.nextFloat());
	}

}
