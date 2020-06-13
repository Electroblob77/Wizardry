package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import java.util.List;

public class GroupHeal extends Spell {

	public GroupHeal(){
		super("group_heal", SpellActions.POINT_UP, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		addProperties(EFFECT_RADIUS, HEALTH);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean flag = false;

		List<EntityLivingBase> targets = EntityUtils.getEntitiesWithinRadius(getProperty(EFFECT_RADIUS).floatValue()
				* modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){

			if(target == caster || AllyDesignationSystem.isAllied(caster, target)){

				if(target.getHealth() < target.getMaxHealth() && target.getHealth() > 0){

					Heal.heal(target, getProperty(HEALTH).floatValue() * modifiers.get(SpellModifiers.POTENCY));

					if(world.isRemote) ParticleBuilder.spawnHealParticles(world, target);
					playSound(world, target, ticksInUse, -1, modifiers);
					flag = true;
				}
			}
		}

		return flag;
	}

}
