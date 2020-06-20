package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class GroupHeal extends SpellAreaEffect {

	public GroupHeal(){
		super("group_heal", SpellActions.POINT_UP, false);
		this.soundValues(0.7f, 1.2f, 0.4f);
		this.targetAllies(true);
		addProperties(HEALTH);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(target.getHealth() < target.getMaxHealth() && target.getHealth() > 0){

			Heal.heal(target, getProperty(HEALTH).floatValue() * modifiers.get(SpellModifiers.POTENCY));

			if(world.isRemote) ParticleBuilder.spawnHealParticles(world, target);
			playSound(world, target, ticksInUse, -1, modifiers);
			return true;
		}

		return false; // Only succeeds if something was healed
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		// We're spawning particles above so don't bother with this method
	}

}
