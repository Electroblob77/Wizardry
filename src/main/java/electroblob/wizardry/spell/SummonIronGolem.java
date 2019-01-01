package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
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
		super("summon_iron_golem", Tier.MASTER, Element.SORCERY, SpellType.MINION, 175, 400, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
		if(pos == null) return false;

		if(!world.isRemote){
			
			EntityIronGolem golem = new EntityIronGolem(world);
			golem.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(golem);
			
		}else{
			
			for(int i=0; i<10; i++){
				double x = pos.getX() + world.rand.nextDouble() * 2 - 1;
				double y = pos.getY() + 0.5 + world.rand.nextDouble();
				double z = pos.getZ() + world.rand.nextDouble() * 2 - 1;
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(0.6f, 0.6f, 1).spawn(world);
			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SPAWN, 1, 1 + 0.2f * world.rand.nextFloat());
		return true;
	}

}
