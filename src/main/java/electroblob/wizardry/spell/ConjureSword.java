package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.client.particle.EntitySparkleFX;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ConjureSword extends Spell {

	public ConjureSword() {
		super(EnumTier.APPRENTICE, 25, EnumElement.SORCERY, "conjure_sword", EnumSpellType.UTILITY, 50, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		ItemStack sword = new ItemStack(Wizardry.spectralSword);
		
		sword.stackTagCompound = new NBTTagCompound();
		sword.stackTagCompound.setFloat("durationMultiplier", durationMultiplier);
		
		if(!caster.inventory.hasItem(Wizardry.spectralSword) && caster.inventory.addItemStackToInventory(sword)){
			for(int i=0; i<10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
				if(world.isRemote){
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.7f, 0.9f, 1.0f);
				}
			}
			ExtendedPlayer.get(caster).conjuredSwordDuration = 0;
			world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);
			return true;
		}
		return false;
	}


}
