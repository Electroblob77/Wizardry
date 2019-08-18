package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when a shrine is conquered to update nearby clients and spawn particles.
 */
public class PacketConquerShrine implements IMessageHandler<PacketConquerShrine.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleConquerShrinePacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public int x;
		public int y;
		public int z;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(BlockPos pos){
			this.x = pos.getX();
			this.y = pos.getY();
			this.z = pos.getZ();
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.x = buf.readInt();
			this.y = buf.readInt();
			this.z = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
		}
	}
}
