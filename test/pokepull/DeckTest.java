package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for Deck.
 */
public class DeckTest
{
    private Card pikachu;
    private Card charmander;
    private Deck deck;

    @BeforeEach
    public void setUp()
    {
        pikachu = new Card("Pikachu", "Electric", new String[] {"Zap"}, 35,
            Rarity.UNCOMMON);
        charmander = new Card("Charmander", "Fire", new String[] {"Ember"},
            50, Rarity.RARE);
        deck = new Deck("Fire");
    }

    /** Builds a complete deck of 15 different cards, 4 copies each. */
    static Deck fullDeck(String name)
    {
        Deck d = new Deck(name);
        LinkedChain<Card> catalog = CardCatalog.gen1();
        Card[] all = catalog.toArray(new Card[catalog.size()]);
        for (int i = 0; i < 15; i++)
        {
            for (int copy = 0; copy < Deck.MAX_COPIES; copy++)
            {
                d.addCard(all[i]);
            }
        }
        return d;
    }

    @Test
    public void constructorStartsEmpty()
    {
        assertEquals("Fire", deck.getName());
        assertEquals(0, deck.size());
        assertFalse(deck.isSavable());
    }

    @Test
    public void constructorRejectsBlankName()
    {
        assertThrows(IllegalArgumentException.class, () -> new Deck("  "));
        assertThrows(IllegalArgumentException.class, () -> new Deck(null));
    }

    @Test
    public void addCardCountsCopies()
    {
        deck.addCard(pikachu);
        deck.addCard(pikachu);
        deck.addCard(pikachu);
        assertEquals(3, deck.size());
        assertEquals(3, deck.countOf(pikachu));
        assertEquals(0, deck.countOf(charmander));
    }

    @Test
    public void addCardRejectsFifthCopy()
    {
        for (int i = 0; i < Deck.MAX_COPIES; i++)
        {
            deck.addCard(pikachu);
        }
        assertThrows(IllegalStateException.class,
            () -> deck.addCard(pikachu));
        assertEquals(4, deck.countOf(pikachu));
    }

    @Test
    public void addCardRejectsCardsPastSixty()
    {
        Deck full = fullDeck("Full");
        assertEquals(60, full.size());
        assertTrue(full.isSavable());
        Card extra = new Card("Extra", "Fire", new String[] {"X"}, 10,
            Rarity.COMMON);
        assertThrows(IllegalStateException.class, () -> full.addCard(extra));
        assertEquals(60, full.size());
    }

    @Test
    public void addCardRejectsNull()
    {
        assertThrows(IllegalArgumentException.class,
            () -> deck.addCard(null));
    }

    @Test
    public void removeCardRemovesOneCopy()
    {
        deck.addCard(pikachu);
        deck.addCard(pikachu);
        assertTrue(deck.removeCard(pikachu));
        assertEquals(1, deck.countOf(pikachu));
    }

    @Test
    public void removeCardReturnsFalseWhenAbsent()
    {
        assertFalse(deck.removeCard(charmander));
    }

    @Test
    public void isSavableNeedsBetweenTwentyAndSixtyCards()
    {
        Deck full = fullDeck("Full");
        assertTrue(full.isSavable());
        Card[] all = full.toArray();
        while (full.size() > Deck.MIN_SIZE)
        {
            full.removeCard(all[full.size() - 1]);
        }
        assertEquals(Deck.MIN_SIZE, full.size());
        assertTrue(full.isSavable());
        full.removeCard(full.toArray()[0]);
        assertFalse(full.isSavable());
        assertFalse(new Deck("Empty").isSavable());
    }

    @Test
    public void copyIsIndependent()
    {
        deck.addCard(pikachu);
        deck.addCard(charmander);
        Deck copy = deck.copy("Fire 2");
        assertEquals("Fire 2", copy.getName());
        assertEquals(2, copy.size());
        copy.addCard(pikachu);
        assertEquals(2, deck.size());
        assertEquals(3, copy.size());
    }

    @Test
    public void copyRejectsBlankName()
    {
        assertThrows(IllegalArgumentException.class, () -> deck.copy(""));
    }

    @Test
    public void drawRandomRemovesEveryCardEventually()
    {
        deck.addCard(pikachu);
        deck.addCard(charmander);
        deck.addCard(pikachu);
        Random rng = new Random(1);
        int pikachuCount = 0;
        for (int i = 0; i < 3; i++)
        {
            if (deck.drawRandom(rng).equals(pikachu))
            {
                pikachuCount++;
            }
        }
        assertEquals(2, pikachuCount);
        assertEquals(0, deck.size());
    }

    @Test
    public void drawRandomFromEmptyDeckThrows()
    {
        assertThrows(NoSuchElementException.class,
            () -> deck.drawRandom(new Random(1)));
    }

    @Test
    public void drawCardsTakesNCardsFromDeck()
    {
        Deck full = fullDeck("Full");
        LinkedChain<Card> hand = full.drawCards(7, new Random(1));
        assertEquals(7, hand.size());
        assertEquals(53, full.size());
    }

    @Test
    public void drawCardsRejectsTooManyOrNegative()
    {
        deck.addCard(pikachu);
        assertThrows(IllegalStateException.class,
            () -> deck.drawCards(2, new Random(1)));
        assertThrows(IllegalArgumentException.class,
            () -> deck.drawCards(-1, new Random(1)));
        assertEquals(1, deck.size());
    }

    @Test
    public void getCardsReturnsIndependentCopy()
    {
        deck.addCard(pikachu);
        LinkedChain<Card> shown = deck.getCards();
        shown.clear();
        assertEquals(1, deck.size());
    }
}
