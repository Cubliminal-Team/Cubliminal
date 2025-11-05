package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.entity.SeatEntity;
import net.limit.cubliminal.entity.hostile.SmilerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class CubliminalEntities implements Initer {
    public static final RegistryKey<EntityType<?>> SMILER_KEY = keyOf("smiler");

    public static final EntityType<SeatEntity> SEAT_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            Cubliminal.id("seat_entity"),
            EntityType.Builder.create(SeatEntity::new, SpawnGroup.MISC)
                    .dimensions(0f, 0f)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Cubliminal.id("seat_entity"))));

    public static EntityType<SmilerEntity> SMILER = Registry.register(
            Registries.ENTITY_TYPE,
            SMILER_KEY,
            EntityType.Builder.create(SmilerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1f, 2f)
                    .build(SMILER_KEY)
    );

    private static RegistryKey<EntityType<?>> keyOf(String name){
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, Cubliminal.id(name));
    }

    @Override
    public void init() {
        FabricDefaultAttributeRegistry.register(SMILER, SmilerEntity.createAttributes());
    }
}

