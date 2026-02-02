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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

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

    // Toggle + echo
    private char defaultEchoCharPass;
    private char defaultEchoCharPass2;
    private JToggleButton togglePass;
    private JToggleButton togglePass2;

    // Placeholder de password (solo UI)
    private static final String PASS_PLACEHOLDER = "******";
    private boolean passPlaceholder = false;
    private boolean pass2Placeholder = false;

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

        // Guardar echo chars por defecto
        defaultEchoCharPass = txtPass.getEchoChar();
        defaultEchoCharPass2 = txtPass2.getEchoChar();

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

        // ===== Password row (con toggle) =====
        addField(form, c, y++, "Contraseña:", buildPasswordRow(txtPass, true));
        addField(form, c, y++, "Confirmar contraseña:", buildPasswordRow(txtPass2, false));

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

        // (Opcional) placeholder visual inicial (si NO quieres placeholder, comenta estas 2 líneas)
        // setPassPlaceholder(txtPass, true);
        // setPassPlaceholder(txtPass2, false);
    }

    private JPanel buildPasswordRow(JPasswordField pass, boolean first) {
        JPanel passRow = new JPanel();
        passRow.setLayout(new BoxLayout(passRow, BoxLayout.X_AXIS));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        pass.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        
        // ===== ICONOS LOCALES (ruta relativa) =====
        // OFF = contraseña oculta
        Icon hidePass = new ImageIcon("src\\secsys\\resources\\password-user.png");
        
        // ON = contraseña visible (si NO tienes este archivo, comenta esta línea
        // y usa hidePass para ambos)
        Icon showPass = new ImageIcon("src\\secsys\\resources\\password-show.png");
        // Icon showPass = hidePass;
        
        JToggleButton toggle = new JToggleButton();
        toggle.setFocusable(false);
        toggle.setPreferredSize(new Dimension(45, 45));
        toggle.setMinimumSize(new Dimension(45, 45));
        toggle.setMaximumSize(new Dimension(45, 45));
        toggle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        toggle.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        toggle.setBackground(Color.WHITE);
        
        // Icono inicial (oculto)
        toggle.setIcon(hidePass);
        
        if (first) togglePass = toggle; else togglePass2 = toggle;
        
        final char defaultEchoChar = first ? defaultEchoCharPass : defaultEchoCharPass2;
        
        toggle.addActionListener(e -> {
            boolean show = toggle.isSelected();
        
            if (isPassPlaceholder(first)) {
                pass.setEchoChar((char) 0);
                toggle.setSelected(false);
                toggle.setIcon(hidePass);
                return;
            }
        
            pass.setEchoChar(show ? (char) 0 : defaultEchoChar);
            toggle.setIcon(show ? showPass : hidePass);
        });
    
        pass.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isPassPlaceholder(first)) {
                    clearPassPlaceholder(pass, first);
                    pass.setEchoChar(defaultEchoChar);
                    toggle.setSelected(false);
                    toggle.setIcon(hidePass);
                }
            }
        
            @Override
            public void focusLost(FocusEvent e) {
                String current = new String(pass.getPassword()).trim();
                if (current.isEmpty()) {
                    setPassPlaceholder(pass, first);
                    toggle.setSelected(false);
                    toggle.setIcon(hidePass);
                }
            }
        });
    
        passRow.add(pass);
        passRow.add(Box.createHorizontalStrut(8));
        passRow.add(toggle);
    
        return passRow;
    }


    private boolean isPassPlaceholder(boolean first) {
        return first ? passPlaceholder : pass2Placeholder;
    }

    private void setPassPlaceholder(JPasswordField pass, boolean first) {
        pass.setText(PASS_PLACEHOLDER);
        pass.setEchoChar((char) 0); // que se vea el placeholder
        if (first) passPlaceholder = true; else pass2Placeholder = true;
    }

    private void clearPassPlaceholder(JPasswordField pass, boolean first) {
        pass.setText("");
        if (first) passPlaceholder = false; else pass2Placeholder = false;
    }

    private void onSave() {
        String msg = validateFormMessage();
        if (msg != null) {
            ActionMessageFrame.showMsg("Verifique los datos", msg);
            return;
        }

        String user = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();

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
            System.out.println("[ADMIN-CREATE] Error verificando existencia:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("No se pudo validar", "No se pudo verificar usuario/correo. Intente nuevamente.");
            return;
        }

        try {
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

            // Si hubiera placeholder (no recomendado para create), lo tratamos como vacío
            String pass1 = isPassPlaceholder(true) ? "" : new String(txtPass.getPassword());
            String pass2 = isPassPlaceholder(false) ? "" : new String(txtPass2.getPassword());

            dto.password = pass1;

            UsuarioValidator.validateOrThrow(dto, pass2);

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

    private String validateFormMessage() {
        String cedula = txtID.getText() == null ? "" : txtID.getText().trim();
        String full = txtFullName.getText() == null ? "" : txtFullName.getText().trim();
        String user = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

        String pass1 = isPassPlaceholder(true) ? "" : new String(txtPass.getPassword()).trim();
        String pass2 = isPassPlaceholder(false) ? "" : new String(txtPass2.getPassword()).trim();

        if (cedula.isEmpty()) return "Debe ingresar la cédula de identidad.";
        if (!cedula.matches("^\\d{10}$")) return "La cédula debe tener 10 dígitos.";
        if (!isValidEcuadorCedula(cedula)) return "Cédula inválida (verifique el número).";


        if (full.isEmpty()) return "Debe ingresar el nombre completo (apellidos y nombres).";

        if (user.isEmpty()) return "Debe ingresar un nombre de usuario.";
        if (user.length() < 4) return "El nombre de usuario debe tener al menos 4 caracteres.";

        if (email.isEmpty()) return "Debe ingresar el correo institucional.";
        if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            return "Ingrese un correo válido. Ejemplo: usuario@segadvice.com";

        if (!email.toLowerCase().endsWith("@segadvice.com"))
            return "El correo debe pertenecer al dominio @segadvice.com.";

        if (cmbRole.getSelectedIndex() == 0) return "Debe seleccionar el rol del usuario.";

        if (pass1.isEmpty()) return "Debe ingresar una contraseña.";
        if (pass2.isEmpty()) return "Debe confirmar la contraseña.";
        if (!pass1.equals(pass2)) return "Las contraseñas no coinciden.";

        // Reglas contraseña: 1 mayúscula, 1 número, 1 símbolo especial
if (!pass1.matches(".*[A-Z].*"))
    return "La contraseña debe contener al menos una letra mayúscula.";

if (!pass1.matches(".*\\d.*"))
    return "La contraseña debe contener al menos un número.";

if (!pass1.matches(".*[^A-Za-z0-9].*"))
    return "La contraseña debe contener al menos un símbolo especial.";


        return null;
    }

    private void resetForm() {
        txtID.setText("");
        txtFullName.setText("");
        txtUsername.setText("");
        txtEmail.setText("");

        txtPass.setText("");
        txtPass2.setText("");
        passPlaceholder = false;
        pass2Placeholder = false;

        // devolver echo char a oculto y apagar toggles
        txtPass.setEchoChar(defaultEchoCharPass);
        txtPass2.setEchoChar(defaultEchoCharPass2);
        if (togglePass != null) togglePass.setSelected(false);
        if (togglePass2 != null) togglePass2.setSelected(false);

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

    // ===== Icono simple (sin .jar) dibujando texto =====
    private static class TextIcon implements Icon {
        private final String text;
        private final Font font;
        private final Color color;

        TextIcon(String text, Font font, Color color) {
            this.text = text;
            this.font = font;
            this.color = color;
        }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(font);
                g2.setColor(color);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (getIconWidth() - fm.stringWidth(text)) / 2;
                int ty = y + (getIconHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
            } finally {
                g2.dispose();
            }
        }

        @Override public int getIconWidth() { return 24; }
        @Override public int getIconHeight() { return 24; }
    }

    // =====================================================
// ✅ Validación de cédula ecuatoriana (10 dígitos)
// Módulo 10: aplica para 3er dígito 0..5
// =====================================================
private static boolean isValidEcuadorCedula(String ced) {
    if (ced == null) return false;
    String s = ced.trim();
    if (!s.matches("^\\d{10}$")) return false;

    int prov = Integer.parseInt(s.substring(0, 2));
    if (prov < 1 || prov > 24) return false;

    int third = s.charAt(2) - '0';
    if (third < 0 || third > 5) return false;

    int sum = 0;
    for (int i = 0; i < 9; i++) {
        int d = s.charAt(i) - '0';

        // posiciones impares (0,2,4,6,8) *2
        if (i % 2 == 0) {
            d *= 2;
            if (d > 9) d -= 9;
        }

        sum += d;
    }

    int mod = sum % 10;
    int check = (mod == 0) ? 0 : (10 - mod);

    int last = s.charAt(9) - '0';
    return check == last;
}

}
