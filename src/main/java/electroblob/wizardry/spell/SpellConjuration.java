package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which conjure an item for a certain duration.
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a conjuration spell; if something extra needs to be done, such as
 * a custom particle effect or conjuring the item in a specific slot, then methods can be overridden (perhaps using an
 * anonymous class) to add the required functionality.
 * <p></p>
 * Properties added by this type of spell: {@link SpellConjuration#ITEM_LIFETIME}
 * <p></p>
 * By default, this type of spell cannot be cast by NPCs. {@link Spell#canBeCastBy(net.minecraft.entity.EntityLiving, boolean)}
 * <p></p>
 * By default, this type of spell cannot be cast by dispensers. {@link Spell#canBeCastBy(net.minecraft.tileentity.TileEntityDispenser)}
 * <p></p>
 * By default, this type of spell requires a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see IConjuredItem
 */
public class SpellConjuration extends Spell {

	public static final String ITEM_LIFETIME = "item_lifetime";

	/** The item that is conjured by this spell. Should implement {@link IConjuredItem}. */
	protected final Item item;

	public SpellConjuration(String name, Item item){
		this(Wizardry.MODID, name, item);
	}

	public SpellConjuration(String modID, String name, Item item){
		super(modID, name, SpellActions.IMBUE, false);
		this.item = item;
		addProperties(ITEM_LIFETIME);
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(conjureItem(caster, modifiers)){
			
			if(world.isRemote) spawnParticles(world, caster, modifiers);
			
			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}
		
		return false;
	}
	
	/** Spawns sparkle particles around the caster. Override to add a custom particle effect. Only called client-side. */
	protected void spawnParticles(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		for(int i=0; i<10; i++){
			double x = caster.posX + world.rand.nextDouble() * 2 - 1;
			double y = caster.getEntityBoundingBox().minY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(0.7f, 0.9f, 1).spawn(world);
		}
	}
	
	/** Adds this spell's item to the given player's inventory, placing it in the main hand if the main hand is empty.
	 * Returns true if the item was successfully added to the player's inventory, false if there as no space or if the
	 * player already had the item. Override to add special conjuring behaviour. */
	protected boolean conjureItem(EntityPlayer caster, SpellModifiers modifiers){

		ItemStack stack = new ItemStack(item);

		IConjuredItem.setDurationMultiplier(stack, modifiers.get(WizardryItems.duration_upgrade));
		IConjuredItem.setDamageMultiplier(stack, modifiers.get(SpellModifiers.POTENCY));
		
		if(InventoryUtils.doesPlayerHaveItem(caster, item)) return false;
		
		if(caster.getHeldItemMainhand().isEmpty()){
			caster.setHeldItem(EnumHand.MAIN_HAND, stack);
			return true;
		}else{
			return caster.inventory.addItemStackToInventory(stack);
		}
	}

}
