package electroblob.wizardry.event;

import electroblob.wizardry.item.ItemArtefact;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import java.util.List;

/**
 * ArtefactCheckEvent is fired when a check happens for an ItemArtefact using {@link ItemArtefact#isArtefactActive(EntityPlayer, Item)}
 * <i>Fired on both sides.</i><br>
 *  <br>
 *  This event is {@link Cancelable}. <br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author ipdnaeip, adapted from WinDanesz
 * @since Wizardry 4.3.10
 */
@Cancelable
public class ArtefactCheckEvent extends PlayerEvent {

	EntityPlayer player;
	List<ItemArtefact.TrackedArtefact> artefacts;

	public ArtefactCheckEvent(EntityPlayer player, List<ItemArtefact.TrackedArtefact> artefacts) {
		super(player);
		this.player = player;
		this.artefacts = artefacts;
	}

	public EntityPlayer getPlayer() {
		return this.player;
	}

	//Returns a modifiable list of active artefacts
	public List<ItemArtefact.TrackedArtefact> getArtefacts() {
		return this.artefacts;
	}

	//Adds an artefact maintaining the inventory and slot to allow for modifying of the ItemStack
	public boolean addArtefact(ItemArtefact.TrackedArtefact slotItemStack) {
		return artefacts.add(slotItemStack);
	}

	//Adds an unmodifiable artefact accounting for ItemStack properties
	public boolean addArtefact(ItemStack itemStack) {
		return this.addArtefact(new ItemArtefact.TrackedArtefact(itemStack));
	}

	//Adds ann unmodifiable artefact item
	public boolean addArtefact(Item itemArtefact) {
		return this.addArtefact(new ItemStack(itemArtefact));
	}

	//Removes all artefacts that match the SlotItemStack, could have a use if the player wants to deny artefacts from a certain inventory
	public boolean removeArtefact(ItemArtefact.TrackedArtefact slotItemStack) {
		return this.artefacts.removeIf(artefact -> artefact == slotItemStack);
	}

	//Removes all artefacts that match the ItemStack
	public boolean removeArtefact(ItemStack itemStack) {
		return this.artefacts.removeIf(artefact -> ItemStack.areItemStacksEqual(artefact.getItemStack(), itemStack));
	}

	//Removes all artefacts that match the Item
	public boolean removeArtefact(Item itemArtefact) {
		return this.artefacts.removeIf(artefact -> artefact.getItemStack().getItem() == itemArtefact);
	}

}
