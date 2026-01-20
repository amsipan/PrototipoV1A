package secsys.views.admin;

import secsys.router.ViewRouter;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RequiredFieldsMessageFrame;

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

    public AdminCreateUserPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

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

        int y = 0;

        txtID = new JTextField(20);
        txtFullName = new JTextField(20);
        txtUsername = new JTextField(20);
        txtEmail = new JTextField(20);

        cmbRole = new JComboBox<>(new String[]{
                "Seleccione",
                "Administrador",
                "Gerente",
                "Presidente",
                "Empleado Operativo"
        });

        addField(form, c, y++, "Cédula de identidad:", txtID);
        addField(form, c, y++, "Nombre completo:", txtFullName);
        addField(form, c, y++, "Nombre de usuario:", txtUsername);
        addField(form, c, y++, "Correo institucional:", txtEmail);
        addField(form, c, y++, "Rol asignado:", cmbRole);

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Crear usuario", "#4A90E2");

        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("admin");
        });

        btnSave.addActionListener(e -> {
            
            if (!validateForm()) {
                    new RequiredFieldsMessageFrame(
                            "Debe completar todos los campos antes de continuar."
                    ).setVisible(true);
                    return;
                }
            
                new SuccessMessageFrame("Usuario registrado correctamente.").setVisible(true);
                ViewRouter.show("dashboard");
        });


        buttons.add(btnBack);
        buttons.add(btnSave);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    // ===== VALIDACIÓN =====
    private boolean validateForm() {

        if (txtID.getText().trim().isEmpty()) return false;
        if (txtFullName.getText().trim().isEmpty()) return false;
        if (txtUsername.getText().trim().isEmpty()) return false;
        if (txtEmail.getText().trim().isEmpty()) return false;

        if (cmbRole.getSelectedIndex() == 0) return false;

        return true;
    }

    // ===== RESET =====
    private void resetForm() {
        txtID.setText("");
        txtFullName.setText("");
        txtUsername.setText("");
        txtEmail.setText("");
        cmbRole.setSelectedIndex(0);
    }

    private void addField(JPanel panel, GridBagConstraints c,
                          int y, String label, JComponent field) {

        c.gridx = 0;
        c.gridy = y;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        panel.add(field, c);
    }

    // ===== FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
