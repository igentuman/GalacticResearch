package igentuman.galacticresearch.client.jei;

import igentuman.galacticresearch.client.jei.satellite.SatelliteRocketRecipeCategory;
import igentuman.galacticresearch.client.jei.satellite.SatelliteRocketRecipeMaker;
import igentuman.galacticresearch.client.jei.satellite.SatelliteRocketRecipeWrapper;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.GCBlocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
@JEIPlugin
public class GalacticResearchJEI extends BlankModPlugin
{

    @Override
    public void register(@Nonnull IModRegistry registry)
    {
        registry.handleRecipes(INasaWorkbenchRecipe.class, SatelliteRocketRecipeWrapper::new, SatelliteRocketRecipeCategory.ID);
        registry.addRecipes(SatelliteRocketRecipeMaker.getRecipesList(), SatelliteRocketRecipeCategory.ID);
        registry.addRecipeCatalyst(new ItemStack(GCBlocks.nasaWorkbench), SatelliteRocketRecipeCategory.ID);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
        registry.addRecipeCategories(new SatelliteRocketRecipeCategory(guiHelper));
    }
}
