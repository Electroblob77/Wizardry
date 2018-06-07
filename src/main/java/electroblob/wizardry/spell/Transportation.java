package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.block.BlockTransportationStone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class Transportation extends Spell {

	public Transportation() {
		super(EnumTier.ADVANCED, 100, EnumElement.SORCERY, "transportation", EnumSpellType.UTILITY, 100, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		ExtendedPlayer properties = ExtendedPlayer.get(caster);
		
		// Only works when the caster is in the same dimension.
		if(properties != null && properties.tpTimer == 0){
			if(caster.dimension == properties.transportDimension){
				// Has to be y since x and z could reasonably be -1.
				if(properties.transportY > -1){
					if(BlockTransportationStone.testForCircle(world, properties.transportX, properties.transportY, properties.transportZ)){
						world.playSoundAtEntity(caster, "portal.trigger", 1.0f, 1.0f);
						caster.addPotionEffect(new PotionEffect(Potion.confusion.id, 150, 0));
						properties.tpTimer = 75;
						return true;
					}else{
						if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.transportation.missing"));
					}
				}else{
					if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.transportation.undefined"));
				}
			}else{
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.transportation.wrongdimension"));
			}
		}
		return false;
	}


}
