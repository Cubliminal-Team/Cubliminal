package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class CubliminalDataComponents implements Initer {

    public static final ComponentType<WrittenDocContentComponent> WRITTEN_DOC_COMPONENT = register(
            "written_doc_content", builder -> builder.codec(WrittenDocContentComponent.CODEC).cache()
    );

    private static <T> ComponentType<T> register(String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Cubliminal.id(name), builderOperator.apply(ComponentType.builder()).build());
    }
}
