package electroblob.wizardry.advancement;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import electroblob.wizardry.Wizardry;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * This class implements a generic custom advancement trigger that can be fired from any point in
 * the code. This replaces the achievement system in instances where the JSON advancement descriptions
 * cannot properly capture the advancement-worthy events. Where possible, advancement conditions
 * should be triggered by JSON descriptions and vanilla advancement triggers.
 *
 * @author 12foo
 * @since 4.1.0
 */
public class CustomAdvancementTrigger implements ICriterionTrigger<CustomAdvancementTrigger.Instance> {

    private final ResourceLocation id;
    private final SetMultimap<PlayerAdvancements, Listener<? extends ICriterionInstance>> listeners = HashMultimap.create();

    /**
     * This is a dummy criterion instance that does nothing on its own (but it is bound to this
     * trigger, and via listeners to the player). We later fire this manually when we want the
     * advancement to happen.
     */
    public static class Instance extends AbstractCriterionInstance {
        public Instance(ResourceLocation triggerId) { 
            super(triggerId);
        }
    }

    public CustomAdvancementTrigger(String name) {
        super();
        id = new ResourceLocation(Wizardry.MODID, name);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public void addListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        listeners.put(playerAdvancementsIn, listener);
    }

    @Override
    public void removeListener(PlayerAdvancements playerAdvancementsIn, Listener<Instance> listener) {
        listeners.remove(playerAdvancementsIn, listener);
    }

    @Override
    public void removeAllListeners(PlayerAdvancements playerAdvancementsIn) {
        listeners.removeAll(playerAdvancementsIn);
    }

    @Override
    public Instance deserializeInstance(JsonObject json, JsonDeserializationContext context) {
        // Every time a trigger with this name is deserialized from the JSON, we just return a new
        // dummy criterion instance.
        return new CustomAdvancementTrigger.Instance(id);
    }

    public void triggerFor(EntityPlayer player) {
        // Fire our dummy criterion manually on all advancements of the player, thereby granting
        // the ones that match it.
        if (player instanceof EntityPlayerMP) {
            final PlayerAdvancements advances = ((EntityPlayerMP) player).getAdvancements();
            listeners.get(advances).forEach((listener) -> listener.grantCriterion(advances));
        }
    }
}
