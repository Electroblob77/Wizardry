package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class LightningRay extends SpellRay {

	public LightningRay(){
		super("lightning_ray", true, SpellActions.POINT);
		this.aimAssist(0.6f);
		addProperties(DAMAGE);
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

		if(WizardryUtilities.isLiving(target)){

			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote && ticksInUse == 1 && caster instanceof EntityPlayer)
					((EntityPlayer)caster).sendStatusMessage(new TextComponentTranslation("spell.resist", target.getName(),
							this.getNameForTranslationFormatted()), true);
			// This now only damages in line with the maxHurtResistantTime. Some mods don't play nicely and fiddle
			// with this mechanic for their own purposes, so this line makes sure that doesn't affect wizardry.
			}else if(ticksInUse % ((EntityLivingBase)target).maxHurtResistantTime == 1){
				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			}
			
			if(world.isRemote){

				if(ticksInUse % 3 == 0) ParticleBuilder.create(Type.LIGHTNING).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);

				// Particle effect
				for(int i=0; i<5; i++){
					ParticleBuilder.create(Type.SPARK, target).spawn(world);
				}
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
		// This is a nice example of when onMiss is used for more than just returning a boolean
		if(world.isRemote && ticksInUse % 4 == 0){

			// The arc does not reach full range when it has a free end
			double freeRange = 0.8 * getRange(world, origin, direction, caster, ticksInUse, modifiers);

			if(caster != null){
				ParticleBuilder.create(Type.LIGHTNING).entity(caster).pos(origin.subtract(caster.getPositionVector()))
						.length(freeRange).spawn(world);
			}else{
				ParticleBuilder.create(Type.LIGHTNING).pos(origin).target(origin.add(direction.scale(freeRange))).spawn(world);
			}
		}
		
		return true;
	}

}
