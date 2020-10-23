package electroblob.wizardry.spell;

import electroblob.wizardry.entity.projectile.EntityEmber;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Disintegration extends SpellRay {

	public static final String EMBER_COUNT = "ember_count";
	public static final String EMBER_LIFETIME = "ember_lifetime";

	public static final String NBT_KEY = "disintegrating";

	public Disintegration(){
		super("disintegration", SpellActions.POINT, false);
		addProperties(DAMAGE, BURN_DURATION, EMBER_LIFETIME, EMBER_COUNT);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
			if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
					new TextComponentTranslation("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
		}else{

			target.setFire((int)(getProperty(BURN_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
			EntityUtils.attackEntityWithoutKnockback(target, caster == null ? DamageSource.MAGIC :
					MagicDamage.causeDirectMagicDamage(caster, DamageType.FIRE),
					getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

			if(target instanceof EntityLivingBase && ((EntityLivingBase)target).getHealth() <= 0){
				spawnEmbers(world, caster, target, getProperty(EMBER_COUNT).intValue());
			}
		}
		
		return true;
	}

	public static void spawnEmbers(World world, EntityLivingBase caster, Entity target, int count){

		target.extinguish();

		if(world.isRemote){ // FIXME: Various syncing issues here!
			// Set NBT client-side, it's only for rendering
			// Normally dying entities are ignored but we're fiddling with them here so double-check
			if(!target.getEntityData().hasKey(NBT_KEY)){
				target.getEntityData().setInteger(NBT_KEY, target.ticksExisted);
			}
		}else{
			for(int i = 0; i < count; i++){
				EntityEmber ember = new EntityEmber(world, caster);
				double x = (world.rand.nextDouble() - 0.5) * target.width;
				double y = world.rand.nextDouble() * target.height;
				double z = (world.rand.nextDouble() - 0.5) * target.width;
				ember.setPosition(target.posX + x, target.posY + y, target.posZ + z);
				ember.ticksExisted = world.rand.nextInt(20);
				float speed = 0.2f;
				ember.motionX = x * speed;
				ember.motionY = y * 0.5f * speed;
				ember.motionZ = z * speed;
				world.spawnEntity(ember);
			}
		}
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(world.isRemote){
			
			for(int i = 0; i < 8; i++){
				world.spawnParticle(EnumParticleTypes.LAVA, hit.x, hit.y, hit.z, 0, 0, 0);
			}
			
			if(world.getBlockState(pos).getMaterial().isSolid()){
				Vec3d vec = hit.add(new Vec3d(side.getDirectionVec()).scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET));
				ParticleBuilder.create(Type.SCORCH).pos(vec).face(side).clr(1, 0.2f, 0).spawn(world);
			}
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticleRay(World world, Vec3d origin, Vec3d direction, EntityLivingBase caster, double distance){
		Vec3d endpoint = origin.add(direction.scale(distance));
		ParticleBuilder.create(Type.BEAM).clr(1, 0.4f, 0).fade(1, 0.1f, 0).time(4).pos(origin).target(endpoint).spawn(world);
	}

}
