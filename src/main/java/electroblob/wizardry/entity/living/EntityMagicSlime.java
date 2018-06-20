package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

/** As of Wizardry 1.2, this is now an ISummonedCreature like the rest of them, and it extends EntitySlime. */
public class EntityMagicSlime extends EntitySlime implements ISummonedCreature {

	// Field implementations
	private int lifetime = 200;
	private WeakReference<EntityLivingBase> casterReference;
	private UUID casterUUID;

	// Setter + getter implementations
	@Override public int getLifetime(){ return lifetime; }
	@Override public void setLifetime(int lifetime){ this.lifetime = lifetime; }
	@Override public WeakReference<EntityLivingBase> getCasterReference(){ return casterReference; }
	@Override public void setCasterReference(WeakReference<EntityLivingBase> reference){ casterReference = reference; }
	@Override public UUID getCasterUUID(){ return casterUUID; }
	@Override public void setCasterUUID(UUID uuid){ this.casterUUID = uuid; }

	public EntityMagicSlime(World world){
		super(world);
		// TESTME: Should this be true or false? Has something to do with health.
		this.setSlimeSize(2, false); // Needs to be called before setting the experience value to 0
		this.experienceValue = 0;
	}

	/**
	 * Creates a new magic slime with the given caster and lifetime, riding the given target.
	 * 
	 * @param world The world that the slime is in.
	 * @param caster The entity that created the slime.
	 * @param target The slime's victim. The slime will automatically start riding this entity.
	 * @param lifetime The number of ticks before the slime bursts.
	 */
	public EntityMagicSlime(World world, EntityLivingBase caster, EntityLivingBase target, int lifetime){
		super(world);
		this.setPosition(target.posX, target.posY, target.posZ);
		this.startRiding(target);
		this.casterReference = new WeakReference<EntityLivingBase>(caster);
		this.setSlimeSize(2, false); // Needs to be called before setting the experience value to 0
		this.experienceValue = 0;
		this.lifetime = lifetime;
	}

	// EntitySlime overrides

	@Override protected void initEntityAI(){} // Has no AI!
	@Override protected void dealDamage(EntityLivingBase entity){} // Handles damage itself

	@Override
	public void setDead(){
		// Restores behaviour from Entity, replacing slime splitting behaviour.
		this.isDead = true;
		// Makes sure that the undoing in onUpdate won't undo this. For some reason, EntitySlime sets isDead directly
		// to do the peaceful despawning, which seems odd but is actually rather handy!
		this.setHealth(0);
		// Bursting effect
		for(int i = 0; i < 30; i++){
			double x = this.posX - 0.5 + rand.nextDouble();
			double y = this.posY - 0.5 + rand.nextDouble();
			double z = this.posZ - 0.5 + rand.nextDouble();
			this.world.spawnParticle(EnumParticleTypes.SLIME, x, y, z, (x - this.posX) * 2, (y - this.posY) * 2,
					(z - this.posZ) * 2);
		}
		this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 2.5f, 0.6f);
		this.playSound(SoundEvents.ENTITY_FIREWORK_BLAST_FAR, 1.0f, 0.5f);
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata){
		// Removes size randomisation
		IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
		this.setSlimeSize(2, false);
		return data;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount){
		// Immune to suffocation
		return source == DamageSource.IN_WALL ? false : super.attackEntityFrom(source, amount);
	}

	// Implementations

	@Override
	public void setRevengeTarget(EntityLivingBase entity){
		if(this.shouldRevengeTarget(entity)) super.setRevengeTarget(entity);
	}

	@Override
	public void onUpdate(){

		super.onUpdate();
		// Undoes the despawning on peaceful behaviour. I don't think there's anything in super.onUpdate that sets
		// isDead other than that, but it's better to do a quick sanity check just to be sure.
		if(this.isDead && world.getDifficulty() == EnumDifficulty.PEACEFUL && this.getHealth() > 0) this.isDead = false;
		// Bursts instantly rather than doing the falling over animation.
		if(this.getHealth() <= 0) this.setDead();

		this.updateDelegate();

		// Damages and slows the slime's victim or makes the slime explode if the victim is dead.
		if(this.getRidingEntity() != null && this.getRidingEntity() instanceof EntityLivingBase
				&& ((EntityLivingBase)this.getRidingEntity()).getHealth() > 0){
			if(this.ticksExisted % 16 == 1){
				this.getRidingEntity().attackEntityFrom(DamageSource.MAGIC, 1);
				((EntityLivingBase)this.getRidingEntity())
						.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 20, 2));
				this.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0f, 1.0f);
				this.squishAmount = 0.5F;
			}
		}else{
			this.setDead();
		}
	}

	@Override
	public void onSpawn(){
	}

	@Override
	public void onDespawn(){
	}

	@Override
	public boolean hasParticleEffect(){
		return false;
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand){
		// In this case, the delegate method determines whether super is called.
		// Rather handily, we can make use of Java's short-circuiting method of evaluating OR statements.
		return this.interactDelegate(player, hand) || super.processInteract(player, hand);
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		this.writeNBTDelegate(nbttagcompound);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		this.readNBTDelegate(nbttagcompound);
	}

	// Recommended overrides

	@Override protected int getExperiencePoints(EntityPlayer player){ return 0; }
	@Override protected boolean canDropLoot(){ return false; }
	@Override protected Item getDropItem(){ return null; }
	@Override protected ResourceLocation getLootTable(){ return null; }
	@Override public boolean canPickUpLoot(){ return false; }
	// This vanilla method has nothing to do with the custom despawn() method.
	@Override protected boolean canDespawn(){ return false; }

}
