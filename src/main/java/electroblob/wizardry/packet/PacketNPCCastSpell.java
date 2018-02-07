package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.packet.PacketNPCCastSpell.Message;
import electroblob.wizardry.util.SpellModifiers;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when an {@link ISpellCaster} casts a spell which returns true, and is
 * sent to clients so they can spawn the particles. Unlike the player packets, this is for both continuous <b>and</b>
 * non-continuous spells.
 */
public class PacketNPCCastSpell implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(new Runnable(){
				@Override
				public void run(){
					Wizardry.proxy.handleNPCCastSpellPacket(message);
				}
			});
		}

		return null;
	}

	public static class Message implements IMessage {

		/** EntityID of the caster */
		public int casterID;
		/** EntityID of the target, or -1 if there is none */
		public int targetID;
		/** ID of the spell being cast */
		public int spellID;
		/** SpellModifiers for the spell */
		public SpellModifiers modifiers;
		/** The hand that is holding the itemstack used to cast the spell. Defaults to MAIN_HAND. */
		public EnumHand hand;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(int casterID, int targetID, EnumHand hand, int spellID, SpellModifiers modifiers){
			this.casterID = casterID;
			this.targetID = targetID;
			this.spellID = spellID;
			this.modifiers = modifiers;
			this.hand = hand == null ? EnumHand.MAIN_HAND : hand;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.casterID = buf.readInt();
			this.targetID = buf.readInt();
			this.spellID = buf.readInt();
			this.modifiers = new SpellModifiers();
			this.modifiers.read(buf);
			this.hand = buf.readBoolean() ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(casterID);
			buf.writeInt(targetID);
			buf.writeInt(spellID);
			this.modifiers.write(buf);
			buf.writeBoolean(this.hand == EnumHand.OFF_HAND);
		}
	}
}
