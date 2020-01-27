package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class SlowTime extends SpellBuff {

	/** A {@code ResourceLocation} representing the shader file used when possessing an entity. */
	public static final ResourceLocation SHADER = new ResourceLocation(Wizardry.MODID, "shaders/post/slow_time.json");

	public SlowTime(){
		super("slow_time", 0.2f, 0.8f, 0.8f, () -> WizardryPotions.slow_time);
		addProperties(EFFECT_RADIUS);
		soundValues(0.6f, 1.5f, 0);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.world.isRemote && caster == net.minecraft.client.Minecraft.getMinecraft().player){
			if(Wizardry.settings.useShaders) net.minecraft.client.Minecraft.getMinecraft().entityRenderer.loadShader(SHADER);
		}

		return super.cast(world, caster, hand, ticksInUse, modifiers);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser){
		return false;
	}

	@Override
	public boolean canBeCastBy(EntityLiving npc, boolean override){
		return false;
	}
}
