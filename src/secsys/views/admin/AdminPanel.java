package secsys.views.admin;

import secsys.router.ViewRouter;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;
import secsys.views.addons.CustomButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminPanel extends JPanel {

    private Image background;

    public AdminPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(800, 520));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Administración de Usuarios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ===== BOTONES =====
        JPanel actionsPanel = new JPanel(new GridLayout(5, 1, 12, 12));
        actionsPanel.setOpaque(false);

        CustomButton btnCreate = new CustomButton("Crear usuario", "#4A90E2");
        CustomButton btnModify = new CustomButton("Modificar usuario", "#5DA9E9");
        CustomButton btnDelete = new CustomButton("Eliminar usuario", "#6C63FF");
        CustomButton btnConsult = new CustomButton("Consultar usuarios", "#7B8DFF");
        CustomButton btnBackup = new CustomButton("Generar respaldo de datos", "#3F7FDB");

        // ---- Navegación (prototipo) ----
        btnCreate.addActionListener(e ->
                ViewRouter.show("admin-create")
        );

        btnModify.addActionListener(e ->
                ViewRouter.show("admin-modify")
        );

        btnDelete.addActionListener(e ->
                ViewRouter.show("admin-delete")
        );

        btnConsult.addActionListener(e ->
                ViewRouter.show("admin-consult")
        );

        btnBackup.addActionListener(e -> {
                new SuccessMessageFrame("Datos respaldados correctamente").setVisible(true);
                ViewRouter.show("dashboard");
        });

        actionsPanel.add(btnCreate);
        actionsPanel.add(btnModify);
        actionsPanel.add(btnDelete);
        actionsPanel.add(btnConsult);
        actionsPanel.add(btnBackup);

        // ===== BOTÓN VOLVER =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e ->
                ViewRouter.show("dashboard")
        );

        footer.add(btnBack);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(actionsPanel, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    // ===== FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
