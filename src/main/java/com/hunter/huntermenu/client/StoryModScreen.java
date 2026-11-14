package com.hunter.huntermenu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class StoryModScreen extends Screen {

    private static final int COLOR_BG_TOP = 0xFF1A1F18;
    private static final int COLOR_BG_BOTTOM = 0xFF0E120C;
    private static final int COLOR_TITLE = 0xFFFF9C1E;
    private static final int COLOR_TEXT = 0xFFE8E4D8;
    private static final int COLOR_HINT = 0xFF8A9A72;

    private static final int COLOR_BTN_IDLE = 0xCC2A3528;
    private static final int COLOR_BTN_HOVER = 0xFFFF9C1E;
    private static final int COLOR_BTN_BORDER = 0xFF5A6B4A;
    private static final int COLOR_BTN_TEXT = 0xFFE8E4D8;
    private static final int COLOR_BTN_TEXT_HOVER = 0xFF1A1F18;

    private final Screen parent;

    public StoryModScreen(Screen parent) {
        super(Component.literal("STORYMOD"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int buttonWidth = 200;
        int buttonHeight = 20;

        this.addRenderableWidget(new HunterButton(
                centerX - buttonWidth / 2,
                this.height - 60,
                buttonWidth, buttonHeight,
                Component.literal("НАЗАД"),
                btn -> this.onClose()
        ));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Плавный вертикальный градиент
        graphics.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);

        int centerX = this.width / 2;

        // Заголовок "STORYMOD"
        Component title = Component.literal("STORYMOD");
        int titleWidth = this.font.width(title);

        graphics.pose().pushPose();
        graphics.pose().translate(centerX - titleWidth, this.height / 2 - 40, 0);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        graphics.drawString(this.font, title, 0, 0, COLOR_TITLE, false);
        graphics.pose().popPose();

        // Подзаголовок
        Component subtitle = Component.literal("СИСТЕМА КВЕСТОВ // В РАЗРАБОТКЕ");
        int subtitleWidth = this.font.width(subtitle);
        graphics.drawString(
                this.font,
                subtitle,
                centerX - subtitleWidth / 2,
                this.height / 2 + 10,
                COLOR_TEXT,
                false
        );

        Component hint = Component.literal("Здесь появятся: снайперская вышка, одежда охотника, квесты");
        int hintWidth = this.font.width(hint);
        graphics.drawString(
                this.font,
                hint,
                centerX - hintWidth / 2,
                this.height / 2 + 26,
                COLOR_HINT,
                false
        );

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Пусто: фон рисуется в render()
    }

    @Override
    public void onClose() {
        // Явный возврат на родительский экран (HunterTitleScreen),
        // а не в пустоту через стандартный setScreen(null)
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
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
            boolean hovered = this.isHovered();
            int fillColor = hovered ? COLOR_BTN_HOVER : COLOR_BTN_IDLE;
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), fillColor);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), COLOR_BTN_BORDER);

            Component msg = getMessage();
            int textColor = hovered ? COLOR_BTN_TEXT_HOVER : COLOR_BTN_TEXT;
            int textX = getX() + (getWidth() - StoryModScreen.this.font.width(msg)) / 2;
            int textY = getY() + (getHeight() - 8) / 2;

            graphics.drawString(
                    StoryModScreen.this.font,
                    msg,
                    textX,
                    textY,
                    textColor,
                    false
            );
        }
    }
}
