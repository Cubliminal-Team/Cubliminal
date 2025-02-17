package net.limit.cubliminal;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.config.CubliminalConfig;
import net.limit.cubliminal.event.ServerTickHandler;
import net.limit.cubliminal.event.command.NoClipCommand;
import net.limit.cubliminal.event.command.SanityCommand;
import net.limit.cubliminal.init.*;
import net.limit.cubliminal.util.IEntityDataSaver;
import net.limit.cubliminal.util.NoClipEngine;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cubliminal implements ModInitializer {
	public static final String MOD_ID = "cubliminal";

	public static Identifier id(String id) {
		return Identifier.of(MOD_ID, id);
	}

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ServerWorld SERVER;

	public static final RegistryKey<DamageType> MENTAL_COLLAPSE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("mental_collapse"));

	public static final Identifier BURIED_TREASURE_ID = Identifier.ofVanilla("chests/buried_treasure");

	@Override
	public void onInitialize() {
		AutoConfig.register(CubliminalConfig.class, GsonConfigSerializer::new);

		CubliminalItemGroups.init();
		CubliminalItems.init();
		CubliminalBlocks.init();
		CubliminalBiomes.init();
		CubliminalSounds.init();
		CubliminalEntities.init();
		CubliminalEffects.init();
		CubliminalBlockEntities.init();

		PayloadTypeRegistry.playC2S().register(CubliminalPackets.NoClipC2SPayload.ID, CubliminalPackets.NoClipC2SPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CubliminalPackets.NoClipSyncPayload.ID, CubliminalPackets.NoClipSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CubliminalPackets.SanitySyncPayload.ID, CubliminalPackets.SanitySyncPayload.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(CubliminalPackets.NoClipC2SPayload.ID, (payload, context) -> {
			ServerPlayerEntity player = context.player();
			NbtCompound nbt = IEntityDataSaver.cast(player);
			if (NoClipEngine.canNoCLip(player)) {
				if (payload.reset()) nbt.putInt("ticksToNc", 0);
				else NoClipEngine.noClip(player);
			}
		});

		ServerTickEvents.START_SERVER_TICK.register(new ServerTickHandler());
		ServerPlayerEvents.AFTER_RESPAWN.register(ServerTickHandler::onAfterDeath);
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(ServerTickHandler::afterWorldChange);
		CommandRegistrationCallback.EVENT.register(NoClipCommand::register);
		CommandRegistrationCallback.EVENT.register(SanityCommand::register);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server.getWorld(CubliminalRegistrar.THE_LOBBY_KEY));

		LootTableEvents.MODIFY.register(((key, tableBuilder, source, registries) -> {
			if (source.isBuiltin() && key.getValue().equals(BURIED_TREASURE_ID)) {
				LootPool.Builder builder = LootPool.builder()
						.rolls(ConstantLootNumberProvider.create(1f))
						.with(ItemEntry.builder(CubliminalItems.SILVER_INGOT))
						.apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2f, 4f)).build());

				tableBuilder.pool(builder.build());
			}
		}));
	}
}