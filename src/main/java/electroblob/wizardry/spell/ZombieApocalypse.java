package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityZombieSpawner;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ZombieApocalypse extends SpellConstruct<EntityZombieSpawner> {

	public static final String MINION_SPAWN_INTERVAL = "minion_spawn_interval";

	public ZombieApocalypse(){
		super("zombie_apocalypse", SpellActions.POINT_UP, EntityZombieSpawner::new, false);
		addProperties(SpellMinion.MINION_LIFETIME, MINION_SPAWN_INTERVAL);
	}

	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EnumFacing side, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		y += 8;
		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

}
