package electroblob.wizardry.command;

import java.util.List;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;

public class CommandDiscoverSpell extends CommandBase {

	@Override
	public String getName(){
		return Wizardry.settings.discoverspellCommandName;
	}

	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 2;
	}

	/* @Override public boolean checkPermission(MinecraftServer server, ICommandSender sender){ // Only ops
	 * (multiplayer) or players with cheats enabled (singleplayer/LAN) can use /discoverspell. return !(sender
	 * instanceof EntityPlayer) ||
	 * server.getServer().getConfigurationManager().func_152596_g(((EntityPlayer)sender).getGameProfile()); } */

	@Override
	public String getUsage(ICommandSender sender){
		// Not ideal, but the way this is implemented means I have no choice. Only used in the help command, so in there
		// the custom command name will not display.
		return "commands.wizardry:discoverspell.usage";
		// return I18n.format("commands.wizardry:discoverspell.usage", Wizardry.settings.discoverspellCommandName);
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] arguments,
			BlockPos pos){
		switch(arguments.length){
		case 1:
			return getListOfStringsMatchingLastWord(arguments, Spell.getSpellNames());
		case 2:
			return getListOfStringsMatchingLastWord(arguments, server.getOnlinePlayerNames());
		}
		return super.getTabCompletions(server, sender, arguments, pos);
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] arguments) throws CommandException{

		if(arguments.length < 1){
			throw new WrongUsageException("commands.wizardry:discoverspell.usage",
					Wizardry.settings.discoverspellCommandName);
		}else{

			int i = 0;
			boolean clear = false;
			boolean all = false;

			EntityPlayerMP player = null;

			try{
				player = getCommandSenderAsPlayer(sender);
			}catch (PlayerNotFoundException exception){
				// Nothing here since the player specifying is done later, I just don't want it to throw an exception
				// here.
			}

			Spell spell = Spells.none;

			if(arguments[i].equals("clear")){
				clear = true;
				i++;
			}else if(arguments[i].equals("all")){
				all = true;
				i++;
			}else{

				spell = Spell.get(arguments[i++]);

				if(spell == null){
					throw new NumberInvalidException("commands.wizardry:discoverspell.not_found",
							new Object[]{arguments[i - 1]});
				}
			}

			if(i < arguments.length){
				// If the second argument is a player and is not the player that gave the command, the spell is
				// discovered as the given player rather than the command sender.
				EntityPlayerMP entityplayermp = getPlayer(server, sender, arguments[i++]);
				if(player != entityplayermp){
					player = entityplayermp;
				}
			}

			// If, after this point, the player is still null, the sender must be a command block or the console and the
			// player must not have been specified, meaning an exception should be thrown.
			if(player == null)
				throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.");

			WizardData properties = WizardData.get(player);

			if(properties != null){
				if(clear){
					properties.spellsDiscovered.clear();
					sender.sendMessage(
							new TextComponentTranslation("commands.wizardry:discoverspell.clear", player.getName()));
				}else if(all){
					properties.spellsDiscovered.addAll(Spell.getSpells(Spell.allSpells));
					sender.sendMessage(
							new TextComponentTranslation("commands.wizardry:discoverspell.all", player.getName()));
				}else{
					if(properties.hasSpellBeenDiscovered(spell)){
						properties.spellsDiscovered.remove(spell);
						sender.sendMessage(new TextComponentTranslation("commands.wizardry:discoverspell.removespell",
								spell.getNameForTranslationFormatted(), player.getName()));
					}else{
						if(!MinecraftForge.EVENT_BUS
								.post(new DiscoverSpellEvent(player, spell, DiscoverSpellEvent.Source.COMMAND))){
							properties.discoverSpell(spell);
							sender.sendMessage(new TextComponentTranslation("commands.wizardry:discoverspell.addspell",
									spell.getNameForTranslationFormatted(), player.getName()));
						}
					}
				}
				properties.sync();
			}
		}
	}

}
