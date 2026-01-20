package secsys.views.platforms;

import secsys.router.ViewRouter;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.CustomButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlatformsPanel extends JPanel {

    private Image background;

    public PlatformsPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(800, 520));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Gestión de Plataformas");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel actions = new JPanel(new GridLayout(3, 1, 15, 15));
        actions.setOpaque(false);

        CustomButton btnRegister = new CustomButton("Registrar plataforma", "#4A90E2");
        CustomButton btnConsult  = new CustomButton("Consultar plataforma", "#5DA9E9");
        CustomButton btnUpdate   = new CustomButton("Actualizar plataforma", "#6C63FF");

        btnRegister.addActionListener(e -> ViewRouter.show("platforms-register"));
        btnConsult.addActionListener(e -> ViewRouter.show("platforms-consult"));
        btnUpdate.addActionListener(e -> ViewRouter.show("platforms-update"));

        actions.add(btnRegister);
        actions.add(btnConsult);
        actions.add(btnUpdate);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> ViewRouter.show("dashboard"));

        footer.add(btnBack);

        card.add(title, BorderLayout.NORTH);
        card.add(actions, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
