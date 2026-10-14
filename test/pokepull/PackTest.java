package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Tests for Pack. Every random test uses a fixed seed.
 */
public class PackTest
{
    private static Card card(String name, Rarity rarity)
    {
        return new Card(name, "Normal", new String[] {"Tackle"}, 10, rarity);
    }

    @Test
    public void constructorAcceptsNormalPool()
    {
        Pack p = new Pack(CardCatalog.gen1(), new Random(1));
        assertEquals(0, p.getPacksSinceRare());
    }

    @Test
    public void constructorRejectsBadArguments()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new Pack(null, new Random(1)));
        assertThrows(IllegalArgumentException.class,
            () -> new Pack(new LinkedChain<Card>(), new Random(1)));
        assertThrows(IllegalArgumentException.class,
            () -> new Pack(CardCatalog.gen1(), null));
    }

    @Test
    public void openReturnsEightToTenCardsFromPool()
    {
        LinkedChain<Card> pool = CardCatalog.gen1();
        Pack p = new Pack(pool, new Random(7));
        for (int i = 0; i < 100; i++)
        {
            LinkedChain<Card> cards = p.open();
            assertTrue(cards.size() >= Pack.MIN_CARDS);
            assertTrue(cards.size() <= Pack.MAX_CARDS);
            for (Card c : cards.toArray(new Card[cards.size()]))
            {
                assertTrue(pool.contains(c));
            }
        }
    }

    @Test
    public void openFallsBackWhenTiersAreMissing()
    {
        LinkedChain<Card> pool = new LinkedChain<Card>();
        pool.add(card("A", Rarity.COMMON));
        pool.add(card("B", Rarity.COMMON));
        Pack p = new Pack(pool, new Random(3));
        for (int i = 0; i < 50; i++)
        {
            LinkedChain<Card> cards = p.open();
            assertTrue(cards.size() >= Pack.MIN_CARDS);
            for (Card c : cards.toArray(new Card[cards.size()]))
            {
                assertEquals(Rarity.COMMON, c.getRarity());
            }
        }
    }

    @Test
    public void poolWithOnlyRareStillWorks()
    {
        LinkedChain<Card> pool = new LinkedChain<Card>();
        pool.add(card("Solo", Rarity.LEGENDARY));
        Pack p = new Pack(pool, new Random(3));
        LinkedChain<Card> cards = p.open();
        assertEquals(cards.size(), cards.count(card("Solo", Rarity.COMMON)));
    }

    @Test
    public void rareCardGuaranteedWithinFivePacks()
    {
        Pack p = new Pack(CardCatalog.gen1(), new Random(11));
        int streakWithoutRare = 0;
        for (int i = 0; i < 2000; i++)
        {
            LinkedChain<Card> cards = p.open();
            if (hasRareOrBetter(cards))
            {
                streakWithoutRare = 0;
            }
            else
            {
                streakWithoutRare++;
            }
            assertTrue(streakWithoutRare <= Pack.GUARANTEE_AFTER);
            assertTrue(p.getPacksSinceRare() <= Pack.GUARANTEE_AFTER);
        }
    }

    @Test
    public void guaranteeWorksWhenOnlyOneRareCardExists()
    {
        LinkedChain<Card> pool = new LinkedChain<Card>();
        for (int i = 0; i < 20; i++)
        {
            pool.add(card("Common" + i, Rarity.COMMON));
        }
        pool.add(card("TheRare", Rarity.RARE));
        Pack p = new Pack(pool, new Random(5));
        int streakWithoutRare = 0;
        for (int i = 0; i < 500; i++)
        {
            if (hasRareOrBetter(p.open()))
            {
                streakWithoutRare = 0;
            }
            else
            {
                streakWithoutRare++;
            }
            assertTrue(streakWithoutRare <= Pack.GUARANTEE_AFTER);
        }
    }

    @Test
    public void rarityWeightsAreRoughlyAsDesigned()
    {
        Pack p = new Pack(CardCatalog.gen1(), new Random(2026));
        int[] seen = new int[Rarity.values().length];
        int total = 0;
        for (int i = 0; i < 1000; i++)
        {
            LinkedChain<Card> cards = p.open();
            for (Card c : cards.toArray(new Card[cards.size()]))
            {
                seen[c.getRarity().ordinal()]++;
                total++;
            }
        }
        double[] expected = {0.60, 0.25, 0.10, 0.04, 0.01};
        for (int t = 0; t < seen.length; t++)
        {
            assertEquals(expected[t], (double)seen[t] / total, 0.03,
                "share of " + Rarity.values()[t]);
        }
    }

    private static boolean hasRareOrBetter(LinkedChain<Card> cards)
    {
        for (Card c : cards.toArray(new Card[cards.size()]))
        {
            if (c.getRarity().ordinal() >= Rarity.RARE.ordinal())
            {
                return true;
            }
        }
        return false;
    }
}
