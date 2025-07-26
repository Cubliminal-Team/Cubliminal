package net.limit.cubliminal;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.config.CubliminalConfig;
import net.limit.cubliminal.event.command.NoclipCommand;
import net.limit.cubliminal.event.command.SanityCommand;
import net.limit.cubliminal.init.*;
import net.limit.cubliminal.event.noclip.NoclipDestination;
import net.limit.cubliminal.particle.CubliminalParticleTypes;
import net.limit.cubliminal.world.connection.ConnectionPlacementType;
import net.limit.cubliminal.world.connection.ConnectionRegistry;
import net.limit.cubliminal.world.room.RoomRegistry;
import net.limit.cubliminal.world.room.RoomType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.ResourceType;
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

    public static final Logger LOGGER = LoggerFactory.getLogger("Cubliminal");

	public static ServerWorld SERVER;

	public static final RegistryKey<DamageType> MENTAL_COLLAPSE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("mental_collapse"));

	public static final Identifier BURIED_TREASURE_ID = Identifier.ofVanilla("chests/buried_treasure");

	@Override
	public void onInitialize() {
		AutoConfig.register(CubliminalConfig.class, GsonConfigSerializer::new);

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new RoomRegistry());
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ConnectionRegistry());

		// Init Initers
		CubliminalParticleTypes.init();
		CubliminalFluids.initFluid();
		Initer.initialise();

		NoclipDestination.init();
		ConnectionPlacementType.init();
		RoomType.init();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> SERVER = server.getWorld(CubliminalRegistrar.THE_LOBBY_KEY));
		ServerPlayerEvents.AFTER_RESPAWN.register(Cubliminal::afterDeath);
		CommandRegistrationCallback.EVENT.register(NoclipCommand::register);
		CommandRegistrationCallback.EVENT.register(SanityCommand::register);

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

	public static void afterDeath(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
		((PEAccessor) newPlayer).getSanityManager().resetTimer();
		int ticksToNc = ((PEAccessor) oldPlayer).getNoclipEngine().getTicksToNc();
		((PEAccessor) newPlayer).getNoclipEngine().setTicksToNc(ticksToNc);
	}
}