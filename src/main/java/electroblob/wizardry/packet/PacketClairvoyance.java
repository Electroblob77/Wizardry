package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketClairvoyance.Message;
import io.netty.buffer.ByteBuf;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>[Server -> Client]</b> This packet is sent when a player casts the clairvoyance spell to allow pathing to chunks
 * outside the render distance.
 */
public class PacketClairvoyance implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(new Runnable(){
				@Override
				public void run(){
					Wizardry.proxy.handleClairvoyancePacket(message);
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		public Path path;
		public float durationMultiplier;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Path path, float durationMultiplier){

			this.path = path;
			this.durationMultiplier = durationMultiplier;
		}

		@Override
		public void fromBytes(ByteBuf buf){

			// The order is important
			this.durationMultiplier = buf.readFloat();

			List<PathPoint> points = new ArrayList<PathPoint>();

			while(buf.isReadable()){
				points.add(new PathPoint(buf.readInt(), buf.readInt(), buf.readInt()));
			}

			this.path = new Path(points.toArray(new PathPoint[0]));
		}

		@Override
		public void toBytes(ByteBuf buf){

			buf.writeFloat(durationMultiplier);

			for(int i = 0; i < path.getCurrentPathLength(); i++){

				PathPoint point = path.getPathPointFromIndex(i);

				buf.writeInt(point.x);
				buf.writeInt(point.y);
				buf.writeInt(point.z);
			}
		}
	}
}
