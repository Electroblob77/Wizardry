package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which conjure an item for a certain duration.
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a conjuration spell; if something extra needs to be done, such as
 * a custom particle effect or conjuring the item in a specific slot, then methods can be overridden (perhaps using an
 * anonymous class) to add the required functionality.
 * <p>
 * By default, this type of spell cannot be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p>
 * By default, this type of spell requires a packet to be sent. {@link Spell#doesSpellRequirePacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see IConjuredItem
 */
public class SpellConjuration extends Spell {
	
	/** The item that is conjured by this spell. Should implement {@link IConjuredItem}. */
	protected final Item item;
	/** The sound that gets played when this spell is cast. */
	protected final SoundEvent sound;

	/** The volume of the sound played when this spell is cast. Defaults to 1. */
	protected float volume = 1;
	/** The pitch of the sound played when this spell is cast. Defaults to 1. */
	protected float pitch = 1;
	/** The pitch variation of the sound played when this spell is cast. Defaults to 0. */
	protected float pitchVariation = 0;
	
	public SpellConjuration(String name, Tier tier, Element element, SpellType type, int cost, int cooldown, Item item, SoundEvent sound){
		this(Wizardry.MODID, name, tier, element, type, cost, cooldown, item, sound);
	}

	public SpellConjuration(String modID, String name, Tier tier, Element element, SpellType type, int cost, int cooldown, Item item, SoundEvent sound){
		super(modID, name, tier, element, type, cost, cooldown, EnumAction.BOW, false);
		this.item = item;
		this.sound = sound;
	}
	
	/**
	 * Sets the sound parameters for this spell.
	 * @param volume 
	 * @param pitch
	 * @param pitchVariation
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellConjuration soundValues(float volume, float pitch, float pitchVariation) {
		this.volume = volume;
		this.pitch = pitch;
		this.pitchVariation = pitchVariation;
		return this;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(conjureItem(caster, modifiers)){
			
			if(world.isRemote) spawnParticles(world, caster, modifiers);
			
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1, 1);
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
		
		if(WizardryUtilities.doesPlayerHaveItem(caster, item)) return false;
		
		if(caster.getHeldItemMainhand().isEmpty()){
			caster.setHeldItem(EnumHand.MAIN_HAND, stack);
			return true;
		}else{
			return caster.inventory.addItemStackToInventory(stack);
		}
	}

}
