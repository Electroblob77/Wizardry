package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class Intimidate extends SpellAreaEffect {

	/** The NBT tag name for storing the feared entity's UUID in the target's tag compound. */
	public static final String NBT_KEY = "fearedEntity";

	// These aren't spell properties because they're part fo the actual potion effect, not the spell itself.
	// However, the avoid distance can be modified using the potion amplifier.
	private static final double BASE_AVOID_DISTANCE = 16;
	private static final double AVOID_DISTANCE_PER_LEVEL = 4;

	public Intimidate(){
		super("intimidate", SpellActions.SUMMON, false);
		this.alwaysSucceed(true);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser){
		return false;
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(caster != null && target instanceof EntityCreature){

			int bonusAmplifier = SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY));

			NBTTagCompound entityNBT = target.getEntityData();
			if(entityNBT != null) entityNBT.setUniqueId(NBT_KEY, caster.getUniqueID());

			target.addPotionEffect(new PotionEffect(WizardryPotions.fear,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					getProperty(EFFECT_STRENGTH).intValue() + bonusAmplifier));
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		if(caster != null) origin = caster.getPositionEyes(0);

		for(int i = 0; i < 30; i++){
			double x = origin.x - 1 + world.rand.nextDouble() * 2;
			double y = origin.y - 0.25 + world.rand.nextDouble() * 0.5;
			double z = origin.z - 1 + world.rand.nextDouble() * 2;
			ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.9f, 0.1f, 0).spawn(world);
		}
	}

	/**
	 * Finds a random position away from the caster and sets the given target's AI path to that location. Defined here
	 * so it can be used both in the spell itself and in the potion effect (event handler).
	 * 
	 * @param target The entity running away
	 * @param caster The entity that is being run away from
	 * @param distance How far the entity will run from the caster
	 * @return True if a new path was found and set, false if not.
	 */
	public static boolean runAway(EntityCreature target, EntityLivingBase caster, double distance){

		if(target.getDistance(caster) < distance){

			Vec3d Vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(target, (int)distance, (int)(distance/2),
					new Vec3d(caster.posX, caster.posY, caster.posZ));

			if(Vec3d == null){
				return false;

			}else{
				// In both cases it is necessary to check if the entity already has a path so it doesn't change
				// direction every tick, unless that path is towards the caster.
				// Path path = target.getNavigator().getPathToXYZ(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord);

				boolean flag = true;

				if(!target.getNavigator().noPath()){
					PathPoint point = target.getNavigator().getPath().getFinalPathPoint();
					if(point != null) flag = caster.getDistance(point.x, point.y, point.z) < distance;
				}
				// Has a built in mind trick effect because for whatever reason this makes it work with skeletons.
				target.setAttackTarget(null);

				if(flag) return target.getNavigator().tryMoveToXYZ(Vec3d.x, Vec3d.y, Vec3d.z, 1.25);// target.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
			}
		}

		return false;
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		if(event.getEntityLiving().isPotionActive(WizardryPotions.fear)
				&& event.getEntityLiving() instanceof EntityCreature){

			NBTTagCompound entityNBT = event.getEntityLiving().getEntityData();
			EntityCreature creature = (EntityCreature)event.getEntityLiving();

			if(entityNBT != null && entityNBT.hasUniqueId(NBT_KEY)){

				Entity caster = EntityUtils.getEntityByUUID(creature.world, entityNBT.getUniqueId(NBT_KEY));

				if(caster instanceof EntityLivingBase){
					double distance = BASE_AVOID_DISTANCE + AVOID_DISTANCE_PER_LEVEL
							* event.getEntityLiving().getActivePotionEffect(WizardryPotions.fear).getAmplifier();
					runAway(creature, (EntityLivingBase)caster, distance);
				}
			}
		}
	}

}
