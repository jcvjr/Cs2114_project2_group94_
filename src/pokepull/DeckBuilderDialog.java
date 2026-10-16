package pokepull;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * A window for building one deck. Cards come from the user's collection;
 * Deck enforces the 60-card and 4-copy rules, and this dialog just shows
 * what Deck says.
 *
 * @version Fall 2026
 */
public class DeckBuilderDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private final PokePull game;
    private final Deck draft;

    private final DefaultListModel<PokePullGui.Row> ownedModel =
        new DefaultListModel<PokePullGui.Row>();
    private final JList<PokePullGui.Row> ownedList =
        new JList<PokePullGui.Row>(ownedModel);
    private final DefaultListModel<PokePullGui.Row> deckModel =
        new DefaultListModel<PokePullGui.Row>();
    private final JList<PokePullGui.Row> deckList =
        new JList<PokePullGui.Row>(deckModel);
    private final JLabel count = new JLabel();
    private final JLabel message = new JLabel(" ");

    /**
     * Opens the builder for a new, empty deck.
     *
     * @param owner the window that opened this dialog
     * @param game  the game whose collection supplies the cards
     * @param draft the empty deck being built
     */
    public DeckBuilderDialog(JFrame owner, PokePull game, Deck draft)
    {
        super(owner, "Build deck: " + draft.getName(), true);
        this.game = game;
        this.draft = draft;

        ownedList.setCellRenderer(new PokePullGui.RarityRenderer());
        ownedList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ownedList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    addSelected();
                }
            }
        });
        deckList.setCellRenderer(new PokePullGui.RarityRenderer());
        deckList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deckList.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    removeSelected();
                }
            }
        });

        JPanel lists = new JPanel(new GridLayout(1, 2, 10, 0));
        lists.add(titled("Your cards (double-click to add)", ownedList));
        lists.add(titled("In this deck (double-click to remove)", deckList));

        JButton add = new JButton("Add card >");
        add.addActionListener(e -> addSelected());
        JButton remove = new JButton("< Remove card");
        remove.addActionListener(e -> removeSelected());
        JButton save = new JButton("Save deck");
        save.addActionListener(e -> save());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(add);
        buttons.add(remove);
        buttons.add(save);
        buttons.add(cancel);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(message, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        content.add(count, BorderLayout.NORTH);
        content.add(lists, BorderLayout.CENTER);
        content.add(bottom, BorderLayout.SOUTH);
        setContentPane(content);

        refresh();
        setSize(760, 520);
        setLocationRelativeTo(owner);
    }

    private static JPanel titled(String title, JList<?> list)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        return panel;
    }

    private void addSelected()
    {
        PokePullGui.Row row = ownedList.getSelectedValue();
        int index = ownedList.getSelectedIndex();
        if (row == null)
        {
            message.setText("Select a card on the left first.");
            return;
        }
        try
        {
            draft.addCard(row.card);
            message.setText(" ");
        }
        catch (IllegalStateException e)
        {
            message.setText(e.getMessage()
                + " Remove a card from the deck to make room.");
        }
        refresh();
        ownedList.setSelectedIndex(index);
    }

    private void removeSelected()
    {
        PokePullGui.Row row = deckList.getSelectedValue();
        if (row == null)
        {
            message.setText("Select a card on the right first.");
            return;
        }
        draft.removeCard(row.card);
        message.setText(" ");
        refresh();
    }

    private void save()
    {
        try
        {
            game.saveDeck(draft);
            dispose();
        }
        catch (IllegalStateException | IllegalArgumentException e)
        {
            message.setText(e.getMessage());
        }
    }

    private void refresh()
    {
        ownedModel.clear();
        for (Card card : PokePullGui.distinct(game.getCollectionCards()))
        {
            ownedModel.addElement(new PokePullGui.Row(card, card.toString()
                + "   owned x" + game.ownedCopies(card)));
        }
        deckModel.clear();
        for (Card card : PokePullGui.distinct(draft.toArray()))
        {
            deckModel.addElement(new PokePullGui.Row(card, card.getName()
                + "   x" + draft.countOf(card)));
        }
        count.setText(draft.getName() + ": " + draft.size() + " / "
            + Deck.MAX_SIZE + " cards (max " + Deck.MAX_COPIES
            + " copies of each)");
    }
}
