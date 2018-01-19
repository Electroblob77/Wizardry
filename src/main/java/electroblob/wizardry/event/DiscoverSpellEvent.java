package electroblob.wizardry.event;

import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * [NYI] DiscoverSpellEvent is fired when a player discovers a spell by any method.<br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * If this event is canceled, the spell is not discovered.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
public class DiscoverSpellEvent extends PlayerEvent {

	public DiscoverSpellEvent(EntityPlayer player, Spell spell, Source source) {
		super(player);
	}
	
	public enum Source {
		/** Signifies that the spell was discovered by trying to cast it. */ 				CASTING,
		/** Signifies that the spell was discovered using a scroll of identification. */ 	IDENTIFICATION_SCROLL,
		/** Signifies that the spell was discovered using commands. */ 						COMMAND,
		/** Signifies that the spell was discovered by some other means (. */ 				OTHER
	}

}
