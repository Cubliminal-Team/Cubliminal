package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.client.screen.DocEditScreenHandler;
import net.limit.cubliminal.client.screen.DocScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.math.BlockPos;

public class CubliminalScreenHandlers implements Initer {

    public static final ScreenHandlerType<DocScreenHandler> DOC_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER, Cubliminal.id("doc_screen_handler"), new ExtendedScreenHandlerType<>(
                    DocScreenHandler::make, BlockPos.PACKET_CODEC
            )
    );

    public static final ScreenHandlerType<DocEditScreenHandler> DOC_EDIT_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER, Cubliminal.id("doc_edit_screen_handler"), new ExtendedScreenHandlerType<>(
                    DocEditScreenHandler::make, BlockPos.PACKET_CODEC
            )
    );

}
