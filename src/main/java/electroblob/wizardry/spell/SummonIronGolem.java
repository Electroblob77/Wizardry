package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonIronGolem extends Spell {

	public SummonIronGolem(){
		super("summon_iron_golem", SpellActions.SUMMON, false);
		addProperties(SpellMinion.SUMMON_RADIUS);
		soundValues(1, 1.1f, 0.2f);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, getProperty(SpellMinion.SUMMON_RADIUS).intValue(),
				getProperty(SpellMinion.SUMMON_RADIUS).intValue());

		if(pos == null) return false;

		if(!world.isRemote){
			
			EntityIronGolem golem = new EntityIronGolem(world);
			golem.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			golem.setPlayerCreated(true);
			world.spawnEntity(golem);
			
		}else{
			
			for(int i=0; i<10; i++){
				double x = pos.getX() + world.rand.nextDouble() * 2 - 1;
				double y = pos.getY() + 0.5 + world.rand.nextDouble();
				double z = pos.getZ() + world.rand.nextDouble() * 2 - 1;
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(0.6f, 0.6f, 1).spawn(world);
			}
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
