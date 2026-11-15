package com.hunter.huntermenu.client;

import com.hunter.huntermenu.quest.HunterQuest;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress.GunStats;
import com.hunter.huntermenu.quest.HunterQuestRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
    private static final int COLOR_HEADER_BG = 0xFF10150F;
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
    private int scrollOffset = 0;    private int mapZoomRadius = 48;

    private long animStartTime = 0L;
    private boolean closing = false;
    private boolean playedAntennaSound = false;
    private boolean playedScreenSound = false;

    public HunterMenuScreen() {
        super(Component.literal("HUNTERMATE // PDA"));
    }

    @Override
    protected void init() {
        super.init();
        refreshData();
        if (animStartTime == 0L) {
            animStartTime = System.currentTimeMillis();
            playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 1.15F, 0.85F);
        }

        int caseX = 20, caseY = 20;
        int caseW = this.width - 40;
        int tabX = caseX + 10;
        int tabY = caseY + 44;

        this.addRenderableWidget(new TabButton(tabX, tabY, 80, 18, "КВЕСТЫ", Tab.QUESTS));
        this.addRenderableWidget(new TabButton(tabX, tabY + 22, 80, 18, "ЖИВОТНЫЕ", Tab.ANIMALS));
        this.addRenderableWidget(new TabButton(tabX, tabY + 44, 80, 18, "ОГНЕСТРЕЛ", Tab.GUNS));
        this.addRenderableWidget(new TabButton(tabX, tabY + 66, 80, 18, "РУКОПАШНОЕ", Tab.MELEE));
        this.addRenderableWidget(new TabButton(tabX, tabY + 88, 80, 18, "КАРТА", Tab.MAP));

        this.addRenderableWidget(new CloseButton(caseX + caseW - 25, caseY + 6, 18, 18));
    }

    public void startClosing() {
        if (!closing) {
            closing = true;
            animStartTime = System.currentTimeMillis();
            playedAntennaSound = false;
            playedScreenSound = false;
            playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 0.7F, 0.55F);
        }
    }

    @Override
    public void onClose() {
        startClosing();
    }

    private void playSound(net.minecraft.sounds.SoundEvent event, float pitch, float volume) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, pitch, volume));
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
        long elapsed = System.currentTimeMillis() - animStartTime;
        float slideProgress;
        float antennaProgress;
        float screenProgress;

        if (!closing) {
            slideProgress = Mth.clamp(elapsed / 180.0F, 0.0F, 1.0F);
            slideProgress = 1.0F - (1.0F - slideProgress) * (1.0F - slideProgress);

            antennaProgress = Mth.clamp((elapsed - 160.0F) / 120.0F, 0.0F, 1.0F);
            if (antennaProgress > 0.05F && !playedAntennaSound) {
                playedAntennaSound = true;
                playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.6F, 0.45F);
            }

            screenProgress = Mth.clamp((elapsed - 260.0F) / 160.0F, 0.0F, 1.0F);
            if (screenProgress > 0.05F && !playedScreenSound) {
                playedScreenSound = true;
                playSound(SoundEvents.NOTE_BLOCK_BIT.value(), 1.45F, 0.55F);
            }
        } else {
            screenProgress = 1.0F - Mth.clamp(elapsed / 140.0F, 0.0F, 1.0F);

            antennaProgress = 1.0F - Mth.clamp((elapsed - 120.0F) / 110.0F, 0.0F, 1.0F);
            if (antennaProgress < 0.9F && !playedAntennaSound) {
                playedAntennaSound = true;
                playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.2F, 0.4F);
            }

            float rawSlide = Mth.clamp((elapsed - 210.0F) / 170.0F, 0.0F, 1.0F);
            if (rawSlide > 0.05F && !playedScreenSound) {
                playedScreenSound = true;
                playSound(SoundEvents.ARMOR_EQUIP_LEATHER.value(), 0.95F, 0.85F);
            }
            slideProgress = 1.0F - rawSlide * rawSlide;

            if (elapsed >= 390L) {
                Minecraft.getInstance().setScreen(null);
                return;
            }
        }

        graphics.fill(0, 0, this.width, this.height, (int) (0xC8 * slideProgress) << 24);

        int offsetY = (int) ((1.0F - slideProgress) * (this.height + 30));
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, offsetY, 0.0F);

        int caseX = 20, caseY = 20;
        int caseW = this.width - 40, caseH = this.height - 40;

        // Телескопическая антенна (справа сверху корпуса)
        int antH = (int) (20 * antennaProgress);
        if (antH > 0) {
            int antX = caseX + caseW - 28;
            int antTop = caseY - antH;
            graphics.fill(antX - 1, antTop, antX + 5, caseY + 2, 0xFF3A3F3A);
            graphics.fill(antX, antTop + 1, antX + 4, caseY + 2, 0xFF1A1D1A);
            if (antennaProgress > 0.75F) {
                graphics.fill(antX - 2, antTop - 3, antX + 6, antTop + 2, 0xFF2B2F2B);
                graphics.fill(antX, antTop - 2, antX + 4, antTop, COLOR_ACCENT);
            }
        }

        // Корпус КПК
        graphics.fill(caseX, caseY, caseX + caseW, caseY + caseH, COLOR_CASE);
        graphics.renderOutline(caseX, caseY, caseW, caseH, 0xFF2B302B);

        int innerX = caseX + 5;
        int innerY = caseY + 5;
        int innerW = caseW - 10;
        int innerH = caseH - 10;

        graphics.fill(innerX, innerY, innerX + innerW, innerY + innerH, 0xFF070907);

        if (screenProgress > 0.01F) {
            int midY = innerY + innerH / 2;
            int halfH = Math.max(1, (int) ((innerH / 2.0F) * screenProgress));
            int visTop = midY - halfH;
            int visBot = midY + halfH;

            graphics.enableScissor(innerX, visTop, innerX + innerW, visBot);

            graphics.fill(innerX, innerY, innerX + innerW, innerY + innerH, COLOR_BG);
            graphics.renderOutline(innerX, innerY, innerW, innerH, COLOR_BORDER);

            // Тёмная тактическая плашка шапки HUNTERMATE PDA
            graphics.fill(caseX + 10, caseY + 10, caseX + 178, caseY + 37, COLOR_HEADER_BG);
            graphics.renderOutline(caseX + 10, caseY + 10, 168, 27, COLOR_BORDER);
            graphics.drawString(this.font, "HUNTERMATE PDA", caseX + 16, caseY + 14, COLOR_ACCENT, false);
            graphics.drawString(this.font, "TACTICAL FIELD TERMINAL", caseX + 16, caseY + 25, COLOR_TEXT_DIM, false);

            if (screenProgress > 0.85F) {
                super.render(graphics, mouseX, mouseY, partialTick);

                int contentX = caseX + 100;
                int contentY = caseY + 44;
                int contentW = caseW - 120;
                int contentH = caseH - 64;

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

            if (screenProgress < 0.95F) {
                graphics.fill(innerX, visTop, innerX + innerW, visTop + 2, 0xCCFF9C1E);
                graphics.fill(innerX, visBot - 2, innerX + innerW, visBot, 0xCCFF9C1E);
            }

            graphics.disableScissor();
        }

        graphics.pose().popPose();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

       private void renderQuests(GuiGraphics graphics, int x, int y, int w, int h) {
        QuestProgress progress = ClientQuestCache.get();
        graphics.enableScissor(x + 2, y + 2, x + w - 2, y + h - 2);
        int drawY = y + 8 - scrollOffset;
        for (HunterQuest quest : questList) {
            boolean completed = progress.isCompleted(quest.id());
            boolean tracked = quest.id().equals(ClientEvents.trackedQuestId);

            if (tracked) {
                graphics.fill(x + 6, drawY - 2, x + w - 6, drawY + 21, 0x33FF9C1E);
            }

            int color = completed ? COLOR_ACCENT : (tracked ? 0xFFFFFFFF : COLOR_TEXT);
            String prefix = tracked ? "► " : "";
            String status = completed ? "[ВЫПОЛНЕНО]" : "[" + progress.getProgress(quest.id()) + "/" + quest.requiredCount() + "]";

            graphics.drawString(this.font, prefix + quest.titleKey(), x + 10, drawY, color, false);
            graphics.drawString(this.font, status, x + w - 92, drawY, color, false);
            graphics.drawString(this.font, quest.descriptionKey() + " (+" + quest.rewardXp() + " XP)", x + 14, drawY + 10, COLOR_TEXT_DIM, false);
            drawY += 25;
        }
        graphics.disableScissor();
    }

        graphics.disableScissor();
    }
    private void renderCards(GuiGraphics graphics, List<String> list, int x, int y, int w, int h, boolean isWeapon) {
        int bottomPanelH = (isWeapon && selectedEntry != null) ? 58 : 0;
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
            graphics.fill(x + 4, py, x + w - 4, py + ph, COLOR_HEADER_BG);
            graphics.renderOutline(x + 4, py, w - 8, ph, COLOR_ACCENT);

            String title = getDisplayName(selectedEntry);
            graphics.drawString(this.font, title, x + 10, py + 5, COLOR_ACCENT, false);

            if (currentTab == Tab.GUNS) {
                GunStats stats = ClientQuestCache.get().getGunStats().get(selectedEntry);
                int kills = stats != null ? stats.kills() : 0;
                double dist = stats != null ? stats.bestDistance() : 0.0;
                String specs = getGunSpecs(selectedEntry);
                graphics.drawString(this.font, specs, x + 10, py + 19, COLOR_TEXT, false);
                graphics.drawString(this.font, "Трофеев: " + kills + " | Рекорд: " + String.format("%.1f", dist) + "м", x + 10, py + 33, COLOR_TEXT_DIM, false);
            } else {
                ItemStack weaponStack = getStackForDisplay(selectedEntry, true);
                String meleeSpecs = getMeleeSpecs(selectedEntry, weaponStack);
                graphics.drawString(this.font, meleeSpecs, x + 10, py + 20, COLOR_TEXT, false);
                graphics.drawString(this.font, "Тип: Холодное / тактический клинок", x + 10, py + 34, COLOR_TEXT_DIM, false);
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

        graphics.fill(centerX - radius, centerY - radius, centerX + radius, centerY + radius, COLOR_HEADER_BG);
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
            } else {
                dotColor = 0xFF3399FF;
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
    private String getGunSpecs(String id) {
        String lower = id.toLowerCase();
        if (lower.contains("m95")) return "Урон: 42.0 | Калибр: .50 BMG | Снайперская";
        if (lower.contains("awp") || lower.contains("awm")) return "Урон: 34.0 | Калибр: .338 Lapua | Снайперская";
        if (lower.contains("m700")) return "Урон: 28.0 | Калибр: .308 Win | Болтовая";
        if (lower.contains("kar98")) return "Урон: 26.0 | Калибр: 7.92x57mm | Винтовка";
        if (lower.contains("svd") || lower.contains("mk14")) return "Урон: 21.0 | Калибр: 7.62x54R | Марксман";
        if (lower.contains("sks")) return "Урон: 16.5 | Калибр: 7.62x39mm | Карабин";
        if (lower.contains("ak47") || lower.contains("akm")) return "Урон: 13.5 | Калибр: 7.62x39mm | Автомат";
        if (lower.contains("scar_h") || lower.contains("g3")) return "Урон: 15.0 | Калибр: 7.62x51mm | Боевая";
        if (lower.contains("m4") || lower.contains("scar_l") || lower.contains("aug") || lower.contains("qbz")) return "Урон: 11.0 | Калибр: 5.56x45mm | Штурмовая";
        if (lower.contains("m870") || lower.contains("spas") || lower.contains("aa12") || lower.contains("db_")) return "Урон: 24.0 | Калибр: 12 Gauge | Дробовик";
        if (lower.contains("deagle")) return "Урон: 18.0 | Калибр: .50 AE | Пистолет";
        if (lower.contains("glock") || lower.contains("m1911") || lower.contains("p320")) return "Урон: 9.5 | Калибр: 9x19mm / .45 | Пистолет";
        if (lower.contains("mp5") || lower.contains("vector") || lower.contains("ump") || lower.contains("p90")) return "Урон: 8.5 | Калибр: 9mm / .45 | ПП";
        return "Урон: 14.0 | Калибр: Стандартный | TACZ";
    }

    private String getMeleeSpecs(String id, ItemStack stack) {
        String lower = id.toLowerCase();
        if (lower.contains("katana") || lower.contains("claymore")) return "Урон: 11.0 | Скорость: 1.5 | Двуручный клинок";
        if (lower.contains("machete") || lower.contains("kukri") || lower.contains("axe")) return "Урон: 9.0 | Скорость: 1.6 | Тяжёлый клинок";
        if (lower.contains("bat") || lower.contains("hammer") || lower.contains("crowbar")) return "Урон: 8.0 | Скорость: 1.4 | Ударное";
        if (lower.contains("knife") || lower.contains("dagger") || lower.contains("karambit") || lower.contains("bayonet")) return "Урон: 6.5 | Скорость: 2.4 | Тактический нож";

        double base = 1.0;
        double speed = 4.0;
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (var entry : modifiers.modifiers()) {
            if (entry.attribute().equals(Attributes.ATTACK_DAMAGE)) base += entry.modifier().amount();
            if (entry.attribute().equals(Attributes.ATTACK_SPEED)) speed += entry.modifier().amount();
        }
        return "Урон: " + String.format("%.1f", base) + " | Скорость атаки: " + String.format("%.2f", Math.max(0.1, speed));
    }

    private String getDisplayName(String id) {
        if (id.startsWith("tacz:")) return id.substring(5).replace('_', ' ').toUpperCase();
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
                tag.putString("MeleeWeaponId", id);
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
        int contentX = caseX + 100, contentY = caseY + 44, contentW = caseW - 120, contentH = caseH - 64;

        if (currentTab == Tab.QUESTS) {
            if (mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= contentY && mouseY <= contentY + contentH) {
                int drawY = contentY + 8 - scrollOffset;
                for (HunterQuest quest : questList) {
                    if (mouseY >= drawY - 2 && mouseY <= drawY + 12) {
                        if (quest.id().equals(ClientEvents.trackedQuestId)) {
                            ClientEvents.trackedQuestId = null;
                        } else {
                            ClientEvents.trackedQuestId = quest.id();
                        }
                        playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.3F, 0.5F);
                        return true;
                    }
                    drawY += 16;
                }
            }
        } else if (currentTab == Tab.ANIMALS || currentTab == Tab.GUNS || currentTab == Tab.MELEE) {
            boolean isWeapon = currentTab != Tab.ANIMALS;
            int bottomPanelH = (isWeapon && selectedEntry != null) ? 58 : 0;
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
                            playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.4F, 0.4F);
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
            super(x, y, width, height, Component.literal("X"), b -> HunterMenuScreen.this.startClosing(), DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), isHovered() ? 0xFFFF4444 : COLOR_PANEL);
            graphics.renderOutline(getX(), getY(), getWidth(), getHeight(), COLOR_BORDER);
            graphics.drawString(HunterMenuScreen.this.font, "X", getX() + 6, getY() + 5, COLOR_TEXT, false);
        }
    }
}
