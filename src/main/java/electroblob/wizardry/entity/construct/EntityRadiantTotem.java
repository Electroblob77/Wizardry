package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.RadiantTotem;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.BlockUtils.SurfaceCriteria;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EntityRadiantTotem extends EntityScaledConstruct {

	private static final int PERIMETER_PARTICLE_DENSITY = 6;

	public EntityRadiantTotem(World world){
		super(world);
		this.setSize(1, 1); // This entity is different in that its area of effect is kind of 'outside' it
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

		if(this.ticksExisted == 1){
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_RADIANT_TOTEM_AMBIENT, WizardrySounds.SPELLS, 1, 1, true);
		}

		super.onUpdate();

		double radius = Spells.radiant_totem.getProperty(Spell.EFFECT_RADIUS).floatValue() * sizeMultiplier;

		if(world.isRemote){

			ParticleBuilder.create(Type.DUST, rand, posX, posY + 0.2, posZ, 0.3, false)
					.vel(0, -0.02 - world.rand.nextFloat() * 0.01, 0).clr(0xffffff).fade(0xffec90).spawn(world);

			for(int i=0; i<PERIMETER_PARTICLE_DENSITY; i++){

				float angle = ((float)Math.PI * 2)/PERIMETER_PARTICLE_DENSITY * (i + rand.nextFloat());

				double x = posX + radius * MathHelper.sin(angle);
				double z = posZ + radius * MathHelper.cos(angle);

				Integer y = BlockUtils.getNearestSurface(world, new BlockPos(x, posY, z), EnumFacing.UP, 5, true, SurfaceCriteria.COLLIDABLE);

				if(y != null){
					ParticleBuilder.create(Type.DUST).pos(x, y, z).vel(0, 0.01, 0).clr(0xffffff).fade(0xffec90).spawn(world);
				}
			}
		}

		List<EntityLivingBase> nearby = EntityUtils.getLivingWithinRadius(radius, posX, posY, posZ, world);
		nearby.sort(Comparator.comparingDouble(e -> e.getDistanceSq(this)));

		List<EntityLivingBase> nearbyAllies = nearby.stream().filter(e -> e == getCaster()
				|| AllyDesignationSystem.isAllied(getCaster(), e)).collect(Collectors.toList());
		nearby.removeAll(nearbyAllies);

		int targetsRemaining = Spells.radiant_totem.getProperty(RadiantTotem.MAX_TARGETS).intValue()
				+ (int)((damageMultiplier - 1) / Constants.POTENCY_INCREASE_PER_TIER);

		while(!nearbyAllies.isEmpty() && targetsRemaining > 0){

			EntityLivingBase ally = nearbyAllies.remove(0);

			if(ally.getHealth() < ally.getMaxHealth()){
				// Slightly slower than healing aura, and it only does 1 at a time (without potency modifiers)
				if(ally.ticksExisted % 8 == 0) ally.heal(Spells.radiant_totem.getProperty(Spell.HEALTH).floatValue());
				targetsRemaining--;

				if(world.isRemote){
					ParticleBuilder.create(Type.BEAM).pos(this.getPositionVector()).target(ally)
							.clr(1, 0.6f + 0.3f * world.rand.nextFloat(), 0.2f).spawn(world);
				}
			}
		}

		while(!nearby.isEmpty() && targetsRemaining > 0){

			EntityLivingBase target = nearby.remove(0);

			if(EntityUtils.isLiving(target)){

				if(target.ticksExisted % target.maxHurtResistantTime == 1){

					float damage = Spells.radiant_totem.getProperty(Spell.DAMAGE).floatValue();

					if(target.isEntityUndead()){
						target.setFire(Spells.radiant_totem.getProperty(Spell.BURN_DURATION).intValue());
//						damage *= getProperty(UNDEAD_DAMAGE_MULTIPLIER).floatValue();
					}

					EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(this,
							getCaster(), DamageType.RADIANT), damage);
				}

				targetsRemaining--;

				if(world.isRemote){
					ParticleBuilder.create(Type.BEAM).pos(this.getPositionVector().add(0, height/2, 0))
							.target(target).clr(1, 0.6f + 0.3f * world.rand.nextFloat(), 0.2f).spawn(world);
				}
			}
		}
	}

	@Override
	public void despawn(){
		this.playSound(WizardrySounds.ENTITY_RADIANT_TOTEM_VANISH, 1, 1);
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
