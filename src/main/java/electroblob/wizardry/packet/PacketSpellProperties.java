package electroblob.wizardry.packet;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

/** <b>[Server -> Client]</b> This packet is sent to sync server-side spell properties with clients on login. */
public class PacketSpellProperties implements IMessageHandler<PacketSpellProperties.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isServer()){

			final EntityPlayerMP player = ctx.getServerHandler().player;

			player.getServerWorld().addScheduledTask(() -> {
				for(int i=0; i<message.propertiesArray.length; i++){
					Spell.byNetworkID(i).setProperties(message.propertiesArray[i]);
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		private SpellProperties[] propertiesArray;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(SpellProperties... properties){
			this.propertiesArray = properties;
		}

		@Override
		public void fromBytes(ByteBuf buf){

			List<SpellProperties> propertiesList = new ArrayList<>();
			int i = 0;

			while(buf.isReadable()){
				propertiesList.add(new SpellProperties(Spell.byNetworkID(i++), buf));
			}

			propertiesArray = propertiesList.toArray(new SpellProperties[0]);
		}

		@Override
		public void toBytes(ByteBuf buf){
			for(SpellProperties properties : propertiesArray) properties.write(buf);
		}
	}
}
