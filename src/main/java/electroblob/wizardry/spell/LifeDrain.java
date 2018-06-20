package electroblob.wizardry.spell;

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
import net.minecraft.world.World;

public class LifeDrain extends SpellRay {
	
	private static final float BASE_DAMAGE = 2;
	/** The fraction of the damage dealt that is transferred to the caster as healing. */
	private static final float HEAL_FACTOR = 0.35f;

	public LifeDrain(){
		super("life_drain", Tier.APPRENTICE, Element.NECROMANCY, SpellType.ATTACK, 10, 0, true, 10, null);
		this.particleVelocity(-0.5);
		this.particleSpacing(0.4);
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// TODO: Temporary solution until I implement a better continuous sound system
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag){
			if(ticksInUse % 18 == 0){
				if(ticksInUse == 0) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SUMMONING, 1, 0.6f);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_CRACKLE, 2, 1);
			}
		}
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag){
			if(ticksInUse % 18 == 0){
				if(ticksInUse == 0) caster.playSound(WizardrySounds.SPELL_SUMMONING, 1, 0.6f);
				caster.playSound(WizardrySounds.SPELL_LOOP_CRACKLE, 2, 1);
			}
		}
		return flag;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){

			if(ticksInUse % 12 == 0){
				
				float damage = BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY);
				
				WizardryUtilities.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster,
						DamageType.MAGIC), damage);
				
				caster.heal(damage * HEAL_FACTOR);
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
		if(world.rand.nextInt(5) == 0) ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).colour(0.1f, 0, 0).spawn(world);
		// This used to multiply the velocity by the distance from the caster
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).lifetime(8 + world.rand.nextInt(6))
		.colour(0.5f, 0, 0).spawn(world);
	}

}
