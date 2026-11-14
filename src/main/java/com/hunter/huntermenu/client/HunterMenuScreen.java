package com.hunter.huntermenu.client;

import com.hunter.huntermenu.quest.HunterQuest;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress.GunStats;
import com.hunter.huntermenu.quest.HunterQuestRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class HunterMenuScreen extends Screen {

    private static final int COLOR_CASE = 0xFF0A0A0A;
    private static final int COLOR_BG = 0xFF1E261D;
    private static final int COLOR_PANEL = 0xFF2A3528;
    private static final int COLOR_CARD = 0xFF212B20;
    private static final int COLOR_BORDER = 0xFF5A6B4A;
    private static final int COLOR_ACCENT = 0xFFFF9C1E;
    private static final int COLOR_TEXT = 0xFFD8C89E;
    private static final int COLOR_TEXT_DIM = 0xFF8A9A72;

    private enum Tab { QUESTS, ANIMALS, GUNS, MELEE, MAP }
    private Tab currentTab = Tab.QUESTS;

    private final List<HunterQuest> questList = new ArrayList<>();
    private final List<String> animalList = new ArrayList<>();
    private final List<String> gunList = new ArrayList<>();
    private final List<String> meleeList = new ArrayList<>();

    private String selectedEntry = null;
    private int scrollOffset = 0;

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
        this.addRenderableWidget(new TabButton(tabX, tabY + 88, 80, 18, "КАРТА", Tab.MAP));

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
        graphics.drawString(this.font, "v1.2 // RADAR & CARDS", caseX + 15, caseY + 26, COLOR_TEXT_DIM, false);

        super.render(graphics, mouseX, mouseY, partialTick);

        int contentX = caseX + 100;
        int contentY = caseY + 40;
        int contentW = caseW - 120;
        int contentH = caseH - 60;

        graphics.fill(contentX, contentY, contentX + contentW, contentY + contentH, COLOR_PANEL);
        graphics.renderOutline(contentX, contentY, contentW, contentH, COLOR_BORDER);

        switch (currentTab) {
            case QUESTS -> renderQuests(graphics, contentX, contentY, contentW, contentH);
            case ANIMALS -> renderCards(graphics, animalList, contentX, contentY, contentW, contentH, false);
            case GUNS -> renderCards(graphics, gunList, contentX, contentY, contentW, contentH, true);
            case MELEE -> renderCards(graphics, meleeList, contentX, contentY, contentW, contentH, true);
            case MAP -> renderRadarMap(graphics, contentX, contentY, contentW, contentH);
        }
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }
    private void renderQuests(GuiGraphics graphics, int x, int y, int w, int h) {
        QuestProgress progress = ClientQuestCache.get();
        graphics.enableScissor(x + 2, y + 2, x + w - 2, y + h - 2);
        int drawY = y + 10 - scrollOffset;
        for (HunterQuest quest : questList) {
            boolean completed = progress.isCompleted(quest.id());
            int color = completed ? COLOR_ACCENT : COLOR_TEXT;
            String status = completed ? "[ВЫПОЛНЕНО]" : "[" + progress.getProgress(quest.id()) + "/" + quest.requiredCount() + "]";

            graphics.drawString(this.font, quest.titleKey(), x + 10, drawY, color, false);
            graphics.drawString(this.font, status, x + w - 95, drawY, color, false);
            drawY += 16;
        }
        graphics.disableScissor();
    }

    private void renderCards(GuiGraphics graphics, List<String> list, int x, int y, int w, int h, boolean isWeapon) {
        int bottomPanelH = (isWeapon && selectedEntry != null) ? 54 : 0;
        int gridH = h - bottomPanelH;

        int cols = 3;
        int gap = 6;
        int cardW = (w - gap * (cols + 1)) / cols;
        int cardH = 38;

        graphics.enableScissor(x + 2, y + 2, x + w - 2, y + gridH - 2);
        for (int i = 0; i < list.size(); i++) {
            String id = list.get(i);
            int col = i % cols;
            int row = i / cols;

            int cx = x + gap + col * (cardW + gap);
            int cy = y + gap + row * (cardH + gap) - scrollOffset;

            if (cy + cardH < y || cy > y + gridH) continue;

            boolean isSelected = id.equals(selectedEntry);
            graphics.fill(cx, cy, cx + cardW, cy + cardH, isSelected ? 0x55FF9C1E : COLOR_CARD);
            graphics.renderOutline(cx, cy, cardW, cardH, isSelected ? COLOR_ACCENT : COLOR_BORDER);

            ItemStack stack = getStackForDisplay(id, isWeapon);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, cx + (cardW - 16) / 2, cy + 4);
            }

            String name = getDisplayName(id);
            if (this.font.width(name) > cardW - 6) {
                name = this.font.plainSubstrByWidth(name, cardW - 10) + "..";
            }
            int textX = cx + (cardW - this.font.width(name)) / 2;
            graphics.drawString(this.font, name, textX, cy + 24, isSelected ? COLOR_ACCENT : COLOR_TEXT, false);
        }
        graphics.disableScissor();

        if (isWeapon && selectedEntry != null) {
            int py = y + gridH + 2;
            int ph = bottomPanelH - 4;
            graphics.fill(x + 4, py, x + w - 4, py + ph, 0xCC0A0E09);
            graphics.renderOutline(x + 4, py, w - 8, ph, COLOR_ACCENT);

            String title = getDisplayName(selectedEntry);
            graphics.drawString(this.font, title, x + 10, py + 6, COLOR_ACCENT, false);

            if (currentTab == Tab.GUNS) {
                GunStats stats = ClientQuestCache.get().getGunStats().get(selectedEntry);
                int kills = stats != null ? stats.kills() : 0;
                double dist = stats != null ? stats.bestDistance() : 0.0;
                String role = getGunRoleInfo(selectedEntry);
                graphics.drawString(this.font, role, x + 10, py + 20, COLOR_TEXT, false);
                graphics.drawString(this.font, "Трофеев: " + kills + " | Рекорд: " + String.format("%.1f", dist) + "м", x + 10, py + 34, COLOR_TEXT_DIM, false);
            } else {
                ItemStack weaponStack = getStackForDisplay(selectedEntry, true);
                double damage = getMeleeDamage(weaponStack);
                double speed = getMeleeSpeed(weaponStack);
                graphics.drawString(this.font, "Урон: " + String.format("%.1f", damage) + " | Скорость атаки: " + String.format("%.2f", speed), x + 10, py + 22, COLOR_TEXT, false);
                graphics.drawString(this.font, "Тип: Холодное / ближний бой", x + 10, py + 34, COLOR_TEXT_DIM, false);
            }
        }
    }

    private void renderRadarMap(GuiGraphics graphics, int x, int y, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        int centerX = x + w / 2;
        int centerY = y + h / 2 - 6;
        int radius = Math.min(w, h) / 2 - 22;

        graphics.fill(centerX - radius, centerY - radius, centerX + radius, centerY + radius, 0xFF121811);
        graphics.renderOutline(centerX - radius, centerY - radius, radius * 2, radius * 2, COLOR_BORDER);
        graphics.hLine(centerX - radius, centerX + radius, centerY, 0x335A6B4A);
        graphics.vLine(centerX, centerY - radius, centerY + radius, 0x335A6B4A);

        double maxRange = 64.0;
        AABB box = player.getBoundingBox().inflate(maxRange);
        List<LivingEntity> entities = mc.level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive());

        float yawRad = (float) Math.toRadians(player.getYRot() + 180.0F);
        float cos = Mth.cos(yawRad);
        float sin = Mth.sin(yawRad);

        for (LivingEntity entity : entities) {
            double dx = entity.getX() - player.getX();
            double dz = entity.getZ() - player.getZ();
            if (dx * dx + dz * dz > maxRange * maxRange) continue;

            double rx = dx * cos + dz * sin;
            double rz = -dx * sin + dz * cos;

            int dotX = centerX + (int) ((rx / maxRange) * (radius - 6));
            int dotY = centerY + (int) ((rz / maxRange) * (radius - 6));

            int dotColor;
            if (entity instanceof Enemy) {
                dotColor = 0xFFFF3333;
            } else if (entity instanceof NeutralMob) {
                dotColor = 0xFFFFD700;
            } else if (entity instanceof Animal) {
                dotColor = 0xFF3399FF;
            } else {
                dotColor = 0xFF88AA88;
            }

            graphics.fill(dotX - 2, dotY - 2, dotX + 2, dotY + 2, dotColor);
        }

        graphics.fill(centerX - 2, centerY - 2, centerX + 3, centerY + 3, 0xFFFFFFFF);

        int legY = y + h - 14;
        graphics.drawString(this.font, "● ВЫ", x + 10, legY, 0xFFFFFFFF, false);
        graphics.drawString(this.font, "● МИРНЫЕ", x + 52, legY, 0xFF3399FF, false);
        graphics.drawString(this.font, "● НЕЙТР.", x + 115, legY, 0xFFFFD700, false);
        graphics.drawString(this.font, "● ВРАГИ", x + 175, legY, 0xFFFF3333, false);
    }
    private String getGunRoleInfo(String id) {
        String lower = id.toLowerCase();
        if (lower.contains("awp") || lower.contains("m700") || lower.contains("m95") || lower.contains("kar98") || lower.contains("sniper") || lower.contains("svd") || lower.contains("sks")) {
            return "Класс: Снайперская / марксманская винтовка";
        }
        if (lower.contains("ak") || lower.contains("m4") || lower.contains("hk") || lower.contains("scar") || lower.contains("aug")) {
            return "Класс: Штурмовая винтовка";
        }
        if (lower.contains("m870") || lower.contains("aa12") || lower.contains("spas") || lower.contains("shotgun")) {
            return "Класс: Дробовик / гладкоствольное";
        }
        if (lower.contains("glock") || lower.contains("deagle") || lower.contains("m1911") || lower.contains("pistol")) {
            return "Класс: Пистолет / короткоствольное";
        }
        if (lower.contains("mp5") || lower.contains("vector") || lower.contains("ump") || lower.contains("p90")) {
            return "Класс: Пистолет-пулемёт";
        }
        return "Класс: Огнестрельное оружие (TACZ)";
    }

    private double getMeleeDamage(ItemStack stack) {
        double base = 1.0;
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) {
                base += entry.modifier().amount();
            }
        }
        return base;
    }

    private double getMeleeSpeed(ItemStack stack) {
        double base = 4.0;
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_SPEED)) {
                base += entry.modifier().amount();
            }
        }
        return Math.max(0.1, base);
    }

    private String getDisplayName(String id) {
        if (id.startsWith("tacz:")) {
            return id.substring(5).replace('_', ' ').toUpperCase();
        }
        if (id.startsWith("lrtactical:")) {
            String base = id.substring(11);
            return base.substring(0, 1).toUpperCase() + base.substring(1).replace('_', ' ');
        }
        if (id.startsWith("alexsmobs:")) {
            String base = id.substring(10);
            return base.substring(0, 1).toUpperCase() + base.substring(1).replace('_', ' ');
        }
        if (id.startsWith("minecraft:")) {
            String base = id.substring(10);
            return base.substring(0, 1).toUpperCase() + base.substring(1).replace('_', ' ');
        }
        return id;
    }

    private ItemStack getStackForDisplay(String id, boolean isWeapon) {
        if (isWeapon && id.startsWith("tacz:")) {
            ResourceLocation gunItemLoc = ResourceLocation.parse("tacz:modern_kinetic_gun");
            if (BuiltInRegistries.ITEM.containsKey(gunItemLoc)) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(gunItemLoc));
                CompoundTag tag = new CompoundTag();
                tag.putString("GunId", id);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return stack;
            }
        }

        if (isWeapon && id.startsWith("lrtactical:")) {
            ResourceLocation meleeItemLoc = ResourceLocation.parse("lrtactical:melee");
            if (BuiltInRegistries.ITEM.containsKey(meleeItemLoc)) {
                ItemStack stack = new ItemStack(BuiltInRegistries.ITEM.get(meleeItemLoc));
                CompoundTag tag = new CompoundTag();
                tag.putString("MeleeWeaponId", id.substring(11));
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                return stack;
            }
        }

        if (!isWeapon && id.contains(":")) {
            ResourceLocation eggId = ResourceLocation.parse(id + "_spawn_egg");
            if (BuiltInRegistries.ITEM.containsKey(eggId)) {
                return new ItemStack(BuiltInRegistries.ITEM.get(eggId));
            }
            return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:egg")));
        }

        ResourceLocation loc = ResourceLocation.tryParse(id);
        if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
            return new ItemStack(BuiltInRegistries.ITEM.get(loc));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int step = 22;
        scrollOffset = Math.max(0, scrollOffset - (int) (scrollY * step));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int caseX = 20, caseY = 20, caseW = this.width - 40, caseH = this.height - 40;
        int contentX = caseX + 100, contentY = caseY + 40, contentW = caseW - 120, contentH = caseH - 60;

        if (currentTab == Tab.ANIMALS || currentTab == Tab.GUNS || currentTab == Tab.MELEE) {
            boolean isWeapon = currentTab != Tab.ANIMALS;
            int bottomPanelH = (isWeapon && selectedEntry != null) ? 54 : 0;
            int gridH = contentH - bottomPanelH;

            if (mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= contentY && mouseY <= contentY + gridH) {
                List<String> currentList = switch (currentTab) {
                    case ANIMALS -> animalList;
                    case GUNS -> gunList;
                    case MELEE -> meleeList;
                    default -> null;
                };

                if (currentList != null) {
                    int cols = 3;
                    int gap = 6;
                    int cardW = (contentW - gap * (cols + 1)) / cols;
                    int cardH = 38;

                    for (int i = 0; i < currentList.size(); i++) {
                        int col = i % cols;
                        int row = i / cols;
                        int cx = contentX + gap + col * (cardW + gap);
                        int cy = contentY + gap + row * (cardH + gap) - scrollOffset;

                        if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH) {
                            selectedEntry = currentList.get(i);
                            return true;
                        }
                    }
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
                scrollOffset = 0;
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
