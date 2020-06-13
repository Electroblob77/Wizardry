package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

/** [NYI] */
public abstract class SpellAreaEffect extends Spell {
	
	// TODO: This class doesn't really work as it is right now, it needs rethinking. The aim is to try and have all the
	// different casting methods call a single (abstract) positional method to do the actual AoE.

	/** The average number of particles to spawn per block in this spell's area of effect. */
	protected float particleDensity = 0.65f;
	
	public SpellAreaEffect(String name, EnumAction action){
		this(Wizardry.MODID, name, action);
	}

	public SpellAreaEffect(String modID, String name, EnumAction action){
		super(modID, name, action, false);
		this.addProperties(EFFECT_RADIUS);
	}
	
	/**
	 * Sets the number of particles to spawn per block for this spell.
	 * @param particleDensity The average number of particles to spawn per block in this spell's area of effect.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellAreaEffect particleDensity(float particleDensity) {
		this.particleDensity = particleDensity;
		return this;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		
		List<EntityLivingBase> targets = EntityUtils.getEntitiesWithinRadius(getProperty(EFFECT_RADIUS).floatValue()
				* modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);
		
		targets.removeIf(target -> !AllyDesignationSystem.isValidTarget(caster, target));
		
		for(EntityLivingBase target : targets){
			affectEntity(world, caster, target, modifiers);
		}
		
		if(world.isRemote){
			spawnParticleEffect(world, caster, modifiers);
		}
		
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
		
	}
	
	/**
	 * Called to do something to each entity within the spell's area of effect.
	 * @param world The world in which the spell was cast.
	 * @param caster The entity that cast the spell.
	 * @param target The entity to do something to.
	 * @param modifiers The modifiers the spell was cast with.
	 */
	protected abstract void affectEntity(World world, EntityLivingBase caster, EntityLivingBase target, SpellModifiers modifiers);
	
	/**
	 * Called to spawn the spell's particle effect. By default, this generates a set of random points within the spell's
	 * area of effect and calls {@link SpellAreaEffect#spawnParticle(World, double, double, double)} at each to spawn
	 * the individual particles. Only called client-side. Override to add a custom particle effect.
	 * @param world The world to spawn the particles in.
	 * @param caster The caster of the spell.
	 * @param modifiers The modifiers the spell was cast with.
 	 */
	protected void spawnParticleEffect(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		double maxRadius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);
		int particleCount = (int)Math.round(particleDensity * Math.PI * maxRadius * maxRadius);
		
		for(int i=0; i<particleCount; i++){
			
			double radius = (1 + world.rand.nextDouble() * (maxRadius - 1));
			float angle = world.rand.nextFloat() * (float)Math.PI * 2f;
			
			spawnParticle(world, caster.posX + radius * MathHelper.cos(angle),
					caster.getEntityBoundingBox().minY,
					caster.posZ + radius * MathHelper.sin(angle));
		}
	}
	
	/**
	 * Called at each point within the spell's area of effect to spawn one or more particles at that point. Only called
	 * client-side. Does nothing by default.
	 * @param world The world in which to spawn the particle.
	 * @param x The x-coordinate to spawn the particle at, already set to a random point within the spell's area of effect.
	 * @param y The y-coordinate to spawn the particle at, already set to a random point within the spell's area of effect.
	 * @param z The z-coordinate to spawn the particle at, already set to a random point within the spell's area of effect.
	 */
	protected void spawnParticle(World world, double x, double y, double z){}

}
