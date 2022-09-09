package electroblob.wizardry.item;

import electroblob.wizardry.api.IElemental;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.item.Item;

public class ItemCrystal extends Item implements IElemental {

	private final Element element;

	public ItemCrystal(Element element) {
		super();
		this.setMaxDamage(0);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.element = element;
	}

	@Override
	public Element getElement() { return element; }
}
