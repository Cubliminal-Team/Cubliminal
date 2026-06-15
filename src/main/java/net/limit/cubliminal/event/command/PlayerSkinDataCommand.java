package net.limit.cubliminal.event.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.limit.cubliminal.event.backrooms.PlayerSkinDataManager;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerSkinDataCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("playerskindata").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> executeRemove(context.getSource(), context.getSource().getServer(), EntityArgumentType.getPlayer(context, "player").getUuid()))
                        )
                        .then(CommandManager.argument("uuid", UuidArgumentType.uuid())
                                .suggests(new PlayerSkinDataUUIDSuggestionProvider())
                                .executes(context -> executeRemove(context.getSource(), context.getSource().getServer(), UuidArgumentType.getUuid(context, "uuid")))
                        )
                )
                .then(CommandManager.literal("set").then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> executeSet(context.getSource(), context.getSource().getServer(), EntityArgumentType.getPlayer(context, "player")))
                ))
                .then(CommandManager.literal("get")
                        .executes(context -> executeGetAll(context.getSource(), context.getSource().getServer()))
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> executeGet(context.getSource(), context.getSource().getServer(), EntityArgumentType.getPlayer(context, "player").getUuid()))
                        )
                        .then(CommandManager.argument("uuid", UuidArgumentType.uuid())
                                .suggests(new PlayerSkinDataUUIDSuggestionProvider())
                                .executes(context -> executeGet(context.getSource(), context.getSource().getServer(), UuidArgumentType.getUuid(context, "uuid")))
                        )
                )
        );
    }

    private static int executeRemove(ServerCommandSource source, MinecraftServer server, UUID uuid) {
        PlayerSkinDataManager.getInstance(server).deletePlayerData(uuid);
        source.sendFeedback(() -> Text.literal("Deleted player data"), false);
        return 1;
    }

    private static int executeSet(ServerCommandSource source, MinecraftServer server, ServerPlayerEntity player) {
        PlayerSkinDataManager.getInstance(server).storePlayerData(PlayerSkinDataManager.createFromPlayer(player));
        source.sendFeedback(() -> Text.literal("Stored player data"), false);
        return 1;
    }

    private static int executeGet(ServerCommandSource source, MinecraftServer server, UUID uuid) {
        PlayerSkinDataManager playerSkinDataManager = PlayerSkinDataManager.getInstance(server);

        Optional<PlayerSkinDataManager.PlayerSkinData> playerSkinData = playerSkinDataManager.getPlayerData(uuid);

        if (playerSkinData.isPresent()) {
            source.sendFeedback(() -> Text.literal(playerSkinData.get().toString().replace(" | ", "\n")), false);

        } else {
            source.sendError(Text.literal("There is no information stored on the player"));
        }
        return 1;
    }

    private static int executeGetAll(ServerCommandSource source, MinecraftServer server) {
        PlayerSkinDataManager playerSkinDataManager = PlayerSkinDataManager.getInstance(server);

        source.sendFeedback(() -> Text.literal(String.valueOf(playerSkinDataManager.getEntryCount())), false);

        return 1;
    }

    private static class PlayerSkinDataUUIDSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            PlayerSkinDataManager playerSkinDataManager = PlayerSkinDataManager.getInstance(context.getSource().getServer());

            for (PlayerSkinDataManager.PlayerSkinData playerSkinData : playerSkinDataManager.getAllData()) {
                builder.suggest(playerSkinData.getUuid().toString());
            }

            return builder.buildFuture();
        }
    }
}
