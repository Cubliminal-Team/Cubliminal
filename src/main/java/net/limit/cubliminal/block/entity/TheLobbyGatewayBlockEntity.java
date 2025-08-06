package net.limit.cubliminal.block.entity;

import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.access.PEAccessor;
import net.limit.cubliminal.advancements.AdvancementHelper;
import net.limit.cubliminal.block.custom.TheLobbyGatewayBlock;
import net.limit.cubliminal.init.CubliminalBlockEntities;
import net.limit.cubliminal.init.CubliminalBlocks;
import net.limit.cubliminal.level.Levels;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TheLobbyGatewayBlockEntity extends BlockEntity {

	public TheLobbyGatewayBlockEntity(BlockPos pos, BlockState state) {
		super(CubliminalBlockEntities.THE_LOBBY_GATEWAY_BLOCK_ENTITY, pos, state);
	}

	private long age = 0L;
	private BlockPos exitPos;

	public void writeExitPos(BlockPos blockPos) {
		this.exitPos = blockPos;
	}

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putLong("Age", this.age);
		if (this.exitPos != null) {
			nbt.put("exitPos", NbtHelper.fromBlockPos(this.exitPos));
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		this.age = nbt.getLong("Age");
		Optional<BlockPos> blockPos = NbtHelper.toBlockPos(nbt, "exitPos");
		if (blockPos.isPresent() && World.isValid(blockPos.get())) {
			this.exitPos = blockPos.get();
		}
	}

	public BlockPos getExitPos() {
		return this.exitPos == null ? Levels.MANILA_ROOM : this.exitPos;
	}

	public static void tick(World world, BlockPos pos, BlockState state, TheLobbyGatewayBlockEntity blockEntity) {
		if (!world.isClient) {
			++blockEntity.age;
			if (!world.getBlockState(pos.down()).isOf(CubliminalBlocks.THE_LOBBY_GATEWAY_BLOCK)) {
				List<Entity> list = world.getEntitiesByClass(Entity.class, new Box(pos), TheLobbyGatewayBlockEntity::canTeleport);
				if (!list.isEmpty()) {
					tryTeleportingEntity(world, pos, state, list.get(world.random.nextInt(list.size())), blockEntity);
				}
				if (blockEntity.age % 100 == 0 && !state.get(Properties.LIT)) {
					for (Entity entity : world.getEntitiesByClass(Entity.class, new Box(pos).expand(16, 2, 11), Entity::isPlayer)) {
						((PEAccessor) entity).getSanityManager().resetTimer();
					}
				}
			}
		}
	}

	public static void tryTeleportingEntity(World world, BlockPos pos, BlockState state, Entity entity, TheLobbyGatewayBlockEntity blockEntity) {
		if (world instanceof ServerWorld serverWorld) {
			Entity finalEntity;
			if (entity instanceof EnderPearlEntity) {
				Entity pearlOwner = ((EnderPearlEntity) entity).getOwner();
				if (pearlOwner instanceof ServerPlayerEntity) {
					Criteria.ENTER_BLOCK.trigger((ServerPlayerEntity) pearlOwner, state);
				}

				if (pearlOwner != null) {
					finalEntity = pearlOwner;
					entity.discard();
				} else {
					finalEntity = entity;
				}
			} else {
				finalEntity = entity.getRootVehicle();
			}

			finalEntity.resetPortalCooldown();
			TeleportTarget teleportTarget = new TeleportTarget(
					serverWorld, Vec3d.ofBottomCenter(blockEntity.getExitPos()), Vec3d.ZERO,
					finalEntity.getYaw(), finalEntity.getPitch(), teleported -> {
						if (teleported instanceof PlayerEntity player) {
							AdvancementHelper.grantAdvancement(player, Cubliminal.id("backrooms/manila_room"));
						}
			});
			finalEntity.teleportTo(teleportTarget);

			if (state.get(TheLobbyGatewayBlock.LIT) && blockEntity.getWorld() != null) {
				BlockPos.stream(new Box(blockEntity.getExitPos()).expand(10))
						.map(blockEntity.getWorld()::getBlockEntity)
						.filter(be -> be instanceof TheLobbyGatewayBlockEntity && !be.getCachedState().get(Properties.LIT))
						.forEach(other -> ((TheLobbyGatewayBlockEntity) other).writeExitPos(pos.add(3, 0, 0)));
			}
		}
	}

	@Override
	public boolean onSyncedBlockEvent(int type, int data) {
		if (type == 1) {
			return true;
		} else return super.onSyncedBlockEvent(type, data);
	}

	public static boolean canTeleport(Entity entity) {
		return EntityPredicates.EXCEPT_SPECTATOR.test(entity) && !entity.getRootVehicle().hasPortalCooldown();
	}

	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
		return this.createNbt(registryLookup);
	}

	public boolean shouldDrawSide(Direction direction) {
		return Block.shouldDrawSide(this.getCachedState(), this.world.getBlockState(this.getPos().offset(direction)), direction);
	}
}
