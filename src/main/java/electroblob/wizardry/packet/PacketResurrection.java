package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent to clients in the same dimension when a player is resurrected.
 */
public class PacketResurrection implements IMessageHandler<PacketResurrection.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleResurrectionPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public int playerID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(int playerID){
			this.playerID = playerID;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			this.playerID = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(playerID);
		}
	}
}
