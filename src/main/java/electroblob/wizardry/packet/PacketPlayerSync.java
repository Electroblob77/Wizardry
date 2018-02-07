package electroblob.wizardry.packet;

import java.util.HashSet;
import java.util.Set;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketPlayerSync.Message;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent to synchronise any fields that need synchronising in
 * {@link electroblob.wizardry.WizardData WizardData}. This packet is not sent often enough and is too small to warrant
 * having separate packets for each field that needs synchronising.
 */
public class PacketPlayerSync implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(new Runnable(){
				@Override
				public void run(){
					Wizardry.proxy.handlePlayerSyncPacket(message);
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		public Set<Spell> spellsDiscovered;
		public int selectedMinionID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Set<Spell> spellsDiscovered2, int selectedMinionID){
			this.spellsDiscovered = spellsDiscovered2;
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
