package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;

public class EntityIceCharge extends EntityBomb {

	public static final String ICE_SHARDS = "ice_shards";

	public EntityIceCharge(World world){
		super(world);
	}

	@Override
	public int getLifetime(){
		return -1;
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){

		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){
			// This is if the ice charge gets a direct hit
			float damage = Spells.ice_charge.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FROST).setProjectile(),
					damage);

			if(entityHit instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
				((EntityLivingBase)entityHit).addPotionEffect(new PotionEffect(WizardryPotions.frost,
						Spells.ice_charge.getProperty(Spell.DIRECT_EFFECT_DURATION).intValue(),
						Spells.ice_charge.getProperty(Spell.DIRECT_EFFECT_STRENGTH).intValue()));
		}

		// Particle effect
		if(world.isRemote){
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
			for(int i = 0; i < 30 * blastMultiplier; i++){

				ParticleBuilder.create(Type.ICE, rand, this.posX, this.posY, this.posZ, 2 * blastMultiplier, false)
				.time(35).gravity(true).spawn(world);

				float brightness = 0.4f + rand.nextFloat() * 0.5f;
				ParticleBuilder.create(Type.DARK_MAGIC, rand, this.posX, this.posY, this.posZ, 2 * blastMultiplier, false)
				.clr(brightness, brightness + 0.1f, 1.0f).spawn(world);
			}
		}

		if(!this.world.isRemote){

			this.playSound(WizardrySounds.ENTITY_ICE_CHARGE_SMASH, 1.5f, rand.nextFloat() * 0.4f + 0.6f);
			this.playSound(WizardrySounds.ENTITY_ICE_CHARGE_ICE, 1.2f, rand.nextFloat() * 0.4f + 1.2f);

			double radius = Spells.ice_charge.getProperty(Spell.EFFECT_RADIUS).floatValue() * blastMultiplier;

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(radius, this.posX, this.posY,
					this.posZ, this.world);

			// Slows targets
			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower()){
					if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
						target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
								Spells.ice_charge.getProperty(Spell.SPLASH_EFFECT_DURATION).intValue(),
								Spells.ice_charge.getProperty(Spell.SPLASH_EFFECT_STRENGTH).intValue()));
				}
			}

			// Places snow and ice on ground.
			for(int i = -1; i < 2; i++){
				for(int j = -1; j < 2; j++){

					BlockPos pos = new BlockPos(this.posX + i, this.posY, this.posZ + j);

					Integer y = BlockUtils.getNearestSurface(world, pos, EnumFacing.UP, 7, true,
							BlockUtils.SurfaceCriteria.SOLID_LIQUID_TO_AIR);

					if(y != null){

						pos = new BlockPos(pos.getX(), y, pos.getZ());

						double dist = this.getDistance(pos.getX(), pos.getY(), pos.getZ());

						// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
						if(rand.nextInt((int)dist * 2 + 1) < 1 && dist < 2){
							if(world.getBlockState(pos.down()).getBlock() == Blocks.WATER){
								world.setBlockState(pos.down(), Blocks.ICE.getDefaultState());
							}else{
								// Don't need to check whether the block at pos can be replaced since getNearestFloorLevelB
								// only ever returns floors with air above them.
								world.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
							}
						}
					}
				}
			}

			// Releases shards
			for(int i = 0; i < Spells.ice_charge.getProperty(ICE_SHARDS).intValue(); i++){
				double dx = rand.nextDouble() - 0.5;
				double dy = rand.nextDouble() - 0.5;
				double dz = rand.nextDouble() - 0.5;
				EntityIceShard iceshard = new EntityIceShard(world);
				iceshard.setPosition(this.posX + dx, this.posY + dy, this.posZ + dz);
				iceshard.motionX = dx * 1.5;
				iceshard.motionY = dy * 1.5;
				iceshard.motionZ = dz * 1.5;
				iceshard.setCaster(this.getThrower());
				iceshard.damageMultiplier = this.damageMultiplier;
				world.spawnEntity(iceshard);
			}

			this.setDead();
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}
}
