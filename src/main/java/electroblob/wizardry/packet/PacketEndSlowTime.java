package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** <b>[Server -> Client]</b> This packet is sent when the slow time potion effect expires or is removed from an
 * entity to unblock all nearby entities' updates. */
public class PacketEndSlowTime implements IMessageHandler<PacketEndSlowTime.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleEndSlowTimePacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public int hostID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Entity host){
			this.hostID = host.getEntityId();
		}

		@Override
		public void fromBytes(ByteBuf buf){
			this.hostID = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(hostID);
		}
	}
}
