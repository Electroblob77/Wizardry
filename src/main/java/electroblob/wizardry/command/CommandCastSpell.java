package electroblob.wizardry.command;

import java.util.List;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CommandCastSpell extends CommandBase {

	@Override
	public String getCommandName(){
		return Wizardry.settings.castCommandName;
	}
	
	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 2;
	}
	
	@Override
	public String getCommandUsage(ICommandSender p_71518_1_){
		// Not ideal, but the way this is implemented means I have no choice. Only used in the help command, so in there
		// the custom command name will not display.
		return "commands.wizardry:cast.usage";
		//return I18n.format("commands.wizardry:cast.usage", Wizardry.settings.castCommandName);
	}
	
	@Override
	public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender, String[] arguments, BlockPos pos) {
		switch(arguments.length){
		case 1: return getListOfStringsMatchingLastWord(arguments, Spell.getSpellNames());
		case 2: return getListOfStringsMatchingLastWord(arguments, server.getAllUsernames());
		}
		return super.getTabCompletionOptions(server, sender, arguments, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException {
		
		if(arguments.length < 1){
            throw new WrongUsageException("commands.wizardry:cast.usage", Wizardry.settings.castCommandName);
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
        		throw new NumberInvalidException("commands.wizardry:cast.not_found", new Object[]{arguments[i-1]});
        	}
        	
        	boolean castAsOtherPlayer = false;
        	
        	if(i < arguments.length){
	        	try{
	        		// If the second argument is a player and is not the player that gave the command, the spell is cast
	        		// as the given player rather than the command sender, and there is a different chat readout.
	        		EntityPlayerMP entityplayermp1 = getPlayer(server, sender, arguments[i++]);
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
            
        	SpellModifiers modifiers = new SpellModifiers();
            
        	if(i < arguments.length){
            	
        		// Copied from CommandGive. Why it doesn't just use arguments[i] itself I don't know.
                String nbt = getChatComponentFromNthArg(sender, arguments, i++).getUnformattedText();

                try{
                    modifiers = SpellModifiers.fromNBT(JsonToNBT.getTagFromJson(nbt));
                }catch(NBTException nbtexception){
                    throw new CommandException("commands.wizardry:cast.tag_error", nbtexception.getMessage());
                }
                
                for(float multiplier : modifiers.getModifiers().values()){
                	if(multiplier < 0){
                        throw new NumberInvalidException("commands.generic.double.tooSmall", multiplier, 0);
                    }else if(multiplier > Wizardry.settings.maxSpellCommandMultiplier){
                        throw new NumberInvalidException("commands.generic.double.tooBig", multiplier, Wizardry.settings.maxSpellCommandMultiplier);
                    }
                }
                
            }
            
        	if(spell.isContinuous){
        		
        		WizardData properties = WizardData.get((EntityPlayer)entityplayermp);
        		
        		if(properties != null){
        			if(properties.isCasting()){
        				WizardData.get((EntityPlayer)entityplayermp).stopCastingContinuousSpell();
        			}else{
        				
        				WizardData.get((EntityPlayer)entityplayermp).startCastingContinuousSpell(spell, modifiers);
        				
        				if(castAsOtherPlayer){
                			sender.addChatMessage(new TextComponentTranslation("commands.wizardry:cast.success_remote_continuous", spell.getNameForTranslationFormatted(), entityplayermp.getName()));
            			}else{
            				sender.addChatMessage(new TextComponentTranslation("commands.wizardry:cast.success_continuous", spell.getNameForTranslationFormatted()));
            			}
        			}
        			
        			return;
        		}
        		
        	}else{

        		if(spell.cast(entityplayermp.worldObj, entityplayermp, EnumHand.MAIN_HAND, 0, modifiers)){
        			
        			if(spell.doesSpellRequirePacket()){
						// Sends a packet to all players in dimension to tell them to spawn particles.
						// Only sent if the spell succeeded, because if the spell failed, you wouldn't
						// need to spawn any particles!
						IMessage msg = new PacketCastSpell.Message(entityplayermp.getEntityId(), null, spell.id(), modifiers);
						WizardryPacketHandler.net.sendToDimension(msg, entityplayermp.worldObj.provider.getDimension());
					}
        			
        			if(WizardData.get((EntityPlayer)entityplayermp) != null){
        				// Added optimisation from 1.7.10: if the spell was already discovered, nothing happens.
        				if(WizardData.get((EntityPlayer)entityplayermp).discoverSpell(spell)){
        					// If the spell didn't send a packet itself, the extended player needs to be synced so the
            				// spell discovery updates on the client.
            				if(!spell.doesSpellRequirePacket()) WizardData.get((EntityPlayer)entityplayermp).sync();
        				}
        			}
        			
        			if(castAsOtherPlayer){
            			sender.addChatMessage(new TextComponentTranslation("commands.wizardry:cast.success_remote", spell.getNameForTranslationFormatted(), entityplayermp.getName()));
        			}else{
        				sender.addChatMessage(new TextComponentTranslation("commands.wizardry:cast.success", spell.getNameForTranslationFormatted()));
        			}
        			return;
        		}
        	}
        	
        	ITextComponent message = new TextComponentTranslation("commands.wizardry:cast.fail", spell.getNameForTranslationFormatted());
        	message.getStyle().setColor(TextFormatting.RED);
			sender.addChatMessage(message);
        }
	}

}
