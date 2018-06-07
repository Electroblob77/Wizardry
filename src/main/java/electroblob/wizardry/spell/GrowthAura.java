package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemDye;
import net.minecraft.world.World;

public class GrowthAura extends Spell {

	public GrowthAura() {
		super(EnumTier.APPRENTICE, 20, EnumElement.EARTH, "growth_aura", EnumSpellType.UTILITY, 50, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		boolean flag = false;

		for(int i=-2; i<1; i++){

			for(int j=-1; j<2; j++){

				if(!world.isRemote){

					int x = (int)caster.posX + i;
					int y = WizardryUtilities.getNearestFloorLevelC(world, (int)caster.posX + i, (int)caster.posY, (int)caster.posZ + j, 2) - 1;
					int z = (int)caster.posZ + j;

					if(y > -1 && caster.getDistance(x, y, z) <= 2){

						Block block = world.getBlock(x, y, z);

						if(block instanceof IGrowable){

							IGrowable igrowable = (IGrowable)block;

							if (igrowable.func_149851_a(world, x, y, z, world.isRemote))
							{
								if (igrowable.func_149852_a(world, world.rand, x, y, z))
								{
									igrowable.func_149853_b(world, world.rand, x, y, z);
								}

								world.playAuxSFX(2005, x, y, z, 0);

								flag = true;
							}

						}
					}
				}
			}
		}

		return flag;
	}


}
