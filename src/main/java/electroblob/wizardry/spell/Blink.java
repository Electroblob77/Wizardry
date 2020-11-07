package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Blink extends Spell {

	public Blink(){
		super("blink", SpellActions.POINT, false);
		addProperties(RANGE);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean teleportMount = caster.isRiding() && ItemArtefact.isArtefactActive(caster, WizardryItems.charm_mount_teleporting);
		boolean hitLiquids = teleportMount && caster.getRidingEntity() instanceof EntityBoat; // Boats teleport to the surface

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		RayTraceResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, hitLiquids, !hitLiquids,false);

		// It's worth noting that on the client side, the cast() method only gets called if the server side
		// cast method succeeded, so you need not check any conditions for spawning particles.
		if(world.isRemote){
			for(int i = 0; i < 10; i++){
				double dx = caster.posX;
				double dy = caster.posY + 2 * world.rand.nextFloat();
				double dz = caster.posZ;
				// For portal particles, velocity is not velocity but the offset where they start, then drift to
				// the actual position given.
				world.spawnParticle(EnumParticleTypes.PORTAL, dx, dy, dz, world.rand.nextDouble() - 0.5,
						world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}

			Wizardry.proxy.playBlinkEffect(caster);
		}

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);
			Entity toTeleport = teleportMount ? caster.getRidingEntity() : caster;

			Vec3d vec = EntityUtils.findSpaceForTeleport(toTeleport, GeometryUtils.getFaceCentre(pos, EnumFacing.DOWN), teleportMount);

			if(vec != null){
				// Plays before and after so it is heard from both positions
				this.playSound(world, caster, ticksInUse, -1, modifiers);

				if(!teleportMount && caster.isRiding()) caster.dismountRidingEntity();
				if(!world.isRemote) toTeleport.setPositionAndUpdate(vec.x, vec.y, vec.z);

				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		float angle = (float)(Math.atan2(target.posZ - caster.posZ, target.posX - caster.posX)
				+ world.rand.nextDouble() * Math.PI);
		double radius = caster.getDistance(target.posX, target.posY, target.posZ)
				+ world.rand.nextDouble() * 3.0d;

		int x = MathHelper.floor(target.posX + MathHelper.sin(angle) * radius);
		int z = MathHelper.floor(target.posZ - MathHelper.cos(angle) * radius);
		Integer y = BlockUtils.getNearestFloor(world, new BlockPos(caster), (int)radius);

		// It's worth noting that on the client side, the cast() method only gets called if the server side
		// cast method succeeded, so you need not check any conditions for spawning particles.

		// For some reason, the wizard version spwans the particles where the wizard started
		if(world.isRemote){
			for(int i = 0; i < 10; i++){
				double dx1 = caster.posX;
				double dy1 = caster.posY + caster.height * world.rand.nextFloat();
				double dz1 = caster.posZ;
				world.spawnParticle(EnumParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
						world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}
		}

		if(y != null){

			// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
			// not teleport 1 block above the ground.
			if(!world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()){
				y--;
			}

			if(world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMovement()
					|| world.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMovement()){
				return false;
			}

			// Plays before and after so it is heard from both positions
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			if(!world.isRemote){
				caster.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override){
		return true;
	}

}
