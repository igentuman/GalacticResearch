package igentuman.galacticresearch.integration.computer;

import net.minecraft.inventory.IInventory;

public interface IComputerIntegration extends IInventory {

    String[] getMethods();
    String getComponentName();
    Object[] invoke(int method, Object[] args) throws NoSuchMethodException;
}