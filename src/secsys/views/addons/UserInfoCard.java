package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class UserInfoCard extends JPanel {

    public UserInfoCard(String fullName,
                        String cedula,
                        String username,
                        String email,
                        String role,
                        String status) {

        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(12, 14, 12, 14));
        setPreferredSize(new Dimension(250, 130));

        // ===== TÍTULO =====
        JLabel lblName = new JLabel(fullName);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblName.setAlignmentX(LEFT_ALIGNMENT);

        // ===== INFORMACIÓN =====
        JLabel lblCedula = createInfoLabel("Cédula: " + cedula);
        JLabel lblUser = createInfoLabel("Usuario: " + username);
        JLabel lblEmail = createInfoLabel("Correo: " + email);
        JLabel lblRole = createInfoLabel("Rol: " + role);
        JLabel lblStatus = createInfoLabel("Estado: " + status);

        add(lblName);
        add(Box.createVerticalStrut(6));
        add(lblCedula);
        add(lblUser);
        add(lblEmail);
        add(lblRole);
        add(lblStatus);
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.DARK_GRAY);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
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
