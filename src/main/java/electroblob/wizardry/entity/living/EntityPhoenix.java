package electroblob.wizardry.entity.living;

import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class EntityPhoenix extends EntitySummonedCreature implements ISpellCaster {

	private double AISpeed = 0.5;

	// Can attack for 7 seconds, then must cool down for 3.
	private EntityAIAttackSpell<EntityPhoenix> spellAttackAI = new EntityAIAttackSpell<>(this, AISpeed, 15f, 60, 140);

	private Spell continuousSpell;
	private int spellCounter;

	private static final List<Spell> attack = Collections.singletonList(Spells.flame_ray);

	/** Creates a new phoenix in the given world. */
	public EntityPhoenix(World world){
		super(world);
		this.isImmuneToFire = true;
		this.height = 2.0f;
		// For some reason this can't be in initEntityAI
		this.tasks.addTask(1, this.spellAttackAI);
	}

	@Override
	protected void initEntityAI(){

		this.tasks.addTask(0, new EntityAIWatchClosest(this, EntityLivingBase.class, 0));
		// this.tasks.addTask(2, new EntityAIWander(this, AISpeed));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		// this.targetTasks.addTask(0, new EntityAIMoveTowardsTarget(this, 1, 10));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));

		this.setAIMoveSpeed((float)AISpeed);
	}

	@Override
	public List<Spell> getSpells(){
		return attack;
	}

	@Override
	public SpellModifiers getModifiers(){
		return new SpellModifiers();
	}

	@Override
	public Spell getContinuousSpell(){
		return continuousSpell;
	}

	@Override
	public void setContinuousSpell(Spell spell){
		continuousSpell = spell;
	}

	@Override
	public void setSpellCounter(int count){
		spellCounter = count;
	}

	@Override
	public int getSpellCounter(){
		return spellCounter;
	}

	@Override
	public boolean hasRangedAttack(){
		return true;
	}

	@Override
	// Makes the flames come from the phoenix's head rather than its body
	public float getEyeHeight(){
		return 2.1f;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return WizardrySounds.ENTITY_PHOENIX_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_PHOENIX_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_PHOENIX_DEATH;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(){
		return 15728880;
	}

	@Override
	public float getBrightness(){
		return 1.0F;
	}

	@Override
	public void onSpawn(){
		this.spawnParticleEffect();
	}

	@Override
	public void onDespawn(){
		this.spawnParticleEffect();
	}

	private void spawnParticleEffect(){
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				this.world.spawnParticle(EnumParticleTypes.FLAME, this.posX + this.rand.nextFloat(),
						this.posY + 1 + this.rand.nextFloat(), this.posZ + this.rand.nextFloat(), 0, 0, 0);
			}
		}
	}

	@Override
	public int getAnimationColour(float animationProgress){
		return DrawingUtils.mix(0xffdd4d, 0xff6600, animationProgress);
	}

	@Override
	public void onLivingUpdate(){

		// Makes the phoenix hover.
		Integer floorLevel = WizardryUtilities.getNearestFloor(world, new BlockPos(this), 4);

		if(floorLevel == null || this.posY - floorLevel > 3){
			this.motionY = -0.1;
		}else if(this.posY - floorLevel < 2){
			this.motionY = 0.1;
		}else{
			this.motionY = 0.0;
		}

		// Living sound
		if(this.rand.nextInt(24) == 0){
			this.playSound(WizardrySounds.ENTITY_PHOENIX_BURN, 1.0F + this.rand.nextFloat(),
					this.rand.nextFloat() * 0.7F + 0.3F);
		}

		// Flapping sound effect
		if(this.ticksExisted % 22 == 0){
			this.playSound(WizardrySounds.ENTITY_PHOENIX_FLAP, 1.0F, 1.0f);
		}

		for(int i = 0; i < 2; i++){
			this.world.spawnParticle(EnumParticleTypes.FLAME,
					this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
					this.posY + this.height / 2 + this.rand.nextDouble() * (double)this.height / 2,
					this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0.0D, -0.1D, 0.0D);
		}

		// Adding this allows the phoenix to attack despite being in the air. However, for some strange reason
		// it will only attack when within about 3 blocks of the ground. Any higher and it just sits there, not even
		// attempting to find targets.

		this.onGround = true;

		super.onLivingUpdate();
	}

	@Override
	public void fall(float distance, float damageMultiplier){} // Immune to fall damage

	@Override
	public boolean isBurning(){
		return false;
	}
}
