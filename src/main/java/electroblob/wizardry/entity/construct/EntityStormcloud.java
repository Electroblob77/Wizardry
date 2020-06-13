package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

import java.util.List;

public class EntityStormcloud extends EntityMagicConstruct {

//	private static final DataParameter<Float> RADIUS = new DataParameter<>(21, DataSerializers.FLOAT);

	public EntityStormcloud(World world){
		super(world);
		this.height = 2.0f;
		this.width = Spells.stormcloud.getProperty(Spell.EFFECT_RADIUS).floatValue() * 2;
	}

//	@Override
//	protected void entityInit(){
//		super.entityInit();
//		this.getDataManager().register(RADIUS, width); // TODO: This doesn't work, implement blast modifiers properly into SpellConstruct
//	}

//	public void multiplyWidth(float multiplier){
//		this.setSize(width * multiplier, height);
//		this.getDataManager().set(RADIUS, width);
//	}

	public void onUpdate(){

		super.onUpdate();

//		if(world.isRemote){
//			float radius = this.getDataManager().get(RADIUS);
//			if(radius != width) this.setSize(radius, height);
//		}else{
//			if(this.getDataManager().get(RADIUS) != width) this.getDataManager().set(RADIUS, width);
//		}

//		if(this.ticksExisted % 35 == 0) this.playSound(WizardrySounds.ENTITY_STORMCLOUD_AMBIENT, 1, 1);

		if(this.world.isRemote){

			float areaFactor = (width * width) / 36; // Ensures cloud/raindrop density stays the same for different sizes

			for(int i = 0; i < 2 * areaFactor; i++) ParticleBuilder.create(Type.CLOUD, this)
					.clr(0.3f, 0.3f, 0.3f).shaded(true).spawn(world);
		}

		List<EntityLivingBase> targets = world.getEntitiesWithinAABB(EntityLivingBase.class,
				this.getEntityBoundingBox().expand(0, -10, 0));

		float damage = Spells.stormcloud.getProperty(Spell.DAMAGE).floatValue() * this.damageMultiplier;

		for(EntityLivingBase target : targets){

			if(this.isValidTarget(target)){

				if(target.ticksExisted % 150 == 0){ // Use target's lifetime so they don't all get hit at once, looks better

					if(!this.world.isRemote){
						EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeIndirectMagicDamage(
								this, this.getCaster(), MagicDamage.DamageType.SHOCK), damage);
					}else{
						ParticleBuilder.create(Type.LIGHTNING).pos(target.posX, posY + height/2, target.posZ)
								.target(target).scale(2).spawn(world);
						ParticleBuilder.spawnShockParticles(world, target.posX, target.getEntityBoundingBox().minY + target.height, target.posZ);
					}

					target.playSound(WizardrySounds.ENTITY_STORMCLOUD_THUNDER, 1, 1.6f);
					target.playSound(WizardrySounds.ENTITY_STORMCLOUD_ATTACK, 1, 1);
				}
			}
		}

//		BlockPos pos = new BlockPos(this);
//
//		for(int x = -(int)(this.width/2); x <= this.width/2 + 0.5; x++){
//			for(int z = -(int)(this.width/2); z <= this.width/2 + 0.5; z++){
//
//				int y = WizardryUtilities.getNearestFloor()
//
//			}
//		}

	}

	// Need to sync the caster so they don't have particles spawned at them

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		if(getCaster() != null) data.writeInt(getCaster().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf data){

		super.readSpawnData(data);

		if(!data.isReadable()) return;

		Entity entity = world.getEntityByID(data.readInt());

		if(entity instanceof EntityLivingBase){
			setCaster((EntityLivingBase)entity);
		}else{
			Wizardry.logger.warn("Stormcloud caster with ID in spawn data not found");
		}
	}

}
