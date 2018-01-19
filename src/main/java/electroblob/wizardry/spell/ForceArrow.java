package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityForceArrow;
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

public class ForceArrow extends Spell {

	public ForceArrow() {
		super(Tier.APPRENTICE, 15, Element.SORCERY, "force_arrow", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(!world.isRemote){
			EntityForceArrow forceArrow = new EntityForceArrow(world, caster, 1*modifiers.get(WizardryItems.range_upgrade), modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntityInWorld(forceArrow);
		}
		
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_FORCE, 1.0f, 1.2f + world.rand.nextFloat()*0.2f);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityForceArrow forceArrow = new EntityForceArrow(world, caster, target, 1*modifiers.get(WizardryItems.range_upgrade), 2, modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntityInWorld(forceArrow);
			}
			
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_FORCE, 1.0f, 1.2f + world.rand.nextFloat()*0.2f);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
