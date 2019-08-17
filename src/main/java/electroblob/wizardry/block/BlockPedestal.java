package electroblob.wizardry.block;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.tileentity.TileEntityShrineCore;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumMap;

public class BlockPedestal extends Block implements ITileEntityProvider {

	public static final PropertyEnum<Element> ELEMENT = PropertyEnum.create("element", Element.class,
			Arrays.copyOfRange(Element.values(), 1, Element.values().length)); // Everything except MAGIC

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

	public BlockPedestal(Material material){
		super(material);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ELEMENT, Element.FIRE).withProperty(NATURAL, false));
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(1.5F);
		this.setResistance(10.0F);
	}

	@Override
	public int damageDropped(IBlockState state){
		return state.getValue(ELEMENT).ordinal(); // Ignore the NATURAL state here, it's unobtainable
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos){
		return map_colours.get(state.getProperties().get(ELEMENT));
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items){
		// Ignore the NATURAL state here, it's unobtainable
		if(this.getCreativeTab() == tab){
			for(Element element : Arrays.copyOfRange(Element.values(), 1, Element.values().length)){
				items.add(new ItemStack(this, 1, element.ordinal()));
			}
		}
	}

	@Override
	public BlockRenderLayer getRenderLayer(){
		return BlockRenderLayer.CUTOUT; // Required to shade parts of the block faces differently to others
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos){
		return state.getValue(NATURAL) ? -1 : super.getBlockHardness(state, world, pos);
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion){
		return world.getBlockState(pos).getValue(NATURAL) ? 6000000.0F : super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	public boolean hasTileEntity(IBlockState state){
		return state.getValue(NATURAL); // Only naturally-generated pedestals have a (shrine core) tile entity
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World world, int meta){
		return new TileEntityShrineCore();
	}

	@Override
	public IBlockState getStateFromMeta(int metadata){
		boolean natural = false;
		if(metadata > ELEMENT.getAllowedValues().size()){
			natural = true;
			metadata -= ELEMENT.getAllowedValues().size();
		}
		return this.getDefaultState().withProperty(ELEMENT, Element.values()[metadata]).withProperty(NATURAL, natural);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(ELEMENT).ordinal() + (state.getValue(NATURAL) ? ELEMENT.getAllowedValues().size() : 0);
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, ELEMENT, NATURAL);
	}

}
