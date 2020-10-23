package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.entity.construct.EntityIceBarrier;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.GeometryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class FrostBarrier extends Spell {

	private static final double BARRIER_DISTANCE = 2;
	private static final double BARRIER_ARC_RADIUS = 10;
	private static final double BARRIER_SPACING = 1.4;

	public FrostBarrier(){
		super("frost_barrier", SpellActions.SUMMON, false);
		this.npcSelector((e, o) -> true);
		addProperties(DURATION);
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser){
		return true;
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.onGround){
			if(!createBarriers(world, caster.getPositionVector(), caster.getLookVec(), caster, modifiers)) return false;
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){

		if(caster.onGround){
			if(!createBarriers(world, caster.getPositionVector(), target.getPositionVector().subtract(caster.getPositionVector()),
					caster, modifiers)) return false;
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		if(!createBarriers(world, new Vec3d(x, y, z), new Vec3d(direction.getDirectionVec()), null, modifiers)) return false;
		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getXOffset(), y - direction.getYOffset(), z - direction.getZOffset(), ticksInUse, duration, modifiers);
		return true;
	}

	private boolean createBarriers(World world, Vec3d origin, Vec3d direction, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		if(!world.isRemote){

			direction = GeometryUtils.horizontalise(direction);
			Vec3d centre = origin.add(direction.scale(BARRIER_DISTANCE - BARRIER_ARC_RADIUS)); // Arc centred behind caster

			// Don't spawn them yet or the anti-overlap will prevent the rest from spawning
			List<EntityIceBarrier> barriers = new ArrayList<>();

			int barrierCount = 1 + Math.max(1, (int)((modifiers.get(SpellModifiers.POTENCY) - 1) / Constants.POTENCY_INCREASE_PER_TIER + 0.5f));

			for(int i = 0; i < barrierCount; i++){

				EntityIceBarrier barrier = createBarrier(world, centre, direction.rotateYaw((float)(BARRIER_SPACING / BARRIER_ARC_RADIUS) * i), caster, modifiers, barrierCount, i);
				if(barrier != null) barriers.add(barrier);

				if(i == 0) continue; // Only one in the middle
				barrier = createBarrier(world, centre, direction.rotateYaw(-(float)(BARRIER_SPACING / BARRIER_ARC_RADIUS) * i), caster, modifiers, barrierCount, i);
				if(barrier != null) barriers.add(barrier);
			}

			if(barriers.isEmpty()) return false;

			barriers.forEach(world::spawnEntity); // Finally spawn them all
		}

		return true;
	}

	private EntityIceBarrier createBarrier(World world, Vec3d centre, Vec3d direction, @Nullable EntityLivingBase caster, SpellModifiers modifiers, int barrierCount, int index){

		Vec3d position = centre.add(direction.scale(BARRIER_ARC_RADIUS));
		Integer floor = BlockUtils.getNearestFloor(world, new BlockPos(position), 3);
		if(floor == null) return null;
		position = GeometryUtils.replaceComponent(position, Axis.Y, floor);

		float scale = 1.5f - (float)index/barrierCount * 0.5f;
		double yOffset = 1.5 * scale;

		EntityIceBarrier barrier = new EntityIceBarrier(world);
		barrier.setPosition(position.x, position.y - yOffset, position.z);
		barrier.setCaster(caster);
		barrier.lifetime = (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
		barrier.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
		barrier.setRotation((float)Math.toDegrees(MathHelper.atan2(-direction.x, direction.z)), barrier.rotationPitch);
		barrier.setSizeMultiplier(scale);
		barrier.setDelay(1 + 3 * index); // Delay 0 seems to move it down 1 block, no idea why

		if(!world.getEntitiesWithinAABB(barrier.getClass(), barrier.getEntityBoundingBox().offset(0, yOffset, 0)).isEmpty()) return null;

		return barrier;
	}

}
