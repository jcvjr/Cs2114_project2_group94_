package pokepull;

/**
 * The catalog of Gen 1 cards that can appear in packs. Holds data only.
 *
 * @version Fall 2026
 */
public final class CardCatalog
{
    /** name | type | moves (separated by ;) | health | rarity */
    private static final String[] ROWS = {
        "Caterpie|Bug|Tackle;String Shot|40|COMMON",
        "Weedle|Bug|Poison Sting|40|COMMON",
        "Pidgey|Flying|Gust|40|COMMON",
        "Rattata|Normal|Bite|30|COMMON",
        "Spearow|Flying|Peck|40|COMMON",
        "Ekans|Poison|Wrap|40|COMMON",
        "Sandshrew|Ground|Scratch|40|COMMON",
        "Zubat|Poison|Supersonic;Leech Life|40|COMMON",
        "Oddish|Grass|Absorb|50|COMMON",
        "Paras|Bug|Scratch;Spore|40|COMMON",
        "Diglett|Ground|Dig|30|COMMON",
        "Meowth|Normal|Pay Day|50|COMMON",
        "Psyduck|Water|Headache;Fury Swipes|50|COMMON",
        "Machop|Fighting|Karate Chop|50|COMMON",
        "Geodude|Rock|Stone Barrage|50|COMMON",
        "Magikarp|Water|Tackle;Flail|30|COMMON",
        "Krabby|Water|Bubble;Vice Grip|50|COMMON",
        "Voltorb|Electric|Tackle;Selfdestruct|40|COMMON",
        "Horsea|Water|Bubble|40|COMMON",
        "Pikachu|Electric|Thunder Shock;Thunderbolt|60|UNCOMMON",
        "Vulpix|Fire|Ember|50|UNCOMMON",
        "Growlithe|Fire|Bite;Flamethrower|60|UNCOMMON",
        "Poliwag|Water|Water Gun|50|UNCOMMON",
        "Abra|Psychic|Teleport;Psyshock|40|UNCOMMON",
        "Bellsprout|Grass|Vine Whip|50|UNCOMMON",
        "Ponyta|Fire|Stomp;Fire Spin|60|UNCOMMON",
        "Magnemite|Electric|Thunder Wave;Spark|40|UNCOMMON",
        "Gastly|Ghost|Lick;Night Shade|50|UNCOMMON",
        "Staryu|Water|Slap;Swift|40|UNCOMMON",
        "Eevee|Normal|Tackle;Quick Attack|50|UNCOMMON",
        "Charmander|Fire|Scratch;Ember|50|RARE",
        "Squirtle|Water|Bubble;Withdraw|40|RARE",
        "Bulbasaur|Grass|Leech Seed;Vine Whip|40|RARE",
        "Machoke|Fighting|Low Kick;Submission|80|RARE",
        "Kadabra|Psychic|Recover;Confuse Ray|60|RARE",
        "Haunter|Ghost|Hypnosis;Dream Eater|60|RARE",
        "Dratini|Dragon|Wrap;Slam|40|RARE",
        "Charizard|Fire|Fire Spin;Flamethrower|120|SUPER_RARE",
        "Blastoise|Water|Hydro Pump;Rain Dance|100|SUPER_RARE",
        "Venusaur|Grass|Solar Beam;Razor Leaf|100|SUPER_RARE",
        "Gyarados|Water|Dragon Rage;Bubblebeam|100|SUPER_RARE",
        "Alakazam|Psychic|Confuse Ray;Psychic|80|SUPER_RARE",
        "Gengar|Ghost|Dark Mind;Curse|80|SUPER_RARE",
        "Dragonite|Dragon|Slam;Hyper Beam|100|SUPER_RARE",
        "Articuno|Ice|Blizzard;Ice Beam|90|LEGENDARY",
        "Zapdos|Electric|Thunder;Thunderbolt|90|LEGENDARY",
        "Moltres|Fire|Wildfire;Dive Bomb|90|LEGENDARY",
        "Mewtwo|Psychic|Psychic;Barrier|60|LEGENDARY",
        "Mew|Psychic|Psywave;Devolution Beam|50|LEGENDARY"
    };

    private CardCatalog()
    {
        // utility class
    }

    /**
     * @return a new chain holding every card in the catalog
     */
    public static LinkedChain<Card> gen1()
    {
        LinkedChain<Card> chain = new LinkedChain<Card>();
        for (String row : ROWS)
        {
            String[] f = row.split("\\|");
            chain.addToEnd(new Card(f[0], f[1], f[2].split(";"),
                Integer.parseInt(f[3]), Rarity.valueOf(f[4])));
        }
        return chain;
    }
}
