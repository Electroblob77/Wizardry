package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.misc.DonationPerksHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** <b>[Client -> Server]</b> This packet is sent when a player logs in and whenever the setting changes to set their
 * donation perk element. */
public class PacketRequestDonationPerks implements IMessageHandler<PacketRequestDonationPerks.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isServer()){

			final EntityPlayerMP player = ctx.getServerHandler().player;

			// The UUID key set itself is immutable so we can safely access it from the networking thread
			if(DonationPerksHandler.isDonor(player)){
				player.getServerWorld().addScheduledTask(() -> DonationPerksHandler.setElement(player, message.element));
			}else{
				Wizardry.logger.warn("Received a donation perk packet from a player that isn't a donor!");
			}

		}

		return null;
	}

	public static class Message implements IMessage {

		private Element element;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(Element element){
			this.element = element;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			element = Element.values()[buf.readShort()];
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeShort(element.ordinal());
		}
	}
}
