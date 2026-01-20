package secsys.views.platforms;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PlatformsUpdatePanel extends JPanel {

    private Image background;

    private JComboBox<String> cmbPlatform;
    private JTextField txtSearchKey;
    private JTextField txtUsers;
    private JTextField txtCostPerUser;
    private JTextField txtCostAnnual;

    public PlatformsUpdatePanel() {

        // ===== IMAGEN DE FONDO =====
        background = new ImageIcon(
                "src\\secsys\\resources\\imagenFondo.png"
        ).getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(860, 560));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Actualizar Plataforma");
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

        txtSearchKey = new JTextField(20);
        txtUsers = new JTextField(20);
        txtCostPerUser = new JTextField(20);
        txtCostAnnual = new JTextField(20);

        addField(form, c, y++, "Plataforma:", cmbPlatform);
        addField(form, c, y++, "Tipo licenciamiento (USA) / URL (LATAM):", txtSearchKey);
        addField(form, c, y++, "Número de usuarios (USA):", txtUsers);
        addField(form, c, y++, "Costo anual por usuario (USA):", txtCostPerUser);
        addField(form, c, y++, "Costo anual (LATAM):", txtCostAnnual);

        // ===== COMPORTAMIENTO DINÁMICO =====
        cmbPlatform.addActionListener(e -> updateFormByPlatform());
        updateFormByPlatform();

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        CustomButton btnUpdate = new CustomButton("Actualizar", "#4A90E2");

        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("platforms");
        });

        btnUpdate.addActionListener(e -> {
            new SuccessMessageFrame("Plataforma Actualizada Correctamente").setVisible(true);
            resetForm();
            ViewRouter.show("dashboard");
        });

        buttons.add(btnBack);
        buttons.add(btnUpdate);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    // ===== HABILITACIÓN POR PLATAFORMA =====
    private void updateFormByPlatform() {

        boolean isUSA = cmbPlatform.getSelectedIndex() == 0;

        txtUsers.setEnabled(isUSA);
        txtCostPerUser.setEnabled(isUSA);

        txtCostAnnual.setEnabled(!isUSA);

        if (isUSA) {
            txtCostAnnual.setText("");
        } else {
            txtUsers.setText("");
            txtCostPerUser.setText("");
        }
    }

    // ===== RESET =====
    private void resetForm() {
        cmbPlatform.setSelectedIndex(0);
        txtSearchKey.setText("");
        txtUsers.setText("");
        txtCostPerUser.setText("");
        txtCostAnnual.setText("");
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
