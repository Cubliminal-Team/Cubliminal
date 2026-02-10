package net.limit.cubliminal.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.limit.cubliminal.block.state.DocumentMode;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.limit.cubliminal.networking.s2c.WrittenDocScreenPayload;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.StringHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WrittenDocumentItem extends BlockItem {

    public WrittenDocumentItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (type.isAdvanced()) {
            WrittenDocContentComponent component = stack.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
            if (component != null && component.mode() == DocumentMode.IMAGE) {
                component.texture().ifPresent(texture -> {
                    if (!StringHelper.isBlank(texture.getPath())) {
                        tooltip.add(Text.literal(texture.getPath()).formatted(Formatting.AQUA));
                    }
                });
            }
        }
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack doc = user.getStackInHand(hand);
        if (user instanceof ServerPlayerEntity player) {
            if (resolve(doc, player.getCommandSource(), user)) {
                player.currentScreenHandler.sendContentUpdates();
            }

            ServerPlayNetworking.send(player, new WrittenDocScreenPayload(hand));
        }

        return ActionResult.SUCCESS;
    }

    public static boolean resolve(ItemStack doc, ServerCommandSource commandSource, @Nullable PlayerEntity player) {
        WrittenDocContentComponent component = doc.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
        if (component != null && !component.resolved()) {
            WrittenDocContentComponent resolvedComponent = component.resolve(commandSource, player);
            if (resolvedComponent != null) {
                doc.set(CubliminalDataComponents.WRITTEN_DOC_COMPONENT, resolvedComponent);
                return true;
            }

            doc.set(CubliminalDataComponents.WRITTEN_DOC_COMPONENT, component.asResolved());
        }

        return false;
    }
}
