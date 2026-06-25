package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * <b>[Server -> Client]</b> This packet is sent when a container is locked or unlocked by Arcane Lock to update clients.
 */
public class PacketSyncArcaneLock implements IMessageHandler<PacketSyncArcaneLock.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		if(ctx.side.isClient()){
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleArcaneLockSyncPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public BlockPos pos;
		public boolean locked;
		public UUID owner;

		public Message(){}

		public Message(BlockPos pos, boolean locked, @Nullable UUID owner){
			this.pos = pos;
			this.locked = locked;
			this.owner = owner != null ? owner : new UUID(0, 0);
		}

		@Override
		public void fromBytes(ByteBuf buf){
			this.pos = BlockPos.fromLong(buf.readLong());
			this.locked = buf.readBoolean();
			this.owner = new UUID(buf.readLong(), buf.readLong());
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeLong(pos.toLong());
			buf.writeBoolean(locked);
			buf.writeLong(owner.getMostSignificantBits());
			buf.writeLong(owner.getLeastSignificantBits());
		}
	}
}
