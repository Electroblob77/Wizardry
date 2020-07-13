package electroblob.wizardry.advancement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import electroblob.wizardry.worldgen.WorldGenWizardryStructure;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.advancements.critereon.AbstractCriterionInstance;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Copied from PositionTrigger and modified to work with wizardry's structures. The majority of any
 * ICriterionTrigger class is just boilerplate, and this is no exception. */
public class StructureTrigger implements ICriterionTrigger<StructureTrigger.Instance> {

	private final ResourceLocation id;
	private final Map<PlayerAdvancements, StructureTrigger.Listeners> listeners = Maps.newHashMap();

	public StructureTrigger(ResourceLocation id){
		this.id = id;
	}

	public ResourceLocation getId(){
		return this.id;
	}

	public void addListener(PlayerAdvancements advancements, Listener<StructureTrigger.Instance> listener){

		StructureTrigger.Listeners listeners = this.listeners.get(advancements);

		if(listeners == null){
			listeners = new StructureTrigger.Listeners(advancements);
			this.listeners.put(advancements, listeners);
		}

		listeners.add(listener);
	}

	public void removeListener(PlayerAdvancements advancements, Listener<StructureTrigger.Instance> listener){

		StructureTrigger.Listeners listeners = this.listeners.get(advancements);

		if(listeners != null){
			listeners.remove(listener);

			if(listeners.isEmpty()){
				this.listeners.remove(advancements);
			}
		}
	}

	public void removeAllListeners(PlayerAdvancements advancements){
		this.listeners.remove(advancements);
	}

	public StructureTrigger.Instance deserializeInstance(JsonObject json, JsonDeserializationContext context){
		return new StructureTrigger.Instance(this.id, JsonUtils.getString(json, "structure_type"));
	}

	public void trigger(EntityPlayerMP player){

		StructureTrigger.Listeners listeners = this.listeners.get(player.getAdvancements());

		if(listeners != null){
			listeners.trigger(player.getServerWorld(), player.posX, player.posY, player.posZ);
		}
	}

	public static class Instance extends AbstractCriterionInstance {

		private final WorldGenWizardryStructure structureType;

		public Instance(ResourceLocation criterionIn, String name){
			super(criterionIn);
			this.structureType = WorldGenWizardryStructure.byName(name);
		}

		public boolean test(WorldServer world, double x, double y, double z){
			return structureType.isInsideStructure(world, x, y, z);
		}
	}

	static class Listeners {

		private final PlayerAdvancements playerAdvancements;
		private final Set<Listener<StructureTrigger.Instance>> listeners = Sets.newHashSet();

		public Listeners(PlayerAdvancements advancements){
			this.playerAdvancements = advancements;
		}

		public boolean isEmpty(){
			return this.listeners.isEmpty();
		}

		public void add(Listener<StructureTrigger.Instance> listener){
			this.listeners.add(listener);
		}

		public void remove(Listener<StructureTrigger.Instance> listener){
			this.listeners.remove(listener);
		}

		public void trigger(WorldServer world, double x, double y, double z){

			List<Listener<StructureTrigger.Instance>> list = null;

			for(Listener<StructureTrigger.Instance> listener : this.listeners){

				if(listener.getCriterionInstance().test(world, x, y, z)){

					if(list == null){
						list = Lists.newArrayList();
					}

					list.add(listener);
				}
			}

			if(list != null){
				for(Listener<StructureTrigger.Instance> listener : list){
					listener.grantCriterion(this.playerAdvancements);
				}
			}
		}
	}
}