package net.limit.cubliminal.mixin;

import net.limit.cubliminal.networking.s2c.StructureTemplateListS2CPayload;
import net.minecraft.block.Block;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Inject(
            method = "saveFromWorld",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/BlockPos;add(Lnet/minecraft/util/math/Vec3i;)Lnet/minecraft/util/math/BlockPos;"
            )
    )
    private void cubliminal$onStructureSave(World world, BlockPos start, Vec3i dimensions,
                                            boolean includeEntities, Block ignoredBlock, CallbackInfo ci) {
        if (world.getServer() != null) {
            world.getServer().getPlayerManager().getPlayerList().forEach(StructureTemplateListS2CPayload::sendTo);
        }
    }

}
