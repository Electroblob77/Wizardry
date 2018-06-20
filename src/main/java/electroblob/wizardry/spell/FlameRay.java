package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
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
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class FlameRay extends SpellRay {
	
	private static final float BASE_DAMAGE = 3;
	/** The base duration for which entities are set on fire by this spell. */
	private static final int BASE_DURATION = 10;

	public FlameRay(){
		super("flame_ray", Tier.APPRENTICE, Element.FIRE, SpellType.ATTACK, 5, 0, true, 10, null);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// TODO: Temporary solution until I implement a better continuous sound system
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag){
			if(ticksInUse % 16 == 0){
				if(ticksInUse == 0) WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_FIRE, 0.5f, 1.0f);
			}
		}
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag){
			if(ticksInUse % 16 == 0){
				if(ticksInUse == 0) caster.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
				caster.playSound(WizardrySounds.SPELL_LOOP_FIRE, 0.5f, 1.0f);
			}
		}
		return flag;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		// Fire can damage armour stands
		if(target instanceof EntityLivingBase){

			if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
				if(!world.isRemote && ticksInUse == 1) caster.sendMessage(new TextComponentTranslation("spell.resist",
						target.getName(), this.getNameForTranslationFormatted()));
			}else {
				target.setFire((int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)));
				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.FIRE),
						BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY));
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
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.MAGIC_FIRE).pos(x, y, z).vel(vx, vy, vz).spawn(world);
		ParticleBuilder.create(Type.MAGIC_FIRE).pos(x, y, z).vel(vx, vy, vz).spawn(world);
	}

}
