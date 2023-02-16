package igentuman.galacticresearch.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class GRSounds {
	
	public static final ObjectSet<String> TICKABLE_SOUNDS = new ObjectOpenHashSet<>();
	
	public static SoundEvent mcs_mission_complete;
	public static SoundEvent mcs_mission_fail;
	
	public static void init() {
		mcs_mission_complete = register("block.mcs.mission_fail");
		mcs_mission_fail = register("block.mcs.mission_finish");
	}
	
	private static SoundEvent register(String name) {
		return register(name, false);
	}
	
	private static SoundEvent register(String name, boolean tickable) {
		ResourceLocation location = new ResourceLocation(MODID, name);
		if (tickable) {
			TICKABLE_SOUNDS.add(location.toString());
		}
		SoundEvent event = new SoundEvent(location);
		
		ForgeRegistries.SOUND_EVENTS.register(event.setRegistryName(location));
		return event;
	}
}
