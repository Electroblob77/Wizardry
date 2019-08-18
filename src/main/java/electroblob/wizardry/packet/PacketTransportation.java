package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketTransportation.Message;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when a player is teleported due to the transportation spell to spawn
 * the particles.
 */
public class PacketTransportation implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleTransportationPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {
		/** EntityID of the caster */
		public int casterID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(int casterID){
			this.casterID = casterID;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.casterID = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(casterID);
		}
	}
}
