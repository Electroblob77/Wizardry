package electroblob.wizardry.event;

import electroblob.wizardry.item.ItemArtefact;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * SpellBindEvent is fired when a check happens for an ItemArtefact using {@link electroblob.wizardry.item.ItemArtefact#isArtefactActive(net.minecraft.entity.player.EntityPlayer, net.minecraft.item.Item)}
 * <i>Fired on both sides.</i><br>
 * <br>
 * This event is {@link Cancelable}. <br>
 * <br>
 * This event has a result. {@link HasResult}. Set the result to Result.ALLOW to consider an artefact "active"<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author WinDanesz
 * @since Wizardry 4.3.10
 */
@Cancelable
@Event.HasResult
public class ArtefactCheckEvent extends PlayerEvent {

	ItemArtefact artefact;
	EntityPlayer player;

	public ArtefactCheckEvent(EntityPlayer player, ItemArtefact artefact) {
		super(player);
		this.player = player;
		this.artefact = artefact;
		setResult(Result.DENY);
	}

	public ItemArtefact getArtefact() {
		return artefact;
	}

	public EntityPlayer getPlayer() {
		return player;
	}

}