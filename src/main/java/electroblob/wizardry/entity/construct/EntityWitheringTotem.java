package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.WitheringTotem;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.BlockUtils.SurfaceCriteria;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class EntityWitheringTotem extends EntityScaledConstruct {

	private static final int PERIMETER_PARTICLE_DENSITY = 6;

	private static final DataParameter<Float> HEALTH_DRAINED = EntityDataManager.createKey(EntityWitheringTotem.class, DataSerializers.FLOAT);

	public EntityWitheringTotem(World world){
		super(world);
		this.setSize(1, 1); // This entity is different in that its area of effect is kind of 'outside' it
	}

	@Override
	protected void entityInit(){
		dataManager.register(HEALTH_DRAINED, 0f);
	}

	public float getHealthDrained(){
		return dataManager.get(HEALTH_DRAINED);
	}

	public void addHealthDrained(float health){
		dataManager.set(HEALTH_DRAINED, getHealthDrained() + health);
	}

	@Override
	protected boolean shouldScaleWidth(){
		return false;
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void onUpdate(){

		if(world.isRemote && this.ticksExisted == 1){
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_WITHERING_TOTEM_AMBIENT, WizardrySounds.SPELLS, 1, 1, true);
		}

		super.onUpdate();

		double radius = Spells.withering_totem.getProperty(Spell.EFFECT_RADIUS).floatValue() * sizeMultiplier;

		if(world.isRemote){

			ParticleBuilder.create(Type.DUST, rand, posX, posY + 0.2, posZ, 0.3, false)
					.vel(0, -0.02 - world.rand.nextFloat() * 0.01, 0).clr(0xf575f5).fade(0x382366).spawn(world);

			for(int i=0; i<PERIMETER_PARTICLE_DENSITY; i++){

				float angle = ((float)Math.PI * 2)/PERIMETER_PARTICLE_DENSITY * (i + rand.nextFloat());

				double x = posX + radius * MathHelper.sin(angle);
				double z = posZ + radius * MathHelper.cos(angle);

				Integer y = BlockUtils.getNearestSurface(world, new BlockPos(x, posY, z), EnumFacing.UP, 5, true, SurfaceCriteria.COLLIDABLE);

				if(y != null){
					ParticleBuilder.create(Type.DUST).pos(x, y, z).vel(0, 0.01, 0).clr(0xf575f5).fade(0x382366).spawn(world);
				}
			}
		}

		List<EntityLivingBase> nearby = EntityUtils.getLivingWithinRadius(radius, posX, posY, posZ, world);
		nearby.removeIf(e -> !isValidTarget(e));
		nearby.sort(Comparator.comparingDouble(e -> e.getDistanceSq(this)));

		int targetsRemaining = Spells.withering_totem.getProperty(WitheringTotem.MAX_TARGETS).intValue()
				+ (int)((damageMultiplier - 1) / Constants.POTENCY_INCREASE_PER_TIER);

		while(!nearby.isEmpty() && targetsRemaining > 0){

			EntityLivingBase target = nearby.remove(0);

			if(EntityUtils.isLiving(target)){

				if(target.ticksExisted % target.maxHurtResistantTime == 1){

					float damage = Spells.withering_totem.getProperty(Spell.DAMAGE).floatValue();

					if(EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(this,
							getCaster(), DamageType.WITHER), damage)){
						addHealthDrained(damage);
					}
				}

				targetsRemaining--;

				if(world.isRemote){

					Vec3d centre = GeometryUtils.getCentre(this);
					Vec3d pos = GeometryUtils.getCentre(target);

					ParticleBuilder.create(Type.BEAM).pos(centre).target(target)
							.clr(0.1f + 0.2f * world.rand.nextFloat(), 0, 0.3f).spawn(world);

					for(int i = 0; i < 3; i++){
						ParticleBuilder.create(Type.DUST, rand, pos.x, pos.y, pos.z, 0.3, false)
								.vel(pos.subtract(centre).normalize().scale(-0.1)).clr(0x0c0024).fade(0x610017).spawn(world);
					}
				}
			}
		}
	}

	@Override
	public void despawn(){

		double radius = Spells.withering_totem.getProperty(Spell.EFFECT_RADIUS).floatValue() * sizeMultiplier;

		List<EntityLivingBase> nearby = EntityUtils.getLivingWithinRadius(radius, posX, posY, posZ, world);
		nearby.removeIf(e -> !isValidTarget(e));

		float damage = Math.min(getHealthDrained() * 0.2f, Spells.withering_totem.getProperty(WitheringTotem.MAX_EXPLOSION_DAMAGE).floatValue());

		for(EntityLivingBase target : nearby){

			if(EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(this,
					getCaster(), DamageType.MAGIC), damage)){
				target.addPotionEffect(new PotionEffect(MobEffects.WITHER, Spells.withering_totem.getProperty(Spell.EFFECT_DURATION).intValue(),
						Spells.withering_totem.getProperty(Spell.EFFECT_STRENGTH).intValue()));
			}
		}

		if(world.isRemote) ParticleBuilder.create(Type.SPHERE).pos(GeometryUtils.getCentre(this)).scale((float)radius).clr(0xbe1a53)
				.fade(0x210f4a).spawn(world);

		this.playSound(WizardrySounds.ENTITY_WITHERING_TOTEM_EXPLODE, 1, 1);
		super.despawn();
	}

	// Usually damage multipliers don't need syncing, but here we're using it for the non-standard purpose of targeting

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeFloat(damageMultiplier);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		damageMultiplier = data.readFloat();
	}
}
