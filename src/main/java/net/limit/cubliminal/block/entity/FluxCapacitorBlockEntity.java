package net.limit.cubliminal.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.access.GameRendererAccessor;
import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.block.custom.FluxCapacitorBlock;
import net.limit.cubliminal.client.sound.ClientSoundHelper;
import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.function.Predicate;

public class FluxCapacitorBlockEntity extends BlockEntity {

	public FluxCapacitorBlockEntity(BlockPos pos, BlockState state) {
		super(CubliminalBlockEntities.FLUX_CAPACITOR_BLOCK_ENTITY, pos, state);
	}

	private boolean canBreakReality;
	private int realityTicks;

	@Environment(EnvType.CLIENT)
    public Object soundInstance;

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putInt("RealityTicks", this.realityTicks);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		this.realityTicks = nbt.getInt("RealityTicks");
	}


	public static void tick(World world, BlockPos pos, BlockState state, FluxCapacitorBlockEntity entity) {
		if (state.get(FluxCapacitorBlock.POWERED) && !entity.canBreakReality) {
			entity.canBreakReality = true;
			if (world.isClient()) {
				ClientSoundHelper.playFluxCapacitor(entity);
			}
		}
		if (entity.canBreakReality) {
			entity.breakReality(world, state);
		}
	}

	@Override
	public void markRemoved() {
		if (this.world != null && this.world.isClient() && this.getCachedState().get(FluxCapacitorBlock.POWERED)) {
			if (this.soundInstance != null) ClientSoundHelper.stopFluxCapacitor(this.soundInstance);
			disableNoclip();
		}
		super.markRemoved();
	}

	@Override
	public boolean onSyncedBlockEvent(int type, int data) {
		if (type == 1) {
			return true;
		} else return super.onSyncedBlockEvent(type, data);
	}

	public void breakReality(World world, BlockState state) {
		if (this.realityTicks > 279) {
			world.setBlockState(this.pos, state.with(FluxCapacitorBlock.POWERED, false));
			this.canBreakReality = false;
			this.realityTicks = 0;
		} else {
			++this.realityTicks;
			if (this.realityTicks >= 220) {
				if (this.realityTicks == 220) {
					if (world.isClient()) {
						disableNoclip();
					} else {
						world.getPlayers()
								.stream()
								.filter(Predicate.not(PlayerEntity::isSpectator))
								.forEach(player -> ((PEAccessor) player).getNoclipEngine().noclip(player));
					}
				}
			} else if (world.isClient() && this.realityTicks >= 100) {
				triggerNoclip();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static void triggerNoclip() {
		if (!MinecraftClient.getInstance().player.isSpectator()) {
			((GameRendererAccessor) MinecraftClient.getInstance().gameRenderer).setTriggered(true);
		}
	}

	@Environment(EnvType.CLIENT)
	public static void disableNoclip() {
		((GameRendererAccessor) MinecraftClient.getInstance().gameRenderer).setTriggered(false);
	}

	public int getRealityTicks() {
		return this.realityTicks;
	}

	public boolean canBreakReality() {
		return this.canBreakReality;
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createNbt(registryLookup);
	}
}