package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpectralPathway extends Spell {
	
	/** The base length of the conjured bridge, in blocks. */
	public static final String LENGTH = "length";

	public SpectralPathway(){
		super("spectral_pathway", SpellActions.POINT, false);
		addProperties(LENGTH, DURATION);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Won't work if caster is airborne or if they are already on a bridge (prevents infinite bridges)
		if(BlockUtils.getBlockEntityIsStandingOn(caster).getBlock() == Blocks.AIR
				|| BlockUtils.getBlockEntityIsStandingOn(caster).getBlock() == WizardryBlocks.spectral_block){
			return false;
		}

		EnumFacing direction = caster.getHorizontalFacing();

		boolean flag = false;

		if(!world.isRemote){

			// Gets the coordinates of the nearest block intersection to the player's feet.
			// Remember that a block always takes the coordinates of its northwestern corner.
			BlockPos origin = new BlockPos(Math.round(caster.posX), (int)caster.getEntityBoundingBox().minY - 1,
					Math.round(caster.posZ));

			int startPoint = direction.getAxisDirection() == AxisDirection.POSITIVE ? -1 : 0;

			for(int i = 0; i < (int)(getProperty(LENGTH).floatValue() * modifiers.get(WizardryItems.range_upgrade)); i++){
				// If either a block gets placed or one has already been placed, flag is set to true.
				flag = placePathwayBlockIfPossible(world, origin.offset(direction, startPoint + i),
						modifiers.get(WizardryItems.duration_upgrade)) || flag;
				flag = placePathwayBlockIfPossible(world, origin.offset(direction, startPoint + i)
						// Moves the BlockPos minus one block perpendicular to direction.
						.offset(EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, direction.rotateY().getAxis())),
						modifiers.get(WizardryItems.duration_upgrade)) || flag;
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return flag;
	}

	private boolean placePathwayBlockIfPossible(World world, BlockPos pos, float durationMultiplier){
		if(BlockUtils.canBlockBeReplaced(world, pos, true)){
			world.setBlockState(pos, WizardryBlocks.spectral_block.getDefaultState());
			if(world.getTileEntity(pos) instanceof TileEntityTimer){
				((TileEntityTimer)world.getTileEntity(pos)).setLifetime((int)(getProperty(DURATION).floatValue() * durationMultiplier));
			}
			return true;
		}
		return false;
	}

}
