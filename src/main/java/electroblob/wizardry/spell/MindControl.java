package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntityWizard;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class MindControl extends Spell {

	/** The NBT tag name for storing the controlling entity's UUID in the target's tag compound. Defined here in case
	 * it changes. */
	public static final String NBT_KEY = "controllingEntity";

	public MindControl() {
		super(EnumTier.ADVANCED, 40, EnumElement.NECROMANCY, "mind_control", EnumSpellType.ATTACK, 150, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 8*rangeMultiplier);

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){

			Entity target = rayTrace.entityHit;

			if(!world.isRemote){
				if(target instanceof EntityPlayer || target instanceof IBossDisplayData || target instanceof INpc){
					// Adds a message saying that the player/boss entity/wizard resisted mind control
					if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));

				}else if(target instanceof EntityLiving){

					if(!MindControl.findMindControlTarget((EntityLiving)target, caster, world)){
						// If no valid target was found, this just acts like mind trick.
						// New AI
						((EntityLiving)target).setAttackTarget(null);
						// Old AI
						if(target instanceof EntityCreature) ((EntityCreature)target).setTarget(null);
					}

					NBTTagCompound entityNBT = target.getEntityData();
					if(entityNBT != null) entityNBT.setString(NBT_KEY, caster.getUniqueID().toString());

					((EntityLiving)target).addPotionEffect(new PotionEffect(Wizardry.mindControl.id, (int)(600*durationMultiplier), 0));
				}
			}else{
				for(int i=0; i<10; i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							WizardryUtilities.getEntityFeetPos(target) + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							WizardryUtilities.getEntityFeetPos(target) + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.2f, 0.04f, 0.25f);
				}
			}
			world.playSoundAtEntity(target, "wizardry:darkaura", 1.0f, 1.0f);
			caster.swingItem();
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		if(target != null){
			if(!world.isRemote){
				if(target instanceof EntityLiving && !(target instanceof IBossDisplayData)
						&& !(target instanceof EntityWizard)){

					if(!MindControl.findMindControlTarget((EntityLiving)target, caster, world)){
						// If no valid target was found, this just acts like mind trick.
						// New AI
						((EntityLiving)target).setAttackTarget(null);
						// Old AI
						if(target instanceof EntityCreature) ((EntityCreature)target).setTarget(null);
					}

					NBTTagCompound entityNBT = target.getEntityData();
					if(entityNBT != null) entityNBT.setString(NBT_KEY, caster.getUniqueID().toString());

					((EntityLiving)target).addPotionEffect(new PotionEffect(Wizardry.mindControl.id, (int)(600*durationMultiplier), 0));
				}
			}else{
				for(int i=0; i<10; i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							WizardryUtilities.getEntityFeetPos(target) + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							WizardryUtilities.getEntityFeetPos(target) + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.2f, 0.04f, 0.25f);
				}
			}
			world.playSoundAtEntity(target, "wizardry:darkaura", 1.0f, 1.0f);
			caster.swingItem();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

	/**
	 * Finds the nearest creature to the given target which it is allowed to attack according to the given caster
	 * and sets it as the target's attack target. Handles both new and old AI and takes follow range into account.
	 * Defined here so it can be used both in the spell itself and in the potion effect (event handler).
	 * @param target The entity being mind controlled
	 * @param caster The entity doing the controlling
	 * @param world The world to look for targets in
	 * @return True if a new target was found and set, false if not.
	 */
	public static boolean findMindControlTarget(EntityLiving target, EntityLivingBase caster, World world){

		// As of 1.1, this now uses the creature's follow range, like normal targeting. It also
		// no longer lasts until the creature dies; instead it is a potion effect which continues to
		// set the target until it wears off.
		List<EntityLivingBase> possibleTargets = WizardryUtilities.getEntitiesWithinRadius(
				((EntityLiving)target).getEntityAttribute(SharedMonsterAttributes.followRange).getAttributeValue(),
				target.posX, target.posY, target.posZ, world);

		possibleTargets.remove(target);

		EntityLivingBase newAITarget = null;

		for(EntityLivingBase possibleTarget : possibleTargets){
			if(WizardryUtilities.isValidTarget(caster, possibleTarget) && (newAITarget == null
					|| target.getDistanceToEntity(possibleTarget) < target.getDistanceToEntity(newAITarget))){
				newAITarget = possibleTarget;
			}
		}

		if(newAITarget != null){
			// Old AI
			if(target instanceof EntityCreature) ((EntityCreature)target).setTarget(newAITarget);
			// New AI - this seems not to work quite right; the entity appears to continue attacking this target
			// after it gets killed (not noticeable in survival since it will target the player again immediately.)
			((EntityLiving)target).setAttackTarget(newAITarget);

			return true;
		}

		return false;

	}


}
