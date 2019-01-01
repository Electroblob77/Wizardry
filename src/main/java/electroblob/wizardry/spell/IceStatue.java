package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IceStatue extends SpellRay {

	private static final int BASE_DURATION = 400;

	public IceStatue(){
		super("ice_statue", Tier.APPRENTICE, Element.ICE, SpellType.ATTACK, 15, 40, false, 10, WizardrySounds.SPELL_ICE);
		this.soundValues(1, 1.4f, 0.4f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof EntityLiving && !world.isRemote){
			// Unchecked cast is fine because the block is a static final field
			if(((BlockStatue)WizardryBlocks.ice_statue).convertToStatue((EntityLiving)target,
					(int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)))){
				
				target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				return true;
			}
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
		float brightness = 0.5f + world.rand.nextFloat() * 0.5f;
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8))
		.clr(brightness, brightness + 0.1f, 1.0f).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).time(20 + world.rand.nextInt(10)).spawn(world);
	}

}
