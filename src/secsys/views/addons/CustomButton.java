package secsys.views.addons;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class CustomButton extends JButton {

    private final Color backgroundColor;
    private final Color hoverColor;
    private final int radius = 18;

    // ✅ Solo texto
    public CustomButton(String text, String hexColor) {
        this(text, null, hexColor);
    }

    // ✅ Solo icono
    public CustomButton(Icon icon, String hexColor) {
        this("", icon, hexColor);
    }

    // ✅ Texto + icono
    public CustomButton(String text, Icon icon, String hexColor) {
        super(text);

        this.backgroundColor = Color.decode(hexColor);
        this.hoverColor = backgroundColor.darker();

        setFont(new Font("Segoe UI", Font.BOLD, 13));
        setForeground(Color.WHITE);

        // ✅ Icono (si hay)
        if (icon != null) {
            setIcon(icon);
            // Ajustes para que se vea bien con/ sin texto
            setIconTextGap(text == null || text.isBlank() ? 0 : 8);
            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setVerticalTextPosition(SwingConstants.CENTER);
        }

        // ✅ Look
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);

        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(180, 40));

        // ✅ Si es solo icono, que no quede gigante ni desalineado
        if ((text == null || text.isBlank()) && icon != null) {
            int w = Math.max(40, icon.getIconWidth() + 22);
            int h = Math.max(40, icon.getIconHeight() + 18);
            setPreferredSize(new Dimension(w, h));
        }

        // Hover effect
        setRolloverEnabled(true);
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = getModel().isRollover() ? hoverColor : backgroundColor;

        g2.setColor(fill);
        g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), radius, radius));

        g2.dispose();
        super.paintComponent(g);
    }
}
