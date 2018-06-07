package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class VanishingBox extends Spell {

	public VanishingBox() {
		super(EnumTier.ADVANCED, 45, EnumElement.SORCERY, "vanishing_box", EnumSpellType.UTILITY, 70, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(!world.isRemote){
			
			InventoryEnderChest enderchest = caster.getInventoryEnderChest();
			
			if(enderchest != null){
				caster.displayGUIChest(enderchest);
			}
		}
		
		world.playSoundAtEntity(caster, "wizardry:aura", 1, 1);
		
		return true;
	}
	
}
