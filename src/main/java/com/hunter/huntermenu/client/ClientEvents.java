package com.hunter.huntermenu.client;

import com.hunter.huntermenu.HunterMenuMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {

    public static final Lazy<KeyMapping> OPEN_MENU_KEY = Lazy.of(() -> new KeyMapping(
            "key.huntermenu.open_menu",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.huntermenu"
    ));

    @EventBusSubscriber(modid = HunterMenuMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(OPEN_MENU_KEY.get());
        }
    }

    @EventBusSubscriber(modid = HunterMenuMod.MOD_ID, value = Dist.CLIENT)
    public static class GameBusEvents {

        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            while (OPEN_MENU_KEY.get().consumeClick()) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.screen == null) {
                    minecraft.setScreen(new HunterMenuScreen());
                }
            }
        }

        /**
         * Перехват открытия TitleScreen: заменяем ванильное главное меню
         * на наш кастомный экран в стиле theHunter: Call of the Wild.
         */
        @SubscribeEvent
        public static void onScreenOpening(ScreenEvent.Opening event) {
            if (event.getScreen() instanceof TitleScreen) {
                event.setNewScreen(new HunterTitleScreen());
            }
        }
    }
}
