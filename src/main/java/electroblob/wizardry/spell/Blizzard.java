package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Blizzard extends Spell {

	public Blizzard(){
		super(Tier.ADVANCED, 40, Element.ICE, "blizzard", SpellType.ATTACK, 100, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(20 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){
			if(!world.isRemote){
				double x = rayTrace.hitVec.x;
				double y = rayTrace.hitVec.y;
				double z = rayTrace.hitVec.z;
				EntityBlizzard blizzard = new EntityBlizzard(world, x, y + 0.5, z, caster,
						(int)(600 * modifiers.get(WizardryItems.duration_upgrade)),
						modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(blizzard);
			}
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				double x = target.posX;
				double y = target.posY;
				double z = target.posZ;
				EntityBlizzard blizzard = new EntityBlizzard(world, x, y + 0.5, z, caster,
						(int)(600 * modifiers.get(WizardryItems.duration_upgrade)),
						modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(blizzard);
			}
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
