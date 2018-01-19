package electroblob.wizardry.command;

import java.util.List;
import java.util.Set;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

public class CommandViewAllies extends CommandBase {

	@Override
	public String getCommandName(){
		return Wizardry.settings.alliesCommandName;
	}

	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 0;
	}
	
	@Override
    public boolean checkPermission(MinecraftServer server, ICommandSender p_71519_1_)
    {
        return true;
    }

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_){
		// Not ideal, but the way this is implemented means I have no choice. Only used in the help command, so in there
		// the custom command name will not display.
		return "commands.wizardry:allies.usage";
		//return I18n.format("commands.wizardry:allies.usage", Wizardry.settings.alliesCommandName);
	}

	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] arguments, BlockPos pos) {
		switch(arguments.length){
		case 1: return getListOfStringsMatchingLastWord(arguments, server.getAllUsernames());
		}
		return super.getTabCompletionOptions(server, sender, arguments, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException {

		EntityPlayerMP player = null;

		try{
			player = getCommandSenderAsPlayer(sender);
		}catch(PlayerNotFoundException exception){
			// Nothing here since the player specifying is done later, I just don't want it to throw an exception here.
		}

		boolean executeAsOtherPlayer = false;

		if(arguments.length > 0){

			player = getPlayer(server, sender, arguments[0]);
			// Don't want to catch the exception here either, because there can be no other first argument.
			
			if(player != sender && sender instanceof EntityPlayer && !WizardryUtilities.isPlayerOp((EntityPlayer)sender, server)){
    			// Displays a chat message if a non-op tries to view another player's allies.
				TextComponentTranslation TextComponentTranslation2 = new TextComponentTranslation("commands.wizardry:allies.permission");
				TextComponentTranslation2.getStyle().setColor(TextFormatting.RED);
				player.addChatMessage(TextComponentTranslation2);
				return;
			}

			if(player != sender) executeAsOtherPlayer = true;
		}

		// If, after this point, player is still null, the sender must be a command block or the console and the
		// player must not have been specified, meaning an exception should be thrown.
		if(player == null) throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");

		if(WizardData.get(player) != null){

			String string = "";
			Set<String> names = WizardData.get(player).allyNames;

			if(!names.isEmpty()){
				for(String name : names){
					string = string + name + ", ";
				}
				// Cuts the last " ," off of the string.
				string = string.substring(0, string.length() - 2);
			}else{
				string = I18n.format("commands.wizardry:allies.none");
			}

			if(executeAsOtherPlayer){
				sender.addChatMessage(new TextComponentTranslation("commands.wizardry:allies.list_other", player.getName(), string));
			}else{
				sender.addChatMessage(new TextComponentTranslation("commands.wizardry:allies.list", string));
			}
		}
	}

}
