package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketCastContinuousSpell.Message;
import electroblob.wizardry.util.SpellModifiers;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when the /cast command is used with a continuous spell, in order to
 * sync the relevant variables in {@link electroblob.wizardry.WizardData WizardData}.
 */
public class PacketCastContinuousSpell implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(new Runnable(){
				@Override
				public void run(){
					Wizardry.proxy.handleCastContinuousSpellPacket(message);
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		// Note that range and blast multipliers are the only two that affect particle spawning, so they are the
		// only two that need to be sent.

		/** EntityID of the caster */
		public int casterID;
		/** ID of the spell being cast */
		public int spellID;
		/** SpellModifiers for the spell */
		public SpellModifiers modifiers;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(int casterID, int spellID, SpellModifiers modifiers){
			this.casterID = casterID;
			this.spellID = spellID;
			this.modifiers = modifiers;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.casterID = buf.readInt();
			this.spellID = buf.readInt();
			this.modifiers = new SpellModifiers();
			modifiers.read(buf);
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(casterID);
			buf.writeInt(spellID);
			this.modifiers.write(buf);
		}
	}
}
