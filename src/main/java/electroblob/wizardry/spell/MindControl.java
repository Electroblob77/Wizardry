package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class MindControl extends Spell {

	/**
	 * The NBT tag name for storing the controlling entity's UUID in the target's tag compound. Defined here in case it
	 * changes.
	 */
	public static final String NBT_KEY = "controllingEntity";

	public MindControl(){
		super(Tier.ADVANCED, 40, Element.NECROMANCY, "mind_control", SpellType.ATTACK, 150, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				8 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.entityHit != null && WizardryUtilities.isLiving(rayTrace.entityHit)){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(!world.isRemote){
				if(!canControl(target)){
					// Adds a message saying that the player/boss entity/wizard resisted mind control
					if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist",
							target.getName(), this.getNameForTranslationFormatted()));

				}else if(target instanceof EntityLiving){

					if(!MindControl.findMindControlTarget((EntityLiving)target, caster, world)){
						// If no valid target was found, this just acts like mind trick.
						((EntityLiving)target).setAttackTarget(null);
					}

					NBTTagCompound entityNBT = target.getEntityData();
					if(entityNBT != null) entityNBT.setUniqueId(NBT_KEY, caster.getUniqueID());

					((EntityLiving)target).addPotionEffect(new PotionEffect(WizardryPotions.mind_control,
							(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
			}else{
				for(int i = 0; i < 10; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
							target.posX - 0.25 + world.rand.nextDouble() * 0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25
									+ world.rand.nextDouble() * 0.5,
							target.posZ - 0.25 + world.rand.nextDouble() * 0.5, 0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
							target.posX - 0.25 + world.rand.nextDouble() * 0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25
									+ world.rand.nextDouble() * 0.5,
							target.posZ - 0.25 + world.rand.nextDouble() * 0.5, 0, 0, 0, 0, 0.2f, 0.04f, 0.25f);
				}
			}
			target.playSound(WizardrySounds.SPELL_SUMMONING, 1.0f, 1.0f);
			caster.swingArm(hand);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			if(!world.isRemote){
				if(canControl(target)){

					if(!MindControl.findMindControlTarget((EntityLiving)target, caster, world)){
						// If no valid target was found, this just acts like mind trick.
						((EntityLiving)target).setAttackTarget(null);
					}

					NBTTagCompound entityNBT = target.getEntityData();
					if(entityNBT != null) entityNBT.setUniqueId(NBT_KEY, caster.getUniqueID());

					((EntityLiving)target).addPotionEffect(new PotionEffect(WizardryPotions.mind_control,
							(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
			}else{
				for(int i = 0; i < 10; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
							target.posX - 0.25 + world.rand.nextDouble() * 0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25
									+ world.rand.nextDouble() * 0.5,
							target.posZ - 0.25 + world.rand.nextDouble() * 0.5, 0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world,
							target.posX - 0.25 + world.rand.nextDouble() * 0.5,
							target.getEntityBoundingBox().minY + target.getEyeHeight() - 0.25
									+ world.rand.nextDouble() * 0.5,
							target.posZ - 0.25 + world.rand.nextDouble() * 0.5, 0, 0, 0, 0, 0.2f, 0.04f, 0.25f);
				}
			}
			target.playSound(WizardrySounds.SPELL_SUMMONING, 1.0f, 1.0f);
			caster.swingArm(hand);
			return true;
		}
		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

	/** Returns true if the given entity can be mind controlled (i.e. is not a player, npc, evil wizard or boss). */
	public static boolean canControl(EntityLivingBase target){
		return target instanceof EntityLiving && target.isNonBoss() && !(target instanceof INpc)
				&& !(target instanceof EntityEvilWizard);
	}

	/**
	 * Finds the nearest creature to the given target which it is allowed to attack according to the given caster and
	 * sets it as the target's attack target. Handles both new and old AI and takes follow range into account. Defined
	 * here so it can be used both in the spell itself and in the potion effect (event handler).
	 * 
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
				((EntityLiving)target).getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).getAttributeValue(),
				target.posX, target.posY, target.posZ, world);

		possibleTargets.remove(target);
		possibleTargets.removeIf(e -> e instanceof EntityArmorStand);

		EntityLivingBase newAITarget = null;

		for(EntityLivingBase possibleTarget : possibleTargets){
			if(WizardryUtilities.isValidTarget(caster, possibleTarget) && (newAITarget == null
					|| target.getDistanceToEntity(possibleTarget) < target.getDistanceToEntity(newAITarget))){
				newAITarget = possibleTarget;
			}
		}

		if(newAITarget != null){
			// From 1.7.10 - this seems not to work quite right; the entity appears to continue attacking this target
			// after it gets killed (not noticeable in survival since it will target the player again immediately.)
			((EntityLiving)target).setAttackTarget(newAITarget);

			return true;
		}

		return false;

	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){
		// This was added because something got changed in the AI classes which means LivingSetAttackTargetEvent doesn't
		// get fired when I want it to... so I'm firing it myself.
		if(event.getEntityLiving().isPotionActive(WizardryPotions.mind_control)
				&& event.getEntityLiving() instanceof EntityLiving
				&& ((EntityLiving)event.getEntityLiving()).getAttackTarget() != null
				&& !((EntityLiving)event.getEntityLiving()).getAttackTarget().isEntityAlive())
			((EntityLiving)event.getEntityLiving()).setAttackTarget(null); // Causes the event to be fired
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		
		if(event.getTarget() == null) return; // Prevents infinite loops with mind trick

		if(event.getEntityLiving().isPotionActive(WizardryPotions.mind_control)
				&& MindControl.canControl(event.getEntityLiving())){

			NBTTagCompound entityNBT = event.getEntityLiving().getEntityData();

			if(entityNBT != null && entityNBT.hasUniqueId(MindControl.NBT_KEY)){

				Entity caster = WizardryUtilities.getEntityByUUID(event.getEntity().world,
						entityNBT.getUniqueId(MindControl.NBT_KEY));

				// If the target that the event tried to set is already a valid mind control target, nothing happens.
				if(WizardryUtilities.isValidTarget(caster, event.getTarget())) return;

				if(caster instanceof EntityLivingBase){

					if(MindControl.findMindControlTarget((EntityLiving)event.getEntityLiving(),
							(EntityLivingBase)caster, event.getEntity().world)){
						// If it worked, skip setting the target to null.
						return;
					}
				}
			}
			// If the caster couldn't be found or no valid target was found, this just acts like mind trick.
			((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
		}
	}

}