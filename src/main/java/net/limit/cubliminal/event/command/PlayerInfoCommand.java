package net.limit.cubliminal.event.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.limit.cubliminal.event.backrooms.skindatabase.PlayerDataManager;
import net.limit.cubliminal.event.backrooms.skindatabase.PlayerInfoManager;
import net.limit.cubliminal.event.backrooms.skindatabase.PlayerSkinData;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerInfoCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("playerinfo").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("skin")
                    .then(CommandManager.literal("remove")
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(context -> executeSkinRemove(context.getSource(), EntityArgumentType.getPlayer(context, "player").getUuid()))
                            )
                            .then(CommandManager.argument("uuid", UuidArgumentType.uuid())
                                    .suggests(new PlayerSkinDataUUIDSuggestionProvider())
                                    .executes(context -> executeSkinRemove(context.getSource(), UuidArgumentType.getUuid(context, "uuid")))
                            )
                    )
                    .then(CommandManager.literal("set").then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(context -> executeSkinSet(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                    ))
                    .then(CommandManager.literal("get")
                            .executes(context -> executeSkinGetAll(context.getSource()))
                            .then(CommandManager.argument("player", EntityArgumentType.player())
                                    .executes(context -> executeSkinGet(context.getSource(), EntityArgumentType.getPlayer(context, "player").getUuid()))
                            )
                            .then(CommandManager.argument("uuid", UuidArgumentType.uuid())
                                    .suggests(new PlayerSkinDataUUIDSuggestionProvider())
                                    .executes(context -> executeSkinGet(context.getSource(), UuidArgumentType.getUuid(context, "uuid")))
                            )
                    )
                )
        );
    }

    private static int executeSkinRemove(ServerCommandSource source, UUID uuid) {
        PlayerInfoManager.getInstance().getSkins().deletePlayerData(uuid);
        source.sendFeedback(() -> Text.literal("Deleted player data"), false);
        return 1;
    }

    private static int executeSkinSet(ServerCommandSource source, ServerPlayerEntity player) {
        PlayerInfoManager.getInstance().getSkins().storePlayerData(PlayerSkinData.createFromPlayer(player));
        source.sendFeedback(() -> Text.literal("Stored player data"), false);
        return 1;
    }

    private static int executeSkinGet(ServerCommandSource source, UUID uuid) {
        PlayerDataManager<PlayerSkinData> skinsData = PlayerInfoManager.getInstance().getSkins();

        Optional<PlayerSkinData> playerSkinData = skinsData.getPlayerData(uuid);

        if (playerSkinData.isPresent()) {
            source.sendFeedback(() -> Text.literal(playerSkinData.get().toString().replace(" | ", "\n")), false);

        } else {
            source.sendError(Text.literal("There is no information stored on the player"));
        }
        return 1;
    }

    private static int executeSkinGetAll(ServerCommandSource source) {
        PlayerDataManager<PlayerSkinData> skinsData = PlayerInfoManager.getInstance().getSkins();

        source.sendFeedback(() -> Text.literal(String.valueOf(skinsData.getEntryCount())), false);

        return 1;
    }

    private static class PlayerSkinDataUUIDSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            PlayerDataManager<PlayerSkinData> skinsData = PlayerInfoManager.getInstance().getSkins();

            for (PlayerSkinData playerSkinData : skinsData.getAllData()) {
                builder.suggest(playerSkinData.getUuid().toString());
            }

            return builder.buildFuture();
        }
    }
}
