package electroblob.wizardry.packet;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.misc.DonationPerksHandler;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>[Server -> Client]</b> This packet is sent to update the donation perks map for each player that logs in, and for
 * all players whenever the map changes.
 */
public class PacketSyncDonationPerks implements IMessageHandler<PacketSyncDonationPerks.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> DonationPerksHandler.setElements(message.elements));
		}

		return null;
	}

	public static class Message implements IMessage {

		public List<Element> elements;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(List<Element> elements){
			this.elements = elements;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.elements = new ArrayList<>();
			while(buf.isReadable()){
				int i = buf.readShort();
				elements.add(i == -1 ? null : Element.values()[buf.readShort()]);
			}
		}

		@Override
		public void toBytes(ByteBuf buf){
			for(Element element : elements) buf.writeShort(element == null ? -1 : element.ordinal());
		}
	}
}
