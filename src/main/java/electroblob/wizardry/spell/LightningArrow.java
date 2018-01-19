package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntityLightningArrow;
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

public class LightningArrow extends Spell {

	public LightningArrow() {
		super(Tier.APPRENTICE, 15, Element.LIGHTNING, "lightning_arrow", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(!world.isRemote){
			EntityLightningArrow lightningArrow = new EntityLightningArrow(world, caster, 2*modifiers.get(WizardryItems.range_upgrade), modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntityInWorld(lightningArrow);
		}
		
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1.0F, world.rand.nextFloat() * 0.3F + 1.3F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
		
			if(!world.isRemote){
				EntityLightningArrow lightningArrow = new EntityLightningArrow(world, caster, target, 2*modifiers.get(WizardryItems.range_upgrade), 4, modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntityInWorld(lightningArrow);
			}
			
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_LIGHTNING, 1.0F, world.rand.nextFloat() * 0.3F + 1.3F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
