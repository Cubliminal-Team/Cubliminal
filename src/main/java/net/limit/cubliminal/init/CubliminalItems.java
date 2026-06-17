package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.apache.commons.io.function.IOQuadFunction;

import java.io.IOException;
import java.util.function.Function;

public class CubliminalItems implements Initer {

    public static final Item YELLOW_WALLPAPER = register("yellow_wallpaper", Item::new, new Item.Settings());

    public static final Item CRIMSON_WALLPAPER = register("crimson_wallpaper", Item::new, new Item.Settings());

    public static final Item NAILED_BAT = registerTool("nailed_bat", SwordItem::new,
            ToolMaterial.IRON,
            1.0f + ToolMaterial.IRON.attackDamageBonus(),
            -2.2f,
            new Item.Settings().maxCount(1));

    public static final Item SILVER_INGOT = register("silver_ingot", Item::new, new Item.Settings());

    public static final Item ALMOND_WATER_BUCKET = register("almond_water_bucket",
            settings -> new BucketItem(CubliminalFluids.ALMOND_WATER, settings),
            new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    public static final Item CONTAMINATED_WATER_BUCKET = register("contaminated_water_bucket",
            settings -> new BucketItem(CubliminalFluids.CONTAMINATED_WATER, settings),
            new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    public static final Item BLACK_SLUDGE_BUCKET = register("black_sludge_bucket",
            settings -> new BucketItem(CubliminalFluids.BLACK_SLUDGE, settings),
            new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1));

    public static Item SMILER_SPAWN_EGG = registerSpawnEgg("smiler", CubliminalEntities.SMILER, 0, 16777215);
    public static Item HOUND_SPAWN_EGG = registerSpawnEgg("hound", CubliminalEntities.HOUND, 56063, 44543);
    public static Item SKIN_STEALER_SPAWN_EGG = registerSpawnEgg("skin_stealer", CubliminalEntities.SKIN_STEALER, 6381921, 0);

    /**
     * Register a spawn egg for an entity.
     * @param entityName The entity name (All lower case).
     * @param type The type of entity
     * @param primaryColor The primary color of the spawn egg
     * @param secondaryColor The secondary color of the spawn egg
     * @return The item
     */
    private static Item registerSpawnEgg(String entityName, EntityType<? extends MobEntity> type, int primaryColor, int secondaryColor){
        return register(entityName + "_spawn_egg", settings -> new SpawnEggItem(type, primaryColor, secondaryColor, settings));
    }

    public static Item register(String id, Function<Item.Settings, Item> factory){
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
        Item item = factory.apply(new Item.Settings().registryKey(itemKey));
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    private static Item register(String id, Function<Item.Settings, Item> itemFactory, Item.Settings itemSettings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
        Item item = itemFactory.apply(itemSettings.registryKey(itemKey));
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    private static <T, U, V> Item registerTool(String id, IOQuadFunction<T, U, V, Item.Settings, Item> itemFactory, T first, U second, V third, Item.Settings itemSettings) {
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
        try {
            Item item = itemFactory.apply(first, second, third, itemSettings.registryKey(itemKey));
            return Registry.register(Registries.ITEM, itemKey, item);
        } catch (IOException e) {
            Cubliminal.LOGGER.error("Failed to register tool '{}'", id);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void init() {
        FuelRegistryEvents.BUILD.register((builder, context) -> {

        });
	}
}
