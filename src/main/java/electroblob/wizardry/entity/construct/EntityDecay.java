package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityDecay extends EntityMagicConstruct {

	public int textureIndex;

	public EntityDecay(World world){
		super(world);
		textureIndex = this.rand.nextInt(10);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.rand.nextInt(700) == 0 && this.ticksExisted + 100 < lifetime)
			this.playSound(WizardrySounds.ENTITY_DECAY_AMBIENT, 0.2F + rand.nextFloat() * 0.2F,
					0.6F + rand.nextFloat() * 0.15F);

		if(!this.world.isRemote){
			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(1.0d, this.posX, this.posY,
					this.posZ, this.world);
			for(EntityLivingBase target : targets){
				if(target != this.getCaster()){
					// If this check wasn't here the potion would be reapplied every tick and hence the entity would be
					// damaged each tick.
					// In this case, we do want particles to be shown.
					if(!target.isPotionActive(WizardryPotions.decay))
						target.addPotionEffect(new PotionEffect(WizardryPotions.decay,
								Spells.decay.getProperty(Spell.EFFECT_DURATION).intValue(), 0));
				}
			}
			
		}else if(this.rand.nextInt(15) == 0){
			
			double radius = rand.nextDouble() * 0.8;
			float angle = rand.nextFloat() * (float)Math.PI * 2;
			float brightness = rand.nextFloat() * 0.4f;
			
			ParticleBuilder.create(Type.DARK_MAGIC)
			.pos(this.posX + radius * MathHelper.cos(angle), this.posY, this.posZ + radius * MathHelper.sin(angle))
			.clr(brightness, 0, brightness + 0.1f)
			.spawn(world);
		}
	}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}
	
}
