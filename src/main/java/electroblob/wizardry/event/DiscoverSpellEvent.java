package electroblob.wizardry.event;

import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * DiscoverSpellEvent is fired when a player discovers a spell by any method.<br>
 * <br>
 * This event is {@link Cancelable}. If this event is canceled, the spell is not discovered.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
public class DiscoverSpellEvent extends PlayerEvent {

	private final Spell spell;
	private final Source source;

	public DiscoverSpellEvent(EntityPlayer player, Spell spell, Source source) {
		super(player);
		this.spell = spell;
		this.source = source;
	}
	
	/** Returns the spell that is being discovered. */
	public Spell getSpell(){
		return spell;
	}
	
	/** Returns the method used to discover the spell. */
	public Source getSource(){
		return source;
	}
	
	public enum Source {
		/** Signifies that the spell was discovered by trying to cast it. */ 				CASTING,
		/** Signifies that the spell was discovered using a scroll of identification. */ 	IDENTIFICATION_SCROLL,
		/** Signifies that the spell was discovered using commands. */ 						COMMAND,
		/** Signifies that the spell was discovered by some other means. */ 				OTHER
	}

}