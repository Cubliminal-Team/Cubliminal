package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.entity.SeatEntity;
import net.limit.cubliminal.entity.hostile.HoundEntity;
import net.limit.cubliminal.entity.hostile.SkinStealerEntity;
import net.limit.cubliminal.entity.hostile.SmilerEntity;
import net.limit.cubliminal.entity.projectiles.FiresaltEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class CubliminalEntities implements Initer {

    public static final RegistryKey<EntityType<?>> SEAT_KEY = keyOf("seat_entity");
    public static final RegistryKey<EntityType<?>> SMILER_KEY = keyOf("smiler");
    public static final RegistryKey<EntityType<?>> HOUND_KEY = keyOf("hound");
    public static final RegistryKey<EntityType<?>> SKIN_STEALER_KEY = keyOf("skin_stealer");
    public static final RegistryKey<EntityType<?>> FIRESALT_KEY = keyOf("firesalt");

    public static final EntityType<SeatEntity> SEAT_ENTITY = Registry.register(
            Registries.ENTITY_TYPE,
            SEAT_KEY,
            EntityType.Builder.create(SeatEntity::new, SpawnGroup.MISC)
                    .dimensions(0f, 0f)
                    .build(SEAT_KEY));

    public static EntityType<SmilerEntity> SMILER = Registry.register(
            Registries.ENTITY_TYPE,
            SMILER_KEY,
            EntityType.Builder.create(SmilerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1f, 2f)
                    .build(SMILER_KEY)
    );

    public static EntityType<HoundEntity> HOUND = Registry.register(
            Registries.ENTITY_TYPE,
            HOUND_KEY,
            EntityType.Builder.create(HoundEntity::new, SpawnGroup.MONSTER)
                    .dimensions(1f, 2f)
                    .build(HOUND_KEY)
    );

    public static EntityType<SkinStealerEntity> SKIN_STEALER = Registry.register(
            Registries.ENTITY_TYPE,
            SKIN_STEALER_KEY,
            EntityType.Builder.create(SkinStealerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.8f, 2.7f)
                    .eyeHeight(2.4f)
                    .maxTrackingRange(8)
                    .build(SKIN_STEALER_KEY)
    );

    public static EntityType<FiresaltEntity> FIRESALT = Registry.register(
            Registries.ENTITY_TYPE,
            FIRESALT_KEY,
            EntityType.Builder.<FiresaltEntity>create(FiresaltEntity::new, SpawnGroup.MISC)
                    .dropsNothing()
                    .dimensions(0.25F, 0.25F)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build(FIRESALT_KEY)
    );

    private static RegistryKey<EntityType<?>> keyOf(String name){
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, Cubliminal.id(name));
    }

    @Override
    public void init() {
        FabricDefaultAttributeRegistry.register(SMILER, SmilerEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(HOUND, HoundEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(SKIN_STEALER, SkinStealerEntity.createAttributes());
    }
}

