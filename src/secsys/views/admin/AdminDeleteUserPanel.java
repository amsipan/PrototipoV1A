package secsys.views.admin;

import secsys.repository.AdminUserRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.ConfirmDialogFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.CustomSelectDialog;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminDeleteUserPanel extends JPanel {

    private Image background;

    private JTextField txtUsernameSearch;
    private JTextField txtCedulaSearch;

    private JTextField txtCedula;
    private JTextField txtFullName;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JComboBox<String> cmbRole;

    private CustomButton btnDelete;
    private CustomButton btnSearch;

    // Estado cargado desde BD
    private java.util.UUID loadedUserId;
    private String loadedEstado; // Activo / Inactivo

    // Repo (queries fuera del panel)
    private final AdminUserRepository repo = new AdminUserRepository();

    public AdminDeleteUserPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(860, 580));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Eliminar Usuario");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ===== BÚSQUEDA =====
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);

        GridBagConstraints s = new GridBagConstraints();
        s.insets = new Insets(0, 0, 0, 10);
        s.fill = GridBagConstraints.HORIZONTAL;
        s.gridy = 0;

        // Username search
        s.gridx = 0;
        searchPanel.add(new JLabel("Usuario:"), s);

        txtUsernameSearch = new JTextField(14);
        s.gridx = 1;
        searchPanel.add(txtUsernameSearch, s);

        // Cedula search (exact)
        s.gridx = 2;
        searchPanel.add(new JLabel("Cédula:"), s);

        txtCedulaSearch = new JTextField(14);
        s.gridx = 3;
        searchPanel.add(txtCedulaSearch, s);

        // Button
        btnSearch = new CustomButton("Buscar", "#4A90E2");
        s.gridx = 4;
        s.insets = new Insets(0, 0, 0, 0);
        searchPanel.add(btnSearch, s);

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int y = 0;

        txtCedula = new JTextField(20);
        txtFullName = new JTextField(20);
        txtUsername = new JTextField(20);
        txtEmail = new JTextField(20);

        cmbRole = new JComboBox<>(new String[]{
                "Administrador",
                "Gerente",
                "Presidente",
                "Empleado Operativo"
        });

        addField(form, c, y++, "Cédula:", txtCedula);
        addField(form, c, y++, "Nombre completo:", txtFullName);
        addField(form, c, y++, "Nombre de usuario:", txtUsername);
        addField(form, c, y++, "Correo institucional:", txtEmail);
        addField(form, c, y++, "Rol:", cmbRole);

        // Solo lectura
        setFormEnabled(false);

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        btnDelete = new CustomButton("Eliminar usuario", "#E53935");
        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");

        btnDelete.setEnabled(false);

        btnSearch.addActionListener(e -> onSearch());
        btnDelete.addActionListener(e -> confirmDisableUser());
        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("admin");
        });

        buttons.add(btnBack);
        buttons.add(btnDelete);

        // ===== CONTENEDOR CENTRAL =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(searchPanel);
        center.add(Box.createVerticalStrut(15));
        center.add(form);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    // ==========================
    // BÚSQUEDA (username LIKE o cédula exacta)
    // ==========================
    private void onSearch() {
        clearLoadedState();
        setFormEnabled(false);
        btnDelete.setEnabled(false);
        clearFormFields();

        String usernameIn = txtUsernameSearch.getText() == null ? "" : txtUsernameSearch.getText().trim();
        String cedulaIn = txtCedulaSearch.getText() == null ? "" : txtCedulaSearch.getText().trim();

        boolean hasCedula = !cedulaIn.isBlank();
        boolean hasUsername = !usernameIn.isBlank();

        // Debe ingresar algo
        if (!hasCedula && !hasUsername) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Ingrese el nombre de usuario o la cédula para buscar.");
            return;
        }

        // ✅ Validación de cédula (solo si se usa)
        if (hasCedula) {
            if (cedulaIn.length() != 10 || !cedulaIn.matches("\\d{10}")) {
                ActionMessageFrame.showMsg("Campos obligatorios", "La cédula debe tener 10 dígitos.");
                return;
            }
        }

        try {
            List<AdminUserRepository.UserRow> rows;

            // Regla: si hay cédula => búsqueda exacta por cédula; caso contrario username LIKE
            if (hasCedula) {
                rows = repo.searchByCedulaExact(cedulaIn);
            } else {
                rows = repo.searchByUsernameLike(usernameIn);
            }

            if (rows.isEmpty()) {
                ActionMessageFrame.showMsg("Sin resultados",
                        hasCedula
                                ? "Usuario con cédula no encontrado."
                                : "Nombre de usuario no encontrado.");
                return;
            }

            // Si hay más de uno, escoger
            int pickIdx = 0;
            if (rows.size() > 1) {
                String[] options = new String[rows.size()];
                for (int i = 0; i < rows.size(); i++) {
                    String u = nvl(rows.get(i).username);
                    String cedu = nvl(rows.get(i).cedula);
                    String est = nvl(rows.get(i).estado);
                    options[i] = u + "  |  " + cedu + "  |  " + est;
                }

                String pick = CustomSelectDialog.showSelect(
                        SwingUtilities.getWindowAncestor(this),
                        "Seleccionar usuario",
                        "Se encontraron " + rows.size() + " usuarios.\nSeleccione uno:",
                        options
                );

                if (pick == null) return;

                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(pick)) { pickIdx = i; break; }
                }
            }

            // Cargar seleccionado
            AdminUserRepository.UserRow r = rows.get(pickIdx);

            loadedUserId = r.usuarioId;
            loadedEstado = r.estado;

            txtCedula.setText(nvl(r.cedula));
            txtFullName.setText(buildNombreCompleto(r.apellidos, r.nombres));
            txtUsername.setText(nvl(r.username));
            txtEmail.setText(nvl(r.correo));

            if (r.rol != null && !r.rol.isBlank()) cmbRole.setSelectedItem(r.rol);

            // Siempre solo lectura
            setFormEnabled(false);

            // Si ya está Inactivo, no permitir “eliminar”
            if ("Inactivo".equalsIgnoreCase(nvl(loadedEstado))) {
                btnDelete.setEnabled(false);
                ActionMessageFrame.showMsg("Aviso", "El usuario ya se encuentra Inactivo.");
            } else {
                btnDelete.setEnabled(true);
            }

        } catch (Exception ex) {
            System.out.println("[ADMIN-DELETE] Error buscando usuario:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo consultar el usuario. Intente nuevamente.");
        }
    }

    // ==========================
    // “Eliminar” = pasar a Inactivo
    // ==========================
    private void confirmDisableUser() {
        if (loadedUserId == null) return;

        boolean ok = ConfirmDialogFrame.showConfirm(
                "Confirmar desactivación",
                "¿Está seguro de que desea desactivar este usuario?"
        );

        if (!ok) return;

        try {
            int updated = repo.disableUser(loadedUserId);

            if (updated == 0) {
                ActionMessageFrame.showMsg("Aviso", "No se actualizó ningún registro.");
                return;
            }

            new SuccessMessageFrame("Usuario desactivado exitosamente.").setVisible(true);

            resetForm();
            ViewRouter.show("dashboard");

        } catch (Exception ex) {
            System.out.println("[ADMIN-DELETE] Error desactivando usuario:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo desactivar el usuario. Intente nuevamente.");
        }
    }


    // ==========================
    // UI helpers
    // ==========================
    private void setFormEnabled(boolean enabled) {
        // Solo lectura SIEMPRE en este panel
        txtCedula.setEnabled(enabled);
        txtFullName.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtEmail.setEnabled(enabled);
        cmbRole.setEnabled(enabled);
    }

    private void clearLoadedState() {
        loadedUserId = null;
        loadedEstado = null;
    }

    private void clearFormFields() {
        txtCedula.setText("");
        txtFullName.setText("");
        txtUsername.setText("");
        txtEmail.setText("");
        cmbRole.setSelectedIndex(0);
    }

    private void resetForm() {
        clearLoadedState();
        txtUsernameSearch.setText("");
        txtCedulaSearch.setText("");
        clearFormFields();
        btnDelete.setEnabled(false);
        setFormEnabled(false);
    }

    private void addField(JPanel panel, GridBagConstraints c, int y, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0.35;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 0.65;
        panel.add(field, c);
    }

    private static String buildNombreCompleto(String apellidos, String nombres) {
        String a = apellidos == null ? "" : apellidos.trim();
        String n = nombres == null ? "" : nombres.trim();
        String out = (a + " " + n).trim();
        return out.isBlank() ? "-" : out;
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    // ===== FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
