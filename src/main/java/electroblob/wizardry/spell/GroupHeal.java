package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntitySummonedCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class GroupHeal extends Spell {

	public GroupHeal() {
		super(EnumTier.ADVANCED, 35, EnumElement.HEALING, "group_heal", EnumSpellType.DEFENCE, 150, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		boolean flag = false;

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(5*blastMultiplier, caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){

			if(target instanceof EntityPlayer){

				if(WizardryUtilities.isPlayerAlly(caster, (EntityPlayer)target) || target == caster){

					if(((EntityPlayer)target).shouldHeal()){

						target.heal((int)(6*damageMultiplier));

						if(world.isRemote){
							for(int i=0; i<10; i++){
								double d0 = (double)((float)target.posX + world.rand.nextFloat()*2 - 1.0F);
								double d1 = (double)((float)WizardryUtilities.getPlayerEyesPos((EntityPlayer)target) - 0.5F + world.rand.nextFloat());
								double d2 = (double)((float)target.posZ + world.rand.nextFloat()*2 - 1.0F);
								Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, d0, d1, d2, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
							}
						}

						world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
						flag = true;
					}
				}
			// Now also works on summoned creatures
			}else if(target instanceof EntitySummonedCreature){

				EntityLivingBase summoner = ((EntitySummonedCreature)target).getCaster();

				if(summoner == caster || (summoner instanceof EntityPlayer && WizardryUtilities.isPlayerAlly(caster, (EntityPlayer)summoner))){

					if(target.getHealth() < target.getMaxHealth()){
						
						target.heal((int)(6*damageMultiplier));

						if(world.isRemote){
							for(int i=0; i<10; i++){
								double d0 = (double)((float)target.posX + world.rand.nextFloat()*2 - 1.0F);
								double d1 = (double)((float)WizardryUtilities.getPlayerEyesPos((EntityPlayer)target) - 0.5F + world.rand.nextFloat());
								double d2 = (double)((float)target.posZ + world.rand.nextFloat()*2 - 1.0F);
								Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, d0, d1, d2, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
							}
						}

						world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
						flag = true;
					}
				}
			}
		}

		return flag;
	}


}
