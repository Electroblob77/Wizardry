package electroblob.wizardry.packet;

import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketSyncSettings.Message;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** <b>[Server -> Client]</b> This packet is sent to synchronise the config settings with clients on player login.
 * @see Settings */
public class PacketSyncSettings implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		if(ctx.side.isClient()){
			// Using a fully qualified name is a good course of action here; we don't really want to clutter the proxy
			// any more than necessary.
			net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(new Runnable(){
				@Override
				public void run(){
					copySettings(message);
				}
			});

		}

		return null;
	}
	
	private static void copySettings(Message message){
		Wizardry.settings.firebombIsCraftable = message.settings.firebombIsCraftable;
		Wizardry.settings.poisonBombIsCraftable = message.settings.poisonBombIsCraftable;
		Wizardry.settings.smokeBombIsCraftable = message.settings.smokeBombIsCraftable;
		Wizardry.settings.useAlternateScrollRecipe = message.settings.useAlternateScrollRecipe;
		Wizardry.settings.discoveryMode = message.settings.discoveryMode;
//		Wizardry.settings.maxSpellCommandMultiplier = message.settings.maxSpellCommandMultiplier;
//		Wizardry.settings.castCommandName = message.settings.castCommandName;
//		Wizardry.settings.discoverspellCommandName = message.settings.discoverspellCommandName;
//		Wizardry.settings.allyCommandName = message.settings.allyCommandName;
//		Wizardry.settings.alliesCommandName = message.settings.alliesCommandName;
	}

	public static class Message implements IMessage {

		/** EntityID of the caster */
		public Settings settings;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){}

		public Message(Settings settings){
			this.settings = settings;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// I'm guessing the settings field will be null here, so it needs initialising.
			// This is also a great reason to have the settings as an actual object.
			settings = new Settings();
			// The order is important
			settings.firebombIsCraftable = buf.readBoolean();
			settings.poisonBombIsCraftable = buf.readBoolean();
			settings.smokeBombIsCraftable = buf.readBoolean();
			settings.useAlternateScrollRecipe = buf.readBoolean();
			settings.discoveryMode = buf.readBoolean();
//			settings.maxSpellCommandMultiplier = buf.readDouble();
//			settings.castCommandName = ByteBufUtils.readUTF8String(buf);
//			settings.discoverspellCommandName = ByteBufUtils.readUTF8String(buf);
//			settings.allyCommandName = ByteBufUtils.readUTF8String(buf);
//			settings.alliesCommandName = ByteBufUtils.readUTF8String(buf);
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeBoolean(settings.firebombIsCraftable);
			buf.writeBoolean(settings.poisonBombIsCraftable);
			buf.writeBoolean(settings.smokeBombIsCraftable);
			buf.writeBoolean(settings.useAlternateScrollRecipe);
			buf.writeBoolean(settings.discoveryMode);
//			buf.writeDouble(settings.maxSpellCommandMultiplier);
//			ByteBufUtils.writeUTF8String(buf, settings.castCommandName);
//			ByteBufUtils.writeUTF8String(buf, settings.discoverspellCommandName);
//			ByteBufUtils.writeUTF8String(buf, settings.allyCommandName);
//			ByteBufUtils.writeUTF8String(buf, settings.alliesCommandName);
		}
	}
}
