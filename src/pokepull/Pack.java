package pokepull;

import java.util.Random;

/**
 * Generates the cards in one opened pack. Each card's rarity is rolled with
 * weights, and every fifth pack at the latest is guaranteed to hold a card
 * of RARE or better.
 *
 * @version Fall 2026
 */
public class Pack
{
    /** Fewest cards in a pack. */
    public static final int MIN_CARDS = 8;
    /** Most cards in a pack. */
    public static final int MAX_CARDS = 10;
    /** After this many packs without a RARE-or-better card, force one. */
    public static final int GUARANTEE_AFTER = 4;

    /**
     * Exclusive upper bound of each tier on a 0..999 roll, in Rarity order:
     * 60% COMMON, 25% UNCOMMON, 10% RARE, 4% SUPER_RARE, 1% LEGENDARY.
     */
    private static final int[] CUTOFFS = {600, 850, 950, 990, 1000};

    private final LinkedChain<Card> cardPool;
    private final Random rng;
    private int packsSinceRare;

    /**
     * Creates a pack generator.
     *
     * @param cardPool every card that can be pulled; copied
     * @param rng      the source of randomness
     * @throws IllegalArgumentException if either argument is null or the
     *                                  pool is empty
     */
    public Pack(LinkedChain<Card> cardPool, Random rng)
    {
        if (cardPool == null || cardPool.isEmpty())
        {
            throw new IllegalArgumentException(
                "The card pool must not be null or empty");
        }
        if (rng == null)
        {
            throw new IllegalArgumentException("The Random must not be null");
        }
        this.cardPool = Deck.copyOf(cardPool);
        this.rng = rng;
        this.packsSinceRare = 0;
    }

    /**
     * Opens a pack.
     *
     * @return 8 to 10 cards
     */
    public LinkedChain<Card> open()
    {
        int size = MIN_CARDS + rng.nextInt(MAX_CARDS - MIN_CARDS + 1);
        int forcedSlot = -1;
        if (packsSinceRare >= GUARANTEE_AFTER)
        {
            forcedSlot = rng.nextInt(size);
        }
        LinkedChain<Card> result = new LinkedChain<Card>();
        boolean hasRare = false;
        for (int i = 0; i < size; i++)
        {
            Rarity wanted = rollRarity();
            if (i == forcedSlot && wanted.ordinal() < Rarity.RARE.ordinal())
            {
                wanted = Rarity.RARE;
            }
            Card card = pickCard(wanted);
            result.addToEnd(card);
            if (card.getRarity().ordinal() >= Rarity.RARE.ordinal())
            {
                hasRare = true;
            }
        }
        packsSinceRare = hasRare ? 0 : packsSinceRare + 1;
        return result;
    }

    /**
     * @return packs opened since the last pack with a RARE-or-better card
     */
    public int getPacksSinceRare()
    {
        return packsSinceRare;
    }

    /**
     * Rolls a rarity using the weights.
     *
     * @return the rarity rolled
     */
    private Rarity rollRarity()
    {
        int roll = rng.nextInt(CUTOFFS[CUTOFFS.length - 1]);
        Rarity[] tiers = Rarity.values();
        for (int i = 0; i < tiers.length; i++)
        {
            if (roll < CUTOFFS[i])
            {
                return tiers[i];
            }
        }
        return Rarity.COMMON;
    }

    /**
     * Picks a random card of the given rarity. If the pool has none of that
     * rarity, tries each lower tier, then each higher tier.
     *
     * @param wanted the rarity to look for
     * @return a card from the pool
     */
    private Card pickCard(Rarity wanted)
    {
        Card[] all = cardPool.toArray(new Card[cardPool.size()]);
        Rarity[] tiers = Rarity.values();
        for (int tier = wanted.ordinal(); tier >= 0; tier--)
        {
            Card found = randomOfTier(all, tiers[tier]);
            if (found != null)
            {
                return found;
            }
        }
        for (int tier = wanted.ordinal() + 1; tier < tiers.length; tier++)
        {
            Card found = randomOfTier(all, tiers[tier]);
            if (found != null)
            {
                return found;
            }
        }
        return all[rng.nextInt(all.length)];
    }

    /**
     * @param all  the cards to choose from
     * @param tier the rarity wanted
     * @return a random card of that rarity, or null if there is none
     */
    private Card randomOfTier(Card[] all, Rarity tier)
    {
        int matches = 0;
        for (Card card : all)
        {
            if (card.getRarity() == tier)
            {
                matches++;
            }
        }
        if (matches == 0)
        {
            return null;
        }
        int target = rng.nextInt(matches);
        for (Card card : all)
        {
            if (card.getRarity() == tier)
            {
                if (target == 0)
                {
                    return card;
                }
                target--;
            }
        }
        return null;
    }
}
