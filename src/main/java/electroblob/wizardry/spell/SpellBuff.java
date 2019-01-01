package electroblob.wizardry.spell;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Triple;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which buff their caster.
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a buff spell; if something extra needs to be done, such as
 * applying a non-potion buff, then methods can be overridden (perhaps using an anonymous class) to add the required
 * functionality.
 * <p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p>
 * By default, this type of spell requires a packet to be sent. {@link Spell#doesSpellRequirePacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
public class SpellBuff extends Spell {
	
	/** An array of triples representing the parameters of the status effects that this spell applies to its caster.
	 * These are of the form [effect, base duration, base amplifier]. */
	protected final Triple<Potion, Integer, Integer>[] effects;
	/** A set of all the different potions (status effects) that this spell applies to its caster. */
	protected final Set<Potion> potionSet;
	/** The sound that gets played when this spell is cast. */
	protected final SoundEvent sound;
	/** The RGB colour values of the particles spawned when this spell is cast. */
	protected final float r, g, b;
	
	/** The volume of the sound played when this spell is cast. Defaults to 1. */
	protected float volume = 1;
	/** The pitch of the sound played when this spell is cast. Defaults to 1. */
	protected float pitch = 1;
	/** The pitch variation of the sound played when this spell is cast. Defaults to 0. */
	protected float pitchVariation = 0;
	/** The number of sparkle particles spawned when this spell is cast. Defualts to 10. */
	protected float particleCount = 10;

	@SafeVarargs
	public SpellBuff(String name, Tier tier, Element element, SpellType type, int cost, int cooldown, SoundEvent sound, float r, float g, float b, Triple<Potion, Integer, Integer>... effects){
		this(Wizardry.MODID, name, tier, element, type, cost, cooldown, sound, r, g, b, effects);
	}

	@SafeVarargs
	public SpellBuff(String modID, String name, Tier tier, Element element, SpellType type, int cost, int cooldown, SoundEvent sound, float r, float g, float b, Triple<Potion, Integer, Integer>... effects){
		super(modID, name, tier, element, type, cost, cooldown, EnumAction.BOW, false);
		this.sound = sound;
		this.effects = effects;
		this.r = r;
		this.g = g;
		this.b = b;
		// Pretty sure this is better than iterating over (or streaming) all the elements each time the spell is cast.
		this.potionSet = Arrays.stream(effects).map(Triple::getLeft).collect(Collectors.toSet());
	}
	
	/**
	 * Sets the sound parameters for this spell.
	 * @param volume 
	 * @param pitch
	 * @param pitchVariation
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellBuff soundValues(float volume, float pitch, float pitchVariation) {
		this.volume = volume;
		this.pitch = pitch;
		this.pitchVariation = pitchVariation;
		return this;
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

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		
		if(!this.applyEffects(caster, modifiers)) return false;
		if(world.isRemote) this.spawnParticles(world, caster, modifiers);
		WizardryUtilities.playSoundAtPlayer(caster, sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		// Wizards can only cast a buff spell if they don't already have its effects.
		if(caster.getActivePotionMap().keySet().containsAll(potionSet)) return false;
		if(!this.applyEffects(caster, modifiers)) return false;
		if(world.isRemote) this.spawnParticles(world, caster, modifiers);
		caster.playSound(sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
		return true;
	}
	
	/** Actually applies the status effects to the caster. By default, this iterates through the array of effects and
	 * applies each in turn, multiplying the duration and amplifier by the appropriate modifiers. Particles are always
	 * hidden and isAmbient is always set to false. Override to do something special, like apply a non-potion buff.
	 * Returns a boolean to allow subclasses to cause the spell to fail if for some reason the effect cannot be applied
	 * (for example, {@link Heal} fails if the caster is on full health). */
	protected boolean applyEffects(EntityLivingBase caster, SpellModifiers modifiers){
		// TODO: Find a good way (if any) of implementing potency modifiers
		for(Triple<Potion, Integer, Integer> effect : effects){
			caster.addPotionEffect(new PotionEffect(effect.getLeft(), (int)(effect.getMiddle()
					* modifiers.get(WizardryItems.duration_upgrade)), effect.getRight(), false, false));
		}
		
		return true;
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
