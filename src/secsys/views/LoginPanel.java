package secsys.views;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.PlaceholderFocus;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;
import secsys.views.planning.RepoFactory;
import secsys.repository.UsuarioRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class LoginPanel extends JPanel {

    private Image background;
    private Image logo;
    private ImageIcon showPass;

    private final UsuarioRepository usuarioRepo;

    private JTextField username;
    private JPasswordField pass;
    private JComboBox<String> cmbRol;

    private char defaultEchoChar;

    private static final String PH_USER = "Usuario";
    private static final String PH_PASS = "Contraseña";

    // ✅ Ajusta estos valores EXACTOS a lo que tienes en la tabla usuarios
    private static final String[] ROLE_ITEMS = new String[]{
            "Seleccione",
            "Empleado Operativo",
            "Presidente",
            "Gerente",
            "Administrador"
    };

    public LoginPanel() {
        this.usuarioRepo = RepoFactory.usuarioRepository();

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();
        Image original = new ImageIcon("src\\secsys\\resources\\logo.png").getImage();
        logo = original.getScaledInstance(200, 60, Image.SCALE_SMOOTH);

        showPass = new ImageIcon("src\\secsys\\resources\\password-user.png");

        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(420, 430)); // un poco más alto por el rol
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Inicio de sesión");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(25));

        username = createTextField(PH_USER);

        // ✅ Password NORMAL primero (oculta por defecto)
        pass = createPasswordFieldBase();
        defaultEchoChar = pass.getEchoChar();

        // ✅ Placeholder al password
        setPassPlaceholder();

        // ✅ Rol
        cmbRol = createRoleCombo();

        JLabel error = new JLabel(" ");
        error.setForeground(Color.RED);
        error.setAlignmentX(Component.CENTER_ALIGNMENT);

        CustomButton loginBtn = new CustomButton("Ingresar", "#4A90E2");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setPreferredSize(new Dimension(200, 45));
        loginBtn.setMaximumSize(new Dimension(200, 45));

        CustomButton loginLost = new CustomButton("¿Olvido su contraseña?", "#4A90E2");
        loginLost.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginLost.setPreferredSize(new Dimension(200, 45));
        loginLost.setMaximumSize(new Dimension(200, 45));

        // ✅ Fila password + toggle
        JPanel passRow = new JPanel();
        passRow.setLayout(new BoxLayout(passRow, BoxLayout.X_AXIS));
        passRow.setOpaque(false);
        passRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JToggleButton togglePass = new JToggleButton();
        togglePass.setFocusable(false);
        togglePass.setPreferredSize(new Dimension(45, 45));
        togglePass.setMinimumSize(new Dimension(45, 45));
        togglePass.setMaximumSize(new Dimension(45, 45));
        togglePass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        togglePass.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        togglePass.setBackground(Color.WHITE);
        togglePass.setIcon(showPass);

        togglePass.addActionListener(e -> {
            boolean show = togglePass.isSelected();

            if (isPassPlaceholder()) {
                pass.setEchoChar((char) 0);
                togglePass.setSelected(false);
                return;
            }
            pass.setEchoChar(show ? (char) 0 : defaultEchoChar);
        });

        pass.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isPassPlaceholder()) {
                    clearPassPlaceholder();
                    pass.setEchoChar(defaultEchoChar);
                    togglePass.setSelected(false);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String current = new String(pass.getPassword()).trim();
                if (current.isEmpty()) {
                    setPassPlaceholder();
                    togglePass.setSelected(false);
                }
            }
        });

        passRow.add(pass);
        passRow.add(Box.createRigidArea(new Dimension(10, 0)));
        passRow.add(togglePass);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);

        buttonsPanel.add(loginBtn);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        buttonsPanel.add(loginLost);

        // ✅ LOGIN: validar user + pass + rol y autenticar
        loginBtn.addActionListener(e -> {
            String userText = username.getText();
            String passText = new String(pass.getPassword());
            String rolText = (cmbRol.getSelectedItem() == null) ? "" : cmbRol.getSelectedItem().toString();

            boolean userInvalid = userText == null || userText.isBlank() || userText.equals(PH_USER);
            boolean passInvalid = passText.isBlank() || isPassPlaceholder() || passText.equals(PH_PASS);
            boolean rolInvalid  = rolText.isBlank() || "Seleccione".equalsIgnoreCase(rolText);

            if (userInvalid || passInvalid || rolInvalid) {
                error.setText("Credenciales incorrectas");
                return;
            }

            try {
                // ✅ NUEVO: validar también el rol
                var session = usuarioRepo.authenticate(userText.trim(), passText, rolText);

                if (session == null) {
                    error.setText("Credenciales incorrectas");
                    return;
                }

                secsys.AppSession.set(session);

                boolean isOperativo = secsys.AppSession.isOperative();

                boolean showAdmin = !isOperativo;
                boolean showPlatforms = !isOperativo;
                boolean showAudit = !isOperativo;

                ViewRouter.register("dashboard",
                        new secsys.views.dashboard.DashboardPanel(showAudit, showAdmin, showPlatforms));
                ViewRouter.show("dashboard");

            } catch (Exception ex) {
                System.err.println("[LOGIN] Error autenticando:");
                ex.printStackTrace();
                error.setText("Credenciales incorrectas");
            }
        });

        loginLost.addActionListener(e -> {
            new SuccessMessageFrame("Código de recuperación enviado al correo registrado").setVisible(true);
            ViewRouter.show("login");
        });

        card.add(username);
        card.add(Box.createVerticalStrut(15));

        // ✅ Rol entre user y pass (puedes moverlo si quieres)
        card.add(cmbRol);
        card.add(Box.createVerticalStrut(15));

        card.add(passRow);
        card.add(Box.createVerticalStrut(20));
        card.add(error);
        card.add(Box.createVerticalStrut(20));
        card.add(buttonsPanel);

        add(card);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);

        int logoWidth = 120;
        int logoHeight = 50;
        g.drawImage(logo, 30, 30, logoWidth, logoHeight, this);
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 12, 10, 12)
        ));

        field.setForeground(Color.GRAY);
        field.addFocusListener(new PlaceholderFocus(field, placeholder));
        return field;
    }

    private JComboBox<String> createRoleCombo() {
        JComboBox<String> cmb = new JComboBox<>(ROLE_ITEMS);
        cmb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        cmb.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(5, 8, 5, 8)
        ));
        cmb.setBackground(Color.WHITE);
        return cmb;
    }

    // ✅ Password base: sin placeholder aquí
    private JPasswordField createPasswordFieldBase() {
        JPasswordField field = new JPasswordField();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 12, 10, 12)
        ));
        return field;
    }

    private boolean isPassPlaceholder() {
        return pass.getForeground().equals(Color.GRAY)
                && new String(pass.getPassword()).equals(PH_PASS)
                && pass.getEchoChar() == (char) 0;
    }

    private void setPassPlaceholder() {
        pass.setText(PH_PASS);
        pass.setForeground(Color.GRAY);
        pass.setEchoChar((char) 0);
    }

    private void clearPassPlaceholder() {
        pass.setText("");
        pass.setForeground(Color.BLACK);
    }
}
