package secsys.views.admin;

import secsys.router.ViewRouter;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.ActionMessageFrame;
import secsys.dto.UsuarioCreateDTO;
import secsys.repository.UsuarioRepository;
import secsys.security.UsuarioValidator;
import secsys.views.planning.RepoFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminCreateUserPanel extends JPanel {

    private Image background;

    // ===== CAMPOS =====
    private JTextField txtFullName;
    private JTextField txtID;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JComboBox<String> cmbRole;
    private JPasswordField txtPass;
    private JPasswordField txtPass2;

    private final UsuarioRepository usuarioRepo;

    public AdminCreateUserPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();
        this.usuarioRepo = RepoFactory.usuarioRepository();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(820, 560));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Crear Usuario");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int y = 0;

        txtID = new JTextField(20);
        txtFullName = new JTextField(20);
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

        addField(form, c, y++, "Cédula de identidad:", txtID);
        addField(form, c, y++, "Nombre completo (Apellidos Nombres):", txtFullName);
        addField(form, c, y++, "Nombre de usuario:", txtUsername);
        addField(form, c, y++, "Correo institucional:", txtEmail);
        addField(form, c, y++, "Rol asignado:", cmbRole);
        addField(form, c, y++, "Contraseña:", txtPass);
        addField(form, c, y++, "Confirmar contraseña:", txtPass2);

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Crear usuario", "#4A90E2");

        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("admin");
        });

        btnSave.addActionListener(e -> onSave());

        buttons.add(btnBack);
        buttons.add(btnSave);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    private void onSave() {
        // 1) Validación "humana" antes de tocar BD
        String msg = validateFormMessage();
        if (msg != null) {
            ActionMessageFrame.showMsg("Verifique los datos", msg);
            return;
        }

        String user = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

        // 2) Verificación de existencia (antes del insert)
        try {
            if (usuarioRepo.existsByUsername(user)) {
                ActionMessageFrame.showMsg("Datos ya registrados", "El nombre de usuario ya está en uso. Pruebe con otro.");
                return;
            }
            if (usuarioRepo.existsByCorreo(email)) {
                ActionMessageFrame.showMsg("Datos ya registrados", "El correo ya está registrado. Verifique e intente nuevamente.");
                return;
            }
        } catch (Exception ex) {
            // Si falla la verificación por BD, igual no seguimos al insert para no confundir al usuario
            System.out.println("[ADMIN-CREATE] Error verificando existencia:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("No se pudo validar", "No se pudo verificar usuario/correo. Intente nuevamente.");
            return;
        }

        try {
            // Separar: "Apellidos Nombres"
            String full = txtFullName.getText().trim().replaceAll("\\s+", " ");
            String[] parts = full.split(" ");
            if (parts.length < 4) {
                ActionMessageFrame.showMsg(
                        "Nombre completo inválido",
                        "Debe ingresar 2 apellidos y 2 nombres."
                );
                return;
            }

            String apellidos = parts[0] + " " + parts[1];
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < parts.length; i++) {
                if (i > 2) sb.append(" ");
                sb.append(parts[i]);
            }
            String nombres = sb.toString();

            UsuarioCreateDTO dto = new UsuarioCreateDTO();
            dto.cedula = txtID.getText().trim();
            dto.apellidos = apellidos;
            dto.nombres = nombres;
            dto.username = user;
            dto.correo = email;
            dto.rol = String.valueOf(cmbRole.getSelectedItem()).trim();
            dto.password = new String(txtPass.getPassword());

            String confirm = new String(txtPass2.getPassword());

            // 3) Reglas del validador (mensaje humano si falla)
            UsuarioValidator.validateOrThrow(dto, confirm);

            // 4) Insert
            usuarioRepo.insert(dto);

            new SuccessMessageFrame("Usuario registrado correctamente.").setVisible(true);
            resetForm();
            ViewRouter.show("dashboard");

        } catch (IllegalArgumentException ex) {
            ActionMessageFrame.showMsg("Verifique los datos", safeMsg(ex));
        } catch (Exception ex) {
            System.out.println("[ADMIN-CREATE] Error creando usuario:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("No se pudo registrar", mapDbErrorToHumanMessage(ex));
        }
    }

    /**
     * Devuelve null si todo OK, o un mensaje "humano" y específico si falta/está mal algo.
     */
    private String validateFormMessage() {
        String cedula = txtID.getText() == null ? "" : txtID.getText().trim();
        String full = txtFullName.getText() == null ? "" : txtFullName.getText().trim();
        String user = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();
        String pass1 = new String(txtPass.getPassword()).trim();
        String pass2 = new String(txtPass2.getPassword()).trim();

        if (cedula.isEmpty()) return "Debe ingresar la cédula de identidad.";
        if (!cedula.matches("^\\d{10}$")) return "La cédula debe tener 10 dígitos.";

        if (full.isEmpty()) return "Debe ingresar el nombre completo (apellidos y nombres).";

        if (user.isEmpty()) return "Debe ingresar un nombre de usuario.";
        if (user.length() < 4) return "El nombre de usuario debe tener al menos 4 caracteres.";

        if (email.isEmpty()) return "Debe ingresar el correo institucional.";
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            return "Ingrese un correo válido. Ejemplo: usuario@segadvice.com";

        // ✅ Dominio obligatorio
        if (!email.toLowerCase().endsWith("@segadvice.com"))
            return "El correo debe pertenecer al dominio @segadvice.com.";

        if (cmbRole.getSelectedIndex() == 0) return "Debe seleccionar el rol del usuario.";

        if (pass1.isEmpty()) return "Debe ingresar una contraseña.";
        if (pass2.isEmpty()) return "Debe confirmar la contraseña.";
        if (!pass1.equals(pass2)) return "Las contraseñas no coinciden.";

        return null;
    }

    private void resetForm() {
        txtID.setText("");
        txtFullName.setText("");
        txtUsername.setText("");
        txtEmail.setText("");
        txtPass.setText("");
        txtPass2.setText("");
        cmbRole.setSelectedIndex(0);
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }

    private static String safeMsg(Throwable t) {
        if (t == null) return "Error inesperado.";
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? "Error inesperado." : m;
    }

    private static String mapDbErrorToHumanMessage(Exception ex) {
        String raw = safeMsg(ex).toLowerCase();

        if (raw.contains("duplicate key") || raw.contains("duplic") || raw.contains("unique")) {
            if (raw.contains("username")) return "El nombre de usuario ya está registrado. Pruebe con otro.";
            if (raw.contains("correo") || raw.contains("email")) return "El correo ya está registrado. Verifique e intente nuevamente.";
            if (raw.contains("cedula")) return "La cédula ya está registrada. Verifique e intente nuevamente.";
            return "Ya existe un usuario con esos datos. Verifique e intente nuevamente.";
        }

        if (raw.contains("check") || raw.contains("violates")) {
            return "Los datos ingresados no cumplen con el formato requerido. Revise los campos e intente nuevamente.";
        }

        return "No se pudo registrar el usuario. Intente nuevamente.";
    }
}
