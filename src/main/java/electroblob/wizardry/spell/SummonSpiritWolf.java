package electroblob.wizardry.spell;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import electroblob.wizardry.util.WizardryUtilities.Operations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class SummonSpiritWolf extends Spell {

	/** The string identifier for the potency attribute modifier. */
	private static final String POTENCY_ATTRIBUTE_MODIFIER = "potency";

	public static final IStoredVariable<UUID> UUID_KEY = IStoredVariable.StoredVariable.ofUUID("spiritWolfUUID", Persistence.ALWAYS);

	public SummonSpiritWolf(){
		super("summon_spirit_wolf", EnumAction.BOW, false);
		addProperties(SpellMinion.SUMMON_RADIUS);
		soundValues(0.7f, 1.2f, 0.4f);
		WizardData.registerStoredVariables(UUID_KEY);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData data = WizardData.get(caster);

		if(!world.isRemote){

			Entity oldWolf = WizardryUtilities.getEntityByUUID(world, data.getVariable(UUID_KEY));

			if(oldWolf != null) oldWolf.setDead();

			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntitySpiritWolf wolf = new EntitySpiritWolf(world);
			wolf.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			wolf.setTamed(true);
			wolf.setOwnerId(caster.getUniqueID());
			// Potency gives the wolf more strength AND more health
			wolf.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(
					new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.get(SpellModifiers.POTENCY) - 1, Operations.MULTIPLY_CUMULATIVE));
			wolf.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(
					new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.amplified(SpellModifiers.POTENCY, 1.5f) - 1, Operations.MULTIPLY_CUMULATIVE));
			wolf.setHealth(wolf.getMaxHealth());

			world.spawnEntity(wolf);

			data.setVariable(UUID_KEY, wolf.getUniqueID());
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;

	}

}
