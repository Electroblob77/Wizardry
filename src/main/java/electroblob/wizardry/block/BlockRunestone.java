package electroblob.wizardry.block;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.EnumMap;

public class BlockRunestone extends Block {

    public static final PropertyEnum<Element> ELEMENT = PropertyEnum.create("element", Element.class,
			Arrays.copyOfRange(Element.values(), 1, Element.values().length)); // Everything except MAGIC

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
	
	public BlockRunestone(Material material){
		super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ELEMENT, Element.FIRE));
        this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(1.5F);
		this.setResistance(10.0F);
	}
	
	@Override
	public int damageDropped(IBlockState state){
        return state.getValue(ELEMENT).ordinal();
    }

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos){
		return map_colours.get(state.getProperties().get(ELEMENT));
	}
	
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items){
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
    public IBlockState getStateFromMeta(int metadata){
		Element element = Element.values()[metadata];
		if(!ELEMENT.getAllowedValues().contains(element)) return this.getDefaultState();
        return this.getDefaultState().withProperty(ELEMENT, element);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(ELEMENT).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ELEMENT);
    }

}
