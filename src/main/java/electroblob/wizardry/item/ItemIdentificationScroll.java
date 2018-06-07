package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.spell.Spell;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemIdentificationScroll extends Item {
	
	public ItemIdentificationScroll() {
		super();
		this.setTextureName("wizardry:identification_scroll");
		this.setUnlocalizedName("identification_scroll");
		this.setCreativeTab(Wizardry.tabWizardry);
	}
	
	@Override
	public boolean hasEffect(ItemStack stack){
		return true;
	}
	
	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		par3List.add(StatCollector.translateToLocalFormatted("item.identification_scroll.desc1", "\u00A77"));
		par3List.add(StatCollector.translateToLocalFormatted("item.identification_scroll.desc2", "\u00A77"));
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
		
		if(ExtendedPlayer.get(player) != null){
			
			ExtendedPlayer properties = ExtendedPlayer.get(player);

			// Isolates just the hotbar
			List hotbar = ((ContainerPlayer)player.openContainer).inventorySlots.subList(36, 45);

			for(Object slot : hotbar){

				if(slot instanceof Slot){

					ItemStack stack1 = ((Slot)slot).getStack();

					if(stack1 != null){
						Spell spell = Spell.get(stack1.getItemDamage());
						if((stack1.getItem() instanceof ItemSpellBook || stack1.getItem() instanceof ItemScroll)
								&& !properties.hasSpellBeenDiscovered(spell)){
							
							// Identification scrolls give the chat readout in creative mode, otherwise it looks like
							// nothing happens!
							properties.discoverSpell(spell);
							player.triggerAchievement(Wizardry.identifySpell);
							world.playSoundAtEntity(player, "random.levelup", 1.25f, 1);
							if(!player.capabilities.isCreativeMode) stack.stackSize--;
							if(!world.isRemote) player.addChatMessage(new ChatComponentTranslation("spell.discover", spell.getDisplayNameWithFormatting()));
							
							return stack;
						}
					}
				}
			}
			// If it found nothing to identify, it says so!
			if(!world.isRemote) player.addChatMessage(new ChatComponentTranslation("item.identification_scroll.nothing_to_identify"));
		}
		
		return stack;
	}
}
