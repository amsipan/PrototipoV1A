package secsys.views.audit;

import secsys.router.ViewRouter;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.CustomButton;
import secsys.views.addons.AuditLogCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AuditPanel extends JPanel {

    private Image background;
    private JPanel resultsPanel;

    public AuditPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(950, 600));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Auditoría");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        // ===== BOTONES =====
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actions.setOpaque(false);

        CustomButton btnLogs = new CustomButton("Ver logs", "#4A90E2");
        CustomButton btnReport = new CustomButton("Generar reporte", "#5DA9E9");
        CustomButton btnHistory = new CustomButton("Historial de reportes", "#6C63FF");

        actions.add(btnLogs);
        actions.add(btnReport);
        actions.add(btnHistory);

        // ===== RESULTADOS =====
        resultsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        resultsPanel.setOpaque(false);

        RoundedPanel container = new RoundedPanel(18);
        container.setBackground(new Color(245, 247, 250));
        container.setBorder(new EmptyBorder(15, 15, 15, 15));
        container.setLayout(new BorderLayout());
        container.setPreferredSize(new Dimension(860, 360));
        container.add(resultsPanel, BorderLayout.CENTER);

        // ===== ACCIONES =====
        btnLogs.addActionListener(e -> showMockLogs());

        btnReport.addActionListener(e ->
                new AuditReportDialog(
                        (JFrame) SwingUtilities.getWindowAncestor(this)
                ).setVisible(true)
        );

        btnHistory.addActionListener(e -> showMockReports());

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> {
            resultsPanel.removeAll();
            resultsPanel.repaint();
            ViewRouter.show("dashboard");
        });

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(btnBack);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(actions);
        center.add(Box.createVerticalStrut(10));
        center.add(container);

        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    // ===== LOGS =====
    private void showMockLogs() {
        resultsPanel.removeAll();

        resultsPanel.add(new AuditLogCard(
                "Creación de usuario",
                "Usuario creado en el sistema",
                "admin",
                "12/04/2024"
        ));

        resultsPanel.add(new AuditLogCard(
                "Consulta de cliente",
                "Consulta de información del cliente",
                "empleado",
                "13/04/2024"
        ));

        resultsPanel.add(new AuditLogCard(
                "Eliminación de usuario",
                "Usuario eliminado del sistema",
                "admin",
                "14/04/2024"
        ));

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ===== HISTORIAL DE REPORTES =====
    private void showMockReports() {
        resultsPanel.removeAll();

        resultsPanel.add(new AuditLogCard(
                "Reporte generado",
                "Auditoría completa",
                "admin",
                "10/04/2024"
        ));

        resultsPanel.add(new AuditLogCard(
                "Reporte generado",
                "Filtrado por modificaciones",
                "gerente",
                "11/04/2024"
        ));

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
