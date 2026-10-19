package pokepull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Draws one card the way it would look on paper: a rarity-colored frame, the
 * name and HP, an art area tinted by the card's type, and its moves.
 *
 * @version Fall 2026
 */
public class CardView extends JPanel
{
    private static final long serialVersionUID = 1L;

    /** Width of the drawn card in pixels. */
    public static final int WIDTH = 300;
    /** Height of the drawn card in pixels. */
    public static final int HEIGHT = 420;

    private final transient Card card;

    /**
     * @param card the card to draw; must not be null
     * @throws IllegalArgumentException if card is null
     */
    public CardView(Card card)
    {
        if (card == null)
        {
            throw new IllegalArgumentException("Card must not be null");
        }
        this.card = card;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(0x30, 0x30, 0x30));
    }

    /**
     * Opens a window showing the card.
     *
     * @param owner the window to open over
     * @param card  the card to show
     */
    public static void show(Window owner, Card card)
    {
        JDialog dialog = new JDialog(owner, card.getName(),
            JDialog.DEFAULT_MODALITY_TYPE);
        dialog.setContentPane(new CardView(card));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * @param type an element type such as "Fire"
     * @return the color used to tint cards of that type
     */
    static Color typeColor(String type)
    {
        switch (type.toLowerCase())
        {
            case "fire":
                return new Color(0xF4A07A);
            case "water":
                return new Color(0x8EC5F0);
            case "grass":
                return new Color(0xA5D88B);
            case "electric":
                return new Color(0xF7E17A);
            case "psychic":
                return new Color(0xF2A6C9);
            case "ghost":
                return new Color(0xB9A3D9);
            case "poison":
                return new Color(0xC79BD0);
            case "bug":
                return new Color(0xCBDB7A);
            case "fighting":
                return new Color(0xD9A38A);
            case "rock":
            case "ground":
                return new Color(0xD8C59A);
            case "flying":
                return new Color(0xB7D3F2);
            case "ice":
                return new Color(0xBDEBF0);
            case "dragon":
                return new Color(0x9FA8E6);
            case "fairy":
                return new Color(0xF5BCD6);
            default:
                return new Color(0xD0D0D0);
        }
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color frame = PokePullGui.RarityRenderer.colorFor(card.getRarity());
        Color tint = typeColor(card.getType());

        g2.setColor(frame);
        g2.fillRoundRect(10, 10, WIDTH - 20, HEIGHT - 20, 24, 24);
        g2.setColor(lighter(tint));
        g2.fillRoundRect(22, 22, WIDTH - 44, HEIGHT - 44, 16, 16);

        // Name and HP
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString(card.getName(), 34, 56);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        String hp = "HP " + card.getHealth();
        FontMetrics hpMetrics = g2.getFontMetrics();
        g2.drawString(hp, WIDTH - 34 - hpMetrics.stringWidth(hp), 56);

        // Art area: big initial of the name on the type color
        int artX = 34;
        int artY = 70;
        int artW = WIDTH - 68;
        int artH = 130;
        g2.setColor(tint);
        g2.fillRoundRect(artX, artY, artW, artH, 12, 12);
        g2.setColor(frame);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(artX, artY, artW, artH, 12, 12);
        g2.setColor(new Color(255, 255, 255, 170));
        g2.setFont(new Font("SansSerif", Font.BOLD, 96));
        FontMetrics big = g2.getFontMetrics();
        String initial = card.getName().substring(0, 1).toUpperCase();
        g2.drawString(initial, artX + (artW - big.stringWidth(initial)) / 2,
            artY + (artH + big.getAscent() - big.getDescent()) / 2);

        // Type
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
        g2.drawString(card.getType() + " type", artX, artY + artH + 22);

        // Moves
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        int y = artY + artH + 56;
        for (String move : card.getMoves())
        {
            g2.setColor(frame);
            g2.fillOval(artX, y - 11, 12, 12);
            g2.setColor(Color.BLACK);
            g2.drawString(move, artX + 22, y);
            y += 28;
        }

        // Rarity footer
        g2.setColor(frame);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        String rarity = card.getRarity().name().replace('_', ' ');
        FontMetrics footer = g2.getFontMetrics();
        g2.drawString(rarity, (WIDTH - footer.stringWidth(rarity)) / 2,
            HEIGHT - 34);
        g2.dispose();
    }

    private static Color lighter(Color c)
    {
        return new Color((c.getRed() + 510) / 3, (c.getGreen() + 510) / 3,
            (c.getBlue() + 510) / 3);
    }
}
