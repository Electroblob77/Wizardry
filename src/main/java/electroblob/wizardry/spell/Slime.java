package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityMagicSlime;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Slime extends SpellRay {
	
	private static final int BASE_DURATION = 200;

	public Slime(){
		super("slime", Tier.ADVANCED, Element.EARTH, SpellType.ATTACK, 20, 50, false, 8, WizardrySounds.SPELL_ICE);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// This spell uses more than one sound, so this is required...
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag) WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, 0.5F);
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag) caster.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, 0.5F);
		return flag;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target) && !(target instanceof EntityMagicSlime)){

			if(target instanceof EntitySlime){
				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
						new TextComponentTranslation("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			}else{

				if(target instanceof EntitySkeleton && caster instanceof EntityPlayer)
					WizardryAdvancementTriggers.slime_skeleton.triggerFor((EntityPlayer)caster);

				if(!world.isRemote){
					EntityMagicSlime slime = new EntityMagicSlime(world, caster, (EntityLivingBase)target,
							(int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)));
					world.spawnEntity(slime);
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
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.SLIME, x, y, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.2f, 0.8f, 0.1f).spawn(world);
	}

}
