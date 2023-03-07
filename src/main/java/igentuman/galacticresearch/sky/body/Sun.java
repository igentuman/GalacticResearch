package igentuman.galacticresearch.sky.body;

import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import net.minecraft.client.Minecraft;

public class Sun extends Researchable {

    private int x;
    private int y;
    private int size;

    public float getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Sun()
    {
        super(new Planet("sun").setRelativeSize(16).setRelativeOrbitTime(20));
    }

    public boolean isVisible()
    {
        return Minecraft.getMinecraft().world.isDaytime();
    }

    public int getX()
    {
        return 300;

    }

    //todo add solarbody coordinates calculation
    public int getY()
    {
        return 20;
    }
}
