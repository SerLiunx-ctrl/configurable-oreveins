package com.serliunx.configurableoreveins.client.gui;

import com.serliunx.configurableoreveins.client.ClientNearbyVeinCache;
import com.serliunx.configurableoreveins.client.ClientLocatorState;
import com.serliunx.configurableoreveins.item.VeinLocatorItem;
import com.serliunx.configurableoreveins.network.LocatorVeinInfo;
import com.serliunx.configurableoreveins.network.MarkVeinMinedMessage;
import com.serliunx.configurableoreveins.network.NetworkHandler;
import com.serliunx.configurableoreveins.network.SetLocatorTargetMessage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Mouse;

/**
 * 矿脉定位器客户端界面。
 *
 * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
 * @version 0.0.1
 * @since 2026/3/7
*/
public class VeinLocatorGui extends GuiScreen {

    private static final int BUTTON_CLEAR_ID = 0;
    private static final int BUTTON_DONE_ID = 1;
    private static final int BUTTON_SEARCH_ID = 2;
    private static final int BUTTON_SORT_ID = 3;
    private static final int BUTTON_MARK_BASE_ID = 3000;
    private static final int VISIBLE_ROWS = 6;
    private static final int ROW_HEIGHT = 24;
    private static final int LIST_MARGIN = 10;
    private static final int SCROLLBAR_WIDTH = 10;
    private static final int SCROLLBAR_GAP = 6;
    private static final int FOOTER_HEIGHT = 62;
    private static final int BUTTON_Y_OFFSET = 26;
    private static final int HEADER_HEIGHT = 48;

    private final EnumHand hand;
    private final int rangeChunks;
    private final List<LocatorVeinInfo> allVeins;
    private final List<LocatorVeinInfo> visibleVeins = new ArrayList<LocatorVeinInfo>();
    private final List<GuiButton> veinButtons = new ArrayList<GuiButton>();
    private final List<GuiButton> markButtons = new ArrayList<GuiButton>();
    private int scrollOffset = 0;
    private int guiLeft;
    private int guiTop;
    private final int xSize = 248;
    private final int ySize = 270;
    private boolean draggingScrollBar;
    private GuiTextField searchField;
    private SortMode sortMode = SortMode.DISTANCE_ASC;

    /**
     * 构造 VeinLocatorGui 实例。
     *
     * @param handOrdinal 参数 handOrdinal。
     * @param rangeChunks 参数 rangeChunks。
     * @param veins 参数 veins。
    */
    public VeinLocatorGui(int handOrdinal, int rangeChunks, List<LocatorVeinInfo> veins) {
        this.hand = EnumHand.values()[Math.max(0, Math.min(EnumHand.values().length - 1, handOrdinal))];
        this.rangeChunks = rangeChunks;
        this.allVeins = new ArrayList<LocatorVeinInfo>(veins);
        this.visibleVeins.addAll(veins);
    }

    /** 执行 initGui 逻辑。 */
    @Override
    public void initGui() {
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;
        searchField = new GuiTextField(2000, fontRenderer, guiLeft + 10, guiTop + 24, 136, 18);
        searchField.setMaxStringLength(48);
        searchField.setCanLoseFocus(true);
        searchField.setFocused(false);
        refreshVisibleVeins();
    }

    /** 更新 Screen。 */
    @Override
    public void updateScreen() {
        super.updateScreen();
        searchField.updateCursorCounter();
    }

    /**
     * 执行 actionPerformed 逻辑。
     *
     * @param button 参数 button。
     * @throws IOException 执行过程中可能抛出的异常。
    */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == BUTTON_CLEAR_ID) {
            ClientLocatorState.setHighlightedVein(null);
            ItemStack stack = mc.player.getHeldItem(hand);
            if (!stack.isEmpty()) {
                VeinLocatorItem.clearTarget(stack);
                mc.player.inventory.markDirty();
            }
            NetworkHandler.CHANNEL.sendToServer(SetLocatorTargetMessage.clear(hand));
            mc.displayGuiScreen(null);
            return;
        }

        if (button.id == BUTTON_DONE_ID) {
            mc.displayGuiScreen(null);
            return;
        }

        if (button.id == BUTTON_SEARCH_ID) {
            searchField.setFocused(true);
            refreshVisibleVeins();
            return;
        }

        if (button.id == BUTTON_SORT_ID) {
            sortMode = sortMode.next();
            refreshVisibleVeins();
            return;
        }

        if (button.id >= 1000) {
            if (button.id >= BUTTON_MARK_BASE_ID) {
                int index = button.id - BUTTON_MARK_BASE_ID;
                toggleVeinMined(index);
                return;
            }
            int index = button.id - 1000;
            selectVein(index);
        }
    }

    /**
     * 绘制 Screen。
     *
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
     * @param partialTicks 参数 partialTicks。
    */
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawGradientRect(guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, 0xD0101010, 0xD01A1A1A);
        Gui.drawRect(guiLeft + 1, guiTop + 1, guiLeft + xSize - 1, guiTop + HEADER_HEIGHT, 0xAA2A2A2A);
        Gui.drawRect(
                guiLeft + 1,
                guiTop + HEADER_HEIGHT + 1,
                guiLeft + xSize - 1,
                guiTop + ySize - FOOTER_HEIGHT,
                0x66000000);
        Gui.drawRect(
                guiLeft + 1,
                guiTop + ySize - FOOTER_HEIGHT + 1,
                guiLeft + xSize - 1,
                guiTop + ySize - 1,
                0x88303030);
        Gui.drawRect(guiLeft, guiTop, guiLeft + xSize, guiTop + 1, 0x90909090);
        Gui.drawRect(guiLeft, guiTop + ySize - 1, guiLeft + xSize, guiTop + ySize, 0x90909090);
        Gui.drawRect(guiLeft, guiTop, guiLeft + 1, guiTop + ySize, 0x90909090);
        Gui.drawRect(guiLeft + xSize - 1, guiTop, guiLeft + xSize, guiTop + ySize, 0x90909090);
        drawCenteredString(
                fontRenderer,
                tr("gui.configurableoreveins.vein_locator.title"),
                guiLeft + (xSize / 2),
                guiTop + 7,
                0xE0E0E0);
        fontRenderer.drawString(
                tr("gui.configurableoreveins.vein_locator.search_label"),
                guiLeft + 10,
                guiTop + 15,
                0xC8C8C8);
        fontRenderer.drawString(
                tr("gui.configurableoreveins.vein_locator.sort_label"),
                guiLeft + 176,
                guiTop + 15,
                0xC8C8C8);
        fontRenderer.drawString(
                tr("gui.configurableoreveins.vein_locator.range", rangeChunks),
                guiLeft + 10,
                guiTop + ySize - 54,
                0xE0E0E0);
        fontRenderer.drawString(
                tr("gui.configurableoreveins.vein_locator.found", visibleVeins.size()),
                guiLeft + 10,
                guiTop + ySize - 42,
                0xE0E0E0);
        drawScrollBar();

        if (visibleVeins.isEmpty()) {
            drawCenteredString(
                    fontRenderer,
                    tr("gui.configurableoreveins.vein_locator.empty"),
                    guiLeft + (xSize / 2),
                    guiTop + 88,
                    0xB0B0B0);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawVeinIcons();
        searchField.drawTextBox();
        drawHoveredVeinTooltip(mouseX, mouseY);
    }

    /**
     * 执行 doesGuiPauseGame 逻辑。
     *
     * @return 处理结果。
    */
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * 执行 handleMouseInput 逻辑。
     *
     * @throws IOException 执行过程中可能抛出的异常。
    */
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel > 0) {
            setScrollOffset(scrollOffset - 1);
        } else if (wheel < 0) {
            setScrollOffset(scrollOffset + 1);
        }
    }

    /**
     * 执行 mouseClicked 逻辑。
     *
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
     * @param mouseButton 参数 mouseButton。
     * @throws IOException 执行过程中可能抛出的异常。
    */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        searchField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0 && isInsideScrollTrack(mouseX, mouseY)) {
            draggingScrollBar = true;
            updateScrollFromMouse(mouseY);
        }
    }

    /**
     * 执行 mouseClickMove 逻辑。
     *
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
     * @param clickedMouseButton 参数 clickedMouseButton。
     * @param timeSinceLastClick 参数 timeSinceLastClick。
    */
    @Override
    protected void mouseClickMove(
            int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (draggingScrollBar && clickedMouseButton == 0) {
            updateScrollFromMouse(mouseY);
        }
    }

    /**
     * 执行 mouseReleased 逻辑。
     *
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
     * @param state 参数 state。
    */
    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingScrollBar = false;
    }

    /**
     * 执行 keyTyped 逻辑。
     *
     * @param typedChar 参数 typedChar。
     * @param keyCode 参数 keyCode。
     * @throws IOException 执行过程中可能抛出的异常。
    */
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (searchField.textboxKeyTyped(typedChar, keyCode)) {
            refreshVisibleVeins();
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    /** 执行 rebuildButtons 逻辑。 */
    private void rebuildButtons() {
        buttonList.clear();
        veinButtons.clear();
        markButtons.clear();

        int startIndex = scrollOffset;
        int endIndex = Math.min(startIndex + VISIBLE_ROWS, visibleVeins.size());
        int entryWidth = getEntryButtonWidth();
        int markButtonX = getListLeft() + entryWidth + 4;
        for (int index = startIndex; index < endIndex; index++) {
            int row = index - startIndex;
            GuiButton button =
                    new GuiButton(
                            1000 + index,
                            getListLeft(),
                            getListTop() + (row * ROW_HEIGHT),
                            entryWidth,
                            20,
                            buildVeinLabel(visibleVeins.get(index)));
            veinButtons.add(button);
            buttonList.add(button);
            GuiButton markButton =
                    new GuiButton(
                            BUTTON_MARK_BASE_ID + index,
                            markButtonX,
                            getListTop() + (row * ROW_HEIGHT),
                            40,
                            20,
                            getMarkButtonLabel(visibleVeins.get(index)));
            markButtons.add(markButton);
            buttonList.add(markButton);
        }

        int buttonY = guiTop + ySize - BUTTON_Y_OFFSET;
        buttonList.add(
                new GuiButton(
                        BUTTON_SEARCH_ID,
                        guiLeft + 150,
                        guiTop + 23,
                        54,
                        20,
                        tr("gui.configurableoreveins.vein_locator.search_button")));
        buttonList.add(
                new GuiButton(BUTTON_SORT_ID, guiLeft + 206, guiTop + 23, 32, 20, getSortButtonLabel()));
        buttonList.add(
                new GuiButton(
                        BUTTON_CLEAR_ID,
                        guiLeft + 10,
                        buttonY,
                        90,
                        20,
                        tr("gui.configurableoreveins.vein_locator.clear")));
        buttonList.add(
                new GuiButton(
                        BUTTON_DONE_ID,
                        guiLeft + xSize - 100,
                        buttonY,
                        90,
                        20,
                        tr("gui.configurableoreveins.vein_locator.done")));
    }

    /**
     * 获取 MaxScrollOffset。
     *
     * @return 处理结果。
    */
    private int getMaxScrollOffset() {
        return Math.max(0, visibleVeins.size() - VISIBLE_ROWS);
    }

    /**
     * 设置 ScrollOffset。
     *
     * @param offset 参数 offset。
    */
    private void setScrollOffset(int offset) {
        int clamped = Math.max(0, Math.min(getMaxScrollOffset(), offset));
        if (clamped != scrollOffset) {
            scrollOffset = clamped;
            rebuildButtons();
        }
    }

    /**
     * 构建 VeinLabel。
     *
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    private String buildVeinLabel(LocatorVeinInfo vein) {
        int horizontalDistance = getHorizontalDistance(vein);
        String suffix = "  [" + horizontalDistance + "m]";
        String prefix =
                vein.isMined() ? tr("gui.configurableoreveins.vein_locator.mined_prefix") + " " : "";
        String name = fontRenderer.trimStringToWidth(prefix + vein.getName(), 112);
        return name + suffix;
    }

    /**
     * 切换矿脉的已挖掘状态。
     *
     * @param index 参数 index。
    */
    private void toggleVeinMined(int index) {
        if (index < 0 || index >= visibleVeins.size()) {
            return;
        }
        LocatorVeinInfo target = visibleVeins.get(index);
        boolean mined = !target.isMined();
        ClientNearbyVeinCache.setMined(
                target.getDimensionId(), target.getChunkX(), target.getChunkZ(), target.getVeinHash(), mined);
        if (mined) {
            LocatorVeinInfo highlighted = ClientLocatorState.getHighlightedVein();
            if (highlighted != null
                    && highlighted.getDimensionId() == target.getDimensionId()
                    && highlighted.getChunkX() == target.getChunkX()
                    && highlighted.getChunkZ() == target.getChunkZ()) {
                ClientLocatorState.setHighlightedVein(null);
            }
        }
        updateLocalVeinState(target, mined);
        refreshVisibleVeins();
        NetworkHandler.CHANNEL.sendToServer(MarkVeinMinedMessage.set(hand, target, mined));
    }

    /**
     * 选择 Vein。
     *
     * @param index 参数 index。
    */
    private void selectVein(int index) {
        if (index < 0 || index >= visibleVeins.size()) {
            return;
        }
        LocatorVeinInfo selected = visibleVeins.get(index);
        if (selected.isMined()) {
            return;
        }
        ClientLocatorState.setHighlightedVein(selected);
        ItemStack stack = mc.player.getHeldItem(hand);
        if (!stack.isEmpty()) {
            VeinLocatorItem.setTarget(
                    stack,
                    selected.getDimensionId(),
                    selected.getCenterX(),
                    selected.getCenterY(),
                    selected.getCenterZ(),
                    selected.getName(),
                    selected.getHighlightColor());
            mc.player.inventory.markDirty();
        }
        NetworkHandler.CHANNEL.sendToServer(SetLocatorTargetMessage.set(hand, selected));
        mc.displayGuiScreen(null);
    }

    /**
     * 获取 ListLeft。
     *
     * @return 处理结果。
    */
    private int getListLeft() {
        return guiLeft + LIST_MARGIN;
    }

    /**
     * 获取 ListTop。
     *
     * @return 处理结果。
    */
    private int getListTop() {
        return guiTop + HEADER_HEIGHT + 10;
    }

    /**
     * 获取 ListWidth。
     *
     * @return 处理结果。
    */
    private int getListWidth() {
        return xSize - (LIST_MARGIN * 2) - SCROLLBAR_WIDTH - SCROLLBAR_GAP;
    }

    /**
     * 获取矿脉条目按钮宽度。
     *
     * @return 处理结果。
    */
    private int getEntryButtonWidth() {
        return getListWidth() - 44;
    }

    /**
     * 获取矿脉状态按钮文案。
     *
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    private String getMarkButtonLabel(LocatorVeinInfo vein) {
        return vein.isMined()
                ? tr("gui.configurableoreveins.vein_locator.unmark_mined")
                : tr("gui.configurableoreveins.vein_locator.mark_mined");
    }

    /**
     * 获取 TrackLeft。
     *
     * @return 处理结果。
    */
    private int getTrackLeft() {
        return getListLeft() + getListWidth() + SCROLLBAR_GAP;
    }

    /**
     * 获取 TrackTop。
     *
     * @return 处理结果。
    */
    private int getTrackTop() {
        return getListTop();
    }

    /**
     * 获取 TrackHeight。
     *
     * @return 处理结果。
    */
    private int getTrackHeight() {
        return (VISIBLE_ROWS * ROW_HEIGHT) - 4;
    }

    /**
     * 判断 InsideScrollTrack。
     *
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
     * @return 处理结果。
    */
    private boolean isInsideScrollTrack(int mouseX, int mouseY) {
        return mouseX >= getTrackLeft()
                && mouseX < getTrackLeft() + SCROLLBAR_WIDTH
                && mouseY >= getTrackTop()
                && mouseY < getTrackTop() + getTrackHeight();
    }

    /**
     * 更新 ScrollFromMouse。
     *
     * @param mouseY 参数 mouseY。
    */
    private void updateScrollFromMouse(int mouseY) {
        int maxOffset = getMaxScrollOffset();
        if (maxOffset <= 0) {
            setScrollOffset(0);
            return;
        }
        int trackTop = getTrackTop();
        int trackHeight = getTrackHeight();
        int thumbHeight = getThumbHeight();
        int movable = Math.max(1, trackHeight - thumbHeight);
        int centeredY = mouseY - trackTop - (thumbHeight / 2);
        double ratio = Math.max(0.0D, Math.min(1.0D, centeredY / (double) movable));
        setScrollOffset((int) Math.round(ratio * maxOffset));
    }

    /**
     * 获取 ThumbHeight。
     *
     * @return 处理结果。
    */
    private int getThumbHeight() {
        if (visibleVeins.isEmpty()) {
            return getTrackHeight();
        }
        int height =
                (int)
                        Math.round(
                                (VISIBLE_ROWS / (double) Math.max(VISIBLE_ROWS, visibleVeins.size()))
                                       * getTrackHeight());
        return Math.max(18, Math.min(getTrackHeight(), height));
    }

    /** 绘制 ScrollBar。 */
    private void drawScrollBar() {
        int trackLeft = getTrackLeft();
        int trackTop = getTrackTop();
        int trackHeight = getTrackHeight();
        int trackRight = trackLeft + SCROLLBAR_WIDTH;
        Gui.drawRect(trackLeft, trackTop, trackRight, trackTop + trackHeight, 0x66000000);
        Gui.drawRect(trackLeft, trackTop, trackRight, trackTop + 1, 0x60FFFFFF);
        Gui.drawRect(trackLeft, trackTop, trackLeft + 1, trackTop + trackHeight, 0x60FFFFFF);
        Gui.drawRect(trackRight - 1, trackTop, trackRight, trackTop + trackHeight, 0x60000000);
        Gui.drawRect(
                trackLeft, trackTop + trackHeight - 1, trackRight, trackTop + trackHeight, 0x60000000);
        int thumbHeight = getThumbHeight();
        int thumbTop = trackTop;
        int maxOffset = getMaxScrollOffset();
        if (maxOffset > 0) {
            thumbTop +=
                    (int) Math.round((scrollOffset / (double) maxOffset) * (trackHeight - thumbHeight));
        }
        Gui.drawRect(trackLeft + 1, thumbTop, trackRight - 1, thumbTop + thumbHeight, 0xB0A0A0A0);
        Gui.drawRect(trackLeft + 1, thumbTop, trackRight - 1, thumbTop + 1, 0xB0FFFFFF);
        Gui.drawRect(trackLeft + 1, thumbTop, trackLeft + 2, thumbTop + thumbHeight, 0xB0FFFFFF);
        Gui.drawRect(trackRight - 2, thumbTop, trackRight - 1, thumbTop + thumbHeight, 0xB0505050);
        Gui.drawRect(
                trackLeft + 1,
                thumbTop + thumbHeight - 1,
                trackRight - 1,
                thumbTop + thumbHeight,
                0xB0505050);
    }

    /** 绘制 VeinIcons。 */
    private void drawVeinIcons() {
        if (mc == null || mc.getRenderItem() == null) {
            return;
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (int row = 0; row < veinButtons.size(); row++) {
            int veinIndex = scrollOffset + row;
            if (veinIndex < 0 || veinIndex >= visibleVeins.size()) {
                continue;
            }
            ItemStack iconStack = createIconStack(visibleVeins.get(veinIndex));
            if (iconStack.isEmpty()) {
                continue;
            }
            GuiButton button = veinButtons.get(row);
            mc.getRenderItem().renderItemAndEffectIntoGUI(iconStack, button.x + 4, button.y + 2);
            mc.getRenderItem()
                    .renderItemOverlayIntoGUI(fontRenderer, iconStack, button.x + 4, button.y + 2, "");
        }
        RenderHelper.disableStandardItemLighting();
    }

    /**
     * 绘制 HoveredVeinTooltip。
     *
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
    */
    private void drawHoveredVeinTooltip(int mouseX, int mouseY) {
        LocatorVeinInfo hovered = getHoveredVein(mouseX, mouseY);
        if (hovered == null) {
            return;
        }
        List<TooltipOreLine> oreLines = buildTooltipOreLines(hovered);
        List<String> headerLines = new ArrayList<>();
        headerLines.add(hovered.getName());
        headerLines.add(
                TextFormatting.GRAY
                        + tr("gui.configurableoreveins.vein_locator.tooltip.size", hovered.getTotalBlocks()));
        if (!oreLines.isEmpty()) {
            headerLines.add(
                    TextFormatting.GRAY
                            + tr("gui.configurableoreveins.vein_locator.tooltip.ore_stats"));
        } else {
            headerLines.add(
                    TextFormatting.GRAY
                            + tr("gui.configurableoreveins.vein_locator.tooltip.ore_stats_unavailable"));
        }
        final int textLineHeight = 10;
        final int oreLineHeight = 18;
        final int oreTextOffsetX = 20;
        final int oreSectionGap = oreLines.isEmpty() ? 0 : 4;

        int width = 0;
        for (String line : headerLines) {
            width = Math.max(width, fontRenderer.getStringWidth(line));
        }
        for (TooltipOreLine line : oreLines) {
            width = Math.max(width, oreTextOffsetX + fontRenderer.getStringWidth(line.label));
        }

        int x = mouseX + 12;
        int y = mouseY - 12;
        int height = 8;
        if (!headerLines.isEmpty()) {
            height += 2 + ((headerLines.size() - 1) * textLineHeight);
        }
        if (!oreLines.isEmpty()) {
            height += oreSectionGap + (oreLines.size() * oreLineHeight);
        }

        if (x + width > this.width) {
            x -= 28 + width;
        }
        if (y + height > this.height) {
            y = this.height - height - 6;
        }
        if (y < 6) {
            y = 6;
        }
        if (x < 6) {
            x = 6;
        }

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableRescaleNormal();
        this.zLevel = 300.0F;
        itemRender.zLevel = 300.0F;
        drawVanillaTooltipBackground(x, y, width, height);

        int cursorY = y;
        for (int index = 0; index < headerLines.size(); index++) {
            String line = headerLines.get(index);
            fontRenderer.drawStringWithShadow(line, x, cursorY, 0xFFFFFFFF);
            if (index == 0) {
                cursorY += 2;
            }
            cursorY += textLineHeight;
        }

        if (!oreLines.isEmpty() && mc != null && mc.getRenderItem() != null) {
            cursorY += oreSectionGap;
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableDepth();
            for (TooltipOreLine line : oreLines) {
                if (!line.stack.isEmpty()) {
                    mc.getRenderItem().renderItemAndEffectIntoGUI(line.stack, x + 2, cursorY - 4);
                    mc.getRenderItem()
                            .renderItemOverlayIntoGUI(fontRenderer, line.stack, x + 2, cursorY - 4, "");
                }
                fontRenderer.drawStringWithShadow(line.label, x + oreTextOffsetX, cursorY + 1, 0xFFFFFFFF);
                cursorY += oreLineHeight;
            }
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
        }
        itemRender.zLevel = 0.0F;
        this.zLevel = 0.0F;
        GlStateManager.enableDepth();
    }

    /** 绘制原版样式的 tooltip 背景。 */
    private void drawVanillaTooltipBackground(int x, int y, int width, int height) {
        int background = 0xF0100010;
        int borderStart = 0x505000FF;
        int borderEnd = 0x5028007F;
        drawGradientRect(x - 3, y - 4, x + width + 3, y - 3, background, background);
        drawGradientRect(x - 3, y + height + 3, x + width + 3, y + height + 4, background, background);
        drawGradientRect(x - 3, y - 3, x + width + 3, y + height + 3, background, background);
        drawGradientRect(x - 4, y - 3, x - 3, y + height + 3, background, background);
        drawGradientRect(x + width + 3, y - 3, x + width + 4, y + height + 3, background, background);
        drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, borderStart, borderEnd);
        drawGradientRect(
                x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, borderStart, borderEnd);
        drawGradientRect(x - 3, y - 3, x + width + 3, y - 3 + 1, borderStart, borderStart);
        drawGradientRect(x - 3, y + height + 2, x + width + 3, y + height + 3, borderEnd, borderEnd);
    }

    /** 执行 refreshVisibleVeins 逻辑。 */
    private void refreshVisibleVeins() {
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        visibleVeins.clear();
        for (LocatorVeinInfo vein : allVeins) {
            if (query.isEmpty() || vein.getName().toLowerCase(Locale.ROOT).contains(query)) {
                visibleVeins.add(vein);
            }
        }

        Collections.sort(
                visibleVeins,
                new Comparator<LocatorVeinInfo>() {
                    @Override
                    public int compare(LocatorVeinInfo left, LocatorVeinInfo right) {
                        if (left.isMined() != right.isMined()) {
                            return left.isMined() ? 1 : -1;
                        }
                        if (sortMode == SortMode.NAME_ASC) {
                            int byName = left.getName().compareToIgnoreCase(right.getName());
                            if (byName != 0) {
                                return byName;
                            }
                            return Integer.compare(getHorizontalDistance(left), getHorizontalDistance(right));
                        }
                        int byDistance =
                                Integer.compare(getHorizontalDistance(left), getHorizontalDistance(right));
                        if (byDistance != 0) {
                            return byDistance;
                        }
                        return left.getName().compareToIgnoreCase(right.getName());
                    }
                });

        scrollOffset = 0;
        rebuildButtons();
    }

    /**
     * 获取 HorizontalDistance。
     *
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    private int getHorizontalDistance(LocatorVeinInfo vein) {
        if (mc == null || mc.player == null) {
            return 0;
        }
        int dx = vein.getCenterX() - mc.player.getPosition().getX();
        int dz = vein.getCenterZ() - mc.player.getPosition().getZ();
        return Math.round((float) Math.sqrt((dx * dx) + (dz * dz)));
    }

    /**
     * 获取 SortButtonLabel。
     *
     * @return 处理结果。
    */
    private String getSortButtonLabel() {
        return sortMode == SortMode.NAME_ASC
                ? tr("gui.configurableoreveins.vein_locator.sort.name")
                : tr("gui.configurableoreveins.vein_locator.sort.distance");
    }

    /**
     * 创建 IconStack。
     *
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    private ItemStack createIconStack(LocatorVeinInfo vein) {
        return createItemStackFromPackedState((vein.getIconBlockId() << 4) | (vein.getIconMeta() & 15));
    }

    /**
     * 创建 ItemStackFromPackedState。
     *
     * @param packedState 参数 packedState。
     * @return 处理结果。
    */
    private ItemStack createItemStackFromPackedState(int packedState) {
        if (packedState <= 0) {
            return ItemStack.EMPTY;
        }

        int blockId = packedState >>> 4;
        int meta = packedState & 15;
        Block block = Block.getBlockById(blockId);
        if (block == null) {
            return ItemStack.EMPTY;
        }
        Item item = Item.getItemFromBlock(block);
        if (item == null) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, 1, meta);
    }

    /**
     * 获取 HoveredVein。
     *
     * @param mouseX 参数 mouseX。
     * @param mouseY 参数 mouseY。
     * @return 处理结果。
    */
    private LocatorVeinInfo getHoveredVein(int mouseX, int mouseY) {
        for (int row = 0; row < veinButtons.size(); row++) {
            GuiButton button = veinButtons.get(row);
            if (mouseX >= button.x
                    && mouseX < button.x + button.width
                    && mouseY >= button.y
                    && mouseY < button.y + button.height) {
                int veinIndex = scrollOffset + row;
                if (veinIndex >= 0 && veinIndex < visibleVeins.size()) {
                    return visibleVeins.get(veinIndex);
                }
            }
        }

        return null;
    }

    /**
     * 构建 TooltipOreLines。
     *
     * @param vein 参数 vein。
     * @return 处理结果。
    */
    private List<TooltipOreLine> buildTooltipOreLines(LocatorVeinInfo vein) {
        List<TooltipOreLine> lines = new ArrayList<TooltipOreLine>();
        int[] packedStates = vein.getOreStateKeys();
        int[] counts = vein.getOreCounts();
        int limit = Math.min(packedStates.length, counts.length);
        for (int index = 0; index < limit; index++) {
            if (counts[index] <= 0 || packedStates[index] <= 0) {
                continue;
            }
            ItemStack stack = createItemStackFromPackedState(packedStates[index]);
            String label;
            if (stack.isEmpty()) {
                label = tr("gui.configurableoreveins.vein_locator.tooltip.unknown_ore", counts[index]);
            } else {
                label =
                        tr(
                                "gui.configurableoreveins.vein_locator.tooltip.ore_entry",
                                stack.getDisplayName(),
                                counts[index]);
            }
            lines.add(new TooltipOreLine(stack, label));
        }
        return lines;
    }

    /**
     * 更新本地矿脉条目的已挖掘状态。
     *
     * @param target 参数 target。
     * @param mined 参数 mined。
    */
    private void updateLocalVeinState(LocatorVeinInfo target, boolean mined) {
        for (int index = 0; index < allVeins.size(); index++) {
            LocatorVeinInfo info = allVeins.get(index);
            if (info.getDimensionId() == target.getDimensionId()
                    && info.getChunkX() == target.getChunkX()
                    && info.getChunkZ() == target.getChunkZ()
                    && info.getVeinHash() == target.getVeinHash()) {
                allVeins.set(
                        index,
                        new LocatorVeinInfo(
                                info.getVeinHash(),
                                mined,
                                info.getDimensionId(),
                                info.getChunkX(),
                                info.getChunkZ(),
                                info.getCenterX(),
                                info.getCenterY(),
                                info.getCenterZ(),
                                info.getName(),
                                info.getHighlightColor(),
                                info.getIconBlockId(),
                                info.getIconMeta(),
                                info.getTotalBlocks(),
                                info.getOreStateKeys(),
                                info.getOreCounts()));
                return;
            }
        }
    }

    /**
     * 执行 tr 逻辑。
     *
     * @param key 参数 key。
     * @param args 参数 args。
     * @return 处理结果。
    */
    private String tr(String key, Object... args) {
        return I18n.format(key, args);
    }

    /**
     * tooltip 中的一行矿物统计展示数据。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    private static class TooltipOreLine {
        private final ItemStack stack;
        private final String label;

        /**
         * 构造 TooltipOreLine 实例。
         *
         * @param stack 参数 stack。
         * @param label 参数 label。
        */
        private TooltipOreLine(ItemStack stack, String label) {
            this.stack = stack;
            this.label = label;
        }
    }

    /**
     * 矿脉列表排序模式枚举。
     *
     * @author <a href="mailto:serliunx@yeah.net">SerLiunx</a>
     * @version 0.0.1
     * @since 2026/3/7
    */
    private enum SortMode {
        DISTANCE_ASC,
        NAME_ASC;

        /**
         * 执行 next 逻辑。
         *
         * @return 处理结果。
        */
        private SortMode next() {
            return this == DISTANCE_ASC ? NAME_ASC : DISTANCE_ASC;
        }
    }
}
