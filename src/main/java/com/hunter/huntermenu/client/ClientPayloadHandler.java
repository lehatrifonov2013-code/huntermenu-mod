package com.hunter.huntermenu.client;

import com.hunter.huntermenu.network.HunterQuestSyncPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {
    public static void handleSync(HunterQuestSyncPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientQuestCache.update(payload.progress());
        });
    }
}
