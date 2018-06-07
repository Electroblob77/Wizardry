package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.entity.construct.EntityHealAura;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class HealingAura extends Spell {

	public HealingAura() {
		super(EnumTier.ADVANCED, 35, EnumElement.HEALING, "healing_aura", EnumSpellType.DEFENCE, 150, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(caster.onGround){
			if(!world.isRemote){
				EntityHealAura healaura = new EntityHealAura(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*durationMultiplier), damageMultiplier);
				world.spawnEntityInWorld(healaura);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		if(target != null){
			if(caster.onGround && world.getEntitiesWithinAABB(EntityHealAura.class, caster.boundingBox).isEmpty()){
				if(!world.isRemote){
					EntityHealAura healaura = new EntityHealAura(world, caster.posX, caster.posY, caster.posZ, caster, (int)(600*durationMultiplier), damageMultiplier);
					world.spawnEntityInWorld(healaura);
				}
				return true;
			}
			return false;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs() {
		return true;
	}

}
