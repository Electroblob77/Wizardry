package electroblob.wizardry.event;

import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * SpellCastEvent is the parent event class for all spell casting events. Methods which subscribe to this event will
 * receive all three child events (it is recommended that you use {@link SpellCastEvent.Pre},
 * {@link SpellCastEvent.Post} or {@link SpellCastEvent.Tick}, depending on the application).<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
public abstract class SpellCastEvent extends LivingEvent {

	private final Spell spell;
	private final SpellModifiers modifiers;
	private final Source source;

	public SpellCastEvent(EntityLivingBase caster, Spell spell, SpellModifiers modifiers, Source source){
		super(caster);
		this.spell = spell;
		this.modifiers = modifiers;
		this.source = source;
	}

	/** Returns the spell being cast. */
	public Spell getSpell(){
		return spell;
	}

	/** Returns the modifiers for the spell being cast. */
	public SpellModifiers getModifiers(){
		return modifiers;
	}

	/** Returns the source of the spell being cast. */
	public Source getSource(){
		return source;
	}

	public enum Source {
		/** Signifies that the spell was cast using a wand. */
		WAND,
		/** Signifies that the spell was cast using a scroll. */
		SCROLL,
		/** Signifies that the spell was cast using commands. */
		COMMAND,
		/** Signifies that the spell was cast by an {@link ISpellCaster}. */
		NPC,
		/** Signifies that the spell was cast by some other means. */
		OTHER
	}

	/**
	 * SpellCastEvent.Pre is fired just before a spell is cast. Use this event to change the spell modifiers and
	 * generally alter the behaviour of the spell, or stop it from being cast entirely. For example, wizardry uses this
	 * event to cancel spells cast by entities that have the arcane jammer effect. Note that for wands, this is called
	 * <i>before</i> mana, tier and cooldowns are checked. Also note that this event is only fired once for continuous
	 * spells, when they start casting.<br>
	 * <br>
	 * This event is {@link Cancelable}. If this event is canceled, the spell is not cast, mana is not consumed, and the
	 * right-click action that caused it (if any) returns a result of FAIL, meaning that the right-click is passed to
	 * the block/entity in front of the player, if any.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 * <br>
	 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
	 *
	 * @author Electroblob
	 * @since Wizardry 2.1
	 */
	@Cancelable
	public static class Pre extends SpellCastEvent {

		public Pre(EntityLivingBase caster, Spell spell, SpellModifiers modifiers, Source source){
			super(caster, spell, modifiers, source);
		}

	}

	/**
	 * SpellCastEvent.Post is fired just after a spell is cast. Use this event for any processing which depends on
	 * whether the spell succeeds, and does not affect the spell itself. For example, wizardry uses this event to keep
	 * track of spellcasting stats. Note that although this event is fired from both sides, it is not fired from common
	 * code; rather, both sides fire it separately, so timing is not guaranteed. Also note that this event is only fired
	 * once for continuous spells, after the first casting tick.<br>
	 * <br>
	 * This event is not {@link Cancelable}.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 * <br>
	 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
	 *
	 * @author Electroblob
	 * @since Wizardry 2.1
	 */
	public static class Post extends SpellCastEvent {

		public Post(EntityLivingBase caster, Spell spell, SpellModifiers modifiers, Source source){
			super(caster, spell, modifiers, source);
		}

	}

	/**
	 * SpellCastEvent.Tick is fired each tick while a continuous spell is being cast.<br>
	 * <br>
	 * This event is {@link Cancelable}. If this event is canceled, the spell is not cast, mana is not consumed, and the
	 * spell casting is interrupted. Cancelling this event on the client side will stop particles being spawned, but
	 * will not interrupt spell casting.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 * <br>
	 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
	 *
	 * @author Electroblob
	 * @since Wizardry 2.1
	 */
	@Cancelable
	public static class Tick extends SpellCastEvent {

		private final int count;

		public Tick(EntityLivingBase caster, Spell spell, SpellModifiers modifiers, Source source, int count){
			super(caster, spell, modifiers, source);
			this.count = count;
		}

		/** Returns the number of ticks this (continuous) spell has already been cast for. */
		public int getCount(){
			return count;
		}

	}

}
