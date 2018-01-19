package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityLightningDisc;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class LightningDisc extends Spell {

	public LightningDisc() {
		super(Tier.ADVANCED, 25, Element.LIGHTNING, "lightning_disc", SpellType.ATTACK, 60, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(!world.isRemote){
			EntityLightningDisc lightningdisc = new EntityLightningDisc(world, caster, modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntityInWorld(lightningdisc);
		}

		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1.0F, world.rand.nextFloat() * 0.3F + 0.8F);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityLightningDisc lightningdisc = new EntityLightningDisc(world, caster, modifiers.get(SpellModifiers.DAMAGE));
				lightningdisc.directTowards(target, 1.2f);
				world.spawnEntityInWorld(lightningdisc);
			}

			caster.playSound(WizardrySounds.SPELL_LIGHTNING, 1.0F, world.rand.nextFloat() * 0.3F + 0.8F);
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
