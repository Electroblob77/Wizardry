package electroblob.wizardry.spell;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Random;

public class InvokeWeather extends Spell {

	public static final String THUNDERSTORM_CHANCE = "thunderstorm_chance";

	public InvokeWeather(){
		super("invoke_weather", SpellActions.POINT_UP, false);
		addProperties(THUNDERSTORM_CHANCE);
		soundValues(0.5f, 1, 0);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.dimension == 0){

			if(!world.isRemote){
				
				int standardWeatherTime = (300 + (new Random()).nextInt(600)) * 20;
				
				if(world.isRaining()){
					caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".sun"), true);
					world.getWorldInfo().setCleanWeatherTime(standardWeatherTime);
					world.getWorldInfo().setRainTime(0);
					world.getWorldInfo().setThunderTime(0);
					world.getWorldInfo().setRaining(false);
					world.getWorldInfo().setThundering(false);
				}else{
					caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".rain"), true);
					world.getWorldInfo().setCleanWeatherTime(0);
					world.getWorldInfo().setRainTime(standardWeatherTime);
					world.getWorldInfo().setThunderTime(standardWeatherTime);
					world.getWorldInfo().setRaining(true);
					// Thunderstorm is guaranteed if the caster has a bottled thundercloud charm equipped
					world.getWorldInfo().setThundering(ItemArtefact.isArtefactActive(caster, WizardryItems.charm_storm)
							|| world.rand.nextFloat() < getProperty(THUNDERSTORM_CHANCE).floatValue());
				}
			}

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x = caster.posX + world.rand.nextDouble() * 2 - 1;
					double y = caster.posY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
					double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
					ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, 0.1, 0).clr(0.5f, 0.7f, 1).spawn(world);
				}
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;
		}
		
		return false;
	}

}
