package electroblob.wizardry.packet;

import java.util.HashSet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketPlayerSync.Message;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;

/** This packet is sent to synchronise extended player data. */
public class PacketPlayerSync implements IMessageHandler<Message, IMessage> {
	
	@Override
	public IMessage onMessage(Message message, MessageContext ctx)
	{
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			Wizardry.proxy.handlePlayerSyncPacket(message);
		}

		return null;
	}

	public static class Message implements IMessage {
		
		public HashSet<Spell> spellsDiscovered;
		public int selectedMinionID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message() {}

		public Message(HashSet<Spell> spellsDiscovered, int selectedMinionID){
			this.spellsDiscovered = spellsDiscovered;
			this.selectedMinionID = selectedMinionID;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			this.selectedMinionID = buf.readInt();
			this.spellsDiscovered = new HashSet<Spell>();
			while(buf.isReadable()){
				this.spellsDiscovered.add(Spell.get(buf.readInt()));
			}
		}

		@Override
		public void toBytes(ByteBuf buf){
			
			buf.writeInt(selectedMinionID);
			
			if(this.spellsDiscovered == null) return;
			
			for(Spell spell : this.spellsDiscovered){
				buf.writeInt(spell.id());
			}
		}
	}
}
