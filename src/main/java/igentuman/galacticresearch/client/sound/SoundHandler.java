package igentuman.galacticresearch.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static igentuman.galacticresearch.GalacticResearch.MODID;


@Mod.EventBusSubscriber(modid = MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class SoundHandler {

    private static Map<Long, ISound> soundMap = new HashMap<>();
    private static boolean IN_MUFFLED_CHECK = false;

    public static void playSound(ISound sound) {
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
    }

    public static ISound startTileSound(ResourceLocation soundLoc, float volume, BlockPos pos) {
        ISound s = soundMap.get(pos.toLong());
        if (s == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(s)) {
            s = new PositionedSoundRecord(soundLoc, SoundCategory.BLOCKS, (float) (volume * 0.5), 1.0f,
                  true, 0, ISound.AttenuationType.LINEAR, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f) {
                @Override
                public float getVolume() {
                    if (this.sound == null) {
                        this.createAccessor(Minecraft.getMinecraft().getSoundHandler());
                    }
                    return super.getVolume();
                }
            };
            playSound(s);
            s = soundMap.get(pos.toLong());
        }

        return s;
    }

    public static void stopTileSound(BlockPos pos) {
        long posKey = pos.toLong();
        ISound s = soundMap.get(posKey);
        if (s != null) {
            Minecraft.getMinecraft().getSoundHandler().stopSound(s);
            soundMap.remove(posKey);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTilePlaySound(PlaySoundEvent event) {
        ISound resultSound = event.getResultSound();
        if (resultSound == null || IN_MUFFLED_CHECK) {
            return;
        }

        ResourceLocation soundLoc = event.getSound().getSoundLocation();
        if (!soundLoc.getNamespace().equals(MODID)) {
            return;
        }
        resultSound = new TileSound(event.getSound(), resultSound.getVolume());
        event.setResultSound(resultSound);
        BlockPos pos = new BlockPos(resultSound.getXPosF() - 0.5f, resultSound.getYPosF() - 0.5f, resultSound.getZPosF() - 0.5);
        soundMap.put(pos.toLong(), resultSound);
    }

    private static class TileSound implements ITickableSound {

        private ISound original;
        private float volume;
        private boolean donePlaying = false;

        private int checkInterval = 60 + ThreadLocalRandom.current().nextInt(20);

        private Minecraft mc = Minecraft.getMinecraft();

        TileSound(ISound original, float volume) {
            this.original = original;
            this.volume = volume;
        }

        @Override
        public void update() {
            if (mc.world.getTotalWorldTime() % checkInterval == 0) {
                IN_MUFFLED_CHECK = true;
                ISound s = ForgeHooksClient.playSound(mc.getSoundHandler().sndManager, original);
                IN_MUFFLED_CHECK = false;

                if (s == this) {
                    volume = original.getVolume();
                } else if (s == null) {
                    donePlaying = true;
                } else {
                    volume = s.getVolume();
                }
            }
        }


        @Override
        public boolean isDonePlaying() {
            return donePlaying;
        }

        @Override
        public float getVolume() {
            return volume;
        }

        @Nonnull
        @Override
        public ResourceLocation getSoundLocation() {
            return original.getSoundLocation();
        }

        @Nullable
        @Override
        public SoundEventAccessor createAccessor(@Nonnull net.minecraft.client.audio.SoundHandler handler) {
            return original.createAccessor(handler);
        }

        @Nonnull
        @Override
        public Sound getSound() {
            return original.getSound();
        }

        @Nonnull
        @Override
        public SoundCategory getCategory() {
            return original.getCategory();
        }

        @Override
        public boolean canRepeat() {
            return original.canRepeat();
        }

        @Override
        public int getRepeatDelay() {
            return original.getRepeatDelay();
        }

        @Override
        public float getPitch() {
            return original.getPitch();
        }

        @Override
        public float getXPosF() {
            return original.getXPosF();
        }

        @Override
        public float getYPosF() {
            return original.getYPosF();
        }

        @Override
        public float getZPosF() {
            return original.getZPosF();
        }

        @Nonnull
        @Override
        public AttenuationType getAttenuationType() {
            return original.getAttenuationType();
        }
    }
}