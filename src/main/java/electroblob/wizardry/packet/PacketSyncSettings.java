package electroblob.wizardry.packet;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketSyncSettings.Message;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>[Server -> Client]</b> This packet is sent to synchronise the config settings with clients on player login.
 * 
 * @see Settings
 */
public class PacketSyncSettings implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> copySettings(message));
		}

		return null;
	}

	private static void copySettings(Message message){
		Wizardry.settings.discoveryMode = message.settings.discoveryMode;
		Wizardry.settings.creativeBypassesArcaneLock = message.settings.creativeBypassesArcaneLock;
		Wizardry.settings.slowTimeAffectsPlayers = message.settings.slowTimeAffectsPlayers;
		Wizardry.settings.forfeitChance = message.settings.forfeitChance;
	}

	public static class Message implements IMessage {

		/** Instance of wizardry's settings object */
		public Settings settings;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(Settings settings){
			this.settings = settings;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// I'm guessing the settings field will be null here, so it needs initialising.
			// This is also a great reason to have the settings as an actual object.
			settings = new Settings();
			// The order is important
			settings.discoveryMode = buf.readBoolean();
			settings.creativeBypassesArcaneLock = buf.readBoolean();
			settings.slowTimeAffectsPlayers = buf.readBoolean();
			settings.replaceVanillaFireballs = buf.readBoolean();
			settings.forfeitChance = buf.readFloat();
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeBoolean(settings.discoveryMode);
			buf.writeBoolean(settings.creativeBypassesArcaneLock);
			buf.writeBoolean(settings.slowTimeAffectsPlayers);
			buf.writeBoolean(settings.replaceVanillaFireballs);
			buf.writeFloat((float)settings.forfeitChance); // Configs don't have floats but this can only be 0-1 anyway
		}
	}
}
