package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Flamecatcher extends SpellConjuration {

	public static final String SHOT_COUNT = "shot_count";
	public static final String SHOTS_REMAINING_NBT_KEY = "shotsRemaining";

	public Flamecatcher(){
		super("flamecatcher", WizardryItems.flamecatcher);
		addProperties(RANGE, SHOT_COUNT, DAMAGE, BURN_DURATION);
	}

	@Override
	protected void addItemExtras(EntityPlayer caster, ItemStack stack, SpellModifiers modifiers){
		if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger(SHOTS_REMAINING_NBT_KEY, (int)(getProperty(SHOT_COUNT).intValue() * modifiers.get(SpellModifiers.POTENCY)));
	}

	@Override
	protected void spawnParticles(World world, EntityLivingBase caster, SpellModifiers modifiers){

		ParticleBuilder.create(Type.BUFF).entity(caster).clr(0xff6d00).spawn(world);

		for(int i=0; i<10; i++){
			double x = caster.posX + world.rand.nextDouble() * 2 - 1;
			double y = caster.posY + caster.getEyeHeight() - 0.5 + world.rand.nextDouble();
			double z = caster.posZ + world.rand.nextDouble() * 2 - 1;
			world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
		}
	}

}
