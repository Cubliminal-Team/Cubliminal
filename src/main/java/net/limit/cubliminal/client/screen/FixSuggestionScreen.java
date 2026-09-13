package net.limit.cubliminal.client.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class FixSuggestionScreen extends Screen {

    private static final int NOTICE_TEXT_Y = 105;

    private final List<Text> fixes;
    private final List<MultilineText> wrappedFixLines = new ArrayList<>();
    private final Runnable actionHandler;
    private final Text buttonText;
    private final boolean shouldCloseOnEsc;
    private int contentX = 40;

    public FixSuggestionScreen(Runnable actionHandler, Text title, List<Text> fixes) {
        this(actionHandler, title, fixes, ScreenTexts.BACK, true);
    }

    public FixSuggestionScreen(Runnable actionHandler, Text title, List<Text> fixes, Text buttonText, boolean shouldCloseOnEsc) {
        super(title);
        this.actionHandler = actionHandler;
        this.fixes = fixes;
        this.buttonText = buttonText;
        this.shouldCloseOnEsc = shouldCloseOnEsc;
    }

    @Override
    public Text getNarratedTitle() {
        return ScreenTexts.joinSentences(super.getNarratedTitle(), ScreenTexts.joinLines(this.fixes));
    }

    @Override
    protected void init() {
        super.init();
        this.wrappedFixLines.clear();

        int titleWidth = this.textRenderer.getWidth(this.title);
        this.contentX = Math.max(10, (this.width - titleWidth) / 2);

        int maxTextWidth = (this.width - this.contentX) - 20;
        int totalLines = 0;

        for (Text fix : this.fixes) {
            MultilineText multiline = MultilineText.create(this.textRenderer, fix, maxTextWidth - 12);
            this.wrappedFixLines.add(multiline);
            totalLines += multiline.count();
        }

        int totalTextHeight = 15 + (totalLines * 9) + (this.fixes.size() * 4);
        int buttonY = MathHelper.clamp(NOTICE_TEXT_Y + totalTextHeight + 12, this.height / 6 + 96, this.height - 24);

        this.addDrawableChild(ButtonWidget.builder(this.buttonText, (button) -> this.actionHandler.run())
                .dimensions((this.width - 150) / 2, buttonY, 150, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 70, 0xFF5555);
        context.drawTextWithShadow(this.textRenderer, Text.literal("Fixes:"), this.contentX, 90, 0xAAAAAA);

        int currentY = NOTICE_TEXT_Y;
        for (MultilineText multilineText : this.wrappedFixLines) {
            context.drawTextWithShadow(this.textRenderer, "-", this.contentX, currentY, 0xFFFFFF);
            multilineText.drawWithShadow(context, this.contentX + 12, currentY, 9, 0xFFFFFF);
            currentY += (multilineText.count() * 9) + 4;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.shouldCloseOnEsc;
    }
}