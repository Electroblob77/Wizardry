package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
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
		super("phase_step", SpellActions.POINT, false);
		addProperties(RANGE, WALL_THICKNESS);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean teleportMount = caster.isRiding() && ItemArtefact.isArtefactActive(caster, WizardryItems.charm_mount_teleporting);
		boolean hitLiquids = teleportMount && caster.getRidingEntity() instanceof EntityBoat; // Boats teleport to the surface

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		RayTraceResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, hitLiquids, !hitLiquids, false);

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

		Entity toTeleport = teleportMount ? caster.getRidingEntity() : caster;

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
				if((BlockUtils.isBlockUnbreakable(world, pos1) || BlockUtils.isBlockUnbreakable(world, pos1.up()))
						&& !Wizardry.settings.teleportThroughUnbreakableBlocks)
					break; // Don't return false yet, there are other possible outcomes below now

				Vec3d vec = GeometryUtils.getFaceCentre(pos1, EnumFacing.DOWN);
				if(attemptTeleport(world, toTeleport, vec, teleportMount, caster, ticksInUse, modifiers)) return true;
			}

			// If no suitable position was found on the other side of the wall, works like blink instead
			pos = pos.offset(rayTrace.sideHit);

			Vec3d vec = GeometryUtils.getFaceCentre(pos, EnumFacing.DOWN);
			if(attemptTeleport(world, toTeleport, vec, teleportMount, caster, ticksInUse, modifiers)) return true;

		}else{ // The ray trace missed
			Vec3d vec = caster.getPositionVector().add(caster.getLookVec().scale(range));
			if(attemptTeleport(world, toTeleport, vec, teleportMount, caster, ticksInUse, modifiers)) return true;
		}

		return false;
	}

	protected boolean attemptTeleport(World world, Entity toTeleport, Vec3d destination, boolean teleportMount, EntityPlayer caster, int ticksInUse, SpellModifiers modifiers){

		destination = EntityUtils.findSpaceForTeleport(toTeleport, destination, teleportMount);

		if(destination != null){
			// Plays before and after so it is heard from both positions
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			if(!teleportMount && caster.isRiding()) caster.dismountRidingEntity();
			if(!world.isRemote) toTeleport.setPositionAndUpdate(destination.x, destination.y, destination.z);

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}

}
