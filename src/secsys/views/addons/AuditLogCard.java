package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class AuditLogCard extends JPanel {

    public AuditLogCard(String eventType,
                        String description,
                        String user,
                        String date) {

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(12, 14, 12, 14));
        setPreferredSize(new Dimension(260, 120));

        JLabel lblEvent = new JLabel(eventType);
        lblEvent.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEvent.setAlignmentX(LEFT_ALIGNMENT);

        add(lblEvent);
        add(Box.createVerticalStrut(6));
        add(createInfo("Descripción: " + description));
        add(createInfo("Usuario: " + user));
        add(createInfo("Fecha: " + date));
    }

    private JLabel createInfo(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(Color.DARK_GRAY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(
                0, 0, getWidth(), getHeight(), 18, 18
        ));

        super.paintComponent(g);
    }
}
