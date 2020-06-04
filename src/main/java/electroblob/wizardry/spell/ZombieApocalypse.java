package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityZombieSpawner;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ZombieApocalypse extends SpellConstruct<EntityZombieSpawner> {

	public static final String MINION_SPAWN_INTERVAL = "minion_spawn_interval";

	private static final int SPAWNER_HEIGHT = 8;

	public ZombieApocalypse(){
		super("zombie_apocalypse", SpellActions.POINT_UP, EntityZombieSpawner::new, false);
		addProperties(SpellMinion.MINION_LIFETIME, MINION_SPAWN_INTERVAL);
		this.soundValues(1.3f, 1, 0);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return createContinuousSpellSounds();
	}

	@Override
	protected boolean spawnConstruct(World world, double x, double y, double z, EnumFacing side, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		y += SPAWNER_HEIGHT;
		return super.spawnConstruct(world, x, y, z, side, caster, modifiers);
	}

	@Override
	protected void addConstructExtras(EntityZombieSpawner construct, EnumFacing side, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		construct.spawnHusks = caster instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer)caster, WizardryItems.charm_minion_variants);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y + SPAWNER_HEIGHT, z, 0, (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
	}
}
