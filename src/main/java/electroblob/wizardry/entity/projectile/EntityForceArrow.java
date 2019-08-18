package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Arrays;

public class EntityForceArrow extends EntityMagicArrow {

	/** The mana used to cast this force arrow, used for artefacts. */
	private int mana = 0;

	/** Creates a new force arrow in the given world. */
	public EntityForceArrow(World world){
		super(world);
	}

	public void setMana(int mana){
		this.mana = mana;
	}

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		this.playSound(WizardrySounds.ENTITY_FORCE_ARROW_HIT, 1.0F, 1.0F);
		if(this.world.isRemote)
			ParticleBuilder.create(Type.FLASH).pos(posX, posY, posZ).scale(1.3f).clr(0.75f, 1, 0.85f).spawn(world);
	}

	@Override
	public void tickInGround(){
		returnManaToCaster();
		this.setDead();
	}

	@Override
	public void onUpdate(){

		if(getLifetime() >=0 && this.ticksExisted > getLifetime()){ // The last tick before it disappears
			returnManaToCaster();
		}

		super.onUpdate();
	}

	private void returnManaToCaster(){

		if(mana > 0 && getCaster() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)getCaster();

			if(!player.capabilities.isCreativeMode && ItemArtefact.isArtefactActive(player, WizardryItems.ring_mana_return)){

				for(ItemStack stack : WizardryUtilities.getPrioritisedHotbarAndOffhand(player)){
					if(stack.getItem() instanceof ISpellCastingItem && stack.getItem() instanceof IManaStoringItem
							&& Arrays.asList(((ISpellCastingItem)stack.getItem()).getSpells(stack)).contains(Spells.force_arrow)){
						((IManaStoringItem)stack.getItem()).rechargeMana(stack, mana);
					}
				}
			}
		}
	}

	@Override
	public void onBlockHit(RayTraceResult hit){
		this.playSound(WizardrySounds.ENTITY_FORCE_ARROW_HIT, 1.0F, 1.0F);
		if(this.world.isRemote){
			// Gets a position slightly away from the block hit so the particle doesn't get cut in half by the block face
			Vec3d vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(0.15));
			ParticleBuilder.create(Type.FLASH).pos(vec).scale(1.3f).clr(0.75f, 1, 0.85f).spawn(world);
			//vec = hit.hitVec.add(new Vec3d(hit.sideHit.getDirectionVec()).scale(WizardryUtilities.ANTI_Z_FIGHTING_OFFSET));
			//ParticleBuilder.create(Type.SCORCH).pos(vec).face(hit.sideHit).clr(0, 1, 0.5f).spawn(world);
		}
	}

	@Override
	public int getLifetime(){
		return 20;
	}

	@Override
	public double getDamage(){
		return Spells.force_arrow.getProperty(Spell.DAMAGE).floatValue();
	}

	@Override
	public DamageType getDamageType(){
		return DamageType.FORCE;
	}

	@Override
	public boolean doGravity(){
		return false;
	}

	@Override
	public boolean doDeceleration(){
		return false;
	}

	@Override
	protected void entityInit(){
		// auto generated
	}

}