package electroblob.wizardry.spell;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntityWitherSkeletonMinion;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
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
	
	public Metamorphosis(){
		super("metamorphosis", Tier.APPRENTICE, Element.NECROMANCY, SpellType.UTILITY, 15, 30, false, 10, WizardrySounds.SPELL_DEFLECTION);
		this.soundValues(0.5f, 0.8f, 0);
	}
	
	@Override public boolean canBeCastByNPCs() { return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			double xPos = target.posX;
			double yPos = target.posY;
			double zPos = target.posZ;

			// Sneaking allows the entities to be cycled through in the other direction.
			Class<? extends EntityLivingBase> newEntityClass = caster.isSneaking() ?
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
				WizardryUtilities.removeUniqueId(tag, "UUID");
				newEntity.readFromNBT(tag);

				target.setDead();
				newEntity.setPosition(xPos, yPos, zPos);
				world.spawnEntity(newEntity);
				
			}else{
				for(int i=0; i<5; i++){
					ParticleBuilder.create(Type.DARK_MAGIC).pos(xPos, yPos, zPos).clr(0.1f, 0, 0).spawn(world);
				}
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
