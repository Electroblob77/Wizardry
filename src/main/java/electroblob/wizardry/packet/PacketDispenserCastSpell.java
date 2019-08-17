package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketDispenserCastSpell.Message;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent when a spell is cast by a dispenser and returns true, and is sent to
 * clients so they can spawn the particles. Unlike the player packets, this is for both continuous <b>and</b>
 * non-continuous spells.
 */
public class PacketDispenserCastSpell implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// methods any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> Wizardry.proxy.handleDispenserCastSpellPacket(message));
		}

		return null;
	}

	public static class Message implements IMessage {

		/** ID of the spell being cast */
		public int spellID;
		/** Coordinates of the spell origin */
		public double x, y, z;
		/** Spell casting direction */
		public EnumFacing direction;
		/** BlockPos of the block that cast this spell. <i>Not necessarily the same as the (x, y, z) coordinates.</i> */
		public BlockPos pos;
		/** The number of ticks to cast the spell for, or -1 if the spell should be cast until stopped. */
		public int duration;
		/** SpellModifiers for the spell */
		public SpellModifiers modifiers;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(double x, double y, double z, EnumFacing direction, BlockPos pos, Spell spell, int duration, SpellModifiers modifiers){

			this.x = x;
			this.y = y;
			this.z = z;
			this.direction = direction;
			this.pos = pos;
			this.spellID = spell.networkID();
			this.duration = duration;
			this.modifiers = modifiers;
			
		}

		@Override
		public void fromBytes(ByteBuf buf){

			// The order is important
			this.x = buf.readDouble();
			this.y = buf.readDouble();
			this.z = buf.readDouble();
			this.direction = EnumFacing.values()[buf.readInt()];
			this.pos = BlockPos.fromLong(buf.readLong());
			this.spellID = buf.readInt();
			this.duration = buf.readInt();
			this.modifiers = new SpellModifiers();
			this.modifiers.read(buf);
		}

		@Override 
		public void toBytes(ByteBuf buf){

			buf.writeDouble(x);
			buf.writeDouble(y);
			buf.writeDouble(z);
			buf.writeInt(direction.ordinal());
			buf.writeLong(pos.toLong());
			buf.writeInt(spellID);
			buf.writeInt(duration);
			this.modifiers.write(buf);
		}
	}
}
