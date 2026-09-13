package net.limit.cubliminal.client.screen.roomcreator;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.Cubliminal;
import net.limit.cubliminal.client.screen.FixSuggestionScreen;
import net.limit.cubliminal.client.screen.roomcreator.data.*;
import net.limit.cubliminal.client.screen.roomcreator.widget.RoomPreviewWidget;
import net.limit.cubliminal.client.screen.roomcreator.widget.TemplateSuggestorWidget;
import net.limit.cubliminal.init.CubliminalStructures;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.level.Levels;
import net.limit.cubliminal.networking.c2s.SaveSelectionC2SPayload;
import net.limit.cubliminal.world.room.PaddingType;
import net.limit.cubliminal.world.room.Room;
import net.limit.cubliminal.world.room.RoomType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.NoticeScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.tab.TabManager;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.*;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class RoomCreatorScreen extends Screen {
    private static final Text ROOM_CREATOR_SCREEN = Text.translatable("gui.cubliminal.room_creator_screen");
    private static final Text CREATE_SELECTION_TEXT = Text.translatable("gui.cubliminal.create_selection");
    private static final Text TEMPLATES_TEXT = Text.translatable("gui.cubliminal.templates");
    private static final Text ROOM_BUILDER_TEXT = Text.translatable("gui.cubliminal.room_builder");
    private static final Text REFRESH_TEXT = Text.translatable("gui.cubliminal.refresh");
    private static final Text CLOSE_TEXT = Text.translatable("gui.cubliminal.close");
    private static final Text STRUCTURE_TOO_TALL_TITLE = Text.translatable("gui.cubliminal.title.too_tall_warning").formatted(Formatting.BOLD, Formatting.RED);
    private static final Function<Integer, Text> STRUCTURE_TOO_TALL = height -> Text.translatable("gui.cubliminal.too_tall_warning", height, height);

    private static final Identifier TAB_HEADER_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/tab_header_background.png");

    private final Screen parent;

    private final RoomCreatorDataManager dataManager = RoomCreatorDataManager.INSTANCE;
    private TabNavigationWidget tabNavigation;
    private final TabManager tabManager = new TabManager(this::addDrawableChild, this::remove);
    private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
    private SelectionTab selectionTab;
    private TemplatesTab templatesTab;
    private RoomBuilderTab roomBuilderTab;

    public RoomCreatorScreen(Screen parent) {
        super(ROOM_CREATOR_SCREEN);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.selectionTab = new SelectionTab();
        this.templatesTab = new TemplatesTab();
        this.roomBuilderTab = new RoomBuilderTab();
        this.tabNavigation = TabNavigationWidget
                .builder(this.tabManager, this.width)
                .tabs(this.selectionTab, this.templatesTab, this.roomBuilderTab)
                .build();
        this.addDrawableChild(this.tabNavigation);

        DirectionalLayoutWidget footerLayout = this.layout.addFooter(DirectionalLayoutWidget.horizontal().spacing(8));

        ButtonWidget refreshButton = ButtonWidget.builder(REFRESH_TEXT, button -> this.refresh()).width(120).build();
        footerLayout.add(refreshButton);

        footerLayout.add(ButtonWidget.builder(CLOSE_TEXT, button -> this.close()).width(120).build());

        this.layout.forEachChild(child -> {
            child.setNavigationOrder(1);
            this.addDrawableChild(child);
        });

        this.tabNavigation.selectTab(0, true);
        this.refreshWidgetPositions();
    }

    private void refresh() {
        this.selectionTab.refresh();
        this.templatesTab.refresh();
    }

    @Override
    protected void refreshWidgetPositions() {
        if (this.tabNavigation != null) {
            this.tabNavigation.setWidth(this.width);
            this.tabNavigation.init();
            int headerBottom = this.tabNavigation.getNavigationFocus().getBottom();

            ScreenRect areaArea = new ScreenRect(0, headerBottom, this.width, this.height - this.layout.getFooterHeight() - headerBottom);
            this.tabManager.setTabArea(areaArea);
            this.layout.setHeaderHeight(headerBottom);
            this.layout.refreshPositions();
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    private boolean isTemplateSuggestorVisible() {
        return this.roomBuilderTab != null && this.roomBuilderTab.templateSuggestorWidget != null && this.roomBuilderTab.templateSuggestorWidget.visible;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawTexture(RenderLayer::getGuiTextured, Screen.FOOTER_SEPARATOR_TEXTURE, 0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);

        if (this.isTemplateSuggestorVisible()) {
            this.roomBuilderTab.templateSuggestorWidget.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    protected void renderDarkening(DrawContext context) {
        context.drawTexture(RenderLayer::getGuiTextured, TAB_HEADER_BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderDarkening(context, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.roomBuilderTab != null) {
                RoomBuilderTab tab = this.roomBuilderTab;
                if (tab.templateSuggestorWidget != null) {
                    if (tab.templateSuggestorWidget.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                    tab.templateSuggestorWidget.setVisible(tab.templateNameWidget.isMouseOver(mouseX, mouseY));
                }
                if (tab.templateListWidget != null && !tab.templateListWidget.isMouseOver(mouseX, mouseY)) {
                    tab.templateListWidget.clearFocus();
                }
            }

            for (Element element : this.tabNavigation.children()) {
                if (element instanceof TabButtonWidget widget && widget.getTab() instanceof TemplatesTab && element.mouseClicked(mouseX, mouseY, button)) {
                    this.templatesTab.updateTemplates();
                    break;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.isTemplateSuggestorVisible()) {
            if (this.roomBuilderTab.templateSuggestorWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.isTemplateSuggestorVisible()) {
            if (this.roomBuilderTab.templateSuggestorWidget.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // Word 'Input' indicates the check is thought for real time user written input, thus being less strict
    // than the final check made before applying the input to a field
    private static boolean isIdentifierInputValid(String identifierAsString) {
        if (identifierAsString.isEmpty()) return true;
        String[] namespaceAndPath = identifierAsString.split(":", 2);
        if (namespaceAndPath.length >= 2) {
            return namespaceAndPath.length == 2 && Identifier.isNamespaceValid(namespaceAndPath[0]) && Identifier.isPathValid(namespaceAndPath[1]);
        }
        return Identifier.isPathValid(identifierAsString);
    }

    // Checks if the string contains a valid namespace and path
    private static boolean isIdentifierValid(String identifierAsString) {
        String[] namespaceAndPath = identifierAsString.split(":", 2);
        return namespaceAndPath.length == 2 && Identifier.isNamespaceValid(namespaceAndPath[0]) && Identifier.isPathValid(namespaceAndPath[1]);
    }

    private static boolean isIntegerNumInputValid(String string) {
        return string.isEmpty() || string.equals("-") || isIntegerNumValid(string);
    }

    private static boolean isIntegerNumValid(String num) {
        return num.matches("^-?\\d+$");
    }

    private static boolean isOptionalNumValid(String num, int min) {
        return num.isEmpty() || (isIntegerNumValid(num) && parseOrDefault(num, -1) >= min);
    }

    private static boolean isOptionalPositiveFloatValid(String num) {
        return num.isEmpty() || (num.matches("^(?=.*\\d)\\d*\\.?\\d*$") && parseFloatOrDefault(num, -1f) >= 0f);
    }

    private static int parseOrDefault(String text, int fallback) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static float parseFloatOrDefault(String text, float fallback) {
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static Identifier getSaveFolderPath(String name, Level level) {
        Identifier levelName = level.name;
        return Identifier.of(
                levelName.getNamespace(),
                 CubliminalStructures.MOD_STRUCTURE_FILES_PATH + levelName.getPath() + "/" + name
        );
    }

    @Environment(EnvType.CLIENT)
    class SelectionTab extends GridScreenTab {
        private Level level = Levels.LEVEL_0;
        private boolean includeEntities = false;
        TextFieldWidget nameWidget;
        TextFieldWidget x_1;
        TextFieldWidget y_1;
        TextFieldWidget z_1;
        TextFieldWidget x_2;
        TextFieldWidget y_2;
        TextFieldWidget z_2;
        TextFieldWidget sizeX;
        TextFieldWidget sizeY;
        TextFieldWidget sizeZ;

        public SelectionTab() {
            super(CREATE_SELECTION_TEXT);
            this.init();
        }

        void init() {
            GridWidget.Adder adder = this.grid.setRowSpacing(5).createAdder(3);

            adder.add(CyclingButtonWidget.builder(Level::getTranslatableName)
                    .values(Levels.ALL)
                    .build(0, 0, 160, 20, Text.literal("Level"), (button, value) -> this.level = value), 2);

            adder.add(CyclingButtonWidget.onOffBuilder(false)
                    .build(0, 0, 80, 20, Text.literal("Entities"), (button, value) -> this.includeEntities = value));

            this.nameWidget = new TextFieldWidget(textRenderer, 240, 20, Text.empty());
            this.nameWidget.setTextPredicate(Identifier::isPathValid);
            this.nameWidget.setMaxLength(128);
            this.nameWidget.setPlaceholder(Text.translatable("structure_block.structure_name").formatted(Formatting.GRAY));
            adder.add(this.nameWidget, 3);

            adder.add(new TextWidget(Text.literal("Corner 1:"), textRenderer), 3);

            this.x_1 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.y_1 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.z_1 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.x_1.setTextPredicate(RoomCreatorScreen::isIntegerNumInputValid);
            this.y_1.setTextPredicate(RoomCreatorScreen::isIntegerNumInputValid);
            this.z_1.setTextPredicate(RoomCreatorScreen::isIntegerNumInputValid);
            adder.add(this.x_1);
            adder.add(this.y_1);
            adder.add(this.z_1);

            adder.add(new TextWidget(Text.literal("Corner 2:"), textRenderer), 3);

            this.x_2 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.y_2 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.z_2 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.x_2.setTextPredicate(RoomCreatorScreen::isIntegerNumInputValid);
            this.y_2.setTextPredicate(RoomCreatorScreen::isIntegerNumInputValid);
            this.z_2.setTextPredicate(RoomCreatorScreen::isIntegerNumInputValid);
            adder.add(this.x_2);
            adder.add(this.y_2);
            adder.add(this.z_2);

            adder.add(new TextWidget(Text.literal("Size:"), textRenderer), 3);

            this.sizeX = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.sizeY = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.sizeZ = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.sizeX.setEditable(false);
            this.sizeY.setEditable(false);
            this.sizeZ.setEditable(false);
            adder.add(this.sizeX);
            adder.add(this.sizeY);
            adder.add(this.sizeZ);

            ButtonWidget approximateButton = ButtonWidget
                    .builder(Text.literal("Approximate"), button -> this.approximateSelection())
                    .size(80, 20).build();
            approximateButton.active = dataManager.hasAllData();
            adder.add(approximateButton);

            ButtonWidget applyButton = ButtonWidget
                    .builder(Text.literal("Apply"), button -> this.applyChanges())
                    .size(80, 20).build();
            applyButton.active = dataManager.hasAllData();
            adder.add(applyButton);

            ButtonWidget saveButton = ButtonWidget
                    .builder(Text.literal("Save"), button -> this.saveSelection())
                    .size(80, 20).build();
            saveButton.active = dataManager.hasAllData();
            adder.add(saveButton);

            this.refresh();
        }

        void refresh() {
            if (dataManager.hasCorner1()) {
                BlockPos corner1 = dataManager.getCorner1();
                this.x_1.setText(Integer.toString(corner1.getX()));
                this.y_1.setText(Integer.toString(corner1.getY()));
                this.z_1.setText(Integer.toString(corner1.getZ()));
            }
            if (dataManager.hasCorner2()) {
                BlockPos corner2 = dataManager.getCorner2();
                this.x_2.setText(Integer.toString(corner2.getX()));
                this.y_2.setText(Integer.toString(corner2.getY()));
                this.z_2.setText(Integer.toString(corner2.getZ()));
            }
            if (dataManager.hasSelection()) {
                BlockBox selection = dataManager.getSelection();
                this.sizeX.setText(Integer.toString(selection.getBlockCountX()));
                this.sizeY.setText(Integer.toString(selection.getBlockCountY()));
                this.sizeZ.setText(Integer.toString(selection.getBlockCountZ()));
            }
        }

        void approximateSelection() {
            if (dataManager.hasAllData()) {
                if (dataManager.approximateSelection(this.level)) {
                    NoticeScreen warningPopup = new NoticeScreen(
                            () -> client.setScreen(RoomCreatorScreen.this),
                            STRUCTURE_TOO_TALL_TITLE,
                            STRUCTURE_TOO_TALL.apply(this.level.layer_height),
                            ScreenTexts.ACKNOWLEDGE,
                            false
                    );

                    client.setScreen(warningPopup);
                }

                this.refresh();
            }
        }

        void applyChanges() {
            if (dataManager.hasAllData()) {
                BlockPos.Mutable corner1 = dataManager.getCorner1().mutableCopy();
                BlockPos.Mutable corner2 = dataManager.getCorner2().mutableCopy();
                if (isIntegerNumValid(this.x_1.getText())) corner1.setX(Integer.parseInt(this.x_1.getText()));
                if (isIntegerNumValid(this.y_1.getText())) corner1.setY(Integer.parseInt(this.y_1.getText()));
                if (isIntegerNumValid(this.z_1.getText())) corner1.setZ(Integer.parseInt(this.z_1.getText()));
                if (isIntegerNumValid(this.x_2.getText())) corner2.setX(Integer.parseInt(this.x_2.getText()));
                if (isIntegerNumValid(this.y_2.getText())) corner2.setY(Integer.parseInt(this.y_2.getText()));
                if (isIntegerNumValid(this.z_2.getText())) corner2.setZ(Integer.parseInt(this.z_2.getText()));
                dataManager.setCorner1(corner1.toImmutable());
                dataManager.setCorner2(corner2.toImmutable());

                this.refresh();
            }
        }

        void saveSelection() {
            if (dataManager.hasAllData() && !this.nameWidget.getText().isBlank()) {
                ClientPlayNetworking.send(new SaveSelectionC2SPayload(
                        RoomCreatorScreen.getSaveFolderPath(this.nameWidget.getText(), this.level),
                        dataManager.getStartPos(),
                        dataManager.getSelectionDimensions(),
                        this.includeEntities
                ));

                RoomCreatorScreen.this.close();
            }
        }
    }

    @Environment(EnvType.CLIENT)
    class TemplatesTab extends GridScreenTab {

        private static final int SEARCH_COLUMN_WIDTH = 100;
        private static final int LIST_ITEM_HEIGHT = 24;
        private static final int RIGHT_COLUMN_MARGIN = 10;

        private TemplateScrollList listWidget;
        private TextFieldWidget searchPatternWidget;

        private String searchPattern = "";
        private boolean minecraftStructures = false;
        private boolean fullNames = false;

        public TemplatesTab() {
            super(TEMPLATES_TEXT);
            this.init();
        }

        void init() {
            this.grid.setColumnSpacing(5);

            int listWidth = RoomCreatorScreen.this.width - SEARCH_COLUMN_WIDTH - RIGHT_COLUMN_MARGIN - 20;
            int listHeight = 180;
            this.listWidget = new TemplateScrollList(MinecraftClient.getInstance(), listWidth, listHeight, 40);
            this.grid.add(this.listWidget, 1, 1, 6, 3);

            this.searchPatternWidget = new TextFieldWidget(textRenderer, SEARCH_COLUMN_WIDTH, 20, Text.empty()) {
                @Override
                public boolean charTyped(char chr, int modifiers) {
                    return RoomCreatorScreen.this.isValidCharacterForName(this.getText(), chr, this.getCursor()) && super.charTyped(chr, modifiers);
                }
            };
            this.searchPatternWidget.setMaxLength(128);
            this.searchPatternWidget.setChangedListener(input -> {
                if (!this.searchPattern.equals(input)) {
                    this.searchPattern = input;
                    this.listWidget.updateTemplates();
                }
            });
            this.grid.add(this.searchPatternWidget, 1, 4, 1, 1);

            this.grid.add(ButtonWidget.builder(
                    Text.literal("Clear Filter"),
                    button -> this.searchPatternWidget.setText("")
            ).size(SEARCH_COLUMN_WIDTH, 20).build(), 2, 4);

            this.grid.add(CyclingButtonWidget.onOffBuilder(false).build(
                    0, 0, SEARCH_COLUMN_WIDTH, 20,
                    Text.literal("Full Names"),
                    (button, value) -> {
                        this.fullNames = value;
                        this.refresh();
                    }), 3, 4);

            this.grid.add(CyclingButtonWidget.onOffBuilder(false).build(
                    0, 0, SEARCH_COLUMN_WIDTH, 20,
                    Text.literal("MC Templates"),
                    (button, value) -> {
                        this.minecraftStructures = value;
                        this.refresh();
                    }), 4, 4);
        }

        void refresh() {
            this.updateTemplates();
        }

        void updateTemplates() {
            this.listWidget.updateTemplates();
        }

        class TemplateScrollList extends ElementListWidget<TemplateScrollList.Entry> {

            private final List<Pair<Identifier, Vec3i>> listedTemplates = new ArrayList<>();

            public TemplateScrollList(MinecraftClient client, int width, int height, int y) {
                super(client, width, height, y, LIST_ITEM_HEIGHT);
            }

            @Override
            public int getRowWidth() {
                return this.width - 20;
            }

            @Override
            protected int getScrollbarX() {
                return this.getX() + this.width - 6;
            }

            void init() {
                this.setScrollAmount(0);
                this.listedTemplates.forEach(pair -> this.addEntry(new Entry(pair.getFirst(), pair.getSecond())));
            }

            void updateTemplates() {
                this.listedTemplates.clear();
                this.listedTemplates.addAll(dataManager.getStructureTemplates());

                String filter = TemplatesTab.this.searchPatternWidget.getText();
                if (!minecraftStructures) {
                    this.listedTemplates.removeIf(pair -> pair.getFirst().getNamespace().equals(Identifier.DEFAULT_NAMESPACE));
                }
                if (!filter.isBlank()) {
                    this.listedTemplates.removeIf(pair -> !pair.getFirst().toString().contains(filter));
                }

                this.clearEntries();
                this.init();
            }

            class Entry extends ElementListWidget.Entry<Entry> {

                private final ButtonWidget button;
                private final TextWidget templateNameWidget;

                private final Identifier templateName;
                private Vec3i size;

                public Entry(Identifier templateName, Vec3i size) {
                    this.templateName = templateName;
                    this.size = size;
                    this.button = ButtonWidget
                            .builder(Text.empty(), button -> {})
                            .tooltip(createTooltip(templateName.toString(), size))
                            .size(TemplateScrollList.this.getRowWidth(), 20)
                            .build();

                    String widgetText = templateName.toString();
                    if (!fullNames) {
                        String[] subStrings = templateName.getPath().split("/");
                        widgetText = subStrings[subStrings.length - 1];
                    }
                    int maxWidth = this.button.getWidth() - 10;
                    String displayText = textRenderer.getWidth(widgetText) > maxWidth
                            ? textRenderer.trimToWidth(widgetText, maxWidth - textRenderer.getWidth("...")) + "..."
                            : widgetText;
                    this.templateNameWidget = new TextWidget(Text.literal(displayText), textRenderer);
                }

                static Tooltip createTooltip(String templateName, Vec3i size) {
                    String description =
                            "Size: [" + size.getX() + ", " + size.getY() + ", " + size.getZ() + "]" +
                            "\n" +
                            "'" + templateName + "'";
                    return Tooltip.of(Text.literal(description).formatted(Formatting.GRAY));
                }

                @Override
                public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                    this.button.setPosition(x - 5, y);
                    this.button.render(context, mouseX, mouseY, tickDelta);
                    this.templateNameWidget.setPosition(x, (int) (y + 0.5 * (this.button.getHeight() - this.templateNameWidget.getHeight())));
                    this.templateNameWidget.alignLeft();
                    this.templateNameWidget.renderWidget(context, mouseX, mouseY, tickDelta);
                }

                @Override
                public List<? extends Selectable> selectableChildren() {
                    return List.of(this.button);
                }

                @Override
                public List<? extends Element> children() {
                    return List.of(this.button);
                }
            }
        }
    }
    @Environment(EnvType.CLIENT)
    class RoomBuilderTab extends GridScreenTab {

        private RoomBuilder roomBuilder = new RoomBuilder();
        private FloorBuilder selectedFloor;
        private ComponentBuilder selectedComponent;
        private DoorBuilder selectedDoor;

        private final List<ClickableWidget> roomInfoWidgets = new ArrayList<>();
        private CyclingButtonWidget<Level> levelWidget;
        private TextFieldWidget roomNameWidget;
        private TextWidget roomSizeText;
        private TextFieldWidget roomSizeXWidget;
        private TextFieldWidget roomSizeZWidget;
        private CyclingButtonWidget<PaddingType> paddingTypeWidget;
        private TextFieldWidget paddingCountWidget;
        private CyclingButtonWidget<RoomType.Type> roomTypeWidget;

        private final List<ClickableWidget> floorEditorWidgets = new ArrayList<>();
        private ButtonWidget deleteComponentButton2;
        private TextWidget componentSizeText;
        private TextFieldWidget componentSizeXWidget;
        private TextFieldWidget componentSizeZWidget;
        private TextWidget componentPosText;
        private TextFieldWidget componentRelXWidget;
        private TextFieldWidget componentRelZWidget;
        private ComponentTemplateList templateListWidget;
        private TextFieldWidget templateNameWidget;
        private TemplateSuggestorWidget templateSuggestorWidget;
        private ButtonWidget addTemplateButton;
        private CyclingButtonWidget<Direction> doorDirectionWidget;
        private DoorIndexSlider doorIndexWidget;
        private ButtonWidget deleteDoorButton2;

        private TextWidget floorLabelWidget;
        private ButtonWidget deleteFloorButton;
        private RoomPreviewWidget previewWidget;

        public RoomBuilderTab() {
            super(ROOM_BUILDER_TEXT);
            this.selectedFloor = this.roomBuilder.addFloor();
            this.init();
        }

        void init() {
            this.grid.setSpacing(3);

            this.initRoomInfoSection();
            this.initComponentSection();
            this.initRight();

            this.setLevel(Levels.ALL[0]);
            this.refreshFloor();
            this.setComponentEditorEnabled(false);
            this.templateSuggestorWidget.setVisible(false);
        }

        void initRoomInfoSection() {
            TextWidget roomInfoHeader = sectionHeader("Room Info");
            this.grid.add(roomInfoHeader, 1, 1, 1, 2);

            this.levelWidget = CyclingButtonWidget.builder(Level::getTranslatableName)
                    .values(Levels.ALL)
                    .build(0, 0, 120, 18, Text.literal("Level"), (b, l) -> this.setLevel(l));
            this.grid.add(this.levelWidget, 2, 1);

            this.roomNameWidget = new TextFieldWidget(textRenderer, 120, 18, Text.empty());
            this.roomNameWidget.setPlaceholder(Text.literal("Room name").formatted(Formatting.GRAY));
            this.roomNameWidget.setTextPredicate(RoomCreatorScreen::isIdentifierInputValid);
            this.roomNameWidget.setChangedListener(s -> {
                this.roomBuilder.setName(s);
            });
            this.roomNameWidget.setMaxLength(128);
            this.grid.add(this.roomNameWidget, 2, 2);

            this.roomSizeText = new TextWidget(Text.literal("Size: (X, Z)"), textRenderer);
            this.roomSizeText.setHeight(16);
            this.roomSizeText.alignLeft();
            this.grid.add(this.roomSizeText, 3, 1);

            this.roomSizeXWidget = smallNumberField(1);
            this.roomSizeXWidget.setText(Integer.toString(this.roomBuilder.getSizeX()));
            this.roomSizeXWidget.setChangedListener(t -> this.refreshFloor());
            this.roomSizeZWidget = smallNumberField(1);
            this.roomSizeZWidget.setText(Integer.toString(this.roomBuilder.getSizeZ()));
            this.roomSizeZWidget.setChangedListener(t -> this.refreshFloor());

            GridWidget sizeBox = new GridWidget().setRowSpacing(0).setColumnSpacing(30);
            sizeBox.add(this.roomSizeXWidget, 1, 1);
            sizeBox.add(this.roomSizeZWidget, 1, 2);
            this.grid.add(sizeBox, 3, 2);

            this.paddingTypeWidget = CyclingButtonWidget
                    .<PaddingType>builder(type -> Text.translatable(type.translatableEntry()))
                    .values(PaddingType.values())
                    .build(0, 0, 120, 18, Text.literal("Padding Type"), (b, v) -> this.roomBuilder.setPaddingType(v));
            this.grid.add(this.paddingTypeWidget, 4, 1);

            this.paddingCountWidget = new TextFieldWidget(textRenderer, 120, 18, Text.empty());
            this.paddingCountWidget.setPlaceholder(Text.literal("Padding Count").formatted(Formatting.GRAY));
            this.paddingCountWidget.setText(Integer.toString(this.roomBuilder.getPaddingCount()));
            this.paddingCountWidget.setTextPredicate(s -> isOptionalNumValid(s, 0));
            this.paddingCountWidget.setChangedListener(n -> this.roomBuilder.setPaddingCount(parseOrDefault(n, 0)));
            this.grid.add(this.paddingCountWidget, 4, 2);

            this.roomTypeWidget = CyclingButtonWidget
                    .<RoomType.Type>builder(type -> Text.literal(type.toString()))
                    .values(RoomType.Type.values())
                    .build(0, 0, 243, 18, Text.literal("Room Type"), (b, v) -> this.roomBuilder.setRoomType(v));
            this.grid.add(this.roomTypeWidget, 5, 1, 1, 2);

            ButtonWidget loadRoomButton = ButtonWidget
                    .builder(Text.literal("Load"), b -> this.openRoomSelectionWindow())
                    .size(120, 18).build();
            this.grid.add(loadRoomButton, 6, 1);

            ButtonWidget saveRoomButton = ButtonWidget
                    .builder(Text.literal("Save"), b -> this.saveRoom())
                    .size(120, 18).build();
            this.grid.add(saveRoomButton, 6, 2);

            Positioner bottomPositioner = this.grid.copyPositioner().alignBottom();
            TextWidget componentsAndDoorsHeader = sectionHeader("Components and Doors");
            this.grid.add(componentsAndDoorsHeader, 7, 1, 1, 2, bottomPositioner);

            ButtonWidget addComponentButton = ButtonWidget.builder(Text.literal("Add Component"), b -> this.addComponent())
                    .size(120, 18).build();
            ButtonWidget addDoorButton = ButtonWidget.builder(Text.literal("Add Door"), b -> this.addDoor())
                    .size(120, 18).build();

            this.grid.add(addComponentButton, 8, 1);
            this.grid.add(addDoorButton, 8, 2);

            this.roomInfoWidgets.addAll(List.of(
                    roomInfoHeader,
                    this.levelWidget,
                    this.roomNameWidget,
                    this.roomSizeText,
                    this.roomSizeXWidget,
                    this.roomSizeZWidget,
                    this.paddingTypeWidget,
                    this.paddingCountWidget,
                    this.roomTypeWidget,
                    loadRoomButton,
                    saveRoomButton,
                    componentsAndDoorsHeader,
                    addComponentButton,
                    addDoorButton
            ));
        }

        void initComponentSection() {
            TextWidget componentsHeader2 = sectionHeader("Components");
            this.grid.add(componentsHeader2, 1, 1, 1, 2);

            ButtonWidget addComponentButton2 = ButtonWidget.builder(Text.literal("Add"), b -> this.addComponent())
                    .size(65, 18).build();
            this.deleteComponentButton2 = ButtonWidget.builder(Text.literal("Delete"), b -> this.deleteSelectedComponent())
                    .size(65, 18).build();

            this.grid.add(addComponentButton2, 2, 1);
            this.grid.add(this.deleteComponentButton2, 2, 2);

            this.componentSizeText = new TextWidget(Text.literal("Size: (X, Z)"), textRenderer);
            this.componentSizeText.setHeight(16);
            this.componentSizeText.alignLeft();
            this.grid.add(this.componentSizeText, 3, 1);

            this.componentSizeXWidget = smallNumberField(1);
            this.componentSizeXWidget.setChangedListener(t -> this.applyComponentFields(true));
            this.componentSizeZWidget = smallNumberField(1);
            this.componentSizeZWidget.setChangedListener(t -> this.applyComponentFields(true));

            GridWidget sizeBox = new GridWidget().setRowSpacing(0).setColumnSpacing(2);
            sizeBox.add(this.componentSizeXWidget, 1, 1);
            sizeBox.add(this.componentSizeZWidget, 1, 2);
            this.grid.add(sizeBox, 3, 2);

            this.componentPosText = new TextWidget(Text.literal("Position: (X, Z)"), textRenderer);
            this.componentPosText.setHeight(16);
            this.componentPosText.alignLeft();
            this.grid.add(this.componentPosText, 4, 1);

            this.componentRelXWidget = smallNumberField(0);
            this.componentRelXWidget.setChangedListener(t -> this.applyComponentFields(false));
            this.componentRelZWidget = smallNumberField(0);
            this.componentRelZWidget.setChangedListener(t -> this.applyComponentFields(false));

            GridWidget posBox = new GridWidget().setRowSpacing(0).setColumnSpacing(2);
            posBox.add(this.componentRelXWidget, 1, 1);
            posBox.add(this.componentRelZWidget, 1, 2);
            this.grid.add(posBox, 4, 2);

            this.templateListWidget = new ComponentTemplateList(client, 187, 38, 40);
            this.grid.add(this.templateListWidget, 5, 1, 1, 2);

            this.templateNameWidget = new TextFieldWidget(textRenderer, 156, 18, Text.empty());
            this.templateNameWidget.setTextPredicate(RoomCreatorScreen::isIdentifierInputValid);
            this.templateNameWidget.setMaxLength(128);

            this.addTemplateButton = ButtonWidget.builder(Text.literal("+"), b -> this.addTemplateName())
                    .size(28, 18).build();

            DirectionalLayoutWidget templateRow = DirectionalLayoutWidget.horizontal().spacing(3);
            Positioner centered = templateRow.copyPositioner().alignVerticalCenter();

            templateRow.add(this.templateNameWidget, centered);
            templateRow.add(this.addTemplateButton, centered);
            this.grid.add(templateRow, 6, 1, 1, 2);

            Positioner bottomPositioner = this.grid.copyPositioner().alignBottom();
            TextWidget doorsHeader2 = sectionHeader("Doors");
            this.grid.add(doorsHeader2, 7, 1, 1, 2, bottomPositioner);

            this.doorDirectionWidget = CyclingButtonWidget.<Direction>builder(dir -> Text.literal(dir.toString().toUpperCase()))
                    .values(Direction.Type.HORIZONTAL.stream().toList())
                    .omitKeyText()
                    .build(0, 0, 65, 18, Text.empty(), (b, d) -> this.applyDoorFields());

            this.doorIndexWidget = new DoorIndexSlider(0, 0, 65, 18, Text.empty(), 3, 0);

            this.grid.add(this.doorDirectionWidget, 8, 1);
            this.grid.add(this.doorIndexWidget, 8, 2);

            ButtonWidget addDoorButton2 = ButtonWidget.builder(Text.literal("Add"), b -> this.addDoor())
                    .size(65, 18).build();
            this.deleteDoorButton2 = ButtonWidget.builder(Text.literal("Delete"), b -> this.deleteSelectedDoor())
                    .size(65, 18).build();

            this.grid.add(addDoorButton2, 9, 1);
            this.grid.add(this.deleteDoorButton2, 9, 2);

            this.templateSuggestorWidget = new TemplateSuggestorWidget(this.levelWidget, this.templateNameWidget);
            this.templateNameWidget.setChangedListener(s -> this.templateSuggestorWidget.setTemplateName(s));

            this.floorEditorWidgets.addAll(List.of(
                    componentsHeader2,
                    addComponentButton2,
                    this.deleteComponentButton2,
                    this.componentSizeText,
                    this.componentSizeXWidget,
                    this.componentSizeZWidget,
                    this.componentPosText,
                    this.componentRelXWidget,
                    this.componentRelZWidget,
                    this.templateListWidget,
                    this.templateNameWidget,
                    this.addTemplateButton,
                    doorsHeader2,
                    this.doorDirectionWidget,
                    this.doorIndexWidget,
                    addDoorButton2,
                    this.deleteDoorButton2
            ));
        }

        void initRight() {
            this.grid.add(new EmptyWidget(32, 1), 1, 3);

            this.grid.add(sectionHeader("Floor"), 1, 4, 1, 2);

            ButtonWidget addFloorButton = ButtonWidget.builder(Text.literal("Add"), button -> this.addFloor())
                    .size(55, 18).build();
            this.deleteFloorButton = ButtonWidget.builder(Text.literal("Delete"), button -> this.removeFloor())
                    .size(55, 18).build();
            this.deleteFloorButton.active = false;

            this.grid.add(addFloorButton, 2, 4);
            this.grid.add(this.deleteFloorButton, 2, 5);

            ButtonWidget decreaseFloorWidget = ButtonWidget.builder(Text.literal("<"), b -> this.cycleFloor(-1))
                    .size(18, 18).build();
            ButtonWidget increaseFloorWidget = ButtonWidget.builder(Text.literal(">"), b -> this.cycleFloor(1))
                    .size(18, 18).build();

            this.floorLabelWidget = new TextWidget(this.floorLabelText(), textRenderer).alignCenter();
            this.floorLabelWidget.setDimensions(72, 18);
            this.floorLabelWidget.alignCenter();

            DirectionalLayoutWidget navBox = DirectionalLayoutWidget.horizontal().spacing(4);
            Positioner centeredPositioner = navBox.copyPositioner().alignHorizontalCenter();

            navBox.add(decreaseFloorWidget, centeredPositioner);
            navBox.add(this.floorLabelWidget, centeredPositioner);
            navBox.add(increaseFloorWidget, centeredPositioner);
            this.grid.add(navBox, 3, 4, 1, 2);

            this.previewWidget = new RoomPreviewWidget(0, 0, 120, 120);
            this.previewWidget.setSelectionListener(this::onSelectedChange);
            this.previewWidget.setDragListener(this::syncPosAfterDrag);
            this.grid.add(this.previewWidget,4, 4, 6, 2);
        }

        private static TextFieldWidget smallNumberField(int min) {
            TextFieldWidget field = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 30, 16, Text.empty());
            field.setTextPredicate(s -> isOptionalNumValid(s, min));
            return field;
        }

        private static void addCellSizeTooltip(ClickableWidget widget, Level level) {
            widget.setTooltip(Tooltip.of(Text.literal(
                    "One square unit is " + level.spacing_x + "x" + level.spacing_z + " blocks"
            ).formatted(Formatting.GRAY)));
        }

        private static TextWidget sectionHeader(String text) {
            return new TextWidget(Text.literal(text).formatted(Formatting.BOLD, Formatting.YELLOW), MinecraftClient.getInstance().textRenderer)
                    .alignCenter();
        }

        private Text floorLabelText() {
            int index = this.roomBuilder.getFloors().indexOf(this.selectedFloor) + 1;
            return Text.literal(index + "/" + this.roomBuilder.getFloors().size()).formatted(Formatting.YELLOW);
        }

        private void refreshDoors() {
            this.doorIndexWidget.setMaxSliderValue(
                    this.doorDirectionWidget.getValue().getAxis() == Direction.Axis.X
                    ? this.roomBuilder.getSizeZ()
                    : this.roomBuilder.getSizeX()
            );
            this.selectedFloor.afterResizingRoom(this.roomBuilder.getSizeX(), this.roomBuilder.getSizeZ());
        }

        private void refreshFloor() {
            this.roomBuilder.setSizeX(parseOrDefault(this.roomSizeXWidget.getText(), this.roomBuilder.getSizeX()));
            this.roomBuilder.setSizeZ(parseOrDefault(this.roomSizeZWidget.getText(), this.roomBuilder.getSizeZ()));

            this.previewWidget.setFloor(this.selectedFloor);
            this.previewWidget.setBounds(this.roomBuilder.getSizeX(), this.roomBuilder.getSizeZ());
            this.floorLabelWidget.setMessage(this.floorLabelText());

            this.refreshDoors();
        }

        private void syncFieldsFromRoom(RoomBuilder room) {
            this.roomNameWidget.setText(room.getName());
            this.roomSizeXWidget.setText(Integer.toString(room.getSizeX()));
            this.roomSizeZWidget.setText(Integer.toString(room.getSizeZ()));
            this.paddingTypeWidget.setValue(room.getPaddingType());
            this.paddingCountWidget.setText(Integer.toString(room.getPaddingCount()));
            this.roomTypeWidget.setValue(room.getRoomType());
            this.levelWidget.setValue(Levels.ALL[0]);
            this.selectedFloor = room.getFloors().getFirst();
        }

        private void cycleFloor(int direction) {
            List<FloorBuilder> floors = this.roomBuilder.getFloors();
            int index = floors.indexOf(this.selectedFloor);
            index = Math.floorMod(index + direction, floors.size());
            this.selectedFloor = floors.get(index);
            this.onSelectedChange();
            this.refreshFloor();
        }

        private void addFloor() {
            this.selectedFloor = this.roomBuilder.addFloor();

            this.deleteFloorButton.active = true;

            this.onSelectedChange();
            this.refreshFloor();
        }

        private void removeFloor() {
            if (this.roomBuilder.getFloors().size() <= 1) return;
            this.roomBuilder.removeFloor(this.selectedFloor);
            this.selectedFloor = this.roomBuilder.getFloors().getFirst();

            boolean singleFloor = this.roomBuilder.getFloors().size() == 1;
            this.deleteFloorButton.active = !singleFloor;

            this.onSelectedChange();
            this.refreshFloor();
        }

        private void setLevel(Level level) {
            addCellSizeTooltip(this.roomSizeText, level);
            addCellSizeTooltip(this.componentSizeText, level);
            addCellSizeTooltip(this.componentPosText, level);

            this.refreshDoors();
            if (this.selectedComponent != null) {
                this.templateSuggestorWidget.recalculateTemplateSize(level, this.selectedComponent);
            }
        }

        private void addComponent() {
            ComponentBuilder component = this.selectedFloor.addComponent();
            this.previewWidget.selectComponent(component);
        }

        private void deleteSelectedComponent() {
            if (this.selectedComponent == null) return;
            this.selectedFloor.removeComponent(this.selectedComponent);
            this.previewWidget.selectComponent(null);
        }

        private void onSelectedChange() {
            ComponentBuilder component = this.previewWidget.getSelectedComponent();
            this.selectedComponent = component;

            if (component == null) {
                this.templateSuggestorWidget.setVisible(false);
            }

            DoorBuilder door = this.previewWidget.getSelectedDoor();
            this.selectedDoor = door;

            this.setComponentEditorEnabled(component != null || door != null);

            this.syncFieldsFromComponent();
            this.syncFieldsFromDoor();

            this.templateListWidget.setScrollAmount(0);
        }

        private void syncPosAfterDrag() {
            ComponentBuilder c = this.selectedComponent;
            if (c != null) {
                int relX = c.getRelX();
                int relZ = c.getRelZ();
                this.componentRelXWidget.setText(Integer.toString(relX));
                this.componentRelZWidget.setText(Integer.toString(relZ));
            } else {
                this.componentRelXWidget.setText("");
                this.componentRelZWidget.setText("");
            }
        }

        private void syncFieldsFromComponent() {
            ComponentBuilder c = this.selectedComponent;
            if (c != null) {
                int sizeX = c.getSizeX();
                int sizeZ = c.getSizeZ();
                int relX = c.getRelX();
                int relZ = c.getRelZ();
                this.deleteComponentButton2.active = true;
                this.componentSizeXWidget.active = true;
                this.componentSizeZWidget.active = true;
                this.componentRelXWidget.active = true;
                this.componentRelZWidget.active = true;
                this.templateListWidget.active = true;
                this.templateNameWidget.active = true;
                this.addTemplateButton.active = true;
                this.componentSizeXWidget.setText(Integer.toString(sizeX));
                this.componentSizeZWidget.setText(Integer.toString(sizeZ));
                this.componentRelXWidget.setText(Integer.toString(relX));
                this.componentRelZWidget.setText(Integer.toString(relZ));
                this.templateListWidget.updateTemplates();
            } else {
                this.componentSizeXWidget.setText("");
                this.componentSizeZWidget.setText("");
                this.componentRelXWidget.setText("");
                this.componentRelZWidget.setText("");
                this.templateListWidget.clear();
                this.templateNameWidget.setText("");
                this.deleteComponentButton2.active = false;
                this.componentSizeXWidget.active = false;
                this.componentSizeZWidget.active = false;
                this.componentRelXWidget.active = false;
                this.componentRelZWidget.active = false;
                this.templateListWidget.active = false;
                this.templateNameWidget.active = false;
                this.addTemplateButton.active = false;
            }
        }

        private void applyComponentFields(boolean updateSuggestor) {
            if (this.selectedComponent == null) return;
            this.selectedComponent.setSizeX(Math.max(1, parseOrDefault(this.componentSizeXWidget.getText(), this.selectedComponent.getSizeX())));
            this.selectedComponent.setSizeZ(Math.max(1, parseOrDefault(this.componentSizeZWidget.getText(), this.selectedComponent.getSizeZ())));
            this.selectedComponent.setRelX(Math.max(0, parseOrDefault(this.componentRelXWidget.getText(), this.selectedComponent.getRelX())));
            this.selectedComponent.setRelZ(Math.max(0, parseOrDefault(this.componentRelZWidget.getText(), this.selectedComponent.getRelZ())));

            if (updateSuggestor) {
                this.templateSuggestorWidget.recalculateTemplateSize(this.levelWidget.getValue(), this.selectedComponent);
            }
        }

        private void syncFieldsFromDoor() {
            DoorBuilder d = this.selectedDoor;
            if (d != null) {
                int index = d.getIndex();
                Direction facing = d.getFacing();
                this.doorIndexWidget.active = true;
                this.doorDirectionWidget.active = true;
                this.deleteDoorButton2.active = true;
                this.doorIndexWidget.setSliderValue(index);
                this.doorDirectionWidget.setValue(facing);
            } else {
                this.doorIndexWidget.setSliderValue(0);
                this.doorDirectionWidget.setValue(Direction.NORTH);
                this.doorIndexWidget.active = false;
                this.doorDirectionWidget.active = false;
                this.deleteDoorButton2.active = false;
            }
        }

        private void applyDoorFields() {
            if (this.selectedDoor == null) return;
            this.doorIndexWidget.setMaxSliderValue(
                    this.doorDirectionWidget.getValue().getAxis() == Direction.Axis.X
                            ? this.roomBuilder.getSizeZ()
                            : this.roomBuilder.getSizeX()
            );
            this.selectedDoor.recalculateData(
                    this.roomBuilder.getSizeX(),
                    this.roomBuilder.getSizeZ(),
                    this.doorIndexWidget.getSliderValue(),
                    this.doorDirectionWidget.getValue()
            );
        }

        private void addDoor() {
            DoorBuilder door = this.selectedFloor.addDoor();
            this.previewWidget.selectDoor(door);
        }

        private void deleteSelectedDoor() {
            if (this.selectedDoor == null) return;
            this.selectedFloor.removeDoor(this.selectedDoor);
            this.previewWidget.selectDoor(null);
        }

        private void setComponentEditorEnabled(boolean enabled) {
            for (ClickableWidget widget : this.roomInfoWidgets) {
                widget.visible = !enabled;
            }
            for (ClickableWidget widget : this.floorEditorWidgets) {
                widget.visible = enabled;
            }
        }

        // A template can't be added twice
        private void addTemplateName() {
            if (this.selectedComponent == null) return;
            String templateName = this.templateNameWidget.getText().trim();
            if (isIdentifierValid(templateName) && this.selectedComponent.hasNotTemplate(templateName)) {
                this.selectedComponent.add(templateName);
                this.templateNameWidget.setText("");
                this.templateListWidget.updateTemplates();
            }
        }

        private void deleteTemplateName(String name) {
            if (this.selectedComponent == null) return;
            this.selectedComponent.remove(name);
        }

        private void openRoomSelectionWindow() {
            dataManager.openRoomSelectionWindow(client).ifPresent(this::loadRoom);
        }

        private void loadRoom(String fileLocation) {
            File roomFile = new File(fileLocation);
            String roomName = roomFile.getName().substring(0, roomFile.getName().length() - ".json".length());
            this.loadRoomFromFile(roomFile).ifPresent(room -> {
                 RoomBuilder builder = RoomBuilder.fromBuiltRoom(room, roomName);
                 this.syncFieldsFromRoom(builder);
                 this.roomBuilder = builder;
                 this.refreshFloor();
            });
        }

        private Optional<Room> loadRoomFromFile(File roomFile) {
            try {
                InputStream inputStream = new FileInputStream(roomFile);
                Optional<Room> decodedResult;
                try {
                    InputStream fixedInputStream = new FixedBufferInputStream(inputStream);
                    try (Reader reader = new InputStreamReader(fixedInputStream)) {
                        decodedResult = Room.CODEC
                                .parse(JsonOps.INSTANCE, JsonHelper.deserialize(reader))
                                .resultOrPartial();
                    } catch (Throwable var9) {
                        try {
                            fixedInputStream.close();
                        } catch (Throwable var8) {
                            var9.addSuppressed(var8);
                        }
                        throw var9;
                    }
                    fixedInputStream.close();
                } catch (Throwable var10) {
                    try {
                        inputStream.close();
                    } catch (Throwable var7) {
                        var10.addSuppressed(var7);
                    }

                    throw var10;
                }
                inputStream.close();
                return decodedResult;
            } catch (FileNotFoundException var11) {
                return Optional.empty();
            } catch (Throwable var12) {
                Cubliminal.LOGGER.error(var12.getMessage());
                return Optional.empty();
            }
        }

        private void saveRoom() {
            Level level = this.levelWidget.getValue();
            List<Text> optionalFixes = this.roomBuilder.trySaveRoom(level);
            if (!optionalFixes.isEmpty()) {
                client.setScreen(new FixSuggestionScreen(
                        () -> client.setScreen(RoomCreatorScreen.this),
                        RoomBuilder.getUnableToConvertText(this.roomTypeWidget.getValue()),
                        optionalFixes
                ));
            } else {
                RoomCreatorScreen.this.close();
            }
        }

        class ComponentTemplateList extends ElementListWidget<ComponentTemplateList.TemplateEntry> {

            public ComponentTemplateList(MinecraftClient client, int width, int height, int y) {
                super(client, width, height, y, 18);
                this.init();
            }

            @Override
            public int getRowWidth() {
                return this.width - 12;
            }

            @Override
            protected int getScrollbarX() {
                return this.getX() + this.width - 6;
            }

            void init() {
                this.setScrollAmount(0);
                this.updateTemplates();
            }

            void remove(TemplateEntry entry) {
                this.removeEntryWithoutScrolling(entry);
            }

            void clear() {
                this.clearEntries();
            }

            void clearFocus() {
                this.setFocused(null);
                for (TemplateEntry entry : this.children()) {
                    entry.setFocused(null);
                    entry.weightWidget.setFocused(false);
                }
            }

            void updateTemplates() {
                this.clear();
                if (selectedComponent == null) return;
                selectedComponent.getTemplates().getValues().forEach(
                        pair -> this.addEntry(
                                new TemplateEntry(pair.getSecond(), pair.getFirst())
                        )
                );
            }

            class TemplateEntry extends ElementListWidget.Entry<TemplateEntry> {

                private final TextWidget nameWidget;
                private final TextFieldWidget weightWidget;
                private final ButtonWidget deleteButton;
                private final String templateName;

                TemplateEntry(String templateName, float weight) {
                    int totalWidth = ComponentTemplateList.this.getRowWidth() - 5;
                    int deleteWidth = 16;
                    int weightWidth = 28;
                    int spacing = 3;
                    int nameWidth = totalWidth - weightWidth - deleteWidth - (spacing * 2);

                    String[] splitName = Identifier.of(templateName).getPath().split("/");
                    this.nameWidget = new TextWidget(Text.literal(splitName[splitName.length - 1]), textRenderer);
                    this.nameWidget.setTooltip(Tooltip.of(Text.literal(templateName).formatted(Formatting.GRAY)));
                    this.nameWidget.setDimensions(nameWidth, 16);
                    this.nameWidget.alignLeft();

                    this.weightWidget = new TextFieldWidget(textRenderer, weightWidth, 16, Text.empty());
                    this.weightWidget.setText(Float.toString(weight));
                    this.weightWidget.setTextPredicate(RoomCreatorScreen::isOptionalPositiveFloatValid);
                    this.weightWidget.setChangedListener(this::updateWeight);

                    this.deleteButton = ButtonWidget.builder(
                            Text.literal("x"), b -> this.delete()
                    ).size(deleteWidth, 16).build();

                    this.templateName = templateName;
                }

                private void updateWeight(String newValue) {
                    if (selectedComponent == null) return;
                    selectedComponent.setWeight(this.templateName, parseFloatOrDefault(newValue, 0.0f));
                }

                private void delete() {
                    if (selectedComponent == null) return;
                    deleteTemplateName(this.templateName);
                    remove(this);
                }

                @Override
                public List<? extends Selectable> selectableChildren() {
                    return List.of(this.nameWidget, this.weightWidget, this.deleteButton);
                }

                @Override
                public List<? extends Element> children() {
                    return List.of(this.nameWidget, this.weightWidget, this.deleteButton);
                }

                @Override
                public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                    this.nameWidget.setPosition(x, y);
                    this.nameWidget.render(context, mouseX, mouseY, tickDelta);

                    int weightX = x + this.nameWidget.getWidth() + 3;
                    this.weightWidget.setPosition(weightX, y);
                    this.weightWidget.render(context, mouseX, mouseY, tickDelta);

                    int deleteX = weightX + this.weightWidget.getWidth() + 3;
                    this.deleteButton.setPosition(deleteX, y);
                    this.deleteButton.render(context, mouseX, mouseY, tickDelta);
                }
            }
        }

        class DoorIndexSlider extends SliderWidget {
            private int maxSliderValue;

            public DoorIndexSlider(int x, int y, int width, int height, Text text, int maxSliderValue, double value) {
                super(x, y, width, height, text, value);
                this.maxSliderValue = maxSliderValue;
                this.updateMessage();
            }

            public void setMaxSliderValue(int max) {
                int newValue = MathHelper.clamp(this.getSliderValue(), 0, max - 1);
                this.maxSliderValue = max - 1;
                this.value = (double) newValue / this.maxSliderValue;
                this.updateMessage();
            }

            public int getSliderValue() {
                return Math.round((float) this.value * this.maxSliderValue);
            }

            public void setSliderValue(int value) {
                int newValue = MathHelper.clamp(value, 0, this.maxSliderValue);
                this.value = (double) newValue / this.maxSliderValue;
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(Integer.toString(this.getSliderValue() + 1)));
            }

            @Override
            protected void applyValue() {
                applyDoorFields();
            }
        }
    }
}