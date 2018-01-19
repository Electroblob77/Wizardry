package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SummonSnowGolem extends Spell {

	public SummonSnowGolem() {
		super(Tier.APPRENTICE, 15, Element.ICE, "summon_snow_golem", SpellType.MINION, 20, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
		if(pos == null) return false;
		
		if(!world.isRemote){
			EntitySnowman snowman = new EntitySnowman(world);
			snowman.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
			world.spawnEntityInWorld(snowman);
		}
		for(int i=0; i<10; i++){
			double x1 = (double)((float)pos.getX() + world.rand.nextFloat()*2 - 1.0F);
			double y1 = (double)((float)pos.getY() + 0.5F + world.rand.nextFloat());
			double z1 = (double)((float)pos.getZ() + world.rand.nextFloat()*2 - 1.0F);
			if(world.isRemote){
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.6f, 0.6f, 1.0f);
			}
		}
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_HEAL, 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
		return true;
	}


}
