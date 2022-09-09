package electroblob.wizardry.block;

import electroblob.wizardry.api.IElemental;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityShrineCore;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.EnumMap;

public class BlockRunestonePedestal extends Block implements ITileEntityProvider, IElemental {

	// A 'natural' pedestal is one that was generated as part of a structure, is unbreakable and has a tileentity
	public static final PropertyBool NATURAL = PropertyBool.create("natural");

	private static final EnumMap<Element, MapColor> map_colours = new EnumMap<>(Element.class);

	static {
		map_colours.put(Element.FIRE, MapColor.RED_STAINED_HARDENED_CLAY);
		map_colours.put(Element.ICE, MapColor.LIGHT_BLUE_STAINED_HARDENED_CLAY);
		map_colours.put(Element.LIGHTNING, MapColor.CYAN_STAINED_HARDENED_CLAY);
		map_colours.put(Element.NECROMANCY, MapColor.PURPLE_STAINED_HARDENED_CLAY);
		map_colours.put(Element.EARTH, MapColor.BROWN_STAINED_HARDENED_CLAY);
		map_colours.put(Element.SORCERY, MapColor.GRAY);
		map_colours.put(Element.HEALING, MapColor.YELLOW_STAINED_HARDENED_CLAY);
	}

	private final Element element;

	public BlockRunestonePedestal(Material material, Element element) {
		super(material);
		this.setDefaultState(this.blockState.getBaseState().withProperty(NATURAL, false));
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(1.5F);
		this.setResistance(10.0F);
		this.element = element;
	}

	@Override
	public Element getElement() {
		return element;
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return map_colours.get(element);
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT; // Required to shade parts of the block faces differently to others
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
		return state.getValue(NATURAL) ? -1 : super.getBlockHardness(state, world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
		return world.getBlockState(pos).getValue(NATURAL) ? 6000000.0F : super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return state.getValue(NATURAL); // Only naturally-generated pedestals have a (shrine core) tile entity
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityShrineCore();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(NATURAL, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(NATURAL) ? 1 : 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, NATURAL);
	}

}
