package electroblob.wizardry.spell;

import java.util.function.Function;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.entity.projectile.EntityBomb;
import electroblob.wizardry.entity.projectile.EntityMagicProjectile;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which launch non-directed projectiles (i.e. instances of {@link EntityMagicProjectile}).
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a projectile spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the projectile class instead whenever possible.
 * <p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#doesSpellRequirePacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
public class SpellProjectile extends Spell {
	
	// The general contract for these spell subtypes is that any required parameters are set via the constructor and are
	// final, whereas any non-critical parameters are set via chainable setters with sensible defaults if not. For example,
	// the actual sound to play is required, but it makes sense for its volume and pitch to default to 1 if unspecified.
	
	/** A factory that creates projectile entities. */
	protected final Function<World, EntityMagicProjectile> projectileFactory;
	/** The base range (projectile speed) of this spell. */
	protected final float baseRange;
	/** The sound that gets played when this spell is cast. */
	protected final SoundEvent sound;
	
	/** The volume of the sound played when this spell is cast. Defaults to 1. */
	protected float volume = 1;
	/** The pitch of the sound played when this spell is cast. Defaults to 1. */
	protected float pitch = 1;
	/** The pitch variation of the sound played when this spell is cast. Defaults to 0. */
	protected float pitchVariation = 0;
	
	public SpellProjectile(String name, Tier tier, Element element, int cost, int cooldown, Function<World, EntityMagicProjectile> projectileFactory, float baseRange, SoundEvent sound) {
		this(Wizardry.MODID, name, tier, element, cost, cooldown, projectileFactory, baseRange, sound);
	}

	public SpellProjectile(String modID, String name, Tier tier, Element element, int cost, int cooldown, Function<World, EntityMagicProjectile> projectileFactory, float baseRange, SoundEvent sound){
		super(modID, name, tier, element, SpellType.PROJECTILE, cost, cooldown, EnumAction.NONE, false);
		this.projectileFactory = projectileFactory;
		this.baseRange = baseRange;
		this.sound = sound;
	}
	
	/**
	 * Sets the sound parameters for this spell.
	 * @param volume 
	 * @param pitch
	 * @param pitchVariation
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellProjectile soundValues(float volume, float pitch, float pitchVariation) {
		this.volume = volume;
		this.pitch = pitch;
		this.pitchVariation = pitchVariation;
		return this;
	}
	
	@Override public boolean doesSpellRequirePacket(){ return false; }
	
	@Override public boolean canBeCastByNPCs(){ return true; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		
		if(!world.isRemote){
			// Creates a projectile from the supplied factory
			EntityMagicProjectile projectile = projectileFactory.apply(world);
			// Sets the necessary parameters
			projectile.aim(caster, baseRange * modifiers.get(WizardryItems.range_upgrade));
			projectile.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
			if(projectile instanceof EntityBomb) ((EntityBomb)projectile).blastMultiplier = modifiers.get(WizardryItems.blast_upgrade);
			// Spawns the projectile in the world
			world.spawnEntity(projectile);
		}

		caster.swingArm(hand);
		
		WizardryUtilities.playSoundAtPlayer(caster, sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));

		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){

			if(!world.isRemote){
				// Creates a projectile from the supplied factory
				EntityMagicProjectile projectile = projectileFactory.apply(world);
				// Sets the necessary parameters
				int aimingError = caster instanceof ISpellCaster ? ((ISpellCaster)caster).getAimingError(world.getDifficulty())
						: WizardryUtilities.getDefaultAimingError(world.getDifficulty());
				projectile.aim(caster, target, baseRange * modifiers.get(WizardryItems.range_upgrade), aimingError);
				projectile.damageMultiplier = modifiers.get(SpellModifiers.POTENCY);
				if(projectile instanceof EntityBomb) ((EntityBomb)projectile).blastMultiplier = modifiers.get(WizardryItems.blast_upgrade);
				// Spawns the projectile in the world
				world.spawnEntity(projectile);
			}

			caster.swingArm(hand);
			
			caster.playSound(sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));

			return true;
		}

		return false;
	}
	
}
