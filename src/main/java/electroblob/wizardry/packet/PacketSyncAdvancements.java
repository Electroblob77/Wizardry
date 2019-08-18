package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

/** <b>[Server -> Client]</b> This packet is fired on login and on advancement gain to update the handbook progress. */
public class PacketSyncAdvancements implements IMessageHandler<PacketSyncAdvancements.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleAdvancementSyncPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public boolean showToasts;
		public ResourceLocation[] completedAdvancements;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(boolean showToasts, ResourceLocation... completed){
			this.showToasts = showToasts;
			this.completedAdvancements = completed;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			showToasts = buf.readBoolean();
			ArrayList<ResourceLocation> advancements = new ArrayList<>();
			while(buf.isReadable()){
				advancements.add(new ResourceLocation(ByteBufUtils.readUTF8String(buf)));
			}
			this.completedAdvancements = advancements.toArray(new ResourceLocation[0]);
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeBoolean(showToasts);
			for(ResourceLocation advancement : completedAdvancements){
				ByteBufUtils.writeUTF8String(buf, advancement.toString());
			}
		}
	}
}
