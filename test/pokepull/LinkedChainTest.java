package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the provided LinkedChain, one normal case and one bad-input case
 * for each public method. We did not write LinkedChain, but the app depends
 * on these behaviors, so the tests pin them down.
 */
public class LinkedChainTest
{
    private LinkedChain<String> chain;

    @BeforeEach
    public void setUp()
    {
        chain = new LinkedChain<String>();
    }

    private static String[] contents(LinkedChain<String> c)
    {
        return c.toArray(new String[c.size()]);
    }

    // constructor

    @Test
    public void newChainIsEmpty()
    {
        assertEquals(0, chain.size());
        assertTrue(chain.isEmpty());
        assertEquals("[]", chain.toString());
    }

    // add

    @Test
    public void addPutsEntryAtTheFront()
    {
        assertTrue(chain.add("a"));
        assertTrue(chain.add("b"));
        assertArrayEquals(new String[] {"b", "a"}, contents(chain));
        assertEquals(2, chain.size());
    }

    @Test
    public void addRejectsNull()
    {
        assertThrows(IllegalArgumentException.class, () -> chain.add(null));
        assertEquals(0, chain.size());
    }

    // addToEnd

    @Test
    public void addToEndKeepsInsertionOrder()
    {
        assertTrue(chain.addToEnd("a"));
        assertTrue(chain.addToEnd("b"));
        assertTrue(chain.addToEnd("c"));
        assertArrayEquals(new String[] {"a", "b", "c"}, contents(chain));
    }

    @Test
    public void addToEndRejectsNull()
    {
        chain.addToEnd("a");
        assertThrows(IllegalArgumentException.class,
            () -> chain.addToEnd(null));
        assertEquals(1, chain.size());
    }

    @Test
    public void addToEndStillWorksAfterRemovingTheLastEntry()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.remove("b");
        chain.addToEnd("c");
        assertArrayEquals(new String[] {"a", "c"}, contents(chain));
    }

    // remove()

    @Test
    public void removeWithNoArgumentTakesTheFirstEntry()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        assertEquals("a", chain.remove());
        assertEquals(1, chain.size());
        assertEquals("b", chain.get(0));
    }

    @Test
    public void removeWithNoArgumentOnEmptyChainReturnsNull()
    {
        assertNull(chain.remove());
        assertEquals(0, chain.size());
    }

    // remove(T)

    @Test
    public void removeEntryRemovesOnlyTheFirstMatch()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("a");
        assertTrue(chain.remove("a"));
        assertArrayEquals(new String[] {"b", "a"}, contents(chain));
    }

    @Test
    public void removeEntryWorksForMiddleAndLastEntries()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("c");
        assertTrue(chain.remove("b"));
        assertTrue(chain.remove("c"));
        assertArrayEquals(new String[] {"a"}, contents(chain));
    }

    @Test
    public void removeEntryReturnsFalseForMissingNullOrEmpty()
    {
        assertFalse(chain.remove("a"));
        chain.add("a");
        assertFalse(chain.remove("z"));
        assertFalse(chain.remove((String)null));
        assertEquals(1, chain.size());
    }

    // clear

    @Test
    public void clearEmptiesTheChain()
    {
        chain.add("a");
        chain.add("b");
        chain.clear();
        assertEquals(0, chain.size());
        assertTrue(chain.isEmpty());
        chain.add("c");
        assertArrayEquals(new String[] {"c"}, contents(chain));
    }

    @Test
    public void clearOnEmptyChainIsHarmless()
    {
        chain.clear();
        assertEquals(0, chain.size());
    }

    // get

    @Test
    public void getReturnsEntryAtPosition()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("c");
        assertEquals("a", chain.get(0));
        assertEquals("c", chain.get(2));
        assertEquals(3, chain.size());
    }

    @Test
    public void getRejectsNegativeAndTooLargeIndexes()
    {
        chain.add("a");
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> chain.get(1));
        assertThrows(IndexOutOfBoundsException.class,
            () -> new LinkedChain<String>().get(0));
    }

    // contains

    @Test
    public void containsFindsPresentEntries()
    {
        chain.add("a");
        chain.add("b");
        assertTrue(chain.contains("a"));
        assertTrue(chain.contains("b"));
    }

    @Test
    public void containsIsFalseForMissingNullOrEmpty()
    {
        assertFalse(chain.contains("a"));
        chain.add("a");
        assertFalse(chain.contains("z"));
        assertFalse(chain.contains(null));
    }

    // count

    @Test
    public void countCountsRepeats()
    {
        chain.add("a");
        chain.add("b");
        chain.add("a");
        assertEquals(2, chain.count("a"));
        assertEquals(1, chain.count("b"));
    }

    @Test
    public void countIsZeroForMissingOrNull()
    {
        chain.add("a");
        assertEquals(0, chain.count("z"));
        assertEquals(0, chain.count(null));
        assertEquals(0, new LinkedChain<String>().count("a"));
    }

    // size / isEmpty

    @Test
    public void sizeAndIsEmptyTrackAddsAndRemoves()
    {
        chain.add("a");
        chain.add("b");
        assertEquals(2, chain.size());
        assertFalse(chain.isEmpty());
        chain.remove();
        chain.remove("a");
        assertEquals(0, chain.size());
        assertTrue(chain.isEmpty());
    }

    @Test
    public void sizeDoesNotChangeWhenARemoveFindsNothing()
    {
        chain.add("a");
        chain.remove("z");
        chain.add("b");
        assertThrows(IllegalArgumentException.class, () -> chain.add(null));
        assertEquals(2, chain.size());
    }

    // toArray

    @Test
    public void toArrayListsEntriesFirstToLast()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        String[] given = new String[2];
        String[] result = chain.toArray(given);
        assertSame(given, result);
        assertArrayEquals(new String[] {"a", "b"}, result);
    }

    @Test
    public void toArrayAcceptsALargerArrayAndLeavesTheRestAlone()
    {
        chain.addToEnd("a");
        String[] result = chain.toArray(new String[] {"x", "y", "z"});
        assertArrayEquals(new String[] {"a", "y", "z"}, result);
    }

    @Test
    public void toArrayRejectsNullAndTooSmallArrays()
    {
        chain.add("a");
        chain.add("b");
        assertThrows(IllegalArgumentException.class,
            () -> chain.toArray(null));
        assertThrows(IllegalArgumentException.class,
            () -> chain.toArray(new String[1]));
    }

    // toString

    @Test
    public void toStringListsEntriesInOrder()
    {
        chain.addToEnd("a");
        chain.addToEnd("b");
        chain.addToEnd("c");
        assertEquals("[a, b, c]", chain.toString());
    }

    @Test
    public void toStringOfEmptyAndSingleEntryChains()
    {
        assertEquals("[]", chain.toString());
        chain.add("only");
        assertEquals("[only]", chain.toString());
    }
}
