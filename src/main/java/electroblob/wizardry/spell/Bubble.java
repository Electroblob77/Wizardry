package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityBubble;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Bubble extends SpellRay {

	public Bubble(){
		super("bubble", Tier.APPRENTICE, Element.EARTH, SpellType.ATTACK, 15, 20, false, 10, WizardrySounds.SPELL_ICE);
		this.soundValues(0.5f, 1.1f, 0.2f);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// This spell uses more than one sound, so this is required...
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag) WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_GENERIC_SWIM, 1, 1 + 0.2f * world.rand.nextFloat());
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag) caster.playSound(SoundEvents.ENTITY_GENERIC_SWIM, 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return flag;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			if(!world.isRemote){
				// Deals a small amount damage so the target counts as being hit by the caster
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC), 1);
				
				EntityBubble bubble = new EntityBubble(world);
				bubble.setPosition(target.posX, target.posY, target.posZ);
				bubble.setCaster(caster);
				bubble.lifetime = ((int)(200 * modifiers.get(WizardryItems.duration_upgrade)));
				bubble.isDarkOrb = false;
				bubble.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				
				world.spawnEntity(bubble);
				target.startRiding(bubble);
			}
		}
		
		return true;
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
		world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x, y, z, 0, 0, 0);
		ParticleBuilder.create(Type.MAGIC_BUBBLE).pos(x, y, z).spawn(world);
	}

}
