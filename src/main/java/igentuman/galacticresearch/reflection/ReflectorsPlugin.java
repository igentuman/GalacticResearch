package igentuman.galacticresearch.reflection;

import com.github.mjaroslav.reflectors.v4.Reflectors;
import igentuman.galacticresearch.ModConfig;
import igentuman.galacticresearch.reflection.entity.EntityBossSkeletonReflector;
import igentuman.galacticresearch.reflection.screen.GameScreenCelestialReflector;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.SortingIndex(1001)
@IFMLLoadingPlugin.Name("ReflectorsPlugin")
public class ReflectorsPlugin extends Reflectors.FMLLoadingPluginAdapter
        implements IFMLLoadingPlugin, IClassTransformer {
    public ReflectorsPlugin() {
        Reflectors.enabledLogs = true;
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{getClass().getName()};
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if (transformedName.equals("micdoodle8.mods.galacticraft.core.tile.TileEntityLandingPad")) {
            return Reflectors.reflectClass(basicClass, transformedName, TileEntityLandingPadReflector.class.getName());
        }

        if (transformedName.equals("com.mjr.extraplanets.client.handlers.MainHandlerClient")) {
            return Reflectors.reflectClass(basicClass, transformedName, MainHandlerClientReflector.class.getName());
        }


        if (transformedName.equals("micdoodle8.mods.galacticraft.core.entities.EntitySkeletonBoss")) {
            return Reflectors.reflectClass(basicClass, transformedName, EntityBossSkeletonReflector.class.getName());
        }

        if (transformedName.equals("micdoodle8.mods.galacticraft.core.tile.TileEntityDungeonSpawner")) {
            return Reflectors.reflectClass(basicClass, transformedName, TileEntityDungeonSpawnerReflector.class.getName());
        }

        if (transformedName.equals("asmodeuscore.core.event.AsmodeusClientEvent")) {
            return Reflectors.reflectClass(basicClass, transformedName, AsmodeusClientEventReflector.class.getName());
        }

        if (transformedName.equals("micdoodle8.mods.galacticraft.core.client.screen.GameScreenCelestial")) {
            return Reflectors.reflectClass(basicClass, transformedName, GameScreenCelestialReflector.class.getName());
        }

        if (transformedName.equals("micdoodle8.mods.galacticraft.core.items.ItemBasic")) {
            return Reflectors.reflectClass(basicClass, transformedName, ItemBasicReflector.class.getName());
        }


        return basicClass;
    }
}