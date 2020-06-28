package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityCombustionRune extends EntityScaledConstruct {

	public EntityCombustionRune(World world){
		super(world);
		setSize(2, 0.2f);
	}

	@Override
	protected boolean shouldScaleWidth(){
		return false; // We're using the blast modifier for an actual explosion here, rather than the entity size
	}

	@Override
	protected boolean shouldScaleHeight(){
		return false;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(width/2, posX, posY, posZ, world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					float strength = Spells.combustion_rune.getProperty(Spell.BLAST_RADIUS).floatValue() * sizeMultiplier;

					world.newExplosion(this.getCaster(), this.posX, this.posY, this.posZ, strength, true,
							getCaster() != null && EntityUtils.canDamageBlocks(getCaster(), world));

					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble() * 0.3;
			float angle = rand.nextFloat() * (float)Math.PI * 2;
			world.spawnParticle(EnumParticleTypes.FLAME, this.posX + radius * MathHelper.cos(angle), this.posY + 0.1,
					this.posZ + radius * MathHelper.sin(angle), 0, 0, 0);
		}
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

}
