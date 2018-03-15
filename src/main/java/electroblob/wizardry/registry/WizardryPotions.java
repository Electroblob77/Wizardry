package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.potion.PotionDecay;
import electroblob.wizardry.potion.PotionFrost;
import electroblob.wizardry.potion.PotionMagicEffect;
import electroblob.wizardry.potion.PotionMagicEffectParticles;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class responsible for defining, storing and registering all of wizardry's potion effects.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardryPotions {

	/* Interestingly, setting the colour to black stops the particles from rendering. This is great, however, the black
	 * colour then 'mixes' with other potions that are applied. Whilst this is a bit annoying, for the amount it is
	 * likely to get noticed it is certainly preferable to always making showParticles false, because now I can give the
	 * user the option (via commands) of having one of my potion effects without particles, and more importantly the
	 * potion effect HUD still gets displayed. TODO: Backport to 1.7.10, assuming it also works in that version. This
	 * means also changing whenever the potion effect is added such that it is NOT ambient. */

	public static final Potion frost = new PotionFrost(true, 0); // Colour was 0x38ddec (was arbitrary anyway)

	public static final Potion transience = new PotionMagicEffectParticles(false, 0, 0){
		@Override
		public void spawnCustomParticle(World world, double x, double y, double z){
			Wizardry.proxy.spawnParticle(WizardryParticleType.DUST, world, x, y, z, 0, 0, 0,
					(int)(16.0D / (Math.random() * 0.8D + 0.2D)), 0.8f, 0.8f, 1.0f);
		}
	}.setBeneficial(); // 0xffe89b

	public static final Potion fireskin = new PotionMagicEffectParticles(false, 0, 1){
		@Override
		public void spawnCustomParticle(World world, double x, double y, double z){
			world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
		}

		@Override
		public void performEffect(EntityLivingBase entitylivingbase, int strength){
			entitylivingbase.extinguish(); // Stops melee mobs that are on fire from setting the player on fire,
			// without allowing the player to actually stand in fire or swim in lava without taking damage.
		};
	}.setBeneficial(); // 0xff2f02

	public static final Potion ice_shroud = new PotionMagicEffectParticles(false, 0, 2){
		@Override
		public void spawnCustomParticle(World world, double x, double y, double z){
			float brightness = 0.5f + (world.rand.nextFloat() / 2);
			Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x, y, z, 0, 0, 0,
					48 + world.rand.nextInt(12), brightness, brightness + 0.1f, 1.0f, true, 0);
			Wizardry.proxy.spawnParticle(WizardryParticleType.SNOW, world, x, y, z, 0, -0.02, 0,
					40 + world.rand.nextInt(10));
		}
	}.setBeneficial(); // 0x52f1ff

	public static final Potion static_aura = new PotionMagicEffectParticles(false, 0, 3){
		@Override
		public void spawnCustomParticle(World world, double x, double y, double z){
			Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world, x, y, z, 0, 0, 0, 3);
		}
	}.setBeneficial(); // 0x0070ff

	public static final Potion decay = new PotionDecay(true, 0x3c006c);
	public static final Potion sixth_sense = new PotionMagicEffect(false, 0xc6ff01, 4).setBeneficial();
	public static final Potion arcane_jammer = new PotionMagicEffect(false, 0xcf4aa2, 5);
	public static final Potion mind_trick = new PotionMagicEffect(true, 0x601683, 6);
	public static final Potion mind_control = new PotionMagicEffect(true, 0x320b44, 7);
	public static final Potion font_of_mana = new PotionMagicEffect(false, 0xffe5bb, 8).setBeneficial();
	public static final Potion fear = new PotionMagicEffect(true, 0xbd0100, 9);

	/**
	 * Sets both the registry and unlocalised names of the given potion, then registers it with the given registry. Use
	 * this instead of {@link Potion#setRegistryName(String)} and {@link Potion#setUnlocalizedName(String)} during
	 * construction, for convenience and consistency.
	 * 
	 * @param registry The registry to register the given potion to.
	 * @param potion The potion to register.
	 * @param name The name of the potion, without the mod ID or the .name stuff. The registry name will be
	 *        {@code ebwizardry:[name]}. The unlocalised name will be {@code potion.ebwizardry:[name].name}.
	 */
	public static void registerPotion(IForgeRegistry<Potion> registry, Potion potion, String name){
		potion.setRegistryName(Wizardry.MODID, name);
		// For some reason, Potion#getName() doesn't prepend "potion." itself, so it has to be done here.
		potion.setPotionName("potion." + potion.getRegistryName().toString());
		registry.register(potion);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Potion> event){

		IForgeRegistry<Potion> registry = event.getRegistry();

		registerPotion(registry, frost, "frost");
		registerPotion(registry, transience, "transience");
		registerPotion(registry, fireskin, "fireskin");
		registerPotion(registry, ice_shroud, "ice_shroud");
		registerPotion(registry, static_aura, "static_aura");
		registerPotion(registry, decay, "decay");
		registerPotion(registry, sixth_sense, "sixth_sense");
		registerPotion(registry, arcane_jammer, "arcane_jammer");
		registerPotion(registry, mind_trick, "mind_trick");
		registerPotion(registry, mind_control, "mind_control");
		registerPotion(registry, font_of_mana, "font_of_mana");
		registerPotion(registry, fear, "fear");
	}

}