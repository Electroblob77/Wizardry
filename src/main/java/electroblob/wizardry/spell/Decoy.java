package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Decoy extends Spell {

	public Decoy(){
		super(Tier.ADVANCED, 40, Element.SORCERY, "decoy", SpellType.UTILITY, 200, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		// Uses the synchronised entity id to ensure it is consistent on client and server, but not always the same.
		double splitSpeed = caster.getEntityId() % 2 == 0 ? 0.3 : -0.3;

		if(!world.isRemote){
			EntityDecoy decoy = new EntityDecoy(world, caster.posX, caster.posY, caster.posZ, caster, 600);
			decoy.setLocationAndAngles(caster.posX, caster.posY, caster.posZ, caster.rotationYaw, caster.rotationPitch);
			decoy.addVelocity(-caster.getLookVec().zCoord * splitSpeed, 0, caster.getLookVec().xCoord * splitSpeed);
			// Ignores the show names setting, since this would allow a player to easily detect a decoy
			decoy.setCustomNameTag(caster.getName());
			world.spawnEntity(decoy);

			// Tricks any mobs that are targeting the caster into targeting the decoy instead.
			for(EntityLiving creature : WizardryUtilities.getEntitiesWithinRadius(16, caster.posX, caster.posY,
					caster.posZ, world, EntityLiving.class)){
				// More likely to trick mobs the higher the damage multiplier. Starts off at 50%.
				if(world.rand.nextInt((int)(6 * modifiers.get(SpellModifiers.DAMAGE))) < 3){
					if(creature.getAttackTarget() == caster) creature.setAttackTarget(decoy);
				}
			}
		}

		caster.addVelocity(caster.getLookVec().zCoord * splitSpeed, 0, -caster.getLookVec().xCoord * splitSpeed);

		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0F,
				0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		double splitSpeed = world.rand.nextBoolean() ? 0.3 : -0.3;

		if(!world.isRemote){
			EntityDecoy decoy = new EntityDecoy(world, caster.posX, caster.posY, caster.posZ, caster, 600);
			decoy.setLocationAndAngles(caster.posX, caster.posY, caster.posZ, caster.rotationYaw, caster.rotationPitch);
			decoy.addVelocity(-caster.getLookVec().zCoord * splitSpeed, 0, caster.getLookVec().xCoord * splitSpeed);
			world.spawnEntity(decoy);

			// Tricks any mobs that are targeting the caster into targeting the decoy instead.
			for(EntityLiving creature : WizardryUtilities.getEntitiesWithinRadius(16, caster.posX, caster.posY,
					caster.posZ, world, EntityLiving.class)){
				// More likely to trick mobs the higher the damage multiplier. Starts off at 50%.
				if(world.rand.nextInt((int)(6 * modifiers.get(SpellModifiers.DAMAGE))) < 3){
					if(creature.getAttackTarget() == caster) creature.setAttackTarget(decoy);
				}
			}
		}
		caster.addVelocity(caster.getLookVec().zCoord * splitSpeed, 0, -caster.getLookVec().xCoord * splitSpeed);

		caster.playSound(WizardrySounds.SPELL_CONJURATION, 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
