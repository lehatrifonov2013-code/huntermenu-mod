package com.hunter.huntermenu.quest;

import java.util.Set;

public record HunterQuest(
        String id,
        String titleKey,
        String descriptionKey,
        String targetEntityId,
        double minDistance,
        Set<String> requiredGunIds,
        Set<String> requiredArmorIds,
        int requiredCount,
        int rewardXp
) {
    public boolean hasDistanceRequirement() { return minDistance > 0.0D; }
    public boolean hasGunRequirement() { return !requiredGunIds.isEmpty(); }
    public boolean hasArmorRequirement() { return !requiredArmorIds.isEmpty(); }
    public boolean hasEntityRequirement() { return targetEntityId != null && !targetEntityId.isEmpty(); }
}
