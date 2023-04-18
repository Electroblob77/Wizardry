package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class LightningWeb extends SpellRay {

	public static final String PRIMARY_DAMAGE = "primary_damage";
	public static final String SECONDARY_DAMAGE = "secondary_damage";
	public static final String TERTIARY_DAMAGE = "tertiary_damage";

	public static final String SECONDARY_RANGE = "secondary_range";
	public static final String TERTIARY_RANGE = "tertiary_range";

	public static final String SECONDARY_MAX_TARGETS = "secondary_max_targets";
	public static final String TERTIARY_MAX_TARGETS = "tertiary_max_targets"; // This is per secondary target

	public LightningWeb(){
		super("lightning_web", SpellActions.POINT, true);
		this.aimAssist(0.6f);
		addProperties(PRIMARY_DAMAGE, SECONDARY_DAMAGE, TERTIARY_DAMAGE, SECONDARY_RANGE, TERTIARY_RANGE,
				SECONDARY_MAX_TARGETS, TERTIARY_MAX_TARGETS);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target) && ticksInUse % 10 == 0){

			electrocute(world, caster, origin, target, getProperty(PRIMARY_DAMAGE).floatValue()
					* modifiers.get(SpellModifiers.POTENCY), ticksInUse);

            // Secondary chaining effect

            List<EntityLivingBase> secondaryTargets = EntityUtils.getLivingWithinRadius(
                getProperty(SECONDARY_RANGE).floatValue(), target.posX, target.posY + target.height / 2,
                target.posZ, world);

            secondaryTargets.stream()
                .filter(entity -> !entity.equals(target))
                .filter(EntityUtils::isLiving)
                .filter(e -> AllyDesignationSystem.isValidTarget(caster, e))
                .limit(getProperty(SECONDARY_MAX_TARGETS).intValue())
                .forEach(secondaryTarget -> {
                    electrocute(world, caster,
                        target.getPositionVector().add(0, target.height / 2, 0),
                        secondaryTarget,
                        getProperty(SECONDARY_DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY),
                        ticksInUse
                    );

                    // Tertiary chaining effect

                    List<EntityLivingBase> tertiaryTargets =
                        EntityUtils.getLivingWithinRadius(
                            getProperty(TERTIARY_RANGE).floatValue(),
                            secondaryTarget.posX,
                            secondaryTarget.posY + secondaryTarget.height / 2,
                            secondaryTarget.posZ,
                            world
                        );

                    tertiaryTargets.stream()
                        .filter(entity -> !secondaryTargets.contains(entity))
                        .filter(entity -> !entity.equals(target))
                        .filter(EntityUtils::isLiving)
                        .filter(e -> AllyDesignationSystem.isValidTarget(caster, e))
                        .limit(getProperty(TERTIARY_MAX_TARGETS).intValue())
                        .forEach(tertiaryTarget ->
                            electrocute(world, caster,
                                secondaryTarget.getPositionVector().add(0, secondaryTarget.height / 2, 0),
                                tertiaryTarget,
                                getProperty(TERTIARY_DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY),
                                ticksInUse
                            )
                        );
                });
		}

		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		// This is a nice example of when onMiss is used for more than just returning a boolean
		if(world.isRemote){

			// The arc does not reach full range when it has a free end
			double freeRange = 0.8 * getRange(world, origin, direction, caster, ticksInUse, modifiers);

			if(caster != null){
				ParticleBuilder.create(Type.BEAM).entity(caster).pos(origin.subtract(caster.getPositionVector()))
						.length(freeRange).clr(0.2f, 0.6f, 1).spawn(world);
			}else{
				ParticleBuilder.create(Type.BEAM).pos(origin).target(origin.add(direction.scale(freeRange)))
						.clr(0.2f, 0.6f, 1).spawn(world);
			}

			if(ticksInUse % 4 == 0){
				if(caster != null){
					ParticleBuilder.create(Type.LIGHTNING).entity(caster).pos(origin.subtract(caster.getPositionVector()))
							.length(freeRange).spawn(world);
				}else{
					ParticleBuilder.create(Type.LIGHTNING).pos(origin).target(origin.add(direction.scale(freeRange))).spawn(world);
				}
			}
		}

		return true;
	}

	private void electrocute(World world, Entity caster, Vec3d origin, Entity target, float damage, int ticksInUse){

		if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
			if(!world.isRemote && ticksInUse == 1 && caster instanceof EntityPlayer)
				((EntityPlayer)caster).sendStatusMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()), true);
		}else{
			EntityUtils.attackEntityWithoutKnockback(target,
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
