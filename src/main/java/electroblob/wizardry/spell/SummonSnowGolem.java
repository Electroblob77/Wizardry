package electroblob.wizardry.spell;

import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonSnowGolem extends Spell {

	public SummonSnowGolem(){
		super("summon_snow_golem", EnumAction.BOW, false);
		this.soundValues(1, 1, 0.4f);
		addProperties(SpellMinion.SUMMON_RADIUS);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
		if(pos == null) return false;

		if(!world.isRemote){
			
			EntitySnowman snowman = new EntitySnowman(world);
			snowman.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntity(snowman);
			
		}else{
			
			for(int i=0; i<10; i++){
				double x = pos.getX() + world.rand.nextDouble() * 2 - 1;
				double y = pos.getY() + 0.5 + world.rand.nextDouble();
				double z = pos.getZ() + world.rand.nextDouble() * 2 - 1;
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(0.6f, 0.6f, 1).spawn(world);
			}
		}
		
		playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
