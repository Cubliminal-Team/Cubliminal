package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class CubliminalItemGroups implements Initer {
    public static final ItemGroup BACKROOMS_GROUP = Registry.register(Registries.ITEM_GROUP, Cubliminal.id("backrooms"),
            FabricItemGroup.builder().displayName(Text.translatable("itemgroup.Backrooms"))
                    .icon(() -> new ItemStack(CubliminalBlocks.YELLOW_WALLPAPERS)).entries((displayContext, entries) -> {

                        entries.add(CubliminalItems.YELLOW_WALLPAPER);
                        entries.add(CubliminalItems.CRIMSON_WALLPAPER);
                        entries.add(CubliminalBlocks.YELLOW_WALLPAPERS);
                        entries.add(CubliminalBlocks.YELLOW_WALLPAPERS_WALL);
						entries.add(CubliminalBlocks.YELLOW_WALLPAPERS_VERTICAL_SLAB);
                        entries.add(CubliminalBlocks.BOTTOM_YELLOW_WALLPAPERS);
                        entries.add(CubliminalBlocks.DAMAGED_YELLOW_WALLPAPERS);
                        entries.add(CubliminalBlocks.FALSE_CEILING);
                        entries.add(CubliminalBlocks.DAMP_CARPET);
                        entries.add(CubliminalBlocks.DIRTY_DAMP_CARPET);
                        entries.add(CubliminalBlocks.RED_DAMP_CARPET);
                        entries.add(CubliminalBlocks.RED_WALLPAPERS);
						entries.add(CubliminalBlocks.FLICKERING_FLUORESCENT_LIGHT);
						entries.add(CubliminalBlocks.FLUORESCENT_LIGHT);
						entries.add(CubliminalBlocks.FUSED_FLUORESCENT_LIGHT);
						entries.add(CubliminalBlocks.MANILA_WALLPAPERS);
						entries.add(CubliminalBlocks.TOP_MANILA_WALLPAPERS);
                        entries.add(CubliminalBlocks.GRAY_ASPHALT);
                        entries.add(CubliminalBlocks.GRAY_ASPHALT_SLAB);
                        entries.add(CubliminalBlocks.WET_GRAY_ASPHALT);
                        entries.add(CubliminalBlocks.CHAIN_WALL);
                        entries.add(CubliminalBlocks.VERTICAL_LIGHT_TUBE);
                        entries.add(CubliminalBlocks.HANGING_FLUORESCENT_LIGHTS);
                        entries.add(CubliminalBlocks.WALL_LIGHT_BULB);
                        entries.add(CubliminalBlocks.SMALL_HANGING_PIPE);
                        entries.add(CubliminalBlocks.VENTILATION_DUCT);
						entries.add(CubliminalBlocks.EMERGENCY_EXIT_DOOR_0);
						entries.add(CubliminalBlocks.EMERGENCY_EXIT_DOOR_1);
						entries.add(CubliminalBlocks.EXIT_SIGN);
						entries.add(CubliminalBlocks.EXIT_SIGN_2);
						entries.add(CubliminalBlocks.GABBRO);
                        entries.add(CubliminalBlocks.MOLD);
						entries.add(CubliminalBlocks.SMOKE_DETECTOR);
						entries.add(CubliminalBlocks.SOCKET);
                        entries.add(CubliminalBlocks.COMPUTER);
                        entries.add(CubliminalBlocks.JUMBLED_DOCUMENTS);
                        entries.add(CubliminalBlocks.ALMOND_WATER);
						entries.add(CubliminalBlocks.TWO_LONG_SPRUCE_TABLE);
                        entries.add(CubliminalBlocks.SPRUCE_CHAIR);
						entries.add(CubliminalBlocks.SINK);
						entries.add(CubliminalBlocks.SHOWER);
                        entries.add(CubliminalItems.WOODEN_PLANK);
                        entries.add(CubliminalBlocks.WOODEN_CRATE);
                        entries.add(CubliminalItems.NAILED_BAT);
                        entries.add(CubliminalItems.SILVER_INGOT);
                        entries.add(CubliminalBlocks.FLUX_CAPACITOR);

                    }).build());

	@Override
    public void init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(entries -> {
            entries.addAfter(Blocks.STRUCTURE_BLOCK, CubliminalBlocks.UNLIMITED_STRUCTURE_BLOCK);
        });
    }
}
