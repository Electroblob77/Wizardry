package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityLectern;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** <b>[Client -> Server]</b> This packet is sent when a player closes the lectern GUI to send the last-viewed spell to
 * the server. */
public class PacketLectern implements IMessageHandler<PacketLectern.Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isServer()){

			final EntityPlayerMP player = ctx.getServerHandler().player;

			player.getServerWorld().addScheduledTask(() -> {

				TileEntity tileentity = player.world.getTileEntity(message.pos);

				if(tileentity instanceof TileEntityLectern){

					((TileEntityLectern)tileentity).currentSpell = message.spell;
					((TileEntityLectern)tileentity).sync(); // Update other clients with the new state

				}else{
					Wizardry.logger.warn("Received a PacketLectern, but no lectern existed at the position specified!");
				}

			});
		}

		return null;
	}

	public static class Message implements IMessage {

		private BlockPos pos;
		private Spell spell;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(BlockPos pos, Spell spell){
			this.pos = pos;
			this.spell = spell;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
			spell = Spell.byNetworkID(buf.readInt());
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(pos.getX());
			buf.writeInt(pos.getY());
			buf.writeInt(pos.getZ());
			buf.writeInt(spell.networkID());
		}
	}
}
