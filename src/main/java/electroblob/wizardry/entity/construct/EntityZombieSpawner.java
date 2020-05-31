package electroblob.wizardry.entity.construct;

import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.SpellMinion;
import electroblob.wizardry.spell.ZombieApocalypse;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities.Operations;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.world.World;

public class EntityZombieSpawner extends EntityMagicConstruct {

	public EntityZombieSpawner(World world){
		super(world);
		this.setSize(4, 2);
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(lifetime - ticksExisted > 10 && rand.nextInt(Spells.zombie_apocalypse.getProperty(ZombieApocalypse.MINION_SPAWN_INTERVAL).intValue()) == 0){

			if(world.isRemote){
				this.playSound(WizardrySounds.ENTITY_ZOMBIE_SPAWNER_SPAWN, 1, 1);

			}else{

				EntityZombieMinion zombie = new EntityZombieMinion(world);

				zombie.setPosition(this.posX, this.posY, this.posZ);
				zombie.setCaster(this.getCaster());
				// Modifier implementation
				// Attribute modifiers are pretty opaque, see https://minecraft.gamepedia.com/Attribute#Modifiers
				zombie.setLifetime(Spells.zombie_apocalypse.getProperty(SpellMinion.MINION_LIFETIME).intValue());
				IAttributeInstance attribute = zombie.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
				attribute.applyModifier(new AttributeModifier(SpellMinion.POTENCY_ATTRIBUTE_MODIFIER,
						damageMultiplier - 1, Operations.MULTIPLY_CUMULATIVE));
				zombie.setHealth(zombie.getMaxHealth()); // Need to set this because we may have just modified the value
				zombie.hurtResistantTime = 30; // Prevent fall damage
				zombie.hideParticles(); // Hide spawn particles or they pop out the top of the hidden box

				world.spawnEntity(zombie);
			}
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
}
