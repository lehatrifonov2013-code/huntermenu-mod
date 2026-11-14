package com.hunter.huntermenu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HunterMenuScreen extends Screen {

    private static final int COLOR_BG_OVERLAY = 0xC81E261D;
    private static final int COLOR_PANEL_FILL = 0xE62A3528;
    private static final int COLOR_PANEL_BORDER = 0xFF5A6B4A;
    private static final int COLOR_TITLE = 0xFFD8C89E;

    private static final int COLOR_BTN_IDLE = 0xCC2A3528;
    private static final int COLOR_BTN_HOVER = 0xCC3D4F37;
    private static final int COLOR_BTN_BORDER = 0xFF5A6B4A;
    private static final int COLOR_BTN_TEXT = 0xFFD8C89E;

    private static final int PANEL_WIDTH = 260;
    private static final int PANEL_HEIGHT = 180;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 22;

    public HunterMenuScreen() {
        super(Component.literal("HUNTERMATE // МЕНЮ"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int buttonX = centerX - BUTTON_WIDTH / 2;
        int buttonY = centerY + PANEL_HEIGHT / 2 - BUTTON_HEIGHT - 18;

        HunterButton closeButton = new HunterButton(
                buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("ЗАКРЫТЬ"),
                btn -> this.onClose()
        );
        this.addRenderableWidget(closeButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, COLOR_BG_OVERLAY);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;

        graphics.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + PANEL_HEIGHT, COLOR_PANEL_FILL);
        graphics.renderOutline(panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT, COLOR_PANEL_BORDER);

        Component title = this.getTitle();
        int titleWidth = this.font.width(title);
        graphics.drawString(
                this.font,
                title,
                centerX - titleWidth / 2,
                panelY + 16,
                COLOR_TITLE,
                false
        );

        int lineY = panelY + 38;
        graphics.hLine(panelX + 20, panelX + PANEL_WIDTH - 20, lineY, COLOR_PANEL_BORDER);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class HunterButton extends Button {

        HunterButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int fillColor = this.isHovered() ? COLOR_BTN_HOVER : COLOR_BTN_IDLE;

            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fillColor);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), COLOR_BTN_BORDER);

            int textX = getX() + (getWidth() - HunterMenuScreen.this.font.width(getMessage())) / 2;
            int textY = getY() + (getHeight() - 8) / 2;
            graphics.drawString(
                    HunterMenuScreen.this.font,
                    getMessage(),
                    textX,
                    textY,
                    COLOR_BTN_TEXT,
                    false
            );
        }
    }
}
