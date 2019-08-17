package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when a spell is cast at a position by commands and returns true, and is
 * sent to clients so they can spawn the particles themselves.
 */
// Soooo many spell casting packets...
public class PacketCastSpellAtPos implements IMessageHandler<PacketCastSpellAtPos.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleCastSpellAtPosPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		/** Position for the spell */
		public Vec3d position;
		/** Direction for the spell */
		public EnumFacing direction;
		/** ID of the spell being cast */
		public int spellID;
		/** SpellModifiers for the spell */
		public SpellModifiers modifiers;
		/** Number of ticks to cast the spell for, or -1 for non-continuous spells */
		public int duration;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(Vec3d position, EnumFacing direction, Spell spell, SpellModifiers modifiers){
			this(position, direction, spell, modifiers, -1);
		}

		public Message(Vec3d position, EnumFacing direction, Spell spell, SpellModifiers modifiers, int duration){
			this.spellID = spell.networkID();
			this.modifiers = modifiers;
			this.position = position;
			this.direction = direction;
			this.duration = duration;
		}

		@Override
		public void fromBytes(ByteBuf buf){

			// The order is important
			position = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			direction = EnumFacing.byIndex(buf.readInt());
			this.spellID = buf.readInt();
			this.modifiers = new SpellModifiers();
			this.modifiers.read(buf);
			this.duration = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf){

			buf.writeDouble(position.x);
			buf.writeDouble(position.y);
			buf.writeDouble(position.z);
			buf.writeInt(direction.getIndex());
			buf.writeInt(spellID);
			this.modifiers.write(buf);
			buf.writeInt(duration);
		}
	}
}
