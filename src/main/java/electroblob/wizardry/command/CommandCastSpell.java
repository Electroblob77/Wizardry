package electroblob.wizardry.command;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.PacketCastSpellAtPos;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;

public class CommandCastSpell extends CommandBase {

	/** The default number of ticks for which /cast will cast a continuous spell, if duration is not specified. */
	public static final int DEFAULT_CASTING_DURATION = 100;
	/** The minimum number of seconds for which /cast may cast a continuous spell. */
	public static final int MIN_CASTING_DURATION = 0;
	/** The maximum number of seconds for which /cast may cast a continuous spell. */
	public static final int MAX_CASTING_DURATION = 1000000;

	@Override
	public String getName(){
		return Wizardry.settings.castCommandName;
	}

	@Override
	public int getRequiredPermissionLevel(){
		// I *think* it's something like 0 = everyone, 1 = moderator, 2 = op/admin, 3 = op/console...
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender){
		// Not ideal, but the way this is implemented means I have no choice. Only used in the help command, so in there
		// the custom command name will not display.
		return "commands." + Wizardry.MODID + ":cast.usage";
		// return I18n.format("commands." + Wizardry.MODID + ":cast.usage", Wizardry.settings.castCommandName);
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
			throw new WrongUsageException("commands." + Wizardry.MODID + ":cast.usage", Wizardry.settings.castCommandName);
		}else{

			// ===== Parameter retrieval =====

			int i = 0;

			EntityPlayerMP caster = null;
			Vec3d origin = null;
			EnumFacing direction = null;

			try{
				caster = getCommandSenderAsPlayer(sender);
			}catch (PlayerNotFoundException exception){
				// Nothing here since the player specifying is done later, I just don't want it to throw an exception
				// here.
			}

			Spell spell = Spell.get(arguments[i++]);

			if(spell == null){
				throw new NumberInvalidException("commands." + Wizardry.MODID + ":cast.not_found", arguments[i - 1]);
			}

			boolean castAsOtherPlayer = false;

			if(i + 3 < arguments.length){

				Vec3d vec3d = sender.getPositionVector();
				CoordinateArg x = parseCoordinate(vec3d.x, arguments[i++], true);
				CoordinateArg y = parseCoordinate(vec3d.y, arguments[i++], 0, 256, false);
				CoordinateArg z = parseCoordinate(vec3d.z, arguments[i++], true);

				origin = new Vec3d(x.getResult(), y.getResult(), z.getResult());

				direction = EnumFacing.byName(arguments[i++]);
				if(direction == null) throw new NumberInvalidException("commands." + Wizardry.MODID + ":cast.invalid_direction", arguments[i - 1]);

			}else if(i < arguments.length){
				try{
					// If the second argument is a player and is not the player that gave the command, the spell is cast
					// as the given player rather than the command sender, and there is a different chat readout.
					EntityPlayerMP entityplayermp = getPlayer(server, sender, arguments[i++]);
					if(caster != entityplayermp){
						castAsOtherPlayer = true;
						caster = entityplayermp;
					}
				}catch (PlayerNotFoundException exception){
					// If no player was found, rather than give an error it simply assumes the player was unspecified.
					i--;
				}
			}

			// If, after this point, the player is still null, the sender must be a command block or the console and the
			// player must not have been specified, meaning an exception should be thrown.
			if(caster == null && origin == null)
				throw new PlayerNotFoundException("commands." + Wizardry.MODID + ":cast.origin_not_specified");

			int duration = DEFAULT_CASTING_DURATION;
			int seconds = duration/20;

			if(spell.isContinuous){

				if(i >= arguments.length) throw new CommandException("commands." + Wizardry.MODID + ":cast.duration_not_specified");

				try{
					seconds = parseInt(arguments[i++]);
				}catch(NumberInvalidException e){
					// If no duration was found, assume it was unspecified
					i--;
				}

				if(seconds < MIN_CASTING_DURATION){
					throw new NumberInvalidException("commands.generic.num.tooSmall", seconds, MIN_CASTING_DURATION);
				}else if(seconds > MAX_CASTING_DURATION){
					throw new NumberInvalidException("commands.generic.num.tooBig", seconds, MAX_CASTING_DURATION);
				}

				duration = seconds * 20;
			}

			SpellModifiers modifiers = new SpellModifiers();

			if(i < arguments.length){

				// Copied from CommandGive. Why it doesn't just use arguments[i] itself I don't know.
				String nbt = getChatComponentFromNthArg(sender, arguments, i++).getUnformattedText();

				try{
					modifiers = SpellModifiers.fromNBT(JsonToNBT.getTagFromJson(nbt));
				}catch (NBTException nbtexception){
					throw new CommandException("commands." + Wizardry.MODID + ":cast.tag_error", nbtexception.getMessage());
				}

				for(float multiplier : modifiers.getModifiers().values()){
					if(multiplier < 0){
						throw new NumberInvalidException("commands.generic.double.tooSmall", multiplier, 0);
					}else if(multiplier > Wizardry.settings.maxSpellCommandMultiplier){
						throw new NumberInvalidException("commands.generic.double.tooBig", multiplier,
								Wizardry.settings.maxSpellCommandMultiplier);
					}
				}

			}

			// ===== Spell casting =====

			if(origin != null){ // Positional

				World world = sender.getEntityWorld();

				// If anything stops the spell working at this point, nothing else happens.
				if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(Source.COMMAND, spell, world,
						origin.x, origin.y, origin.z, direction, modifiers))){
					if(server.sendCommandFeedback()) displayFailMessage(sender, spell);
					return;
				}

				if(spell.isContinuous){
					// We need not query Spell#canBeCastByDispensers since with commands there's no difference between
					// a spell that can't be cast positionally and one that can be cast positionally but fails
					if(spell.cast(world, origin.x, origin.y, origin.z, direction, 0, duration, modifiers)){

						MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.COMMAND, spell, world, origin.x, origin.y, origin.z, direction, modifiers));

						SpellEmitter.add(spell, world, origin.x, origin.y, origin.z, direction, duration, modifiers);
						IMessage msg = new PacketCastSpellAtPos.Message(origin, direction, spell, modifiers, duration);
						WizardryPacketHandler.net.sendToDimension(msg, world.provider.getDimension());

						if(server.sendCommandFeedback()){
							sender.sendMessage(new TextComponentTranslation("commands." + Wizardry.MODID + ":cast.success_position_continuous",
									spell.getNameForTranslationFormatted(), origin.x, origin.y, origin.z, seconds));
						}

						return;
					}

				}else{

					if(spell.cast(world, origin.x, origin.y, origin.z, direction, 0, -1, modifiers)){

						MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.COMMAND, spell, world, origin.x, origin.y, origin.z, direction, modifiers));

						if(spell.requiresPacket()){
							// Sends a packet to all players in dimension to tell them to spawn particles.
							// Only sent if the spell succeeded, because if the spell failed, you wouldn't
							// need to spawn any particles!
							IMessage msg = new PacketCastSpellAtPos.Message(origin, direction, spell, modifiers);
							WizardryPacketHandler.net.sendToDimension(msg, world.provider.getDimension());
						}

						if(server.sendCommandFeedback()){
							sender.sendMessage(new TextComponentTranslation("commands." + Wizardry.MODID + ":cast.success_position",
									spell.getNameForTranslationFormatted(), origin.x, origin.y, origin.z));
						}

						return;
					}
				}

			}else{ // Player-based

				// If anything stops the spell working at this point, nothing else happens.
				if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(Source.COMMAND, spell, caster, modifiers))){
					if(server.sendCommandFeedback()) displayFailMessage(sender, spell);
					return;
				}

				if(spell.isContinuous){

					WizardData data = WizardData.get(caster);

					// Events/packets for continuous spell casting via commands are dealt with in WizardData.

					if(data != null){
						if(data.isCasting()){
							data.stopCastingContinuousSpell(); // I think on balance this is quite a nice feature to leave in
						}else{

							data.startCastingContinuousSpell(spell, modifiers, duration);

							if(server.sendCommandFeedback()){
								if(castAsOtherPlayer){
									sender.sendMessage(
											new TextComponentTranslation("commands." + Wizardry.MODID + ":cast.success_remote_continuous",
													spell.getNameForTranslationFormatted(), caster.getName(), seconds));
								}else{
									sender.sendMessage(new TextComponentTranslation("commands." + Wizardry.MODID + ":cast.success_continuous",
											spell.getNameForTranslationFormatted(), seconds));
								}
							}
						}

						return;
					}

				}else{

					if(spell.cast(caster.world, caster, EnumHand.MAIN_HAND, 0, modifiers)){

						MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.COMMAND, spell, caster, modifiers));

						if(spell.requiresPacket()){
							// Sends a packet to all players in dimension to tell them to spawn particles.
							// Only sent if the spell succeeded, because if the spell failed, you wouldn't
							// need to spawn any particles!
							IMessage msg = new PacketCastSpell.Message(caster.getEntityId(), null, spell, modifiers);
							WizardryPacketHandler.net.sendToDimension(msg, caster.world.provider.getDimension());
						}

						if(server.sendCommandFeedback()){
							if(castAsOtherPlayer){
								sender.sendMessage(new TextComponentTranslation("commands." + Wizardry.MODID + ":cast.success_remote",
										spell.getNameForTranslationFormatted(), caster.getName()));
							}else{
								sender.sendMessage(new TextComponentTranslation("commands." + Wizardry.MODID + ":cast.success",
										spell.getNameForTranslationFormatted()));
							}
						}

						return;
					}
				}
			}

			if(server.sendCommandFeedback()) displayFailMessage(sender, spell);
		}
	}

	/** Displays the "Unable to cast [spell]" message in the chat. */
	private void displayFailMessage(ICommandSender sender, Spell spell){
		ITextComponent message = new TextComponentTranslation("commands." + Wizardry.MODID + ":cast.fail",
				spell.getNameForTranslationFormatted());
		message.getStyle().setColor(TextFormatting.RED);
		sender.sendMessage(message);
	}

}
