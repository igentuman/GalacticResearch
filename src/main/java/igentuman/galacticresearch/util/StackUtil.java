package igentuman.galacticresearch.util;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

public class StackUtil {
    private static Block getBlock(String location) {
        ResourceLocation resLoc = new ResourceLocation(location);
        if (!Loader.isModLoaded(resLoc.getNamespace())) {
            return null;
        }
        return ForgeRegistries.BLOCKS.getValue(resLoc);
    }

    private static int getMeta(String location) {
        if (StringUtils.countMatches(location, ':') < 2) {
            return 0;
        }
        return Integer.parseInt(location.substring(location.lastIndexOf(':') + 1));
    }

    private static String starting(String s, int length) {
        int fixedLength = Math.min(length, s.length());
        return s.substring(0, fixedLength);
    }

    private static String removeMeta(String location) {
        if (StringUtils.countMatches(location, ':') < 2) {
            return location;
        }
        return starting(location, location.lastIndexOf(':'));
    }

    public static ItemStack stackFromRegistry(String location) {
        Block block = getBlock(removeMeta(location));
        return block == null ? null : new ItemStack(block, 1, getMeta(location));
    }
}
