package electroblob.wizardry.entity.living;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class EntityStormElemental extends EntitySummonedCreature implements ISpellCaster {

	private double AISpeed = 1.0;

	private EntityAIAttackSpell<EntityStormElemental> spellAttackAI = new EntityAIAttackSpell<EntityStormElemental>(this, AISpeed, 15f, 30, 0);

	private static final List<Spell> attack = Collections.singletonList(Spells.lightning_disc);

	/** Creates a new storm elemental in the given world. */
	public EntityStormElemental(World world){
		super(world);
		// For some reason this can't be in initEntityAI
		this.tasks.addTask(0, this.spellAttackAI);
	}

	@Override
	protected void initEntityAI(){

		this.tasks.addTask(1, new EntityAIAttackMelee(this, AISpeed, false));
		this.tasks.addTask(2, new EntityAIWander(this, AISpeed));
		this.tasks.addTask(3, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
				0, false, true, this.getTargetSelector()));

		this.setAIMoveSpeed((float)AISpeed);
	}

	@Override
	public boolean hasRangedAttack(){
		return true;
	}

	@Override
	public List<Spell> getSpells(){
		return attack;
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(AISpeed);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0D);
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return WizardrySounds.ENTITY_STORM_ELEMENTAL_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_STORM_ELEMENTAL_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_STORM_ELEMENTAL_DEATH;
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
	public void onLivingUpdate(){

		if(this.ticksExisted % 120 == 1){
			this.playSound(WizardrySounds.ENTITY_STORM_ELEMENTAL_WIND, 1.0f, 1.0f);
		}

		if(this.rand.nextInt(24) == 0){
			this.playSound(WizardrySounds.ENTITY_STORM_ELEMENTAL_BURN, 1.0F + this.rand.nextFloat(),
					this.rand.nextFloat() * 0.7F + 0.3F);
		}

		// Slow fall
		if(!this.onGround && this.motionY < 0.0D){
			this.motionY *= 0.6D;
		}

		if(world.isRemote){

			for(int i=0; i<2; ++i){
				
				world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
						this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.width,
						this.posY + this.rand.nextDouble() * (double)this.height,
						this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.width, 0, 0, 0);
				
				ParticleBuilder.create(Type.SPARK, this).spawn(world);
			}

			for(int i=0; i<10; i++){
				
				float brightness = rand.nextFloat() * 0.2f;
				double dy = this.rand.nextDouble() * (double)this.height;
				
				ParticleBuilder.create(Type.SPARKLE).pos(this.posX, this.posY + dy, this.posZ)
				.time(20 + rand.nextInt(10)).clr(0, brightness, brightness)//.entity(this)
				.spin(0.2 + 0.5 * dy, 0.1 + 0.05 * world.rand.nextDouble()).spawn(world);
			}
		}

		super.onLivingUpdate();
	}

	@Override
	public void fall(float distance, float damageMultiplier){
		// Immune to fall damage.
	}

	@Override
	public void onStruckByLightning(EntityLightningBolt lightning){
		// Immune to lightning.
	}
}