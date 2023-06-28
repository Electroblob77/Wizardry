package electroblob.wizardry.packet;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.ResurrectionEvent;
import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.packet.PacketControlInput.Message;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Possession;
import electroblob.wizardry.spell.Resurrection;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.SpellModifiers;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** <b>[Client -> Server]</b> This packet is for control events such as buttons in GUIs and key presses. */
public class PacketControlInput implements IMessageHandler<Message, IMessage> {

	@Override
	public IMessage onMessage(Message message, MessageContext ctx){

		// Just to make sure that the side is correct
		if(ctx.side.isServer()){

			final EntityPlayerMP player = ctx.getServerHandler().player;

			player.getServerWorld().addScheduledTask(() -> {

				ItemStack wand = player.getHeldItemMainhand();

				if(!(wand.getItem() instanceof ISpellCastingItem)){
					wand = player.getHeldItemOffhand();
				}

				switch(message.controlType){

				case APPLY_BUTTON:

					if(!(player.openContainer instanceof ContainerArcaneWorkbench)){
						Wizardry.logger.warn("Received a PacketControlInput, but the player that sent it was not " +
								"currently using an arcane workbench. This should not happen!");
					}else{
						((ContainerArcaneWorkbench)player.openContainer).onApplyButtonPressed(player);
					}

					break;

				case CLEAR_BUTTON:

					if(!(player.openContainer instanceof ContainerArcaneWorkbench)){
						Wizardry.logger.warn("Received a PacketControlInput, but the player that sent it was not " +
								"currently using an arcane workbench. This should not happen!");
					}else{
						((ContainerArcaneWorkbench)player.openContainer).onClearButtonPressed(player);
					}

					break;

				case NEXT_SPELL_KEY:

					if(wand.getItem() instanceof ISpellCastingItem){

						((ISpellCastingItem)wand.getItem()).selectNextSpell(wand);
						// This line fixes the bug with continuous spells casting when they shouldn't be
						player.stopActiveHand();
					}

					break;

				case PREVIOUS_SPELL_KEY:

					if(wand.getItem() instanceof ISpellCastingItem){

						((ISpellCastingItem)wand.getItem()).selectPreviousSpell(wand);
						// This line fixes the bug with continuous spells casting when they shouldn't be
						player.stopActiveHand();
					}

					break;

				case RESURRECT_BUTTON:

					if(player.isDead && Resurrection.getRemainingWaitTime(player.deathTime) == 0){

						ItemStack stack = InventoryUtils.getHotbar(player).stream()
								.filter(s -> Resurrection.canStackResurrect(s, player)).findFirst().orElse(null);

						if(stack != null){
							if(MinecraftForge.EVENT_BUS.post(new ResurrectionEvent(player, player))) break;
							// This should suffice, since this is the only way a player can cast resurrection when dead!
							((ISpellCastingItem)stack.getItem()).cast(stack, Spells.resurrection, player, EnumHand.MAIN_HAND, 0, new SpellModifiers());
							break;
						}
					}

					Wizardry.logger.warn("Received a resurrect button packet, but the player that sent it was not" +
							" currently able to resurrect. This should not happen!");

					break;

				case CANCEL_RESURRECT:

					if(player.world.getGameRules().getBoolean("keepInventory")) break; // Shouldn't even receive this

					if(player.isDead){

						ItemStack stack = InventoryUtils.getHotbar(player).stream()
								.filter(s -> Resurrection.canStackResurrect(s, player)).findFirst().orElse(null);

						if(stack != null){
							player.dropItem(stack, true, false);
							player.inventory.deleteStack(stack); // Might as well
							break;
						}

						Wizardry.logger.warn("Received a cancel resurrect packet, but the player that sent it was not" +
								" holding a wand with the resurrection spell. This should not happen!");
					}

					Wizardry.logger.warn("Received a cancel resurrect packet, but the player that sent it was not" +
							" currently dead. This should not happen!");

					break;

				case POSSESSION_PROJECTILE:

					if(!Possession.isPossessing(player)) Wizardry.logger.warn("Received a possession projectile packet, " +
							"but the player that sent it is not currently possessing anything!");

					Possession.shootProjectile(player);

					break;
				}
			});
		}

		return null;
	}

	public enum ControlType {
		APPLY_BUTTON, NEXT_SPELL_KEY, PREVIOUS_SPELL_KEY, RESURRECT_BUTTON, CANCEL_RESURRECT, POSSESSION_PROJECTILE, CLEAR_BUTTON
	}

	public static class Message implements IMessage {

		private ControlType controlType;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message(){
		}

		public Message(ControlType type){
			this.controlType = type;
		}

		@Override
		public void fromBytes(ByteBuf buf){
			// The order is important
			this.controlType = ControlType.values()[buf.readInt()];
		}

		@Override
		public void toBytes(ByteBuf buf){
			buf.writeInt(controlType.ordinal());
		}
	}
}
