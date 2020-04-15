package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.potion.*;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

/**
 * Class responsible for defining, storing and registering all of wizardry's potion effects.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@ObjectHolder(Wizardry.MODID)
@Mod.EventBusSubscriber
public final class WizardryPotions {

	private WizardryPotions(){} // No instances!

	@Nonnull
	@SuppressWarnings("ConstantConditions")
	private static <T> T placeholder(){ return null; }

	public static final Potion frost = placeholder();
	public static final Potion transience = placeholder();
	public static final Potion fireskin = placeholder();
	public static final Potion ice_shroud = placeholder();
	public static final Potion static_aura = placeholder();
	public static final Potion decay = placeholder();
	public static final Potion sixth_sense = placeholder();
	public static final Potion arcane_jammer = placeholder();
	public static final Potion mind_trick = placeholder();
	public static final Potion mind_control = placeholder();
	public static final Potion font_of_mana = placeholder();
	public static final Potion fear = placeholder();
	public static final Potion curse_of_soulbinding = placeholder();
	public static final Potion paralysis = placeholder();
	public static final Potion muffle = placeholder();
	public static final Potion ward = placeholder();
	public static final Potion slow_time = placeholder();
	public static final Potion empowerment = placeholder();
	public static final Potion curse_of_enfeeblement = placeholder();
	public static final Potion curse_of_undeath = placeholder();
	public static final Potion containment = placeholder();
	public static final Potion frost_step = placeholder();

	/**
	 * Sets both the registry and unlocalised names of the given potion, then registers it with the given registry. Use
	 * this instead of {@link Potion#setRegistryName(String)} and {@link Potion#setPotionName(String)} during
	 * construction, for convenience and consistency.
	 * 
	 * @param registry The registry to register the given potion to.
	 * @param name The name of the potion, without the mod ID or the .name stuff. The registry name will be
	 *        {@code ebwizardry:[name]}. The unlocalised name will be {@code potion.ebwizardry:[name].name}.
	 * @param potion The potion to register.
	 */
	public static void registerPotion(IForgeRegistry<Potion> registry, String name, Potion potion){
		potion.setRegistryName(Wizardry.MODID, name);
		// For some reason, Potion#getName() doesn't prepend "potion." itself, so it has to be done here.
		potion.setPotionName("potion." + potion.getRegistryName().toString());
		registry.register(potion);
	}

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Potion> event){

		IForgeRegistry<Potion> registry = event.getRegistry();

		// Interestingly, setting the colour to black stops the particles from rendering.

		registerPotion(registry, "frost", new PotionFrost(true, 0)); // Colour was 0x38ddec (was arbitrary anyway)
		
		registerPotion(registry, "transience", new PotionMagicEffectParticles(false, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/transience.png")){
			@Override
			public void spawnCustomParticle(World world, double x, double y, double z){
				ParticleBuilder.create(Type.DUST).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).shaded(true).spawn(world);
			}
		}.setBeneficial()); // 0xffe89b
		
		registerPotion(registry, "fireskin", new PotionMagicEffectParticles(false, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/fireskin.png")){
			@Override
			public void spawnCustomParticle(World world, double x, double y, double z){
				world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
			}

			@Override
			public void performEffect(EntityLivingBase entitylivingbase, int strength){
				entitylivingbase.extinguish(); // Stops melee mobs that are on fire from setting the player on fire,
				// without allowing the player to actually stand in fire or swim in lava without taking damage.
			}
		}.setBeneficial()); // 0xff2f02
		
		registerPotion(registry, "ice_shroud", new PotionMagicEffectParticles(false, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/ice_shroud.png")){
			@Override
			public void spawnCustomParticle(World world, double x, double y, double z){
				float brightness = 0.5f + (world.rand.nextFloat() / 2);
				ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(brightness, brightness + 0.1f, 1.0f).gravity(true).spawn(world);
				ParticleBuilder.create(Type.SNOW).pos(x, y, z).spawn(world);
			}
		}.setBeneficial()); // 0x52f1ff
		
		registerPotion(registry, "static_aura", new PotionMagicEffectParticles(false, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/static_aura.png")){
			@Override
			public void spawnCustomParticle(World world, double x, double y, double z){
				ParticleBuilder.create(Type.SPARK).pos(x, y, z).spawn(world);
			}
		}.setBeneficial()); // 0x0070ff
		
		registerPotion(registry, "decay", new PotionDecay(true, 0x3c006c));

		registerPotion(registry, "sixth_sense", new PotionMagicEffect(false, 0xc6ff01,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/sixth_sense.png")){
			@Override
			public void performEffect(EntityLivingBase target, int strength){
				// Reset the shader (a bit dirty but both the potion expiry hooks are only fired server-side, and
				// there's no point sending packets unnecessarily if we can just do this instead)
				if(target.getActivePotionEffect(this).getDuration() <= 1 && target.world.isRemote
						&& target == net.minecraft.client.Minecraft.getMinecraft().player){
					net.minecraft.client.Minecraft.getMinecraft().entityRenderer.stopUseShader();
				}
			}
		}.setBeneficial());

		registerPotion(registry, "arcane_jammer", new PotionMagicEffect(true, 0xcf4aa2,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/arcane_jammer.png")));

		registerPotion(registry, "mind_trick", new PotionMagicEffect(true, 0x601683,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/mind_trick.png")));

		registerPotion(registry, "mind_control", new PotionMagicEffect(true, 0x320b44,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/mind_control.png")));

		registerPotion(registry, "font_of_mana", new PotionMagicEffect(false, 0xffe5bb,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/font_of_mana.png")).setBeneficial());

		registerPotion(registry, "fear", new PotionMagicEffect(true, 0xbd0100,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/fear.png")));
		
		registerPotion(registry, "curse_of_soulbinding", new Curse(true, 0x0f000f,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/curse_of_soulbinding.png")){
			@Override // We're not removing any attributes, but it's called when we want it to be so...
			public void removeAttributesModifiersFromEntity(EntityLivingBase entity, net.minecraft.entity.ai.attributes.AbstractAttributeMap attributeMapIn, int amplifier){
				// TODO: Hmmmm...
			}
		});
		
		registerPotion(registry, "paralysis", new PotionMagicEffectParticles(true, 0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/paralysis.png")){
			@Override
			public void spawnCustomParticle(World world, double x, double y, double z){
				ParticleBuilder.create(Type.SPARK).pos(x, y, z).spawn(world);
			}
		});
		
		registerPotion(registry, "muffle", new PotionMagicEffect(false, 0x4464d9,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/muffle.png")).setBeneficial());

		registerPotion(registry, "ward", new PotionMagicEffect(false, 0xc991d0,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/ward.png")).setBeneficial());

		registerPotion(registry, "slow_time", new PotionSlowTime(false, 0x5be3bb).setBeneficial());

		registerPotion(registry, "empowerment", new PotionMagicEffect(false, 0x8367bd,
				new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/empowerment.png")).setBeneficial());

		registerPotion(registry, "curse_of_enfeeblement", new CurseEnfeeblement(true, 0x36000b));

		registerPotion(registry, "curse_of_undeath", new CurseUndeath(true, 0x685c00));

		registerPotion(registry, "containment", new PotionContainment(true, 0x7988cc));

		registerPotion(registry, "frost_step", new PotionFrostStep(false, 0).setBeneficial());

	}

}