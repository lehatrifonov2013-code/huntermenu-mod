package com.hunter.huntermenu.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class HunterQuestAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, "huntermenu");

    public static final Supplier<AttachmentType<QuestProgress>> QUEST_PROGRESS =
            ATTACHMENT_TYPES.register("quest_progress", () ->
                    AttachmentType.builder(QuestProgress::new)
                            .serialize(QuestProgress.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static class QuestProgress {
        private final Map<String, Integer> progress = new HashMap<>();
        private final Map<String, Boolean> completed = new HashMap<>();
        private int totalKills = 0;
        private double bestDistance = 0.0D;
        private int survivorStreak = 0;

        private final Set<String> discoveredAnimals = new HashSet<>();
        private final Map<String, GunStats> gunStats = new HashMap<>();
        private final Set<String> discoveredMelee = new HashSet<>();

        public record GunStats(int kills, double bestDistance) {
            public static final Codec<GunStats> CODEC = RecordCodecBuilder.create(instance ->
                    instance.group(
                            Codec.INT.fieldOf("kills").forGetter(GunStats::kills),
                            Codec.DOUBLE.fieldOf("best_distance").forGetter(GunStats::bestDistance)
                    ).apply(instance, GunStats::new)
            );

            public static final StreamCodec<FriendlyByteBuf, GunStats> STREAM_CODEC =
                    StreamCodec.composite(
                            ByteBufCodecs.VAR_INT, GunStats::kills,
                            ByteBufCodecs.DOUBLE, GunStats::bestDistance,
                            GunStats::new
                    );
        }

        public static final Codec<QuestProgress> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("progress").forGetter(p -> p.progress),
                        Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("completed").forGetter(p -> p.completed),
                        Codec.INT.fieldOf("total_kills").forGetter(p -> p.totalKills),
                        Codec.DOUBLE.fieldOf("best_distance").forGetter(p -> p.bestDistance),
                        Codec.INT.fieldOf("survivor_streak").forGetter(p -> p.survivorStreak),
                        Codec.STRING.listOf().xmap(HashSet::new, java.util.ArrayList::new).fieldOf("discovered_animals").forGetter(p -> new HashSet<>(p.discoveredAnimals)),
                        Codec.unboundedMap(Codec.STRING, GunStats.CODEC).fieldOf("gun_stats").forGetter(p -> p.gunStats),
                        Codec.STRING.listOf().xmap(HashSet::new, java.util.ArrayList::new).fieldOf("discovered_melee").forGetter(p -> new HashSet<>(p.discoveredMelee))
                ).apply(instance, QuestProgress::new)
        );
        public static final StreamCodec<FriendlyByteBuf, QuestProgress> STREAM_CODEC = StreamCodec.of(
                (buf, p) -> {
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).encode(buf, new HashMap<>(p.progress));
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.BOOL).encode(buf, new HashMap<>(p.completed));
                    buf.writeVarInt(p.totalKills);
                    buf.writeDouble(p.bestDistance);
                    buf.writeVarInt(p.survivorStreak);
                    ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8).encode(buf, new HashSet<>(p.discoveredAnimals));
                    ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, GunStats.STREAM_CODEC).encode(buf, new HashMap<>(p.gunStats));
                    ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8).encode(buf, new HashSet<>(p.discoveredMelee));
                },
                buf -> new QuestProgress(
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT).decode(buf),
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.BOOL).decode(buf),
                        buf.readVarInt(),
                        buf.readDouble(),
                        buf.readVarInt(),
                        ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8).decode(buf),
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, GunStats.STREAM_CODEC).decode(buf),
                        ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8).decode(buf)
                )
        );

        public QuestProgress() {}

        private QuestProgress(Map<String, Integer> progress, Map<String, Boolean> completed,
                              int totalKills, double bestDistance, int survivorStreak,
                              Set<String> discoveredAnimals, Map<String, GunStats> gunStats, Set<String> discoveredMelee) {
            this.progress.putAll(progress);
            this.completed.putAll(completed);
            this.totalKills = totalKills;
            this.bestDistance = bestDistance;
            this.survivorStreak = survivorStreak;
            this.discoveredAnimals.addAll(discoveredAnimals);
            this.gunStats.putAll(gunStats);
            this.discoveredMelee.addAll(discoveredMelee);
        }

        public int getProgress(String questId) { return progress.getOrDefault(questId, 0); }
        public boolean isCompleted(String questId) { return completed.getOrDefault(questId, false); }
        public void incrementProgress(String questId, int max) {
            int current = getProgress(questId);
            if (current >= max) return;
            int next = current + 1;
            progress.put(questId, next);
            if (next >= max) completed.put(questId, true);
        }
        public void resetProgress(String questId) {
            progress.remove(questId);
            completed.remove(questId);
        }
        public Map<String, Integer> getProgressMap() { return Map.copyOf(progress); }
        public Map<String, Boolean> getCompletedMap() { return Map.copyOf(completed); }
        public int getTotalKills() { return totalKills; }
        public void incrementTotalKills() { totalKills++; }
        public double getBestDistance() { return bestDistance; }
        public void updateBestDistance(double d) { if (d > bestDistance) bestDistance = d; }
        public int getSurvivorStreak() { return survivorStreak; }
        public void incrementSurvivorStreak() { survivorStreak++; }
        public void resetSurvivorStreak() { survivorStreak = 0; }

        public Set<String> getDiscoveredAnimals() { return Set.copyOf(discoveredAnimals); }
        public void discoverAnimal(String id) { discoveredAnimals.add(id); }

        public Map<String, GunStats> getGunStats() { return Map.copyOf(gunStats); }
        public void addGunKill(String gunId, double distance) {
            GunStats stats = gunStats.getOrDefault(gunId, new GunStats(0, 0.0D));
            gunStats.put(gunId, new GunStats(stats.kills() + 1, Math.max(stats.bestDistance(), distance)));
        }
        public void unlockGun(String gunId) {
            gunStats.putIfAbsent(gunId, new GunStats(0, 0.0D));
        }

        public Set<String> getDiscoveredMelee() { return Set.copyOf(discoveredMelee); }
        public void discoverMelee(String id) { discoveredMelee.add(id); }

        public static QuestProgress get(Player player) {
            return player.getData(QUEST_PROGRESS);
        }
    }
}
