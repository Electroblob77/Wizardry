package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Firebomb extends Spell {

	public Firebomb() {
		super(Tier.APPRENTICE, 15, Element.FIRE, "firebomb", SpellType.ATTACK, 25, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(!world.isRemote){
			EntityFirebomb firebomb = new EntityFirebomb(world, caster, modifiers.get(SpellModifiers.DAMAGE), modifiers.get(WizardryItems.blast_upgrade));
			world.spawnEntityInWorld(firebomb);
		}
		
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_SNOWBALL_THROW, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
			
			if(!world.isRemote){
				EntityFirebomb firebomb = new EntityFirebomb(world, caster, modifiers.get(SpellModifiers.DAMAGE), modifiers.get(WizardryItems.blast_upgrade));
				firebomb.directTowards(target, 1.5f);
				world.spawnEntityInWorld(firebomb);
			}
			
			caster.swingArm(hand);
			caster.playSound(SoundEvents.ENTITY_SNOWBALL_THROW, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
