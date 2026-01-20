package secsys.views;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.PlaceholderFocus;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {

    private Image background;
    private Image logo;

    public LoginPanel() {
        // Cargar imágenes
        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();
        Image original = new ImageIcon("src\\secsys\\resources\\logo.png").getImage();

        int newWidth = 200;  // ancho deseado
        int newHeight = 60;  // alto deseado
            
        logo = original.getScaledInstance(
                newWidth,
                newHeight,
                Image.SCALE_SMOOTH
        );


        setLayout(new GridBagLayout());
        setOpaque(false);

        // ---- Card del login ----
        JPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(420, 360));
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Inicio de sesión");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Gap grande entre título e inputs
        card.add(title);
        card.add(Box.createVerticalStrut(35));

        JTextField userField = createTextField("Usuario");
        JPasswordField passField = createPasswordField("Contraseña");

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


        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setOpaque(false);
            
        buttonsPanel.add(loginBtn);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 15))); // GAP VERTICAL
        buttonsPanel.add(loginLost);

        loginBtn.addActionListener(e -> {

            String userText = userField.getText();
            String passText = new String(passField.getPassword());

            boolean userInvalid =
                    userText.isEmpty() || userText.equals("Usuario");

            boolean passInvalid =
                    passText.isEmpty() || passText.equals("Contraseña");

            if (userInvalid || passInvalid) {
                error.setText("Ingrese usuario y contraseña");
                return;
            }
        
            error.setText(" "); // limpia error
            ViewRouter.show("dashboard");
        });

        loginLost.addActionListener(e -> {
            new SuccessMessageFrame("Código de recuperación enviado al correo registrado").setVisible(true);
            ViewRouter.show("login");
        });

        card.add(userField);
        card.add(Box.createVerticalStrut(15));
        card.add(passField);
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
        g.drawImage(
                logo,
                30,
                30,
                logoWidth,
                logoHeight,
                this
        );
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

    private JPasswordField createPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField(placeholder);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                new EmptyBorder(10, 12, 10, 12)
        ));

        field.setEchoChar((char) 0);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new PlaceholderFocus(field, placeholder));
        return field;
    }
}
