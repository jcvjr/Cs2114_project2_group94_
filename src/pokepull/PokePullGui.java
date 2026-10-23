package pokepull;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.NoSuchElementException;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.DefaultListCellRenderer;

/**
 * The Swing window for PokePull. It only shows state and forwards button
 * clicks to PokePull; every game rule lives in PokePull, Deck and Pack.
 *
 * @version Fall 2026
 */
public class PokePullGui extends JFrame
{
    private static final long serialVersionUID = 1L;

    private final PokePull game;

    private final JLabel status = new JLabel();
    private final JTabbedPane tabs = new JTabbedPane();

    private final DefaultListModel<Row> collectionModel =
        new DefaultListModel<Row>();
    private final JTextField collectionSearch = new JTextField(22);
    private final JLabel collectionCount = new JLabel(" ");
    private JList<Row> collectionListRef;

    private final JButton openPackButton = new JButton();
    private final DefaultListModel<Row> pulledModel =
        new DefaultListModel<Row>();
    private final DefaultListModel<Row> tradeModel =
        new DefaultListModel<Row>();
    private final JList<Row> tradeList = new JList<Row>(tradeModel);

    private final DefaultListModel<String> deckModel =
        new DefaultListModel<String>();
    private final JList<String> deckList = new JList<String>(deckModel);

    private final DefaultComboBoxModel<String> playDeckModel =
        new DefaultComboBoxModel<String>();
    private final JComboBox<String> playDeckBox =
        new JComboBox<String>(playDeckModel);
    private final DefaultListModel<Row> handModel =
        new DefaultListModel<Row>();

    /**
     * Builds the window around a game that has already been started.
     *
     * @param game the game to show
     * @throws IllegalArgumentException if game is null
     */
    public PokePullGui(PokePull game)
    {
        super("PokePull");
        if (game == null)
        {
            throw new IllegalArgumentException("Game must not be null");
        }
        this.game = game;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setJMenuBar(buildMenu());

        tabs.addTab("Collection", buildCollectionTab());
        tabs.addTab("Packs", buildPacksTab());
        tabs.addTab("Decks", buildDecksTab());
        tabs.addTab("Play", buildPlayTab());
        status.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(760, 520));
        pack();
        setLocationRelativeTo(null);
        refreshAll();
    }

    /**
     * Program entry point for the GUI.
     *
     * @param args unused
     */
    public static void main(String[] args)
    {
        PokePull game = new PokePull(new Random());
        game.startNewGame();
        SwingUtilities.invokeLater(() -> new PokePullGui(game)
            .setVisible(true));
    }

    // ----------------------------------------------------------------
    // Layout
    // ----------------------------------------------------------------

    private JMenuBar buildMenu()
    {
        JMenu gameMenu = new JMenu("Game");
        JMenuItem newGame = new JMenuItem("New game");
        newGame.addActionListener(e -> newGame());
        JMenuItem quit = new JMenuItem("Quit");
        quit.addActionListener(e -> System.exit(0));
        gameMenu.add(newGame);
        gameMenu.add(quit);
        JMenuBar bar = new JMenuBar();
        bar.add(gameMenu);
        return bar;
    }

    private JPanel buildCollectionTab()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search (name, type, rarity or move):"));
        top.add(collectionSearch);
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> collectionSearch.setText(""));
        top.add(clear);
        top.add(collectionCount);
        JButton view = new JButton("View card");
        view.addActionListener(e -> viewSelectedCard());
        top.add(view);
        collectionSearch.getDocument().addDocumentListener(
            new SimpleDocumentListener(this::refreshCollection));
        panel.add(top, BorderLayout.NORTH);
        JList<Row> collectionList = cardList(collectionModel);
        collectionList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    viewSelectedCard();
                }
            }
        });
        collectionListRef = collectionList;
        panel.add(scroll(collectionList), BorderLayout.CENTER);
        panel.add(new JLabel("Double-click a card to see it."),
            BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPacksTab()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel open = new JPanel(new BorderLayout(6, 6));
        open.setBorder(BorderFactory.createTitledBorder("Open a pack"));
        openPackButton.addActionListener(e -> openPack());
        JPanel openTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        openTop.add(openPackButton);
        open.add(openTop, BorderLayout.NORTH);
        open.add(scroll(cardList(pulledModel)), BorderLayout.CENTER);

        JPanel trade = new JPanel(new BorderLayout(6, 6));
        trade.setBorder(BorderFactory.createTitledBorder("Trade "
            + PokePull.TRADE_COST + " cards for a pack"));
        tradeList.setSelectionMode(
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tradeList.setCellRenderer(new RarityRenderer());
        trade.add(new JLabel("Select exactly " + PokePull.TRADE_COST
            + " cards (Ctrl-click to pick several)"), BorderLayout.NORTH);
        trade.add(scroll(tradeList), BorderLayout.CENTER);
        JButton tradeButton = new JButton("Trade selected cards");
        tradeButton.addActionListener(e -> trade());
        JPanel tradeBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        tradeBottom.add(tradeButton);
        trade.add(tradeBottom, BorderLayout.SOUTH);

        JPanel both = new JPanel(new java.awt.GridLayout(1, 2, 10, 0));
        both.add(open);
        both.add(trade);
        panel.add(both, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildDecksTab()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(new JLabel("Your saved decks:"), BorderLayout.NORTH);
        deckList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(scroll(deckList), BorderLayout.CENTER);

        JButton create = new JButton("New deck...");
        create.addActionListener(e -> newDeck());
        JButton copy = new JButton("Copy deck...");
        copy.addActionListener(e -> copyDeck());
        JButton delete = new JButton("Delete deck");
        delete.addActionListener(e -> deleteDeck());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(create);
        buttons.add(copy);
        buttons.add(delete);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPlayTab()
    {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton drawHand = new JButton("Draw opening hand ("
            + PokePull.HAND_SIZE + ")");
        drawHand.addActionListener(e -> drawHand());
        JButton drawOne = new JButton("Draw another card");
        drawOne.addActionListener(e -> drawCard());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Deck:"));
        top.add(playDeckBox);
        top.add(drawHand);
        top.add(drawOne);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll(cardList(handModel)), BorderLayout.CENTER);
        return panel;
    }

    private static JList<Row> cardList(DefaultListModel<Row> model)
    {
        JList<Row> list = new JList<Row>(model);
        list.setCellRenderer(new RarityRenderer());
        return list;
    }

    private static JScrollPane scroll(Component view)
    {
        return new JScrollPane(view);
    }

    // ----------------------------------------------------------------
    // Actions
    // ----------------------------------------------------------------

    private void newGame()
    {
        int answer = JOptionPane.showConfirmDialog(this,
            "Start over? Your cards and decks will be lost.", "New game",
            JOptionPane.OK_CANCEL_OPTION);
        if (answer == JOptionPane.OK_OPTION)
        {
            game.startNewGame();
            pulledModel.clear();
            handModel.clear();
            refreshAll();
        }
    }

    private void openPack()
    {
        try
        {
            LinkedChain<Card> pulled = game.openPack();
            pulledModel.clear();
            for (Card card : pulled.toArray(new Card[pulled.size()]))
            {
                pulledModel.addElement(new Row(card, card.toString()));
            }
            refreshAll();
        }
        catch (IllegalStateException e)
        {
            error(e);
        }
    }

    private void trade()
    {
        int[] picked = tradeList.getSelectedIndices();
        if (picked.length != PokePull.TRADE_COST)
        {
            message("Select exactly " + PokePull.TRADE_COST
                + " cards to trade (you selected " + picked.length + ").");
            return;
        }
        LinkedChain<Card> offered = new LinkedChain<Card>();
        for (int index : picked)
        {
            offered.add(tradeModel.get(index).card);
        }
        if (game.tradeForPack(offered))
        {
            message("Traded! You now have " + game.getPacksOwned()
                + " pack(s).");
        }
        else
        {
            message("Trade failed: you don't own that many copies.");
        }
        refreshAll();
    }

    private void newDeck()
    {
        String name = JOptionPane.showInputDialog(this, "Name for the new "
            + "deck:", "New deck", JOptionPane.PLAIN_MESSAGE);
        while (name != null && (name.isBlank() || game.findDeck(name) != null))
        {
            String why = name.isBlank() ? "The name can't be blank."
                : "A deck named \"" + name.trim() + "\" already exists.";
            name = JOptionPane.showInputDialog(this, why
                + " Pick another name:", "New deck", JOptionPane.PLAIN_MESSAGE);
        }
        if (name == null)
        {
            return;
        }
        new DeckBuilderDialog(this, game, new Deck(name)).setVisible(true);
        refreshAll();
    }

    private void copyDeck()
    {
        String name = deckList.getSelectedValue();
        if (name == null)
        {
            message("Select a deck to copy first.");
            return;
        }
        String newName = JOptionPane.showInputDialog(this,
            "Name for the copy:", "Copy deck", JOptionPane.PLAIN_MESSAGE);
        if (newName == null)
        {
            return;
        }
        try
        {
            game.copyDeck(name, newName);
            refreshAll();
        }
        catch (IllegalArgumentException e)
        {
            error(e);
        }
    }

    private void deleteDeck()
    {
        String name = deckList.getSelectedValue();
        if (name == null)
        {
            message("Select a deck to delete first.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
            "Delete \"" + name + "\"?", "Delete deck",
            JOptionPane.OK_CANCEL_OPTION);
        if (answer == JOptionPane.OK_OPTION)
        {
            if (!game.deleteDeck(name))
            {
                message("No such deck: \"" + name + "\".");
            }
            refreshAll();
        }
    }

    private void drawHand()
    {
        String name = (String)playDeckBox.getSelectedItem();
        if (name == null)
        {
            message("Save a deck first, then pick it here.");
            return;
        }
        try
        {
            LinkedChain<Card> hand = game.startHand(name);
            handModel.clear();
            for (Card card : hand.toArray(new Card[hand.size()]))
            {
                handModel.addElement(new Row(card, card.toString()));
            }
        }
        catch (IllegalArgumentException | IllegalStateException e)
        {
            error(e);
        }
    }

    private void drawCard()
    {
        try
        {
            Card card = game.drawCard();
            handModel.addElement(new Row(card, card.toString()));
        }
        catch (IllegalStateException | NoSuchElementException e)
        {
            error(e);
        }
    }

    // ----------------------------------------------------------------
    // Refreshing what's on screen
    // ----------------------------------------------------------------

    /**
     * Redraws every list and the status bar from the game's current state.
     */
    void refreshAll()
    {
        Card[] all = game.getCollectionCards();

        refreshCollection();

        tradeModel.clear();
        for (Card card : all)
        {
            tradeModel.addElement(new Row(card, card.toString()));
        }

        openPackButton.setText("Open a pack (" + game.getPacksOwned()
            + " left)");
        openPackButton.setEnabled(game.getPacksOwned() > 0);

        String selected = deckList.getSelectedValue();
        deckModel.clear();
        playDeckModel.removeAllElements();
        for (Deck deck : game.getDecks())
        {
            deckModel.addElement(deck.getName());
            playDeckModel.addElement(deck.getName());
        }
        if (selected != null)
        {
            deckList.setSelectedValue(selected, true);
        }

        status.setText("Packs: " + game.getPacksOwned() + "     Cards: "
            + game.getCollectionSize() + "     Decks: "
            + game.getDeckCount());
    }

    /**
     * Redraws the collection tab's list using the search box.
     */
    private void refreshCollection()
    {
        collectionModel.clear();
        Card[] found = game.searchCollection(collectionSearch.getText());
        for (Card card : found)
        {
            collectionModel.addElement(new Row(card, card + "   x"
                + game.ownedCopies(card)));
        }
        int total = PokePull.distinct(game.getCollectionCards()).length;
        collectionCount.setText("Showing " + found.length + " of " + total);
    }

    /**
     * Lists each different card once, in first-seen order.
     *
     * @param cards cards that may repeat
     * @return the distinct cards
     */
    static Card[] distinct(Card[] cards)
    {
        return PokePull.distinct(cards);
    }

    private void viewSelectedCard()
    {
        Row row = collectionListRef.getSelectedValue();
        if (row == null)
        {
            message("Select a card first.");
            return;
        }
        CardView.show(this, row.card);
    }

    private void message(String text)
    {
        JOptionPane.showMessageDialog(this, text, "PokePull",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(Exception e)
    {
        JOptionPane.showMessageDialog(this, e.getMessage(), "PokePull",
            JOptionPane.WARNING_MESSAGE);
    }

    // ----------------------------------------------------------------
    // List rows and coloring
    // ----------------------------------------------------------------

    /**
     * One line in a list: the text to show and the card it stands for.
     */
    static final class Row
    {
        final Card card;
        final String text;

        Row(Card card, String text)
        {
            this.card = card;
            this.text = text;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }

    /**
     * Colors each row's text by the card's rarity.
     */
    static final class RarityRenderer extends DefaultListCellRenderer
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list,
            Object value, int index, boolean selected, boolean focused)
        {
            JLabel label = (JLabel)super.getListCellRendererComponent(list,
                value, index, selected, focused);
            if (!selected && value instanceof Row)
            {
                label.setForeground(colorFor(((Row)value).card.getRarity()));
            }
            return label;
        }

        static Color colorFor(Rarity rarity)
        {
            switch (rarity)
            {
                case UNCOMMON:
                    return new Color(0x2E7D32);
                case RARE:
                    return new Color(0x1565C0);
                case SUPER_RARE:
                    return new Color(0x7B1FA2);
                case LEGENDARY:
                    return new Color(0xE65100);
                default:
                    return new Color(0x444444);
            }
        }
    }

    /**
     * A DocumentListener that runs one action on every change.
     */
    static final class SimpleDocumentListener implements DocumentListener
    {
        private final Runnable action;

        SimpleDocumentListener(Runnable action)
        {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent e)
        {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e)
        {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e)
        {
            action.run();
        }
    }
}
