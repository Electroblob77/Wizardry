package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityThunderbolt;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Thunderbolt extends Spell {

	public Thunderbolt(){
		super(Tier.BASIC, 10, Element.LIGHTNING, "thunderbolt", SpellType.ATTACK, 15, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			EntityThunderbolt thunderbolt = new EntityThunderbolt(world, caster, modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntity(thunderbolt);
		}

		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.8F,
				world.rand.nextFloat() * 0.2F + 0.8F);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				EntityThunderbolt thunderbolt = new EntityThunderbolt(world, caster,
						modifiers.get(SpellModifiers.DAMAGE));
				thunderbolt.directTowards(target, 2.5f);
				world.spawnEntity(thunderbolt);
			}

			caster.playSound(WizardrySounds.SPELL_ICE, 0.8F, world.rand.nextFloat() * 0.2F + 0.8F);
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
