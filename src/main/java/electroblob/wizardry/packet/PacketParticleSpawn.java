package electroblob.wizardry.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import electroblob.wizardry.client.particle.EntityBlizzardFX;
import electroblob.wizardry.client.particle.EntityDarkMagicFX;
import electroblob.wizardry.client.particle.EntityDustFX;
import electroblob.wizardry.client.particle.EntityLeafFX;
import electroblob.wizardry.client.particle.EntityMagicBubbleFX;
import electroblob.wizardry.client.particle.EntityMagicFireFX;
import electroblob.wizardry.client.particle.EntitySnowFX;
import electroblob.wizardry.client.particle.EntitySparkFX;
import electroblob.wizardry.client.particle.EntitySparkleFX;
import electroblob.wizardry.client.particle.EntityTornadoFX;
import electroblob.wizardry.packet.PacketParticleSpawn.Message;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

/** This packet is for spawning particles from any method which is client-inconsistent, such as Item#onItemRightClick.
 * It can also be used as a lazy fix when the particles depend on conditions which are not synchronised (though it is
 * strictly better to synchronise the conditions themselves, in practice it isn't usually worth it unless there are
 * loads of particles)
 * <p>
 * DEPRECATED in favour of an 'event packet' based system. */
@Deprecated
public class PacketParticleSpawn implements IMessageHandler<Message, IMessage> {
	
	@Override
	public IMessage onMessage(Message message, MessageContext ctx)
	{
		// Just to make sure that the side is correct
		if(ctx.side.isClient()){
			
			World world = Minecraft.getMinecraft().theWorld;
			
			if(message.name == EntityBlizzardFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntityBlizzardFX(world, message.r, message.g, message.b, message.maxAge, message.x, message.z, message.velY, message.y));
			}
			else if(message.name == EntityDarkMagicFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntityDarkMagicFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ, message.r, message.g, message.b));
			}
			else if(message.name == EntityDustFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntityDustFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ, message.r, message.g, message.b, false));
			}
			else if(message.name == EntityLeafFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntityLeafFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ, message.maxAge));
			}
			else if(message.name == EntityMagicBubbleFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntityMagicBubbleFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ));
			}
			else if(message.name == EntityMagicFireFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntityMagicFireFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ, message.maxAge, 1));
			}
			else if(message.name == EntitySnowFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySnowFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ, message.maxAge));
			}
			else if(message.name == EntitySparkFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ));
			}
			else if(message.name == EntitySparkleFX.NAME){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ, message.r, message.g, message.b, message.maxAge, false));
			}
			else if(message.name == EntitySparkleFX.NAME_GRAVITY){
				Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, message.x, message.y, message.z, message.velX, message.velY, message.velZ, message.r, message.g, message.b, message.maxAge, true));
			}
			else{
				world.spawnParticle(message.name, message.x, message.y, message.z, message.velX, message.velY, message.velZ);
			}
		}

		return null;
	}

	public static class Message implements IMessage
	{
		// Some of these fields have multiple uses depending on the particle type. This is to save on packet
		// size as much as possible.
		
		/** Particle name. Either a vanilla name (see RenderGlobal) or one of the custom particles' NAME field. */
		private String name;
		
		/** Usually the x position of the particle, but a few particles use it differently. */
		private double x;
		/** Usually the y position of the particle, but a few particles use it differently. */
		private double y;
		/** Usually the z position of the particle, but a few particles use it differently. */
		private double z;

		/** Usually the x velocity of the particle, but a few particles use it differently. */
		private double velX;
		/** Usually the y velocity of the particle, but a few particles use it differently - 
		 * for example, tornado and blizzard fx use this as the radius. */
		private double velY;
		/** Usually the z velocity of the particle, but a few particles use it differently. */
		private double velZ;
		
		private int maxAge;

		private float r;
		private float g;
		private float b;

		// This constructor is required otherwise you'll get errors (used somewhere in fml through reflection)
		public Message() {}

		public Message(String name, double x, double y, double z, double velX, double velY, double velZ, int maxAge, float r, float g, float b)
		{
			this.name = name;
			this.x = x;
			this.y = y;
			this.z = z;
			this.velX = velX;
			this.velY = velY;
			this.velZ = velZ;
			this.maxAge = maxAge;
			this.r = r;
			this.g = g;
			this.b = b;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			// The order is important
			this.name = ByteBufUtils.readUTF8String(buf);
			this.x = buf.readDouble();
			this.y = buf.readDouble();
			this.z = buf.readDouble();
			this.velX = buf.readDouble();
			this.velY = buf.readDouble();
			this.velZ = buf.readDouble();
			this.maxAge = buf.readInt();
			this.r = buf.readFloat();
			this.g = buf.readFloat();
			this.b = buf.readFloat();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			ByteBufUtils.writeUTF8String(buf, name);
			buf.writeDouble(x);
			buf.writeDouble(y);
			buf.writeDouble(z);
			buf.writeDouble(velX);
			buf.writeDouble(velY);
			buf.writeDouble(velZ);
			buf.writeInt(maxAge);
			buf.writeFloat(r);
			buf.writeFloat(g);
			buf.writeFloat(b);
		}
	}
}
