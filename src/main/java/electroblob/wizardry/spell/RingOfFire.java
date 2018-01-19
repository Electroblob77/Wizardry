package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityFireRing;
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

public class RingOfFire extends Spell {

	public RingOfFire() {
		super(Tier.ADVANCED, 30, Element.FIRE, "ring_of_fire", SpellType.ATTACK, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(caster.onGround){
			if(!world.isRemote){
				EntityFireRing firering = new EntityFireRing(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*modifiers.get(WizardryItems.duration_upgrade)), modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntityInWorld(firering);
			}

			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
			if(caster.onGround && world.getEntitiesWithinAABB(EntityFireRing.class, caster.getEntityBoundingBox()).isEmpty()){
				if(!world.isRemote){
					EntityFireRing firering = new EntityFireRing(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*modifiers.get(WizardryItems.duration_upgrade)), modifiers.get(SpellModifiers.DAMAGE));
					world.spawnEntityInWorld(firering);
				}

				caster.playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1, 1);
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
