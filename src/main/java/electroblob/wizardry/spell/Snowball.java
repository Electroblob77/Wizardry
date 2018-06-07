package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.item.EnumAction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Snowball extends Spell {

	public Snowball() {
		super(EnumTier.BASIC, 1, EnumElement.ICE, "snowball", EnumSpellType.ATTACK, 1, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		if(!world.isRemote){
			
			EntitySnowball snowball = new EntitySnowball(world, caster);
			
			//snowball.motionX = look.xCoord * 1.5;
			//snowball.motionY = look.yCoord * 1.5;
			//snowball.motionZ = look.zCoord * 1.5;
			
			world.spawnEntityInWorld(snowball);
		}
		
		world.playSoundAtEntity(caster, "random.bow", 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
		caster.swingItem();
		return true;
	}


}
