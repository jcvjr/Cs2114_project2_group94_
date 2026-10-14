package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for PokePull's game actions and its console menu.
 */
public class PokePullTest
{
    private PokePull game;

    @BeforeEach
    public void setUp()
    {
        game = new PokePull(new Random(42));
        game.startNewGame();
    }

    private Card[] firstCatalogCards()
    {
        LinkedChain<Card> catalog = CardCatalog.gen1();
        return catalog.toArray(new Card[catalog.size()]);
    }

    private Deck fullDeck(String name)
    {
        return DeckTest.fullDeck(name);
    }

    private String runMenu(String input)
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        game.run(new Scanner(input), new PrintStream(bytes));
        return bytes.toString();
    }

    // startNewGame / constructor

    @Test
    public void startNewGameGivesStarterCollectionAndPacks()
    {
        assertEquals(PokePull.STARTER_CARDS, game.getCollectionSize());
        assertTrue(game.getPacksOwned() == 2 || game.getPacksOwned() == 3);
        assertEquals(0, game.getDeckCount());
    }

    @Test
    public void startNewGameAgainResetsEverything()
    {
        game.openPack();
        game.saveDeck(fullDeck("A"));
        game.startNewGame();
        assertEquals(PokePull.STARTER_CARDS, game.getCollectionSize());
        assertEquals(0, game.getDeckCount());
    }

    @Test
    public void constructorRejectsTinyCatalog()
    {
        LinkedChain<Card> tiny = new LinkedChain<Card>();
        tiny.add(firstCatalogCards()[0]);
        assertThrows(IllegalArgumentException.class,
            () -> new PokePull(tiny, new Random(1)));
    }

    // openPack

    @Test
    public void openPackSpendsPackAndAddsCards()
    {
        int packs = game.getPacksOwned();
        LinkedChain<Card> pulled = game.openPack();
        assertEquals(packs - 1, game.getPacksOwned());
        assertEquals(PokePull.STARTER_CARDS + pulled.size(),
            game.getCollectionSize());
    }

    @Test
    public void openPackWithNoPacksThrowsAndChangesNothing()
    {
        while (game.getPacksOwned() > 0)
        {
            game.openPack();
        }
        int size = game.getCollectionSize();
        assertThrows(IllegalStateException.class, () -> game.openPack());
        assertEquals(size, game.getCollectionSize());
    }

    // tradeForPack

    @Test
    public void tradeForPackSwapsFiveOwnedCardsForAPack()
    {
        Card[] owned = ownedCards(5);
        LinkedChain<Card> offer = new LinkedChain<Card>();
        for (Card c : owned)
        {
            offer.add(c);
        }
        int packs = game.getPacksOwned();
        assertTrue(game.tradeForPack(offer));
        assertEquals(packs + 1, game.getPacksOwned());
        assertEquals(PokePull.STARTER_CARDS - 5, game.getCollectionSize());
    }

    @Test
    public void tradeForPackRejectsWrongCountOrUnownedCards()
    {
        Card[] owned = ownedCards(4);
        LinkedChain<Card> four = new LinkedChain<Card>();
        for (Card c : owned)
        {
            four.add(c);
        }
        assertFalse(game.tradeForPack(four));
        assertFalse(game.tradeForPack(null));

        LinkedChain<Card> stranger = new LinkedChain<Card>();
        Card notOwned = firstUnowned();
        for (int i = 0; i < PokePull.TRADE_COST; i++)
        {
            stranger.add(notOwned);
        }
        assertFalse(game.tradeForPack(stranger));
        assertEquals(PokePull.STARTER_CARDS, game.getCollectionSize());
    }

    @Test
    public void tradeForPackRejectsMoreCopiesThanOwned()
    {
        Card one = ownedCards(1)[0];
        LinkedChain<Card> offer = new LinkedChain<Card>();
        for (int i = 0; i < PokePull.TRADE_COST; i++)
        {
            offer.add(one);
        }
        assertFalse(game.tradeForPack(offer));
        assertEquals(PokePull.STARTER_CARDS, game.getCollectionSize());
    }

    // saveDeck / findDeck / deleteDeck / copyDeck

    @Test
    public void saveDeckStoresCompleteDeck()
    {
        game.saveDeck(fullDeck("Fire"));
        assertEquals(1, game.getDeckCount());
        assertNotNull(game.findDeck("fire"));
    }

    @Test
    public void saveDeckRejectsDuplicateNameNullAndIncompleteDeck()
    {
        game.saveDeck(fullDeck("Fire"));
        assertThrows(IllegalArgumentException.class,
            () -> game.saveDeck(fullDeck("FIRE")));
        assertThrows(IllegalArgumentException.class,
            () -> game.saveDeck(null));
        assertThrows(IllegalStateException.class,
            () -> game.saveDeck(new Deck("Short")));
        assertEquals(1, game.getDeckCount());
    }

    @Test
    public void findDeckFindsByNameAndReturnsNullIfMissing()
    {
        game.saveDeck(fullDeck("Fire"));
        assertEquals("Fire", game.findDeck("Fire").getName());
        assertNull(game.findDeck("Water"));
        assertNull(game.findDeck(null));
    }

    @Test
    public void deleteDeckRemovesIt()
    {
        game.saveDeck(fullDeck("Fire"));
        assertTrue(game.deleteDeck("Fire"));
        assertEquals(0, game.getDeckCount());
    }

    @Test
    public void deleteDeckReturnsFalseForMissingDeck()
    {
        assertFalse(game.deleteDeck("Water"));
        assertFalse(game.deleteDeck(null));
    }

    @Test
    public void copyDeckSavesIndependentCopy()
    {
        game.saveDeck(fullDeck("Fire"));
        Deck copy = game.copyDeck("Fire", "Fire 2");
        assertEquals(2, game.getDeckCount());
        assertEquals(60, copy.size());
        assertNotSame(copy, game.findDeck("Fire"));
    }

    @Test
    public void copyDeckRejectsMissingDeckBlankOrTakenName()
    {
        game.saveDeck(fullDeck("Fire"));
        assertThrows(IllegalArgumentException.class,
            () -> game.copyDeck("Water", "Water 2"));
        assertThrows(IllegalArgumentException.class,
            () -> game.copyDeck("Fire", " "));
        assertThrows(IllegalArgumentException.class,
            () -> game.copyDeck("Fire", "fire"));
        assertEquals(1, game.getDeckCount());
    }

    // ownedCopies / findOwned

    @Test
    public void ownedCopiesAndFindOwnedUseTheCollection()
    {
        Card owned = ownedCards(1)[0];
        assertEquals(1, game.ownedCopies(owned));
        assertEquals(owned, game.findOwned(owned.getName().toUpperCase()));
    }

    @Test
    public void ownedCopiesAndFindOwnedHandleUnownedCards()
    {
        Card notOwned = firstUnowned();
        assertEquals(0, game.ownedCopies(notOwned));
        assertNull(game.findOwned(notOwned.getName()));
        assertNull(game.findOwned(null));
    }

    // startHand / drawCard

    @Test
    public void startHandDrawsSevenAndLeavesSavedDeckAlone()
    {
        game.saveDeck(fullDeck("Fire"));
        LinkedChain<Card> hand = game.startHand("Fire");
        assertEquals(PokePull.HAND_SIZE, hand.size());
        assertEquals(60, game.findDeck("Fire").size());
        assertNotNull(game.drawCard());
    }

    @Test
    public void startHandRejectsUnknownDeck()
    {
        assertThrows(IllegalArgumentException.class,
            () -> game.startHand("Nope"));
    }

    @Test
    public void drawCardWithoutHandThrows()
    {
        assertThrows(IllegalStateException.class, () -> game.drawCard());
    }

    @Test
    public void drawCardFromExhaustedPileThrows()
    {
        game.saveDeck(fullDeck("Fire"));
        game.startHand("Fire");
        for (int i = 0; i < 60 - PokePull.HAND_SIZE; i++)
        {
            game.drawCard();
        }
        assertThrows(NoSuchElementException.class, () -> game.drawCard());
    }

    // console menu

    @Test
    public void menuQuitsCleanly()
    {
        assertTrue(runMenu("0\n").contains("Goodbye"));
    }

    @Test
    public void menuSurvivesGarbageInput()
    {
        String out = runMenu("abc\n\n99\n0\n");
        assertTrue(out.contains("Please enter a number"));
        assertTrue(out.contains("Goodbye"));
    }

    @Test
    public void menuSurvivesInputEndingEarly()
    {
        assertTrue(runMenu("").contains("Goodbye"));
        assertTrue(runMenu("4\n").contains("Goodbye"));
    }

    @Test
    public void menuReportsNoPacksLeft()
    {
        String out = runMenu("2\n2\n2\n2\n0\n");
        assertTrue(out.contains("You have no packs to open."));
    }

    @Test
    public void menuReportsMissingDeckAndEmptyDraw()
    {
        String out = runMenu("7\nNope\n8\nNope\n9\n0\n");
        assertTrue(out.contains("No such deck"));
        assertTrue(out.contains("Start a hand from a deck"));
    }

    @Test
    public void menuRepromptsForBlankDeckName()
    {
        // Duplicate names are covered by the saveDeck tests: run() starts a
        // new game, so no deck can already exist when the menu opens.
        String out = runMenu("4\n   \ncancel\n0\n");
        assertTrue(out.contains("can't be blank"));
        assertTrue(out.contains("Deck building cancelled"));
    }

    @Test
    public void menuRefusesFifthCopyWhileBuilding()
    {
        // run() deals the starter cards first, and a fresh PokePull with the
        // same seed deals the same ones, so we can learn an owned name.
        PokePull reference = new PokePull(new Random(5));
        reference.startNewGame();
        String name = ownedName(reference);
        PokePull fixed = new PokePull(new Random(5));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        fixed.run(new Scanner("4\nMine\n" + name + " 5\ncancel\n0\n"),
            new PrintStream(bytes));
        String out = bytes.toString();
        assertTrue(out.contains("at most 4 copies"));
        assertTrue(out.contains("remove <name>"));
    }

    private String ownedName(PokePull g)
    {
        LinkedChain<Card> catalog = CardCatalog.gen1();
        for (Card c : catalog.toArray(new Card[catalog.size()]))
        {
            if (g.ownedCopies(c) > 0)
            {
                return c.getName();
            }
        }
        throw new AssertionError("no owned card");
    }

    private Card[] ownedCards(int n)
    {
        Card[] result = new Card[n];
        int found = 0;
        for (Card c : firstCatalogCards())
        {
            if (found < n && game.ownedCopies(c) > 0)
            {
                result[found] = c;
                found++;
            }
        }
        assertEquals(n, found);
        return result;
    }

    private Card firstUnowned()
    {
        for (Card c : firstCatalogCards())
        {
            if (game.ownedCopies(c) == 0)
            {
                return c;
            }
        }
        throw new AssertionError("all cards owned");
    }
}
