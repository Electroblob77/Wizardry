package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when a bomb entity impacts something. It is used to guarantee that the
 * client always sees the explosion effect, even in singleplayer where the server may remove the entity before the
 * client's own simulation can call {@code onImpact()}.
 */
public class PacketBombExplosion implements IMessageHandler<PacketBombExplosion.Message, IMessage> {

	/** Bomb type constants, matching the order bombs are handled in {@link electroblob.wizardry.CommonProxy}. */
	public static final int FIREBOMB    = 0;
	public static final int POISON_BOMB = 1;
	public static final int SMOKE_BOMB  = 2;
	public static final int SPARK_BOMB  = 3;

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		if(ctx.side.isClient()){
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(
					() -> Wizardry.proxy.handleBombExplosionPacket(message));
		}
		return null;
	}

	public static class Message implements IMessage {

		/** One of the bomb type constants defined in {@link PacketBombExplosion}. */
		public int bombType;
		public double x, y, z;
		public float blastMultiplier;
		/** Entity IDs of secondary targets struck by the spark bomb chain. Empty for other bomb types. */
		public int[] secondaryTargetIDs;

		// Required no-arg constructor
		public Message(){}

		public Message(int bombType, double x, double y, double z, float blastMultiplier){
			this(bombType, x, y, z, blastMultiplier, new int[0]);
		}

		public Message(int bombType, double x, double y, double z, float blastMultiplier, int[] secondaryTargetIDs){
			this.bombType = bombType;
			this.x = x;
			this.y = y;
			this.z = z;
			this.blastMultiplier = blastMultiplier;
			this.secondaryTargetIDs = secondaryTargetIDs;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			bombType = buf.readInt();
			x = buf.readDouble();
			y = buf.readDouble();
			z = buf.readDouble();
			blastMultiplier = buf.readFloat();
			int count = buf.readInt();
			secondaryTargetIDs = new int[count];
			for(int i = 0; i < count; i++) secondaryTargetIDs[i] = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(bombType);
			buf.writeDouble(x);
			buf.writeDouble(y);
			buf.writeDouble(z);
			buf.writeFloat(blastMultiplier);
			buf.writeInt(secondaryTargetIDs.length);
			for(int id : secondaryTargetIDs) buf.writeInt(id);
		}
	}
}
