package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Ignite extends SpellRay {

	/** The base duration for which entities are set on fire by this spell. */
	private static final int BASE_DURATION = 10;
	
	public Ignite(){
		super("ignite", Tier.BASIC, Element.FIRE, SpellType.ATTACK, 5, 10, false, 10, SoundEvents.ITEM_FLINTANDSTEEL_USE);
		this.soundValues(1, 1, 0.4f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		// Fire can damage armour stands, so this includes them
		if(target instanceof EntityLivingBase) {
			
			if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
						new TextComponentTranslation("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				target.setFire((int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)));
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		pos = pos.offset(side);
		
		if(world.isAirBlock(pos)){
			
			if(!world.isRemote){
				world.setBlockState(pos, Blocks.FIRE.getDefaultState());
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
	}

}
