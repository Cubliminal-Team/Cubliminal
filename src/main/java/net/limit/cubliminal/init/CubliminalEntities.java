package net.limit.cubliminal.init;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.entity.custom.SeatEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class CubliminalEntities implements Initer {

    public static final EntityType<SeatEntity> SEAT_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            Cubliminal.id("seat_entity"),
            EntityType.Builder.create(SeatEntity::new, SpawnGroup.MISC)
                    .dimensions(0f, 0f)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Cubliminal.id("seat_entity"))));

}

