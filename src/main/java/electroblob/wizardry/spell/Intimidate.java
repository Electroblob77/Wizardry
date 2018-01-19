package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Intimidate extends Spell {

	/** The NBT tag name for storing the feared entity's UUID in the target's tag compound. Defined here in case
	 * it changes. */
	public static final String NBT_KEY = "fearedEntity";

	public Intimidate() {
		super(Tier.APPRENTICE, 20, Element.NECROMANCY, "intimidate", SpellType.ATTACK, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		if(!world.isRemote){

			List<EntityCreature> entities = WizardryUtilities.getEntitiesWithinRadius(8*modifiers.get(WizardryItems.range_upgrade), caster.posX, caster.posY, caster.posZ, world, EntityCreature.class);

			for(EntityCreature target : entities){

				runAway(target, caster);

				NBTTagCompound entityNBT = target.getEntityData();
				if(entityNBT != null) entityNBT.setUniqueId(NBT_KEY, caster.getUniqueID());

				((EntityLiving)target).addPotionEffect(new PotionEffect(WizardryPotions.fear, (int)(600*modifiers.get(WizardryItems.duration_upgrade)), 0));

			}
			
		}else{
			for(int i=0; i<30; i++){
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, caster.posX - 1 + world.rand.nextDouble()*2,
						caster.getEntityBoundingBox().minY + 1.5 + world.rand.nextDouble()*0.5,
						caster.posZ - 1 + world.rand.nextDouble()*2,
						0, 0, 0, 0, 0.9f, 0.1f, 0.0f);
			}
		}
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_ENDERDRAGON_GROWL, 1.0f, 1.0f);
		return true;
	}

	/**
	 * Finds a random position away from the caster and sets the given target's AI path to that location. Defined here
	 * so it can be used both in the spell itself and in the potion effect (event handler).
	 * @param target The entity running away
	 * @param caster The entity that is being run away from
	 * @return True if a new path was found and set, false if not.
	 */
	public static boolean runAway(EntityCreature target, EntityLivingBase caster){

		if(target.getDistanceToEntity(caster) < 16){

			Vec3d Vec3d = RandomPositionGenerator.findRandomTargetBlockAwayFrom(target, 16, 7, new Vec3d(caster.posX, caster.posY, caster.posZ));

			if (Vec3d == null){
				return false;

			}else{
				// In both cases it is necessary to check if the entity already has a path so it doesn't change direction
				// every tick, unless that path is towards the caster.
				//Path path = target.getNavigator().getPathToXYZ(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord);

				boolean flag = true;

				if(!target.getNavigator().noPath()){
					PathPoint point = target.getNavigator().getPath().getFinalPathPoint();
					if(point != null) flag = caster.getDistance(point.xCoord, point.yCoord, point.zCoord) < 16;
				}
				// Has a built in mind trick effect because for whatever reason this makes it work with skeletons.
				target.setAttackTarget(null);

				if(flag) return target.getNavigator().tryMoveToXYZ(Vec3d.xCoord, Vec3d.yCoord, Vec3d.zCoord, 1.25);//target.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
			}
		}

		return false;
	}

}
