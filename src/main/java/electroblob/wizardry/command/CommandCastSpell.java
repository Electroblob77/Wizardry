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

public class CommandCastSpell extends CommandBase {

	@Override
	public String getCommandName(){
		return Wizardry.castCommandName;
	}
	
	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 2;
	}
	
	@Override
    public boolean canCommandSenderUseCommand(ICommandSender sender){
		// Only ops (multiplayer) or players with cheats enabled (singleplayer/LAN) can use /cast.
        return !(sender instanceof EntityPlayer) || MinecraftServer.getServer().getConfigurationManager().func_152596_g(((EntityPlayer)sender).getGameProfile());
    }

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_){
		return StatCollector.translateToLocalFormatted("commands.cast.usage", Wizardry.castCommandName);
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
            throw new WrongUsageException("commands.cast.usage", Wizardry.castCommandName);
        }else{
            
        	int i=0;
        	
        	EntityPlayerMP entityplayermp = null;
        	
        	try{
        		entityplayermp = getCommandSenderAsPlayer(sender);
        	}catch(PlayerNotFoundException exception){
        		// Nothing here since the player specifying is done later, I just don't want it to throw an exception here.
        	}
        	
        	Spell spell = Spell.get(arguments[i++]);
        	
        	if(spell == null){
        		throw new NumberInvalidException("commands.cast.not_found", new Object[]{arguments[i-1]});
        	}
        	
        	boolean castAsOtherPlayer = false;
        	
        	if(i < arguments.length){
	        	try{
	        		// If the second argument is a player and is not the player that gave the command, the spell is cast
	        		// as the given player rather than the command sender, and there is a different chat readout.
	        		EntityPlayerMP entityplayermp1 = getPlayer(sender, arguments[i++]);
	        		if(entityplayermp != entityplayermp1){
	        			castAsOtherPlayer = true;
	        			entityplayermp = entityplayermp1;
	        		}
	        	}catch(PlayerNotFoundException exception){
	        		// If no player was found, rather than give an error it simply assumes the player was unspecified.
	        		i--;
	        	}
        	}
        	
        	// If, after this point, the player is still null, the sender must be a command block or the console and the
        	// player must not have been specified, meaning an exception should be thrown.
        	if(entityplayermp == null) throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");
        	
    		float damageMultiplier = i < arguments.length ? (float)this.parseDoubleBounded(sender, arguments[i++], 0d, Wizardry.maxSpellCommandMultiplier) : 1;
    		float rangeMultiplier = i < arguments.length ? (float)this.parseDoubleBounded(sender, arguments[i++], 0d, Wizardry.maxSpellCommandMultiplier) : 1;
    		float durationMultiplier = i < arguments.length ? (float)this.parseDoubleBounded(sender, arguments[i++], 0d, Wizardry.maxSpellCommandMultiplier) : 1;
    		float blastMultiplier = i < arguments.length ? (float)this.parseDoubleBounded(sender, arguments[i++], 0d, Wizardry.maxSpellCommandMultiplier) : 1;
        	
        	if(spell.isContinuous){
        		
        		ExtendedPlayer properties = ExtendedPlayer.get((EntityPlayer)entityplayermp);
        		
        		if(properties != null){
        			if(properties.isCasting()){
        				ExtendedPlayer.get((EntityPlayer)entityplayermp).stopCastingContinuousSpell();
        			}else{
        				ExtendedPlayer.get((EntityPlayer)entityplayermp).startCastingContinuousSpell(spell, damageMultiplier, rangeMultiplier, durationMultiplier, blastMultiplier);
        				
        				if(castAsOtherPlayer){
                			sender.addChatMessage(new ChatComponentTranslation("commands.cast.success_remote_continuous", spell.getDisplayNameWithFormatting(), entityplayermp.getCommandSenderName()));
            			}else{
            				sender.addChatMessage(new ChatComponentTranslation("commands.cast.success_continuous", spell.getDisplayNameWithFormatting()));
            			}
        			}
        			
        			return;
        		}
        		
        	}else{

        		if(spell.cast(entityplayermp.worldObj, entityplayermp, 0, damageMultiplier, rangeMultiplier, durationMultiplier, blastMultiplier)){
        			
        			// TODO: In this case, is it more efficient/simpler/better design to just do away with the packet
        			// optimisation entirely rather than calling sync() for spells which don't send packets?
        			
        			if(spell.doesSpellRequirePacket()){
						// Sends a packet to all players in dimension to tell them to spawn particles.
						// Only sent if the spell succeeded, because if the spell failed, you wouldn't
						// need to spawn any particles!
						IMessage msg = new PacketCastSpell.Message(entityplayermp.getEntityId(), 0, spell.id(), damageMultiplier, rangeMultiplier, blastMultiplier);
						WizardryPacketHandler.net.sendToDimension(msg, entityplayermp.worldObj.provider.dimensionId);
					}
        			
        			if(ExtendedPlayer.get((EntityPlayer)entityplayermp) != null){
        				ExtendedPlayer.get((EntityPlayer)entityplayermp).discoverSpell(spell);
        				// If the spell didn't send a packet itself, the extended player needs to be synced so the
        				// spell discovery updates on the client.
        				if(!spell.doesSpellRequirePacket()) ExtendedPlayer.get((EntityPlayer)entityplayermp).sync();
        			}
        			
        			if(castAsOtherPlayer){
            			sender.addChatMessage(new ChatComponentTranslation("commands.cast.success_remote", spell.getDisplayNameWithFormatting(), entityplayermp.getCommandSenderName()));
        			}else{
        				sender.addChatMessage(new ChatComponentTranslation("commands.cast.success", spell.getDisplayNameWithFormatting()));
        			}
        			return;
        		}
        	}
        	
        	IChatComponent message = new ChatComponentTranslation("commands.cast.fail", spell.getDisplayNameWithFormatting());
        	message.getChatStyle().setColor(EnumChatFormatting.RED);
			sender.addChatMessage(message);
        }
	}

}
