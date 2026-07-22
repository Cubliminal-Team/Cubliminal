package net.limit.cubliminal.client.screen.roomcreator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.limit.cubliminal.client.screen.roomcreator.data.ComponentData;
import net.limit.cubliminal.client.screen.roomcreator.data.FloorData;
import net.limit.cubliminal.client.screen.roomcreator.data.RoomData;
import net.limit.cubliminal.client.screen.roomcreator.widget.RoomPreviewWidget;
import net.limit.cubliminal.level.Level;
import net.limit.cubliminal.level.Levels;
import net.limit.cubliminal.networking.c2s.SaveSelectionC2SPayload;
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
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
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
    private static final Text STRUCTURE_TOO_TALL_TITLE = Text.translatable("gui.cubliminal.title.too_tall_warning");
    private static final Function<Integer, Text> STRUCTURE_TOO_TALL = height -> Text.translatable("gui.cubliminal.too_tall_warning", height, height);

    private static final Identifier TAB_HEADER_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/tab_header_background.png");
    public static final String SAVES_FOLDER_STRUCTURE = "/structure/nbt/";

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
        this.tabNavigation = TabNavigationWidget.builder(this.tabManager, this.width)
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

    public void updateTemplates() {
        this.templatesTab.updateTemplates();
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

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawTexture(RenderLayer::getGuiTextured, Screen.FOOTER_SEPARATOR_TEXTURE, 0, this.height - this.layout.getFooterHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);
    }

    @Override
    protected void renderDarkening(DrawContext context) {
        context.drawTexture(RenderLayer::getGuiTextured, TAB_HEADER_BACKGROUND_TEXTURE, 0, 0, 0.0F, 0.0F, this.width, this.layout.getHeaderHeight(), 16, 16);
        this.renderDarkening(context, 0, this.layout.getHeaderHeight(), this.width, this.height);
    }

    private static boolean isStringInputValid(String string) {
        return string.isEmpty() || string.equals("-") || string.matches("^-?\\d+$");
    }

    private static boolean isNumValid(String num) {
        return num.matches("^-?\\d+$");
    }

    public static Identifier getSaveFolderPath(String name, Level level) {
        Identifier levelName = level.name;
        return Identifier.of(
                levelName.getNamespace(),
                levelName.getPath() + SAVES_FOLDER_STRUCTURE + name
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (Element element : this.tabNavigation.children()) {
                if (element instanceof TabButtonWidget widget && widget.getTab() instanceof TemplatesTab tab && element.mouseClicked(mouseX, mouseY, button)) {
                    this.updateTemplates();
                    break;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
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

            this.nameWidget = new TextFieldWidget(textRenderer, 240, 20, Text.empty()) {
                @Override
                public boolean charTyped(char chr, int modifiers) {
                    return RoomCreatorScreen.this.isValidCharacterForName(this.getText(), chr, this.getCursor()) && super.charTyped(chr, modifiers);
                }
            };
            this.nameWidget.setMaxLength(128);
            this.nameWidget.setPlaceholder(Text.translatable("structure_block.structure_name").formatted(Formatting.GRAY));
            adder.add(this.nameWidget, 3);

            adder.add(new TextWidget(Text.literal("Corner 1:"), textRenderer), 3);

            this.x_1 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.y_1 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.z_1 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.x_1.setTextPredicate(RoomCreatorScreen::isStringInputValid);
            this.y_1.setTextPredicate(RoomCreatorScreen::isStringInputValid);
            this.z_1.setTextPredicate(RoomCreatorScreen::isStringInputValid);
            adder.add(this.x_1);
            adder.add(this.y_1);
            adder.add(this.z_1);

            adder.add(new TextWidget(Text.literal("Corner 2:"), textRenderer), 3);

            this.x_2 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.y_2 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.z_2 = new TextFieldWidget(textRenderer, 80, 20, Text.empty());
            this.x_2.setTextPredicate(RoomCreatorScreen::isStringInputValid);
            this.y_2.setTextPredicate(RoomCreatorScreen::isStringInputValid);
            this.z_2.setTextPredicate(RoomCreatorScreen::isStringInputValid);
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
                if (isNumValid(this.x_1.getText())) corner1.setX(Integer.parseInt(this.x_1.getText()));
                if (isNumValid(this.y_1.getText())) corner1.setY(Integer.parseInt(this.y_1.getText()));
                if (isNumValid(this.z_1.getText())) corner1.setZ(Integer.parseInt(this.z_1.getText()));
                if (isNumValid(this.x_2.getText())) corner2.setX(Integer.parseInt(this.x_2.getText()));
                if (isNumValid(this.y_2.getText())) corner2.setY(Integer.parseInt(this.y_2.getText()));
                if (isNumValid(this.z_2.getText())) corner2.setZ(Integer.parseInt(this.z_2.getText()));
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

            private final List<Identifier> listedTemplates = new ArrayList<>();

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
                this.listedTemplates.forEach(id -> this.addEntry(new Entry(id)));
            }

            void updateTemplates() {
                this.listedTemplates.clear();
                this.listedTemplates.addAll(DATA_MANAGER.getStructureTemplates());

                String filter = TemplatesTab.this.searchPatternWidget.getText();
                if (!minecraftStructures) {
                    this.listedTemplates.removeIf(id -> id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE));
                }
                if (!filter.isBlank()) {
                    this.listedTemplates.removeIf(id -> !id.toString().contains(filter));
                }

                this.clearEntries();
                this.init();
            }

            class Entry extends ElementListWidget.Entry<Entry> {

                private final ButtonWidget button;

                private final Identifier templateName;
                private Vec3i size = null;

                public Entry(Identifier templateName) {
                    this.templateName = templateName;
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

        private static final int COLUMNS = 6;

        private final RoomData roomData = new RoomData();
        private FloorData selectedFloor;
        private ComponentData selectedComponent;
        private Level level = Levels.LEVEL_0;

        private RoomPreviewWidget previewWidget;
        private TextFieldWidget roomNameWidget;
        private TextFieldWidget boundsXWidget;
        private TextFieldWidget boundsYWidget;
        private TextFieldWidget boundsZWidget;
        private TextWidget floorLabelWidget;

        private TextFieldWidget componentWidthWidget;
        private TextFieldWidget componentLengthWidget;
        private TextFieldWidget componentRelXWidget;
        private TextFieldWidget componentRelZWidget;
        private TextFieldWidget newTemplateNameWidget;
        private TextWidget templateListWidget;

        public RoomBuilderTab() {
            super(ROOM_BUILDER_TEXT);
            this.selectedFloor = this.roomData.addFloor();
            this.init();
        }

        void init() {
            GridWidget.Adder adder = this.grid.setRowSpacing(4).setColumnSpacing(5).createAdder(COLUMNS);

            adder.add(sectionHeader("Room Info"), COLUMNS);

            adder.add(CyclingButtonWidget.builder(Level::getTranslatableName)
                    .values(Levels.ALL)
                    .build(0, 0, 90, 20, Text.literal("Level"), (button, value) -> this.level = value));

            this.roomNameWidget = new TextFieldWidget(textRenderer, 140, 20, Text.empty());
            this.roomNameWidget.setPlaceholder(Text.literal("Room name").formatted(Formatting.GRAY));
            this.roomNameWidget.setMaxLength(128);
            adder.add(this.roomNameWidget, 3);

            adder.add(new TextWidget(Text.literal("Bounds:"), textRenderer));
            this.boundsXWidget = numberField();
            adder.add(this.boundsXWidget);

            this.boundsYWidget = numberField();
            this.boundsZWidget = numberField();
            this.boundsXWidget.setText("16");
            this.boundsYWidget.setText("8");
            this.boundsZWidget.setText("16");
            adder.add(this.boundsYWidget);
            adder.add(this.boundsZWidget);

            // no doordata model yet, just placeholder
            adder.add(new TextWidget(Text.literal("Doors:"), textRenderer));
            ButtonWidget addDoorButton = ButtonWidget.builder(Text.literal("Add Door"), b -> {})
                    .tooltip(Tooltip.of(Text.literal("Door placement isn't wired up yet").formatted(Formatting.GRAY)))
                    .size(90, 20).build();
            addDoorButton.active = false;
            adder.add(addDoorButton);

            adder.add(sectionHeader("Floor"), COLUMNS);

            this.floorLabelWidget = new TextWidget(this.floorLabelText(), textRenderer);
            adder.add(this.floorLabelWidget, 2);
            adder.add(ButtonWidget.builder(Text.literal("<"), b -> this.cycleFloor(-1)).size(20, 20).build());
            adder.add(ButtonWidget.builder(Text.literal(">"), b -> this.cycleFloor(1)).size(20, 20).build());
            adder.add(ButtonWidget.builder(Text.literal("Add Floor"), b -> this.addFloor()).size(90, 20).build());
            adder.add(ButtonWidget.builder(Text.literal("Remove Floor"), b -> this.removeFloor()).size(90, 20).build());

            this.previewWidget = new RoomPreviewWidget(0, 0, 240, 120);
            this.previewWidget.setSelectionListener(this::onComponentSelected);
            this.previewWidget.setDragListener(this::syncFieldsFromComponent);
            adder.add(this.previewWidget, 4);
            adder.add(blank(), 2);

            adder.add(sectionHeader("Components"), COLUMNS);

            adder.add(ButtonWidget.builder(Text.literal("Add Component"), b -> this.addComponent()).size(100, 20).build(), 2);

            adder.add(new TextWidget(Text.literal("Size X:"), textRenderer));
            this.componentWidthWidget = numberField();
            this.componentWidthWidget.setChangedListener(t -> this.applyComponentFields());
            adder.add(this.componentWidthWidget);

            adder.add(new TextWidget(Text.literal("Size Z:"), textRenderer));
            this.componentLengthWidget = numberField();
            this.componentLengthWidget.setChangedListener(t -> this.applyComponentFields());
            adder.add(this.componentLengthWidget);

            adder.add(new TextWidget(Text.literal("Rel X:"), textRenderer));
            this.componentRelXWidget = numberField();
            this.componentRelXWidget.setChangedListener(t -> this.applyComponentFields());
            adder.add(this.componentRelXWidget);

            adder.add(new TextWidget(Text.literal("Rel Z:"), textRenderer));
            this.componentRelZWidget = numberField();
            this.componentRelZWidget.setChangedListener(t -> this.applyComponentFields());
            adder.add(this.componentRelZWidget);

            this.newTemplateNameWidget = new TextFieldWidget(textRenderer, 120, 20, Text.empty());
            this.newTemplateNameWidget.setPlaceholder(Text.literal("Template name").formatted(Formatting.GRAY));
            adder.add(this.newTemplateNameWidget, 2);
            adder.add(ButtonWidget.builder(Text.literal("Add Template"), b -> this.addTemplateName()).size(90, 20).build());

            this.templateListWidget = new TextWidget(Text.empty(), textRenderer);
            adder.add(this.templateListWidget, COLUMNS);

            adder.add(ButtonWidget.builder(Text.literal("Delete Component"), b -> this.deleteSelectedComponent()).size(115, 20).build(), 2);
            adder.add(ButtonWidget.builder(Text.literal("Save as Template"), b -> this.saveAsTemplate()).size(115, 20).build(), 2);
            adder.add(ButtonWidget.builder(Text.literal("Save Room"), b -> this.saveRoom()).size(115, 20).build(), 2);

            this.refreshFloor();
            this.setComponentEditorEnabled(false);
        }

        private static TextWidget sectionHeader(String text) {
            return new TextWidget(Text.literal(text).formatted(Formatting.BOLD, Formatting.YELLOW), MinecraftClient.getInstance().textRenderer)
                    .alignCenter();
        }

        private static TextWidget blank() {
            return new TextWidget(Text.empty(), MinecraftClient.getInstance().textRenderer);
        }

        private static TextFieldWidget numberField() {
            TextFieldWidget field = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, 50, 20, Text.empty());
            field.setTextPredicate(RoomCreatorScreen::isStringInputValid);
            return field;
        }

        private Text floorLabelText() {
            int index = this.roomData.getFloors().indexOf(this.selectedFloor) + 1;
            return Text.literal("Floor " + index + " / " + this.roomData.getFloors().size());
        }

        private void refreshFloor() {
            this.previewWidget.setFloor(this.selectedFloor);
            this.previewWidget.setBounds(
                    parseOrDefault(this.boundsXWidget.getText(), 16),
                    parseOrDefault(this.boundsZWidget.getText(), 16)
            );
            this.floorLabelWidget.setMessage(this.floorLabelText());
        }

        private void cycleFloor(int direction) {
            List<FloorData> floors = this.roomData.getFloors();
            int index = floors.indexOf(this.selectedFloor);
            index = Math.floorMod(index + direction, floors.size());
            this.selectedFloor = floors.get(index);
            this.onComponentSelected(null);
            this.refreshFloor();
        }

        private void addFloor() {
            this.selectedFloor = this.roomData.addFloor();
            this.onComponentSelected(null);
            this.refreshFloor();
        }

        private void removeFloor() {
            if (this.roomData.getFloors().size() <= 1) return;
            this.roomData.removeFloor(this.selectedFloor);
            this.selectedFloor = this.roomData.getFloors().get(0);
            this.onComponentSelected(null);
            this.refreshFloor();
        }

        private void addComponent() {
            ComponentData component = this.selectedFloor.addComponent();
            this.previewWidget.select(component);
            this.onComponentSelected(component);
        }

        private void deleteSelectedComponent() {
            if (this.selectedComponent == null) return;
            this.selectedFloor.removeComponent(this.selectedComponent);
            this.onComponentSelected(null);
        }

        private void onComponentSelected(ComponentData component) {
            this.selectedComponent = component;
            this.setComponentEditorEnabled(component != null);
            this.syncFieldsFromComponent();
        }

        private void syncFieldsFromComponent() {
            ComponentData c = this.selectedComponent;
            if (c != null) {
                this.componentWidthWidget.setText(Integer.toString(c.getWidth()));
                this.componentLengthWidget.setText(Integer.toString(c.getLength()));
                this.componentRelXWidget.setText(Integer.toString(c.getRelX()));
                this.componentRelZWidget.setText(Integer.toString(c.getRelZ()));
                this.templateListWidget.setMessage(Text.literal(String.join(", ", c.getTemplateNames())));
            } else {
                this.componentWidthWidget.setText("");
                this.componentLengthWidget.setText("");
                this.componentRelXWidget.setText("");
                this.componentRelZWidget.setText("");
                this.templateListWidget.setMessage(Text.empty());
            }
        }

        private void setComponentEditorEnabled(boolean enabled) {
            this.componentWidthWidget.setEditable(enabled);
            this.componentLengthWidget.setEditable(enabled);
            this.componentRelXWidget.setEditable(enabled);
            this.componentRelZWidget.setEditable(enabled);
            this.newTemplateNameWidget.setEditable(enabled);
        }

        private void addTemplateName() {
            if (this.selectedComponent == null) return;
            String name = this.newTemplateNameWidget.getText().trim();
            if (!name.isBlank()) {
                this.selectedComponent.getTemplateNames().add(name);
                this.newTemplateNameWidget.setText("");
                this.syncFieldsFromComponent();
            }
        }

        private void applyComponentFields() {
            if (this.selectedComponent == null) return;
            this.selectedComponent.setWidth(Math.max(1, parseOrDefault(this.componentWidthWidget.getText(), this.selectedComponent.getWidth())));
            this.selectedComponent.setLength(Math.max(1, parseOrDefault(this.componentLengthWidget.getText(), this.selectedComponent.getLength())));
            this.selectedComponent.setRelX(Math.max(0, parseOrDefault(this.componentRelXWidget.getText(), this.selectedComponent.getRelX())));
            this.selectedComponent.setRelZ(Math.max(0, parseOrDefault(this.componentRelZWidget.getText(), this.selectedComponent.getRelZ())));
        }

        private void saveRoom() {
            this.applyComponentFields();
            this.roomData.setName(this.roomNameWidget.getText());
            this.roomData.setBoundingBox(
                    parseOrDefault(this.boundsXWidget.getText(), 16),
                    parseOrDefault(this.boundsYWidget.getText(), 8),
                    parseOrDefault(this.boundsZWidget.getText(), 16)
            );

            if (this.roomData.getName().isBlank()) return;

            // TODO: give either this.roomData or this.roomData.toJson() to the networking + codec
            // `this.level` is available here if the payload needs it
        }

        private void saveAsTemplate() {
            this.applyComponentFields();
            this.roomData.setName(this.roomNameWidget.getText());
            this.roomData.setBoundingBox(
                    parseOrDefault(this.boundsXWidget.getText(), 16),
                    parseOrDefault(this.boundsYWidget.getText(), 8),
                    parseOrDefault(this.boundsZWidget.getText(), 16)
            );

            if (this.roomData.getName().isBlank()) return;

            // TODO: same hand off as saveRoom(), but intended for a reusable room template store
            // rather than a finalized per level room. give this to whatever endpoint/codec
            // knows templates from saved rooms
        }

        private static int parseOrDefault(String text, int fallback) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return fallback;
            }
        }
    }
}