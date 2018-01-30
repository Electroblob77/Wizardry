package electroblob.wizardry.event;

import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;

/**
 * SpellBindEvent is fired when a player presses the apply button in the arcane workbench. <i>Note that this
 * event is only fired on the server side.</i><br>
 * <br>
 * This event is {@link Cancelable}. If this event is canceled, no further processing takes place: spells are not bound,
 * upgrades are not applied and crystals are not consumed.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Cancelable
public class SpellBindEvent extends PlayerContainerEvent {

	public SpellBindEvent(EntityPlayer player, ContainerArcaneWorkbench container) {
		super(player, container);
	}

}