package igentuman.galacticresearch.client.jei.mining;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.planets.GalacticraftPlanets;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import static igentuman.galacticresearch.GalacticResearch.MODID;

public class MiningRocketRecipeCategory implements IRecipeCategory
{

    private static final ResourceLocation rocketGuiTexture = new ResourceLocation(MODID, "textures/gui/schematic/mining_rocket.png");

    @Nonnull private final IDrawable background;
    @Nonnull private final String localizedName;
    public static String ID = MODID+"mining";

    public MiningRocketRecipeCategory(IGuiHelper guiHelper)
    {
        this.background = guiHelper.createDrawable(rocketGuiTexture, 3, 4, 168, 125);
        this.localizedName = GCCoreUtil.translate("tile.rocket_workbench.name");

    }

    @Nonnull
    @Override
    public String getUid()
    {
        return ID;
    }

    @Nonnull
    @Override
    public String getTitle()
    {
        return this.localizedName;
    }

    @Nonnull
    @Override
    public IDrawable getBackground()
    {
        return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, IIngredients ingredients)
    {
        IGuiItemStackGroup itemstacks = recipeLayout.getItemStacks();

        itemstacks.init(0, true, 44, 13);
        itemstacks.init(1, true, 44, 31);
        itemstacks.init(2, true, 35, 49);
        itemstacks.init(3, true, 35, 67);
        itemstacks.init(4, true, 35, 85);
        itemstacks.init(5, true, 53, 49);
        itemstacks.init(6, true, 53, 67);
        itemstacks.init(7, true, 53, 85);
        itemstacks.init(8, true, 17, 85);
        itemstacks.init(9, true, 71, 85);
        itemstacks.init(10, true, 44, 103);
        itemstacks.init(11, true, 17, 103);
        itemstacks.init(12, true, 71, 103);
        itemstacks.init(13, false, 138, 91);
        itemstacks.set(ingredients);

        itemstacks.set(ingredients);
    }

    @Override
    public String getModName()
    {
        return GalacticraftPlanets.NAME;
    }
}
