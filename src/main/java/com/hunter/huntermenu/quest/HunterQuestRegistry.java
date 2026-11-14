package com.hunter.huntermenu.quest;

import net.neoforged.fml.ModList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class HunterQuestRegistry {
    private HunterQuestRegistry() {}

    public static final Set<String> SNIPER_GUN_IDS = Set.of(
            "tacz:ai_awp", "tacz:m700", "tacz:m95", "tacz:kar98k", "tacz:awm"
    );
    public static final Set<String> HUNTER_ARMOR_IDS = Set.of();

    public static final String MOOSE_ID = "alexsmobs:moose";
    public static final String GRIZZLY_BEAR_ID = "alexsmobs:grizzly_bear";
    public static final String BLUE_JAY_ID = "alexsmobs:blue_jay";
    public static final String TOUCAN_ID = "alexsmobs:toucan";

    private static final boolean ALEXSMOBS_LOADED = ModList.get().isLoaded("alexsmobs");
    private static final boolean TACZ_LOADED = ModList.get().isLoaded("tacz");
    private static final boolean IMMERSIVE_ARMORS_LOADED = ModList.get().isLoaded("immersive_armors");

    private static final Map<String, HunterQuest> QUESTS = new HashMap<>();

    static {
        register(new HunterQuest("first_blood", "Первая кровь", "Убей любого животного", null, 0.0D, Set.of(), Set.of(), 1, 10));

        if (TACZ_LOADED) {
            register(new HunterQuest("sniper_100", "Снайпер", "Убей животное с 100+ блоков из снайперки", null, 100.0D, SNIPER_GUN_IDS, Set.of(), 1, 50));
            register(new HunterQuest("sniper_200", "Дальний выстрел", "Убей животное с 200+ блоков из снайперки", null, 200.0D, SNIPER_GUN_IDS, Set.of(), 1, 100));
        }

        if (ALEXSMOBS_LOADED) {
            register(new HunterQuest("moose_hunt", "Лосиная охота", "Убей лося", MOOSE_ID, 0.0D, Set.of(), Set.of(), 1, 75));
            register(new HunterQuest("grizzly_trophy", "Медвежий трофей", "Убей гризли", GRIZZLY_BEAR_ID, 0.0D, Set.of(), Set.of(), 1, 75));
            register(new HunterQuest("bird_hunter", "Пернатый", "Убей 3 птиц (сойка/тукан)", BLUE_JAY_ID, 0.0D, Set.of(), Set.of(), 3, 40));
            register(new HunterQuest("moose_master", "Охотник на оленей", "Убей 5 лосей", MOOSE_ID, 0.0D, Set.of(), Set.of(), 5, 150));
        }

        register(new HunterQuest("melee_hunter", "Без выстрела", "Убей животное в ближнем бою", null, 0.0D, Set.of(), Set.of(), 1, 30));
        register(new HunterQuest("night_hunter", "Ночной охотник", "Убей животное ночью", null, 0.0D, Set.of(), Set.of(), 1, 40));
        register(new HunterQuest("survivor", "Выживший", "Убей 10 животных, не умирая", null, 0.0D, Set.of(), Set.of(), 10, 200));

        if (IMMERSIVE_ARMORS_LOADED && !HUNTER_ARMOR_IDS.isEmpty()) {
            register(new HunterQuest("armored_hunter", "Одетый охотник", "Надень броню охотника", null, 0.0D, Set.of(), HUNTER_ARMOR_IDS, 1, 60));
        }
    }

    private static void register(HunterQuest quest) { QUESTS.put(quest.id(), quest); }
    public static HunterQuest get(String id) { return QUESTS.get(id); }
    public static Map<String, HunterQuest> getAll() { return Map.copyOf(QUESTS); }
}
