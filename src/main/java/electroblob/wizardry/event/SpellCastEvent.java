package electroblob.wizardry.event;

import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nullable;

/**
 * SpellCastEvent is the parent event class for all spell casting events. Methods which subscribe to this event will
 * receive all four child events (it is recommended that you use {@link SpellCastEvent.Pre},
 * {@link SpellCastEvent.Post}, {@link SpellCastEvent.Tick} or {@link SpellCastEvent.Finish}, depending on the application).<br>
 * <br>
 * <i>A note about spell modifiers: As of Wizardry 4.2, item-based spell casting has been reorganised, a notable change
 * being that spell modifiers are no longer recalculated each tick for continuous spells. Instead, spell modifiers
 * are stored in {@link WizardData WizardData} at the start (after {@code SpellCastEvent.Pre})
 * and simply passed in each tick. This means the {@code SpellModifiers} object received by {@code SpellCastEvent.Tick}
 * contains those modified values, and any changes made to them will <b>not</b> persist between ticks.
 * <p></p>
 * This means that where before you had to change the modifiers in {@code Pre} <b>and</b> {@code Tick}, you now only
 * need to change them in {@code Pre} unless you want them to change partway through casting.</i>
 * <p></p>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
public abstract class SpellCastEvent extends Event {

	private final Spell spell;
	private final SpellModifiers modifiers;
	private final Source source;
	private final EntityLivingBase caster;
	private final World world;
	private final double x, y, z;
	private final EnumFacing direction;

	public SpellCastEvent(Source source, Spell spell, EntityLivingBase caster, SpellModifiers modifiers){
		super();
		this.spell = spell;
		this.modifiers = modifiers;
		this.source = source;
		this.caster = caster;
		this.world = caster.world; // World is required for the position-based casting, but we may as well set it here
		this.x = Double.NaN; // Better to use NaN than some arbitrary number, because NaN will throw an exception when
		this.y = Double.NaN; // someone tries to operate on it whereas 0, for example, will likely just cause strange
		this.z = Double.NaN; // behaviour - the cause of which may not be immediately obvious.
		this.direction = null;
	}
	
	public SpellCastEvent(Source source, Spell spell, World world, double x, double y, double z, EnumFacing direction, SpellModifiers modifiers){
		super();
		this.spell = spell;
		this.modifiers = modifiers;
		this.source = source;
		this.caster = null;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.direction = direction;
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
	
	/** Returns the entity that cast this spell, or null if it was cast from a dispenser or a command with coordinates. */
	@Nullable
	public EntityLivingBase getCaster(){
		return caster;
	}
	
	/** Returns the world in which this spell was cast. If the spell was cast by an entity, this is equivalent to
	 * {@code getCaster().world}. */
	public World getWorld(){
		return world;
	}
	
	/** Returns the x coordinate at which this spell was cast, or NaN if the spell was not cast from a position. */
	public double getX(){
		return x;
	}
	
	/** Returns the y coordinate at which this spell was cast, or NaN if the spell was not cast from a position. */
	public double getY(){
		return y;
	}
	
	/** Returns the z coordinate at which this spell was cast, or NaN if the spell was not cast from a position. */
	public double getZ(){
		return z;
	}
	
	/** Returns the direction in which this spell was cast, or null if the spell was not cast from a position. */
	@Nullable
	public EnumFacing getDirection(){
		return direction;
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
		/** Signifies that the spell was cast from a dispenser. */
		DISPENSER,
		/** Signifies that the spell was cast by some other means. */
		OTHER
	}

	/**
	 * SpellCastEvent.Pre is fired just before a spell is cast. Use this event to change the spell modifiers and
	 * generally alter the behaviour of the spell, or stop it from being cast entirely. For example, wizardry uses this
	 * event to cancel spells cast by entities that have the arcane jammer effect. For wands, this is called
	 * <i>before</i> mana, tier and cooldowns are checked, and before charge-up. Also note that this event is only fired
	 * once for continuous spells, when they start casting.<br>
	 * <br>
	 * This event is {@link Cancelable}. If this event is canceled, the spell is not cast, mana is not consumed, and the
	 * right-click action that caused it (if any) returns a result of FAIL, meaning that the right-click is passed to
	 * the block/entity in front of the player, if any.<br>
	 * <br>
	 * <b>Priority convention for {@code SpellCastEvent.Pre}:</b><br>
	 * {@code HIGHEST} - General spell prevention e.g. arcane jammer<br>
	 * {@code HIGH} - Specific spell prevention e.g. spells disabled in config/JSONs<br>
	 * {@code NORMAL} - Everything else e.g. forfeits<br>
	 * {@code LOW} - Changes to modifiers e.g. from potion effects<br>
	 * {@code LOWEST} - Anything that requires modifiers to be at their final values, unused in wizardry<br>
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

		public Pre(Source source, Spell spell, EntityLivingBase caster, SpellModifiers modifiers){
			super(source, spell, caster, modifiers);
		}
		
		public Pre(Source source, Spell spell, World world, double x, double y, double z, EnumFacing direction, SpellModifiers modifiers){
			super(source, spell, world, x, y, z, direction, modifiers);
		}
	}

	/**
	 * SpellCastEvent.Post is fired just after a spell is cast. Use this event for any processing which depends on
	 * whether the spell succeeds, and does not affect the spell itself. For example, wizardry uses this event to keep
	 * track of spellcasting stats. Note that although this event is fired from both sides, it is not fired from common
	 * code; rather, both sides fire it separately, so timing is not guaranteed. Also note that this event is only fired
	 * once for continuous spells, after the first casting tick. Changing the modifiers within this event will likely
	 * have no effect, with the exception of cooldown modifiers.<br>
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

		public Post(Source source, Spell spell, EntityLivingBase caster, SpellModifiers modifiers){
			super(source, spell, caster, modifiers);
		}
		
		public Post(Source source, Spell spell, World world, double x, double y, double z, EnumFacing direction, SpellModifiers modifiers){
			super(source, spell, world, x, y, z, direction, modifiers);
		}

	}

	/**
	 * SpellCastEvent.Tick is fired each tick while a continuous spell is being cast.<br>
	 * <br>
	 * This event is {@link Cancelable}. If this event is canceled, the spell is not cast, mana is not consumed, and the
	 * spell casting is interrupted. Cancelling this event on the client side only will stop particles being spawned, but
	 * will not interrupt spell casting. Cancelling this event on the server side only may have different results
	 * depending on the source of the spell; as such it is recommended that this event is cancelled on both sides. <br>
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

		public Tick(Source source, Spell spell, EntityLivingBase caster, SpellModifiers modifiers, int count){
			super(source, spell, caster, modifiers);
			this.count = count;
		}
		
		public Tick(Source source, Spell spell, World world, double x, double y, double z, EnumFacing direction, SpellModifiers modifiers, int count){
			super(source, spell, world, x, y, z, direction, modifiers);
			this.count = count;
		}

		/** Returns the number of ticks this (continuous) spell has already been cast for. */
		public int getCount(){
			return count;
		}

	}

	/**
	 * SpellCastEvent.Finish is fired just after a continuous spell stops being cast. Use this event for anything
	 * that needs to know the total time for which a continuous spell has been cast, or should only happen when the
	 * spell finishes (as opposed to just after it starts, as in {@link Post}).<br>
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
	public static class Finish extends SpellCastEvent {

		private final int count;

		public Finish(Source source, Spell spell, EntityLivingBase caster, SpellModifiers modifiers, int count){
			super(source, spell, caster, modifiers);
			this.count = count;
		}

		public Finish(Source source, Spell spell, World world, double x, double y, double z, EnumFacing direction, SpellModifiers modifiers, int count){
			super(source, spell, world, x, y, z, direction, modifiers);
			this.count = count;
		}

		/** Returns the total number of ticks this (continuous) spell was cast for. */
		public int getCount(){
			return count;
		}

	}

}
