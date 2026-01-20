package secsys.views.platforms;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlatformsRegisterPanel extends JPanel {

    private Image background;

    private JTextField txtLicense;
    private JTextField txtUsers;
    private JTextField txtCostPerUser;
    private JTextField txtCostAnnual;
    private JTextField txtURL;

    private JComboBox<String> cmbPlatform;

    public PlatformsRegisterPanel() {

        // ===== IMAGEN DE FONDO =====
        background = new ImageIcon(
                "src\\secsys\\resources\\imagenFondo.png"
        ).getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(860, 560));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Registrar Plataforma");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        cmbPlatform = new JComboBox<>(new String[]{
                "KnowBe4 (USA)",
                "SMARTFENSE (LATAM)"
        });

        txtLicense = new JTextField(20);
        txtUsers = new JTextField(20);
        txtCostPerUser = new JTextField(20);
        txtCostAnnual = new JTextField(20);
        txtURL = new JTextField(20);

        addField(form, c, y++, "Plataforma:", cmbPlatform);
        addField(form, c, y++, "Tipo de licenciamiento:", txtLicense);
        addField(form, c, y++, "Número de usuarios (USA):", txtUsers);
        addField(form, c, y++, "Costo anual por usuario (USA):", txtCostPerUser);
        addField(form, c, y++, "Costo anual total (LATAM):", txtCostAnnual);
        addField(form, c, y++, "URL (LATAM):", txtURL);

        // ===== COMPORTAMIENTO DINÁMICO =====
        cmbPlatform.addActionListener(e -> updateFormByPlatform());
        updateFormByPlatform();

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Registrar plataforma", "#4A90E2");

        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("platforms");
        });

        btnSave.addActionListener(e -> {
                new SuccessMessageFrame("Plataforma registrada correctamente").setVisible(true);
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

    // ===== LÓGICA DE HABILITACIÓN =====
    private void updateFormByPlatform() {

        boolean isUSA = cmbPlatform.getSelectedIndex() == 0;

        txtUsers.setEnabled(isUSA);
        txtCostPerUser.setEnabled(isUSA);

        txtCostAnnual.setEnabled(!isUSA);
        txtURL.setEnabled(!isUSA);

        if (isUSA) {
            txtCostAnnual.setText("");
            txtURL.setText("");
        } else {
            txtUsers.setText("");
            txtCostPerUser.setText("");
        }
    }

    // ===== RESET =====
    private void resetForm() {
        cmbPlatform.setSelectedIndex(0);
        txtLicense.setText("");
        txtUsers.setText("");
        txtCostPerUser.setText("");
        txtCostAnnual.setText("");
        txtURL.setText("");
        updateFormByPlatform();
    }

    private void addField(JPanel panel, GridBagConstraints c,
                          int y, String label, JComponent field) {

        c.gridx = 0;
        c.gridy = y;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        panel.add(field, c);
    }

    // ===== DIBUJO DEL FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
