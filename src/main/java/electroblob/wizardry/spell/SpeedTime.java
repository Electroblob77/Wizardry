package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a blank spell used to fill empty slots on wands. It is unobtainable in-game, except via
 * commands, and does nothing when the player attempts to cast it. Its instance can be referenced directly using
 * {@link electroblob.wizardry.registry.Spells#none Spells.none}
 */
public class SpeedTime extends Spell {

	/** The base number of ticks to add to the world time for each tick the spell is cast. */
	public static final String TIME_INCREMENT = "time_increment";
	/** The number of extra times to tick each nearby block, entity and tile entity each tick the spell is cast. */
	public static final String EXTRA_TICKS = "extra_ticks";

	public SpeedTime(){
		super("speed_time", SpellActions.POINT_UP, true);
		addProperties(EFFECT_RADIUS, TIME_INCREMENT, EXTRA_TICKS);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		boolean flag = false;

		// Hold onto your hats ladies and gentlemen, this effect scales with potency modifiers! Speeeeeeeeed!
		if(Wizardry.settings.worldTimeManipulation){
			world.setWorldTime(world.getWorldTime() + (long)(getProperty(TIME_INCREMENT).floatValue() * modifiers.get(SpellModifiers.POTENCY)));
			flag = true;
		}

		double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		// Doubles the normal effect of the modifier
		float potencyLevel = ((modifiers.get(SpellModifiers.POTENCY) - 1) * 2 + 1) * getProperty(EXTRA_TICKS).floatValue();

		// Ticks all the entities near the caster
		List<Entity> entities = new ArrayList<>(world.loadedEntityList);
		entities.removeIf(e -> e instanceof EntityPlayer);
		entities.removeIf(e -> caster.getDistance(e) > radius);

		if(!entities.isEmpty()){
			for(int i = 0; i < potencyLevel; i++){
				entities.forEach(Entity::onUpdate);
			}
			flag = true;
		}

		// Ticks all the tile entities near the caster
		// Copy the list first!
		List<TileEntity> tileentities = new ArrayList<>(world.tickableTileEntities);
		tileentities.removeIf(t -> caster.getDistanceSq(t.getPos()) > radius*radius);

		if(!tileentities.isEmpty()){
			for(int i = 0; i < potencyLevel; i++){
				tileentities.forEach(t -> ((ITickable)t).update());
			}
			flag = true;
		}

		if(!world.isRemote){

			List<BlockPos> sphere = BlockUtils.getBlockSphere(caster.getPosition(), radius);

			for(BlockPos pos : sphere){

				if(world.getBlockState(pos).getBlock().getTickRandomly()){
					for(int i = 0; i < potencyLevel; i++){
						world.getBlockState(pos).getBlock().randomTick(world, pos, world.getBlockState(pos), world.rand);
						flag = true;
					}
				}
			}
		}

		// Particle effects
		if(world.isRemote){

			for(int i=1; i<3; i++){

				double particleSpread = 2;
				double x = caster.posX + 2;
				double y = caster.getEntityBoundingBox().minY + caster.height / 2;
				double z = caster.posZ;

				ParticleBuilder.create(ParticleBuilder.Type.SPARKLE, world.rand, x, y, z, particleSpread, false)
						.vel(-0.25, 0, 0).time(16).clr(1f, 1f, 1f).spawn(world);

				ParticleBuilder.create(ParticleBuilder.Type.FLASH, world.rand, x, y, z, particleSpread, false)
						.vel(-0.25, 0, 0).time(16).scale(0.5f).clr(0.6f + world.rand.nextFloat() * 0.4f,
						0.6f + world.rand.nextFloat() * 0.4f, 0.6f + world.rand.nextFloat() * 0.4f).spawn(world);
			}
		}

		if(flag) playSound(world, caster, ticksInUse, -1, modifiers);
		// Always return true if the world time was changed, otherwise return false if nothing was ticked.
		return flag;
	}

}
