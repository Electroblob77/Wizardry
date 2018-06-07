package electroblob.wizardry.command;

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

public class CommandSetAlly extends CommandBase {

	@Override
	public String getCommandName(){
		return Wizardry.allyCommandName;
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
		return StatCollector.translateToLocalFormatted("commands.ally.usage", Wizardry.allyCommandName);
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
		switch(arguments.length){
		case 1: return getListOfStringsMatchingLastWord(arguments, MinecraftServer.getServer().getAllUsernames());
		case 2: return getListOfStringsMatchingLastWord(arguments, MinecraftServer.getServer().getAllUsernames());
		}
		return super.addTabCompletionOptions(sender, arguments);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments){
		
		if(arguments.length < 1){
            throw new WrongUsageException("commands.ally.usage", Wizardry.allyCommandName);
        }else{
        	
        	EntityPlayerMP allyOf = null;
        	
        	try{
        		allyOf = getCommandSenderAsPlayer(sender);
        	}catch(PlayerNotFoundException exception){
        		// Nothing here since the player specifying is done later, I just don't want it to throw an exception here.
        	}
        	
        	boolean executeAsOtherPlayer = false;
        	
        	EntityPlayerMP ally = getPlayer(sender, arguments[0]);
        	// Don't want to catch the exception here, because the first player argument is always required.

        	if(arguments.length > 1){
        		
            	allyOf = getPlayer(sender, arguments[1]);
            	// Don't want to catch the exception here either, because there can be no other second argument.
            	
        		// The long-winded statement after the '!' checks if the player is op (multiplayer) or if cheats are enabled for them (singleplayer).
        		if(allyOf != sender && allyOf instanceof EntityPlayer && !MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayer)allyOf).getGameProfile())){
	        			
	                    ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.ally.permission");
	                    chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
	                    allyOf.addChatMessage(chatcomponenttranslation2);
	                    return;
        		}
        		
        		if(allyOf != sender) executeAsOtherPlayer = true;
        	}
        	
        	// If, after this point, allyOf is still null, the sender must be a command block or the console and two
        	// players must not have been specified, meaning an exception should be thrown.
        	if(allyOf == null) throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");
        	
        	if(allyOf == ally) throw new NumberInvalidException("commands.ally.self");
        	
        	if(ExtendedPlayer.get(allyOf) != null){
        		String string = ExtendedPlayer.get(allyOf).toggleAlly(ally) ? "add" : "remove";
    			if(executeAsOtherPlayer){
        			sender.addChatMessage(new ChatComponentTranslation("commands.ally." + string + "ally", ally.getCommandSenderName(), allyOf.getCommandSenderName()));
        			// In this case, the player whose allies have been modified is also notified.
        			allyOf.addChatMessage(new ChatComponentTranslation("item.wand." + string + "ally", ally.getCommandSenderName()));
    			}else{
        			sender.addChatMessage(new ChatComponentTranslation("item.wand." + string + "ally", ally.getCommandSenderName()));
    			}
        	}
        	
        }
	}

}
