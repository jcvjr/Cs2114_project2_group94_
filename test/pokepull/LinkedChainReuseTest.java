package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Shows that the one generic LinkedChain stores several element types and
 * that the unmodified class behaves as the application relies on.
 */
public class LinkedChainReuseTest
{
    @Test
    public void sameClassStoresCardsDecksAndStrings()
    {
        LinkedChain<Card> cards = new LinkedChain<Card>();
        LinkedChain<Deck> decks = new LinkedChain<Deck>();
        LinkedChain<String> names = new LinkedChain<String>();
        Card pikachu = new Card("Pikachu", "Electric", new String[] {"Zap"},
            35, Rarity.UNCOMMON);
        Deck deck = new Deck("Fire");
        cards.add(pikachu);
        decks.add(deck);
        names.add("pikachu");
        assertSame(pikachu, cards.get(0));
        assertSame(deck, decks.get(0));
        assertTrue(names.contains("pikachu"));
    }

    @Test
    public void addRejectsNull()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new LinkedChain<Card>().add(null));
    }

    @Test
    public void countUsesEqualsSoRepeatCardsCountTogether()
    {
        LinkedChain<Card> cards = new LinkedChain<Card>();
        cards.add(new Card("Pikachu", "Electric", new String[] {"A"}, 1,
            Rarity.COMMON));
        cards.add(new Card("pikachu", "Electric", new String[] {"B"}, 2,
            Rarity.RARE));
        assertEquals(2, cards.count(new Card("PIKACHU", "X",
            new String[] {"C"}, 3, Rarity.COMMON)));
    }
}
