package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class LightningWeb extends SpellRay {
	
	private static final float PRIMARY_DAMAGE = 5;
	private static final float SECONDARY_DAMAGE = 4;
	private static final float TERTIARY_DAMAGE = 3;

	private static final double PRIMARY_RANGE = 10;
	private static final double SECONDARY_RANGE = 5;
	private static final double TERTIARY_RANGE = 5;
	
	private static final int SECONDARY_MAX_TARGETS = 5;
	private static final int TERTIARY_MAX_TARGETS = 2; // This is per secondary target, giving 10 in total

	public LightningWeb(){
		super("lightning_web", Tier.MASTER, Element.LIGHTNING, SpellType.ATTACK, 15, 0, true, PRIMARY_RANGE, null);
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// TODO: Temporary solution until I implement a better continuous sound system
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag){
			if(ticksInUse == 1){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1, 1);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_LIGHTNING, 1, 1);
			}
		}
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag){
			if(ticksInUse == 1){
				caster.playSound(WizardrySounds.SPELL_LIGHTNING, 1, 1);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				caster.playSound(WizardrySounds.SPELL_LOOP_LIGHTNING, 1, 1);
			}
		}
		return flag;
	}
	
	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			electrocute(world, caster, caster, target, PRIMARY_DAMAGE * modifiers.get(SpellModifiers.POTENCY), ticksInUse);
			
			// Secondary chaining effect

			List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(SECONDARY_RANGE,
					target.posX, target.posY + target.height / 2, target.posZ, world);
			
			secondaryTargets.remove(target);
			secondaryTargets.removeIf(e -> !WizardryUtilities.isLiving(e));
			secondaryTargets.removeIf(e -> !WizardryUtilities.isValidTarget(caster, e));
			if(secondaryTargets.size() > SECONDARY_MAX_TARGETS) secondaryTargets = secondaryTargets.subList(0, SECONDARY_MAX_TARGETS);

			for(EntityLivingBase secondaryTarget : secondaryTargets){

				electrocute(world, caster, target, secondaryTarget,
						SECONDARY_DAMAGE * modifiers.get(SpellModifiers.POTENCY), ticksInUse);

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
							TERTIARY_DAMAGE * modifiers.get(SpellModifiers.POTENCY), ticksInUse);
				}
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		// This is a nice example of when onMiss is used for more than just returning a boolean
		if(world.isRemote){
			
			if(caster != null) origin = origin.subtract(0, Y_OFFSET, 0);
			
			double freeRange = 0.8 * baseRange; // The arc does not reach full range when it has a free end
			Vec3d endpoint = origin.add(direction.scale(freeRange));
			
			ParticleBuilder.create(Type.BEAM).entity(caster).clr(0.2f, 0.6f, 1)
			.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(endpoint).spawn(world);
			
			if(ticksInUse % 2 == 0){

				ParticleBuilder.create(Type.LIGHTNING).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(endpoint).spawn(world);
				
			}
		}
		
		return true;
	}
	
	private void electrocute(World world, Entity caster, Entity origin, Entity target, float damage, int ticksInUse){

		if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
			if(!world.isRemote && ticksInUse == 1)
				caster.sendMessage(new TextComponentTranslation("spell.resist",
						target.getName(), this.getNameForTranslationFormatted()));
		}else{
			WizardryUtilities.attackEntityWithoutKnockback(target,
					MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), damage);
		}
		
		if(world.isRemote){
			
			ParticleBuilder.create(Type.BEAM).entity(caster).clr(0.2f, 0.6f, 1)
			.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
			
			if(ticksInUse % 3 == 0){
				ParticleBuilder.create(Type.LIGHTNING).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
			}

			// Particle effect
			for(int i=0; i<5; i++){
				ParticleBuilder.create(Type.SPARK, target).spawn(world);
			}
		}
	}

}
