package electroblob.wizardry.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;

/** <b>[Client -> Server]</b> Fired on resource reload to request that the server re-sync the player's advancements. */
public class PacketRequestAdvancementSync implements IMessageHandler<PacketRequestAdvancementSync.Message, PacketSyncAdvancements.Message> {

	@Override
	public PacketSyncAdvancements.Message onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isServer()){

			final EntityPlayerMP player = ctx.getServerHandler().player;

			ArrayList<ResourceLocation> advancements = new ArrayList<>();

			for(Advancement advancement : player.getServer().getAdvancementManager().getAdvancements()){
				if(player.getAdvancements().getProgress(advancement).isDone()) advancements.add(advancement.getId());
			}

			return new PacketSyncAdvancements.Message(false, advancements.toArray(new ResourceLocation[0]));
		}

		return null;
	}


	public static class Message implements IMessage {

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		// Don't need to put anything in here!

		@Override
		public void fromBytes(ByteBuf buf){}

		@Override
		public void toBytes(ByteBuf buf){}
	}
}
