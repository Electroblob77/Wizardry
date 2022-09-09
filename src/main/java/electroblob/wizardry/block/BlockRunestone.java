package electroblob.wizardry.block;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.EnumMap;

public class BlockRunestone extends Block {

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

	public BlockRunestone(Material material, Element element) {
		super(material);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(1.5F);
		this.setResistance(10.0F);
		this.element = element;
	}

	public Element getElement() { return element; }

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) { return map_colours.get(element); }

	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT; // Required to shade parts of the block faces differently to others
	}
}
