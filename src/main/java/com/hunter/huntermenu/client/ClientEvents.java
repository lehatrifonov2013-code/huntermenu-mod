package com.hunter.huntermenu.client;

import com.hunter.huntermenu.HunterMenuMod;
import com.hunter.huntermenu.quest.HunterQuest;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;
import com.hunter.huntermenu.quest.HunterQuestRegistry;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = HunterMenuMod.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    public static final KeyMapping OPEN_PDA_KEY = new KeyMapping(
            "key.huntermenu.open_pda",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "category.huntermenu"
    );

    public static String trackedQuestId = null;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (OPEN_PDA_KEY.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new HunterMenuScreen());
            } else if (mc.screen instanceof HunterMenuScreen pda) {
                pda.startClosing();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || trackedQuestId == null) return;

        HunterQuest quest = HunterQuestRegistry.get(trackedQuestId);
        if (quest == null) return;

        QuestProgress progress = ClientQuestCache.get();
        boolean done = progress.isCompleted(quest.id());
        int current = progress.getProgress(quest.id());

        String header = "● КОНТРАКТ HUNTERMATE";
        String title = quest.titleKey();
        String status = done ? "ВЫПОЛНЕНО!" : (quest.descriptionKey() + " [" + current + "/" + quest.requiredCount() + "]");

        int boxW = Math.max(145, Math.max(mc.font.width(title), mc.font.width(status)) + 16);
        int boxH = 38;
        int x = mc.getWindow().getGuiScaledWidth() - boxW - 10;
        int y = 10;

        GuiGraphics g = event.getGuiGraphics();
        g.fill(x, y, x + boxW, y + boxH, 0xCC121811);
        g.renderOutline(x, y, boxW, boxH, done ? 0xFF44FF44 : 0xFFFF9C1E);

        g.drawString(mc.font, header, x + 6, y + 4, 0xFFFF9C1E, false);
        g.drawString(mc.font, title, x + 6, y + 15, 0xFFD8C89E, false);
        g.drawString(mc.font, status, x + 6, y + 26, done ? 0xFF55FF55 : 0xFF8A9A72, false);
    }

    @EventBusSubscriber(modid = HunterMenuMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(OPEN_PDA_KEY);
        }
    }
}
