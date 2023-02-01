package igentuman.galacticresearch;


import igentuman.galacticresearch.network.TileProcessUpdatePacket;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface ISidedProxy {


    void preInit(FMLPreInitializationEvent event);

    void init(FMLInitializationEvent event);

    void postInit(FMLPostInitializationEvent event);

    void handleProcessUpdatePacket(TileProcessUpdatePacket message, MessageContext context);

}