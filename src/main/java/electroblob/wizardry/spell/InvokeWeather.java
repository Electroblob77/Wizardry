package electroblob.wizardry.spell;

import java.util.Random;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class InvokeWeather extends Spell {

	public InvokeWeather() {
		super(EnumTier.ADVANCED, 30, EnumElement.LIGHTNING, "invoke_weather", EnumSpellType.UTILITY, 100, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		if(caster.dimension == 0){
			if(!world.isRemote){
				if(world.isRaining()){
					caster.addChatComponentMessage(new ChatComponentTranslation("spell.invoke_weather.sun"));
					world.getWorldInfo().setRainTime(0);
					world.getWorldInfo().setRaining(false);
				}else{
					caster.addChatComponentMessage(new ChatComponentTranslation("spell.invoke_weather.rain"));
					world.getWorldInfo().setRainTime((300 + (new Random()).nextInt(600)) * 20);
					world.getWorldInfo().setRaining(true);
				}
			}
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.5f, 0.7f, 1.0f);
				}
			}
			world.playSoundAtEntity(caster, "ambient.weather.thunder", 0.5F, 1.0f);
			return true;
		}
		return false;
	}


}
