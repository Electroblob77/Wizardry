package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class PhaseStep extends Spell {

	public static final String WALL_THICKNESS = "wall_thickness";

	public PhaseStep(){
		super("phase_step", EnumAction.NONE, false);
		addProperties(RANGE, WALL_THICKNESS);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);
		RayTraceResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, false);

		// This is here because the conditions are false on the client for whatever reason. (see the Javadoc for cast()
		// for an explanation)
		if(world.isRemote){

			for(int i = 0; i < 10; i++){
				double dx1 = caster.posX;
				double dy1 = caster.getEntityBoundingBox().minY + 2 * world.rand.nextFloat();
				double dz1 = caster.posZ;
				world.spawnParticle(EnumParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
						world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}

			Wizardry.proxy.playBlinkEffect(caster);
		}

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos();

			// The maximum wall thickness as determined by the range multiplier. The + 0.5f is so that
			// weird float processing doesn't incorrectly round it down.
			int maxThickness = getProperty(WALL_THICKNESS).intValue()
					+ (int)((modifiers.get(WizardryItems.range_upgrade) - 1) / Constants.RANGE_INCREASE_PER_LEVEL + 0.5f);

			if(rayTrace.sideHit == EnumFacing.UP) maxThickness++; // Allow space for the player's head

			// i represents how far the player needs to teleport to get through the wall
			for(int i = 0; i <= maxThickness; i++){

				BlockPos pos1 = pos.offset(rayTrace.sideHit.getOpposite(), i);

				// Prevents the player from teleporting through unbreakable blocks, so they cannot cheat in other
				// mods' mazes and dungeons.
				if((WizardryUtilities.isBlockUnbreakable(world, pos1) || WizardryUtilities.isBlockUnbreakable(world, pos1.up()))
						&& !Wizardry.settings.teleportThroughUnbreakableBlocks)
					break; // Don't return false yet, there are other possible outcomes below now

				if(!world.getBlockState(pos1).getMaterial().blocksMovement()
						&& !world.getBlockState(pos1.up()).getMaterial().blocksMovement()){

					// Plays before and after so it is heard from both positions
					this.playSound(world, caster, ticksInUse, -1, modifiers);

					if(!world.isRemote){
						caster.setPositionAndUpdate(pos1.getX() + 0.5, pos1.getY() + 0.5, pos1.getZ() + 0.5);
					}

					caster.swingArm(hand);
					this.playSound(world, caster, ticksInUse, -1, modifiers);
					return true;
				}
			}

			// If no suitable position was found on the other side of the wall, works like blink instead

			// Leave space for the player's head
			if(rayTrace.sideHit == EnumFacing.DOWN) pos = pos.down();

			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the player does
			// not teleport 1 block above the ground.
			if(rayTrace.sideHit == EnumFacing.UP && !world.getBlockState(pos).getMaterial().blocksMovement()){
				pos = pos.down();
			}

			pos = pos.offset(rayTrace.sideHit);

			// Prevents the player from teleporting into blocks and suffocating
			if(world.getBlockState(pos).getMaterial().blocksMovement()
					|| world.getBlockState(pos.up()).getMaterial().blocksMovement()){
				return false;
			}

			// Plays before and after so it is heard from both positions
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			if(!world.isRemote) caster.setPositionAndUpdate(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;

		}else{ // The ray trace missed

			Vec3d destination = caster.getPositionVector().add(caster.getLookVec().scale(range));
			BlockPos pos = new BlockPos(destination);

			// Prevents the player from teleporting into blocks and suffocating.
			if(world.getBlockState(pos).getMaterial().blocksMovement()
					|| world.getBlockState(pos.up()).getMaterial().blocksMovement()){
				return false;
			}

			if(!world.isRemote) caster.setPositionAndUpdate(destination.x, destination.y, destination.z);

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;
		}
	}

}
