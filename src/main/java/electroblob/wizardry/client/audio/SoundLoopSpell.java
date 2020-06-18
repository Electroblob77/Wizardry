package electroblob.wizardry.client.audio;

import electroblob.wizardry.data.DispenserCastingData;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Abstract base class for sound loops associated with spells; see subclasses below for implementations. */
public abstract class SoundLoopSpell extends SoundLoop {

	private final Spell spell;

	public SoundLoopSpell(SoundEvent start, SoundEvent loop, SoundEvent end, float volume, ISoundFactory factory, Spell spell){
		super(start, loop, end, WizardrySounds.SPELLS, volume, factory);
		this.spell = spell;
	}

	@Override
	public void update(){
		// This may be a bit overkill but I might as well put functionality in superclasses where possible
		if(stillCasting(spell)){
			// This can't called in the same tick as endLoop because otherwise, if the spell casting stops during the
			// same tick as the transition to the loop sound it will try and stop the loop immediately after it has
			// started - and for whatever reason, this causes the 'Channel null in method stop' error.
			super.update();
		}else{
			endLoop();
		}
	}

	protected abstract boolean stillCasting(Spell spell);

	/** Implements a sound loop for continuous spells cast by entities. */
	public static class SoundLoopSpellEntity extends SoundLoopSpell {

		private final EntityLivingBase source;

		public SoundLoopSpellEntity(SoundEvent start, SoundEvent loop, SoundEvent end, Spell spell, EntityLivingBase source, float volume, float pitch){
			super(start, loop, end, volume, (sound, category, v, repeat) -> new MovingSoundEntity<>(source, sound, category, v, pitch, repeat), spell);
			this.source = source;
		}

		@Override
		protected boolean stillCasting(Spell spell){
			return EntityUtils.isCasting(source, spell);
		}
	}

	public static abstract class SoundLoopSpellPosition extends SoundLoopSpell {

		public SoundLoopSpellPosition (SoundEvent start, SoundEvent loop, SoundEvent end, Spell spell,
									   double x, double y, double z, float sndVolume, float sndPitch){
			// Huh, I actually found a use for a non-static initialiser block - hence the double curly brackets...
			super(start, loop, end, sndVolume, (sound, category, v, r) -> new PositionedSound(sound, category){{
				// ...et voila, we can just set protected fields as we please using external variables
				this.xPosF = (float)x;
				this.yPosF = (float)y;
				this.zPosF = (float)z;
				this.repeat = r;
				this.volume = v;
				this.pitch = sndPitch;
			}}, spell);
		}
	}

	/** Implements a sound loop for continuous spells cast by dispensers. */
	public static class SoundLoopSpellDispenser extends SoundLoopSpellPosition {

		private final TileEntityDispenser source;

		public SoundLoopSpellDispenser(SoundEvent start, SoundEvent loop, SoundEvent end, Spell spell, World world,
									   double x, double y, double z, float sndVolume, float sndPitch){
			super(start, loop, end, spell, x, y, z, sndVolume, sndPitch);

			TileEntity tileentity = world.getTileEntity(new BlockPos(x, y, z));

			if(tileentity instanceof TileEntityDispenser) this.source = (TileEntityDispenser)tileentity;
			else throw new NullPointerException(String.format("Playing continuous spell sound: no dispenser found at %s, %s, %s", x, y, z));
		}

		@Override
		protected boolean stillCasting(Spell spell){
			return DispenserCastingData.get(source).currentlyCasting() == spell;
		}

	}

	/** Implements a sound loop for continuous spells cast at a position (via commands). */
	public static class SoundLoopSpellPosTimed extends SoundLoopSpellPosition {

		private int timeLeft;

		public SoundLoopSpellPosTimed(SoundEvent start, SoundEvent loop, SoundEvent end, Spell spell, int duration,
								double x, double y, double z, float sndVolume, float sndPitch){
			super(start, loop, end, spell, x, y, z, sndVolume, sndPitch);
			this.timeLeft = duration;
		}

		@Override
		public void update(){
			super.update();
			timeLeft--;
		}

		@Override
		protected boolean stillCasting(Spell spell){
			return timeLeft > 0;
		}
	}
}
