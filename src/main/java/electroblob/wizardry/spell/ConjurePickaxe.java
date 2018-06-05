package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.IConjuredItem;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ConjurePickaxe extends Spell {

	public ConjurePickaxe(){
		super(Tier.APPRENTICE, 25, Element.SORCERY, "conjure_pickaxe", SpellType.UTILITY, 50, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		ItemStack pickaxe = new ItemStack(WizardryItems.spectral_pickaxe);

		IConjuredItem.setDurationMultiplier(pickaxe, modifiers.get(WizardryItems.duration_upgrade));

		if(!WizardryUtilities.doesPlayerHaveItem(caster, WizardryItems.spectral_pickaxe)
				&& ConjureBow.conjureItemInInventory(caster, pickaxe)){
			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F
							+ world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 0.7f, 0.9f, 1.0f);
				}
			}
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
			return true;
		}
		return false;
	}

}
