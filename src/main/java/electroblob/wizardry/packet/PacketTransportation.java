package electroblob.wizardry.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketTransportation.Message;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/** This packet is sent when a player is teleported due to the transportation spell to spawn the particles. */
public class PacketTransportation implements IMessageHandler<Message, IMessage> {
	
	@Override
	public IMessage onMessage(Message message, MessageContext ctx)
	{
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			Wizardry.proxy.handleTransportationPacket(message);
		}

		return null;
	}

	public static class Message implements IMessage
	{
		/** EntityID of the caster */
		public int casterID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message() {}

		public Message(int casterID)
		{
			this.casterID = casterID;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			// The order is important
			this.casterID = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(casterID);
		}
	}
}
