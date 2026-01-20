package secsys.views.clients;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ClientsActionsView extends JPanel {

    private Image background;

    public ClientsActionsView() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(800, 480));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Clientes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 14, 0));

        // ===== PANEL CENTRAL (CENTRADO V + H) =====
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 16, 16));
        buttonsPanel.setOpaque(false);

        CustomButton btnRegistrar = new CustomButton("Registrar cliente", "#4A90E2");
        CustomButton btnConsultar = new CustomButton("Consultar clientes", "#4A90E2");
        CustomButton btnActualizar = new CustomButton("Actualizar cliente", "#4A90E2");
        CustomButton btnVolver = new CustomButton("Volver", "#9E9E9E");

        // ✅ Ajusta keys a tus rutas reales
        btnRegistrar.addActionListener(e -> ViewRouter.show("clients-register"));
        btnConsultar.addActionListener(e -> ViewRouter.show("clients-consult"));
        btnActualizar.addActionListener(e -> ViewRouter.show("clients-update"));
        btnVolver.addActionListener(e -> ViewRouter.show("dashboard"));

        buttonsPanel.add(btnRegistrar);
        buttonsPanel.add(btnConsultar);
        buttonsPanel.add(btnActualizar);
        buttonsPanel.add(btnVolver);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        centerWrapper.add(buttonsPanel, gbc);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(centerWrapper, BorderLayout.CENTER);

        add(card);
    }

    // ===== FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
