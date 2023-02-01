package igentuman.galacticresearch.sky.body;

import igentuman.galacticresearch.sky.SkyItem;
import net.minecraft.client.Minecraft;

public class Sun extends Researchable {

    private int x;
    private int y;
    private SkyItem body;
    private int size;

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public SkyItem getBody() {
        return body;
    }

    public void setBody(SkyItem body) {
        this.body = body;
    }


    public Sun(SkyItem body)
    {
        super(body);
        this.body = body;
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
