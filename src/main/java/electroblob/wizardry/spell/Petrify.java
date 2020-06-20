package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Petrify extends SpellRay {

	// This is more descriptive and more accurate than the standard "effect_duration" in this case
	public static final String MINIMUM_EFFECT_DURATION = "minimum_effect_duration";

	public Petrify(){
		super("petrify", SpellActions.POINT, false);
		this.soundValues(1, 1.1f, 0.2f);
		addProperties(MINIMUM_EFFECT_DURATION);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof EntityLiving && !world.isRemote){
			// Unchecked cast is fine because the block is a static final field
			if(((BlockStatue)WizardryBlocks.petrified_stone).convertToStatue((EntityLiving)target,
					(int)(getProperty(MINIMUM_EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)))){
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
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8)).clr(0.2f, 0.2f, 0.2f).spawn(world);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0.1f, 0.1f).spawn(world);
	}

}
