package electroblob.wizardry.packet;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import electroblob.wizardry.Wizardry;

public class WizardryPacketHandler {
	
	public static SimpleNetworkWrapper net;
  
	public static void initPackets()
	{
		net = NetworkRegistry.INSTANCE.newSimpleChannel(Wizardry.MODID.toUpperCase());
		registerMessage(PacketControlInput.class, PacketControlInput.Message.class);
		//registerMessage(PacketParticleSpawn.class, PacketParticleSpawn.Message.class);
		registerMessage(PacketCastSpell.class, PacketCastSpell.Message.class);
		registerMessage(PacketTransportation.class, PacketTransportation.Message.class);
		registerMessage(PacketPlayerSync.class, PacketPlayerSync.Message.class);
		registerMessage(PacketGlyphData.class, PacketGlyphData.Message.class);
		registerMessage(PacketCastContinuousSpell.class, PacketCastContinuousSpell.Message.class);
		registerMessage(PacketClairvoyance.class, PacketClairvoyance.Message.class);
	}
	
	private static int nextPacketId = 0;
	
	private static void registerMessage(Class packet, Class message)
	{
		net.registerMessage(packet, message, nextPacketId, Side.CLIENT);
		net.registerMessage(packet, message, nextPacketId, Side.SERVER);
		nextPacketId++;
	}
}