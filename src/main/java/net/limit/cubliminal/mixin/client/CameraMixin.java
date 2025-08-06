package net.limit.cubliminal.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.access.CameraAccessor;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin implements CameraAccessor {

    @Shadow
    private BlockView area;

    @Shadow
    @Final
    private BlockPos.Mutable blockPos;

    @Override
    public BlockView getArea(){
        return this.area;
    }

    @Override
    public BlockPos getBlockPos(){
        return this.blockPos;
    }

}
