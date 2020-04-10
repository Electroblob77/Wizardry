package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Glide extends Spell {

	public static final String SPEED = "speed";
	public static final String FALL_SPEED = "fall_speed";
	public static final String ACCELERATION = "acceleration";

	public Glide(){
		super("glide", EnumAction.NONE, true);
		addProperties(SPEED, FALL_SPEED, ACCELERATION);
	}

	@Override
	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		if(ticksInUse == 0 && world.isRemote) Wizardry.proxy.playSpellSoundLoop(entity, this, this.sounds[0], this.sounds[0], this.sounds[0],
				WizardrySounds.SPELLS, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		if(ticksInUse == 0 && world.isRemote){
			Wizardry.proxy.playSpellSoundLoop(world, x, y, z, this, this.sounds[0], this.sounds[0], this.sounds[0],
					WizardrySounds.SPELLS, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f), duration);
		}
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.motionY < -0.1 && !caster.isInWater()){

			float speed = getProperty(SPEED).floatValue() * modifiers.get(SpellModifiers.POTENCY);
			// There seems to be some sort of 'terminal velocity', presumably due to the slight slowing-down effect in
			// vanilla - this means we have to apply potency modifiers to the acceleration as well as the speed or they
			// appear to have no effect (a bug which had me confused for quite a while!) This also applies to flight.
			float acceleration = getProperty(ACCELERATION).floatValue() * modifiers.get(SpellModifiers.POTENCY);

			caster.motionY = -getProperty(FALL_SPEED).floatValue();
			if(Math.abs(caster.motionX) < speed && Math.abs(caster.motionZ) < speed){
				caster.addVelocity(caster.getLookVec().x * acceleration, 0, caster.getLookVec().z * acceleration);
			}

			if(!Wizardry.settings.replaceVanillaFallDamage) caster.fallDistance = 0.0f;
		}

		if(world.isRemote){
			double x = caster.posX - 0.25 + world.rand.nextDouble() / 2;
			double y = caster.getEntityBoundingBox().minY + world.rand.nextDouble();
			double z = caster.posZ - 0.25 + world.rand.nextDouble() / 2;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).time(15).clr(1f, 1f, 1f).spawn(world);
			x = caster.posX - 0.25 + world.rand.nextDouble() / 2;
			y = caster.getEntityBoundingBox().minY + world.rand.nextDouble();
			z = caster.posZ - 0.25 + world.rand.nextDouble() / 2;
			ParticleBuilder.create(Type.LEAF).pos(x, y, z).time(20).spawn(world);
		}

		if(ticksInUse % 24 == 0){
			this.playSound(world, caster, ticksInUse, -1, modifiers);
		}
		
		return true;
	}

}
