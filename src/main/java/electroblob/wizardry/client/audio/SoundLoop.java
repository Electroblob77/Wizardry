package electroblob.wizardry.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Set;

/**
 * Instances of this class represent a set of sounds which together form a looped sound: start, loop and end.
 * Currently this is only used for continuous spell sounds in wizardry itself, but feel free to make your own
 * implementations - this class will take care of the internals.
 *
 * @since Wizardry 4.2
 * @author Electroblob
 */
// See MusicTicker, this is the same idea of just storing the sounds statically client-side and ticking them
@Mod.EventBusSubscriber(Side.CLIENT)
public abstract class SoundLoop implements ITickable {

	private static final Set<SoundLoop> activeLoops = new HashSet<>();

	// The issue we had here was that there is no way of hooking into the client tick loop between the sound manager
	// update and the world tick (other than via the sounds themselves, but playing other sounds from there results in
	// CMEs, which is why we made this class in the first place). This meant there was always a gap between consecutive
	// sounds, which could be heard in-game as a weird audio artefact. The solution is simply to overlap the sounds by
	// 1 tick, which is fairly straightforward for the end sound, but because the start sound has to finish for us to
	// know when to play the others, this doubling-up solution is required (bit of a waste of resources but meh).
	/** A dummy sound with effectively zero volume, played one tick in advance of the actual start sound so the loop
	 * sound can start one tick before the actual start sound ends. */
	private final ISound dummyStart; // It's the stupidest system ever but it works!

	private final ISound start;
	private final ISound loop;
	private final ISound end;

	private boolean looping = false;
	private boolean needsRemoving = false;

	public SoundLoop(SoundEvent start, SoundEvent loop, SoundEvent end, SoundCategory category, float volume, ISoundFactory factory){
		// The reason I've gone to the effort of having a factory for these is that we need SoundLoop to have control
		// over which sounds are repeated and which aren't whilst keeping them private.
		this.dummyStart = factory.create(start, category, 0.00001f, false); // Inaudible but not actually 0
		this.start = factory.create(start, category, volume, false);
		this.loop = factory.create(loop, category, volume, true);
		this.end = factory.create(end, category, volume, false);
	}

	@Override
	public void update(){
		// Check every tick if the start sound is done playing and if so, start the loop sound
		if(!looping && !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(dummyStart)){
			Minecraft.getMinecraft().getSoundHandler().playSound(loop);
			looping = true;
		}
	}

	/** Starts playing the end part immediately and marks the loop part to stop next tick. This may be called from
	 * subclasses or externally depending on the implementation. */
	// For continuous spell sounds it's internally
	public void endLoop(){
		Minecraft.getMinecraft().getSoundHandler().playSound(end);
		// Can't modify activeLoops directly since we'll probably be calling this method from update(), which is
		// during iteration of activeLoops so it could cause a ConcurrentModificationException
		this.markForRemoval();
	}

	/** Marks this sound loop to be removed next tick. */
	protected void markForRemoval(){
		this.needsRemoving = true;
	}

	/** Stops the start and loop sounds immediately. */
	protected void stopStartAndLoop(){
		Minecraft.getMinecraft().getSoundHandler().stopSound(dummyStart);
		Minecraft.getMinecraft().getSoundHandler().stopSound(start);
		Minecraft.getMinecraft().getSoundHandler().stopSound(loop);
	}

	// Static methods

	public static void addLoop(SoundLoop loop){
		activeLoops.add(loop);
		// Do this here rather than in the constructor in case someone wants to play the loop later or reuse it
		Minecraft.getMinecraft().getSoundHandler().playSound(loop.dummyStart);
		Minecraft.getMinecraft().getSoundHandler().playDelayedSound(loop.start, 2); // 1 seems to be insufficient
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event){
		// Using the END phase means we can check for stopped sounds as soon as they are stopped (effectively),
		// meaning we don't get the 'cut' between the start and loop sounds
		// FIXME: Apparently this only works for dispensers. What the heck is the difference?!
		if(event.phase == TickEvent.Phase.END){
			activeLoops.stream().filter(s -> s.needsRemoving).forEach(SoundLoop::stopStartAndLoop);
			activeLoops.removeIf(s -> s.needsRemoving);
			activeLoops.forEach(SoundLoop::update); // Do this last to allow a 1-tick overlap
		}
	}

	@FunctionalInterface
	public interface ISoundFactory {
		ISound create(SoundEvent sound, SoundCategory category, float volume, boolean repeat);
	}
}
