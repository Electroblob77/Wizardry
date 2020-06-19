package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Snare extends SpellRay {

	public Snare(){
		super("snare", false, SpellActions.POINT);
		this.soundValues(1, 1.4f, 0.4f);
		this.ignoreLivingEntities(true);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(side == EnumFacing.UP && world.isSideSolid(pos, EnumFacing.UP) && BlockUtils.canBlockBeReplaced(world, pos.up())){
			if(!world.isRemote){
				world.setBlockState(pos.up(), WizardryBlocks.snare.getDefaultState());
				((TileEntityPlayerSave)world.getTileEntity(pos.up())).setCaster(caster);
				((TileEntityPlayerSave)world.getTileEntity(pos.up())).sync();
			}
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = world.rand.nextFloat() * 0.25f;
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(20 + world.rand.nextInt(8))
		.clr(brightness, brightness + 0.1f, 0).spawn(world);
		ParticleBuilder.create(Type.LEAF).pos(x, y, z).vel(0, -0.01, 0).time(40 + world.rand.nextInt(10)).spawn(world);
	}

}
