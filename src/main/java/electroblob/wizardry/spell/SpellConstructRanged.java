package electroblob.wizardry.spell;

import java.util.function.Function;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which conjure constructs (i.e. instances of {@link EntityMagicConstruct}) at an
 * aimed-at position (players and dispensers) or target (non-player spell casters).
 * This allows all the relevant code to be centralised, since these spells all work in a similar way. Usually, a simple
 * instantiation of this class is sufficient to create a construct spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the construct entity class instead whenever possible.
 * <p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#doesSpellRequirePacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see SpellConstruct
 */
public class SpellConstructRanged<T extends EntityMagicConstruct> extends SpellConstruct<T> {
	
	/** The base range of this spell. */
	protected final double baseRange;

	public SpellConstructRanged(String name, Tier tier, Element element, SpellType type, int cost, int cooldown,
			Function<World, T> constructFactory, int baseDuration, double baseRange, SoundEvent sound){
		this(Wizardry.MODID, name, tier, element, type, cost, cooldown, constructFactory, baseDuration, baseRange, sound);
	}

	public SpellConstructRanged(String modID, String name, Tier tier, Element element, SpellType type, int cost, int cooldown,
			Function<World, T> constructFactory, int baseDuration, double baseRange, SoundEvent sound){
		super(modID, name, tier, element, type, cost, cooldown, EnumAction.NONE, constructFactory, baseDuration, sound);
		this.baseRange = baseRange;
	}

	@Override public boolean doesSpellRequirePacket(){ return false; }

	@Override public boolean canBeCastByNPCs(){ return true; }
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = baseRange * modifiers.get(WizardryItems.range_upgrade);
		RayTraceResult rayTrace = WizardryUtilities.standardBlockRayTrace(world, caster, range, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && (rayTrace.sideHit == EnumFacing.UP ||
				!requiresFloor)){
			
			if(!world.isRemote){
				
				double x = rayTrace.hitVec.x;
				double y = rayTrace.hitVec.y;
				double z = rayTrace.hitVec.z;
				
				if(!spawnConstruct(world, x, y, z, caster, modifiers)) return false;
			}
			
		}else if(!requiresFloor){
			
			if(!world.isRemote){
				
				Vec3d look = caster.getLookVec();
				
				double x = caster.posX + look.x * range;
				double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() + look.y * range;
				double z = caster.posZ + look.z * range;
				
				if(!spawnConstruct(world, x, y, z, caster, modifiers)) return false;
			}
			
		}else{
			return false;
		}
		
		caster.swingArm(hand);
		if(sound != null) WizardryUtilities.playSoundAtPlayer(caster, sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		double range = baseRange * modifiers.get(WizardryItems.range_upgrade);
		
		if(target != null && caster.getDistance(target) <= range){

			if(!world.isRemote){
				
				double x = target.posX;
				double y = target.posY;
				double z = target.posZ;
				
				// If the target is not on the ground but the construct must be placed on the floor, searches for the
				// floor under the caster and returns false if it does not find one within 3 blocks.
				if(!target.onGround && requiresFloor){
					y = WizardryUtilities.getNearestFloorLevel(world, new BlockPos(x, y, z), 3);
					if(y < 0) return false;
				}
				
				if(!spawnConstruct(world, x, y, z, caster, modifiers)) return false;
			}
			
			caster.swingArm(hand);
			if(sound != null) caster.playSound(sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
			return true;
		}

		return false;
	}

}
