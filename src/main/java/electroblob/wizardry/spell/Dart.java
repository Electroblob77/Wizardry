package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityDart;
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

public class Dart extends Spell {

	public Dart() {
		super(Tier.BASIC, 5, Element.EARTH, "dart", SpellType.ATTACK, 10, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		if(!world.isRemote){
			EntityDart dart = new EntityDart(world, caster, 2*modifiers.get(WizardryItems.range_upgrade), modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntityInWorld(dart);
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_ARROW_SHOOT, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityDart dart = new EntityDart(world, caster, target, 2*modifiers.get(WizardryItems.range_upgrade), 2, modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntityInWorld(dart);
			}
			caster.swingArm(hand);
			caster.playSound(SoundEvents.ENTITY_ARROW_SHOOT, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
