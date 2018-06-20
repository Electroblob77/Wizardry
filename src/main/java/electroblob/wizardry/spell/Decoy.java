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
		super("decoy", Tier.ADVANCED, Element.SORCERY, SpellType.UTILITY, 40, 200, EnumAction.BOW, false);
	}

	@Override public boolean canBeCastByNPCs(){ return true; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		// Uses the synchronised entity id to ensure it is consistent on client and server, but not always the same.
		double splitSpeed = caster.getEntityId() % 2 == 0 ? 0.3 : -0.3;
		spawnDecoy(world, caster, modifiers, splitSpeed);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}
	

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		// Determines whether the caster moves left and the decoy moves right, or vice versa.
		double splitSpeed = world.rand.nextBoolean() ? 0.3 : -0.3;
		spawnDecoy(world, caster, modifiers, splitSpeed);
		caster.playSound(WizardrySounds.SPELL_CONJURATION, 1.0F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}
	
	private void spawnDecoy(World world, EntityLivingBase caster, SpellModifiers modifiers, double splitSpeed){

		if(!world.isRemote){
			EntityDecoy decoy = new EntityDecoy(world);
			decoy.setCaster(caster);
			decoy.setLifetime(600);
			decoy.setLocationAndAngles(caster.posX, caster.posY, caster.posZ, caster.rotationYaw, caster.rotationPitch);
			decoy.addVelocity(-caster.getLookVec().z * splitSpeed, 0, caster.getLookVec().x * splitSpeed);
			// Ignores the show names setting, since this would allow a player to easily detect a decoy
			// Instead, a decoy player has its caster's name tag shown permanently and non-player decoys have nothing
			if(caster instanceof EntityPlayer) decoy.setCustomNameTag(caster.getName());
			world.spawnEntity(decoy);

			// Tricks any mobs that are targeting the caster into targeting the decoy instead.
			for(EntityLiving creature : WizardryUtilities.getEntitiesWithinRadius(16, caster.posX, caster.posY,
					caster.posZ, world, EntityLiving.class)){
				// More likely to trick mobs the higher the damage multiplier. Starts off at 50%.
				if(world.rand.nextInt((int)(6 * modifiers.get(SpellModifiers.POTENCY))) < 3){
					if(creature.getAttackTarget() == caster) creature.setAttackTarget(decoy);
				}
			}
		}

		caster.addVelocity(caster.getLookVec().z * splitSpeed, 0, -caster.getLookVec().x * splitSpeed);
	}

}
