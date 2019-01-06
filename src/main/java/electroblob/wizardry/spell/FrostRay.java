package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
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
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class FrostRay extends SpellRay {

	private static final float BASE_DAMAGE = 3;
	/** The base duration of the forstbite effect applied by this spell. */
	private static final int BASE_DURATION = 200;

	public FrostRay(){
		super("frost_ray", Tier.APPRENTICE, Element.ICE, SpellType.ATTACK, 5, 0, true, 10, null);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// TODO: Temporary solution until I implement a better continuous sound system
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag){
			if(ticksInUse % 12 == 0){
				if(ticksInUse == 0) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.5f, 1);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_ICE, 0.5f, 1);
			}
		}
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag){
			if(ticksInUse % 12 == 0){
				if(ticksInUse == 0) caster.playSound(WizardrySounds.SPELL_ICE, 0.5f, 1);
				caster.playSound(WizardrySounds.SPELL_LOOP_ICE, 0.5f, 1);
			}
		}
		return flag;
	}
	
	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){

			if(target.isBurning()) target.extinguish();

			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote && ticksInUse == 1 && caster instanceof EntityPlayer) ((EntityPlayer)caster)
				.sendStatusMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()), true);
			}else{
				// For frost ray the entity can move slightly, unlike freeze
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.frost,
						(int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)), 0));

				float damage = BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY);
				if(target instanceof EntityBlaze || target instanceof EntityMagmaCube) damage *= 2;
				
				WizardryUtilities.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster,
						DamageType.FROST), damage);

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
		float brightness = world.rand.nextFloat();
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12))
		.clr(0.4f + 0.6f * brightness, 0.6f + 0.4f*brightness, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12)).spawn(world);
	}

}
