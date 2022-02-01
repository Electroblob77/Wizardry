package electroblob.wizardry.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * ResurrectionEvent is fired when a player is about to be resurrected, after all other checks have been performed.
 * <i>Note that this event is only fired on the server side.</i><br>
 * <br>
 * This event is {@link Cancelable}. If this event is canceled, the player is not resurrected.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Cancelable
public class ResurrectionEvent extends PlayerEvent {

	private final EntityPlayer caster;

	public ResurrectionEvent(EntityPlayer player, EntityPlayer caster){
		super(player);
		this.caster = caster;
	}

	/** Returns the player that cast the resurrection spell. If the player resurrected themselves, this will be the
	 * same as {@link ResurrectionEvent#getEntityPlayer()}. */
	public EntityPlayer getCaster(){
		return caster;
	}

}
