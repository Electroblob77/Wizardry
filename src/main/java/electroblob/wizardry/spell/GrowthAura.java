package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemDye;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrowthAura extends Spell {

	public GrowthAura() {
		super(Tier.APPRENTICE, 20, Element.EARTH, "growth_aura", SpellType.UTILITY, 50, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		boolean flag = false;

		for(int i=-2; i<1; i++){

			for(int j=-1; j<2; j++){

				int x = (int)caster.posX + i;
				int y = WizardryUtilities.getNearestFloorLevelC(world, new BlockPos(caster.posX + i, caster.posY, caster.posZ + j), 2) - 1;
				int z = (int)caster.posZ + j;

				BlockPos pos = new BlockPos(x, y, z);

				if(y > -1 && caster.getDistance(x, y, z) <= 2){

					IBlockState state = world.getBlockState(pos);

					if(state.getBlock() instanceof IGrowable){

						IGrowable igrowable = (IGrowable)state.getBlock();

						if(igrowable.canGrow(world, pos, state, world.isRemote)){

							if(!world.isRemote){
								if(igrowable.canUseBonemeal(world, world.rand, pos, state)){
									igrowable.grow(world, world.rand, pos, state);
								}
							}else{
								// Yes, it's meant to be 0, and it automatically changes it to 15.
								ItemDye.spawnBonemealParticles(world, pos, 0);
							}

							flag = true;
						}
					}
				}
			}
		}

		if(flag) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
		return flag;
	}


}
