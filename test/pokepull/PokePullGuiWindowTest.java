package pokepull;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.util.Random;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/**
 * Builds the real windows (without showing them) to check that they start
 * up and reject bad arguments. Skipped on machines with no display.
 */
public class PokePullGuiWindowTest
{
    private static JTabbedPane findTabs(Container container)
    {
        for (Component c : container.getComponents())
        {
            if (c instanceof JTabbedPane)
            {
                return (JTabbedPane)c;
            }
            if (c instanceof Container)
            {
                JTabbedPane inner = findTabs((Container)c);
                if (inner != null)
                {
                    return inner;
                }
            }
        }
        return null;
    }

    private static PokePull startedGame()
    {
        PokePull game = new PokePull(new Random(4));
        game.startNewGame();
        return game;
    }

    @Test
    public void windowBuildsWithAllFourTabs() throws Exception
    {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(() ->
        {
            PokePullGui window = new PokePullGui(startedGame());
            assertEquals("PokePull", window.getTitle());
            assertEquals(4, findTabs(window.getContentPane()).getTabCount());
            window.dispose();
        });
    }

    @Test
    public void windowRejectsNullGame() throws Exception
    {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(() -> assertThrows(
            IllegalArgumentException.class, () -> new PokePullGui(null)));
    }

    @Test
    public void builderDialogBuildsForAnEmptyDeck() throws Exception
    {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(() ->
        {
            PokePull game = startedGame();
            PokePullGui window = new PokePullGui(game);
            DeckBuilderDialog dialog = new DeckBuilderDialog(window, game,
                new Deck("Mine"));
            assertEquals("Build deck: Mine", dialog.getTitle());
            dialog.dispose();
            window.dispose();
        });
    }

    @Test
    public void builderDialogRejectsNullGameOrDeck() throws Exception
    {
        assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(() ->
        {
            PokePull game = startedGame();
            PokePullGui window = new PokePullGui(game);
            assertThrows(IllegalArgumentException.class,
                () -> new DeckBuilderDialog(window, game, null));
            assertThrows(IllegalArgumentException.class,
                () -> new DeckBuilderDialog(window, null, new Deck("Mine")));
            window.dispose();
        });
    }
}
