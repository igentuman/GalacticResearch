package igentuman.galacticresearch.common.capability;

import igentuman.galacticresearch.GalacticResearch;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpaceCapabilityHandler {

	@CapabilityInject(ISpaceData.class)
	public static Capability<ISpaceData> PLAYER_SPACE_DATA = null;
	public static final ResourceLocation PLAYER_SPACE_DATA_NAME = new ResourceLocation(GalacticResearch.MODID, "capability_player_space_data");

	@SubscribeEvent
	public void attachEntityRadiationCapability(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityPlayer) {
			event.addCapability(PLAYER_SPACE_DATA_NAME, new SpaceDataProvider());
		}
/*		else if (event.getObject() instanceof EntityLivingBase) {
			event.addCapability(PLAYER_SPACE_DATA_NAME, new SpaceDataProvider((EntityLivingBase) event.getObject()));
		}*/
	}
}
