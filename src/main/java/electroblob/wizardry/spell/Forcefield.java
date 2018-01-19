package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Forcefield extends Spell {

	public Forcefield() {
		super(Tier.ADVANCED, 45, Element.HEALING, "forcefield", SpellType.DEFENCE, 200, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(caster.onGround){
			if(!world.isRemote){
				EntityForcefield forcefield = new EntityForcefield(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*modifiers.get(WizardryItems.duration_upgrade)));
				world.spawnEntityInWorld(forcefield);
			}
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION_LARGE, 1.0f, 1.0f);
			return true;
		}
		
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
			// Wizards can no longer cast forcefield when they are inside one
			if(caster.onGround && world.getEntitiesWithinAABB(EntityForcefield.class, caster.getEntityBoundingBox()).isEmpty()){
				if(!world.isRemote){
					EntityForcefield forcefield = new EntityForcefield(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*modifiers.get(WizardryItems.duration_upgrade)));
					world.spawnEntityInWorld(forcefield);
				}
				caster.playSound(WizardrySounds.SPELL_CONJURATION_LARGE, 1.0f, 1.0f);
				return true;
			}
			
			return false;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
	
}
