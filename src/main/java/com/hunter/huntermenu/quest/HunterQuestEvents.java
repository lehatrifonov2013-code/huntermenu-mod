package com.hunter.huntermenu.quest;

import com.hunter.huntermenu.HunterMenuMod;
import com.hunter.huntermenu.network.HunterQuestSyncPayload;
import com.hunter.huntermenu.quest.HunterQuestAttachment.QuestProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
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

        boolean isAnimal = entity instanceof Animal;
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();

        if (isAnimal) {
            progress.incrementTotalKills();
            progress.discoverAnimal(entityId);
        }

        double distance = player.distanceTo(entity);
        ItemStack weapon = player.getMainHandItem();
        String weaponKey = getWeaponKey(weapon);
        String gunId = getGunId(weapon);

        boolean isMelee = event.getSource().getDirectEntity() == player;

        if (gunId != null && isAnimal) {
            progress.addGunKill(gunId, distance);
        } else if (isMelee && isAnimal && weaponKey != null && !weaponKey.equals("minecraft:air")) {
            progress.discoverMelee(weaponKey);
        }

        for (Map.Entry<String, HunterQuest> entry : HunterQuestRegistry.getAll().entrySet()) {
            HunterQuest quest = entry.getValue();
            if (progress.isCompleted(quest.id())) continue;

            if (quest.hasEntityRequirement() && !quest.targetEntityId().equals(entityId)) {
                if (quest.id().equals("bird_hunter") && (entityId.contains("toucan") || entityId.contains("crow") || entityId.contains("seagull") || entityId.contains("bald_eagle"))) {
                    // ok
                } else {
                    continue;
                }
            }

            if (quest.hasDistanceRequirement() && distance < quest.minDistance()) continue;

            if (quest.hasGunRequirement()) {
                if (gunId == null || !quest.requiredGunIds().contains(gunId)) continue;
            }

            if (quest.id().startsWith("gun_") && gunId == null) continue;
            if (quest.id().startsWith("melee_") && !isMelee) continue;
            if (quest.id().equals("close_range") && distance >= 5.0D) continue;

            if (quest.id().startsWith("night_")) {
                long time = player.level().getDayTime() % 24000L;
                if (time < 13000L || time > 23000L) continue;
            }

            if (quest.id().equals("rain_1") && !player.level().isRaining()) continue;
            if (quest.id().equals("storm_1") && !player.level().isThundering()) continue;

            if (quest.id().startsWith("survivor")) {
                progress.incrementSurvivorStreak();
                if (progress.getSurvivorStreak() >= quest.requiredCount()) {
                    progress.incrementProgress(quest.id(), quest.requiredCount());
                    if (progress.isCompleted(quest.id())) {
                        giveQuestReward(player, quest);
                    }
                }
                continue;
            }

            progress.incrementProgress(quest.id(), quest.requiredCount());
            if (progress.isCompleted(quest.id())) {
                giveQuestReward(player, quest);
            }
        }

        if (distance > progress.getBestDistance()) {
            progress.updateBestDistance(distance);
        }

        PacketDistributor.sendToPlayer(player, new HunterQuestSyncPayload(progress));
    }
    private static void giveQuestReward(ServerPlayer player, HunterQuest quest) {
        if (quest.rewardXp() > 0) {
            player.giveExperiencePoints(quest.rewardXp());
        }

        String rewardName = "";
        if (quest.rewardItemId() != null && !quest.rewardItemId().isEmpty() && quest.rewardItemCount() > 0) {
            ResourceLocation loc = ResourceLocation.tryParse(quest.rewardItemId());
            if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                ItemStack rewardStack = new ItemStack(BuiltInRegistries.ITEM.get(loc), quest.rewardItemCount());
                rewardName = " + " + rewardStack.getHoverName().getString() + " x" + quest.rewardItemCount();
                if (!player.getInventory().add(rewardStack)) {
                    player.drop(rewardStack, false);
                }
            }
        }

        player.sendSystemMessage(
                Component.literal("[HUNTERMATE] ")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(Component.literal("Контракт выполнен: " + quest.titleKey())
                                .withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" (+" + quest.rewardXp() + " XP" + rewardName + ")")
                                .withStyle(ChatFormatting.GREEN))
        );
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
        } else {
            String meleeKey = getWeaponKey(stack);
            if (meleeKey != null && (meleeKey.startsWith("lrtactical:") || itemId.contains("sword") || itemId.contains("axe") || itemId.contains("knife") || itemId.contains("dagger") || itemId.contains("blade"))) {
                if (!progress.getDiscoveredMelee().contains(meleeKey)) {
                    progress.discoverMelee(meleeKey);
                    changed = true;
                }
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

    private static String getWeaponKey(ItemStack stack) {
        if (stack.isEmpty()) return null;
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if (itemId.equals("lrtactical:melee")) {
            var customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                String meleeId = customData.copyTag().getString("MeleeWeaponId");
                if (!meleeId.isEmpty()) {
                    return meleeId.contains(":") ? meleeId : "lrtactical:" + meleeId;
                }
            }
        }
        return itemId;
    }
}
