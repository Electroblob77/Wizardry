package electroblob.wizardry.potion;

import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Interface for potion effects that spawn custom particles instead of (or as well as) the vanilla 'swirly' particles.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
// TODO: Backport.
@Mod.EventBusSubscriber
public interface ICustomPotionParticles {

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

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){
		if(event.getEntityLiving().world.isRemote){
			// Behold the power of interfaces!
			for(PotionEffect effect : event.getEntityLiving().getActivePotionEffects()){

				if(effect.getPotion() instanceof ICustomPotionParticles && effect.doesShowParticles()){

					double x = event.getEntityLiving().posX
							+ (event.getEntityLiving().world.rand.nextDouble() - 0.5) * event.getEntityLiving().width;
					double y = event.getEntityLiving().getEntityBoundingBox().minY
							+ event.getEntityLiving().world.rand.nextDouble() * event.getEntityLiving().height;
					double z = event.getEntityLiving().posZ
							+ (event.getEntityLiving().world.rand.nextDouble() - 0.5) * event.getEntityLiving().width;

					((ICustomPotionParticles)effect.getPotion()).spawnCustomParticle(event.getEntityLiving().world, x,
							y, z);
				}
			}
		}
	}
}
