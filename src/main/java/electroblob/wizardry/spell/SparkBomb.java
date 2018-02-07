package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.projectile.EntitySparkBomb;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class SparkBomb extends Spell {

	public SparkBomb(){
		super(Tier.APPRENTICE, 15, Element.LIGHTNING, "spark_bomb", SpellType.ATTACK, 25, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			EntitySparkBomb sparkBomb = new EntitySparkBomb(world, caster, modifiers.get(SpellModifiers.DAMAGE),
					modifiers.get(WizardryItems.blast_upgrade));
			world.spawnEntity(sparkBomb);
		}

		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_SNOWBALL_THROW, 0.5F,
				0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				EntitySparkBomb sparkBomb = new EntitySparkBomb(world, caster, modifiers.get(SpellModifiers.DAMAGE),
						modifiers.get(WizardryItems.blast_upgrade));
				sparkBomb.directTowards(target, 1.5f);
				world.spawnEntity(sparkBomb);
			}

			caster.swingArm(hand);
			caster.playSound(SoundEvents.ENTITY_SNOWBALL_THROW, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
