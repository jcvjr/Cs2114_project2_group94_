package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for CardCatalog.
 */
public class CardCatalogTest
{
    @Test
    public void catalogHasSeventyFiveCards()
    {
        assertEquals(75, CardCatalog.gen1().size());
    }

    @Test
    public void everyCardNameIsUnique()
    {
        LinkedChain<Card> catalog = CardCatalog.gen1();
        for (Card card : catalog.toArray(new Card[catalog.size()]))
        {
            assertEquals(1, catalog.count(card), card.getName());
        }
    }

    @Test
    public void everyRarityTierHasCards()
    {
        LinkedChain<Card> catalog = CardCatalog.gen1();
        int[] perTier = new int[Rarity.values().length];
        for (Card card : catalog.toArray(new Card[catalog.size()]))
        {
            perTier[card.getRarity().ordinal()]++;
        }
        for (int count : perTier)
        {
            assertTrue(count > 0);
        }
    }

    @Test
    public void eachCallReturnsAnIndependentChain()
    {
        LinkedChain<Card> first = CardCatalog.gen1();
        first.clear();
        assertEquals(75, CardCatalog.gen1().size());
    }

    @Test
    public void catalogIsLargeEnoughForAnAllUniqueSixtyCardDeck()
    {
        assertTrue(CardCatalog.gen1().size() >= Deck.MAX_SIZE);
    }
}
