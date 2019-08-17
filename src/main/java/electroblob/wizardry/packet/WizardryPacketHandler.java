package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class WizardryPacketHandler {

	public static SimpleNetworkWrapper net;

	public static void initPackets(){
		net = NetworkRegistry.INSTANCE.newSimpleChannel(Wizardry.MODID.toUpperCase());
		registerMessage(PacketControlInput.class, PacketControlInput.Message.class);
		registerMessage(PacketCastSpell.class, PacketCastSpell.Message.class);
		registerMessage(PacketTransportation.class, PacketTransportation.Message.class);
		registerMessage(PacketPlayerSync.class, PacketPlayerSync.Message.class);
		registerMessage(PacketGlyphData.class, PacketGlyphData.Message.class);
		registerMessage(PacketCastContinuousSpell.class, PacketCastContinuousSpell.Message.class);
		registerMessage(PacketClairvoyance.class, PacketClairvoyance.Message.class);
		registerMessage(PacketSyncSettings.class, PacketSyncSettings.Message.class);
		registerMessage(PacketNPCCastSpell.class, PacketNPCCastSpell.Message.class);
		registerMessage(PacketDispenserCastSpell.class, PacketDispenserCastSpell.Message.class);
		registerMessage(PacketSpellProperties.class, PacketSpellProperties.Message.class);
		registerMessage(PacketSyncAdvancements.class, PacketSyncAdvancements.Message.class);
		registerMessage(PacketRequestAdvancementSync.class, PacketRequestAdvancementSync.Message.class);
		registerMessage(PacketEndSlowTime.class, PacketEndSlowTime.Message.class);
		registerMessage(PacketResurrection.class, PacketResurrection.Message.class);
		registerMessage(PacketCastSpellAtPos.class, PacketCastSpellAtPos.Message.class);
		registerMessage(PacketEmitterData.class, PacketEmitterData.Message.class);
		registerMessage(PacketPossession.class, PacketPossession.Message.class);
		registerMessage(PacketConquerShrine.class, PacketConquerShrine.Message.class);
	}

	private static int nextPacketId = 0;

	private static <REQ extends IMessage, REPLY extends IMessage> void registerMessage(
			Class<? extends IMessageHandler<REQ, REPLY>> packet, Class<REQ> message){
		net.registerMessage(packet, message, nextPacketId, Side.CLIENT);
		net.registerMessage(packet, message, nextPacketId, Side.SERVER);
		nextPacketId++;
	}
}