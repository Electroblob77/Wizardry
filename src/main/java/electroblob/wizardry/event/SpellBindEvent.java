package electroblob.wizardry.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * [NYI] SpellBindEvent is fired when a player presses the apply button in the arcane workbench.<br>
 * <br>
 * This event is {@link Cancelable}.<br>
 * If this event is canceled, no further processing takes place: spells are not bound, upgrades are not applied and
 * crystals are not consumed.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
public class SpellBindEvent extends PlayerEvent {

	public SpellBindEvent(EntityPlayer player) {
		super(player);
	}

}
