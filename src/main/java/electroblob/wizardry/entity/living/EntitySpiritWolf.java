package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/**
 * Does not implement ISummonedCreature because it has different despawning rules and because EntityWolf already has an
 * owner system.
 */
public class EntitySpiritWolf extends EntityWolf {

	private int dispelTimer = 0;

	private static final int DISPEL_TIME = 10;

	public EntitySpiritWolf(World world){
		super(world);
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
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata){

		// Adds Particles on spawn. Due to client/server differences this cannot be done
		// in the item.
		if(this.world.isRemote){
			this.spawnAppearParticles();
		}

		return livingdata;
	}

	private void spawnAppearParticles(){
		for(int i=0; i<15; i++){
			double x = this.posX - this.width / 2 + this.rand.nextFloat() * width;
			double y = this.posY + this.height * this.rand.nextFloat() + 0.2f;
			double z = this.posZ - this.width / 2 + this.rand.nextFloat() * width;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).spawn(world);
		}
	}

	public float getOpacity(){
		return 1 - (float)dispelTimer/DISPEL_TIME;
	}
	
	@Override
	public void onUpdate(){

		super.onUpdate();

		if(dispelTimer > 0){
			if(dispelTimer++ > DISPEL_TIME){
				this.setDead();
			}
		}

		// Adds a dust particle effect
		if(this.world.isRemote){
			double x = this.posX - this.width / 2 + this.rand.nextFloat() * width;
			double y = this.posY + this.height * this.rand.nextFloat() + 0.2f;
			double z = this.posZ - this.width / 2 + this.rand.nextFloat() * width;
			ParticleBuilder.create(Type.DUST).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).shaded(true).spawn(world);
		}
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		if(this.isTamed()){

			// Allows the owner (but not other players) to dispel the spirit wolf using a
			// wand.
			if(stack.getItem() instanceof ISpellCastingItem && this.getOwner() == player && player.isSneaking()){
				// Prevents accidental double clicking.
				if(this.ticksExisted > 20){
					
					this.dispelTimer++;
					
					this.playSound(WizardrySounds.ENTITY_SPIRIT_WOLF_VANISH, 0.7F, rand.nextFloat() * 0.4F + 1.0F);
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
	protected int getExperiencePoints(EntityPlayer player){
		return 0;
	}

	@Override
	protected boolean canDropLoot(){
		return false;
	}

	@Override
	protected Item getDropItem(){
		return null;
	}

	@Override
	protected ResourceLocation getLootTable(){
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
		return Wizardry.settings.summonedCreatureNames && getOwner() != null;
	}

}
