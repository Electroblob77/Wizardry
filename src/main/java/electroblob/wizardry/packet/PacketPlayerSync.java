package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.IVariable;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.packet.PacketPlayerSync.Message;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <b>[Server -> Client]</b> This packet is sent to synchronise any fields that need synchronising in
 * {@link WizardData WizardData}. This packet is not sent often enough and is too small to warrant
 * having separate packets for each field that needs synchronising.
 */
public class PacketPlayerSync implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handlePlayerSyncPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		public long seed;
		public Set<Spell> spellsDiscovered;
		public int selectedMinionID;
		public Map<IVariable, Object> spellData;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(long seed, Set<Spell> spellsDiscovered, int selectedMinionID, Map<IVariable, Object> spellData){
			this.seed = seed;
			this.spellsDiscovered = spellsDiscovered;
			this.selectedMinionID = selectedMinionID;
			this.spellData = spellData;
		}

		@Override
		public void fromBytes(ByteBuf buf){

			this.seed = buf.readLong();
			this.selectedMinionID = buf.readInt();
			this.spellData = new HashMap<>();
			WizardData.getSyncedVariablesOrderedByKey().forEach(v -> spellData.put(v, v.read(buf)));
			// Have to send empty tags to guarantee correct ByteBuf size/order, but no point keeping the resulting nulls
			spellData.values().removeIf(Objects::isNull);

			this.spellsDiscovered = new HashSet<>();
			while(buf.isReadable()){
				this.spellsDiscovered.add(Spell.byNetworkID(buf.readInt()));
			}
		}

		@Override
		@SuppressWarnings("unchecked") // We know it's ok
		public void toBytes(ByteBuf buf){

			buf.writeLong(seed);
			buf.writeInt(selectedMinionID);

			WizardData.getSyncedVariablesOrderedByKey().forEach(v -> v.write(buf, spellData.get(v)));

			if(this.spellsDiscovered == null) return;
			for(Spell spell : this.spellsDiscovered){
				buf.writeInt(spell.networkID());
			}
		}
	}
}
