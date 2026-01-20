package secsys.views.addons;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CustomButton extends JButton {

    private final Color backgroundColor;
    private final Color hoverColor;
    private final int radius = 18;

    public CustomButton(String text, String hexColor) {
        super(text);

        // Colores
        this.backgroundColor = Color.decode(hexColor);
        this.hoverColor = backgroundColor.darker();

        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);

        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(180, 40));

        // Hover effect
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                repaint();
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Detect hover
        Color fill = getModel().isRollover() ? hoverColor : backgroundColor;

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Double(
                0, 0, getWidth(), getHeight(), radius, radius
        ));

        super.paintComponent(g);
        g2.dispose();
    }
}
