package pokepull;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * Tests for CardView. The panel is painted into an image, so no window opens.
 */
public class CardViewTest
{
    private static BufferedImage paint(Card card)
    {
        CardView view = new CardView(card);
        view.setSize(CardView.WIDTH, CardView.HEIGHT);
        BufferedImage image = new BufferedImage(CardView.WIDTH,
            CardView.HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        view.paint(g);
        g.dispose();
        return image;
    }

    @Test
    public void everyCatalogCardPaintsWithoutError()
    {
        LinkedChain<Card> catalog = CardCatalog.gen1();
        for (Card card : catalog.toArray(new Card[catalog.size()]))
        {
            assertNotNull(paint(card));
        }
    }

    @Test
    public void paintDrawsTheRarityFrameColor()
    {
        Card legendary = new Card("Mew", "Psychic", new String[] {"Psywave"},
            50, Rarity.LEGENDARY);
        Color frame = PokePullGui.RarityRenderer.colorFor(Rarity.LEGENDARY);
        assertEquals(frame.getRGB(), paint(legendary).getRGB(14, 200));
    }

    @Test
    public void differentTypesGetDifferentTints()
    {
        assertNotEquals(CardView.typeColor("Fire"),
            CardView.typeColor("Water"));
        assertEquals(CardView.typeColor("fire"), CardView.typeColor("FIRE"));
    }

    @Test
    public void unknownTypeStillGetsAColor()
    {
        assertNotNull(CardView.typeColor("Cosmic"));
    }

    @Test
    public void constructorRejectsNullCard()
    {
        assertThrows(IllegalArgumentException.class,
            () -> new CardView(null));
    }
}
