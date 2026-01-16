package net.limit.cubliminal.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.limit.cubliminal.block.state.DocumentMode;
import net.limit.cubliminal.init.CubliminalDataComponents;
import net.limit.cubliminal.item.component.WrittenDocContentComponent;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public class DocBlockScreen extends DocScreen implements ScreenHandlerProvider<DocScreenHandler> {

    private final DocScreenHandler handler;

    private DocBlockScreen(Text title, DocScreenHandler handler, DocumentMode mode, Contents contents) {
        super(title, mode, contents);
        this.handler = handler;
    }

    public static DocBlockScreen make(DocScreenHandler handler, PlayerInventory inventory, Text title) {
        ItemStack stack = handler.getDocItem();
        WrittenDocContentComponent component = stack.get(CubliminalDataComponents.WRITTEN_DOC_COMPONENT);
        return new DocBlockScreen(title, handler, component == null ? null : component.mode(), Contents.create(stack));
    }

    @Override
    public DocScreenHandler getScreenHandler() {
        return this.handler;
    }

    @Override
    public void close() {
        this.client.player.closeHandledScreen();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    protected void closeScreen() {
        this.client.player.closeHandledScreen();
    }
}
