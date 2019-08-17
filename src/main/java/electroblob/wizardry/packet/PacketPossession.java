package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;

/** <b>[Server -> Client]</b> This packet is sent when a player possesses an entity or when a player stops possessing
 * to update all clients. */
public class PacketPossession implements IMessageHandler<PacketPossession.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handlePossessionPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public int playerID;
		public int targetID;
		public int duration;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(EntityPlayer host, @Nullable EntityLiving target, int duration){
			this.playerID = host.getEntityId();
			this.targetID = target == null ? -1 : target.getEntityId();
			this.duration = duration;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			this.playerID = buf.readInt();
			this.targetID = buf.readInt();
			this.duration = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(playerID);
			buf.writeInt(targetID);
			buf.writeInt(duration);
		}
	}
}
