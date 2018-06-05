package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonIronGolem extends Spell {

	public SummonIronGolem(){
		super(Tier.MASTER, 175, Element.SORCERY, "summon_iron_golem", SpellType.MINION, 400, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
		if(pos == null) return false;

		if(!world.isRemote){
			EntityIronGolem golem = new EntityIronGolem(world);
			golem.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(golem);
		}

		for(int i = 0; i < 10; i++){
			double x1 = (double)((float)pos.getX() + world.rand.nextFloat() * 2 - 1.0F);
			double y1 = (double)((float)pos.getY() + 0.5F + world.rand.nextFloat());
			double z1 = (double)((float)pos.getZ() + world.rand.nextFloat() * 2 - 1.0F);
			if(world.isRemote){
				Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, 0, 0, 0,
						48 + world.rand.nextInt(12), 0.6f, 0.6f, 1.0f);
			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SPAWN, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

}
