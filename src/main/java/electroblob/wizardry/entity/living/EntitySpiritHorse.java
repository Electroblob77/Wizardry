package electroblob.wizardry.entity.living;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

/**
 * Does not implement ISummonedCreature because it has different despawning rules and because EntityHorse already has an
 * owner system.
 */
@SuppressWarnings("deprecation") // It's what Entity does, so...
public class EntitySpiritHorse extends EntityHorse {

	private int idleTimer = 0;

	private int dispelTimer = 0;

	private static final int DISPEL_TIME = 10;

	public EntitySpiritHorse(World par1World){
		super(par1World);
	}

	@Override
	public String getName(){
		if(this.hasCustomName()){
			return this.getCustomNameTag();
		}else{
			return I18n.translateToLocal("entity.wizardry.spirit_horse.name");
		}
	}

	@Override
	public int getTotalArmorValue(){
		return 0;
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
	protected void dropFewItems(boolean par1, int par2){
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0D);
	}

	@Override
	public void openGUI(EntityPlayer p_110199_1_){
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand){

		ItemStack itemstack = player.getHeldItem(hand);

		// Allows the owner (but not other players) to dispel the spirit horse using a wand (shift-clicking, because
		// clicking mounts the horse in this case).
		if(itemstack.getItem() instanceof ISpellCastingItem && this.getOwner() == player && player.isSneaking()){
			// Prevents accidental double clicking.
			if(this.ticksExisted > 20){

				this.dispelTimer++;
				
				this.playSound(WizardrySounds.ENTITY_SPIRIT_HORSE_VANISH, 0.7F, rand.nextFloat() * 0.4F + 1.0F);
				// This is necessary to prevent the wand's spell being cast when performing this action.
				return true;
			}
			return false;
		}

		return super.processInteract(player, hand);
	}
	
	private void spawnAppearParticles(){
		for(int i=0; i<15; i++){
			double x = this.posX - this.width / 2 + this.rand.nextFloat() * width;
			double y = this.posY + this.height * this.rand.nextFloat() + 0.2f;
			double z = this.posZ - this.width / 2 + this.rand.nextFloat() * width;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).clr(0.8f, 0.8f, 1.0f).spawn(world);
		}
	}

	// I wrote this one!
	private EntityLivingBase getOwner(){

		// I think the DataManager stores any objects, so it now stores the UUID instead of its string representation.
		Entity owner = WizardryUtilities.getEntityByUUID(world, this.getOwnerUniqueId());

		if(owner instanceof EntityLivingBase){
			return (EntityLivingBase)owner;
		}else{
			return null;
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

		// Spirit horse disappears a short time after being dismounted.
		if(!this.isBeingRidden()){
			this.idleTimer++;
		}else if(this.idleTimer > 0){
			this.idleTimer = 0;
		}

		if(this.idleTimer > 200){
			
			this.playSound(WizardrySounds.ENTITY_SPIRIT_HORSE_VANISH, 0.7F, rand.nextFloat() * 0.4F + 1.0F);
			
			this.dispelTimer++;
		}
	}

	@Override
	public boolean canMateWith(EntityAnimal par1EntityAnimal){
		return false;
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data){

		// Adds Particles on spawn. Due to client/server differences this cannot be done in the item.
		if(this.world.isRemote){
			this.spawnAppearParticles();
		}

		return super.onInitialSpawn(difficulty, data);
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
		// If this returns true, the renderer will show the nameplate when looking directly at the entity
		return Wizardry.settings.summonedCreatureNames && getOwner() != null;
	}

}
