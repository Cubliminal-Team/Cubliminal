package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.text.Decoration;

public class CubliminalMessageTypes implements Initer {
    public static final RegistryKey<MessageType> MIMIC = register("mimic");

    private static RegistryKey<MessageType> register(String id) {
        return RegistryKey.of(RegistryKeys.MESSAGE_TYPE, Cubliminal.id(id));
    }

    public static void registerAll(RegistryOps.RegistryInfoGetter infoLookup, RegistryKey<? extends Registry<MessageType>> registryKey, MutableRegistry<MessageType> registry) {
        registry.add(MIMIC, new MessageType(MessageType.CHAT_TEXT_DECORATION, Decoration.ofChat("chat.type.text.narrate")), RegistryEntryInfo.DEFAULT);
    }
}
