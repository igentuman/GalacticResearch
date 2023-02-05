package igentuman.galacticresearch.common.entity;

import micdoodle8.mods.galacticraft.api.prefab.entity.EntityAutoRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase;

public interface IGRAutoRocket {
    void setMission(String mission);
    void setLaunchPhase(EntitySpaceshipBase.EnumLaunchPhase phase);
    void setAutolaunchSetting(EntityAutoRocket.EnumAutoLaunch setting);
}
