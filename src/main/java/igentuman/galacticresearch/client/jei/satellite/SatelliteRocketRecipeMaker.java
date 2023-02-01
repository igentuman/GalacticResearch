package igentuman.galacticresearch.client.jei.satellite;

import igentuman.galacticresearch.common.CommonProxy;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;
import micdoodle8.mods.galacticraft.core.client.jei.tier1rocket.Tier1RocketRecipeMaker;

import java.util.ArrayList;
import java.util.List;

public class SatelliteRocketRecipeMaker
{

    public static List<INasaWorkbenchRecipe> getRecipesList()
    {
        List<INasaWorkbenchRecipe> recipes = new ArrayList<>();
        for (INasaWorkbenchRecipe recipe : CommonProxy.getSatelliteRocketRecipes())
        {
            recipes.add(recipe);
        }

        return recipes;
    }
}
