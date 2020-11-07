package electroblob.wizardry.entity.living;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.world.EnumDifficulty;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Interface for entities that can cast spells. Mainly intended for use by wizard-type entities, but can be implemented
 * by any subclass of EntityLiving. Designed to be as flexible as possible - ranging from the simplest use of giving an
 * entity a specific spell as an attack, to a complex AI which selects different spell types depending on the situation.
 * The only restriction is that the spells must be castable by NPCs.
 * <p></p>
 * This is intended for entities that use {@link EntityAIAttackSpell}. If so, all the spell casting code (including
 * packets) is handled by that class, and all the implementor needs to do is decide which spell(s) to select.
 * <p></p>
 * This class also allows Wizardry to do all the syncing necessary for continuous spell casting. All the implementor
 * needs to do is store the actual fields involved, by implementing {@link ISpellCaster#setContinuousSpell(Spell)},
 * {@link ISpellCaster#getContinuousSpell()}, {@link ISpellCaster#setSpellCounter(int)}, {@link ISpellCaster#getSpellCounter()}
 * and {@link ISpellCaster#getModifiers()}.
 */
/* Perhaps this should be a capability? Though I can't help thinking they're mainly for attaching data to vanilla
 * classes, rather than custom ones. For now, the main purpose of this is to centralise code within wizardry itself, and
 * I may as well make it an API feature - but I'm not writing a capability unless someone sees a reason to attach it to
 * a class they don't own, and I don't see that happening anytime soon. */
// For even more fun, combine this with an ISummonedCreature and have skeletons that can cast spells!
public interface ISpellCaster {

	/**
	 * Called each time the entity attacks to get the spells that can be cast. For simple implementations, just return a
	 * list of size one containing the spell that the entity uses as an attack, perhaps performing some simple logic
	 * within this method. For more intelligent implementations based on spell types, consider using
	 * {@link IIntelligentSpellCaster} instead.
	 * 
	 * @return A list of {@link Spell} instances. A random spell from this list will be cast when the entity attacks.
	 *         The list will not be modified by the AI class and can therefore be an immutable list. The spells in the
	 *         list <b>must</b> be castable by NPCs (i.e. {@link Spell#canBeCastBy(net.minecraft.entity.EntityLiving, boolean)} returns true).
	 */
	@Nonnull
	List<Spell> getSpells();

	/**
	 * Called each time the entity attacks to get the modifiers to apply to the spell.
	 * 
	 * @return A {@link SpellModifiers} object representing the modifiers to apply to the spell. If no modifiers are
	 *         required, pass in an empty {@code SpellModifiers} object.
	 */
	@Nonnull
	default SpellModifiers getModifiers(){
		return new SpellModifiers(); // May seem wasteful but this should never be called so it doesn't matter
	}

	/**
	 * Returns the continuous spell that is currently being cast, or the None spell if there is none. Implementors
	 * should simply store this as a private field and return it here. Will be synced by the AI class, but whether it is
	 * saved to NBT is up to you. If the implementing class only ever uses one continuous spell, do <b>not</b> just
	 * return that spell; the field must still be stored.
	 */
	@Nonnull
	default Spell getContinuousSpell(){
		return Spells.none;
	}

	/**
	 * Sets the continuous spell that is currently being cast, or the None spell if there is none. Implementors should
	 * simply store this as a private field and assign it here. Will be synced by the AI class, but whether it is saved
	 * to NBT is up to you. If the implementing class does not deal with continuous spells, leave this method blank.
	 */
	default void setContinuousSpell(Spell spell){
		// Do nothing
	}

	/** Returns the number of ticks the current spell has been cast for. Implementors should simply store this as a
	 * private field and return it here. This is only used client-side. */
	default int getSpellCounter(){
		return 0;
	}

	/** Sets the number of ticks the current spell has been cast for. Implementors should simply store this as a
	 * private field and assign it here. This is only used client-side. */
	default void setSpellCounter(int count){
		// Do nothing
	}
	
	/**
	 * Returns the aiming error for the given difficulty, used in projectile spells. Defaults to the values used by
	 * skeletons, which are: Easy - 10, Normal - 6, Hard - 2, Peaceful - 10 (rarely used).
	 */
	default int getAimingError(EnumDifficulty difficulty) {
		return EntityUtils.getDefaultAimingError(difficulty);
	}
}
