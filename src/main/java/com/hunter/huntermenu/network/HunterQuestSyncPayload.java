package com.hunter.huntermenu.network;

import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HunterQuestSyncPayload(QuestProgress progress) implements CustomPacketPayload {
    public static final Type<HunterQuestSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("huntermenu", "quest_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HunterQuestSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    QuestProgress.STREAM_CODEC,
                    HunterQuestSyncPayload::progress,
                    HunterQuestSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
