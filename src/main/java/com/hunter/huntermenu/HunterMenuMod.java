package com.hunter.huntermenu;

import com.hunter.huntermenu.network.HunterQuestSyncPayload;
import com.hunter.huntermenu.quest.HunterQuestAttachment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(HunterMenuMod.MOD_ID)
public class HunterMenuMod {
    public static final String MOD_ID = "huntermenu";

    public HunterMenuMod(IEventBus modEventBus, ModContainer modContainer) {
        HunterQuestAttachment.ATTACHMENT_TYPES.register(modEventBus);
        modEventBus.addListener(this::registerPayloads);
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                HunterQuestSyncPayload.TYPE,
                HunterQuestSyncPayload.STREAM_CODEC,
                (payload, context) -> com.hunter.huntermenu.client.ClientPayloadHandler.handleSync(payload, context)
        );
    }
}
