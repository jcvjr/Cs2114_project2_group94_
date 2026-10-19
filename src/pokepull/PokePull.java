package pokepull;

import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;

/**
 * Runs PokePull: console menus, input parsing and re-prompting. Owns the
 * user's collection, saved decks and pack count. The game rules live in
 * Card, Deck and Pack; this class decides what to say when they reject
 * something.
 *
 * @version Fall 2026
 */
public class PokePull
{
    /** Cards in the opening hand. */
    public static final int HAND_SIZE = 7;
    /** Cards the user must give up to trade for one pack. */
    public static final int TRADE_COST = 5;
    /** Cards the user starts with. */
    public static final int STARTER_CARDS = 10;

    private final LinkedChain<Card> catalog;
    private final LinkedChain<Card> collection;
    private final LinkedChain<Deck> decks;
    private final Pack pack;
    private final Random rng;
    private int packsOwned;
    private Deck drawPile;

    /**
     * Creates a game using the Gen 1 catalog.
     *
     * @param rng the source of randomness
     */
    public PokePull(Random rng)
    {
        this(CardCatalog.gen1(), rng);
    }

    /**
     * Creates a game with a given catalog. The user owns nothing until
     * startNewGame() is called.
     *
     * @param catalog every card that exists; must hold at least
     *                STARTER_CARDS different cards
     * @param rng     the source of randomness
     * @throws IllegalArgumentException if catalog is too small
     */
    public PokePull(LinkedChain<Card> catalog, Random rng)
    {
        if (catalog == null || catalog.size() < STARTER_CARDS)
        {
            throw new IllegalArgumentException(
                "The catalog needs at least " + STARTER_CARDS + " cards");
        }
        this.catalog = catalog;
        this.rng = rng;
        this.collection = new LinkedChain<Card>();
        this.decks = new LinkedChain<Deck>();
        this.pack = new Pack(catalog, rng);
        this.packsOwned = 0;
        this.drawPile = null;
    }

    /**
     * Program entry point.
     *
     * @param args unused
     */
    public static void main(String[] args)
    {
        PokePull game = new PokePull(new Random());
        game.run(new Scanner(System.in), System.out);
    }

    // ----------------------------------------------------------------
    // Game actions (no console I/O)
    // ----------------------------------------------------------------

    /**
     * Resets the game: a fresh collection of STARTER_CARDS different random
     * cards, 2 or 3 packs, and no saved decks.
     */
    public void startNewGame()
    {
        collection.clear();
        decks.clear();
        drawPile = null;
        Card[] all = catalog.toArray(new Card[catalog.size()]);
        while (collection.size() < STARTER_CARDS)
        {
            Card pick = all[rng.nextInt(all.length)];
            if (!collection.contains(pick))
            {
                collection.add(pick);
            }
        }
        packsOwned = 2 + rng.nextInt(2);
    }

    /**
     * Opens one pack and adds its cards to the collection.
     *
     * @return the cards pulled
     * @throws IllegalStateException if the user has no packs
     */
    public LinkedChain<Card> openPack()
    {
        if (packsOwned == 0)
        {
            throw new IllegalStateException("You have no packs to open.");
        }
        packsOwned--;
        LinkedChain<Card> pulled = pack.open();
        Card[] cards = pulled.toArray(new Card[pulled.size()]);
        for (Card card : cards)
        {
            collection.add(card);
        }
        return pulled;
    }

    /**
     * Trades cards for one pack. Nothing changes unless the trade is valid.
     *
     * @param offered the cards given up; must be exactly TRADE_COST cards,
     *                all owned
     * @return true if the trade happened
     */
    public boolean tradeForPack(LinkedChain<Card> offered)
    {
        if (offered == null || offered.size() != TRADE_COST)
        {
            return false;
        }
        Card[] cards = offered.toArray(new Card[offered.size()]);
        for (Card card : cards)
        {
            if (offered.count(card) > collection.count(card))
            {
                return false;
            }
        }
        for (Card card : cards)
        {
            collection.remove(card);
        }
        packsOwned++;
        return true;
    }

    /**
     * Adds one card to a deck being built, if the user owns enough copies.
     * Deck still enforces the maximum deck size and 4-copy rules.
     *
     * @param deck the deck being built
     * @param card the card to add
     * @throws IllegalArgumentException if deck or card is null
     * @throws IllegalStateException    if the deck already holds every copy
     *                                  of card the user owns, or Deck rejects
     *                                  the card
     */
    public void addToDeck(Deck deck, Card card)
    {
        if (deck == null || card == null)
        {
            throw new IllegalArgumentException(
                "Deck and card must not be null");
        }
        int owned = collection.count(card);
        if (owned == 0)
        {
            throw new IllegalStateException(
                "You don't own " + card.getName() + ".");
        }
        if (deck.countOf(card) >= owned)
        {
            throw new IllegalStateException("You only own " + owned
                + (owned == 1 ? " copy of " : " copies of ")
                + card.getName() + ".");
        }
        deck.addCard(card);
    }

    /**
     * Saves a finished deck.
     *
     * @param deck the deck to save
     * @throws IllegalArgumentException if deck is null or its name is
     *                                  already taken
     * @throws IllegalStateException    if the deck is not complete
     */
    public void saveDeck(Deck deck)
    {
        if (deck == null)
        {
            throw new IllegalArgumentException("Deck must not be null");
        }
        if (findDeck(deck.getName()) != null)
        {
            throw new IllegalArgumentException(
                "A deck named \"" + deck.getName() + "\" already exists.");
        }
        if (!deck.isSavable())
        {
            throw new IllegalStateException("A deck needs between "
                + Deck.MIN_SIZE + " and " + Deck.MAX_SIZE
                + " cards; this one has " + deck.size() + ".");
        }
        decks.add(deck);
    }

    /**
     * Deletes a saved deck.
     *
     * @param name the deck's name
     * @return true if it was deleted, false if no deck has that name
     */
    public boolean deleteDeck(String name)
    {
        Deck found = findDeck(name);
        if (found == null)
        {
            return false;
        }
        return decks.remove(found);
    }

    /**
     * Saves a copy of a deck under a new name.
     *
     * @param name    the deck to copy
     * @param newName the name for the copy
     * @return the new deck
     * @throws IllegalArgumentException if name does not exist, or newName
     *                                  is blank or taken
     */
    public Deck copyDeck(String name, String newName)
    {
        Deck source = findDeck(name);
        if (source == null)
        {
            throw new IllegalArgumentException(
                "No such deck: \"" + name + "\".");
        }
        Deck copy = source.copy(newName);
        saveDeck(copy);
        return copy;
    }

    /**
     * @param name the deck name (case-insensitive)
     * @return the saved deck with that name, or null
     */
    public Deck findDeck(String name)
    {
        if (name == null)
        {
            return null;
        }
        Deck[] all = decks.toArray(new Deck[decks.size()]);
        for (Deck deck : all)
        {
            if (deck.getName().equalsIgnoreCase(name.trim()))
            {
                return deck;
            }
        }
        return null;
    }

    /**
     * @param card the card
     * @return how many copies of card the user owns
     */
    public int ownedCopies(Card card)
    {
        return collection.count(card);
    }

    /**
     * @param name a card name (case-insensitive)
     * @return the owned card with that name, or null
     */
    public Card findOwned(String name)
    {
        if (name == null)
        {
            return null;
        }
        Card[] all = collection.toArray(new Card[collection.size()]);
        for (Card card : all)
        {
            if (card.getName().equalsIgnoreCase(name.trim()))
            {
                return card;
            }
        }
        return null;
    }

    /**
     * Searches the user's collection.
     *
     * @param query text to look for in a card's name, type, rarity or
     *              moves; null or blank matches everything
     * @return each matching card once, in the order first owned
     */
    public Card[] searchCollection(String query)
    {
        LinkedChain<Card> matches = new LinkedChain<Card>();
        for (Card card : distinct(getCollectionCards()))
        {
            if (card.matches(query))
            {
                matches.addToEnd(card);
            }
        }
        return matches.toArray(new Card[matches.size()]);
    }

    /**
     * Lists each different card once, in first-seen order.
     *
     * @param cards cards that may repeat
     * @return the distinct cards
     */
    public static Card[] distinct(Card[] cards)
    {
        LinkedChain<String> seen = new LinkedChain<String>();
        LinkedChain<Card> unique = new LinkedChain<Card>();
        for (Card card : cards)
        {
            String key = card.getName().toLowerCase();
            if (!seen.contains(key))
            {
                seen.add(key);
                unique.addToEnd(card);
            }
        }
        return unique.toArray(new Card[unique.size()]);
    }

    /**
     * Shuffles a saved deck into a draw pile and draws the opening hand.
     * The saved deck itself is not changed.
     *
     * @param deckName the deck to play
     * @return the opening hand
     * @throws IllegalArgumentException if there is no such deck
     */
    public LinkedChain<Card> startHand(String deckName)
    {
        Deck deck = findDeck(deckName);
        if (deck == null)
        {
            throw new IllegalArgumentException(
                "No such deck: \"" + deckName + "\".");
        }
        Deck working = deck.copy(deck.getName());
        LinkedChain<Card> hand = working.drawCards(HAND_SIZE, rng);
        drawPile = working;
        return hand;
    }

    /**
     * Draws one more card from the current draw pile.
     *
     * @return the card drawn
     * @throws IllegalStateException  if no hand has been started
     * @throws NoSuchElementException if the draw pile is empty
     */
    public Card drawCard()
    {
        if (drawPile == null)
        {
            throw new IllegalStateException(
                "Start a hand from a deck before drawing.");
        }
        return drawPile.drawRandom(rng);
    }

    /**
     * @return a snapshot of every card owned, repeats included
     */
    public Card[] getCollectionCards()
    {
        return collection.toArray(new Card[collection.size()]);
    }

    /**
     * @return a snapshot of the saved decks
     */
    public Deck[] getDecks()
    {
        return decks.toArray(new Deck[decks.size()]);
    }

    /**
     * @return the number of unopened packs
     */
    public int getPacksOwned()
    {
        return packsOwned;
    }

    /**
     * @return the number of cards owned, counting repeats
     */
    public int getCollectionSize()
    {
        return collection.size();
    }

    /**
     * @return the number of saved decks
     */
    public int getDeckCount()
    {
        return decks.size();
    }

    // ----------------------------------------------------------------
    // Console menu
    // ----------------------------------------------------------------

    /**
     * Runs the menu loop until the user quits or input ends.
     *
     * @param in  where to read the user's input
     * @param out where to print
     */
    public void run(Scanner in, PrintStream out)
    {
        out.println("Welcome to PokePull!");
        startNewGame();
        out.println("You start with " + STARTER_CARDS + " cards and "
            + packsOwned + " packs.");
        boolean running = true;
        while (running)
        {
            printMenu(out);
            String choice = prompt(in, out, "Choose an option: ");
            if (choice == null)
            {
                break;
            }
            try
            {
                running = handle(choice.trim(), in, out);
            }
            catch (IllegalArgumentException | IllegalStateException
                | NoSuchElementException e)
            {
                out.println(e.getMessage());
            }
        }
        out.println("Goodbye!");
    }

    private void printMenu(PrintStream out)
    {
        out.println();
        out.println("Packs: " + packsOwned + "   Cards: " + collection.size()
            + "   Decks: " + decks.size());
        out.println("1) Show collection   2) Open a pack");
        out.println("3) Trade " + TRADE_COST + " cards for a pack");
        out.println("4) Build a deck      5) List decks");
        out.println("6) Copy a deck       7) Delete a deck");
        out.println("8) Draw opening hand 9) Draw another card");
        out.println("10) Search your collection");
        out.println("0) Quit");
    }

    private boolean handle(String choice, Scanner in, PrintStream out)
    {
        switch (choice)
        {
            case "1":
                showCollection(out);
                break;
            case "2":
                LinkedChain<Card> pulled = openPack();
                out.println("You pulled:");
                printChain(pulled, out);
                break;
            case "3":
                tradeMenu(in, out);
                break;
            case "4":
                buildDeckMenu(in, out);
                break;
            case "5":
                listDecks(out);
                break;
            case "6":
                copyMenu(in, out);
                break;
            case "7":
                deleteMenu(in, out);
                break;
            case "8":
                handMenu(in, out);
                break;
            case "9":
                out.println("You drew: " + drawCard());
                break;
            case "10":
                searchMenu(in, out);
                break;
            case "0":
                return false;
            default:
                out.println("Please enter a number from the menu.");
                break;
        }
        return true;
    }

    private void showCollection(PrintStream out)
    {
        LinkedChain<String> seen = new LinkedChain<String>();
        Card[] all = collection.toArray(new Card[collection.size()]);
        for (Card card : all)
        {
            String key = card.getName().toLowerCase();
            if (!seen.contains(key))
            {
                seen.add(key);
                out.println("  " + card + " x" + collection.count(card));
            }
        }
    }

    private void printChain(LinkedChain<Card> chain, PrintStream out)
    {
        Card[] cards = chain.toArray(new Card[chain.size()]);
        for (Card card : cards)
        {
            out.println("  " + card);
        }
    }

    private void listDecks(PrintStream out)
    {
        if (decks.isEmpty())
        {
            out.println("You have no saved decks.");
            return;
        }
        Deck[] all = decks.toArray(new Deck[decks.size()]);
        for (Deck deck : all)
        {
            out.println("  " + deck);
        }
    }

    private void tradeMenu(Scanner in, PrintStream out)
    {
        LinkedChain<Card> offered = new LinkedChain<Card>();
        for (int i = 1; i <= TRADE_COST; i++)
        {
            String name = prompt(in, out,
                "Card " + i + " of " + TRADE_COST + " to trade (or cancel): ");
            if (name == null || name.trim().equalsIgnoreCase("cancel"))
            {
                out.println("Trade cancelled.");
                return;
            }
            Card card = findOwned(name);
            if (card == null)
            {
                out.println("You don't own \"" + name.trim()
                    + "\". Trade cancelled.");
                return;
            }
            offered.add(card);
        }
        if (tradeForPack(offered))
        {
            out.println("Traded! You now have " + packsOwned + " pack(s).");
        }
        else
        {
            out.println("Trade failed: you don't own that many copies.");
        }
    }

    private void buildDeckMenu(Scanner in, PrintStream out)
    {
        String name = prompt(in, out, "Name for the new deck (or cancel): ");
        while (name != null && !name.trim().equalsIgnoreCase("cancel")
            && (name.isBlank() || findDeck(name) != null))
        {
            out.println(name.isBlank() ? "The name can't be blank."
                : "A deck named \"" + name.trim() + "\" already exists.");
            name = prompt(in, out, "Pick another name (or cancel): ");
        }
        if (name == null || name.trim().equalsIgnoreCase("cancel"))
        {
            out.println("Deck building cancelled.");
            return;
        }
        Deck draft = new Deck(name);
        out.println("Type a card name (add a number to add several, e.g. "
            + "\"Pikachu 4\"), \"remove <name>\", \"list\", \"save\" (needs "
            + Deck.MIN_SIZE + " to " + Deck.MAX_SIZE + " cards), or "
            + "\"cancel\".");
        while (draft.size() < Deck.MAX_SIZE)
        {
            String line = prompt(in, out, draft.getName() + " ("
                + draft.size() + "/" + Deck.MAX_SIZE + "): ");
            if (line == null || line.trim().equalsIgnoreCase("cancel"))
            {
                out.println("Deck building cancelled.");
                return;
            }
            if (line.trim().equalsIgnoreCase("save"))
            {
                if (draft.isSavable())
                {
                    break;
                }
                out.println("Add at least " + Deck.MIN_SIZE
                    + " cards before saving (this deck has " + draft.size()
                    + ").");
                continue;
            }
            draftCommand(draft, line.trim(), out);
        }
        saveDeck(draft);
        out.println("Saved deck \"" + draft.getName() + "\".");
    }

    private void draftCommand(Deck draft, String line, PrintStream out)
    {
        if (line.equalsIgnoreCase("list"))
        {
            showCollection(out);
            return;
        }
        if (line.toLowerCase().startsWith("remove "))
        {
            Card card = findOwned(line.substring(7));
            if (card != null && draft.removeCard(card))
            {
                out.println("Removed one " + card.getName() + ".");
            }
            else
            {
                out.println("That card isn't in the deck.");
            }
            return;
        }
        int times = 1;
        String cardName = line;
        int space = line.lastIndexOf(' ');
        if (space > 0)
        {
            try
            {
                times = Integer.parseInt(line.substring(space + 1));
                cardName = line.substring(0, space);
            }
            catch (NumberFormatException e)
            {
                times = 1;
            }
        }
        Card card = findOwned(cardName);
        if (card == null)
        {
            out.println("You don't own a card called \"" + cardName + "\".");
            return;
        }
        for (int i = 0; i < times; i++)
        {
            try
            {
                addToDeck(draft, card);
            }
            catch (IllegalStateException e)
            {
                out.println(e.getMessage()
                    + " Use \"remove <name>\" to swap a card out.");
                return;
            }
        }
    }

    private void searchMenu(Scanner in, PrintStream out)
    {
        String query = prompt(in, out,
            "Search by name, type, rarity or move: ");
        if (query == null)
        {
            return;
        }
        Card[] found = searchCollection(query);
        if (found.length == 0)
        {
            out.println("No cards match \"" + query.trim() + "\".");
            return;
        }
        for (Card card : found)
        {
            out.println("  " + card + " x" + collection.count(card));
        }
    }

    private void copyMenu(Scanner in, PrintStream out)
    {
        String name = prompt(in, out, "Deck to copy: ");
        String newName = prompt(in, out, "Name for the copy: ");
        if (name == null || newName == null)
        {
            return;
        }
        copyDeck(name, newName);
        out.println("Copied \"" + name.trim() + "\" to \"" + newName.trim()
            + "\".");
    }

    private void deleteMenu(Scanner in, PrintStream out)
    {
        String name = prompt(in, out, "Deck to delete: ");
        if (name == null)
        {
            return;
        }
        if (deleteDeck(name))
        {
            out.println("Deleted \"" + name.trim() + "\".");
        }
        else
        {
            out.println("No such deck: \"" + name.trim() + "\".");
        }
    }

    private void handMenu(Scanner in, PrintStream out)
    {
        String name = prompt(in, out, "Deck to play: ");
        if (name == null)
        {
            return;
        }
        LinkedChain<Card> hand = startHand(name);
        out.println("Your opening hand:");
        printChain(hand, out);
    }

    /**
     * Prints a prompt and reads one line.
     *
     * @return the line, or null if input has ended
     */
    private String prompt(Scanner in, PrintStream out, String message)
    {
        out.print(message);
        out.flush();
        if (!in.hasNextLine())
        {
            return null;
        }
        return in.nextLine();
    }
}
