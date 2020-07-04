package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class ChainLightning extends SpellRay {

	public static final String PRIMARY_DAMAGE = "primary_damage";
	public static final String SECONDARY_DAMAGE = "secondary_damage";
	public static final String TERTIARY_DAMAGE = "tertiary_damage";

	public static final String SECONDARY_RANGE = "secondary_range";
	public static final String TERTIARY_RANGE = "tertiary_range";

	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";
	public static final String TERTIARY_MAX_TARGETS = "tertiary_max_targets"; // This is per secondary target

	public ChainLightning(){
		super("chain_lightning", SpellActions.POINT, false);
		this.aimAssist(0.6f);
		this.soundValues(1, 1.7f, 0.2f);
		addProperties(PRIMARY_DAMAGE, SECONDARY_DAMAGE, TERTIARY_DAMAGE, SECONDARY_RANGE, TERTIARY_RANGE,
				SECONDARY_MAX_TARGETS, TERTIARY_MAX_TARGETS);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		// Anything can be attacked with the initial arc, because the player has control over where it goes. If they
		// hit a minion or an ally, it's their problem!
		if(EntityUtils.isLiving(target)){

			electrocute(world, caster, origin, target, getProperty(PRIMARY_DAMAGE).floatValue()
					* modifiers.get(SpellModifiers.POTENCY));

			// Secondary chaining effect
			List<EntityLivingBase> secondaryTargets = EntityUtils.getLivingWithinRadius(
					getProperty(SECONDARY_RANGE).doubleValue(), target.posX, target.posY + target.height / 2, target.posZ, world);

			secondaryTargets.remove(target);
			secondaryTargets.removeIf(e -> !EntityUtils.isLiving(e));
			secondaryTargets.removeIf(e -> !AllyDesignationSystem.isValidTarget(caster, e));
			if(secondaryTargets.size() > getProperty(SECONDARY_MAX_TARGETS).intValue())
				secondaryTargets = secondaryTargets.subList(0, getProperty(SECONDARY_MAX_TARGETS).intValue());

			for(EntityLivingBase secondaryTarget : secondaryTargets){

				electrocute(world, caster, target.getPositionVector().add(0, target.height/2, 0), secondaryTarget,
						getProperty(SECONDARY_DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

				// Tertiary chaining effect

				List<EntityLivingBase> tertiaryTargets = EntityUtils.getLivingWithinRadius(
						getProperty(TERTIARY_RANGE).doubleValue(), secondaryTarget.posX,
						secondaryTarget.posY + secondaryTarget.height / 2, secondaryTarget.posZ, world);

				tertiaryTargets.remove(target);
				tertiaryTargets.removeAll(secondaryTargets);
				tertiaryTargets.removeIf(e -> !EntityUtils.isLiving(e));
				tertiaryTargets.removeIf(e -> !AllyDesignationSystem.isValidTarget(caster, e));
				if(tertiaryTargets.size() > getProperty(TERTIARY_MAX_TARGETS).intValue())
					tertiaryTargets = tertiaryTargets.subList(0, getProperty(TERTIARY_MAX_TARGETS).intValue());

				for(EntityLivingBase tertiaryTarget : tertiaryTargets){
					electrocute(world, caster, secondaryTarget.getPositionVector().add(0, secondaryTarget.height/2, 0),
							tertiaryTarget, getProperty(TERTIARY_DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
				}
			}

			return true;
		}

		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	private void electrocute(World world, Entity caster, Vec3d origin, Entity target, float damage){

		if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
			if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
					new TextComponentTranslation("spell.resist", target.getName(), this.getNameForTranslationFormatted()),
					true);
		}else{
			target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), damage);
		}

		if(world.isRemote){
			
			ParticleBuilder.create(Type.LIGHTNING).entity(caster)
			.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
			
			ParticleBuilder.spawnShockParticles(world, target.posX, target.posY + target.height/2, target.posZ);
		}

		//target.playSound(WizardrySounds.SPELL_SPARK, 1, 1.5f + 0.4f * world.rand.nextFloat());
	}

}
