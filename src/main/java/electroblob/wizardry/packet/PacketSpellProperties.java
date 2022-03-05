package electroblob.wizardry.packet;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import io.netty.buffer.ByteBuf;
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
		if(ctx.side.isClient()){

			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {

				int spellId = message.firstId;

				for(int i = 0; i <message.propertiesArray.length; i++){
					Spell spell = Spell.byNetworkID(spellId);
					SpellProperties props = message.propertiesArray[i];
					spell.setPropertiesClient(props);
					spellId++;
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		private SpellProperties[] propertiesArray;
		private int firstId;
		private int count;
		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(int firstId, int count, SpellProperties... properties){
			this.firstId = firstId;
			this.count = count;
			this.propertiesArray = properties;

		}

		@Override
		public void fromBytes(ByteBuf buf){
			List<SpellProperties> propertiesList = new ArrayList<>();
			firstId = buf.readInt();
			count = buf.readInt();

			int k = 0;
			int j = firstId;
			while(k < count){
				SpellProperties props = new SpellProperties(Spell.byNetworkID(j), buf);
				propertiesList.add(props);
				k++;
				j++;
			}

			propertiesArray = propertiesList.toArray(new SpellProperties[0]);
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(firstId);
			buf.writeInt(count);
			for(SpellProperties properties : propertiesArray) properties.write(buf);
		}
	}
}
