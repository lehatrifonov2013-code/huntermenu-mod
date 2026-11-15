package com.hunter.huntermenu.quest;

import net.neoforged.fml.ModList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class HunterQuestRegistry {
    private HunterQuestRegistry() {}

    public static final Set<String> SNIPER_GUN_IDS = Set.of(
            "tacz:ai_awp", "tacz:m700", "tacz:m95", "tacz:kar98k", "tacz:awm", "tacz:sks_tactical", "tacz:svd", "tacz:mk14"
    );

    public static final Set<String> ASSAULT_GUN_IDS = Set.of(
            "tacz:ak47", "tacz:m4a1", "tacz:scar_h", "tacz:scar_l", "tacz:hk_g3", "tacz:aug", "tacz:qbz_95"
    );

    public static final Set<String> SHOTGUN_GUN_IDS = Set.of(
            "tacz:m870", "tacz:aa12", "tacz:spas_12", "tacz:db_short", "tacz:db_long"
    );

    public static final Set<String> PISTOL_GUN_IDS = Set.of(
            "tacz:glock_17", "tacz:deagle", "tacz:deagle_golden", "tacz:m1911", "tacz:cz75", "tacz:p320"
    );

    private static final boolean ALEXSMOBS_LOADED = ModList.get().isLoaded("alexsmobs");
    private static final boolean TACZ_LOADED = ModList.get().isLoaded("tacz");

    private static final Map<String, HunterQuest> QUESTS = new LinkedHashMap<>();

    static {
        // --- 1. ОБЩИЙ ПРОМЫСЕЛ И ПРОГРЕСС (10 квестов) ---
        register(new HunterQuest("first_blood", "Первая кровь", "Добыть первого зверя", null, 0.0D, Set.of(), Set.of(), 1, 15, "minecraft:cooked_beef", 8));
        register(new HunterQuest("novice_hunter", "Начинающий следопыт", "Добыть 5 животных", null, 0.0D, Set.of(), Set.of(), 5, 30, "minecraft:leather", 12));
        register(new HunterQuest("field_tracker", "Полевой охотник", "Добыть 15 животных", null, 0.0D, Set.of(), Set.of(), 15, 60, "minecraft:iron_ingot", 8));
        register(new HunterQuest("seasoned_hunter", "Бывалый промысловик", "Добыть 30 животных", null, 0.0D, Set.of(), Set.of(), 30, 100, "minecraft:emerald", 6));
        register(new HunterQuest("veteran_hunter", "Ветеран угодий", "Добыть 60 животных", null, 0.0D, Set.of(), Set.of(), 60, 160, "minecraft:golden_apple", 2));
        register(new HunterQuest("master_hunter", "Мастер охоты", "Добыть 100 животных", null, 0.0D, Set.of(), Set.of(), 100, 250, "minecraft:diamond", 4));
        register(new HunterQuest("grand_hunter", "Гранд-мастер тайги", "Добыть 250 животных", null, 0.0D, Set.of(), Set.of(), 250, 400, "minecraft:netherite_scrap", 2));
        register(new HunterQuest("apex_predator", "Высший хищник", "Добыть 500 животных", null, 0.0D, Set.of(), Set.of(), 500, 700, "minecraft:totem_of_undying", 1));
        register(new HunterQuest("legend_of_wild", "Легенда дикой природы", "Добыть 1000 животных", null, 0.0D, Set.of(), Set.of(), 1000, 1500, "minecraft:netherite_ingot", 2));
        register(new HunterQuest("sharp_eye", "Зоркий глаз", "Добыть зверя с 40+ метров", null, 40.0D, Set.of(), Set.of(), 1, 45, "minecraft:spyglass", 1));

        // --- 2. ВАНИЛЬНАЯ ФАУНА (20 квестов) ---
        register(new HunterQuest("cow_1", "Скотовод I", "Добыть 5 коров", "minecraft:cow", 0.0D, Set.of(), Set.of(), 5, 25, "minecraft:cooked_beef", 12));
        register(new HunterQuest("cow_2", "Скотовод II", "Добыть 20 коров", "minecraft:cow", 0.0D, Set.of(), Set.of(), 20, 70, "minecraft:emerald", 4));
        register(new HunterQuest("pig_1", "Охота на кабана I", "Добыть 5 свиней", "minecraft:pig", 0.0D, Set.of(), Set.of(), 5, 25, "minecraft:cooked_porkchop", 12));
        register(new HunterQuest("pig_2", "Охота на кабана II", "Добыть 20 свиней", "minecraft:pig", 0.0D, Set.of(), Set.of(), 20, 70, "minecraft:golden_carrot", 10));
        register(new HunterQuest("sheep_1", "Горный баран I", "Добыть 5 овец", "minecraft:sheep", 0.0D, Set.of(), Set.of(), 5, 25, "minecraft:cooked_mutton", 12));
        register(new HunterQuest("sheep_2", "Горный баран II", "Добыть 20 овец", "minecraft:sheep", 0.0D, Set.of(), Set.of(), 20, 70, "minecraft:emerald", 4));
        register(new HunterQuest("chicken_1", "Птичий двор I", "Добыть 8 куриц", "minecraft:chicken", 0.0D, Set.of(), Set.of(), 8, 25, "minecraft:arrow", 24));
        register(new HunterQuest("chicken_2", "Птичий двор II", "Добыть 25 куриц", "minecraft:chicken", 0.0D, Set.of(), Set.of(), 25, 65, "minecraft:spectral_arrow", 16));
        register(new HunterQuest("rabbit_1", "Ловец зайцев I", "Добыть 3 кроликов", "minecraft:rabbit", 0.0D, Set.of(), Set.of(), 3, 40, "minecraft:rabbit_foot", 1));
        register(new HunterQuest("rabbit_2", "Ловец зайцев II", "Добыть 12 кроликов", "minecraft:rabbit", 0.0D, Set.of(), Set.of(), 12, 95, "minecraft:emerald", 6));
        register(new HunterQuest("wolf_1", "Санитар леса I", "Добыть 3 волков", "minecraft:wolf", 0.0D, Set.of(), Set.of(), 3, 50, "minecraft:bone", 16));
        register(new HunterQuest("wolf_2", "Волкодав II", "Добыть 10 волков", "minecraft:wolf", 0.0D, Set.of(), Set.of(), 10, 120, "minecraft:diamond", 2));
        register(new HunterQuest("fox_1", "Лисий след I", "Добыть 3 лис", "minecraft:fox", 0.0D, Set.of(), Set.of(), 3, 45, "minecraft:sweet_berries", 24));
        register(new HunterQuest("fox_2", "Охотник за мехом II", "Добыть 10 лис", "minecraft:fox", 0.0D, Set.of(), Set.of(), 10, 110, "minecraft:emerald", 7));
        register(new HunterQuest("polar_bear_1", "Полярный экспедитор", "Добыть 2 белых медведей", "minecraft:polar_bear", 0.0D, Set.of(), Set.of(), 2, 90, "minecraft:cooked_salmon", 16));
        register(new HunterQuest("polar_bear_2", "Хозяин Арктики", "Добыть 6 белых медведей", "minecraft:polar_bear", 0.0D, Set.of(), Set.of(), 6, 180, "minecraft:diamond", 3));
        register(new HunterQuest("goat_1", "Скалолаз", "Добыть 4 горных козлов", "minecraft:goat", 0.0D, Set.of(), Set.of(), 4, 60, "minecraft:emerald", 4));
        register(new HunterQuest("turtle_1", "Береговой патруль", "Добыть 3 черепах", "minecraft:turtle", 0.0D, Set.of(), Set.of(), 3, 55, "minecraft:scute", 2));
        register(new HunterQuest("horse_1", "Степной ветер", "Добыть 5 лошадей", "minecraft:horse", 0.0D, Set.of(), Set.of(), 5, 50, "minecraft:saddle", 1));
        register(new HunterQuest("camel_1", "Караванщик пустыни", "Добыть 3 верблюдов", "minecraft:camel", 0.0D, Set.of(), Set.of(), 3, 75, "minecraft:golden_apple", 1));

        // --- 3. БЛИЖНИЙ БОЙ И ВЫЖИВАНИЕ (15 квестов) ---
        register(new HunterQuest("melee_1", "Без выстрела", "Добыть зверя в ближнем бою", null, 0.0D, Set.of(), Set.of(), 1, 30, "minecraft:iron_ingot", 6));
        register(new HunterQuest("melee_2", "Мастер клинка I", "Добыть 10 зверей в ближнем бою", null, 0.0D, Set.of(), Set.of(), 10, 80, "minecraft:diamond", 1));
        register(new HunterQuest("melee_3", "Мастер клинка II", "Добыть 25 зверей в ближнем бою", null, 0.0D, Set.of(), Set.of(), 25, 160, "minecraft:golden_apple", 2));
        register(new HunterQuest("melee_4", "Берсерк диких земель", "Добыть 50 зверей в ближнем бою", null, 0.0D, Set.of(), Set.of(), 50, 300, "minecraft:netherite_scrap", 1));
        register(new HunterQuest("night_1", "Ночной охотник", "Добыть зверя ночью", null, 0.0D, Set.of(), Set.of(), 1, 40, "minecraft:golden_carrot", 8));
        register(new HunterQuest("night_2", "Ночной сталкер I", "Добыть 10 зверей ночью", null, 0.0D, Set.of(), Set.of(), 10, 100, "minecraft:ender_pearl", 4));
        register(new HunterQuest("night_3", "Повелитель ночи II", "Добыть 25 зверей ночью", null, 0.0D, Set.of(), Set.of(), 25, 220, "minecraft:diamond", 4));
        register(new HunterQuest("rain_1", "Охота под ливнем", "Добыть 5 зверей в дождь", null, 0.0D, Set.of(), Set.of(), 5, 85, "minecraft:emerald", 5));
        register(new HunterQuest("storm_1", "Грозовой фронт", "Добыть зверя в грозу", null, 0.0D, Set.of(), Set.of(), 1, 120, "minecraft:golden_apple", 2));
        register(new HunterQuest("survivor_5", "Осторожный следопыт", "Добыть 5 зверей без смертей", null, 0.0D, Set.of(), Set.of(), 5, 80, "minecraft:golden_carrot", 10));
        register(new HunterQuest("survivor", "Выживший", "Добыть 10 зверей, не умирая", null, 0.0D, Set.of(), Set.of(), 10, 180, "minecraft:golden_apple", 2));
        register(new HunterQuest("survivor_25", "Неуязвимый охотник", "Добыть 25 зверей без смертей", null, 0.0D, Set.of(), Set.of(), 25, 350, "minecraft:totem_of_undying", 1));
        register(new HunterQuest("survivor_50", "Бессмертная легенда", "Добыть 50 зверей без смертей", null, 0.0D, Set.of(), Set.of(), 50, 750, "minecraft:enchanted_golden_apple", 1));
        register(new HunterQuest("close_range", "В упор", "Добыть 10 зверей с дистанции < 5м", null, 0.0D, Set.of(), Set.of(), 10, 75, "minecraft:emerald", 5));
        register(new HunterQuest("mid_range", "Уверенная дистанция", "Добыть 15 зверей с 30+ метров", null, 30.0D, Set.of(), Set.of(), 15, 110, "minecraft:diamond", 2));
        // --- 4. ОГНЕСТРЕЛ И СНАЙПИНГ TACZ (15 квестов) ---
        if (TACZ_LOADED) {
            register(new HunterQuest("gun_1", "Первый выстрел", "Добыть зверя из огнестрела", null, 0.0D, Set.of(), Set.of(), 1, 25, "minecraft:iron_ingot", 4));
            register(new HunterQuest("gun_25", "Стрелок угодий", "Добыть 25 зверей из огнестрела", null, 0.0D, Set.of(), Set.of(), 25, 110, "minecraft:emerald", 6));
            register(new HunterQuest("gun_100", "Мастер баллистики", "Добыть 100 зверей из огнестрела", null, 0.0D, Set.of(), Set.of(), 100, 300, "minecraft:diamond", 4));
            register(new HunterQuest("sniper_60", "Марксман", "Добыть зверя с 60+м из винтовки", null, 60.0D, SNIPER_GUN_IDS, Set.of(), 1, 55, "minecraft:golden_carrot", 8));
            register(new HunterQuest("sniper_100", "Снайпер", "Добыть зверя с 100+м из снайперки", null, 100.0D, SNIPER_GUN_IDS, Set.of(), 1, 90, "minecraft:golden_apple", 2));
            register(new HunterQuest("sniper_150", "Дальнобойщик", "Добыть зверя с 150+м из снайперки", null, 150.0D, SNIPER_GUN_IDS, Set.of(), 1, 140, "minecraft:diamond", 2));
            register(new HunterQuest("sniper_200", "Дальний выстрел", "Добыть зверя с 200+м из снайперки", null, 200.0D, SNIPER_GUN_IDS, Set.of(), 1, 220, "minecraft:enchanted_golden_apple", 1));
            register(new HunterQuest("sniper_300", "Призрачный калибр", "Добыть зверя с 300+м из снайперки", null, 300.0D, SNIPER_GUN_IDS, Set.of(), 1, 500, "minecraft:netherite_ingot", 1));
            register(new HunterQuest("sniper_kills_10", "Снайперская серия", "Добыть 10 зверей из снайперки", null, 0.0D, SNIPER_GUN_IDS, Set.of(), 10, 130, "minecraft:emerald", 8));
            register(new HunterQuest("assault_10", "Штурмовой карабин I", "Добыть 10 зверей из штурмовой винтовки", null, 0.0D, ASSAULT_GUN_IDS, Set.of(), 10, 80, "minecraft:iron_ingot", 10));
            register(new HunterQuest("assault_40", "Штурмовой карабин II", "Добыть 40 зверей из штурмовой винтовки", null, 0.0D, ASSAULT_GUN_IDS, Set.of(), 40, 180, "minecraft:diamond", 3));
            register(new HunterQuest("shotgun_5", "Дробовик на крупного зверя I", "Добыть 5 зверей из дробовика", null, 0.0D, SHOTGUN_GUN_IDS, Set.of(), 5, 70, "minecraft:emerald", 4));
            register(new HunterQuest("shotgun_25", "Мастер картечи II", "Добыть 25 зверей из дробовика", null, 0.0D, SHOTGUN_GUN_IDS, Set.of(), 25, 160, "minecraft:golden_apple", 2));
            register(new HunterQuest("pistol_5", "Короткий ствол I", "Добыть 5 зверей из пистолета", null, 0.0D, PISTOL_GUN_IDS, Set.of(), 5, 65, "minecraft:golden_carrot", 6));
            register(new HunterQuest("pistol_20", "Ковбой диких прерий II", "Добыть 20 зверей из пистолета", null, 0.0D, PISTOL_GUN_IDS, Set.of(), 20, 150, "minecraft:diamond", 2));
        }

        // --- 5. РЕДКИЕ ЗВЕРИ ALEX'S MOBS (25 квестов) ---
        if (ALEXSMOBS_LOADED) {
            register(new HunterQuest("moose_hunt", "Лосиная охота", "Добыть лося", "alexsmobs:moose", 0.0D, Set.of(), Set.of(), 1, 75, "minecraft:emerald", 5));
            register(new HunterQuest("moose_master", "Охотник на лосей", "Добыть 5 лосей", "alexsmobs:moose", 0.0D, Set.of(), Set.of(), 5, 180, "minecraft:totem_of_undying", 1));
            register(new HunterQuest("grizzly_trophy", "Медвежий трофей", "Добыть медведя гризли", "alexsmobs:grizzly_bear", 0.0D, Set.of(), Set.of(), 1, 85, "minecraft:diamond", 2));
            register(new HunterQuest("grizzly_master", "Хозяин тайги", "Добыть 5 медведей гризли", "alexsmobs:grizzly_bear", 0.0D, Set.of(), Set.of(), 5, 220, "minecraft:netherite_scrap", 1));
            register(new HunterQuest("bird_hunter", "Пернатый трофей", "Добыть 3 лесных птиц", "alexsmobs:blue_jay", 0.0D, Set.of(), Set.of(), 3, 45, "minecraft:feather", 16));
            register(new HunterQuest("roadrunner_1", "Бегун пустыни", "Добыть 3 дорожных бегунов", "alexsmobs:roadrunner", 0.0D, Set.of(), Set.of(), 3, 55, "minecraft:emerald", 4));
            register(new HunterQuest("bison_1", "Охота на бизона I", "Добыть 2 бизонов", "alexsmobs:bison", 0.0D, Set.of(), Set.of(), 2, 80, "minecraft:cooked_beef", 20));
            register(new HunterQuest("bison_2", "Прерии грохочут II", "Добыть 8 бизонов", "alexsmobs:bison", 0.0D, Set.of(), Set.of(), 8, 190, "minecraft:diamond", 3));
            register(new HunterQuest("gazelle_1", "Быстрая газель", "Добыть 4 газелей", "alexsmobs:gazelle", 0.0D, Set.of(), Set.of(), 4, 70, "minecraft:golden_carrot", 10));
            register(new HunterQuest("kangaroo_1", "Австралийский след", "Добыть 3 кенгуру", "alexsmobs:kangaroo", 0.0D, Set.of(), Set.of(), 3, 65, "minecraft:emerald", 5));
            register(new HunterQuest("crocodile_1", "Болотный хищник I", "Добыть 2 крокодилов", "alexsmobs:crocodile", 0.0D, Set.of(), Set.of(), 2, 90, "minecraft:emerald", 6));
            register(new HunterQuest("crocodile_2", "Гроза болот II", "Добыть 6 крокодилов", "alexsmobs:crocodile", 0.0D, Set.of(), Set.of(), 6, 200, "minecraft:diamond", 3));
            register(new HunterQuest("komodo_1", "Дракон Комодо", "Добыть 2 комодских варанов", "alexsmobs:komodo_dragon", 0.0D, Set.of(), Set.of(), 2, 110, "minecraft:golden_apple", 2));
            register(new HunterQuest("anaconda_1", "Охотник джунглей", "Добыть 2 анаконд", "alexsmobs:anaconda", 0.0D, Set.of(), Set.of(), 2, 100, "minecraft:emerald", 7));
            register(new HunterQuest("bald_eagle_1", "Гордый орёл", "Добыть 2 белоголовых орланов", "alexsmobs:bald_eagle", 0.0D, Set.of(), Set.of(), 2, 95, "minecraft:diamond", 1));
            register(new HunterQuest("raccoon_1", "Ночной воришка", "Добыть 4 енотов", "alexsmobs:raccoon", 0.0D, Set.of(), Set.of(), 4, 55, "minecraft:sweet_berries", 20));
            register(new HunterQuest("skunk_1", "Опасный аромат", "Добыть 3 скунсов", "alexsmobs:skunk", 0.0D, Set.of(), Set.of(), 3, 50, "minecraft:emerald", 3));
            register(new HunterQuest("tasmanian_1", "Тасманский дьявол", "Добыть 3 тасманских дьяволов", "alexsmobs:tasmanian_devil", 0.0D, Set.of(), Set.of(), 3, 75, "minecraft:iron_ingot", 12));
            register(new HunterQuest("snow_leopard_1", "Снежный барс", "Добыть 2 снежных барсов", "alexsmobs:snow_leopard", 0.0D, Set.of(), Set.of(), 2, 140, "minecraft:diamond", 2));
            register(new HunterQuest("tiger_1", "Полосатый владыка", "Добыть 2 тигров", "alexsmobs:tiger", 0.0D, Set.of(), Set.of(), 2, 160, "minecraft:golden_apple", 2));
            register(new HunterQuest("gorilla_1", "Хозяин тропиков", "Добыть 2 горилл", "alexsmobs:gorilla", 0.0D, Set.of(), Set.of(), 2, 120, "minecraft:emerald", 8));
            register(new HunterQuest("elephant_1", "Саванный гигант", "Добыть 2 слонов", "alexsmobs:elephant", 0.0D, Set.of(), Set.of(), 2, 180, "minecraft:diamond", 3));
            register(new HunterQuest("rhinoceros_1", "Броня саванны", "Добыть 2 носорогов", "alexsmobs:rhinoceros", 0.0D, Set.of(), Set.of(), 2, 180, "minecraft:netherite_scrap", 1));
            register(new HunterQuest("walrus_1", "Ледяной бивень", "Добыть 3 моржей", "alexsmobs:walrus", 0.0D, Set.of(), Set.of(), 3, 95, "minecraft:cooked_cod", 20));
            register(new HunterQuest("seal_1", "Северный тюлень", "Добыть 4 тюленей", "alexsmobs:seal", 0.0D, Set.of(), Set.of(), 4, 65, "minecraft:emerald", 4));
        }
    }

    private static void register(HunterQuest quest) { QUESTS.put(quest.id(), quest); }
    public static HunterQuest get(String id) { return QUESTS.get(id); }
    public static Map<String, HunterQuest> getAll() { return Map.copyOf(QUESTS); }
}
