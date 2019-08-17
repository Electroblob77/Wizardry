package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.command.SpellEmitter;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>[Server -> Client]</b> This packet is sent each time a player joins a world to synchronise the client-side spell
 * emitters with the server-side ones.
 */
public class PacketEmitterData implements IMessageHandler<PacketEmitterData.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleEmitterDataPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public List<SpellEmitter> emitters;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(List<SpellEmitter> emitters){
			this.emitters = emitters;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			emitters = new ArrayList<>();
			while(buf.isReadable()){
				emitters.add(SpellEmitter.read(buf));
			}
		}

		@Override
		public void toBytes(ByteBuf buf){
			emitters.forEach(s -> s.write(buf));
		}
	}
}
