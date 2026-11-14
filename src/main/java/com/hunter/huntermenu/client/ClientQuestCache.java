package com.hunter.huntermenu.client;

import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;

public class ClientQuestCache {
    private static QuestProgress cachedProgress = new QuestProgress();

    public static void update(QuestProgress progress) {
        cachedProgress = progress;
    }

    public static QuestProgress get() {
        return cachedProgress;
    }
}
