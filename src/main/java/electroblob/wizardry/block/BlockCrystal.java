package electroblob.wizardry.block;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.EnumMap;

public class BlockCrystal extends Block {

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

	private final Element element;

	public BlockCrystal(Element element) {
		super(Material.IRON);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHarvestLevel("pickaxe", 2);
		this.element = element;
	}

	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
		return map_colours.get(state.getProperties().get(element));
	}

}
