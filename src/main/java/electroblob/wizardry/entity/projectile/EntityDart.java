package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityDart extends EntityMagicArrow {
	
	/** Creates a new dart in the given world. */
	public EntityDart(World world){
		super(world);
	}

	@Override public double getDamage(){ return Spells.dart.getProperty(Spell.DAMAGE).doubleValue(); }

	@Override public boolean doGravity(){ return true; }

	@Override public boolean doDeceleration(){ return true; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		// Adds a weakness effect to the target.
		entityHit.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, Spells.dart.getProperty(Spell.EFFECT_DURATION).intValue(),
				Spells.dart.getProperty(Spell.EFFECT_STRENGTH).intValue(), false, false));
		this.playSound(WizardrySounds.ENTITY_DART_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void onBlockHit(RayTraceResult hit){
		this.playSound(WizardrySounds.ENTITY_DART_HIT_BLOCK, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void tickInAir(){
		if(this.world.isRemote){
			ParticleBuilder.create(Type.LEAF, this).time(10 + rand.nextInt(5)).spawn(world);
		}
	}

	// Replicates the original behaviour of staying stuck in block for a few seconds before disappearing.
	@Override
	public void tickInGround(){
		if(this.ticksInGround > 60){
			this.setDead();
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public int getLifetime(){
		return -1;
	}
}