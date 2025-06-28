package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import org.apache.commons.io.function.IOQuadFunction;

import java.io.IOException;
import java.util.function.Function;

import static net.minecraft.item.Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
import static net.minecraft.item.Item.BASE_ATTACK_SPEED_MODIFIER_ID;


public class CubliminalItems implements Initer {

    public static final Item YELLOW_WALLPAPER = register("yellow_wallpaper", Item::new, new Item.Settings());
    public static final Item CRIMSON_WALLPAPER = register("crimson_wallpaper", Item::new, new Item.Settings());
    public static final Item WOODEN_PLANK = register("wooden_plank", Item::new, new Item.Settings().attributeModifiers(
            AttributeModifiersComponent.builder()
                    .add(EntityAttributes.ATTACK_DAMAGE,
                            new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, 2.0d,
                                    EntityAttributeModifier.Operation.ADD_VALUE),
                            AttributeModifierSlot.MAINHAND)
                    .add(EntityAttributes.ATTACK_SPEED,
                            new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, -2.0d,
                                    EntityAttributeModifier.Operation.ADD_VALUE),
                            AttributeModifierSlot.MAINHAND)
                    .build()));

    public static final Item NAILED_BAT = registerTool("nailed_bat", SwordItem::new,
            ToolMaterial.IRON,
            3.0f + ToolMaterial.IRON.attackDamageBonus(),
            -2.2f,
            new Item.Settings().maxCount(1));

    public static final Item SILVER_INGOT = register("silver_ingot", Item::new, new Item.Settings());



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
            builder.add(WOODEN_PLANK, 100);
        });
	}
}
