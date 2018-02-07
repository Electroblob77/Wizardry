package electroblob.wizardry.entity.living;

import java.util.List;

import electroblob.wizardry.spell.Spell;

/**
 * [NYI] Interface for entities that can select between spells based on their current circumstances, to be used in
 * conjunction with {@link EntityAISelectSpell}.
 */
public interface IIntelligentSpellCaster extends ISpellCaster {

	/**
	 * Called from {@link EntityAISelectSpell} to set the spells that the entity can use in its current situation.
	 * Implementors should assign the given list to some internal field and retrieve it when {@link #getSpells()} is
	 * called.
	 */
	public void setCurrentSpells(List<Spell> spells);

	/**
	 * Returns a list of spells that this entity 'knows'. The entity's current spell will be selected from this list
	 * based on the current situation: whether it is attacking, its health, etc. Most likely, you will want to return a
	 * constant list, but it may change for some reason - perhaps if the entity 'learns' a new spell.
	 */
	public List<Spell> getKnownSpells();
}
