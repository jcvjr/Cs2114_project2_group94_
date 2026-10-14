package pokepull;

/**
 * One immutable Pokemon card. Two cards are equal when they have the same
 * name (ignoring case), so repeat pulls of the same card are interchangeable.
 *
 * @version Fall 2026
 */
public class Card
{
    private final String name;
    private final String type;
    private final String[] moves;
    private final int health;
    private final Rarity rarity;

    /**
     * Creates a card.
     *
     * @param name   the card name; must not be null or blank
     * @param type   the element type; must not be null or blank
     * @param moves  the card's moves; must not be null, empty, or contain
     *               a blank entry. The array is copied.
     * @param health hit points; must be positive
     * @param rarity the rarity tier; must not be null
     * @throws IllegalArgumentException if any argument is invalid
     */
    public Card(String name, String type, String[] moves, int health,
        Rarity rarity)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Card name must not be blank");
        }
        if (type == null || type.isBlank())
        {
            throw new IllegalArgumentException("Card type must not be blank");
        }
        if (moves == null || moves.length == 0)
        {
            throw new IllegalArgumentException(
                "A card needs at least one move");
        }
        for (String move : moves)
        {
            if (move == null || move.isBlank())
            {
                throw new IllegalArgumentException(
                    "Card moves must not be blank");
            }
        }
        if (health <= 0)
        {
            throw new IllegalArgumentException("Card health must be positive");
        }
        if (rarity == null)
        {
            throw new IllegalArgumentException("Card rarity must not be null");
        }
        this.name = name.trim();
        this.type = type.trim();
        this.moves = moves.clone();
        this.health = health;
        this.rarity = rarity;
    }

    /**
     * @return the card name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the element type
     */
    public String getType()
    {
        return type;
    }

    /**
     * @return a copy of the moves array
     */
    public String[] getMoves()
    {
        return moves.clone();
    }

    /**
     * @return the hit points
     */
    public int getHealth()
    {
        return health;
    }

    /**
     * @return the rarity tier
     */
    public Rarity getRarity()
    {
        return rarity;
    }

    /**
     * Cards are equal if their names match, ignoring case.
     *
     * @param other the object to compare with
     * @return true if other is a Card with the same name
     */
    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (!(other instanceof Card))
        {
            return false;
        }
        return name.equalsIgnoreCase(((Card)other).name);
    }

    /**
     * @return a hash code consistent with equals()
     */
    @Override
    public int hashCode()
    {
        return name.toLowerCase().hashCode();
    }

    /**
     * @return a one-line description such as "Pikachu (Electric, 35 HP,
     *         UNCOMMON)"
     */
    @Override
    public String toString()
    {
        return name + " (" + type + ", " + health + " HP, " + rarity + ")";
    }
}
