package secsys.views.admin;

import secsys.repository.AdminUserRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminConsultUserPanel extends JPanel {

    private Image background;
    private JPanel resultsPanel;

    private JTextField txtUsername; // (en tu mensaje lo llamas “razón social”)
    private JTextField txtCedula;

    private final AdminUserRepository repo = new AdminUserRepository();

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

        txtUsername = new JTextField(15);
        txtCedula = new JTextField(15);

        CustomButton btnSearch = new CustomButton("Buscar", "#4A90E2");
        CustomButton btnShowAll = new CustomButton("Mostrar todos", "#5DA9E9");

        // Si tu UI realmente dice “Razón social”, cambia el label aquí
        searchPanel.add(new JLabel("Nombre de usuario:"));
        searchPanel.add(txtUsername);
        searchPanel.add(new JLabel("Cédula:"));
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
        btnSearch.addActionListener(e -> showFilteredActiveFromDb());
        btnShowAll.addActionListener(e -> showAllActiveFromDb());

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

        // Cargar desde BD al abrir
        showAllActiveFromDb();
    }

    // ==========================
    // Mostrar SOLO activos
    // ==========================
    private void showAllActiveFromDb() {
        resultsPanel.removeAll();

        try {
            List<AdminUserRepository.UserRow> rows = repo.searchActiveAll();

            if (rows.isEmpty()) {
                resultsPanel.add(new InfoCard("Sin usuarios", "No existen usuarios Activos."));
            } else {
                for (AdminUserRepository.UserRow r : rows) {
                    resultsPanel.add(new UserInfoCard(
                            buildNombreCompleto(r.apellidos, r.nombres),
                            nvl(r.cedula),
                            nvl(r.username),
                            nvl(r.correo),
                            nvl(r.rol),
                            nvl(r.estado)
                    ));
                }
            }

        } catch (Exception ex) {
            System.out.println("[ADMIN-CONSULT] Error cargando usuarios activos:");
            ex.printStackTrace();
            resultsPanel.add(new InfoCard("Error", "No se pudo consultar usuarios en la base de datos."));
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ==========================
    // Buscar por username (COINCIDENCIA) o cédula (EXACTA), solo Activo
    // Regla: si hay cédula => usa cédula; caso contrario usa username/razón social
    // ==========================
   private void showFilteredActiveFromDb() {
        resultsPanel.removeAll();

        String usernameIn = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String cedulaIn = txtCedula.getText() == null ? "" : txtCedula.getText().trim();

        boolean hasCedula = !cedulaIn.isBlank();
        boolean hasUsername = !usernameIn.isBlank();

        if (!hasCedula && !hasUsername) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Ingrese un nombre de usuario o cédula");
            return;
        }

        // Validación de cédula (solo si se usa)
        if (hasCedula) {
            if (cedulaIn.length() != 10 || !cedulaIn.matches("\\d{10}")) {
                ActionMessageFrame.showMsg("Campos obligatorios", "La cédula debe tener 10 dígitos.");
                return;
            }
        }

        try {
            List<AdminUserRepository.UserRow> rows;

            if (hasCedula) {
                rows = repo.searchActiveByCedulaExact(cedulaIn);     // exacto
                if (rows.isEmpty()) {
                    ActionMessageFrame.showMsg("Sin resultados", "Usuario no encontrado.");
                    return;
                }
            } else {
                rows = repo.searchActiveByUsernameLike(usernameIn);  // coincidencia
                if (rows.isEmpty()) {
                    // ✅ lo que pediste: sin card
                    ActionMessageFrame.showMsg("Sin resultados", "Usuario no encontrado.");
                    return;
                }
            }

            // Pintar resultados encontrados
            for (AdminUserRepository.UserRow r : rows) {
                resultsPanel.add(new UserInfoCard(
                        buildNombreCompleto(r.apellidos, r.nombres),
                        nvl(r.cedula),
                        nvl(r.username),
                        nvl(r.correo),
                        nvl(r.rol),
                        nvl(r.estado)
                ));
            }

        } catch (Exception ex) {
            System.out.println("[ADMIN-CONSULT] Error buscando usuario activo:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo consultar el usuario en la base de datos.");
            return;
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }


    private static String buildNombreCompleto(String apellidos, String nombres) {
        String a = (apellidos == null) ? "" : apellidos.trim();
        String n = (nombres == null) ? "" : nombres.trim();
        String out = (a + " " + n).trim();
        return out.isBlank() ? "-" : out;
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // ===== RESET =====
    private void resetResults() {
        txtUsername.setText("");
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
