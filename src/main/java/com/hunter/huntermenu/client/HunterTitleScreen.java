package com.hunter.huntermenu.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SelectWorldScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.ModListScreen;

public class HunterTitleScreen extends Screen {

    // Палитра theHunter: Call of the Wild
    private static final int COLOR_BG_TOP = 0xFF1A1F18;
    private static final int COLOR_BG_BOTTOM = 0xFF0E120C;
    private static final int COLOR_LOGO_MAIN = 0xFFFFFFFF;
    private static final int COLOR_LOGO_ACCENT = 0xFFFF9C1E;
    private static final int COLOR_LOGO_SUB = 0xFFB8B8A8;
    private static final int COLOR_DIVIDER = 0x55FF9C1E;

    // Кнопки
    private static final int COLOR_BTN_TEXT = 0xFFE8E4D8;
    private static final int COLOR_BTN_TEXT_HOVER = 0xFF1A1F18;
    private static final int COLOR_BTN_ACCENT = 0xFFFF9C1E;
    private static final int COLOR_BTN_SHADOW = 0x66000000;

    private static final int BUTTON_WIDTH = 220;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 2;

    public HunterTitleScreen() {
        super(Component.literal("theHunter // MINECRAFT EDITION"));
    }

    @Override
    protected void init() {
        super.init();

        int buttonX = 40;
        int firstButtonY = 100;

        String[] labels = {
                "ОДИНОЧНАЯ ИГРА",
                "СЕТЕВАЯ ИГРА",
                "МОДЫ",
                "STORYMOD",
                "НАСТРОЙКИ",
                "ВЫЙТИ"
        };

        Button.OnPress[] actions = {
                btn -> this.minecraft.setScreen(new SelectWorldScreen(this)),
                btn -> this.minecraft.setScreen(new JoinMultiplayerScreen(this)),
                btn -> this.minecraft.setScreen(new ModListScreen(this)),
                btn -> this.minecraft.setScreen(new StoryModScreen(this)),
                btn -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)),
                btn -> this.minecraft.stop()
        };

        for (int i = 0; i < labels.length; i++) {
            int y = firstButtonY + i * (BUTTON_HEIGHT + BUTTON_SPACING);
            this.addRenderableWidget(new HunterButton(
                    buttonX, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                    Component.literal(labels[i]),
                    actions[i]
            ));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Плавный вертикальный градиент фона
        graphics.fillGradient(0, 0, this.width, this.height, COLOR_BG_TOP, COLOR_BG_BOTTOM);

        // 2. Логотип "theHunter" (крупно, белым) + "MINECRAFT EDITION" (оранжевым)
        int logoX = 40;
        int logoY = 40;

        graphics.pose().pushPose();
        graphics.pose().translate(logoX, logoY, 0);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        graphics.drawString(this.font, "theHunter", 0, 0, COLOR_LOGO_MAIN, false);
        graphics.pose().popPose();

        graphics.drawString(this.font, "MINECRAFT EDITION", logoX + 2, logoY + 24, COLOR_LOGO_ACCENT, false);

        // Разделительная линия под логотипом
        graphics.hLine(logoX, logoX + BUTTON_WIDTH, logoY + 40, COLOR_DIVIDER);

        // 3. Виджеты с hover и тултипами
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Пусто: фон рисуется в render(), ванильный blur отключён намеренно
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // Кнопка в стиле theHunter: плоская, при hover — оранжевая полоса
    // во всю ширину + текст сдвигается вправо и становится тёмным
    // ─────────────────────────────────────────────────────────────
    private class HunterButton extends Button {

        HunterButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean hovered = this.isHovered();

            if (hovered) {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), COLOR_BTN_ACCENT);
            } else {
                graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), COLOR_BTN_SHADOW);
            }

            Component msg = getMessage();
            int textColor = hovered ? COLOR_BTN_TEXT_HOVER : COLOR_BTN_TEXT;
            int textX = getX() + (hovered ? 8 : 4);
            int textY = getY() + (getHeight() - 8) / 2;

            graphics.drawString(
                    HunterTitleScreen.this.font,
                    msg,
                    textX,
                    textY,
                    textColor,
                    false
            );
        }
    }
}
