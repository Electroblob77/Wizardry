package electroblob.wizardry.spell;

import java.util.Random;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class InvokeWeather extends Spell {

	public InvokeWeather(){
		super(Tier.ADVANCED, 30, Element.LIGHTNING, "invoke_weather", SpellType.UTILITY, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.dimension == 0){

			if(!world.isRemote){
				// TODO: Backport these changes.
				int standardWeatherTime = (300 + (new Random()).nextInt(600)) * 20;
				if(world.isRaining()){
					caster.sendMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".sun"));
					world.getWorldInfo().setCleanWeatherTime(standardWeatherTime);
					world.getWorldInfo().setRainTime(0);
					world.getWorldInfo().setThunderTime(0);
					world.getWorldInfo().setRaining(false);
					world.getWorldInfo().setThundering(false);
				}else{
					caster.sendMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".rain"));
					world.getWorldInfo().setCleanWeatherTime(0);
					world.getWorldInfo().setRainTime(standardWeatherTime);
					world.getWorldInfo().setThunderTime(standardWeatherTime);
					world.getWorldInfo().setRaining(true);
					// 1/3 chance for a thunderstorm
					world.getWorldInfo().setThundering(world.rand.nextInt(3) == 0);
				}
			}

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat() * 2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F
							+ world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0,
							48 + world.rand.nextInt(12), 0.5f, 0.7f, 1.0f);
				}
			}

			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5F, 1.0f);
			return true;
		}
		return false;
	}

}
