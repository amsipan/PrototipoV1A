package secsys.views.platforms;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.InfoCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlatformsConsultPanel extends JPanel {

    private Image background;

    private JPanel resultsPanel;
    private JComboBox<String> cmbPlatform;

    public PlatformsConsultPanel() {

        // ===== IMAGEN DE FONDO =====
        background = new ImageIcon(
                "src\\secsys\\resources\\imagenFondo.png"
        ).getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(860, 540));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Consultar Plataforma");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ===== PANEL DE BÚSQUEDA =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        searchPanel.setOpaque(false);

        cmbPlatform = new JComboBox<>(new String[]{
                "KnowBe4 (USA)",
                "SMARTFENSE (LATAM)"
        });

        CustomButton btnSearch = new CustomButton("Consultar", "#4A90E2");

        searchPanel.add(new JLabel("Plataforma:"));
        searchPanel.add(cmbPlatform);
        searchPanel.add(btnSearch);

        // ===== RESULTADOS =====
        resultsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        resultsPanel.setOpaque(false);

        RoundedPanel resultsContainer = new RoundedPanel(18);
        resultsContainer.setLayout(new BorderLayout());
        resultsContainer.setBackground(new Color(245, 247, 250));
        resultsContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        resultsContainer.setPreferredSize(new Dimension(800, 300));
        resultsContainer.add(resultsPanel, BorderLayout.CENTER);

        // ===== ACCIÓN CONSULTAR =====
        btnSearch.addActionListener(e -> showMockResults());

        // ===== BOTONES =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> {
            resetView();
            ViewRouter.show("platforms");
        });

        footer.add(btnBack);

        // ===== CONTENEDOR CENTRAL =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(searchPanel);
        center.add(Box.createVerticalStrut(15));
        center.add(resultsContainer);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    // ===== RESULTADOS SIMULADOS =====
    private void showMockResults() {

        resultsPanel.removeAll();

        boolean isUSA = cmbPlatform.getSelectedIndex() == 0;

        if (isUSA) {
            resultsPanel.add(new InfoCard(
                    "Tipo de licenciamiento",
                    "Anual corporativo"
            ));
            resultsPanel.add(new InfoCard(
                    "Costo anual por usuario",
                    "$45.00"
            ));
        } else {
            resultsPanel.add(new InfoCard(
                    "URL",
                    "https://latam.smartfense.com"
            ));
            resultsPanel.add(new InfoCard(
                    "Tipo de licenciamiento",
                    "Anual empresarial"
            ));
            resultsPanel.add(new InfoCard(
                    "Costo anual",
                    "$2,500.00"
            ));
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ===== RESET =====
    private void resetView() {
        cmbPlatform.setSelectedIndex(0);
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ===== DIBUJO DEL FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
