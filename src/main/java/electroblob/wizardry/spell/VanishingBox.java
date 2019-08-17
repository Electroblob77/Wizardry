package electroblob.wizardry.spell;

import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class VanishingBox extends Spell {

	public VanishingBox(){
		super("vanishing_box", EnumAction.BOW, false);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){

			InventoryEnderChest enderchest = caster.getInventoryEnderChest();

			if(enderchest != null){
				caster.displayGUIChest(enderchest);
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
