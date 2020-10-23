package electroblob.wizardry.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.stream.Collectors;

/**
 * Interface for potion effects that spawn custom particles instead of (or as well as) the vanilla 'swirly' particles.<br>
 * <br>
 * To hide the vanilla 'swirly' particles, set the potion's liquid colour to 0 (black). By default, potions that
 * implement this interface do not mix their colour with other potions.<br>
 * <br>
 * Potions that implement this interface also implement {@link ISyncedPotion} since any custom particles require syncing
 * to disappear correctly when the effect ends; if syncing is not required, override
 * {@link ISyncedPotion#shouldSync(EntityLivingBase)} to return false.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
@Mod.EventBusSubscriber
public interface ICustomPotionParticles extends ISyncedPotion {

	/**
	 * Called from the event handler to spawn a <b>single</b> custom potion particle. To get an instance of
	 * <code>Random</code> inside this method, use <code>world.rand</code>.
	 * 
	 * @param world The world to spawn the particle in.
	 * @param x The x coordinate of the particle, already set to a random value within the entity's bounding box.
	 * @param y The y coordinate of the particle, already set to a random value within the entity's bounding box.
	 * @param z The z coordinate of the particle, already set to a random value within the entity's bounding box.
	 */
	void spawnCustomParticle(World world, double x, double y, double z);
	
	/** Returns true if this potion should mix its colour with others, false if not. Defaults to false. */
	default boolean shouldMixColour(){
		return false;
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){
		if(event.getEntityLiving().world.isRemote){
			// Behold the power of interfaces!
			for(PotionEffect effect : event.getEntityLiving().getActivePotionEffects()){

				if(effect.getPotion() instanceof ICustomPotionParticles && effect.doesShowParticles()){

					double x = event.getEntityLiving().posX
							+ (event.getEntityLiving().world.rand.nextDouble() - 0.5) * event.getEntityLiving().width;
					double y = event.getEntityLiving().posY
							+ event.getEntityLiving().world.rand.nextDouble() * event.getEntityLiving().height;
					double z = event.getEntityLiving().posZ
							+ (event.getEntityLiving().world.rand.nextDouble() - 0.5) * event.getEntityLiving().width;

					((ICustomPotionParticles)effect.getPotion()).spawnCustomParticle(event.getEntityLiving().world, x, y, z);
				}
			}
		}
	}
	
	@SubscribeEvent
	// Prevents instances of this interface for which shouldMixColour() returns false from affecting mixed potion colours
	public static void onPotionColourCalculationEvent(PotionColorCalculationEvent event){
		event.setColor(PotionUtils.getPotionColorFromEffectList(event.getEffects().stream().filter(
				p -> !(p instanceof ICustomPotionParticles && !((ICustomPotionParticles)p).shouldMixColour()))
				.collect(Collectors.toList())));
	}

}
