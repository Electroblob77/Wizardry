package electroblob.wizardry.spell;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import electroblob.wizardry.util.WizardryUtilities.Operations;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.UUID;

public class SummonSpiritHorse extends Spell {

	/** The string identifier for the potency attribute modifier. */
	private static final String POTENCY_ATTRIBUTE_MODIFIER = "potency";

	private static final IAttribute JUMP_STRENGTH;
	// Why is this protected? Doesn't that defeat the point of the attribute system?
	static {
		// Great, now I have to reflect into this class too.
		JUMP_STRENGTH = ObfuscationReflectionHelper.getPrivateValue(AbstractHorse.class, null, "field_110271_bv");
	}

	public static final IStoredVariable<UUID> UUID_KEY = IStoredVariable.StoredVariable.ofUUID("spiritHorseUUID", Persistence.ALWAYS);

	public SummonSpiritHorse(){
		super("summon_spirit_horse", EnumAction.BOW, false);
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

			Entity oldHorse = WizardryUtilities.getEntityByUUID(world, data.getVariable(UUID_KEY));

			if(oldHorse != null) oldHorse.setDead();

			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntitySpiritHorse horse = new EntitySpiritHorse(world);
			horse.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			horse.setTamedBy(caster);
			horse.setHorseSaddled(true);
			world.spawnEntity(horse);

			horse.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(
					new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.get(SpellModifiers.POTENCY) - 1, Operations.MULTIPLY_CUMULATIVE));
			// Jump strength increases ridiculously fast, so we're reducing the effect of the modifier by 75%
			horse.getEntityAttribute(JUMP_STRENGTH).applyModifier(new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER,
					modifiers.amplified(SpellModifiers.POTENCY, 0.25f) - 1, Operations.MULTIPLY_CUMULATIVE));

			data.setVariable(UUID_KEY, horse.getUniqueID());
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
