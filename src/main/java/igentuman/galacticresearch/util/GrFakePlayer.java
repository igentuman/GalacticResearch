package igentuman.galacticresearch.util;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class GrFakePlayer {
    public static String uiid = "408d12c9-559c-3212-bca5-d1a3fc38a0f7";

    public static GameProfile getProfile() {
        return new GameProfile(UUID.fromString(uiid),"[GR-FakePlayer]");
    }
}
