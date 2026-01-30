package secsys.views.admin;

import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.ConfirmDialogFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.CustomSelectDialog;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import secsys.dto.UsuarioInfoDTO;
import secsys.repository.AdminUserRepository;
import secsys.repository.UsuarioRepository;
import secsys.views.planning.RepoFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class AdminModifyUserPanel extends JPanel {

    private Image background;

    // Buscar
    private JTextField txtSearchUsername;
    private JTextField txtSearchCedula;
    private CustomButton btnSearch;

    // Datos (bloqueados)
    private JTextField txtCedula;
    private JTextField txtNombreCompleto;

    // Editables
    private JTextField txtUsername; // NO editable
    private JTextField txtEmail;
    private JComboBox<String> cmbRole;

    private JPasswordField txtPass;
    private JPasswordField txtPass2;

    private final UsuarioRepository usuarioRepo; // solo para update
    private final AdminUserRepository adminRepo; // busquedas

    // Estado actual
    private UUID usuarioIdLoaded;

    // Icons (si no tienes, queda fallback con texto)
    private Icon showPassIcon;
    private Icon hidePassIcon;

    public AdminModifyUserPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        this.usuarioRepo = RepoFactory.usuarioRepository();
        this.adminRepo = new AdminUserRepository();

        // Intenta cargar íconos (si existen). Si no, luego usamos texto.
        // Ajusta las rutas si ya tienes tus íconos en otro lado.
        try {
            showPassIcon = new ImageIcon("src\\secsys\\resources\\password-user.png");
            hidePassIcon = new ImageIcon("src\\secsys\\resources\\password-user.png");
        } catch (Exception ignored) { }

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(860, 580));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Modificar Usuario");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));

        // ===== Buscar =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        txtSearchUsername = new JTextField(14);
        txtSearchCedula = new JTextField(14);
        btnSearch = new CustomButton("Buscar", "#4A90E2");

        searchPanel.add(new JLabel("Nombre de usuario:"));
        searchPanel.add(txtSearchUsername);
        searchPanel.add(new JLabel("Cédula:"));
        searchPanel.add(txtSearchCedula);
        searchPanel.add(btnSearch);

        // ===== Form =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        txtCedula = new JTextField(20);
        txtNombreCompleto = new JTextField(20);

        txtUsername = new JTextField(20);
        txtEmail = new JTextField(20);

        txtPass = new JPasswordField(20);
        txtPass2 = new JPasswordField(20);

        cmbRole = new JComboBox<>(new String[]{
                "Seleccione",
                "Administrador",
                "Gerente",
                "Presidente",
                "Empleado Operativo"
        });

        // Bloqueados
        txtCedula.setEditable(false);
        txtNombreCompleto.setEditable(false);
        txtUsername.setEditable(false);
        txtEmail.setEditable(false);

        addField(form, c, y++, "Cédula:", txtCedula);
        addField(form, c, y++, "Nombre completo:", txtNombreCompleto);
        addField(form, c, y++, "Nombre de usuario:", txtUsername);

        addField(form, c, y++, "Correo institucional:", txtEmail);
        addField(form, c, y++, "Rol asignado:", cmbRole);

        // ===== Password rows con toggle =====
        JPanel passRow1 = buildPasswordRow(txtPass);
        JPanel passRow2 = buildPasswordRow(txtPass2);

        addField(form, c, y++, "Nueva contraseña (opcional):", passRow1);
        addField(form, c, y++, "Confirmar contraseña:", passRow2);

        // ===== Buttons =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Guardar cambios", "#4A90E2");

        btnBack.addActionListener(e -> {
            resetAll();
            ViewRouter.show("admin");
        });

        btnSave.addActionListener(e -> onSave());

        buttons.add(btnBack);
        buttons.add(btnSave);

        // ===== Center =====
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BorderLayout());
        center.add(searchPanel, BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);

        btnSearch.addActionListener(e -> onSearch());

        setFormEnabled(false);
    }

    // =========================================================
    // Password row con toggle show/hide (misma lógica base)
    // =========================================================
    private JPanel buildPasswordRow(JPasswordField pass) {

        final char defaultEchoChar = pass.getEchoChar();

        JPanel passRow = new JPanel();
        passRow.setLayout(new BoxLayout(passRow, BoxLayout.X_AXIS));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JToggleButton togglePass = new JToggleButton();
        togglePass.setFocusable(false);
        togglePass.setPreferredSize(new Dimension(45, 45));
        togglePass.setMinimumSize(new Dimension(45, 45));
        togglePass.setMaximumSize(new Dimension(45, 45));
        togglePass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        togglePass.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        togglePass.setBackground(Color.WHITE);

        // Icon fallback si no existen imágenes
        if (showPassIcon != null) togglePass.setIcon(showPassIcon);
        else togglePass.setText("👁");

        togglePass.addActionListener(e -> {
            boolean show = togglePass.isSelected();

            // Si está vacío, no permitir mostrar
            String current = new String(pass.getPassword()).trim();
            if (current.isEmpty()) {
                pass.setEchoChar(defaultEchoChar);
                togglePass.setSelected(false);
                if (showPassIcon != null) togglePass.setIcon(showPassIcon);
                return;
            }

            pass.setEchoChar(show ? (char) 0 : defaultEchoChar);

            // Cambiar ícono si existe
            if (showPassIcon != null && hidePassIcon != null) {
                togglePass.setIcon(show ? hidePassIcon : showPassIcon);
            }
        });

        // Si el usuario borra todo y sale, resetea el toggle
        pass.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String current = new String(pass.getPassword()).trim();
                if (current.isEmpty()) {
                    pass.setEchoChar(defaultEchoChar);
                    togglePass.setSelected(false);
                    if (showPassIcon != null) togglePass.setIcon(showPassIcon);
                }
            }
        });

        passRow.add(pass);
        passRow.add(Box.createHorizontalStrut(8));
        passRow.add(togglePass);

        return passRow;
    }

    private void onSearch() {
        usuarioIdLoaded = null;
        clearForm();

        String username = txtSearchUsername.getText() == null ? "" : txtSearchUsername.getText().trim();
        String ced = txtSearchCedula.getText() == null ? "" : txtSearchCedula.getText().trim();

        boolean hasUsername = !username.isBlank();
        boolean hasCedula = !ced.isBlank();

        if (!hasUsername && !hasCedula) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Ingrese el nombre de usuario o la cédula.");
            setFormEnabled(false);
            return;
        }

        if (hasCedula && !ced.matches("\\d{10}")) {
            ActionMessageFrame.showMsg("Campos obligatorios", "La cédula debe tener 10 dígitos.");
            setFormEnabled(false);
            return;
        }

        try {
            UsuarioInfoDTO info;

            if (hasCedula) {
                info = adminRepo.findUserInfoByCedulaExact(ced);
                if (info == null) {
                    ActionMessageFrame.showMsg("Sin resultados", "Usuario no encontrado.");
                    setFormEnabled(false);
                    return;
                }
            } else {
                List<UsuarioInfoDTO> matches = adminRepo.findUserInfoByUsernameLike(username);

                if (matches == null || matches.isEmpty()) {
                    ActionMessageFrame.showMsg("Sin resultados", "Usuario no encontrado.");
                    setFormEnabled(false);
                    return;
                }

                if (matches.size() == 1) {
                    info = matches.get(0);
                } else {
                    String[] options = new String[matches.size()];
                    for (int i = 0; i < matches.size(); i++) {
                        UsuarioInfoDTO m = matches.get(i);
                        String ou = nvl(m.username);
                        String oc = nvl(m.cedula);
                        String oe = nvl(m.estado);
                        options[i] = ou + "  |  " + oc + "  |  " + oe;
                    }

                    String pick = CustomSelectDialog.showSelect(
                            SwingUtilities.getWindowAncestor(this),
                            "Seleccionar usuario",
                            "Se encontraron " + matches.size() + " usuarios.\nSeleccione uno:",
                            options
                    );

                    if (pick == null) {
                        setFormEnabled(false);
                        return;
                    }

                    int idx = 0;
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(pick)) { idx = i; break; }
                    }
                    info = matches.get(idx);
                }
            }

            usuarioIdLoaded = info.usuarioId;

            txtCedula.setText(nvl(info.cedula));
            txtNombreCompleto.setText((nvl(info.apellidos) + " " + nvl(info.nombres)).trim());
            txtUsername.setText(nvl(info.username));
            txtEmail.setText(nvl(info.correo));

            selectRole(info.rol);

            setFormEnabled(true);

        } catch (Exception ex) {
            System.out.println("[ADMIN-MODIFY] Error buscando usuario:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo buscar el usuario.");
            setFormEnabled(false);
        }
    }

    private void onSave() {
        if (usuarioIdLoaded == null) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Primero busque y cargue un usuario.");
            return;
        }

        String correo = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        if (correo.isBlank()) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Debe ingresar el correo.");
            return;
        }

        if (cmbRole.getSelectedIndex() == 0) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Debe seleccionar un rol.");
            return;
        }

        String rol = String.valueOf(cmbRole.getSelectedItem()).trim();

        String pass1 = new String(txtPass.getPassword());
        String pass2 = new String(txtPass2.getPassword());

        String passToChange = null;
        if (!pass1.isBlank() || !pass2.isBlank()) {
            if (!pass1.equals(pass2)) {
                ActionMessageFrame.showMsg("Campos obligatorios", "Las contraseñas no coinciden.");
                return;
            }
            if (pass1.length() < 6) {
                ActionMessageFrame.showMsg("Campos obligatorios", "La contraseña debe tener al menos 6 caracteres.");
                return;
            }
            passToChange = pass1;
        }

        boolean confirm = ConfirmDialogFrame.showConfirm(
                "Confirmar cambios",
                "¿Está seguro de que desea guardar los cambios del usuario?"
        );

        if (!confirm) return;

        try {
            usuarioRepo.updateEditableFields(usuarioIdLoaded, correo, rol, passToChange);

            new SuccessMessageFrame("Usuario actualizado correctamente.").setVisible(true);
            txtPass.setText("");
            txtPass2.setText("");

        } catch (Exception ex) {
            System.out.println("[ADMIN-MODIFY] Error actualizando usuario:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo actualizar el usuario. Verifique los datos.");
        }
    }

    private void setFormEnabled(boolean enabled) {
        txtEmail.setEnabled(enabled);
        cmbRole.setEnabled(enabled);
        txtPass.setEnabled(enabled);
        txtPass2.setEnabled(enabled);
    }

    private void clearForm() {
        txtCedula.setText("");
        txtNombreCompleto.setText("");
        txtUsername.setText("");
        txtEmail.setText("");
        cmbRole.setSelectedIndex(0);
        txtPass.setText("");
        txtPass2.setText("");
    }

    private void resetAll() {
        txtSearchUsername.setText("");
        txtSearchCedula.setText("");
        usuarioIdLoaded = null;
        clearForm();
        setFormEnabled(false);
    }

    private void addField(JPanel panel, GridBagConstraints c, int y, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        panel.add(field, c);
    }

    private void selectRole(String rol) {
        if (rol == null) {
            cmbRole.setSelectedIndex(0);
            return;
        }
        for (int i = 1; i < cmbRole.getItemCount(); i++) {
            if (rol.equalsIgnoreCase(cmbRole.getItemAt(i))) {
                cmbRole.setSelectedIndex(i);
                return;
            }
        }
        cmbRole.setSelectedIndex(0);
    }

    private static String nvl(String s) {
        return (s == null) ? "" : s;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
