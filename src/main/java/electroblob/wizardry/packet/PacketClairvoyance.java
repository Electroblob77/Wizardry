package electroblob.wizardry.packet;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketClairvoyance.Message;
import io.netty.buffer.ByteBuf;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;

/** This packet is sent when a player casts the clairvoyance spell to allow pathing to chunks outside the render
 * distance. */
public class PacketClairvoyance implements IMessageHandler<Message, IMessage> {
	
	@Override
	public IMessage onMessage(Message message, MessageContext ctx)
	{
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			Wizardry.proxy.handleClairvoyancePacket(message);
		}

		return null;
	}

	public static class Message implements IMessage {

		public PathEntity path;
		public float durationMultiplier;
		
		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message() {}

		public Message(PathEntity path, float durationMultiplier){
			
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
			
			this.path = new PathEntity(points.toArray(new PathPoint[points.size()]));
		}

		@Override
		public void toBytes(ByteBuf buf){
			
			buf.writeFloat(durationMultiplier);
			
			for(int i=0; i<path.getCurrentPathLength(); i++){
				
				PathPoint point = path.getPathPointFromIndex(i);
				
				buf.writeInt(point.xCoord);
				buf.writeInt(point.yCoord);
				buf.writeInt(point.zCoord);
			}
		}
	}
}
