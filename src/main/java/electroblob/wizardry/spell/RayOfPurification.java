package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class RayOfPurification extends SpellRay {

	/** The number by which this spell's damage is multiplied for undead entities. */
	public static final String UNDEAD_DAMAGE_MULTIPLIER = "undead_damage_multiplier";

	public RayOfPurification(){
		super("ray_of_purification", SpellActions.POINT, true);
		addProperties(DAMAGE, EFFECT_DURATION, BURN_DURATION, UNDEAD_DAMAGE_MULTIPLIER);
	}

	// The following three methods serve as a good example of how to implement continuous spell sounds (hint: it's easy)
	
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

		if(EntityUtils.isLiving(target)){

			if(MagicDamage.isEntityImmune(DamageType.RADIANT, target)){
				if(!world.isRemote && ticksInUse == 1 && caster instanceof EntityPlayer) ((EntityPlayer)caster)
				.sendStatusMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()), true);
			}else{

				float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
				// Fire
				if(((EntityLivingBase)target).isEntityUndead()){
					target.setFire((int)(getProperty(BURN_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
					damage *= getProperty(UNDEAD_DAMAGE_MULTIPLIER).floatValue();
				}
				// Damage
				EntityUtils.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.RADIANT), damage);
				// Blindness
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(MobEffects.BLINDNESS,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade))));
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}

	@Override
	protected void spawnParticleRay(World world, Vec3d origin, Vec3d direction, EntityLivingBase caster, double distance){

		if(caster != null){
			ParticleBuilder.create(Type.BEAM).entity(caster).pos(origin.subtract(caster.getPositionVector()))
					.length(distance).clr(1, 0.6f + 0.3f * world.rand.nextFloat(), 0.2f)
					.scale(MathHelper.sin(caster.ticksExisted * 0.2f) * 0.1f + 1.4f).spawn(world);
		}else{
			ParticleBuilder.create(Type.BEAM).pos(origin).target(origin.add(direction.scale(distance)))
					.clr(1, 0.6f + 0.3f * world.rand.nextFloat(), 0.2f)
					.scale(MathHelper.sin(Wizardry.proxy.getThePlayer().ticksExisted * 0.2f) * 0.1f + 1.4f).spawn(world);
		}
	}
}
