package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Intimidate extends Spell {

	/** The NBT tag name for storing the feared entity's UUID in the target's tag compound. Defined here in case
	 * it changes. */
	public static final String NBT_KEY = "fearedEntity";

	public Intimidate() {
		super(EnumTier.APPRENTICE, 20, EnumElement.NECROMANCY, "intimidate", EnumSpellType.ATTACK, 100, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		if(!world.isRemote){

			List<EntityCreature> entities = WizardryUtilities.getEntitiesWithinRadius(8*rangeMultiplier, caster.posX, caster.posY, caster.posZ, world, EntityCreature.class);

			for(EntityCreature target : entities){

				runAway(target, caster);

				NBTTagCompound entityNBT = target.getEntityData();
				if(entityNBT != null) entityNBT.setString(NBT_KEY, caster.getUniqueID().toString());

				((EntityLiving)target).addPotionEffect(new PotionEffect(Wizardry.fear.id, (int)(600*durationMultiplier), 0));

			}
			
		}else{
			for(int i=0; i<30; i++){
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, caster.posX - 1 + world.rand.nextDouble()*2,
						caster.boundingBox.minY + 1.5 + world.rand.nextDouble()*0.5,
						caster.posZ - 1 + world.rand.nextDouble()*2,
						0, 0, 0, 0, 0.9f, 0.1f, 0.0f);
			}
		}
		world.playSoundAtEntity(caster, "mob.enderdragon.growl", 1.0f, 1.0f);
		return true;
	}

	/**
	 * Finds a random position away from the caster and sets the given target's AI path to that location. Handles both
	 * new AI and old AI. Defined here so it can be used both in the spell itself and in the potion effect (event handler).
	 * @param target The entity running away
	 * @param caster The entity that is being run away from
	 * @return True if a new path was found and set, false if not.
	 */
	public static boolean runAway(EntityCreature target, EntityLivingBase caster){

		if(target.getDistanceToEntity(caster) < 16){

			Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockAwayFrom(target, 16, 7, Vec3.createVectorHelper(caster.posX, caster.posY, caster.posZ));

			if (vec3 == null){
				return false;

			}else{
				// In both cases it is necessary to check if the entity already has a path so it doesn't change direction
				// every tick, unless that path is towards the caster.
				PathEntity path = target.getNavigator().getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);

				// Old AI
				if(!target.hasPath() || target.getEntityToAttack() == caster) target.setPathToEntity(path);
				target.setTarget(null);

				// New AI
				boolean flag = true;

				if(!target.getNavigator().noPath()){
					PathPoint point = target.getNavigator().getPath().getFinalPathPoint();
					if(point != null) flag = caster.getDistance(point.xCoord, point.yCoord, point.zCoord) < 16;
				}
				// Has a built in mind trick effect because for whatever reason this makes it work with skeletons.
				target.setAttackTarget(null);

				if(flag) return target.getNavigator().tryMoveToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord, 1.25);//target.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
			}
		}

		return false;
	}

}
