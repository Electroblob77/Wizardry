package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntitySpark;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class HomingSpark extends Spell {

	public HomingSpark() {
		super(Tier.APPRENTICE, 10, Element.LIGHTNING, "homing_spark", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(!world.isRemote){
			EntitySpark spark = new EntitySpark(world, caster, modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntityInWorld(spark);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		}
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
		
			if(!world.isRemote){
				EntitySpark spark = new EntitySpark(world, caster, modifiers.get(SpellModifiers.DAMAGE));
				spark.directTowards(target, 0.5f);
				world.spawnEntityInWorld(spark);
				caster.playSound(WizardrySounds.SPELL_CONJURATION, 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
			}
			caster.swingArm(hand);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
