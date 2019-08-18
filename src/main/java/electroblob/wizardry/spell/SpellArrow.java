package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.entity.projectile.EntityMagicArrow;
import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Generic superclass for all spells which launch directed projectiles (i.e. instances of {@link EntityMagicArrow}). This
 * allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a projectile spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the projectile class instead whenever possible.
 * <p></p>
 * Properties added by this type of spell: {@link Spell#RANGE}
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastByDispensers()}
 * <p></p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
public class SpellArrow<T extends EntityMagicArrow> extends Spell {
	
	private static final float DISPENSER_INACCURACY = 1; // This is the same as for players
	private static final float FALLBACK_VELOCITY = 2; // 2 seems to be a pretty standard value
	
	// The general contract for these spell subtypes is that any required parameters are set via the constructor and are
	// final, whereas any non-critical parameters are set via chainable setters with sensible defaults if not. For example,
	// the actual sound to play is required, but it makes sense for its volume and pitch to default to 1 if unspecified.
	
	/** A factory that creates projectile entities. */
	protected final Function<World, T> arrowFactory;
	
	public SpellArrow(String name, Function<World, T> arrowFactory){
		this(Wizardry.MODID, name, arrowFactory);
	}

	public SpellArrow(String modID, String name, Function<World, T> arrowFactory){
		super(modID, name, EnumAction.NONE, false);
		this.arrowFactory = arrowFactory;
		this.addProperties(RANGE);
	}
	
	@Override public boolean requiresPacket(){ return false; }
	
	@Override public boolean canBeCastByNPCs(){ return true; }
	
	@Override public boolean canBeCastByDispensers() { return true; }

	/** Computes the velocity the projectile should be launched at to achieve the required range. */
	// Long story short, it doesn't make much sense to me to have the JSON file specify the velocity - even less so if
	// the velocity is masquerading under the tag 'range' - so we'll let the code do the heavy lifting so people can
	// input something meaningful.
	protected float calculateVelocity(EntityMagicArrow projectile, SpellModifiers modifiers, float launchHeight){
		// The required range
		float range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		if(!projectile.doGravity()){
			// No sensible spell will do this - range is meaningless if the particle has no gravity or lifetime
			if(projectile.getLifetime() <= 0) return FALLBACK_VELOCITY;
			// Speed = distance/time (trivial, I know, but I've put it here for the sake of completeness)
			return range / projectile.getLifetime();
		}else{
			// Arrows have gravity 0.05
			float g = 0.05f;
			// Assume horizontal projection
			return range / MathHelper.sqrt(2 * launchHeight/g);
		}
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		
		if(!world.isRemote){
			// Creates a projectile from the supplied factory
			T projectile = arrowFactory.apply(world);
			// Sets the necessary parameters
			projectile.aim(caster, calculateVelocity(projectile, modifiers, caster.getEyeHeight()
					- (float)EntityMagicArrow.LAUNCH_Y_OFFSET));
			projectile.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
			addArrowExtras(projectile, caster, modifiers);
			// Spawns the projectile in the world
			world.spawnEntity(projectile);
		}

		caster.swingArm(hand);
		
		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){

			if(!world.isRemote){
				// Creates a projectile from the supplied factory
				T projectile = arrowFactory.apply(world);
				// Sets the necessary parameters
				int aimingError = caster instanceof ISpellCaster ? ((ISpellCaster)caster).getAimingError(world.getDifficulty())
						: WizardryUtilities.getDefaultAimingError(world.getDifficulty());
				projectile.aim(caster, target, calculateVelocity(projectile, modifiers, caster.getEyeHeight()
						- (float)EntityMagicProjectile.LAUNCH_Y_OFFSET), aimingError);
				projectile.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				addArrowExtras(projectile, caster, modifiers);
				// Spawns the projectile in the world
				world.spawnEntity(projectile);
			}

			caster.swingArm(hand);
			
			this.playSound(world, caster, ticksInUse, -1, modifiers);

			return true;
		}

		return false;
	}
	
	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		
		if(!world.isRemote){
			// Creates a projectile from the supplied factory
			T projectile = arrowFactory.apply(world);
			// Sets the necessary parameters
			projectile.setPosition(x, y, z);
			Vec3i vec = direction.getDirectionVec();
			projectile.shoot(vec.getX(), vec.getY(), vec.getZ(), calculateVelocity(projectile, modifiers,
					0.375f), DISPENSER_INACCURACY); // 0.375 is the height of the hole in a dispenser
			projectile.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
			addArrowExtras(projectile, null, modifiers);
			// Spawns the projectile in the world
			world.spawnEntity(projectile);
		}

		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getXOffset(), y - direction.getYOffset(), z - direction.getZOffset(), ticksInUse, duration, modifiers);

		return true;
	}

	/**
	 * Called just before the arrow is spawned. Does nothing by default, but subclasses can override to call extra
	 * methods on the spawned arrow. This method is only called server-side so cannot be used to spawn particles directly.
	 * @param arrow The entity being spawned.
	 * @param caster The caster of this spell, or null if it was cast by a dispenser.
	 * @param modifiers The modifiers this spell was cast with.
	 */
	protected void addArrowExtras(T arrow, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		// Subclasses can put spell-specific stuff here
	}
	
}
