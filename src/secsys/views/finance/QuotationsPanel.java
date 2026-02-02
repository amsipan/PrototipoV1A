package secsys.views.finance;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class QuotationsPanel extends JPanel {

    private Image background;

    public QuotationsPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(800, 560));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Cotizaciones");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel actions = new JPanel(new GridLayout(4, 1, 15, 15));
        actions.setOpaque(false);

        CustomButton btnGenerate = new CustomButton("Generar cotización", "#4A90E2");
        CustomButton btnReview   = new CustomButton("Revisar cotización", "#5DA9E9");
        CustomButton btnConsult  = new CustomButton("Consultar cotizaciones", "#6C63FF");
        CustomButton btnExports  = new CustomButton("Descargas y envíos de cotizaciones", "#7E57C2");

        // Rutas (ajusta los nombres a tus keys reales del ViewRouter)
        btnGenerate.addActionListener(e -> ViewRouter.show("finance-generate"));
        btnReview.addActionListener(e -> ViewRouter.show("finance-review"));
        btnConsult.addActionListener(e -> ViewRouter.show("finance-consult"));
        btnExports.addActionListener(e -> ViewRouter.show("finance-exports"));

        actions.add(btnGenerate);
        actions.add(btnReview);
        actions.add(btnConsult);
        actions.add(btnExports);

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
