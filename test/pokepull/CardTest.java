package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests for Card.
 */
public class CardTest
{
    private static Card pikachu()
    {
        return new Card("Pikachu", "Electric", new String[] {"Thunder Shock"},
            35, Rarity.UNCOMMON);
    }

    @Test
    public void constructorKeepsAllFields()
    {
        Card c = pikachu();
        assertEquals("Pikachu", c.getName());
        assertEquals("Electric", c.getType());
        assertEquals(35, c.getHealth());
        assertEquals(Rarity.UNCOMMON, c.getRarity());
        assertArrayEquals(new String[] {"Thunder Shock"}, c.getMoves());
    }

    @Test
    public void constructorRejectsBadData()
    {
        String[] moves = {"Tackle"};
        assertThrows(IllegalArgumentException.class,
            () -> new Card(" ", "Fire", moves, 10, Rarity.COMMON));
        assertThrows(IllegalArgumentException.class,
            () -> new Card(null, "Fire", moves, 10, Rarity.COMMON));
        assertThrows(IllegalArgumentException.class,
            () -> new Card("A", "", moves, 10, Rarity.COMMON));
        assertThrows(IllegalArgumentException.class,
            () -> new Card("A", "Fire", new String[0], 10, Rarity.COMMON));
        assertThrows(IllegalArgumentException.class,
            () -> new Card("A", "Fire", null, 10, Rarity.COMMON));
        assertThrows(IllegalArgumentException.class,
            () -> new Card("A", "Fire", new String[] {" "}, 10,
                Rarity.COMMON));
        assertThrows(IllegalArgumentException.class,
            () -> new Card("A", "Fire", moves, 0, Rarity.COMMON));
        assertThrows(IllegalArgumentException.class,
            () -> new Card("A", "Fire", moves, 10, null));
    }

    @Test
    public void gettersDoNotExposeInternalMoves()
    {
        Card c = pikachu();
        c.getMoves()[0] = "Changed";
        assertEquals("Thunder Shock", c.getMoves()[0]);
    }

    @Test
    public void inputArrayChangesDoNotAffectCard()
    {
        String[] moves = {"Tackle"};
        Card c = new Card("A", "Fire", moves, 10, Rarity.COMMON);
        moves[0] = "Changed";
        assertEquals("Tackle", c.getMoves()[0]);
    }

    @Test
    public void equalsComparesNamesIgnoringCase()
    {
        Card other = new Card("PIKACHU", "Water", new String[] {"X"}, 99,
            Rarity.LEGENDARY);
        assertEquals(pikachu(), other);
        assertEquals(pikachu().hashCode(), other.hashCode());
    }

    @Test
    public void equalsRejectsDifferentNameAndNull()
    {
        Card charmander = new Card("Charmander", "Fire", new String[] {"X"},
            50, Rarity.RARE);
        assertNotEquals(pikachu(), charmander);
        assertNotEquals(null, pikachu());
        assertNotEquals("Pikachu", pikachu());
    }

    @Test
    public void toStringDescribesCard()
    {
        assertEquals("Pikachu (Electric, 35 HP, UNCOMMON)",
            pikachu().toString());
    }
}
