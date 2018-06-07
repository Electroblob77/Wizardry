package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntityDecoy;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class Decoy extends Spell {

	public Decoy() {
		super(EnumTier.ADVANCED, 40, EnumElement.SORCERY, "decoy", EnumSpellType.UTILITY, 200, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		// Uses the synchronised entity id to ensure it is consistent on client and server, but not always the same.
		double splitSpeed = caster.getEntityId() % 2 == 0 ? 0.3 : -0.3;

		if(!world.isRemote){
			EntityDecoy decoy = new EntityDecoy(world, caster.posX, caster.posY, caster.posZ, caster, 600);
			decoy.setLocationAndAngles(caster.posX, caster.posY, caster.posZ, caster.rotationYaw, caster.rotationPitch);
			decoy.addVelocity(-caster.getLookVec().zCoord*splitSpeed, 0, caster.getLookVec().xCoord*splitSpeed);
			// Ignores the show names setting, since this would allow a player to easily detect a decoy
			decoy.setCustomNameTag(caster.getCommandSenderName());
			world.spawnEntityInWorld(decoy);

			// Tricks any mobs that are targeting the caster into targeting the decoy instead.
			for(EntityLiving creature : WizardryUtilities.getEntitiesWithinRadius(16, caster.posX, caster.posY, caster.posZ, world, EntityLiving.class)){
				// More likely to trick mobs the higher the damage multiplier. Starts off at 50%.
				if(world.rand.nextInt((int)(6*damageMultiplier)) < 3){
					// New AI
					if(creature.getAttackTarget() == caster) creature.setAttackTarget(decoy);
					// Old AI
					if(creature instanceof EntityCreature && ((EntityCreature)creature).getEntityToAttack() == caster) ((EntityCreature)creature).setTarget(decoy);
				}
			}
		}

		caster.addVelocity(caster.getLookVec().zCoord*splitSpeed, 0, -caster.getLookVec().xCoord*splitSpeed);

		world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		double splitSpeed = world.rand.nextBoolean() ? 0.3 : -0.3;

		if(!world.isRemote){
			EntityDecoy decoy = new EntityDecoy(world, caster.posX, caster.posY, caster.posZ, caster, 600);
			decoy.setLocationAndAngles(caster.posX, caster.posY, caster.posZ, caster.rotationYaw, caster.rotationPitch);
			decoy.addVelocity(-caster.getLookVec().zCoord*splitSpeed, 0, caster.getLookVec().xCoord*splitSpeed);
			world.spawnEntityInWorld(decoy);

			// Tricks any mobs that are targeting the caster into targeting the decoy instead.
			for(EntityLiving creature : WizardryUtilities.getEntitiesWithinRadius(16, caster.posX, caster.posY, caster.posZ, world, EntityLiving.class)){
				// More likely to trick mobs the higher the damage multiplier. Starts off at 50%.
				if(world.rand.nextInt((int)(6*damageMultiplier)) < 3){
					// New AI
					if(creature.getAttackTarget() == caster) creature.setAttackTarget(decoy);
					// Old AI
					if(creature instanceof EntityCreature && ((EntityCreature)creature).getEntityToAttack() == caster) ((EntityCreature)creature).setTarget(decoy);
				}
			}
		}
		caster.addVelocity(caster.getLookVec().zCoord*splitSpeed, 0, -caster.getLookVec().xCoord*splitSpeed);

		world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
