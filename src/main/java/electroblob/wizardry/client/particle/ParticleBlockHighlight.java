package electroblob.wizardry.client.particle;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

//@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class ParticleBlockHighlight extends ParticleWizardry {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "particle/block_highlight");

	public ParticleBlockHighlight(World world, double x, double y, double z){
		
		super(world, x, y, z, TEXTURE);
		
		this.particleGravity = 0;
		this.setMaxAge(160);
		this.particleScale = 5;
		this.shaded = false;
	}

	@Override
	public boolean shouldDisableDepth(){
		return true;
	}
	
	@Override
	public void onUpdate(){

		super.onUpdate();
		
		// Fading
		if(this.particleAge > this.particleMaxAge/2){
			this.setAlphaF(1 - ((float)this.particleAge - this.particleMaxAge/2f) / (this.particleMaxAge/2f));
		}
		
		EnumFacing facing = EnumFacing.fromAngle(yaw);
		if(pitch == 90) facing = EnumFacing.UP;
		if(pitch == -90) facing = EnumFacing.DOWN;
		
		// Disappears if there is no block behind it (this is the same check used to spawn it)
		if(!world.getBlockState(new BlockPos(posX, posY, posZ).offset(facing.getOpposite())).getMaterial().isSolid()){
			this.setExpired();
		}
	}
	
	@SubscribeEvent
	public static void onTextureStitchEvent(TextureStitchEvent.Pre event){
		event.getMap().registerSprite(TEXTURE);
	}
}
