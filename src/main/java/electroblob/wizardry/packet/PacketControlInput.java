package electroblob.wizardry.packet;

import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketControlInput.Message;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.util.WandHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** <b>[Client -> Server]</b> This packet is for control events such as buttons in GUIs and key presses. */
public class PacketControlInput implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isServer()){

			final EntityPlayerMP player = ctx.getServerHandler().playerEntity;

			player.getServerWorld().addScheduledTask(new Runnable(){

				public void run() {

					ItemStack wand = player.getHeldItemMainhand();

					if(wand == null || !(wand.getItem() instanceof ItemWand)){
						wand = player.getHeldItemOffhand();
					}

					switch(message.controlType){

					case APPLY_BUTTON:

						((ContainerArcaneWorkbench)player.openContainer).onApplyButtonPressed(player);
						break;

					case NEXT_SPELL_KEY:

						if(wand != null && wand.getItem() instanceof ItemWand){

							WandHelper.selectNextSpell(wand);
							// This line fixes the bug with continuous spells casting when they shouldn't be
							player.stopActiveHand();
						}

						break;

					case PREVIOUS_SPELL_KEY:

						if(wand != null && wand.getItem() instanceof ItemWand){

							WandHelper.selectPreviousSpell(wand);
							// This line fixes the bug with continuous spells casting when they shouldn't be
							player.stopActiveHand();
						}

						break;
					}
				}
			}
					);
		}

		return null;
	}

	public static enum ControlType {
		APPLY_BUTTON,
		NEXT_SPELL_KEY,
		PREVIOUS_SPELL_KEY;
	}

	public static class Message implements IMessage {

		private ControlType controlType;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message() {}

		public Message(ControlType type){
			this.controlType = type;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.controlType = ControlType.values()[buf.readInt()];
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(controlType.ordinal());
		}
	}
}
