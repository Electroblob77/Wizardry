package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityIceShard extends EntityMagicArrow {

	/** Creates a new ice shard in the given world. */
	public EntityIceShard(World world){
		super(world);
	}

	@Override public double getDamage(){ return Spells.ice_shard.getProperty(Spell.DAMAGE).floatValue(); }

	@Override public int getLifetime(){ return -1; }

	@Override public DamageType getDamageType(){ return DamageType.FROST; }

	@Override public boolean doGravity(){ return true; }

	@Override public boolean doDeceleration(){ return true; }

	@Override public boolean canRenderOnFire(){ return false; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){

		// Adds a freeze effect to the target.
		if(!MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
			entityHit.addPotionEffect(new PotionEffect(WizardryPotions.frost,
					Spells.ice_shard.getProperty(Spell.EFFECT_DURATION).intValue(),
					Spells.ice_shard.getProperty(Spell.EFFECT_STRENGTH).intValue()));

		this.playSound(WizardrySounds.ENTITY_ICE_SHARD_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void onBlockHit(RayTraceResult hit){
		
		// Adds a particle effect when the ice shard hits a block.
		if(this.world.isRemote){
			// Gets a position slightly away from the block hit so the particle doesn't get cut in half by the block face
			Vec3d vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(0.15));
			ParticleBuilder.create(Type.FLASH).pos(vec).clr(0.75f, 1, 1).spawn(world);
			
			for(int j = 0; j < 10; j++){
				ParticleBuilder.create(Type.ICE, this.rand, this.posX, this.posY, this.posZ, 0.5, true)
				.time(20 + rand.nextInt(10)).gravity(true).spawn(world);
			}
		}
		// Parameters for sound: sound event name, volume, pitch.
		this.playSound(WizardrySounds.ENTITY_ICE_SHARD_SMASH, 1.0F, rand.nextFloat() * 0.4F + 1.2F);

	}

	@Override
	protected void entityInit(){}

}