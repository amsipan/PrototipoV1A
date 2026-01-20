package secsys.views.admin;

import secsys.router.ViewRouter;
import secsys.views.addons.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminConsultUserPanel extends JPanel {

    private Image background;
    private JPanel resultsPanel;
    private JTextField txtCedula;

    public AdminConsultUserPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(900, 560));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Consultar Usuarios");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        // ===== BÚSQUEDA =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        searchPanel.setOpaque(false);

        txtCedula = new JTextField(15);
        CustomButton btnSearch = new CustomButton("Buscar", "#4A90E2");
        CustomButton btnShowAll = new CustomButton("Mostrar todos", "#5DA9E9");

        searchPanel.add(new JLabel("Número de cédula:"));
        searchPanel.add(txtCedula);
        searchPanel.add(btnSearch);
        searchPanel.add(btnShowAll);

        // ===== RESULTADOS =====
        resultsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        resultsPanel.setOpaque(false);

        RoundedPanel resultsContainer = new RoundedPanel(18);
        resultsContainer.setLayout(new BorderLayout());
        resultsContainer.setBackground(new Color(245, 247, 250));
        resultsContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        resultsContainer.setPreferredSize(new Dimension(820, 320));
        resultsContainer.add(resultsPanel, BorderLayout.CENTER);

        // ===== ACCIONES =====
        btnSearch.addActionListener(e -> showMockFiltered());
        btnShowAll.addActionListener(e -> showMockAll());

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> {
            resetResults();
            ViewRouter.show("admin");
        });

        

        buttons.add(btnBack);

        // ===== CONTENEDOR CENTRAL =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(searchPanel);
        center.add(Box.createVerticalStrut(10));
        center.add(resultsContainer);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);

        // Mostrar todos por defecto
        showMockAll();
    }

    // ===== MOSTRAR TODOS =====
    private void showMockAll() {
        resultsPanel.removeAll();

        resultsPanel.add(new UserInfoCard(
                "Juan Pérez", "0102030405", "jperez",
                "juan.perez@segadvice.com", "Administrador", "Activo"
        ));

        resultsPanel.add(new UserInfoCard(
                "María López", "0918273645", "mlopez",
                "maria.lopez@segadvice.com", "Empleado Operativo", "Activo"
        ));

        resultsPanel.add(new UserInfoCard(
                "Carlos Ruiz", "1102938475", "cruiz",
                "carlos.ruiz@segadvice.com", "Gerente", "Inactivo"
        ));
        

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ===== FILTRADO POR CÉDULA =====
    private void showMockFiltered() {
        resultsPanel.removeAll();

        resultsPanel.add(new UserInfoCard(
                "Carlos Ruiz", "1102938475", "cruiz",
                "carlos.ruiz@segadvice.com", "Gerente", "Inactivo"
        ));

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ===== RESET =====
    private void resetResults() {
        txtCedula.setText("");
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
