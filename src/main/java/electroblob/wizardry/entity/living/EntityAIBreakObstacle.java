package electroblob.wizardry.entity.living;

import electroblob.wizardry.util.BlockUtils;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Generic entity AI task that allows a creature to break through a solid block obstructing the direct path to its
 * attack target, once its navigator has been unable to find a path to that target for a while. This stops the
 * entity being cheaply trapped or walled in by blocks placed by the player, since it will eventually dig its own
 * way out rather than standing there uselessly. Respects the {@code mobGriefing} game rule and any other block
 * damage prevention mods via {@link BlockUtils#canBreakBlock(net.minecraft.entity.Entity, World, BlockPos)}.
 */
public class EntityAIBreakObstacle extends EntityAIBase {

	/** The number of consecutive ticks the entity must fail to find a path to its target before it resorts to
	 * breaking through blocks. */
	private static final int STUCK_THRESHOLD = 60;
	/** The number of ticks it takes to break through an obstructing block. */
	private static final int BREAK_TIME = 40;
	/** The maximum number of blocks ahead of the entity to search for an obstruction. */
	private static final int SEARCH_RANGE = 2;

	private final EntityCreature entity;
	private int stuckTime;
	private int breakProgress;
	private BlockPos targetBlock;

	public EntityAIBreakObstacle(EntityCreature entity){
		this.entity = entity;
		// Shares the movement mutex bit so this interrupts normal movement/pathing AI while active.
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute(){

		EntityLivingBase target = entity.getAttackTarget();

		if(target == null || !entity.getNavigator().noPath()){
			stuckTime = 0;
			return false;
		}

		stuckTime++;

		return stuckTime > STUCK_THRESHOLD && findObstacle(target) != null;
	}

	@Override
	public boolean shouldContinueExecuting(){
		return targetBlock != null && entity.getAttackTarget() != null
				&& !entity.world.isAirBlock(targetBlock) && entity.getNavigator().noPath();
	}

	@Override
	public void startExecuting(){
		entity.getNavigator().clearPath();
		targetBlock = findObstacle(entity.getAttackTarget());
		breakProgress = 0;
	}

	@Override
	public void resetTask(){
		targetBlock = null;
		breakProgress = 0;
		stuckTime = 0;
	}

	@Override
	public void updateTask(){

		if(targetBlock == null) return;

		entity.getLookHelper().setLookPosition(targetBlock.getX() + 0.5, targetBlock.getY() + 0.5,
				targetBlock.getZ() + 0.5, 30, 30);

		World world = entity.world;

		breakProgress++;

		// Periodic feedback so the player can tell what's happening and has a chance to react.
		if(breakProgress % 8 == 0){
			world.playSound(null, targetBlock, SoundEvents.BLOCK_STONE_HIT, SoundCategory.HOSTILE, 0.25F, 0.9F);
		}

		if(breakProgress >= BREAK_TIME){

			if(BlockUtils.canBreakBlock(entity, world, targetBlock)){
				world.destroyBlock(targetBlock, true);
			}

			targetBlock = null;
			breakProgress = 0;
			stuckTime = 0;
		}
	}

	/**
	 * Finds a solid, breakable block directly obstructing the straight-line route from the entity towards the given
	 * target, within {@link #SEARCH_RANGE} blocks.
	 * @return The position of the obstructing block, or null if none was found.
	 */
	private BlockPos findObstacle(EntityLivingBase target){

		World world = entity.world;

		double dx = target.posX - entity.posX;
		double dz = target.posZ - entity.posZ;
		double horizontalDistance = MathHelper.sqrt(dx * dx + dz * dz);

		if(horizontalDistance < 0.1) return null;

		dx /= horizontalDistance;
		dz /= horizontalDistance;

		for(int i = 1; i <= SEARCH_RANGE; i++){

			BlockPos pos = new BlockPos(entity.posX + dx * i, entity.posY + entity.height / 2, entity.posZ + dz * i);

			if(!world.isAirBlock(pos) && !BlockUtils.isBlockUnbreakable(world, pos)
					&& BlockUtils.canBreakBlock(entity, world, pos)){
				return pos;
			}
		}

		return null;
	}
}
