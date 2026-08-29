package net.limit.cubliminal.client.screen.roomcreator;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.client.screen.roomcreator.data.*;
import net.limit.cubliminal.client.screen.roomcreator.widget.RoomPreviewWidget;
import net.limit.cubliminal.client.screen.roomcreator.widget.TemplateSuggestorWidget;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.level.Levels;
import net.limit.cubliminal.networking.c2s.SaveSelectionC2SPayload;
import net.limit.cubliminal.world.room.PaddingType;
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
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;

import java.util.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class RoomCreatorScreen extends Screen {
    private static final Text ROOM_CREATOR_SCREEN = Text.translatable("gui.cubliminal.room_creator_screen");
    private static final Text CREATE_SELECTION_TEXT = Text.translatable("gui.cubliminal.create_selection");
    private static final Text TEMPLATES_TEXT = Text.translatable("gui.cubliminal.templates");
    private static final Text ROOM_BUILDER_TEXT = Text.translatable("gui.cubliminal.room_builder");
    private static final Text REFRESH_TEXT = Text.translatable("gui.cubliminal.refresh");
    private static final Text CLOSE_TEXT = Text.translatable("gui.cubliminal.close");
    private static final Text STRUCTURE_TOO_TALL_TITLE = Text.translatable("gui.cubliminal.title.too_tall_warning");
    private static final Function<Integer, Text> STRUCTURE_TOO_TALL = height -> Text.translatable("gui.cubliminal.too_tall_warning", height, height);

    private static final Identifier TAB_HEADER_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/tab_header_background.png");
    public static final String SAVES_FOLDER_STRUCTURE = "structure/nbt/";

    private final Screen parent;

    private static final RoomCreatorDataManager DATA_MANAGER = RoomCreatorDataManager.INSTANCE;
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
                 SAVES_FOLDER_STRUCTURE + levelName.getPath() + "/" + name
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
            approximateButton.active = DATA_MANAGER.hasAllData();
            adder.add(approximateButton);

            ButtonWidget applyButton = ButtonWidget
                    .builder(Text.literal("Apply"), button -> this.applyChanges())
                    .size(80, 20).build();
            applyButton.active = DATA_MANAGER.hasAllData();
            adder.add(applyButton);

            ButtonWidget saveButton = ButtonWidget
                    .builder(Text.literal("Save"), button -> this.saveSelection())
                    .size(80, 20).build();
            saveButton.active = DATA_MANAGER.hasAllData();
            adder.add(saveButton);

            this.refresh();
        }

        void refresh() {
            if (DATA_MANAGER.hasCorner1()) {
                BlockPos corner1 = DATA_MANAGER.getCorner1();
                this.x_1.setText(Integer.toString(corner1.getX()));
                this.y_1.setText(Integer.toString(corner1.getY()));
                this.z_1.setText(Integer.toString(corner1.getZ()));
            }
            if (DATA_MANAGER.hasCorner2()) {
                BlockPos corner2 = DATA_MANAGER.getCorner2();
                this.x_2.setText(Integer.toString(corner2.getX()));
                this.y_2.setText(Integer.toString(corner2.getY()));
                this.z_2.setText(Integer.toString(corner2.getZ()));
            }
            if (DATA_MANAGER.hasSelection()) {
                BlockBox selection = DATA_MANAGER.getSelection();
                this.sizeX.setText(Integer.toString(selection.getBlockCountX()));
                this.sizeY.setText(Integer.toString(selection.getBlockCountY()));
                this.sizeZ.setText(Integer.toString(selection.getBlockCountZ()));
            }
        }

        void approximateSelection() {
            if (DATA_MANAGER.hasAllData()) {
                if (DATA_MANAGER.approximateSelection(this.level)) {
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
            if (DATA_MANAGER.hasAllData()) {
                BlockPos.Mutable corner1 = DATA_MANAGER.getCorner1().mutableCopy();
                BlockPos.Mutable corner2 = DATA_MANAGER.getCorner2().mutableCopy();
                if (isIntegerNumValid(this.x_1.getText())) corner1.setX(Integer.parseInt(this.x_1.getText()));
                if (isIntegerNumValid(this.y_1.getText())) corner1.setY(Integer.parseInt(this.y_1.getText()));
                if (isIntegerNumValid(this.z_1.getText())) corner1.setZ(Integer.parseInt(this.z_1.getText()));
                if (isIntegerNumValid(this.x_2.getText())) corner2.setX(Integer.parseInt(this.x_2.getText()));
                if (isIntegerNumValid(this.y_2.getText())) corner2.setY(Integer.parseInt(this.y_2.getText()));
                if (isIntegerNumValid(this.z_2.getText())) corner2.setZ(Integer.parseInt(this.z_2.getText()));
                DATA_MANAGER.setCorner1(corner1.toImmutable());
                DATA_MANAGER.setCorner2(corner2.toImmutable());

                this.refresh();
            }
        }

        void saveSelection() {
            if (DATA_MANAGER.hasAllData() && !this.nameWidget.getText().isBlank()) {
                ClientPlayNetworking.send(new SaveSelectionC2SPayload(
                        RoomCreatorScreen.getSaveFolderPath(this.nameWidget.getText(), this.level),
                        DATA_MANAGER.getStartPos(),
                        DATA_MANAGER.getSelectionDimensions(),
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

            this.grid.add(ButtonWidget.builder(
                    Text.literal("Placement"),
                    button -> {

                    }
            ).size(SEARCH_COLUMN_WIDTH, 20).build(), 5, 4);
        }

        void refresh() {
            this.updateTemplates();
        }

        void updateTemplates() {
            this.listWidget.updateTemplates();
        }

        class TemplateScrollList extends ElementListWidget<TemplateScrollList.Entry> {

            private final Map<Identifier, Vec3i> listedTemplates = new HashMap<>();

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
                this.listedTemplates.forEach((id, size) -> this.addEntry(new Entry(id, size)));
            }

            void updateTemplates() {
                this.listedTemplates.clear();
                this.listedTemplates.putAll(DATA_MANAGER.getStructureTemplates());

                String filter = TemplatesTab.this.searchPatternWidget.getText();
                if (!minecraftStructures) {
                    this.listedTemplates.keySet().removeIf(id -> id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE));
                }
                if (!filter.isBlank()) {
                    this.listedTemplates.keySet().removeIf(id -> !id.toString().contains(filter));
                }

                this.clearEntries();
                this.init();
            }

            class Entry extends ElementListWidget.Entry<Entry> {

                private final ButtonWidget button;

                private final Identifier templateName;
                private Vec3i size;

                public Entry(Identifier templateName, Vec3i size) {
                    this.templateName = templateName;
                    this.size = size;
                    this.button = ButtonWidget.builder(
                                    Util.make(() -> {
                                        String name;
                                        if (!fullNames) {
                                            String[] subStrings = templateName.getPath().split("/");
                                            name = subStrings[subStrings.length - 1];
                                        } else {
                                            name = templateName.toString();
                                        }

                                        return Text.literal(name);
                                    }),
                                    button -> {

                                    }
                            ).tooltip(Tooltip.of(Text.literal(templateName.toString()).formatted(Formatting.GRAY)))
                            .size(TemplateScrollList.this.getRowWidth(), 20).build();
                }

                @Override
                public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                    this.button.setX(x);
                    this.button.setY(y);
                    this.button.render(context, mouseX, mouseY, tickDelta);
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

        private final RoomBuilder roomBuilder = new RoomBuilder();
        private FloorBuilder selectedFloor;
        private ComponentBuilder selectedComponent;

        private final List<ClickableWidget> roomInfoWidgets = new ArrayList<>();
        private CyclingButtonWidget<Level> levelWidget;
        private TextWidget roomSizeText;
        private TextFieldWidget roomSizeXWidget;
        private TextFieldWidget roomSizeZWidget;

        private final List<ClickableWidget> floorEditorWidgets = new ArrayList<>();
        private TextWidget componentSizeText;
        private TextFieldWidget componentSizeXWidget;
        private TextFieldWidget componentSizeZWidget;
        private TextWidget componentPosText;
        private TextFieldWidget componentRelXWidget;
        private TextFieldWidget componentRelZWidget;
        private ComponentTemplateList templateListWidget;
        private TextFieldWidget templateNameWidget;
        private TemplateSuggestorWidget templateSuggestorWidget;
        private TextWidget doorsHeader2;
        private CyclingButtonWidget<Direction> doorDirectionWidget;
        private DoorIndexSlider doorIndexWidget;
        private ButtonWidget addDoorButton2;
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

            TextFieldWidget roomNameWidget = new TextFieldWidget(textRenderer, 120, 18, Text.empty());
            roomNameWidget.setPlaceholder(Text.literal("Room name").formatted(Formatting.GRAY));
            roomNameWidget.setTextPredicate(RoomCreatorScreen::isIdentifierInputValid);
            roomNameWidget.setMaxLength(128);
            this.grid.add(roomNameWidget, 2, 2);

            this.roomSizeText = new TextWidget(Text.literal("Size: (X, Z)"), textRenderer);
            this.roomSizeText.setHeight(16);
            this.roomSizeText.alignLeft();
            this.grid.add(this.roomSizeText, 3, 1);

            this.roomSizeXWidget = smallNumberField(1);
            this.roomSizeXWidget.setChangedListener(t -> this.refreshFloor());
            this.roomSizeZWidget = smallNumberField(1);
            this.roomSizeZWidget.setChangedListener(t -> this.refreshFloor());

            GridWidget sizeBox = new GridWidget().setRowSpacing(0).setColumnSpacing(30);
            sizeBox.add(this.roomSizeXWidget, 1, 1);
            sizeBox.add(this.roomSizeZWidget, 1, 2);
            this.grid.add(sizeBox, 3, 2);

            CyclingButtonWidget<PaddingType> paddingTypeWidget = CyclingButtonWidget
                    .<PaddingType>builder(type -> Text.translatable(type.translatableEntry()))
                    .values(PaddingType.values())
                    .build(0, 0, 120, 18, Text.literal("Padding Type"));
            this.grid.add(paddingTypeWidget, 4, 1);

            TextFieldWidget paddingCountWidget = new TextFieldWidget(textRenderer, 120, 18, Text.empty());
            paddingCountWidget.setPlaceholder(Text.literal("Padding Count").formatted(Formatting.GRAY));
            paddingCountWidget.setTextPredicate(s -> isOptionalNumValid(s, 0));
            this.grid.add(paddingCountWidget, 4, 2);

            ButtonWidget loadRoomButton = ButtonWidget
                    .builder(Text.literal("Load"), b -> this.loadRoom())
                    .size(120, 18).build();
            this.grid.add(loadRoomButton, 5, 1);

            ButtonWidget saveRoomButton = ButtonWidget
                    .builder(Text.literal("Save"), b -> this.saveRoom())
                    .size(120, 18).build();
            this.grid.add(saveRoomButton, 5, 2);

            Positioner bottomPositioner = this.grid.copyPositioner().alignBottom();
            TextWidget componentsHeader = sectionHeader("Components");
            this.grid.add(componentsHeader, 6, 1, 1, 2, bottomPositioner);

            ButtonWidget addComponentButton = ButtonWidget.builder(Text.literal("Add Component"), b -> this.addComponent())
                    .size(120, 18).build();
            ButtonWidget deleteComponentButton = ButtonWidget.builder(Text.literal("Delete Component"), b -> this.deleteSelectedComponent())
                    .size(120, 18).build();

            this.grid.add(addComponentButton, 7, 1);
            this.grid.add(deleteComponentButton, 7, 2);

            TextWidget doorsHeader = sectionHeader("Doors");
            this.grid.add(doorsHeader, 8, 1, 1, 2, bottomPositioner);

            ButtonWidget addDoorButton = ButtonWidget.builder(Text.literal("Add Door"), b -> this.addDoor())
                    .tooltip(Tooltip.of(Text.literal("Door placement coming soon").formatted(Formatting.GRAY)))
                    .size(120, 18).build();
            ButtonWidget deleteDoorButton = ButtonWidget.builder(Text.literal("Delete Door"), b -> this.deleteSelectedDoor())
                    .size(120, 18).build();
            addDoorButton.active = false;
            deleteDoorButton.active = false;

            this.grid.add(addDoorButton, 9, 1);
            this.grid.add(deleteDoorButton, 9, 2);

            this.roomInfoWidgets.addAll(List.of(
                    roomInfoHeader,
                    this.levelWidget,
                    roomNameWidget,
                    roomSizeText,
                    this.roomSizeXWidget,
                    this.roomSizeZWidget,
                    paddingTypeWidget,
                    paddingCountWidget,
                    loadRoomButton,
                    saveRoomButton,
                    componentsHeader,
                    addComponentButton,
                    deleteComponentButton,
                    doorsHeader,
                    addDoorButton,
                    deleteDoorButton
            ));
        }

        void initComponentSection() {
            TextWidget componentsHeader2 = sectionHeader("Components");
            this.grid.add(componentsHeader2, 1, 1, 1, 2);

            ButtonWidget addComponentButton2 = ButtonWidget.builder(Text.literal("Add"), b -> this.addComponent())
                    .size(65, 18).build();
            ButtonWidget deleteComponentButton2 = ButtonWidget.builder(Text.literal("Delete"), b -> this.deleteSelectedComponent())
                    .size(65, 18).build();

            this.grid.add(addComponentButton2, 2, 1);
            this.grid.add(deleteComponentButton2, 2, 2);

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

            ButtonWidget addTemplateButton = ButtonWidget.builder(Text.literal("+"), b -> this.addTemplateName())
                    .size(28, 18).build();

            DirectionalLayoutWidget templateRow = DirectionalLayoutWidget.horizontal().spacing(3);
            Positioner centered = templateRow.copyPositioner().alignVerticalCenter();

            templateRow.add(this.templateNameWidget, centered);
            templateRow.add(addTemplateButton, centered);
            this.grid.add(templateRow, 6, 1, 1, 2);

            Positioner bottomPositioner = this.grid.copyPositioner().alignBottom();
            this.doorsHeader2 = sectionHeader("Doors");
            this.grid.add(this.doorsHeader2, 7, 1, 1, 2, bottomPositioner);

            this.doorDirectionWidget = CyclingButtonWidget.<Direction>builder(dir -> Text.literal(dir.toString().toUpperCase()))
                    .values(Direction.Type.HORIZONTAL.stream().toList())
                    .omitKeyText()
                    .build(0, 0, 65, 18, Text.empty(), (b, d) -> this.refreshDoors());

            this.doorIndexWidget = new DoorIndexSlider(0, 0, 65, 18, Text.empty(), 3, 0);

            this.grid.add(this.doorDirectionWidget, 8, 1);
            this.grid.add(this.doorIndexWidget, 8, 2);

            this.addDoorButton2 = ButtonWidget.builder(Text.literal("Add"), b -> this.addDoor())
                    .size(65, 18).build();
            this.deleteDoorButton2 = ButtonWidget.builder(Text.literal("Delete"), b -> this.deleteSelectedDoor())
                    .size(65, 18).build();
            this.addDoorButton2.active = false;
            this.deleteDoorButton2.active = false;

            this.grid.add(this.addDoorButton2, 9, 1);
            this.grid.add(this.deleteDoorButton2, 9, 2);

            this.templateSuggestorWidget = new TemplateSuggestorWidget(this.templateNameWidget);
            this.templateNameWidget.setChangedListener(s -> this.templateSuggestorWidget.setTemplateName(s));

            this.floorEditorWidgets.addAll(List.of(
                    componentsHeader2,
                    addComponentButton2,
                    deleteComponentButton2,
                    this.componentSizeText,
                    this.componentSizeXWidget,
                    this.componentSizeZWidget,
                    this.componentPosText,
                    this.componentRelXWidget,
                    this.componentRelZWidget,
                    this.templateListWidget,
                    this.templateNameWidget,
                    addTemplateButton,
                    this.doorsHeader2,
                    this.doorDirectionWidget,
                    this.doorIndexWidget,
                    this.addDoorButton2,
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
                    ? this.roomBuilder.getSizeX()
                    : this.roomBuilder.getSizeZ()
            );
        }

        private void refreshFloor() {
            this.roomBuilder.setSizeX(parseOrDefault(this.roomSizeXWidget.getText(), this.roomBuilder.getSizeX()));
            this.roomBuilder.setSizeZ(parseOrDefault(this.roomSizeZWidget.getText(), this.roomBuilder.getSizeZ()));

            this.previewWidget.setFloor(this.selectedFloor);
            this.previewWidget.setBounds(this.roomBuilder.getSizeX(), this.roomBuilder.getSizeZ());
            this.floorLabelWidget.setMessage(this.floorLabelText());

            this.refreshDoors();
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
            if (this.selectedComponent != null) {
                this.templateSuggestorWidget.recalculateTemplateSize(level, this.selectedComponent);
            }
            this.refreshDoors();
        }

        private void addComponent() {
            ComponentBuilder component = this.selectedFloor.addComponent();
            this.previewWidget.select(component);
        }

        private void deleteSelectedComponent() {
            if (this.selectedComponent == null) return;
            this.selectedFloor.removeComponent(this.selectedComponent);
            this.previewWidget.select(null);
        }

        private void onSelectedChange() {
            ComponentBuilder component = this.previewWidget.getSelectedComponent();
            this.selectedComponent = component;
            this.setComponentEditorEnabled(component != null);

            if (component == null) {
                this.templateSuggestorWidget.setVisible(false);
            }

            this.syncFieldsFromComponent();
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
            }
        }

        private void addDoor() {

        }

        private void deleteSelectedDoor() {

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

        private void loadRoom() {

        }

        private void saveRoom() {

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

                    this.nameWidget = new TextWidget(Util.make(() -> {
                        String[] subStrings = Identifier.of(templateName).getPath().split("/");
                        return Text.literal(subStrings[subStrings.length - 1]);
                    }), textRenderer);
                    this.nameWidget.setTooltip(Tooltip.of(Text.literal(templateName).formatted(Formatting.GRAY)));
                    this.nameWidget.setDimensions(nameWidth, 16);
                    this.nameWidget.alignLeft();

                    this.weightWidget = new TextFieldWidget(textRenderer, weightWidth, 16, Text.literal(Float.toString(weight)));
                    this.weightWidget.setTextPredicate(RoomCreatorScreen::isOptionalPositiveFloatValid);
                    this.weightWidget.setChangedListener(this::updateWeight);

                    this.deleteButton = ButtonWidget.builder(
                            Text.literal("x"), b -> this.delete()
                    ).size(deleteWidth, 16).build();

                    this.templateName = templateName;
                }

                private void updateWeight(String newValue) {
                    if (selectedComponent == null) return;
                    ListIterator<Pair<Float, String>> listIterator = selectedComponent.getTemplates().getValues().listIterator();
                    while (listIterator.hasNext()) {
                        Pair<Float, String> pair = listIterator.next();
                        if (pair.getSecond().equals(this.templateName)) {
                            listIterator.set(Pair.of(parseFloatOrDefault(newValue, 0.0f), this.templateName));
                        }
                    }
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

        static class DoorIndexSlider extends SliderWidget {
            private int maxSliderValue;

            public DoorIndexSlider(int x, int y, int width, int height, Text text, int maxSliderValue, double value) {
                super(x, y, width, height, text, value);
                this.maxSliderValue = maxSliderValue;
                this.updateMessage();
            }

            public void setMaxSliderValue(int max) {
                int newValue = MathHelper.clamp(this.getSliderValue(), 0, max);
                this.maxSliderValue = max;
                this.value = (double) newValue / this.maxSliderValue;
            }

            public int getSliderValue() {
                return Math.round((float) this.value * this.maxSliderValue);
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Text.literal(Integer.toString(this.getSliderValue())));
            }

            @Override
            protected void applyValue() {}
        }
    }
}