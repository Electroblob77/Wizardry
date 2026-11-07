package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellProperties;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** <b>[Server -> Client]</b> This packet is sent to sync server-side spell properties with clients on login. */
public class PacketSpellProperties implements IMessageHandler<PacketSpellProperties.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){

			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {

				Set<Spell> synced = new HashSet<>();

				for(Message.Entry entry : message.entries){
					entry.spell.setPropertiesClient(entry.properties);
					synced.add(entry.spell);
				}

				// The server sent properties for a spell this client has never heard of - almost always a sign the
				// client and server have different mods/spell packs (or different versions of the same one)
				if(!message.unrecognisedSpells.isEmpty()){
					Wizardry.logger.error("The server sent spell properties for the following spell(s), which this" +
							" client does not have registered: " + message.unrecognisedSpells + " - this client" +
							" and the server most likely have different mods/spell packs installed, or different" +
							" versions of the same one. Those spells will not work correctly here!");
				}

				// The reverse case - this client knows about a spell the server never even mentioned
				List<String> missing = Spell.getAllSpells().stream().filter(s -> !synced.contains(s))
						.map(s -> s.getRegistryName().toString()).sorted().collect(Collectors.toList());

				if(!missing.isEmpty()){
					Wizardry.logger.error("This client has the following spell(s) registered, but the server never" +
							" sent properties for them: " + missing + " - this client and the server most likely" +
							" have different mods/spell packs installed, or different versions of the same one." +
							" Those spells will not work correctly here!");
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		private List<Entry> entries = new ArrayList<>();
		private List<String> unrecognisedSpells = new ArrayList<>();

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(Spell[] spells, SpellProperties[] properties){
			for(int i = 0; i < spells.length; i++) entries.add(new Entry(spells[i], properties[i]));
		}

		// Each entry is identified by its spell's registry name (a stable string) rather than by array position/
		// networkID, and is length-prefixed so it can always be skipped in full even if the spell or its shape isn't
		// recognised on the receiving end. This is deliberate: networkIDs are only guaranteed consistent within a
		// single side's own registry, NOT between client and server (spell packs can easily end up registering
		// spells in different orders, or being missing/mismatched entirely on one side), and previously a single
		// such mismatch would silently desync every spell property read after it, corrupting the whole packet.
		@Override
		public void fromBytes(ByteBuf buf){

			int count = buf.readInt();
			entries = new ArrayList<>(count);
			unrecognisedSpells = new ArrayList<>();

			for(int i = 0; i < count; i++){

				String name = ByteBufUtils.readUTF8String(buf);
				int length = buf.readInt();
				int contentStart = buf.readerIndex();

				Spell spell = Spell.registry.getValue(new ResourceLocation(name));

				if(spell == null){
					unrecognisedSpells.add(name);
				}else{

					SpellProperties properties = new SpellProperties(spell, buf);
					entries.add(new Entry(spell, properties));

					int consumed = buf.readerIndex() - contentStart;
					if(consumed != length){
						Wizardry.logger.error("Spell properties for " + name + " were sent as " + length + " bytes" +
								" but " + consumed + " were read while decoding them - the client and server" +
								" versions of this spell disagree on its properties (mismatched mod version?)." +
								" Using what was read; this spell may not work correctly here.");
					}
				}

				// Always reset to the recorded boundary, regardless of what happened above - this is what stops a
				// single unrecognised or mismatched spell from desyncing every spell that comes after it
				buf.readerIndex(contentStart + length);
			}
		}

		@Override
		public void toBytes(ByteBuf buf){

			buf.writeInt(entries.size());

			for(Entry entry : entries){

				ByteBufUtils.writeUTF8String(buf, entry.spell.getRegistryName().toString());

				int lengthIndex = buf.writerIndex();
				buf.writeInt(0); // Placeholder, patched below once we know how big this entry actually is

				int contentStart = buf.writerIndex();
				entry.properties.write(buf);
				buf.setInt(lengthIndex, buf.writerIndex() - contentStart);
			}
		}

		private static class Entry {

			private final Spell spell;
			private final SpellProperties properties;

			private Entry(Spell spell, SpellProperties properties){
				this.spell = spell;
				this.properties = properties;
			}
		}
	}
}
