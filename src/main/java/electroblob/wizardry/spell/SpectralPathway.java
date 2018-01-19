package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpectralPathway extends Spell {

	public SpectralPathway() {
		super(Tier.ADVANCED, 40, Element.SORCERY, "spectral_pathway", SpellType.UTILITY, 300, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		// Won't work if caster is airborne or if they are already on a bridge (prevents infinite bridges)
		if(WizardryUtilities.getBlockEntityIsStandingOn(caster).getBlock() == Blocks.AIR
				|| WizardryUtilities.getBlockEntityIsStandingOn(caster).getBlock() == WizardryBlocks.spectral_block){
			return false;
		}
		
		EnumFacing direction = caster.getHorizontalFacing();
		
		boolean flag = false;

		if(!world.isRemote){
			
			int baseLength = 15;
			
			// Gets the coordinates of the nearest block intersection to the player's feet.
			// Remember that a block always takes the coordinates of its northwestern corner.
			BlockPos origin = new BlockPos(Math.round(caster.posX), (int)caster.getEntityBoundingBox().minY-1, Math.round(caster.posZ));
			
			int startPoint = direction.getAxisDirection() == AxisDirection.POSITIVE ? -1 : 0;
			
			for(int i=0; i<(int)(baseLength*modifiers.get(WizardryItems.range_upgrade)); i++){
				// If either a block gets placed or one has already been placed, flag is set to true.
				flag = placePathwayBlockIfPossible(world, origin.offset(direction, startPoint + i), modifiers.get(WizardryItems.duration_upgrade)) || flag;
				flag = placePathwayBlockIfPossible(world, origin.offset(direction, startPoint + i)
						// Moves the BlockPos minus one block perpendicular to direction.
						.offset(EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, direction.rotateY().getAxis())), modifiers.get(WizardryItems.duration_upgrade)) || flag;
			}
		}
		// TODO: There may be some client/server discrepancies here.
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION_LARGE, 1.0f, 1.0f);
		
		return flag;
	}

	private static boolean placePathwayBlockIfPossible(World world, BlockPos pos, float durationMultiplier){
		if(WizardryUtilities.canBlockBeReplacedB(world, pos)){
			world.setBlockState(pos, WizardryBlocks.spectral_block.getDefaultState());
			if(world.getTileEntity(pos) instanceof TileEntityTimer){
				((TileEntityTimer)world.getTileEntity(pos)).setLifetime((int)(1200*durationMultiplier));
			}
			return true;
		}
		return false;
	}
	
}
