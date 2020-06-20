package electroblob.wizardry.spell;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.*;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Metamorphosis extends SpellRay {

	public static final BiMap<Class<? extends EntityLivingBase>, Class<? extends EntityLivingBase>> TRANSFORMATIONS = HashBiMap.create();

	static {
		addTransformation(EntityPig.class, EntityPigZombie.class);
		addTransformation(EntityCow.class, EntityMooshroom.class);
		addTransformation(EntityChicken.class, EntityBat.class);
		addTransformation(EntityZombie.class, EntityHusk.class);
		addTransformation(EntitySkeleton.class, EntityStray.class, EntityWitherSkeleton.class);
		addTransformation(EntitySpider.class, EntityCaveSpider.class);
		addTransformation(EntitySlime.class, EntityMagmaCube.class);
		addTransformation(EntityZombieMinion.class, EntityHuskMinion.class);
		addTransformation(EntitySkeletonMinion.class, EntityStrayMinion.class, EntityWitherSkeletonMinion.class);
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
	
	public Metamorphosis(){
		super("metamorphosis", SpellActions.POINT, false);
		this.soundValues(0.5f, 1f, 0);
	}
	
	@Override public boolean canBeCastBy(EntityLiving npc, boolean override) { return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target)){

			double xPos = target.posX;
			double yPos = target.posY;
			double zPos = target.posZ;

			// Sneaking allows the entities to be cycled through in the other direction.
			// Dispensers always cycle through entities in the normal direction.
			Class<? extends EntityLivingBase> newEntityClass = caster != null && caster.isSneaking() ?
					TRANSFORMATIONS.inverse().get(target.getClass()) : TRANSFORMATIONS.get(target.getClass());

			if(newEntityClass == null) return false;

			EntityLivingBase newEntity = null;

			try {
				newEntity = newEntityClass.getConstructor(World.class).newInstance(world);
			} catch (Exception e){
				Wizardry.logger.error("Error while attempting to transform entity " + target.getClass() + " to entity "
						+ newEntityClass);
				e.printStackTrace();
			}
			
			if(newEntity == null) return false;

			if(!world.isRemote){
				// Transfers attributes from the old entity to the new one.
				newEntity.setHealth(((EntityLivingBase)target).getHealth());
				NBTTagCompound tag = new NBTTagCompound();
				target.writeToNBT(tag);
				// Remove the UUID because keeping it the same causes the entity to disappear
				NBTExtras.removeUniqueId(tag, "UUID");
				newEntity.readFromNBT(tag);

				target.setDead();
				newEntity.setPosition(xPos, yPos, zPos);
				world.spawnEntity(newEntity);
				
			}else{
				for(int i=0; i<20; i++){
					ParticleBuilder.create(Type.DARK_MAGIC, world.rand, xPos, yPos + 1, zPos, 1, false)
							.clr(0.1f, 0, 0).spawn(world);
				}
				ParticleBuilder.create(Type.BUFF).pos(xPos, yPos, zPos).clr(0xd363cb).spawn(world);
			}

			this.playSound(world, (EntityLivingBase)target, ticksInUse, -1, modifiers);
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
