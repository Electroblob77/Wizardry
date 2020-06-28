package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class IceSpikes extends SpellConstructRanged<EntityIceSpike> {
	
	public static final String ICE_SPIKE_COUNT = "ice_spike_count";

	public IceSpikes(){
		super("ice_spikes", EntityIceSpike::new, true);
		addProperties(EFFECT_RADIUS, ICE_SPIKE_COUNT, DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		this.ignoreUncollidables(true);
	}
	
	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){

		if(side == null) return false;

		BlockPos blockHit = new BlockPos(x, y, z);
		if(side.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) blockHit = blockHit.offset(side);

		if(world.getBlockState(blockHit).isNormalCube()) return false;

		Vec3d origin = new Vec3d(x, y, z);

		Vec3d pos = origin.add(new Vec3d(side.getOpposite().getDirectionVec()));
		
		// Now always spawns a spike exactly at the position aimed at
		super.spawnConstruct(world, pos.x, pos.y, pos.z, side, caster, modifiers);
		// -1 because of the one spawned above
		int quantity = (int)(getProperty(ICE_SPIKE_COUNT).floatValue() * modifiers.get(WizardryItems.blast_upgrade)) - 1;

		float maxRadius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		for(int i=0; i<quantity; i++){

			double radius = 0.5 + world.rand.nextDouble() * (maxRadius - 0.5);

			// First, generate a random vector of length radius with a z component of zero
			// Then rotate it so that what was south is now the side that was hit
			Vec3d offset = Vec3d.fromPitchYaw(world.rand.nextFloat() * 180 - 90, world.rand.nextBoolean() ? 0 : 180)
					.scale(radius).rotateYaw(side.getHorizontalAngle() * (float)Math.PI/180).rotatePitch(GeometryUtils.getPitch(side) * (float)Math.PI/180);

			if(side.getAxis().isHorizontal()) offset = offset.rotateYaw((float)Math.PI/2);

			Integer surface = BlockUtils.getNearestSurface(world, new BlockPos(origin.add(offset)), side,
					(int)maxRadius, true, BlockUtils.SurfaceCriteria.basedOn(IBlockState::isNormalCube));

			if(surface != null){
				Vec3d vec = GeometryUtils.replaceComponent(origin.add(offset), side.getAxis(), surface)
						.subtract(new Vec3d(side.getDirectionVec()));
				super.spawnConstruct(world, vec.x, vec.y, vec.z, side, caster, modifiers);
			}
		}
		
		return true;
	}
	
	@Override
	protected void addConstructExtras(EntityIceSpike construct, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		// In this particular case, lifetime is implemented as a delay instead so is treated differently.
		construct.lifetime = 30 + construct.world.rand.nextInt(15);
		construct.setFacing(side);
	}

}
