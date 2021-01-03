package electroblob.wizardry.misc;

import com.google.common.collect.ImmutableMap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.packet.PacketRequestDonationPerks;
import electroblob.wizardry.packet.PacketSyncDonationPerks;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.util.Box;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Class responsible for storing and handling the donation perk settings for all eligible connected players */
// We probably could have used WizardData for this, but since this is config-based and only for rendering it makes more
// sense to handle it separately, especially given that the vast majority of players aren't donors
@Mod.EventBusSubscriber
public class DonationPerksHandler {

	// Thanks to everyone who has donated their time and/or money! <3 (These are your UUIDs - don't worry, they're not
	// sensitive info, they're just player identifiers that won't ever change, even if you change your username)
	// Convert between usernames and UUIDs here https://mcuuid.net/
	// Normally I'd use an EnumMap for this, but somehow making the UUIDs into an enum doesn't seem right
	private static final Map<UUID, Box<Element>> DONOR_UUID_MAP = ImmutableMap.<UUID, Box<Element>>builder()
			.put(UUID.fromString("4b29263e-007b-48ef-b3e6-ce86cca989e9"), new Box<>(Element.MAGIC)) // Me!
			.put(UUID.fromString("2b583703-1407-4ec6-93d2-d9c03b0c08fc"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("f6c7001f-4ce8-43f7-b4a1-a9d9d5fadb93"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("01535a73-ff8d-4d6c-851e-c71f89e936aa"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("32ca0099-c1d6-4682-82ab-8f06059bb801"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("0a704e20-1e7a-413e-9122-fae5b244a05e"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("41fec037-12a5-4b19-868e-b62e39952e96"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("3f5e0cec-949a-4dd4-ae60-f4eae481dd06"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("3ad78c5f-d8cf-47f1-bff9-a890f8e638ff"), new Box<>(Element.MAGIC))
			.put(UUID.fromString("93479771-d23a-4d80-8868-2950e144500b"), new Box<>(Element.MAGIC))
	.build();

	/** Returns true if the given player's UUID appears in the list of donor UUIDs, false otherwise (thread-safe). */
	public static boolean isDonor(EntityPlayer player){
		return DONOR_UUID_MAP.containsKey(player.getUniqueID());
	}

	/** Sets each donor's perk element in order according to the given list. The given list <i>will</i> be modified. */
	public static void setElements(List<Element> elements){
		if(elements.size() < DONOR_UUID_MAP.size()){
			Wizardry.logger.warn("Received fewer perk settings than the number of donors, cannot sync!");
			return;
		}
		DONOR_UUID_MAP.forEach((k, v) -> v.set(elements.remove(0)));
		if(!elements.isEmpty()) Wizardry.logger.warn("Received more perk settings than the number of donors, something must have gone wrong!");
	}

	/** Sets the donor perk to the given element for the given player and sends a packet to all clients if it changed. */
	public static void setElement(EntityPlayerMP player, Element element){
		Box<Element> box = DONOR_UUID_MAP.get(player.getUniqueID());
		if(box != null && box.get() != element){
			box.set(element);
			syncAll(player.dimension);
		}
	}

	/** Returns the given player's donor perk element, or null if they are not a donor (may also be null for donors if
	 * they have disabled the perk). */
	public static Element getElement(EntityPlayer player){
		Box<Element> box = DONOR_UUID_MAP.get(player.getUniqueID());
		return box == null ? null : box.get();
	}

	/** Sends the current donor perk information to all players in the given dimension (server-side only). */
	private static void syncAll(int dimension){
		Wizardry.logger.info("Sending global donation perk settings to all players");
		PacketSyncDonationPerks.Message packet = new PacketSyncDonationPerks.Message(DONOR_UUID_MAP.values().stream().map(Box::get).collect(Collectors.toList()));
		WizardryPacketHandler.net.sendToDimension(packet, dimension);
	}

	/** Sends the current donor perk information to the given player (server-side only). */
	private static void syncWith(EntityPlayerMP player){
		Wizardry.logger.info("Sending global donation perk settings to {}", player.getName());
		PacketSyncDonationPerks.Message packet = new PacketSyncDonationPerks.Message(DONOR_UUID_MAP.values().stream().map(Box::get).collect(Collectors.toList()));
		WizardryPacketHandler.net.sendTo(packet, player);
	}

	/** Sends the client player's donor perk setting to the server (client-side only) */
	public static void sendToServer(EntityPlayer player){
		Wizardry.logger.info("Sending donation perk settings for {} to server", player.getName());
		IMessage message = new PacketRequestDonationPerks.Message(Wizardry.settings.donationPerkElement);
		WizardryPacketHandler.net.sendToServer(message);
	}

	@SubscribeEvent
	public static void onEntityJoinWorldEvent(EntityJoinWorldEvent event){
		if(event.getEntity() instanceof EntityPlayerMP){
			// Send donation perks to anyone who logs in
			syncWith((EntityPlayerMP)event.getEntity());
		}else if(event.getEntity() instanceof EntityPlayer){
			// Send donation perks setting to the server if the player is a donor
			if(isDonor((EntityPlayer)event.getEntity())) sendToServer((EntityPlayer)event.getEntity());
		}
	}

}
