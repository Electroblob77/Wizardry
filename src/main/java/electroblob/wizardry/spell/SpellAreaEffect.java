package electroblob.wizardry.spell;

import java.util.List;

import javax.annotation.Nullable;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class SpellAreaEffect extends Spell {
	
	// TODO: This class doesn't really work as it is right now, it needs rethinking. The aim is to try and have all the
	// different casting methods call a single (abstract) positional method to do the actual AoE.
	
	/** The base radius of this spell's area of effect. */
	protected final double baseRadius;
	/** The sound that gets played when this spell is cast. */
	@Nullable
	protected final SoundEvent sound;
	
	/** The average number of particles to spawn per block in this spell's area of effect. */
	protected float particleDensity = 0.65f;
	/** The volume of the sound played when this spell is cast. Defaults to 1. */
	protected float volume = 1;
	/** The pitch of the sound played when this spell is cast. Defaults to 1. */
	protected float pitch = 1;
	/** The pitch variation of the sound played when this spell is cast. Defaults to 0. */
	protected float pitchVariation = 0;

	public SpellAreaEffect(String name, Tier tier, Element element, SpellType type, int cost, int cooldown, EnumAction action, double baseRadius, SoundEvent sound){
		this(Wizardry.MODID, name, tier, element, type, cost, cooldown, action, baseRadius, sound);
	}

	public SpellAreaEffect(String modID, String name, Tier tier, Element element, SpellType type, int cost, int cooldown, EnumAction action, double baseRadius, SoundEvent sound){
		super(modID, name, tier, element, type, cost, cooldown, action, false);
		this.baseRadius = baseRadius;
		this.sound = sound;
	}
	
	/**
	 * Sets the sound parameters for this spell.
	 * @param volume 
	 * @param pitch
	 * @param pitchVariation
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellAreaEffect soundValues(float volume, float pitch, float pitchVariation) {
		this.volume = volume;
		this.pitch = pitch;
		this.pitchVariation = pitchVariation;
		return this;
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
		
		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
				baseRadius * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);
		
		targets.removeIf(target -> !WizardryUtilities.isValidTarget(caster, target));
		
		for(EntityLivingBase target : targets){
			affectEntity(world, caster, target, modifiers);
		}
		
		if(world.isRemote){
			spawnParticleEffect(world, caster, modifiers);
		}
		
		if(sound != null) WizardryUtilities.playSoundAtPlayer(caster, sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
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
	 * @param world
	 * @param caster
	 * @param modifiers
	 */
	protected void spawnParticleEffect(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		double maxRadius = baseRadius * modifiers.get(WizardryItems.blast_upgrade);
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
