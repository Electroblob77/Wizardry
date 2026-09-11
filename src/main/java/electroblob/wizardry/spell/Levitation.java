package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class Levitation extends Spell {

	public static final String SPEED = "speed";
	public static final String ACCELERATION = "acceleration";

	public Levitation(){
		super("levitation", SpellActions.POINT_DOWN, true);
		addProperties(SPEED, ACCELERATION);
		soundValues(0.5f, 1, 0);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double prevMotionY = caster.motionY;

		float potency = modifiers.get(SpellModifiers.POTENCY);
		float upwardVelocity = this.getProperty(SPEED).floatValue() * potency;
		//Needs to be greater than 0.08 to counteract gravity
		float upwardAcceleration = this.getProperty(ACCELERATION).floatValue() * potency;

		//Change to min check to avoid jittery screen effects at high acceleration
		caster.motionY = Math.min(caster.motionY + upwardAcceleration, upwardVelocity);

		if (!Wizardry.settings.replaceVanillaFallDamage) {
			if (caster.motionY < 0 && caster.motionY > prevMotionY) {
				caster.fallDistance *= (float)(caster.motionY / prevMotionY);
			} else if (caster.motionY >= 0) {
				caster.fallDistance = 0;
			}
		}

		if(world.isRemote){
			double x = caster.posX - 0.25 + world.rand.nextDouble() * 0.5;
			double y = caster.getPositionEyes(1).y;
			double z = caster.posZ - 0.25 + world.rand.nextDouble() * 0.5;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).time(15).clr(0.5f, 1, 0.7f).spawn(world);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
