package igentuman.galacticresearch.common.item;

import igentuman.galacticresearch.GalacticResearch;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;


public class ItemDataCard extends Item {

	public ItemDataCard() {
		super();
		this.setMaxDamage(0);
		this.setHasSubtypes(false);
		this.setMaxStackSize(1);
		this.setTranslationKey("data_card");
		this.setRegistryName(GalacticResearch.MODID, "data_card");
		this.setCreativeTab(CreativeTabs.DECORATIONS);
	}

}
