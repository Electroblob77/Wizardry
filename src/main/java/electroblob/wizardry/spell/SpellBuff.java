package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Generic superclass for all spells which buff their caster.
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a buff spell; if something extra needs to be done, such as
 * applying a non-potion buff, then methods can be overridden (perhaps using an anonymous class) to add the required
 * functionality.
 * <p></p>
 * Properties added by this type of spell: {@link SpellBuff#getDurationKey(Potion)}, {@link SpellBuff#getStrengthKey(Potion)}
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastByDispensers()}
 * <p></p>
 * By default, this type of spell requires a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
public class SpellBuff extends Spell {
	
	/** An array of factories for the status effects that this spell applies to its caster. The effect factory
	 * avoids the issue of the potions being registered after the spell. */
	protected final Supplier<Potion>[] effects;
	/** A set of all the different potions (status effects) that this spell applies to its caster. Loaded during
	 * init(). */
	protected Set<Potion> potionSet;
	/** The RGB colour values of the particles spawned when this spell is cast. */
	protected final float r, g, b;
	
	/** The number of sparkle particles spawned when this spell is cast. Defaults to 10. */
	protected float particleCount = 10;

	@SafeVarargs
	public SpellBuff(String name, float r, float g, float b, Supplier<Potion>... effects){
		this(Wizardry.MODID, name, r, g, b, effects);
	}

	@SafeVarargs
	public SpellBuff(String modID, String name, float r, float g, float b, Supplier<Potion>... effects){
		super(modID, name, EnumAction.BOW, false);
		this.effects = effects;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	@Override
	public void init(){
		// Loads the potion set
		this.potionSet = Arrays.stream(effects).map(Supplier::get).collect(Collectors.toSet());

		for(Potion potion : potionSet){
			// I don't like having this for all buff spells when some potions aren't affected by amplifiers
			// TODO: Find a way of only adding the strength key if the potion is affected by amplifiers (dynamically if possible)
			// BrewingRecipeRegistry#getOutput might be a good place to start
			addProperties(getStrengthKey(potion));
			if(!potion.isInstant()) addProperties(getDurationKey(potion));
		}
	}

	// Potion-specific equivalent to defining the identifiers as constants

	protected static String getDurationKey(Potion potion){
		return potion.getRegistryName().getPath() + "_duration";
	}

	protected static String getStrengthKey(Potion potion){
		return potion.getRegistryName().getPath() + "_strength";
	}

	/**
	 * Sets the number of sparkle particles spawned when this spell is cast.
	 * @param particleCount The number of particles.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellBuff particleCount(int particleCount){
		this.particleCount = particleCount;
		return this;
	}
	
	@Override public boolean canBeCastByNPCs(){ return true; }
	
	@Override public boolean canBeCastByDispensers() { return true; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// Only return on the server side or the client probably won't spawn particles
		if(!this.applyEffects(caster, modifiers) && !world.isRemote) return false;
		if(world.isRemote) this.spawnParticles(world, caster, modifiers);
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		// Wizards can only cast a buff spell if they don't already have its effects.
		if(caster.getActivePotionMap().keySet().containsAll(potionSet)) return false;
		// Only return on the server side or the client probably won't spawn particles
		if(!this.applyEffects(caster, modifiers) && !world.isRemote) return false;
		if(world.isRemote) this.spawnParticles(world, caster, modifiers);
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}
	
	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		// Gets a 1x1x1 bounding box corresponding to the block in front of the dispenser
		AxisAlignedBB boundingBox = new AxisAlignedBB(new BlockPos(x, y, z));
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, boundingBox);
		
		float distance = -1;
		EntityLivingBase nearestEntity = null;
		// Finds the nearest entity within the bounding box
		for(EntityLivingBase entity : entities){
			float newDistance = (float)entity.getDistance(x, y, z);
			if(distance == -1 || newDistance < distance){
				distance = newDistance;
				nearestEntity = entity;
			}
		}
		
		if(nearestEntity == null) return false;

		// Only return on the server side or the client probably won't spawn particles
		if(!this.applyEffects(nearestEntity, modifiers) && !world.isRemote) return false;
		if(world.isRemote) this.spawnParticles(world, nearestEntity, modifiers);
		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getXOffset(), y - direction.getYOffset(), z - direction.getZOffset(), ticksInUse, duration, modifiers);

		return true;
	}
	
	/** Actually applies the status effects to the caster. By default, this iterates through the array of effects and
	 * applies each in turn, multiplying the duration and amplifier by the appropriate modifiers. Particles are always
	 * hidden and isAmbient is always set to false. Override to do something special, like apply a non-potion buff.
	 * Returns a boolean to allow subclasses to cause the spell to fail if for some reason the effect cannot be applied
	 * (for example, {@link Heal} fails if the caster is on full health). */
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){
		// This will generate 0 for novice and apprentice, and 1 for advanced and master
		// TODO: Once we've found a way of detecting if amplifiers actually affect the potion type, implement it here.
		int bonusAmplifier = getBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));

		for(Potion potion : potionSet){
			caster.addPotionEffect(new PotionEffect(potion, potion.isInstant() ? 1 :
					(int)(getProperty(getDurationKey(potion)).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					(int)getProperty(getStrengthKey(potion)).floatValue() + bonusAmplifier,
					false, true));
		}
		
		return true;
	}

	/** Returns the number to be added to the potion amplifier(s) based on the given potency modifier. Override
	 * to define custom modifier handling. Delegates to {@link SpellBuff#getStandardBonusAmplifier(float)} by
	 * default. */
	protected int getBonusAmplifier(float potencyModifier){
		return getStandardBonusAmplifier(potencyModifier);
	}

	/** Returns a number to be added to potion amplifiers based on the given potency modifier. This method uses
	 * a standard calculation which results in zero extra levels for novice and apprentice wands and one extra
	 * level for advanced and master wands (this generally seems to give about the right weight to potency
	 * modifiers). This is public static because it is useful in a variety of places. */
	public static int getStandardBonusAmplifier(float potencyModifier){
		return (int)((potencyModifier - 1) / 0.4);
	}
	
	/** Spawns buff particles around the caster. Override to add a custom particle effect. Only called client-side. */
	protected void spawnParticles(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		for(int i = 0; i < particleCount; i++){
			double x = caster.posX + world.rand.nextDouble() * 2 - 1;
			double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(r, g, b).spawn(world);
		}
		
		ParticleBuilder.create(Type.BUFF).entity(caster).clr(r, g, b).spawn(world);
	}

}
