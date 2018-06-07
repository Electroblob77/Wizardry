package electroblob.wizardry.packet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.packet.PacketControlInput.Message;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** This packet is for control events such as buttons in GUIs and key presses. */
public class PacketControlInput implements IMessageHandler<Message, IMessage>
{
	@Override
	public IMessage onMessage(Message message, MessageContext ctx)
	{
		// Just to make sure that the side is correct
		if (ctx.side.isServer())
		{
			int eventID = message.eventID;
			EntityPlayer entityplayer = ctx.getServerHandler().playerEntity;
			
			if(eventID == 0){
        		// Note: the following long line of code gets the itemstack in a roundabout way because it needs
        		// to access the tile entity rather than the container to actually get to the items.
	        	TileEntityArcaneWorkbench tileentity = ((ContainerArcaneWorkbench)entityplayer.openContainer).tileEntityArcaneWorkbench;
	        	
	        	ItemStack wand = tileentity.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);
	        	ItemStack[] spellBooks = new ItemStack[ContainerArcaneWorkbench.CRYSTAL_SLOT];
	        	for(int i=0; i<spellBooks.length; i++){
	        		spellBooks[i] = tileentity.getStackInSlot(i);
	        	}
	        	ItemStack crystals = tileentity.getStackInSlot(ContainerArcaneWorkbench.CRYSTAL_SLOT);
	        	ItemStack upgrade = tileentity.getStackInSlot(ContainerArcaneWorkbench.UPGRADE_SLOT);
	        	
	        	// Since the workbench now accepts armour as well as wands, this check is needed.
	        	if(wand != null && wand.getItem() instanceof ItemWand){
	        		
	        		// Upgrades wand if necessary. Damage is copied, preserving remaining durability,
	        		// and also the entire NBT tag compound.
        			if(upgrade != null){
        				
        				if(upgrade.getItem() == Wizardry.arcaneTome){
        					
        					ItemStack newWand;
        					
	        				switch(EnumTier.values()[upgrade.getItemDamage()]){

							case APPRENTICE: 
								if(((ItemWand)wand.getItem()).tier == EnumTier.BASIC){
									newWand = new ItemStack(WizardryUtilities.getWand(EnumTier.values()[upgrade.getItemDamage()], ((ItemWand)wand.getItem()).element));
									newWand.stackTagCompound = wand.stackTagCompound;
									// This needs to be done after copying the tag compound so the max damage for the new wand takes storage
									// upgrades into account.
									newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
									tileentity.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, newWand);
									tileentity.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
									entityplayer.addStat(Wizardry.apprentice, 1);
								}
								break;
								
							case ADVANCED: 
								if(((ItemWand)wand.getItem()).tier == EnumTier.APPRENTICE){
									newWand = new ItemStack(WizardryUtilities.getWand(EnumTier.values()[upgrade.getItemDamage()], ((ItemWand)wand.getItem()).element));
									newWand.stackTagCompound = wand.stackTagCompound;
									newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
									tileentity.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, newWand);
									tileentity.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
								}
								break;
								
							case MASTER:
								if(((ItemWand)wand.getItem()).tier == EnumTier.ADVANCED){
									newWand = new ItemStack(WizardryUtilities.getWand(EnumTier.values()[upgrade.getItemDamage()], ((ItemWand)wand.getItem()).element));
									newWand.stackTagCompound = wand.stackTagCompound;
									newWand.setItemDamage(newWand.getMaxDamage() - (wand.getMaxDamage() - wand.getItemDamage()));
									tileentity.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, newWand);
									tileentity.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
									entityplayer.addStat(Wizardry.master, 1);
								}
								break;
								
							default:
								break;
	        				}
	        				
	        				// This needs to happen so the charging works on the new wand, not the old one.
	        				wand = tileentity.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);
        				
        				}else{

        					// Special upgrades
        					
        					// Used to preserve existing mana when upgrading storage rather than creating free mana.
        					int prevMana = wand.getMaxDamage() - wand.getItemDamage();
        					
        					if(WandHelper.getTotalUpgrades(wand) < ((ItemWand)wand.getItem()).tier.upgradeLimit
        							&& WandHelper.getUpgradeLevel(wand, upgrade.getItem()) < Wizardry.UPGRADE_STACK_LIMIT){
        						
        						WandHelper.applyUpgrade(wand, upgrade.getItem());
        						
        						// Special behaviours for specific upgrades
	        					if(upgrade.getItem() == Wizardry.storageUpgrade){
	        						wand.setItemDamage(wand.getMaxDamage() - prevMana);
	        					}
	        					if(upgrade.getItem() == Wizardry.attunementUpgrade){
	        						
		        					Spell[] spells = WandHelper.getSpells(wand);
		        					Spell[] newSpells = new Spell[5 + WandHelper.getUpgradeLevel(wand, Wizardry.attunementUpgrade)];
		        					
		        					for(int i=0; i<newSpells.length; i++){
		        						// Prevents both NPEs and AIOOBEs
			        					newSpells[i] = i < spells.length && spells[i] != null ? spells[i] : WizardryRegistry.none;
			        				}
		        					
		        					WandHelper.setSpells(wand, newSpells);
		        					
		        					int[] cooldown = WandHelper.getCooldowns(wand);
		        					int[] newCooldown = new int[5 + WandHelper.getUpgradeLevel(wand, Wizardry.attunementUpgrade)];
		        					
		        					if(cooldown.length > 0){
		        						for(int i=0; i<cooldown.length; i++){
			        						newCooldown[i] = cooldown[i];
			        					}
		        					}
		        					
		        					WandHelper.setCooldowns(wand, newCooldown);
	        					}
        						
        						tileentity.decrStackSize(ContainerArcaneWorkbench.UPGRADE_SLOT, 1);
        						entityplayer.addStat(Wizardry.specialUpgrade, 1);
        						
        						if(WandHelper.getTotalUpgrades(wand) == EnumTier.MASTER.upgradeLimit){
            						entityplayer.addStat(Wizardry.maxOutWand, 1);
        						}
        					}
        				}
        			}
        			
        			// Reads NBT spell id array to variable, edits this, then writes it back to NBT.
        			// Original spells are preserved; if a slot is left empty the existing spell binding will remain.
        			// Accounts for spells which cannot be applied because they are above the wand's tier; these spells
        			// will not bind but the existing spell in that slot will remain and other applicable spells will
        			// be bound as normal, along with any upgrades and crystals.
        			Spell[] spells = WandHelper.getSpells(wand);
        			if(spells.length <= 0){
        				// 5 here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
        				spells = new Spell[5];
        			}
        			for(int i=0; i<spells.length; i++){
        				if(spellBooks[i] != null && !(Spell.get(spellBooks[i].getItemDamage()).tier.level > ((ItemWand)wand.getItem()).tier.level)){
        					spells[i] = Spell.get(spellBooks[i].getItemDamage());
        				}
        			}
        			WandHelper.setSpells(wand, spells);
        			
        			// Charges wand by appropriate amount
        			if(crystals != null){
	        			int chargeDepleted = wand.getItemDamage();
		        		//System.out.println("Charge depleted: " + chargeDepleted);
		        		//System.out.println("Crystals found: " + crystals.stackSize);
	        			if(crystals.stackSize * Wizardry.MANA_PER_CRYSTAL < chargeDepleted){
	    	        		//System.out.println("charging");
	        				wand.setItemDamage(chargeDepleted - crystals.stackSize * Wizardry.MANA_PER_CRYSTAL);
	        				tileentity.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, crystals.stackSize);
	        			}else if(chargeDepleted != 0){
	        				//System.out.println((int)Math.ceil(((double)chargeDepleted)/50));
	        				tileentity.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, (int)Math.ceil(((double)chargeDepleted)/Wizardry.MANA_PER_CRYSTAL));
	        				wand.setItemDamage(0);
	        			}
	        		}
        		
        		// Armour
	        	}else if(wand != null && wand.getItem() instanceof ItemWizardArmour){
	        		// Applies legendary upgrade
	        		if(upgrade != null && upgrade.getItem() == Wizardry.armourUpgrade){
	        			if(!wand.hasTagCompound()){
	        				wand.stackTagCompound = new NBTTagCompound();
	        			}
	        			if(!wand.stackTagCompound.hasKey("legendary")){
	        				wand.stackTagCompound.setBoolean("legendary", true);
	        				tileentity.setInventorySlotContents(ContainerArcaneWorkbench.UPGRADE_SLOT, null);
	            			entityplayer.triggerAchievement(Wizardry.legendary);
	        			}
	        		}
	        		// Charges armour by appropriate amount
        			if(crystals != null){
	        			int chargeDepleted = wand.getItemDamage();
	        			if(crystals.stackSize * Wizardry.MANA_PER_CRYSTAL < chargeDepleted){
	        				wand.setItemDamage(chargeDepleted - crystals.stackSize * Wizardry.MANA_PER_CRYSTAL);
	        				tileentity.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, crystals.stackSize);
	        			}else if(chargeDepleted != 0){
	        				tileentity.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, (int)Math.ceil(((double)chargeDepleted)/Wizardry.MANA_PER_CRYSTAL));
	        				wand.setItemDamage(0);
	        			}
	        		}
        			
        		// Scrolls
	        	}else if(wand != null && wand.getItem() == Wizardry.blankScroll){
	        		// Spells can only be bound to scrolls if the player has already cast them (prevents casting of master spells without getting a master wand)
	        		// This restriction does not apply in creative mode
	        		if(spellBooks[0] != null && (entityplayer.capabilities.isCreativeMode || (ExtendedPlayer.get(entityplayer) != null
	        				&& ExtendedPlayer.get(entityplayer).hasSpellBeenDiscovered(Spell.get(spellBooks[0].getItemDamage()))))
	        				&& crystals != null && crystals.stackSize * Wizardry.MANA_PER_CRYSTAL > Spell.get(spellBooks[0].getItemDamage()).cost){

        				tileentity.decrStackSize(ContainerArcaneWorkbench.CRYSTAL_SLOT, (int)Math.ceil(((double)Spell.get(spellBooks[0].getItemDamage()).cost)/Wizardry.MANA_PER_CRYSTAL));
        				tileentity.setInventorySlotContents(ContainerArcaneWorkbench.WAND_SLOT, new ItemStack(Wizardry.scroll, 1, spellBooks[0].getItemDamage()));
    				}
	        	}
	        	
	        //////////////////////////////////////////////////////////////////////////////////////////////	
	        	
	        }else if(eventID == 1){
	        	if(entityplayer.inventory.getCurrentItem() != null &&
	           		 	entityplayer.inventory.getCurrentItem().getItem() instanceof ItemWand){
	        		
		           	ItemStack wand = entityplayer.inventory.getCurrentItem();
		           	
		   			if(wand != null) WandHelper.selectNextSpell(wand);
		   			
		   			// This line fixes the bug with continuous spells casting when they shouldn't be
		   			entityplayer.clearItemInUse();
	            }
	        	
	        //////////////////////////////////////////////////////////////////////////////////////////////
	        	
	        }else if(eventID == 2){
	        	if(entityplayer.inventory.getCurrentItem() != null &&
		           		 entityplayer.inventory.getCurrentItem().getItem() instanceof ItemWand){
	        		
		           	ItemStack wand = entityplayer.inventory.getCurrentItem();
		   			
		           	if(wand != null) WandHelper.selectPreviousSpell(wand);

		   			// This line fixes the bug with continuous spells casting when they shouldn't be
		   			entityplayer.clearItemInUse();
	        	}
	        }
		}

		return null;
	}

	public static class Message implements IMessage
	{
		/** 0 = Apply button in arcane workbench, 1 = N key, 2 = B key */
		private int eventID;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message() {}

		public Message(int eventID)
		{
			this.eventID = eventID;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			// The order is important
			this.eventID = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(eventID);
		}
	}
}
