package secsys.views.finance;

import secsys.router.ViewRouter;
import secsys.views.addons.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class QuotationLinkPanel extends JPanel {

    private Image background;

    private JPanel resultsPanel;
    private JTextField txtRuc;
    private CustomButton btnLink;

    public QuotationLinkPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(820, 520));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Enlazar Cotización a Cliente");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // ===== FORM SUPERIOR =====
        JPanel topForm = new JPanel(new GridBagLayout());
        topForm.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        JTextField txtQuote = new JTextField(15);
        CustomButton btnSearch = new CustomButton("Buscar cotización", "#4A90E2");

        // Número de cotización
        c.gridx = 0;
        c.gridy = y;
        topForm.add(new JLabel("Número de cotización:"), c);

        c.gridx = 1;
        topForm.add(txtQuote, c);

        c.gridx = 2;
        topForm.add(btnSearch, c);

        // ===== RESULTADOS (CARDS) =====
        resultsPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        resultsPanel.setOpaque(false);

        RoundedPanel resultsContainer = new RoundedPanel(18);
        resultsContainer.setLayout(new BorderLayout());
        resultsContainer.setBackground(new Color(245, 247, 250));
        resultsContainer.setBorder(new EmptyBorder(12, 12, 12, 12));
        resultsContainer.setPreferredSize(new Dimension(760, 220));
        resultsContainer.add(resultsPanel, BorderLayout.CENTER);

        // ===== FORM INFERIOR (RUC) =====
        JPanel bottomForm = new JPanel(new GridBagLayout());
        bottomForm.setOpaque(false);

        txtRuc = new JTextField(15);
        txtRuc.setEnabled(false);

        btnLink = new CustomButton("Enlazar cotización", "#4A90E2");
        btnLink.setEnabled(false);

        y = 0;

        c.gridx = 0;
        c.gridy = y;
        bottomForm.add(new JLabel("RUC del cliente:"), c);

        c.gridx = 1;
        bottomForm.add(txtRuc, c);

        c.gridx = 2;
        bottomForm.add(btnLink, c);

        // ===== ACCIÓN BUSCAR (SIMULADO) =====
        btnSearch.addActionListener(e -> showMockQuotation());

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");

        btnBack.addActionListener(e -> {
                resetQuotation(txtQuote);
                ViewRouter.show("dashboard");
        });

        btnLink.addActionListener(e -> {
            new SuccessMessageFrame("Cotización # enlazada correctamente").setVisible(true);
            ViewRouter.show("dashboard");
        });

        buttons.add(btnBack);

        // ===== CONTENEDOR CENTRAL =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(topForm);
        center.add(Box.createVerticalStrut(10));
        center.add(resultsContainer);
        center.add(Box.createVerticalStrut(10));
        center.add(bottomForm);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    // ===== RESULTADOS MOCK =====
    private void showMockQuotation() {
        resultsPanel.removeAll();

        resultsPanel.add(new InfoCard("Cotización", "#COT-2024-001"));
        resultsPanel.add(new InfoCard("Empresa", "Empresa Ejemplo S.A."));
        resultsPanel.add(new InfoCard("Fecha", "15/03/2024"));
        resultsPanel.add(new InfoCard("Subtotal", "$1,200.00"));
        resultsPanel.add(new InfoCard("IVA", "$144.00"));
        resultsPanel.add(new InfoCard("Total", "$1,344.00"));

        resultsPanel.revalidate();
        resultsPanel.repaint();

        // Habilitar enlace
        txtRuc.setEnabled(true);
        btnLink.setEnabled(true);
    }

    // ===== RESET VISUAL =====
    private void resetQuotation(JTextField txtQuote) {
        txtQuote.setText("");
        txtRuc.setText("");
        txtRuc.setEnabled(false);
        btnLink.setEnabled(false);

        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ===== FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
