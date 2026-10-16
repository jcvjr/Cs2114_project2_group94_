package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Tests for the parts of the GUI that don't need a window: the helper that
 * lists distinct cards and the PokePull accessors the GUI relies on.
 */
public class PokePullGuiTest
{
    private static Card card(String name)
    {
        return new Card(name, "Normal", new String[] {"Tackle"}, 10,
            Rarity.COMMON);
    }

    @Test
    public void distinctListsEachCardOnceInFirstSeenOrder()
    {
        Card[] cards = {card("A"), card("B"), card("a"), card("C"),
            card("B")};
        Card[] result = PokePullGui.distinct(cards);
        assertEquals(3, result.length);
        assertEquals("A", result[0].getName());
        assertEquals("B", result[1].getName());
        assertEquals("C", result[2].getName());
    }

    @Test
    public void distinctOfNoCardsIsEmpty()
    {
        assertEquals(0, PokePullGui.distinct(new Card[0]).length);
    }

    @Test
    public void getCollectionCardsMatchesSizeAndIsASnapshot()
    {
        PokePull game = new PokePull(new Random(9));
        game.startNewGame();
        Card[] snapshot = game.getCollectionCards();
        assertEquals(game.getCollectionSize(), snapshot.length);
        snapshot[0] = null;
        assertNotNull(game.getCollectionCards()[0]);
    }

    @Test
    public void getCollectionCardsIsEmptyBeforeNewGame()
    {
        PokePull game = new PokePull(new Random(9));
        assertEquals(0, game.getCollectionCards().length);
    }

    @Test
    public void getDecksListsSavedDecks()
    {
        PokePull game = new PokePull(new Random(9));
        game.startNewGame();
        game.saveDeck(DeckTest.fullDeck("Fire"));
        Deck[] decks = game.getDecks();
        assertEquals(1, decks.length);
        assertEquals("Fire", decks[0].getName());
    }

    @Test
    public void getDecksIsEmptyWithNoDecks()
    {
        PokePull game = new PokePull(new Random(9));
        assertEquals(0, game.getDecks().length);
    }
}
