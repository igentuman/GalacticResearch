package igentuman.galacticresearch.client.jei.mining;

import igentuman.galacticresearch.common.CommonProxy;
import micdoodle8.mods.galacticraft.api.recipe.INasaWorkbenchRecipe;

import java.util.ArrayList;
import java.util.List;

public class MiningRocketRecipeMaker
{

    public static List<INasaWorkbenchRecipe> getRecipesList()
    {
        List<INasaWorkbenchRecipe> recipes = new ArrayList<>();
        for (INasaWorkbenchRecipe recipe : CommonProxy.getMiningRocketRecipes())
        {
            recipes.add(recipe);
        }

        return recipes;
    }
}
