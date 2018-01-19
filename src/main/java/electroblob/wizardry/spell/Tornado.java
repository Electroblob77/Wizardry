package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityTornado;
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

public class Tornado extends Spell {

	public Tornado() {
		super(Tier.ADVANCED, 35, Element.EARTH, "tornado", SpellType.ATTACK, 80, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(!world.isRemote){
			double x = caster.posX + caster.getLookVec().xCoord;
			double y = caster.posY;
			double z = caster.posZ + caster.getLookVec().zCoord;
			
			EntityTornado tornado = new EntityTornado(world, x, y, z, caster, (int)(200*modifiers.get(WizardryItems.duration_upgrade)), caster.getLookVec().xCoord/3, caster.getLookVec().zCoord/3, modifiers.get(SpellModifiers.DAMAGE));
			world.spawnEntityInWorld(tornado);
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
		return true;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
			
			if(!world.isRemote){
				double x = caster.posX + caster.getLookVec().xCoord;
				double y = caster.posY;
				double z = caster.posZ + caster.getLookVec().zCoord;
				
				EntityTornado tornado = new EntityTornado(world, x, y, z, caster, (int)(200*modifiers.get(WizardryItems.duration_upgrade)), caster.getLookVec().xCoord/3, caster.getLookVec().zCoord/3, modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntityInWorld(tornado);
			}
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
