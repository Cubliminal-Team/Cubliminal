package net.limit.cubliminal.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.limit.cubliminal.access.ServerWorldAccessor;
import net.limit.cubliminal.block.custom.template.BlackoutListener;
import net.limit.cubliminal.event.backrooms.BlackoutManager;
import net.limit.cubliminal.init.CubliminalRegistrar;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements ServerWorldAccessor {

    @Shadow
    public abstract PersistentStateManager getPersistentStateManager();

    @Unique
    private BlackoutManager blackoutManager;

    @Nullable
    @Override
    public BlackoutManager blackoutManager() {
        return blackoutManager;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session,
                        ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions,
                        WorldGenerationProgressListener worldGenerationProgressListener, boolean debugWorld,
                        long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, RandomSequencesState randomSequencesState,
                        CallbackInfo ci) {
        if (worldKey.equals(CubliminalRegistrar.HABITABLE_ZONE_KEY)) {
            this.blackoutManager = this.getPersistentStateManager().getOrCreate(BlackoutManager.getPersistentStateType((ServerWorld)(Object)this), "blackouts");
        }
    }

    @SuppressWarnings("all")
    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;getSectionArray()[Lnet/minecraft/world/chunk/ChunkSection;", shift = At.Shift.AFTER))
    private void onRandomTick(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci, @Local Profiler profiler) {
        if (this.blackoutManager != null && this.blackoutManager.shouldTickChunk()) {
            profiler.push("blackoutTick");

            ChunkPos chunkPos = chunk.getPos();
            int startX = chunkPos.getStartX();
            int startZ = chunkPos.getStartZ();
            BlockPos.Mutable mutablePos = new BlockPos.Mutable();
            ServerWorld world = (ServerWorld) (Object) this;
            ChunkSection[] chunkSections = chunk.getSectionArray();
            List<Integer> attachedSections = new ArrayList<>(chunk.getAttachedOrElse(BlackoutManager.ATTACHMENT, List.of()));

            for (int l = 0; l < chunkSections.length; l++) {
                ChunkSection chunkSection = chunkSections[l];

                if (chunkSection.hasRandomTicks()) {
                    int sectionY = ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(l));
                    boolean lightsOff = this.blackoutManager.lightsOffInChunk(chunkPos, sectionY);
                    if (attachedSections.contains(l) != lightsOff) {
                        this.blackoutManager.onChunkTick();
                        this.blackoutManager.playSound(chunkPos, sectionY + 7, lightsOff);
                        if (lightsOff) {
                            attachedSections.add(l);
                        } else {
                            attachedSections.remove((Integer) l);
                        }

                        for (int dx = 0; dx < 16; dx++) {
                            for (int dy = 0; dy < 16; dy++) {
                                for (int dz = 0; dz < 16; dz++) {
                                    mutablePos.set(startX + dx, sectionY + dy, startZ + dz);
                                    BlockState blockState = chunk.getBlockState(mutablePos);
                                    if (blockState.getBlock() instanceof BlackoutListener listener) {
                                        listener.blackoutUpdate(blockState, world, mutablePos, lightsOff, world.getRandom());
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!this.blackoutManager.shouldTickChunk()) {
                chunk.setAttached(BlackoutManager.ATTACHMENT, attachedSections);
            }

            profiler.pop();
        }
    }
}