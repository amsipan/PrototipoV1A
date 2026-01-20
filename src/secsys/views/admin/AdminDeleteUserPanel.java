package secsys.views.admin;

import secsys.router.ViewRouter;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;
import secsys.views.addons.CustomButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDeleteUserPanel extends JPanel {

    private Image background;

    private JTextField txtCedula;
    private JTextField txtFullName;
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JComboBox<String> cmbRole;


    private CustomButton btnDelete;

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
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        searchPanel.setOpaque(false);

        txtCedula = new JTextField(15);
        JButton btnSearch = new JButton("Buscar");

        searchPanel.add(new JLabel("Número de cédula:"));
        searchPanel.add(txtCedula);
        searchPanel.add(btnSearch);

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        txtFullName = new JTextField(20);
        txtUsername = new JTextField(20);
        txtEmail = new JTextField(20);

        cmbRole = new JComboBox<>(new String[]{
                "Administrador",
                "Gerente",
                "Presidente",
                "Empleado Operativo"
        });



        addField(form, c, y++, "Nombre completo:", txtFullName);
        addField(form, c, y++, "Nombre de usuario:", txtUsername);
        addField(form, c, y++, "Correo institucional:", txtEmail);
        addField(form, c, y++, "Rol:", cmbRole);
 

        // Todos los campos deshabilitados (solo lectura)
        setFormEnabled(false);

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        btnDelete = new CustomButton("Eliminar usuario", "#E53935");
        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");

        btnDelete.setEnabled(false);

        btnSearch.addActionListener(e -> loadMockUser());

        btnDelete.addActionListener(e -> confirmDelete());

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

    // ===== CARGA SIMULADA DEL USUARIO =====
    private void loadMockUser() {

        txtFullName.setText("María López");
        txtUsername.setText("mlopez");
        txtEmail.setText("maria.lopez@segadvice.com");

        cmbRole.setSelectedItem("Empleado Operativo");


        txtCedula.setEnabled(false);

        // Habilitar botón eliminar
        btnDelete.setEnabled(true);
    }

    // ===== CONFIRMACIÓN =====
    private void confirmDelete() {

        int result = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea eliminar este usuario?\nEsta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            new SuccessMessageFrame("Usuario Eliminado Correctamente").setVisible(true);
            resetForm();
            ViewRouter.show("dashboard");
        }
    }

    // ===== HABILITAR / DESHABILITAR CAMPOS =====
    private void setFormEnabled(boolean enabled) {
        txtFullName.setEnabled(enabled);
        txtUsername.setEnabled(enabled);
        txtEmail.setEnabled(enabled);
        cmbRole.setEnabled(enabled);

    }

    // ===== RESET VISUAL =====
    private void resetForm() {

        txtCedula.setText("");
        txtCedula.setEnabled(true);

        txtFullName.setText("");
        txtUsername.setText("");
        txtEmail.setText("");

        cmbRole.setSelectedIndex(0);


        btnDelete.setEnabled(false);
        setFormEnabled(false);
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
