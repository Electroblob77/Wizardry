package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// For future reference - extend BlockContainer whenever possible because it has methods for removing tile entities on
// block break.
@Mod.EventBusSubscriber
public class BlockSpectral extends BlockContainer {

	public BlockSpectral(Material material){
		super(material);
		this.setSoundType(SoundType.GLASS);
	}

	// Replaces getRenderBlockPass
	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	// Apparently it's OK to override this, despite it being deprecated. More importantly, it being deprecated is not
	// Forge's doing, rather it is Mojang themselves misusing the @Deprecated annotation to mean 'internal, don't call'.
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos){
		return false;
	}

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

	// Overriden to make the block always look full brightness despite not emitting
	// full light.
	@Override
	public int getPackedLightmapCoords(IBlockState state, IBlockAccess source, BlockPos pos){
		return 15;
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
