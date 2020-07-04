package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Function;

/**
 * Generic superclass for all spells which conjure constructs (i.e. instances of {@link EntityMagicConstruct}) at an
 * aimed-at position (players and dispensers) or target (non-player spell casters).
 * This allows all the relevant code to be centralised, since these spells all work in a similar way. Usually, a simple
 * instantiation of this class is sufficient to create a construct spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the construct entity class instead whenever possible.
 * <p></p>
 * Properties added by this type of spell: {@link Spell#RANGE}, {@link Spell#DURATION} (if the construct is not
 * permanent)
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastBy(EntityLiving, boolean)}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastBy(TileEntityDispenser)}
 * <p></p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see SpellConstruct
 */
public class SpellConstructRanged<T extends EntityMagicConstruct> extends SpellConstruct<T> {

	/** Whether liquids count as blocks when raytracing. Defaults to false. */
	protected boolean hitLiquids = false;
	/** Whether to ignore uncollidable blocks when raytracing. Defaults to false. */
	protected boolean ignoreUncollidables = false;

	public SpellConstructRanged(String name, Function<World, T> constructFactory, boolean permanent){
		this(Wizardry.MODID, name, constructFactory, permanent);
	}

	public SpellConstructRanged(String modID, String name, Function<World, T> constructFactory, boolean permanent){
		super(modID, name, SpellActions.POINT, constructFactory, permanent);
		this.addProperties(RANGE);
		this.npcSelector((e, o) -> true);
	}

	/**
	 * Sets whether liquids count as blocks when raytracing.
	 * @param hitLiquids Whether to hit liquids when raytracing. If this is false, the spell will pass through
	 * liquids as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell hitLiquids(boolean hitLiquids){
		this.hitLiquids = hitLiquids;
		return this;
	}

	/**
	 * Sets whether uncollidable blocks are ignored when raytracing.
	 * @param ignoreUncollidables Whether to hit uncollidable blocks when raytracing. If this is false, the spell will
	 * pass through uncollidable blocks as if they weren't there.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public Spell ignoreUncollidables(boolean ignoreUncollidables){
		this.ignoreUncollidables = ignoreUncollidables;
		return this;
	}

	@Override public boolean requiresPacket(){ return false; }
	
	@Override public boolean canBeCastBy(TileEntityDispenser dispenser) { return true; }
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade);
		RayTraceResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, hitLiquids, ignoreUncollidables, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && (rayTrace.sideHit == EnumFacing.UP ||
				!requiresFloor)){
			
			if(!world.isRemote){
				
				double x = rayTrace.hitVec.x;
				double y = rayTrace.hitVec.y;
				double z = rayTrace.hitVec.z;
				
				if(!spawnConstruct(world, x, y, z, rayTrace.sideHit, caster, modifiers)) return false;
			}
			
		}else if(!requiresFloor){
			
			if(!world.isRemote){
				
				Vec3d look = caster.getLookVec();
				
				double x = caster.posX + look.x * range;
				double y = caster.posY + caster.getEyeHeight() + look.y * range;
				double z = caster.posZ + look.z * range;
				
				if(!spawnConstruct(world, x, y, z, null, caster, modifiers)) return false;
			}
			
		}else{
			return false;
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		double range = getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade);
		Vec3d origin = caster.getPositionEyes(1);

		if(target != null && caster.getDistance(target) <= range){

			if(!world.isRemote){
				
				double x = target.posX;
				double y = target.posY;
				double z = target.posZ;

				RayTraceResult hit = world.rayTraceBlocks(origin, new Vec3d(x, y, z), hitLiquids, ignoreUncollidables, false);

				if(hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK && !hit.getBlockPos().equals(new BlockPos(x, y, z))){
					return false; // Something was in the way
				}

				EnumFacing side = null;
				
				// If the target is not on the ground but the construct must be placed on the floor, searches for the
				// floor under the caster and returns false if it does not find one within 3 blocks.
				if(!target.onGround && requiresFloor){
					Integer floor = BlockUtils.getNearestFloor(world, new BlockPos(x, y, z), 3);
					if(floor == null) return false;
					y = floor;
					side = EnumFacing.UP;
				}
				
				if(!spawnConstruct(world, x, y, z, side, caster, modifiers)) return false;
			}
			
			caster.swingArm(hand);
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}

		return false;
	}
	
	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		
		double range = getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade);
		Vec3d origin = new Vec3d(x, y, z);
		Vec3d endpoint = origin.add(new Vec3d(direction.getDirectionVec()).scale(range));
		RayTraceResult rayTrace = world.rayTraceBlocks(origin, endpoint, hitLiquids, ignoreUncollidables, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && (rayTrace.sideHit == EnumFacing.UP ||
				!requiresFloor)){
			
			if(!world.isRemote){
				
				double x1 = rayTrace.hitVec.x;
				double y1 = rayTrace.hitVec.y;
				double z1 = rayTrace.hitVec.z;
				
				if(!spawnConstruct(world, x1, y1, z1, rayTrace.sideHit, null, modifiers)) return false;
			}
			
		}else if(!requiresFloor){
			
			if(!world.isRemote){
				
				if(!spawnConstruct(world, endpoint.x, endpoint.y, endpoint.z, null, null, modifiers)) return false;
			}
			
		}else{
			return false;
		}

		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getXOffset(), y - direction.getYOffset(), z - direction.getZOffset(), ticksInUse, duration, modifiers);
		return true;
	}

}
