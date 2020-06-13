package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.living.EntityHuskMinion;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.SpellMinion;
import electroblob.wizardry.spell.ZombieApocalypse;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityZombieSpawner extends EntityMagicConstruct {

	private static final double MAX_NUDGE_DISTANCE = 0.1; // Prevents zombies all bunching up directly below the spawner

	public boolean spawnHusks;

	private int spawnTimer = 10;

	public EntityZombieSpawner(World world){
		super(world);
		this.setSize(4, 2);
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(lifetime - ticksExisted > 10 && spawnTimer-- == 0){

			this.playSound(WizardrySounds.ENTITY_ZOMBIE_SPAWNER_SPAWN, 1, 1);

			if(!world.isRemote){

				EntityZombieMinion zombie = spawnHusks ? new EntityHuskMinion(world) : new EntityZombieMinion(world);

				zombie.setPosition(this.posX + (rand.nextDouble() * 2 - 1) * MAX_NUDGE_DISTANCE, this.posY,
						this.posZ + (rand.nextDouble() * 2 - 1) * MAX_NUDGE_DISTANCE);
				zombie.setCaster(this.getCaster());
				// Modifier implementation
				// Attribute modifiers are pretty opaque, see https://minecraft.gamepedia.com/Attribute#Modifiers
				zombie.setLifetime(Spells.zombie_apocalypse.getProperty(SpellMinion.MINION_LIFETIME).intValue());
				IAttributeInstance attribute = zombie.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
				attribute.applyModifier(new AttributeModifier(SpellMinion.POTENCY_ATTRIBUTE_MODIFIER,
						damageMultiplier - 1, EntityUtils.Operations.MULTIPLY_CUMULATIVE));
				zombie.setHealth(zombie.getMaxHealth()); // Need to set this because we may have just modified the value
				zombie.hurtResistantTime = 30; // Prevent fall damage
				zombie.hideParticles(); // Hide spawn particles or they pop out the top of the hidden box

				world.spawnEntity(zombie);
			}

			spawnTimer += Spells.zombie_apocalypse.getProperty(ZombieApocalypse.MINION_SPAWN_INTERVAL).intValue() + rand.nextInt(20);
		}

		if(world.isRemote){

			float b = 0.15f;

			for(double r = 1.5; r < 4; r += 0.2){
				ParticleBuilder.create(Type.CLOUD).clr(b-=0.02, 0, 0).pos(posX, posY - 0.3, posZ).scale(0.5f / (float)r)
						.spin(r, 0.02/r * (1 + world.rand.nextDouble())).spawn(world);
			}

		}

	}

	@Override
	public boolean shouldRenderInPass(int pass){
		return super.shouldRenderInPass(pass);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt){
		super.writeEntityToNBT(nbt);
		nbt.setInteger("spawnTimer", spawnTimer);
		nbt.setBoolean("spawnHusks", spawnHusks);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt){
		super.readEntityFromNBT(nbt);
		this.spawnTimer = nbt.getInteger("spawnTimer");
		this.spawnHusks = nbt.getBoolean("spawnHusks");
	}

}
