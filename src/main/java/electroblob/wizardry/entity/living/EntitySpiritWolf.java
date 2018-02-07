package electroblob.wizardry.entity.living;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/**
 * Does not implement ISummonedCreature because it has different despawning rules and because EntityWolf already has an
 * owner system.
 */
public class EntitySpiritWolf extends EntityWolf {

	public EntitySpiritWolf(World par1World){

		super(par1World);
		this.experienceValue = 0;
	}

	@Override
	protected void initEntityAI(){

		this.aiSit = new EntityAISit(this);
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.tasks.addTask(2, this.aiSit);
		this.tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
		this.tasks.addTask(4, new EntityAIAttackMelee(this, 1.0D, true));
		this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
		this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
		this.tasks.addTask(9, new EntityAILookIdle(this));
		this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
		this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
		this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true, new Class[0]));
	}

	@Override
	public void onDeath(DamageSource source){

		// Allows player to summon another spirit wolf once this one has died.
		// NOTE: This has been known to work incorrectly.
		if(this.getOwner() instanceof EntityPlayer && WizardData.get((EntityPlayer)this.getOwner()) != null){
			WizardData.get((EntityPlayer)this.getOwner()).hasSpiritWolf = false;
		}

		super.onDeath(source);
	}

	@Override
	protected int getExperiencePoints(EntityPlayer p_70693_1_){
		return 0;
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata){

		// Adds Particles on spawn. Due to client/server differences this cannot be done
		// in the item.
		if(this.world.isRemote){
			for(int i = 0; i < 15; i++){
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world,
						this.posX - this.width / 2 + this.rand.nextFloat() * width,
						this.posY + this.height * this.rand.nextFloat() + 0.2f,
						this.posZ - this.width / 2 + this.rand.nextFloat() * width, 0, 0, 0, 48 + this.rand.nextInt(12),
						0.8f, 0.8f, 1.0f);
			}
		}

		return livingdata;
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		// Adds a dust particle effect
		if(this.world.isRemote){
			Wizardry.proxy.spawnParticle(WizardryParticleType.DUST, world,
					this.posX - this.width / 2 + this.rand.nextFloat() * width,
					this.posY + this.height * this.rand.nextFloat() + 0.2f,
					this.posZ - this.width / 2 + this.rand.nextFloat() * width, 0, 0, 0, 0, 0.8f, 0.8f, 1.0f);
		}
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		if(this.isTamed()){

			// Allows the owner (but not other players) to dispel the spirit wolf using a
			// wand.
			if(stack.getItem() instanceof ItemWand && this.getOwner() == player && player.isSneaking()){
				// Prevents accidental double clicking.
				if(this.ticksExisted > 20){
					for(int i = 0; i < 10; i++){
						Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world,
								this.posX - this.width / 2 + this.rand.nextFloat() * width,
								this.posY + this.height * this.rand.nextFloat() + 0.2f,
								this.posZ - this.width / 2 + this.rand.nextFloat() * width, 0, 0, 0,
								48 + this.rand.nextInt(12), 0.8f, 0.8f, 1.0f);
					}
					this.setDead();
					if(WizardData.get(player) != null){
						WizardData.get(player).hasSpiritWolf = false;
					}
					this.playSound(WizardrySounds.SPELL_HEAL, 0.7F, rand.nextFloat() * 0.4F + 1.0F);
					// This is necessary to prevent the wand's spell being cast when performing this
					// action.
					return true;
				}
			}
		}

		return super.processInteract(player, hand);

	}

	@Override
	public EntityWolf createChild(EntityAgeable par1EntityAgeable){
		return null;
	}

	@Override
	protected Item getDropItem(){
		return null;
	}

	@Override
	public ITextComponent getDisplayName(){
		if(getOwner() != null){
			return new TextComponentTranslation(ISummonedCreature.NAMEPLATE_TRANSLATION_KEY, getOwner().getName(),
					new TextComponentTranslation("entity." + this.getEntityString() + ".name"));
		}else{
			return super.getDisplayName();
		}
	}

	@Override
	public boolean hasCustomName(){
		// If this returns true, the renderer will show the nameplate when looking
		// directly at the entity
		return Wizardry.settings.showSummonedCreatureNames && getOwner() != null;
	}

}
