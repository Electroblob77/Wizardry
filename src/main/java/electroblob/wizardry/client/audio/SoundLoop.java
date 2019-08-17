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

	private final ISound start;
	private final ISound loop;
	private final ISound end;

	private boolean looping = false;
	private boolean needsRemoving = false;

	public SoundLoop(SoundEvent start, SoundEvent loop, SoundEvent end, SoundCategory category, ISoundFactory factory){
		// The reason I've gone to the effort of having a factory for these is that we need SoundLoop to have control
		// over which sounds are repeated and which aren't whilst keeping them private.
		this.start = factory.create(start, category, false);
		this.loop = factory.create(loop, category, true);
		this.end = factory.create(end, category, false);
	}

	@Override
	public void update(){
		// Check every tick if the start sound is done playing and if so, start the loop sound
		if(!looping && !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(start)){
			Minecraft.getMinecraft().getSoundHandler().playSound(loop);
			looping = true;
		}
	}

	/** Stops the loop part of the sound immediately and starts playing the end part. This may be called from subclasses
	 * or externally depending on the implementation. */
	// For continuous spell sounds it's internally
	public void endLoop(){
		Minecraft.getMinecraft().getSoundHandler().stopSound(start);
		Minecraft.getMinecraft().getSoundHandler().stopSound(loop);
		Minecraft.getMinecraft().getSoundHandler().playSound(end);
		// Can't modify activeLoops directly since we'll probably be calling this method from update(), which is
		// during iteration of activeLoops so it could cause a ConcurrentModificationException
		this.markForRemoval();
	}

	/** Marks this sound loop to be removed next tick. */
	protected void markForRemoval(){
		this.needsRemoving = true;
	}

	// Static methods

	public static void addLoop(SoundLoop loop){
		activeLoops.add(loop);
		// Do this here rather than in the constructor in case someone wants to play the loop later or reuse it
		Minecraft.getMinecraft().getSoundHandler().playSound(loop.start);
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event){
		// Using the END phase means we can check for stopped sounds as soon as they are stopped (effectively),
		// meaning we don't get the 'cut' between the start and loop sounds
		// FIXME: Apparently this only works for dispensers. What the heck is the difference?!
		if(event.phase == TickEvent.Phase.END){
			activeLoops.forEach(SoundLoop::update);
			activeLoops.removeIf(s -> s.needsRemoving);
		}
	}

	@FunctionalInterface
	public interface ISoundFactory {
		ISound create(SoundEvent sound, SoundCategory category, boolean repeat);
	}
}
