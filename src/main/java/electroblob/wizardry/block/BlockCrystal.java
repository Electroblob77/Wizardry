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
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.EnumMap;

public class BlockCrystal extends Block {

    public static final PropertyEnum<Element> ELEMENT = PropertyEnum.create("element", Element.class);

    private static final EnumMap<Element, MapColor> map_colours = new EnumMap<>(Element.class);

    static {
    	map_colours.put(Element.MAGIC, MapColor.PINK);
		map_colours.put(Element.FIRE, MapColor.ORANGE_STAINED_HARDENED_CLAY);
		map_colours.put(Element.ICE, MapColor.LIGHT_BLUE);
		map_colours.put(Element.LIGHTNING, MapColor.CYAN);
		map_colours.put(Element.NECROMANCY, MapColor.PURPLE);
		map_colours.put(Element.EARTH, MapColor.GREEN);
		map_colours.put(Element.SORCERY, MapColor.LIME);
		map_colours.put(Element.HEALING, MapColor.YELLOW);
	}
	
	public BlockCrystal(Material material){
		super(material);
        this.setDefaultState(this.blockState.getBaseState().withProperty(ELEMENT, Element.MAGIC));
        this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHarvestLevel("pickaxe", 2);
	}
	
	@Override
	public int damageDropped(IBlockState state){
        return (state.getValue(ELEMENT)).ordinal();
    }

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos){
		return map_colours.get(state.getProperties().get(ELEMENT));
	}

	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items){
		if(this.getCreativeTab() == tab){
        	for(Element element : Element.values()){
        		items.add(new ItemStack(this, 1, element.ordinal()));
        	}
        }
	}
	
	@Override
    public IBlockState getStateFromMeta(int metadata){
        return this.getDefaultState().withProperty(ELEMENT, Element.values()[metadata]);
    }

    @Override
    public int getMetaFromState(IBlockState state){
        return (state.getValue(ELEMENT)).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, ELEMENT);
    }
}
