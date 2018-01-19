package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Class responsible for defining, storing and registering all of wizardry's sound events. For some reason, these
 * worked in the beta versions despite not being registered...
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardrySounds {

	// Anything with LOOP in its name is intended to be played in a continuous loop.
	public static final SoundEvent SPELL_SPARK = createSound("arc");
	public static final SoundEvent SPELL_CONJURATION = createSound("aura");
	public static final SoundEvent SPELL_SHOCKWAVE = createSound("boom");
	public static final SoundEvent SPELL_LOOP_CRACKLE = createSound("crackle");
	public static final SoundEvent SPELL_SUMMONING = createSound("darkaura");
	public static final SoundEvent SPELL_DEFLECTION = createSound("effect");
	public static final SoundEvent SPELL_LIGHTNING = createSound("electricitya");
	public static final SoundEvent SPELL_LOOP_LIGHTNING = createSound("electricityb");
	public static final SoundEvent SPELL_LOOP_FIRE = createSound("flameray");
	public static final SoundEvent SPELL_FREEZE = createSound("freeze");
	public static final SoundEvent SPELL_LOOP_ICE = createSound("frostray");
	public static final SoundEvent SPELL_HEAL = createSound("heal");
	public static final SoundEvent SPELL_ICE = createSound("ice");
	public static final SoundEvent SPELL_CONJURATION_LARGE = createSound("largeaura");
	public static final SoundEvent SPELL_MAGIC = createSound("magic");
	public static final SoundEvent SPELL_LOOP_SPARKLE = createSound("sparkle");
	public static final SoundEvent SPELL_LOOP_WIND = createSound("wind");
	public static final SoundEvent SPELL_EARTHQUAKE = createSound("rumble");
	public static final SoundEvent SPELL_FORCE = createSound("force");
	
	/** Trick borrowed from the Twilight Forest, makes things neater. */
	public static SoundEvent createSound(String name){
		// All the setRegistryName methods delegate to this one, it doesn't matter which you use.
		return new SoundEvent(new ResourceLocation(Wizardry.MODID, name)).setRegistryName(name);
	}
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<SoundEvent> event){
		event.getRegistry().register(SPELL_SPARK);
		event.getRegistry().register(SPELL_CONJURATION);
		event.getRegistry().register(SPELL_SHOCKWAVE);
		event.getRegistry().register(SPELL_LOOP_CRACKLE);
		event.getRegistry().register(SPELL_SUMMONING);
		event.getRegistry().register(SPELL_DEFLECTION);
		event.getRegistry().register(SPELL_LIGHTNING);
		event.getRegistry().register(SPELL_LOOP_LIGHTNING);
		event.getRegistry().register(SPELL_LOOP_FIRE);
		event.getRegistry().register(SPELL_FREEZE);
		event.getRegistry().register(SPELL_LOOP_ICE);
		event.getRegistry().register(SPELL_HEAL);
		event.getRegistry().register(SPELL_ICE);
		event.getRegistry().register(SPELL_CONJURATION_LARGE);
		event.getRegistry().register(SPELL_MAGIC);
		event.getRegistry().register(SPELL_LOOP_SPARKLE);
		event.getRegistry().register(SPELL_LOOP_WIND);
		event.getRegistry().register(SPELL_EARTHQUAKE);
		event.getRegistry().register(SPELL_FORCE);
	}
}