package pokepull;

import java.util.NoSuchElementException;
import java.util.Random;

/**
 * One named deck of cards. Enforces the deck-size and copy-limit rules and
 * can draw random cards. All storage is a LinkedChain of Card.
 *
 * @version Fall 2026
 */
public class Deck
{
    /** Cards in a complete deck. */
    public static final int MAX_SIZE = 60;
    /** Most copies of one card allowed in a deck. */
    public static final int MAX_COPIES = 4;

    private final String name;
    private final LinkedChain<Card> cards;

    /**
     * Creates an empty deck.
     *
     * @param name the deck name; must not be null or blank
     * @throws IllegalArgumentException if name is null or blank
     */
    public Deck(String name)
    {
        if (name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Deck name must not be blank");
        }
        this.name = name.trim();
        this.cards = new LinkedChain<Card>();
    }

    /**
     * @return the deck name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Adds one card to the deck.
     *
     * @param card the card to add
     * @throws IllegalArgumentException if card is null
     * @throws IllegalStateException    if the deck already has MAX_SIZE
     *                                  cards or MAX_COPIES copies of card
     */
    public void addCard(Card card)
    {
        if (card == null)
        {
            throw new IllegalArgumentException("Card must not be null");
        }
        if (cards.size() >= MAX_SIZE)
        {
            throw new IllegalStateException(
                "The deck is full (" + MAX_SIZE + " cards).");
        }
        if (cards.count(card) >= MAX_COPIES)
        {
            throw new IllegalStateException("A deck can hold at most "
                + MAX_COPIES + " copies of " + card.getName() + ".");
        }
        cards.add(card);
    }

    /**
     * Removes one copy of a card.
     *
     * @param card the card to remove
     * @return true if a copy was removed, false if the deck had none
     */
    public boolean removeCard(Card card)
    {
        return cards.remove(card);
    }

    /**
     * @param card the card to look for
     * @return the number of copies of card in the deck
     */
    public int countOf(Card card)
    {
        return cards.count(card);
    }

    /**
     * @return the number of cards in the deck
     */
    public int size()
    {
        return cards.size();
    }

    /**
     * @return true if the deck holds exactly MAX_SIZE cards
     */
    public boolean isComplete()
    {
        return cards.size() == MAX_SIZE;
    }

    /**
     * Makes an independent deck with the same cards.
     *
     * @param newName the new deck's name; must not be null or blank
     * @return the copy
     * @throws IllegalArgumentException if newName is null or blank
     */
    public Deck copy(String newName)
    {
        Deck result = new Deck(newName);
        result.cards.clear();
        Card[] all = toArray();
        for (int i = all.length - 1; i >= 0; i--)
        {
            result.cards.add(all[i]);
        }
        return result;
    }

    /**
     * Removes and returns one uniformly random card.
     *
     * @param rng the source of randomness
     * @return the card drawn
     * @throws NoSuchElementException if the deck is empty
     */
    public Card drawRandom(Random rng)
    {
        if (cards.isEmpty())
        {
            throw new NoSuchElementException("The deck is empty.");
        }
        Card drawn = cards.get(rng.nextInt(cards.size()));
        cards.remove(drawn);
        return drawn;
    }

    /**
     * Removes n random cards from this deck and returns them.
     *
     * @param n   how many cards to draw; must not be negative
     * @param rng the source of randomness
     * @return a new chain holding the drawn cards
     * @throws IllegalArgumentException if n is negative
     * @throws IllegalStateException    if the deck has fewer than n cards
     */
    public LinkedChain<Card> drawCards(int n, Random rng)
    {
        if (n < 0)
        {
            throw new IllegalArgumentException("Cannot draw " + n + " cards");
        }
        if (cards.size() < n)
        {
            throw new IllegalStateException("The deck has only "
                + cards.size() + " cards; cannot draw " + n + ".");
        }
        LinkedChain<Card> drawn = new LinkedChain<Card>();
        for (int i = 0; i < n; i++)
        {
            drawn.addToEnd(drawRandom(rng));
        }
        return drawn;
    }

    /**
     * @return a copy of the deck's cards, for display
     */
    public LinkedChain<Card> getCards()
    {
        return copyOf(cards);
    }

    /**
     * @return the deck's cards in an array (a snapshot)
     */
    public Card[] toArray()
    {
        return cards.toArray(new Card[cards.size()]);
    }

    /**
     * @return the deck name and size
     */
    @Override
    public String toString()
    {
        return name + " (" + cards.size() + " cards)";
    }

    /**
     * Copies a chain of cards into a new chain with the same order.
     *
     * @param source the chain to copy
     * @return a new chain holding the same cards
     */
    static LinkedChain<Card> copyOf(LinkedChain<Card> source)
    {
        Card[] all = source.toArray(new Card[source.size()]);
        LinkedChain<Card> result = new LinkedChain<Card>();
        for (int i = all.length - 1; i >= 0; i--)
        {
            result.add(all[i]);
        }
        return result;
    }
}
