package electroblob.wizardry.command;

import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
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

public class CommandDiscoverSpell extends CommandBase {

	@Override
	public String getCommandName(){
		return Wizardry.discoverspellCommandName;
	}
	
	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 2;
	}
	
	@Override
    public boolean canCommandSenderUseCommand(ICommandSender sender){
		// Only ops (multiplayer) or players with cheats enabled (singleplayer/LAN) can use /discoverspell.
        return !(sender instanceof EntityPlayer) || MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayer)sender).getGameProfile());
    }

	
	@Override
	public String getCommandUsage(ICommandSender p_71518_1_){
		return StatCollector.translateToLocalFormatted("commands.discoverspell.usage", Wizardry.discoverspellCommandName);
	}
	
	@Override
	public List addTabCompletionOptions(ICommandSender sender, String[] arguments) {
		switch(arguments.length){
		case 1: return getListOfStringsMatchingLastWord(arguments, Spell.getUnlocalisedNames());
		case 2: return getListOfStringsMatchingLastWord(arguments, MinecraftServer.getServer().getAllUsernames());
		}
		return super.addTabCompletionOptions(sender, arguments);
	}

	@Override
	public void processCommand(ICommandSender sender, String[] arguments){
		
		if(arguments.length < 1){
            throw new WrongUsageException("commands.discoverspell.usage", Wizardry.discoverspellCommandName);
        }else{
            
        	int i=0;
        	boolean clear = false;
        	boolean all = false;
        	
        	EntityPlayerMP entityplayermp = null;
        	
        	try{
        		entityplayermp = getCommandSenderAsPlayer(sender);
        	}catch(PlayerNotFoundException exception){
        		// Nothing here since the player specifying is done later, I just don't want it to throw an exception here.
        	}
        	
        	Spell spell = WizardryRegistry.none;
        	
        	if(arguments[i].equals("clear")){
        		clear = true;
        		i++;
        	}else if(arguments[i].equals("all")){
        		all = true;
        		i++;
        	}else{
        		
	        	spell = Spell.get(arguments[i++]);
	        	
	        	if(spell == null){
	        		throw new NumberInvalidException("commands.discoverspell.not_found", new Object[]{arguments[i-1]});
	        	}
        	}
        	
        	boolean castAsOtherPlayer = false;
        	
        	if(i < arguments.length){
        		// If the second argument is a player and is not the player that gave the command, the spell is cast
        		// as the given player rather than the command sender, and there is a different chat readout.
        		EntityPlayerMP entityplayermp1 = getPlayer(sender, arguments[i++]);
        		if(entityplayermp != entityplayermp1){
        			castAsOtherPlayer = true;
        			entityplayermp = entityplayermp1;
        		}
        	}
        	
        	// If, after this point, the player is still null, the sender must be a command block or the console and the
        	// player must not have been specified, meaning an exception should be thrown.
        	if(entityplayermp == null) throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");
        	
        	ExtendedPlayer properties = ExtendedPlayer.get(entityplayermp);
        	
        	if(properties != null){
	        	if(clear){
	        		properties.spellsDiscovered.clear();
        			sender.addChatMessage(new ChatComponentTranslation("commands.discoverspell.clear", entityplayermp.getCommandSenderName()));
	        	}else if(all){
	        		properties.spellsDiscovered.addAll(Spell.getSpells(Spell.allSpells));
        			sender.addChatMessage(new ChatComponentTranslation("commands.discoverspell.all", entityplayermp.getCommandSenderName()));
	        	}else{
	        		if(properties.hasSpellBeenDiscovered(spell)){
	        			properties.spellsDiscovered.remove(spell);
	        			sender.addChatMessage(new ChatComponentTranslation("commands.discoverspell.removespell", spell.getDisplayNameWithFormatting(), entityplayermp.getCommandSenderName()));
	        		}else{
		        		properties.discoverSpell(spell);
	        			sender.addChatMessage(new ChatComponentTranslation("commands.discoverspell.addspell", spell.getDisplayNameWithFormatting(), entityplayermp.getCommandSenderName()));
	        		}
	        	}
	        	properties.sync();
        	}
        }
	}

}
