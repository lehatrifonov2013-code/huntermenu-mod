package com.hunter.huntermenu.quest;

import com.hunter.huntermenu.HunterMenuMod;
import com.hunter.huntermenu.network.HunterQuestSyncPayload;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemPickupEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

@EventBusSubscriber(modid = HunterMenuMod.MOD_ID)
public class HunterQuestEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        QuestProgress progress = QuestProgress.get(player);

        boolean isAnimal = entity instanceof net.minecraft.world.entity.Animal;
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();

        if (isAnimal) {
            progress.incrementTotalKills();
            progress.discoverAnimal(entityId);
        }

        double distance = player.distanceTo(entity);
        ItemStack weapon = player.getMainHandItem();
        String itemId = BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString();
        String gunId = getGunId(weapon);

        boolean isSniper = gunId != null && HunterQuestRegistry.SNIPER_GUN_IDS.contains(gunId);
        boolean isMelee = event.getSource().getDirectEntity() == player;

        if (gunId != null && isAnimal) {
            progress.addGunKill(gunId, distance);
        } else if (isMelee && isAnimal && !itemId.equals("minecraft:air")) {
            progress.discoverMelee(itemId);
        }

        boolean leveledUp = false;

        for (Map.Entry<String, HunterQuest> entry : HunterQuestRegistry.getAll().entrySet()) {
            HunterQuest quest = entry.getValue();
            if (progress.isCompleted(quest.id())) continue;

            if (quest.hasEntityRequirement() && !quest.targetEntityId().equals(entityId)) {
                if (quest.id().equals("bird_hunter") && entityId.contains("toucan")) {
                    // ok
                } else {
                    continue;
                }
            }

            if (quest.hasDistanceRequirement() && distance < quest.minDistance()) continue;
            if (quest.hasGunRequirement() && !isSniper) continue;

            if (quest.id().equals("melee_hunter") && !isMelee) continue;
            if (quest.id().equals("night_hunter")) {
                long time = player.level().getDayTime() % 24000L;
                if (time < 13000L || time > 23000L) continue;
            }
            if (quest.id().equals("survivor")) {
                if (progress.getSurvivorStreak() >= 10) continue;
                progress.incrementSurvivorStreak();
                if (progress.getSurvivorStreak() >= quest.requiredCount()) {
                    progress.incrementProgress(quest.id(), quest.requiredCount());
                    leveledUp = true;
                }
                continue;
            }
            progress.incrementProgress(quest.id(), quest.requiredCount());
            if (progress.isCompleted(quest.id())) {
                leveledUp = true;
            }
        }

        if (distance > progress.getBestDistance()) {
            progress.updateBestDistance(distance);
        }

        if (leveledUp) {
            player.giveExperiencePoints(10);
        }

        PacketDistributor.sendToPlayer(player, new HunterQuestSyncPayload(progress));
    }

    @SubscribeEvent
    public static void onItemPickup(ItemPickupEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        processItemDiscovery(player, event.getStack());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameTime() % 20 != 0) return;

        for (ItemStack stack : player.getInventory().items) {
            processItemDiscovery(player, stack);
        }
    }

    private static void processItemDiscovery(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        QuestProgress progress = QuestProgress.get(player);
        boolean changed = false;

        if (itemId.equals("tacz:modern_kinetic_gun")) {
            String gunId = getGunId(stack);
            if (gunId != null && !progress.getGunStats().containsKey(gunId)) {
                progress.unlockGun(gunId);
                changed = true;
            }
        } else if (itemId.contains("sword") || itemId.contains("axe") || itemId.contains("knife") || itemId.contains("dagger") || itemId.contains("blade")) {
            if (!progress.getDiscoveredMelee().contains(itemId)) {
                progress.discoverMelee(itemId);
                changed = true;
            }
        }

        if (changed) {
            PacketDistributor.sendToPlayer(player, new HunterQuestSyncPayload(progress));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            QuestProgress newProgress = QuestProgress.get(event.getEntity());
            newProgress.resetSurvivorStreak();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new HunterQuestSyncPayload(QuestProgress.get(player)));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new HunterQuestSyncPayload(QuestProgress.get(player)));
        }
    }

    private static String getGunId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (!itemId.equals("tacz:modern_kinetic_gun")) return null;

        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            String gunId = customData.copyTag().getString("GunId");
            return gunId.isEmpty() ? null : gunId;
        }
        return null;
    }
}
