package electroblob.wizardry.client.gui;

import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.spell.Spell;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiSpellBook extends GuiSpellInfo {

	private final ItemSpellBook book;
	private final Spell spell;

	public GuiSpellBook(ItemStack stack){

		super(288, 180);

		if(!(stack.getItem() instanceof ItemSpellBook)){
			throw new ClassCastException("Cannot create spell book GUI for item that does not extend ItemSpellBook!");
		}

		this.book = (ItemSpellBook)stack.getItem();
		this.spell = (Spell.byMetadata(stack.getItemDamage()));
	}

	@Override
	public Spell getSpell(){
		return spell;
	}

	@Override
	public ResourceLocation getTexture(){
		return book.getGuiTexture(spell);
	}

}
