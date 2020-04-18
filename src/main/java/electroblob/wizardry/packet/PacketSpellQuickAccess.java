package electroblob.wizardry.packet;

import electroblob.wizardry.item.ISpellCastingItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** <b>[Client -> Server]</b> This packet is for the spell quick access keys. */
public class PacketSpellQuickAccess implements IMessageHandler<PacketSpellQuickAccess.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isServer()){

			final EntityPlayerMP player = ctx.getServerHandler().player;

			player.getServerWorld().addScheduledTask(() -> {

				ItemStack wand = player.getHeldItemMainhand();

				if(!(wand.getItem() instanceof ISpellCastingItem)){
					wand = player.getHeldItemOffhand();
				}

				if(wand.getItem() instanceof ISpellCastingItem){

					((ISpellCastingItem)wand.getItem()).selectSpell(wand, message.index);
					// This line fixes the bug with continuous spells casting when they shouldn't be
					player.stopActiveHand();
				}

			});
		}

		return null;
	}

	public static class Message implements IMessage {

		private int index;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(int index){
			this.index = index;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.index = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(index);
		}
	}
}
