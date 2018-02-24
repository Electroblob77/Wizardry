package electroblob.wizardry.spell;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntityWitherSkeletonMinion;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
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

	public static final BiMap<Class<? extends EntityLivingBase>, Class<? extends EntityLivingBase>> TRANSFORMATIONS = HashBiMap.create();

	public Metamorphosis(){
		super(Tier.APPRENTICE, 15, Element.NECROMANCY, "metamorphosis", SpellType.UTILITY, 30, EnumAction.NONE, false);

		addTransformation(EntityPig.class, EntityPigZombie.class);
		addTransformation(EntityCow.class, EntityMooshroom.class);
		addTransformation(EntityChicken.class, EntityBat.class);
		addTransformation(EntityZombie.class, EntityHusk.class);
		addTransformation(EntitySkeleton.class, EntityWitherSkeleton.class, EntityStray.class);
		addTransformation(EntitySpider.class, EntityCaveSpider.class);
		addTransformation(EntitySlime.class, EntityMagmaCube.class);
		addTransformation(EntitySkeletonMinion.class, EntityWitherSkeletonMinion.class);
	}

	/** Adds circular mappings between the given entity classes to the transformations map. In other words, given an
	 * array of entity classes [A, B, C, D], adds mappings A -> B, B -> C, C -> D and D -> A. */
	@SafeVarargs
	public static void addTransformation(Class<? extends EntityLivingBase>... entities){
		Class<? extends EntityLivingBase> previousEntity = entities[entities.length - 1];
		for(Class<? extends EntityLivingBase> entity : entities){
			TRANSFORMATIONS.put(previousEntity, entity);
			previousEntity = entity;
		}
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.entityHit != null && WizardryUtilities.isLiving(rayTrace.entityHit)){

			Entity entityHit = rayTrace.entityHit;
			double xPos = entityHit.posX;
			double yPos = entityHit.posY;
			double zPos = entityHit.posZ;

			// Sneaking allows the entities to be cycled through in the other direction.
			Class<? extends EntityLivingBase> newEntityClass = caster.isSneaking() ?
					TRANSFORMATIONS.inverse().get(entityHit.getClass()) : TRANSFORMATIONS.get(entityHit.getClass());

			if(newEntityClass == null) return false;

			EntityLivingBase newEntity = null;

			try {
				newEntity = newEntityClass.getConstructor(World.class).newInstance(world);
			} catch (Exception e){
				Wizardry.logger.error("Error while attempting to transform entity " + entityHit.getClass() + " to entity " + newEntityClass);
				e.printStackTrace();
			}
			
			if(newEntity == null) return false;

			if(!world.isRemote){
				// Transfers attributes from the old entity to the new one.
				newEntity.setHealth(((EntityLivingBase)entityHit).getHealth());
				newEntity.readFromNBT(entityHit.getEntityData());

				entityHit.setDead();
				newEntity.setPosition(xPos, yPos, zPos);
				world.spawnEntity(newEntity);
				
			}else{
				
				for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
					// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
					double x1 = caster.posX + look.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.y * i / 2
							+ world.rand.nextFloat() / 5 - 0.1f;
					double z1 = caster.posZ + look.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
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
		return false;
	}

}
