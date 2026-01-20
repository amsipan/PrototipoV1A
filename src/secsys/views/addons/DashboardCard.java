package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class DashboardCard extends JPanel {

    public DashboardCard(String title, String description, String value, JComponent actions) {

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ---- Textos ----
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(Color.DARK_GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(new Color(33, 150, 243));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitle, BorderLayout.NORTH);
        header.add(lblDesc, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(lblValue, BorderLayout.CENTER);

        // ---- Acciones ----
        if (actions != null) {
            add(actions, BorderLayout.SOUTH);
        }
    }

    public DashboardCard(String title, JComponent actions) {

        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(lblTitle, BorderLayout.NORTH);

        add(header, BorderLayout.NORTH);

        if (actions != null) {

            JPanel verticalCenterWrapper = new JPanel(new GridBagLayout());
            verticalCenterWrapper.setOpaque(false);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;

            gbc.weighty = 1.0;          
            gbc.weightx = 1.0;          
            gbc.fill = GridBagConstraints.HORIZONTAL; 

            gbc.anchor = GridBagConstraints.CENTER;   

            verticalCenterWrapper.add(actions, gbc);

            add(verticalCenterWrapper, BorderLayout.CENTER);
        }
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Double(
                0, 0, getWidth(), getHeight(), 20, 20
        ));

        super.paintComponent(g);
    }
}
