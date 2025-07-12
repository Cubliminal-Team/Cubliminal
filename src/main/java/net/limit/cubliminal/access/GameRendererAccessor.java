package net.limit.cubliminal.access;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface GameRendererAccessor {

    void setClippingIntoWall(boolean bl);

    void setTriggered(boolean bl);
}
