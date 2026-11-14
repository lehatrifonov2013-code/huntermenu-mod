package com.hunter.huntermenu.client;

import com.hunter.huntermenu.quest.HunterQuest;
import com.hunter.huntermenu.quest.HunterQuestAttachment.GunStats;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;
import com.hunter.huntermenu.quest.HunterQuestRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class HunterMenuScreen extends Screen {

    private static final int COLOR_CASE = 0xFF0A0A0A;
    private static final int COLOR_BG = 0xFF1E261D;
    private static final int COLOR_PANEL = 0xFF2A3528;
    private static final int COLOR_BORDER = 0xFF5A6B4A;
    private static final int COLOR_ACCENT = 0xFFFF9C1E;
    private static final int COLOR_TEXT = 0xFFD8C89E;
    private static final int COLOR_TEXT_DIM = 0xFF8A9A72;

    private enum Tab { QUESTS, ANIMALS, GUNS, MELEE }
    private Tab currentTab = Tab.QUESTS;

    private final List<HunterQuest> questList = new ArrayList<>();
    private final List<String> animalList = new ArrayList<>();
    private final List<String> gunList = new ArrayList<>();
    private final List<String> meleeList = new ArrayList<>();

    private String selectedEntry = null;

    public HunterMenuScreen() {
        super(Component.literal("HUNTERMATE // КПК"));
    }

    @Override
    protected void init() {
        super.init();
        refreshData();

        int caseX = 20, caseY = 20;
        int caseW = this.width - 40;
        int tabX = caseX + 10;
        int tabY = caseY + 40;

        this.addRenderableWidget(new TabButton(tabX, tabY, 80, 18, "КВЕСТЫ", Tab.QUESTS));
        this.addRenderableWidget(new TabButton(tabX, tabY + 22, 80, 18, "ЖИВОТНЫЕ", Tab.ANIMALS));
        this.addRenderableWidget(new TabButton(tabX, tabY + 44, 80, 18, "ОГНЕСТРЕЛ", Tab.GUNS));
        this.addRenderableWidget(new TabButton(tabX, tabY + 66, 80, 18, "РУКОПАШНОЕ", Tab.MELEE));

        this.addRenderableWidget(new CloseButton(caseX + caseW - 25, caseY + 5, 18, 18));
    }

    private void refreshData() {
        QuestProgress progress = ClientQuestCache.get();
        questList.clear();
        questList.addAll(HunterQuestRegistry.getAll().values());
        animalList.clear();
        animalList.addAll(progress.getDiscoveredAnimals());
        gunList.clear();
        gunList.addAll(progress.getGunStats().keySet());
        meleeList.clear();
        meleeList.addAll(progress.getDiscoveredMelee());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xC8000000);
        int caseX = 20, caseY = 20;
        int caseW = this.width - 40, caseH = this.height - 40;

        graphics.fill(caseX, caseY, caseX + caseW, caseY + caseH, COLOR_CASE);
        graphics.fill(caseX + 5, caseY + 5, caseX + caseW - 5, caseY + caseH - 5, COLOR_BG);
        graphics.renderOutline(caseX + 5, caseY + 5, caseW - 10, caseH - 10, COLOR_BORDER);

        int antX = caseX + caseW - 30;
        int antY = caseY - 15;
        graphics.fill(antX, antY, antX + 4, caseY, COLOR_CASE);
        graphics.fill(antX - 2, antY, antX + 6, antY + 6, COLOR_CASE);

        graphics.drawString(this.font, "HUNTERMATE", caseX + 15, caseY + 15, COLOR_ACCENT, false);
        graphics.drawString(this.font, "v1.0 // SYSTEM READY", caseX + 15, caseY + 26, COLOR_TEXT_DIM, false);

        super.render(graphics, mouseX, mouseY, partialTick);

        int contentX = caseX + 100;
        int contentY = caseY + 40;
        int contentW = caseW - 120;
        int contentH = caseH - 60;

        graphics.fill(contentX, contentY, contentX + contentW, contentY + contentH, COLOR_PANEL);
        graphics.renderOutline(contentX, contentY, contentW, contentH, COLOR_BORDER);

        switch (currentTab) {
            case QUESTS -> renderQuests(graphics, contentX, contentY, contentW, contentH);
            case ANIMALS -> renderList(graphics, animalList, contentX, contentY, contentW, contentH, false);
            case GUNS -> renderList(graphics, gunList, contentX, contentY, contentW, contentH, true);
            case MELEE -> renderList(graphics, meleeList, contentX, contentY, contentW, contentH, true);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }
    private void renderQuests(GuiGraphics graphics, int x, int y, int w, int h) {
        QuestProgress progress = ClientQuestCache.get();
        int drawY = y + 10;
        for (HunterQuest quest : questList) {
            boolean completed = progress.isCompleted(quest.id());
            int color = completed ? COLOR_ACCENT : COLOR_TEXT;
            String status = completed ? "[ВЫПОЛНЕНО]" : "[" + progress.getProgress(quest.id()) + "/" + quest.requiredCount() + "]";

            graphics.drawString(this.font, quest.titleKey(), x + 10, drawY, color, false);
            graphics.drawString(this.font, status, x + w - 100, drawY, color, false);
            drawY += 15;
            if (drawY > y + h - 15) break;
        }
    }

    private void renderList(GuiGraphics graphics, List<String> list, int x, int y, int w, int h, boolean isWeapon) {
        int drawY = y + 10;
        for (String id : list) {
            boolean isSelected = id.equals(selectedEntry);
            if (isSelected) {
                graphics.fill(x + 5, drawY - 2, x + w - 5, drawY + 10, 0x44FF9C1E);
            }

            if (isWeapon) {
                ItemStack stack = getStackForDisplay(id);
                if (!stack.isEmpty()) {
                    graphics.renderItem(stack, x + 10, drawY - 4);
                }
            }

            graphics.drawString(this.font, id, x + 30, drawY, COLOR_TEXT, false);
            drawY += 20;
            if (drawY > y + h - 15) break;
        }

        if (isWeapon && selectedEntry != null) {
            GunStats stats = ClientQuestCache.get().getGunStats().get(selectedEntry);
            if (stats != null) {
                graphics.fill(x + w - 120, y + 10, x + w - 10, y + 80, 0x88000000);
                graphics.renderOutline(x + w - 120, y + 10, 110, 70, COLOR_ACCENT);
                graphics.drawString(this.font, "СТАТИСТИКА", x + w - 110, y + 15, COLOR_ACCENT, false);
                graphics.drawString(this.font, "Убийств: " + stats.kills(), x + w - 110, y + 30, COLOR_TEXT, false);
                graphics.drawString(this.font, "Рекорд: " + String.format("%.1f", stats.bestDistance()) + "м", x + w - 110, y + 45, COLOR_TEXT, false);
            }
        }
    }

    private ItemStack getStackForDisplay(String id) {
        if (id.startsWith("tacz:")) {
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse("tacz:modern_kinetic_gun"));
            return new ItemStack(item);
        }
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        return new ItemStack(item);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int caseX = 20, caseY = 20, caseW = this.width - 40, caseH = this.height - 40;
        int contentX = caseX + 100, contentY = caseY + 40, contentW = caseW - 120, contentH = caseH - 60;

        if (mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= contentY && mouseY <= contentY + contentH) {
            int listY = contentY + 10;
            List<String> currentList = switch (currentTab) {
                case ANIMALS -> animalList;
                case GUNS -> gunList;
                case MELEE -> meleeList;
                default -> null;
            };

            if (currentList != null) {
                for (String id : currentList) {
                    if (mouseY >= listY - 4 && mouseY <= listY + 12) {
                        selectedEntry = id;
                        return true;
                    }
                    listY += 20;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class TabButton extends Button {
        private final Tab targetTab;
        TabButton(int x, int y, int width, int height, String msg, Tab tab) {
            super(x, y, width, height, Component.literal(msg), b -> {
                currentTab = tab;
                selectedEntry = null;
                refreshData();
            }, DEFAULT_NARRATION);
            this.targetTab = tab;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            boolean active = currentTab == targetTab;
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), active ? COLOR_ACCENT : COLOR_PANEL);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), COLOR_BORDER);
            graphics.drawString(HunterMenuScreen.this.font, getMessage(), getX() + 5, getY() + 5, active ? 0xFF000000 : COLOR_TEXT, false);
        }
    }

    private class CloseButton extends Button {
        CloseButton(int x, int y, int width, int height) {
            super(x, y, width, height, Component.literal("X"), b -> HunterMenuScreen.this.onClose(), DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), isHovered() ? 0xFFFF4444 : COLOR_PANEL);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), COLOR_BORDER);
            graphics.drawString(HunterMenuScreen.this.font, "X", getX() + 6, getY() + 5, COLOR_TEXT, false);
        }
    }
}
