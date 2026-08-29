package net.limit.cubliminal.mixin;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.networking.s2c.StructureTemplateInfoS2CPayload;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureTemplateManager.class)
public abstract class StructureTemplateManagerMixin {

    @Inject(method = "saveTemplate", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/NbtCompound;Ljava/io/OutputStream;)V", shift = At.Shift.AFTER))
    private void cubliminal$onSaveTemplate(Identifier templateName, CallbackInfoReturnable<Boolean> cir) {
        StructureTemplateInfoS2CPayload.sendToAll(Cubliminal.SERVER.getServer().getPlayerManager().getPlayerList());
    }

}
