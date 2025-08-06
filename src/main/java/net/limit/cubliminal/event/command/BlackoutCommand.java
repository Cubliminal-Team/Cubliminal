package net.limit.cubliminal.event.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.limit.cubliminal.access.ServerWorldAccessor;
import net.limit.cubliminal.event.backrooms.BlackoutManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;

import java.util.Optional;

public class BlackoutCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("blackout").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("toggle")
                        .then(CommandManager.argument("lightsOff", BoolArgumentType.bool())
                                .executes(context -> execute(
                                        context.getSource(), BoolArgumentType.getBool(context, "lightsOff")
                                ))))
                .then(CommandManager.literal("set")
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(1))
                                .then(CommandManager.argument("lightsOff", BoolArgumentType.bool())
                                        .executes(context -> execute(
                                                context.getSource(), IntegerArgumentType.getInteger(context, "ticks"), BoolArgumentType.getBool(context, "lightsOff")
                                        )))))
        );
    }

    private static int execute(ServerCommandSource source, boolean lightsOff) {
        ServerWorld world = source.getWorld();
        BlackoutManager blackoutManager = ((ServerWorldAccessor) world).blackoutManager();
        if (blackoutManager != null) {
            BlockPos pos = BlockPos.ofFloored(source.getPosition());
            RegistryKey<Biome> biome = world.getGeneratorStoredBiome(
                    BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(pos.getY()), BiomeCoords.fromBlock(pos.getZ())
            ).getKey().orElseThrow();
            Optional<BlackoutManager.Entry> optional = blackoutManager.forBiome(biome);
            if (optional.isPresent()) {
                blackoutManager.toggleState(optional.get(), lightsOff);
                source.sendFeedback(() -> Text.translatable("commands.blackout.success.toggle", lightsOff), true);
                return 1;
            }

            source.sendError(Text.stringifiedTranslatable("commands.blackout.failed.invalid_biome", biome.getValue()));
        } else {
            source.sendError(Text.translatable("commands.blackout.failed.invalid_world"));
        }

        return 0;
    }

    private static int execute(ServerCommandSource source, int ticks, boolean lightsOff) {
        ServerWorld world = source.getWorld();
        BlackoutManager blackoutManager = ((ServerWorldAccessor) world).blackoutManager();
        if (blackoutManager != null) {
            BlockPos pos = BlockPos.ofFloored(source.getPosition());
            RegistryKey<Biome> biome = world.getGeneratorStoredBiome(
                    BiomeCoords.fromBlock(pos.getX()), BiomeCoords.fromBlock(pos.getY()), BiomeCoords.fromBlock(pos.getZ())
            ).getKey().orElseThrow();
            Optional<BlackoutManager.Entry> optional = blackoutManager.forBiome(biome);
            if (optional.isPresent()) {
                blackoutManager.toggleState(optional.get(), ticks, lightsOff);
                blackoutManager.markDirty();
                source.sendFeedback(() -> Text.translatable("commands.blackout.success.set", lightsOff, ticks), true);
                return 1;
            }

            source.sendError(Text.stringifiedTranslatable("commands.blackout.failed.invalid_biome", biome.getValue()));
        } else {
            source.sendError(Text.translatable("commands.blackout.failed.invalid_world"));
        }

        return 0;
    }
}
