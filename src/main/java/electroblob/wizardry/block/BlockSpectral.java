package electroblob.wizardry.block;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

@Mod.EventBusSubscriber
public class BlockSpectral extends Block implements ITileEntityProvider {

	public BlockSpectral(Material material){
		super(material);
		this.setSoundType(SoundType.GLASS);
	}

	// Replaces getRenderBlockPass
	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.TRANSLUCENT;
	}

	// See BlockTransportationStone for what all these do
	@Override public boolean isFullCube(IBlockState state){ return false; }
	@Override public boolean isBlockNormalCube(IBlockState state){ return false; }
	@Override public boolean isNormalCube(IBlockState state){ return false; }
	@Override public boolean isOpaqueCube(IBlockState state){ return false; }

	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random){
		
		for(int i=0; i<2; i++){
			ParticleBuilder.create(Type.DUST)
			.pos(pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(), pos.getZ() + random.nextDouble())
			.time((int)(16.0D / (Math.random() * 0.8D + 0.2D)))
			.clr(0.4f + random.nextFloat() * 0.2f, 0.6f + random.nextFloat() * 0.4f, 0.6f + random.nextFloat() * 0.4f)
			.shaded(true).spawn(world);
		}
	}

//	// Overriden to make the block always look full brightness despite not emitting
//	// full light.
//	@Override
//	public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos){
//		return 15;
//	}

	@Override
	public boolean hasTileEntity(IBlockState state){
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata){
		return new TileEntityTimer(1200);
	}

	@Override
	public int quantityDropped(Random par1Random){
		return 0;
	}

	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos,
			EnumFacing side){

		IBlockState iblockstate = blockAccess.getBlockState(pos.offset(side));
		Block block = iblockstate.getBlock();

		return block == this ? false : super.shouldSideBeRendered(blockState, blockAccess, pos, side);
	}

	@SubscribeEvent
	public static void onBlockPlaceEvent(BlockEvent.PlaceEvent event){
		// Spectral blocks cannot be built on
		if(event.getPlacedAgainst() == WizardryBlocks.spectral_block){
			event.setCanceled(true);
		}
	}

}
