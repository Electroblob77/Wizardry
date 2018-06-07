package electroblob.wizardry.command;

import java.util.HashSet;
import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

public class CommandViewAllies extends CommandBase {

	@Override
	public String getCommandName(){
		return Wizardry.alliesCommandName;
	}

	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 0;
	}
	
	@Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_)
    {
        return true;
    }

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_){
		return StatCollector.translateToLocalFormatted("commands.allies.usage", Wizardry.alliesCommandName);
	}

	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
		switch(arguments.length){
		case 1: return getListOfStringsMatchingLastWord(arguments, MinecraftServer.getServer().getAllUsernames());
		}
		return super.addTabCompletionOptions(sender, arguments);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments){

		EntityPlayerMP player = null;

		try{
			player = getCommandSenderAsPlayer(sender);
		}catch(PlayerNotFoundException exception){
			// Nothing here since the player specifying is done later, I just don't want it to throw an exception here.
		}

		boolean executeAsOtherPlayer = false;

		if(arguments.length > 0){

			player = getPlayer(sender, arguments[0]);
			// Don't want to catch the exception here either, because there can be no other first argument.
			
			// The long-winded statement after the '!' checks if the player is op (multiplayer) or if cheats are enabled for them (singleplayer).
			if(player != sender && player instanceof EntityPlayer && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayer)player).getGameProfile())){

				ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.allies.permission");
				chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
				player.addChatMessage(chatcomponenttranslation2);
				return;
			}

			if(player != sender) executeAsOtherPlayer = true;
		}

		// If, after this point, player is still null, the sender must be a command block or the console and the
		// player must not have been specified, meaning an exception should be thrown.
		if(player == null) throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");

		if(ExtendedPlayer.get(player) != null){

			String string = "";
			HashSet<String> names = ExtendedPlayer.get(player).allyNames;

			if(!names.isEmpty()){
				for(String name : names){
					string = string + name + ", ";
				}
				// Cuts the last " ," off of the string.
				string = string.substring(0, string.length() - 2);
			}else{
				string = StatCollector.translateToLocal("commands.allies.none");
			}

			if(executeAsOtherPlayer){
				sender.addChatMessage(new ChatComponentTranslation("commands.allies.list_other", player.getCommandSenderName(), string));
			}else{
				sender.addChatMessage(new ChatComponentTranslation("commands.allies.list", string));
			}
		}
	}

}
