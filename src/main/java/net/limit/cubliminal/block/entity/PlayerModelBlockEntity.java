package net.limit.cubliminal.block.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class PlayerModelBlockEntity extends BlockEntity {
    private GameProfile profile;

    public PlayerModelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setProfile(GameProfile profile){
        this.profile = profile;
        markDirty();
    }

    public GameProfile getProfile(){
        return this.profile;
    }
}
