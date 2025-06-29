package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.block.CustomProperties;
import net.limit.cubliminal.block.custom.*;
import net.limit.cubliminal.block.custom.template.RotatableBlock;
import net.limit.cubliminal.block.custom.template.RotatableLightBlock;
import net.limit.cubliminal.item.AlmondWaterBlockItem;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.OperatorOnlyBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Rarity;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static net.minecraft.block.Blocks.createLightLevelFromLitBlockState;

public class CubliminalBlocks implements Initer {

	private static Block register(String id, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings, BiFunction<Block, Item.Settings, BlockItem> itemFactory, Item.Settings itemSettings) {
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id(id));

		Block block = blockFactory.apply(blockSettings.registryKey(blockKey));
		BlockItem item = itemFactory.apply(block, itemSettings.registryKey(itemKey));
		Registry.register(Registries.ITEM, itemKey, item);
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	private static <T> Block register(String id, BiFunction<T, AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings, T constructorData, BiFunction<Block, Item.Settings, BlockItem> itemFactory, Item.Settings itemSettings) {
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id(id));

		Block block = blockFactory.apply(constructorData, blockSettings.registryKey(blockKey));
		BlockItem item = itemFactory.apply(block, itemSettings.registryKey(itemKey));
		Registry.register(Registries.ITEM, itemKey, item);
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	private static Block register(String id, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings) {
		return register(id, blockFactory, blockSettings, BlockItem::new, new Item.Settings());
	}

	private static Block registerBlock(String id, Block block, BiFunction<Block, Item.Settings, BlockItem> itemFactory, Item.Settings itemSettings) {
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id(id));
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
		BlockItem item = itemFactory.apply(block, itemSettings.registryKey(itemKey));
		Registry.register(Registries.ITEM, itemKey, item);
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	public static TagKey<Block> of(String id) {
		return TagKey.of(RegistryKeys.BLOCK, Cubliminal.id(id));
	}

	public static final TagKey<Block> FLOOR_PALETTE = of("floor_palette");


	public static final Block UNLIMITED_STRUCTURE_BLOCK = register("unlimited_structure_block", UnlimitedStructureBlock::new,
			AbstractBlock.Settings.copy(Blocks.STRUCTURE_BLOCK), OperatorOnlyBlockItem::new, new Item.Settings().rarity(Rarity.EPIC));

	public static final Block YELLOW_WALLPAPERS = register("yellow_wallpapers", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.sounds(BlockSoundGroup.BASALT)
					.strength(5, 7)
					.requiresTool());

    public static final Block YELLOW_WALLPAPERS_WALL = register("yellow_wallpapers_wall", WallBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.sounds(BlockSoundGroup.BASALT)
					.strength(5, 7)
					.requiresTool()
					.solid());

	public static final Block YELLOW_WALLPAPERS_VERTICAL_SLAB = register("yellow_wallpapers_vertical_slab", VerticalSlabBlock::new,
			AbstractBlock.Settings.create()
				.mapColor(MapColor.TERRACOTTA_YELLOW)
				.sounds(BlockSoundGroup.BASALT)
				.strength(5, 7)
				.requiresTool());

    public static final Block DAMAGED_YELLOW_WALLPAPERS = register("damaged_yellow_wallpapers", Block::new,
            AbstractBlock.Settings.create()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.sounds(BlockSoundGroup.BASALT)
					.strength(2, 6));

    public static final Block BOTTOM_YELLOW_WALLPAPERS = register("bottom_yellow_wallpapers", Block::new,
            AbstractBlock.Settings.create()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.sounds(BlockSoundGroup.BASALT)
					.strength(5, 7)
					.requiresTool());

    public static final Block FALSE_CEILING = register("false_ceiling", Block::new,
            AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.sounds(BlockSoundGroup.CALCITE)
					.strength(2, 6)
					.requiresTool());

    public static final Block DAMP_CARPET = register("damp_carpet", Block::new,
            AbstractBlock.Settings.create()
					.mapColor(MapColor.OAK_TAN)
					.sounds(BlockSoundGroup.WOOL)
					.strength(1, 3));

    public static final Block DIRTY_DAMP_CARPET = register("dirty_damp_carpet", Block::new,
            AbstractBlock.Settings.create()
					.mapColor(MapColor.OAK_TAN)
					.sounds(BlockSoundGroup.WOOL)
					.strength(1, 3));

	public static final Block RED_WALLPAPERS = register("red_wallpapers", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.TERRACOTTA_RED)
					.sounds(BlockSoundGroup.BASALT)
					.strength(5, 7)
					.requiresTool());

	public static final Block RED_DAMP_CARPET = register("red_damp_carpet", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.RED)
					.sounds(BlockSoundGroup.WOOL)
					.strength(1, 3)
					.slipperiness(0.7f));

	public static final Block FLICKERING_FLUORESCENT_LIGHT = register("fluorescent_light", FluorescentLightBlock::new,
			AbstractBlock.Settings.create()
				.mapColor(MapColor.WHITE)
				.strength(1, 2)
				.ticksRandomly()
				.luminance(shouldBeRed(15, 8))
				.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.emissiveLighting(Blocks::always)
					.requiresTool());

	public static final Block FLUORESCENT_LIGHT = register("deco_fluorescent_light", FluorescentLightBlock::new,
			AbstractBlock.Settings.create()
				.mapColor(MapColor.WHITE)
				.strength(1, 2)
				.luminance(shouldBeRed(15, 8))
				.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.emissiveLighting(Blocks::always)
					.requiresTool());

	public static final Block FUSED_FLUORESCENT_LIGHT = register("fused_fluorescent_light", FusedFluorescentLightBlock::new,
			AbstractBlock.Settings.create()
				.mapColor(MapColor.STONE_GRAY)
				.strength(1, 2)
				.ticksRandomly()
				.luminance(shouldBeRed(6, 4))
				.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.requiresTool());

	public static final Block SMOKE_DETECTOR = register("smoke_detector", SmokeDetectorBlock::new,
			AbstractBlock.Settings.create()
				.mapColor(MapColor.DEEPSLATE_GRAY)
				.strength(2.6f, 2.6f)
				.offset(AbstractBlock.OffsetType.XZ)
				.dynamicBounds()
				.sounds(BlockSoundGroup.METAL)
				.pistonBehavior(PistonBehavior.DESTROY)
				.requiresTool());

	public static final Block SOCKET = registerBlock("socket", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("socket")))
					.mapColor(MapColor.WHITE_GRAY)
					.strength(3, 3)
					.sounds(BlockSoundGroup.CALCITE)
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool())
			.needsAttachment()
			.voxelShapes(4.5, 4, 0, 11.5, 12.5, 0.5), BlockItem::new, new Item.Settings());

	public static final Block ALMOND_WATER = register("almond_water", AlmondWaterBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.PALE_YELLOW)
					.breakInstantly()
					.dynamicBounds()
					.offset(AbstractBlock.OffsetType.XZ)
					.sounds(BlockSoundGroup.LANTERN)
					.pistonBehavior(PistonBehavior.DESTROY),
			AlmondWaterBlockItem::new, new Item.Settings()
					.food(CubliminalFoodComponents.ALMOND_WATER)
					.maxCount(16)
					.component(DataComponentTypes.CONSUMABLE, CubliminalFoodComponents.ALMOND_WATER_COMPONENT));

	public static final Block JUMBLED_DOCUMENTS = register("jumbled_documents", JumbledDocumentsBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.WHITE)
					.breakInstantly()
					.sounds(new BlockSoundGroup(1.0f, 1.0f, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundEvents.ITEM_BOOK_PAGE_TURN,
							SoundEvents.ITEM_BOOK_PAGE_TURN, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundEvents.ITEM_BOOK_PAGE_TURN))
					.nonOpaque()
					.noCollision()
					.noBlockBreakParticles()
					.pistonBehavior(PistonBehavior.DESTROY));

	public static final Block TWO_LONG_SPRUCE_TABLE = register("two_long_spruce_table", TwoLongTableBlock::new,
			AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS)
					.requiresTool());

	public static final Block SPRUCE_CHAIR = register("spruce_chair", ChairBlock::new,
			AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS)
					.requiresTool());

	public static final Block MANILA_WALLPAPERS = register("manila_wallpapers", Block::new,
			AbstractBlock.Settings.create()
				.mapColor(MapColor.IRON_GRAY)
				.sounds(BlockSoundGroup.BASALT)
				.strength(5, 7)
				.requiresTool());

	public static final Block TOP_MANILA_WALLPAPERS = register("top_manila_wallpapers", Block::new,
			AbstractBlock.Settings.create()
				.mapColor(MapColor.IRON_GRAY)
				.sounds(BlockSoundGroup.BASALT)
				.strength(5, 7)
				.requiresTool());

	public static final Block EMERGENCY_EXIT_DOOR_0 = register("emergency_exit_door_0", DoorBlock::new,
				AbstractBlock.Settings.create()
						.mapColor(MapColor.RED)
						.strength(5.0f)
						.nonOpaque()
						.pistonBehavior(PistonBehavior.DESTROY)
						.requiresTool(),
			BlockSetType.COPPER, BlockItem::new, new Item.Settings());

	public static final Block EMERGENCY_EXIT_DOOR_1 = register("emergency_exit_door_1", DoorBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.GRAY)
					.strength(5.0f)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool(),
			BlockSetType.COPPER, BlockItem::new, new Item.Settings());

	public static final Block EXIT_SIGN = registerBlock("exit_sign", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("exit_sign")))
					.mapColor(MapColor.PALE_GREEN)
					.strength(3, 3)
					.sounds(BlockSoundGroup.CALCITE)
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool())
			.needsAttachment()
			.voxelShapes(0, 4, 0, 16, 13, 1), BlockItem::new, new Item.Settings());

	public static final Block EXIT_SIGN_2 = registerBlock("exit_sign_2", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("exit_sign_2")))
					.mapColor(MapColor.PALE_GREEN)
					.strength(3, 3)
					.sounds(BlockSoundGroup.CALCITE)
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool())
			.needsAttachment()
			.voxelShapes(0, 4, 0, 16, 13, 1), BlockItem::new, new Item.Settings());

	public static final Block COMPUTER = registerBlock("computer", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("computer")))
					.mapColor(MapColor.PALE_YELLOW)
					.strength(4, 5)
					.sounds(BlockSoundGroup.GLASS)
					.dropsNothing()
					.nonOpaque()
					.pistonBehavior(PistonBehavior.BLOCK))
			.voxelShapes(2, 0, 2, 14, 14	, 14), BlockItem::new, new Item.Settings());

	public static final Block SINK = register("sink", SinkBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.PALE_YELLOW)
					.strength(4, 5)
					.sounds(BlockSoundGroup.DEEPSLATE_BRICKS)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.BLOCK)
					.requiresTool());

	public static final Block SHOWER = register("shower", ShowerBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.DEEPSLATE_GRAY)
					.strength(4, 5)
					.sounds(BlockSoundGroup.COPPER)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.BLOCK)
					.requiresTool());

	public static final Block GRAY_ASPHALT = register("gray_asphalt", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(DyeColor.GRAY)
					.strength(2f)
					.requiresTool());

	public static final Block GRAY_ASPHALT_SLAB = register("gray_asphalt_slab", SlabBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(DyeColor.GRAY)
					.strength(2f)
					.requiresTool());

	public static final Block WET_GRAY_ASPHALT = register("wet_gray_asphalt", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(DyeColor.GRAY)
					.strength(2f)
					.requiresTool()
					.slipperiness(0.87f));

	public static final Block VERTICAL_LIGHT_TUBE = registerBlock("vertical_light_tube", new RotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("vertical_light_tube")))
					.mapColor(MapColor.WHITE)
					.strength(1, 2)
					.luminance(createLightLevelFromLitBlockState(15))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.requiresTool())
			.voxelShapes(6, 0, 0, 10, 32, 3), BlockItem::new, new Item.Settings());

	public static final Block HANGING_FLUORESCENT_LIGHTS = registerBlock("hanging_fluorescent_lights", new RotatableLightBlock(
					AbstractBlock.Settings.create()
							.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("hanging_fluorescent_lights")))
							.mapColor(MapColor.WHITE)
							.strength(3f, 2.6f)
							.luminance(createLightLevelFromLitBlockState(15))
							.sounds(BlockSoundGroup.GLASS))
					.voxelShapes(0, 14.4, 5, 16, 15.9, 11),
			BlockItem::new, new Item.Settings());

	public static final Block SMALL_HANGING_PIPE = register("small_hanging_pipe", SmallHangingPipeBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.strength(2.6f, 2.6f)
					.requiresTool()
					.sounds(BlockSoundGroup.METAL)
					.nonOpaque());

	public static final Block LETTER_F = registerBlock("letter_f", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("letter_f")))
					.pistonBehavior(PistonBehavior.DESTROY)
					.breakInstantly()
					.sounds(BlockSoundGroup.INTENTIONALLY_EMPTY)
					.nonOpaque()
					.noCollision()
					.noBlockBreakParticles())
					.voxelShapes(0, 0, 0, 16, 16, 0.1)
					.notCollidable()
					.notSolid(),
			BlockItem::new, new Item.Settings());

	public static final Block VENTILATION_DUCT = register("ventilation_duct", VentilationDuctBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.strength(5.0f, 6.0f)
					.requiresTool()
					.sounds(BlockSoundGroup.METAL));

	public static final Block WALL_LIGHT_BULB = registerBlock("wall_light_bulb", new RotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("wall_light_bulb")))
					.mapColor(MapColor.WHITE)
					.strength(3f, 2.6f)
					.luminance(createLightLevelFromLitBlockState(15))
					.sounds(BlockSoundGroup.GLASS))
					.voxelShapes(3.5, 3.5, 0, 12.5, 12.5, 7.5),
			BlockItem::new, new Item.Settings());

	public static final Block CHAIN_WALL = registerBlock("chain_wall", new RotatableBlock(
			AbstractBlock.Settings.copy(Blocks.IRON_BARS)
					.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id("chain_wall"))))
			.voxelShapes(0, 0, 14, 16, 16, 16),
			BlockItem::new, new Item.Settings());

	public static final Block WOODEN_CRATE = register("wooden_crate", Block::new,
			AbstractBlock.Settings.copy(Blocks.BARREL));

	public static final Block THE_LOBBY_GATEWAY_BLOCK = register("the_lobby_gateway_block", TheLobbyGatewayBlock::new,
			AbstractBlock.Settings.copy(Blocks.GLASS)
					.strength(-1, 3600000)
					.noCollision()
					.dropsNothing()
					.pistonBehavior(PistonBehavior.BLOCK)
					.noBlockBreakParticles()
					.luminance(createLightLevelFromLitBlockState(9)));

	public static final Block FLUX_CAPACITOR = register("flux_capacitor", FluxCapacitorBlock::new,
			AbstractBlock.Settings.copy(Blocks.OBSIDIAN)
					.mapColor(MapColor.GRAY)
					.luminance(isPowered(15)));

	public static final Block GABBRO = register("gabbro", Block::new,
			AbstractBlock.Settings.copy(Blocks.STONE)
					.mapColor(MapColor.BLACK)
					.dropsNothing()
					.pistonBehavior(PistonBehavior.BLOCK)
					.strength(-1, 3600000));

	public static final Block MOLD = register("mold", MoldBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.BLACK)
					.replaceable()
					.noCollision()
					.strength(0.2f)
					.sounds(BlockSoundGroup.GLOW_LICHEN)
					.burnable()
					.pistonBehavior(PistonBehavior.DESTROY));

	public static final Block POOL_TILES = register("pool_tiles", Block::new,
			AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE)
					.sounds(BlockSoundGroup.DEEPSLATE_TILES));

	public static final Block POOL_TILE_STAIRS = register("pool_tile_stairs", StairsBlock::new,
					AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE),
			CubliminalBlocks.POOL_TILES.getDefaultState(), BlockItem::new, new Item.Settings());

	public static final Block POOL_TILE_SLAB = register("pool_tile_slab", SlabBlock::new,
			AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE));

	public static final Block POOL_TILE_WALL = register("pool_tile_wall", WallBlock::new,
			AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE).solid());


	public static ToIntFunction<BlockState> shouldBeRed(int defaultLevel, int redLevel) {
		return (state) -> {
			int litLevel = state.get(CustomProperties.RED) ? redLevel : defaultLevel;
			return (Boolean) state.get(Properties.LIT) ? litLevel : 0;
		};
	}

	public static ToIntFunction<BlockState> isPowered(int litLevel) {
		return (state) -> state.get(Properties.POWERED) ? litLevel : 0;
	}

	@Override
    public void init() {
		FuelRegistryEvents.BUILD.register((builder, context) -> {
			builder.add(YELLOW_WALLPAPERS.asItem(), 300);
			builder.add(YELLOW_WALLPAPERS_WALL.asItem(), 300);
			builder.add(YELLOW_WALLPAPERS_VERTICAL_SLAB.asItem(), 300);
			builder.add(BOTTOM_YELLOW_WALLPAPERS.asItem(), 300);
			builder.add(DAMAGED_YELLOW_WALLPAPERS.asItem(), 200);
			builder.add(MANILA_WALLPAPERS.asItem(), 300);
			builder.add(TOP_MANILA_WALLPAPERS.asItem(), 300);
			builder.add(TWO_LONG_SPRUCE_TABLE.asItem(), 1000);
			builder.add(SPRUCE_CHAIR.asItem(), 800);
			builder.add(DAMP_CARPET.asItem(), 100);
			builder.add(DIRTY_DAMP_CARPET.asItem(), 100);
			builder.add(RED_DAMP_CARPET.asItem(), 100);
			builder.add(RED_WALLPAPERS.asItem(), 300);
			builder.add(WOODEN_CRATE.asItem(), 300);
		});
    }
}
