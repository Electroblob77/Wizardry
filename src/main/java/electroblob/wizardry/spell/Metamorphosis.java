package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Metamorphosis extends Spell {

	public Metamorphosis(){
		super(Tier.APPRENTICE, 15, Element.NECROMANCY, "metamorphosis", SpellType.UTILITY, 30, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){

			Entity entityHit = rayTrace.entityHit;
			double xPos = entityHit.posX;
			double yPos = entityHit.posY;
			double zPos = entityHit.posZ;

			EntityLiving newEntity = null;

			// IDEA: Interaction with husks/strays?
			// TODO: Replace with a bimap or something.
			if(entityHit instanceof EntityPig){
				newEntity = new EntityPigZombie(world);
			}else if(entityHit instanceof EntityPigZombie){
				newEntity = new EntityPig(world);
			}else if(entityHit instanceof EntitySkeleton){
				newEntity = new EntityWitherSkeleton(world);
			}else if(entityHit instanceof EntityWitherSkeleton){
				newEntity = new EntitySkeleton(world);
			}else if(entityHit instanceof EntityCow && !(entityHit instanceof EntityMooshroom)){
				newEntity = new EntityMooshroom(world);
			}else if(entityHit instanceof EntityMooshroom){
				newEntity = new EntityCow(world);
			}else if(entityHit instanceof EntityChicken){
				newEntity = new EntityBat(world);
			}else if(entityHit instanceof EntityBat){
				newEntity = new EntityChicken(world);
			}else if(entityHit instanceof EntitySlime && !(entityHit instanceof EntityMagmaCube)){
				newEntity = new EntityMagmaCube(world);
			}else if(entityHit instanceof EntityMagmaCube){
				newEntity = new EntitySlime(world);
			}else if(entityHit instanceof EntitySpider && !(entityHit instanceof EntityCaveSpider)){
				newEntity = new EntityCaveSpider(world);
			}else if(entityHit instanceof EntityCaveSpider){
				newEntity = new EntitySpider(world);
			}

			if(newEntity != null){

				if(!world.isRemote && newEntity != null){
					// Transfers attributes from the old entity to the new one.
					newEntity.setHealth(((EntityLiving)entityHit).getHealth());
					// newEntity.writeToNBT(entityHit.getEntityData());

					entityHit.setDead();
					newEntity.setPosition(xPos, yPos, zPos);
					world.spawnEntity(newEntity);
				}

				if(world.isRemote){
					for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
						// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
						double x1 = caster.posX + look.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
						double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord * i / 2
								+ world.rand.nextFloat() / 5 - 0.1f;
						double z1 = caster.posZ + look.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
						// world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
						Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
								12 + world.rand.nextInt(8), 0.2f, 0.0f, 0.1f);
					}
					for(int i = 0; i < 5; i++){
						Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, xPos, yPos, zPos, 0.0d,
								0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
					}
				}

				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_DEFLECTION, 0.5F, 0.8f);
				return true;
			}
		}
		return false;
	}

}
