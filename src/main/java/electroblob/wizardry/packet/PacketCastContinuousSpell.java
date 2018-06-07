package electroblob.wizardry.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketCastContinuousSpell.Message;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/** This packet is sent when the /cast command is used with a continuous spell, in order to sync the relevant variables
 * in ExtendedPlayer. */
public class PacketCastContinuousSpell implements IMessageHandler<Message, IMessage> {
	
	@Override
	public IMessage onMessage(Message message, MessageContext ctx)
	{
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			Wizardry.proxy.handleCastContinuousSpellPacket(message);
		}

		return null;
	}

	public static class Message implements IMessage
	{
		// Note that range and blast multipliers are the only two that affect particle spawning, so they are the
		// only two that need to be sent.
		
		/** EntityID of the caster */
		public int casterID;
		/** ID of the spell being cast */
		public int spellID;
		/** Range multiplier for the spell */
		public float damageMultiplier;
		/** Range multiplier for the spell */
		public float rangeMultiplier;
		/** Blast multiplier for the spell */
		public float blastMultiplier;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message() {}

		public Message(int casterID, int spellID, float damageMultiplier, float rangeMultiplier, float blastMultiplier)
		{
			this.casterID = casterID;
			this.spellID = spellID;
			this.damageMultiplier = damageMultiplier;
			this.rangeMultiplier = rangeMultiplier;
			this.blastMultiplier = blastMultiplier;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			// The order is important
			this.casterID = buf.readInt();
			this.spellID = buf.readInt();
			this.damageMultiplier = buf.readFloat();
			this.rangeMultiplier = buf.readFloat();
			this.blastMultiplier = buf.readFloat();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(casterID);
			buf.writeInt(spellID);
			buf.writeFloat(damageMultiplier);
			buf.writeFloat(rangeMultiplier);
			buf.writeFloat(blastMultiplier);
		}
	}
}
