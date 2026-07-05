package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.Initer;
import net.limit.cubliminal.block.custom.template.*;
import net.limit.cubliminal.block.state.CustomProperties;
import net.limit.cubliminal.block.custom.*;
import net.limit.cubliminal.block.custom.pipe.*;
import net.limit.cubliminal.block.fluid.AlmondWaterFluidBlock;
import net.limit.cubliminal.block.fluid.BlackSludgeFluidBlock;
import net.limit.cubliminal.block.fluid.ContaminatedWaterBlock;
import net.limit.cubliminal.block.fluid.CustomFluidBlock;
import net.limit.cubliminal.block.fluid.FluidBlockFactory;
import net.limit.cubliminal.item.AlmondWaterItem;
import net.limit.cubliminal.item.CanItem;
import net.limit.cubliminal.item.WrittenDocumentItem;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.fluid.FlowableFluid;
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
import net.minecraft.util.math.Vec3d;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static net.minecraft.block.Blocks.createLightLevelFromLitBlockState;
import static net.minecraft.item.Item.BASE_ATTACK_DAMAGE_MODIFIER_ID;
import static net.minecraft.item.Item.BASE_ATTACK_SPEED_MODIFIER_ID;

/**
 * This class holds block related registry utilities and all the custom block and block tag registries. Fuel registry is at the bottom.
 */

@SuppressWarnings("deprecation")
public class CubliminalBlocks implements Initer {

	//region Registration Methods
	/**
	 * Registers a block with a custom block item constructor and/or custom block item settings.
	 * @param id The name of the block.
	 * @param blockFactory The block constructor.
	 * @param blockSettings The settings used to create the block.
	 * @param itemFactory The block item constructor.
	 * @param itemSettings The settings used to create the block item.
	 * @return a new block.
	 */
	private static Block register(String id, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings, BiFunction<Block, Item.Settings, BlockItem> itemFactory, Item.Settings itemSettings) {
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
		RegistryKey<Block> blockKey = key(id);

		Block block = blockFactory.apply(blockSettings.registryKey(blockKey));
		BlockItem item = itemFactory.apply(block, itemSettings.registryKey(itemKey));
		Registry.register(Registries.ITEM, itemKey, item);
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	/**
	 * Registers a block with the default block item constructor and block item settings. This is the default registration function.
	 * @param id The name of the block.
	 * @param blockFactory The block constructor.
	 * @param blockSettings The settings used to create the block.
	 * @return a new block.
	 */
	private static Block register(String id, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings) {
		return register(id, blockFactory, blockSettings, BlockItem::new, new Item.Settings());
	}

	/**
	 * Registers a block that requires additional data in its block constructor (e.g. stair blocks) with a custom block item constructor and/or custom block item settings.
	 * @param id The name of the block.
	 * @param blockFactory The block constructor.
	 * @param blockSettings The settings used to create the block.
	 * @param constructorData The additional data imputed to the block constructor.
	 * @param itemFactory The block item constructor.
	 * @param itemSettings The settings used to create the block item.
	 * @return a new block.
	 */
	private static <T> Block register(String id, BiFunction<T, AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings, T constructorData, BiFunction<Block, Item.Settings, BlockItem> itemFactory, Item.Settings itemSettings) {
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
		RegistryKey<Block> blockKey = key(id);

		Block block = blockFactory.apply(constructorData, blockSettings.registryKey(blockKey));
		BlockItem item = itemFactory.apply(block, itemSettings.registryKey(itemKey));
		Registry.register(Registries.ITEM, itemKey, item);
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	/**
	 * Registers a block that requires additional data in its block constructor (e.g. stair blocks) with the default block item constructor and block item settings.
	 * @param id The name of the block.
	 * @param blockFactory The block constructor.
	 * @param blockSettings The settings used to create the block.
	 * @param constructorData The additional data imputed to the block constructor.
	 * @return a new block.
	 */
	private static <T> Block register(String id, BiFunction<T, AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings, T constructorData) {
		return register(id, blockFactory, blockSettings, constructorData, BlockItem::new, new Item.Settings());
	}

	/**
	 * Registers a block with a custom block item constructor and/or custom block item settings, yet using an already created block object. Often used when custom block functions want to be used <p>(e.g. voxel shapes).<p><b>Warning! you need to add the registry key to block settings manually with {@link Block.Settings#registryKey(RegistryKey)}</b>
	 * @param id The name of the block.
	 * @param block The new block object to be registered.
	 * @param itemFactory The block item constructor.
	 * @param itemSettings The settings used to create the block item.
	 * @return a new block.
	 */
	private static Block registerBlock(String id, Block block, BiFunction<Block, Item.Settings, BlockItem> itemFactory, Item.Settings itemSettings) {
		RegistryKey<Block> blockKey = key(id);
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Cubliminal.id(id));
		BlockItem item = itemFactory.apply(block, itemSettings.registryKey(itemKey));
		Registry.register(Registries.ITEM, itemKey, item);
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	/**
	 * Registers a block with the default block item constructor and block item settings, yet using an already created block object. Often used when custom block functions want to be used <p>(e.g. voxel shapes).
	 * <p><b>Warning! you need to add the registry key to block settings manually with {@link Block.Settings#registryKey(RegistryKey)}</b>
	 * @param id The name of the block.
	 * @param block The new block object to be registered.
	 * @return a new block.
	 */
	private static Block registerBlock(String id, Block block) {
		return registerBlock(id, block, BlockItem::new, new Item.Settings());
	}

	/**
	 * Registers a block without block item. Mostly used for fluids.
	 * @param id The name of the block.
	 * @param blockFactory The block constructor.
	 * @param blockSettings The settings used to create the block.
	 * @return a new block.
	 */
	private static Block registerWithoutItem(String id, Function<AbstractBlock.Settings, Block> blockFactory, AbstractBlock.Settings blockSettings) {
		RegistryKey<Block> blockKey = key(id);
		Block block = blockFactory.apply(blockSettings.registryKey(blockKey));
		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	/**
	 * Registers a fluid block.
	 * @param name The name of the fluid.
	 * @param flowableFluid The flowable fluid.
	 * @param factory The fluid block factory
	 * @param settings The fluid settings.
	 * @return a block of the custom fluid.
	 */
	private static Block registerFluidBlock(String name, FlowableFluid flowableFluid, FluidBlockFactory factory, CustomFluidBlock.Settings settings){
		return registerWithoutItem(
				name + "_fluid",
				blockSettings -> factory.create(flowableFluid, blockSettings, settings),
				AbstractBlock.Settings.copyShallow(Blocks.WATER)
		);
	}
	//endregion

	public static RegistryKey<Block> key(String id) {
		return RegistryKey.of(RegistryKeys.BLOCK, Cubliminal.id(id));
	}

	public static TagKey<Block> of(String id) {
		return TagKey.of(RegistryKeys.BLOCK, Cubliminal.id(id));
	}

	public static final TagKey<Block> FLOOR_PALETTE = of("floor_palette");

	public static final BlockSoundGroup PAPER = new BlockSoundGroup(1.0f, 1.0f, CubliminalSounds.PAPER_BREAK, CubliminalSounds.PAPER_STEP, CubliminalSounds.PAPER_PLACE, CubliminalSounds.PAPER_HIT, CubliminalSounds.PAPER_FALL);

	//region Blocks
	public static final Block MULTISTRUCTURE_BLOCK = register("multistructure_block", MultiStructureBlock::new,
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

    public static final Block BOTTOM_YELLOW_WALLPAPERS = register("bottom_yellow_wallpapers", BlockWithSocket::new,
            AbstractBlock.Settings.create()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.sounds(BlockSoundGroup.BASALT)
					.strength(5, 7)
					.requiresTool());

	public static final Block TOP_YELLOW_WALLPAPERS = register("top_yellow_wallpapers", Block::new,
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

	public static final Block CARPETED_YELLOW_WALLPAPERS = register("carpeted_yellow_wallpapers", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.OAK_TAN)
					.sounds(new BlockSoundGroup(
							1.0f, 1.0f,
							SoundEvents.BLOCK_BASALT_BREAK,
							SoundEvents.BLOCK_WOOL_STEP,
							SoundEvents.BLOCK_BASALT_PLACE,
							SoundEvents.BLOCK_BASALT_HIT,
							SoundEvents.BLOCK_WOOL_FALL
					))
					.strength(5, 7));

    public static final Block DAMP_CARPET = register("damp_carpet", Block::new,
            AbstractBlock.Settings.create()
					.mapColor(MapColor.OAK_TAN)
					.sounds(BlockSoundGroup.WOOL)
					.strength(1, 3));

	public static final Block DAMP_CARPET_STAIRS = register("damp_carpet_stairs", StairsBlock::new,
			AbstractBlock.Settings.copy(CARPETED_YELLOW_WALLPAPERS), CARPETED_YELLOW_WALLPAPERS.getDefaultState());

	public static final Block DAMP_CARPET_SLAB = register("damp_carpet_slab", SlabBlock::new, AbstractBlock.Settings.copy(DAMP_CARPET));

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

	public static final Block RED_DROP_CEILING = register("red_drop_ceiling", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.TERRACOTTA_RED)
					.sounds(BlockSoundGroup.CALCITE)
					.strength(2, 6)
					.requiresTool());

	public static final Block FLICKERING_FLUORESCENT_LIGHT = registerBlock("fluorescent_light", new FluorescentLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("fluorescent_light"))
					.mapColor(MapColor.WHITE)
					.strength(1, 2)
					.luminance(shouldBeRed(15, 8))
					.sounds(BlockSoundGroup.GLASS)
					.ticksRandomly()
					.nonOpaque()
					.emissiveLighting((s, w, p) -> s.get(FluorescentLightBlock.LIT))
					.requiresTool(), false)
			.needsAttachment());

	public static final Block FLUORESCENT_LIGHT = registerBlock("deco_fluorescent_light", new FluorescentLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("deco_fluorescent_light"))
					.mapColor(MapColor.WHITE)
					.strength(1, 2)
					.luminance(shouldBeRed(15, 8))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.emissiveLighting((s, w, p) -> s.get(FluorescentLightBlock.LIT))
					.requiresTool(), false)
			.needsAttachment());

	public static final Block FUSED_FLUORESCENT_LIGHT = registerBlock("fused_fluorescent_light", new FluorescentLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("fused_fluorescent_light"))
					.mapColor(MapColor.STONE_GRAY)
					.strength(1, 2)
					.luminance(shouldBeRed(6, 4))
					.sounds(BlockSoundGroup.GLASS)
					.ticksRandomly()
					.nonOpaque()
					.requiresTool(), true)
			.needsAttachment());

	public static final Block SMOKE_DETECTOR = register("smoke_detector", SmokeDetectorBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.DEEPSLATE_GRAY)
					.strength(2.6f)
					.offset(AbstractBlock.OffsetType.XZ)
					.dynamicBounds()
					.sounds(BlockSoundGroup.METAL)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool());

	public static final Block SOCKET = registerBlock("socket", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("socket"))
					.mapColor(MapColor.WHITE_GRAY)
					.strength(3, 3)
					.sounds(BlockSoundGroup.CALCITE)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool())
			.needsAttachment()
			.voxelShapes(4.5, 4, 0, 11.5, 12.5, 0.5));

	public static final Block ALMOND_WATER_CAN = register("almond_water_can", CanBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.PALE_YELLOW)
					.breakInstantly()
					.dynamicBounds()
					.offset(AbstractBlock.OffsetType.XZ)
					.sounds(BlockSoundGroup.LANTERN)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY),
			AlmondWaterItem::new, new Item.Settings()
					.food(CubliminalFoodComponents.ALMOND_WATER)
					.maxCount(16)
					.component(DataComponentTypes.CONSUMABLE, CubliminalFoodComponents.ALMOND_WATER_COMPONENT));

	public static final Block CAN = register("can", CanBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.breakInstantly()
					.dynamicBounds()
					.offset(AbstractBlock.OffsetType.XZ)
					.sounds(BlockSoundGroup.LANTERN)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY),
			CanItem::new, new Item.Settings().maxCount(16));

	public static final Block JUMBLED_DOCUMENTS = register("jumbled_documents", JumbledDocumentsBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.WHITE)
					.breakInstantly()
					.sounds(PAPER)
					.nonOpaque()
					.noCollision()
					.noBlockBreakParticles()
					.pistonBehavior(PistonBehavior.DESTROY));

	public static final Block WRITTEN_DOCUMENT = register("written_document", WrittenDocumentBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.WHITE)
					.breakInstantly()
					.sounds(PAPER)
					.nonOpaque()
					.noCollision()
					.noBlockBreakParticles()
					.dropsNothing()
					.pistonBehavior(PistonBehavior.DESTROY),
			WrittenDocumentItem::new, new Item.Settings().maxCount(16));

	public static final Block TWO_LONG_SPRUCE_TABLE = register("two_long_spruce_table", TwoLongTableBlock::new,
			AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS).requiresTool());

	public static final Block SPRUCE_CHAIR = register("spruce_chair", ChairBlock::new,
			AbstractBlock.Settings.copy(Blocks.SPRUCE_PLANKS).requiresTool());

	public static final Block DARK_OAK_RAILING = register("dark_oak_railing", RailingBlock::new,
			AbstractBlock.Settings.copy(Blocks.DARK_OAK_PLANKS));

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

	public static final Block BOTTOM_MANILA_WALLPAPERS = register("bottom_manila_wallpapers", Block::new,
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
			BlockSetType.COPPER);

	public static final Block EMERGENCY_EXIT_DOOR_1 = register("emergency_exit_door_1", DoorBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.GRAY)
					.strength(5.0f)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool(),
			BlockSetType.COPPER);

	public static final Block EXIT_SIGN = registerBlock("exit_sign", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("exit_sign"))
					.mapColor(MapColor.PALE_GREEN)
					.strength(3, 3)
					.sounds(BlockSoundGroup.CALCITE)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool())
			.needsAttachment()
			.voxelShapes(0, 4, 0, 16, 13, 1));

	public static final Block EXIT_SIGN_2 = registerBlock("exit_sign_2", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("exit_sign_2"))
					.mapColor(MapColor.PALE_GREEN)
					.strength(3, 3)
					.sounds(BlockSoundGroup.CALCITE)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool())
			.needsAttachment()
			.voxelShapes(0, 4, 0, 16, 13, 1));

	public static final Block COMPUTER = registerBlock("computer", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("computer"))
					.mapColor(MapColor.PALE_YELLOW)
					.strength(4, 5)
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.BLOCK)
					.requiresTool())
			.voxelShapes(2, 0, 2, 14, 14, 14));

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

	public static final Block CYAN_TERRACOTTA_STAIRS = register("cyan_terracotta_stairs", StairsBlock::new, AbstractBlock.Settings.copy(Blocks.CYAN_TERRACOTTA), Blocks.CYAN_TERRACOTTA.getDefaultState());

	public static final Block GRAY_ASPHALT = register("gray_asphalt", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(DyeColor.GRAY)
					.strength(2f)
					.requiresTool());

	public static final Block GRAY_ASPHALT_SLAB = register("gray_asphalt_slab", SlabBlock::new, AbstractBlock.Settings.copy(GRAY_ASPHALT));

	public static final Block GRAY_ASPHALT_STAIRS = register("gray_asphalt_stairs", StairsBlock::new,
			AbstractBlock.Settings.copy(GRAY_ASPHALT), GRAY_ASPHALT.getDefaultState());

	public static final Block WET_GRAY_ASPHALT = register("wet_gray_asphalt", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(DyeColor.GRAY)
					.strength(2f)
					.requiresTool()
					.slipperiness(0.87f));

	public static final Block WHITER_CONCRETE = register("whiter_concrete", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.WHITE)
					.strength(3.5f, 6.0f)
					.requiresTool());

	public static final Block WHITE_BRICKS = register("white_bricks", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.WHITE)
					.strength(3.5f, 6.0f)
					.requiresTool());

	public static final Block WHITE_BRICK_SLAB = register("white_brick_slab", SlabBlock::new, AbstractBlock.Settings.copy(WHITE_BRICKS));

	public static final Block CRACKED_WHITE_BRICKS = register("cracked_white_bricks", Block::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.WHITE)
					.strength(2.5f, 5.5f)
					.requiresTool());

	public static final Block FUSED_VERTICAL_LIGHT_TUBE = registerBlock("fused_vertical_light_tube", new RotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("fused_vertical_light_tube"))
					.mapColor(MapColor.STONE_GRAY)
					.strength(1, 2)
					.luminance(createLightLevelFromLitBlockState(6))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.ticksRandomly()
					.requiresTool(), true)
			.voxelShapes(6, 0, 0, 10, 32, 3));

	public static final Block VERTICAL_LIGHT_TUBE = registerBlock("vertical_light_tube", new VariatedRotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("vertical_light_tube"))
					.mapColor(MapColor.WHITE)
					.strength(1, 2)
					.luminance(createLightLevelFromLitBlockState(15))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.requiresTool(), 0.15f, FUSED_VERTICAL_LIGHT_TUBE.getDefaultState())
			.voxelShapes(6, 0, 0, 10, 32, 3));

	public static final Block FUSED_HANGING_FLUORESCENT_LIGHTS = registerBlock("fused_hanging_fluorescent_lights", new RotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("fused_hanging_fluorescent_lights"))
					.mapColor(MapColor.STONE_GRAY)
					.strength(1, 2)
					.luminance(createLightLevelFromLitBlockState(6))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.ticksRandomly(), true)
			.voxelShapes(0, 14.4, 5, 16, 15.9, 11));

	public static final Block HANGING_FLUORESCENT_LIGHTS = registerBlock("hanging_fluorescent_lights", new VariatedRotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("hanging_fluorescent_lights"))
					.mapColor(MapColor.WHITE)
					.strength(1, 2)
					.luminance(createLightLevelFromLitBlockState(15))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque(), 0.15f, FUSED_HANGING_FLUORESCENT_LIGHTS.getDefaultState())
			.voxelShapes(0, 14.4, 5, 16, 15.9, 11));

	public static final Block SMALL_HANGING_PIPE = register("small_hanging_pipe", SmallHangingPipeBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.strength(2.6f)
					.sounds(BlockSoundGroup.METAL)
					.requiresTool()
					.nonOpaque());

	public static final Block RED_SMALL_HANGING_PIPE = register("red_small_hanging_pipe", SmallHangingPipeBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.DULL_RED)
					.strength(2.6f)
					.sounds(BlockSoundGroup.METAL)
					.requiresTool()
					.nonOpaque());

	public static final Block LARGE_HORIZONTAL_PIPE = register("large_horizontal_pipe", LargeHorizontalPipeBlock::new,
			AbstractBlock.Settings.copy(SMALL_HANGING_PIPE));

	public static final Block VERTICAL_PIPE = register("vertical_pipe", VerticalPipeBlock::new,
			AbstractBlock.Settings.copy(SMALL_HANGING_PIPE));

	public static final Block CEILING_PIPE = register("ceiling_pipe", CeilingPipeBlock::new,
			AbstractBlock.Settings.copy(SMALL_HANGING_PIPE));

	public static final Block LETTER_F = registerBlock("letter_f", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("letter_f"))
					.pistonBehavior(PistonBehavior.DESTROY)
					.breakInstantly()
					.sounds(BlockSoundGroup.INTENTIONALLY_EMPTY)
					.nonOpaque()
					.noCollision()
					.noBlockBreakParticles())
			.voxelShapes(0, 0, 0, 16, 16, 0.1)
			.notSolid());

	public static final Block VENTILATION_DUCT = register("ventilation_duct", VentilationDuctBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.strength(3)
					.requiresTool()
					.sounds(BlockSoundGroup.METAL));

	public static final Block VENTILATION_PIPE = register("ventilation_pipe", VentilationPipeBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.strength(3)
					.sounds(BlockSoundGroup.METAL)
					.requiresTool());

	public static final Block FUSED_WALL_LIGHT_BULB = registerBlock("fused_wall_light_bulb", new RotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("fused_wall_light_bulb"))
					.mapColor(MapColor.STONE_GRAY)
					.strength(1, 2)
					.luminance(createLightLevelFromLitBlockState(6))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.ticksRandomly()
					.requiresTool(), true)
			.voxelShapes(3.5, 3.5, 0, 12.5, 12.5, 7.5));

	public static final Block WALL_LIGHT_BULB = registerBlock("wall_light_bulb", new VariatedRotatableLightBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("wall_light_bulb"))
					.mapColor(MapColor.WHITE)
					.strength(1, 2)
					.luminance(createLightLevelFromLitBlockState(15))
					.sounds(BlockSoundGroup.GLASS)
					.nonOpaque()
					.requiresTool(), 0.15f, FUSED_WALL_LIGHT_BULB.getDefaultState())
			.voxelShapes(3.5, 3.5, 0, 12.5, 12.5, 7.5));

	public static final Block CHAIN_BLOCK = register("chain_block", Block::new,
			AbstractBlock.Settings.copy(Blocks.IRON_BARS)
					.registryKey(key("chain_block"))
					.sounds(BlockSoundGroup.CHAIN)
					.requiresTool()
					.nonOpaque());

	public static final Block CHAIN_SLAB = register("chain_slab", TransparentSlabBlock::new, AbstractBlock.Settings.copy(CHAIN_BLOCK));

	public static final Block CHAIN_STAIRS = register("chain_stairs", StairsBlock::new,
			AbstractBlock.Settings.copy(CHAIN_BLOCK), CHAIN_BLOCK.getDefaultState());

	public static final Block CHAIN_WALL = registerBlock("chain_wall", new TransparentBoardBlock(
			AbstractBlock.Settings.copy(CHAIN_BLOCK).registryKey(key("chain_wall")))
			.voxelShapes(2));

	public static final Block RED_CHAIN_WALL = registerBlock("red_chain_wall", new BoardBlock(
			AbstractBlock.Settings.copy(Blocks.IRON_BARS)
					.registryKey(key("red_chain_wall"))
					.mapColor(MapColor.RED)
					.sounds(BlockSoundGroup.CHAIN)
					.requiresTool()
					.nonOpaque())
			.voxelShapes(2));

	public static final Block WOODEN_CRATE = register("wooden_crate", WoodenCrateBlock::new, AbstractBlock.Settings.copy(Blocks.BARREL));

	public static final Block CONTROL_BOX = registerBlock("control_box", new ControlBoxBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("control_box"))
					.mapColor(MapColor.RED)
					.strength(5f, 6f)
					.sounds(BlockSoundGroup.METAL)
					.requiresTool())
			.voxelShapes(2, 0, 0, 14, 16, 8));

	public static final Block CONTROL_CABLE = registerBlock("control_cable", new WallCeilBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("control_cable"))
					.strength(1)
					.sounds(BlockSoundGroup.TUFF)
					.nonOpaque()
					.noCollision()
					.requiresTool())
			.voxelShapes(7, 0, 0, 9, 16, 1));

	public static final Block WHITE_METAL_PANE = registerBlock("white_metal_pane", new BoardBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("white_metal_pane"))
					.mapColor(MapColor.WHITE)
					.strength(5, 6)
					.sounds(BlockSoundGroup.METAL)
					.requiresTool())
			.voxelShapes(2));

	public static final Block GRAY_METAL_PANE = registerBlock("gray_metal_pane", new BoardBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("gray_metal_pane"))
					.mapColor(MapColor.STONE_GRAY)
					.strength(5, 6)
					.sounds(BlockSoundGroup.METAL)
					.requiresTool())
			.voxelShapes(2));

	public static final Block LARGE_HANGING_LAMP = register("large_hanging_lamp", LargeHangingLampBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.WHITE)
					.strength(3f, 2.6f)
					.sounds(BlockSoundGroup.GLASS)
					.luminance(createLightLevelFromLitBlockState(15))
					.nonOpaque()
					.requiresTool());

	public static final Block FUSED_LARGE_HANGING_LAMP = registerBlock("fused_large_hanging_lamp", new LargeHangingLampBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("fused_large_hanging_lamp"))
					.mapColor(MapColor.STONE_GRAY)
					.strength(3f, 2.6f)
					.sounds(BlockSoundGroup.GLASS)
					.luminance(createLightLevelFromLitBlockState(6))
					.nonOpaque()
					.ticksRandomly()
					.requiresTool(), true));

	public static final Block REINFORCED_CONCRETE_BEAM = register("reinforced_concrete_beam", LargeBeamBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LIGHT_GRAY)
					.strength(6)
					.pistonBehavior(PistonBehavior.BLOCK)
					.nonOpaque()
					.requiresTool());

	public static final Block GRAY_STEEL_BEAM = register("gray_steel_beam", LargeBeamBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.GRAY)
					.strength(6)
					.pistonBehavior(PistonBehavior.BLOCK)
					.sounds(BlockSoundGroup.METAL)
					.nonOpaque()
					.requiresTool());

	public static final Block CABLE_TRAY = register("cable_tray", CableTrayBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.GRAY)
					.strength(3)
					.sounds(BlockSoundGroup.LANTERN)
					.nonOpaque()
					.requiresTool());

	public static final Block WOODEN_PLANK = registerBlock("wooden_plank", new BoardBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("wooden_plank"))
					.mapColor(MapColor.BROWN)
					.strength(2, 3)
					.sounds(BlockSoundGroup.WOOD)
					.burnable()
					.nonOpaque())
			.voxelShapes(3), BlockItem::new, new Item.Settings().attributeModifiers(
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

	public static final Block PLYWOOD_SHEET = registerBlock("plywood_sheet", new BoardBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("plywood_sheet"))
					.mapColor(MapColor.BROWN)
					.strength(2, 3)
					.sounds(BlockSoundGroup.WOOD)
					.burnable())
			.voxelShapes(2));

	public static final Block CARDBOARD_SHEET = registerBlock("cardboard_sheet", new BoardBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("cardboard_sheet"))
					.mapColor(MapColor.BROWN)
					.strength(0.2f)
					.sounds(BlockSoundGroup.SNOW)
					.burnable()
					.nonOpaque()
					.requiresTool())
			.needsAttachment()
			.voxelShapes(1));

	public static final Block FIRE_ALARM_BUTTON = registerBlock("fire_alarm_button", new RotatableBlock(
			AbstractBlock.Settings.create()
					.registryKey(key("fire_alarm_button"))
					.mapColor(MapColor.RED)
					.strength(2.6f)
					.sounds(BlockSoundGroup.METAL)
					.nonOpaque()
					.pistonBehavior(PistonBehavior.DESTROY)
					.requiresTool())
			.voxelShapes(3.5, 2.5, 0, 12.5, 16, 3));

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

	public static final Block MOLD_SPROUTS = register("mold_sprouts", MoldSproutsBlock::new,
			AbstractBlock.Settings.create()
					.mapColor(MapColor.LICHEN_GREEN)
					.replaceable()
					.noCollision()
					.breakInstantly()
					.ticksRandomly()
					.offset(AbstractBlock.OffsetType.XZ)
					.sounds(BlockSoundGroup.GLOW_LICHEN)
					.burnable()
					.pistonBehavior(PistonBehavior.DESTROY));

	public static final Block POOL_TILES = register("pool_tiles", Block::new,
			AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE).sounds(BlockSoundGroup.DEEPSLATE_TILES));

	public static final Block POOL_TILE_STAIRS = register("pool_tile_stairs", StairsBlock::new,
			AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE), CubliminalBlocks.POOL_TILES.getDefaultState());

	public static final Block POOL_TILE_SLAB = register("pool_tile_slab", SlabBlock::new, AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE));

	public static final Block POOL_TILE_WALL = register("pool_tile_wall", WallBlock::new, AbstractBlock.Settings.copy(Blocks.REINFORCED_DEEPSLATE).solid());

	public static final Block CRATE_AIR = registerWithoutItem("crate_air", CrateAirBlock::new, AbstractBlock.Settings.copy(Blocks.AIR));
	//endregion

	//region Liquids
	/**
	 * The almond water fluid block will primarily be used in levels that contains oceans of almond water.
	 * This fluid block is currently just for decoration.
	 */
	public static final Block ALMOND_WATER_BLOCK = registerFluidBlock("almond_water", CubliminalFluids.ALMOND_WATER, AlmondWaterFluidBlock::new,
			CustomFluidBlock.Settings.create()
					.setColor(0xFFECB3)
					.setSplashParticles(CustomFluidBlock.FluidSplashParticleManager.create()
							.setParticles(CubliminalParticleTypes.LANDING_ALMOND_WATER, CubliminalParticleTypes.ALMOND_WATER_BUBBLE)
					)
	);

	/**
	 * Contaminated water will primarily be used in level 2, also known as pipe dreams.
	 * This fluid gives negative effects to entities.
	 */
	public static final Block CONTAMINATED_WATER_BLOCK = registerFluidBlock("contaminated_water", CubliminalFluids.CONTAMINATED_WATER, ContaminatedWaterBlock::new,
			CustomFluidBlock.Settings.create()
					.setColor(0x556B2F)
					.setFogEnd(10.0f)
					.setFogAlpha(0.8f)
					.setSplashParticles(
							CustomFluidBlock.FluidSplashParticleManager.create()
									.setParticles(CubliminalParticleTypes.CONTAMINATED_WATER_SPLASH, CubliminalParticleTypes.CONTAMINATED_WATER_BUBBLE)
					)
	);

	/**
	 * This fluid block will be on levels such as level 2, as well as any other levels that contains black sludge.
	 * Black sludge is a thick fluid that is very harmful to an entity sanity.
 	 */
	public static final Block BLACK_SLUDGE_BLOCK = registerFluidBlock("black_sludge", CubliminalFluids.BLACK_SLUDGE, BlackSludgeFluidBlock::new,
			CustomFluidBlock.Settings.create()
					.setColor(0x1C1F1C)
					.setSpeed(0.005f)
					.setDrag(new Vec3d(0.5, 0.4, 0.5))
					.setFogStart(0.25F)
					.setFogEnd(1.0F)
					.setFogAlpha(1.0F)
	);
	//endregion

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
			builder.add(WOODEN_PLANK.asItem(), 100);
		});
    }
}
