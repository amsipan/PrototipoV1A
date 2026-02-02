package secsys.views.platforms;

import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.ConfirmDialogFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import secsys.repository.PlatformLicensingRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class PlatformsRegisterPanel extends JPanel {

    private Image background;

    private JTextField txtLicense;

    // KnowBe4 (USA)
    private JTextField txtUsers;
    private JTextField txtCostPerUser;

    // Smartfense (LATAM)
    private JTextField txtCostAnnual;
    private JTextField txtURL;

    private JComboBox<String> cmbPlatform;

    // Repo externo (BD)
    private final PlatformLicensingRepository repo = new PlatformLicensingRepository();

    public PlatformsRegisterPanel() {

        // ===== IMAGEN DE FONDO =====
        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

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

        // ===== FORM =====
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

        txtLicense = new JTextField(22);

        txtUsers = new JTextField(22);
        txtCostPerUser = new JTextField(22);

        txtCostAnnual = new JTextField(22);
        txtURL = new JTextField(22);

        addField(form, c, y++, "Plataforma:", cmbPlatform);
        addField(form, c, y++, "Nombre de licenciamiento:", txtLicense);
        addField(form, c, y++, "Número de usuarios:", txtUsers);
        addField(form, c, y++, "Costo anual por usuario:", txtCostPerUser);
        addField(form, c, y++, "Costo anual total:", txtCostAnnual);
        addField(form, c, y++, "URL:", txtURL);

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
        boolean isUSA = cmbPlatform.getSelectedIndex() == 0;

        String license = text(txtLicense);

        // Validación común: 3..100, alfabeto americano (A-Z y espacios)
        if (!isValidLicenseName(license)) {
            ActionMessageFrame.showMsg(
                    "Datos inválidos o incompletos",
                    "El nombre del licenciamiento debe tener al menor 3 caracteres."
            );
            return;
        }

        boolean confirm = ConfirmDialogFrame.showConfirm(
                "Confirmar registro",
                "¿Desea registrar el licenciamiento para " + (isUSA ? "KnowBe4" : "SMARTFENSE") + "?"
        );
        if (!confirm) return;

        try {
            if (isUSA) {
                String usersStr = text(txtUsers);
                String costStr = text(txtCostPerUser);

                // usuarios: positivo hasta 4 cifras
                if (!usersStr.matches("^[1-9]\\d{0,3}$")) {
                    ActionMessageFrame.showMsg("Datos inválidos o incompletos", "Número de usuarios inválido.");
                    return;
                }

                // costo por usuario: 2 enteros + 2 decimales
                if (!costStr.matches("^\\d{1,2}\\.\\d{2}$")) {
                    ActionMessageFrame.showMsg("Datos inválidos o incompletos", "Costo anual por usuario inválido.");
                    return;
                }

                int users = Integer.parseInt(usersStr);
                BigDecimal costPerUser = new BigDecimal(costStr);

                if (users <= 0 || costPerUser.compareTo(BigDecimal.ZERO) <= 0) {
                    ActionMessageFrame.showMsg("Datos inválidos o incompletos", "Los valores deben ser positivos.");
                    return;
                }

                repo.insertKnowBe4(license, users, costPerUser);

            } else {
                String annualStr = text(txtCostAnnual);
                String url = text(txtURL);

                // costo anual total: hasta 3 enteros + 2 decimales
                if (!annualStr.matches("^\\d{1,3}\\.\\d{2}$")) {
                    ActionMessageFrame.showMsg("Datos inválidos o incompletos", "Costo anual total inválido.");
                    return;
                }

                if (!isValidUrl(url)) {
                    ActionMessageFrame.showMsg("Datos inválidos o incompletos", "URL inválida. Debe seguir el formato https://dominio.tld");
                    return;
                }

                BigDecimal annualTotal = new BigDecimal(annualStr);
                if (annualTotal.compareTo(BigDecimal.ZERO) <= 0) {
                    ActionMessageFrame.showMsg("Datos inválidos o incompletos", "El costo anual total debe ser positivo.");
                    return;
                }

                repo.insertSmartfense(license, annualTotal, url);
            }

            new SuccessMessageFrame("Plataforma registrada exitosamente").setVisible(true);
            resetForm();
            ViewRouter.show("platforms");

        } catch (PlatformLicensingRepository.DuplicatePlatformIdException dup) {
            ActionMessageFrame.showMsg("Identificador de plataforma duplicado", "Identificador de plataforma duplicado");
        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo registrar la plataforma.");
        }
    }

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

    private void resetForm() {
        cmbPlatform.setSelectedIndex(0);
        txtLicense.setText("");
        txtUsers.setText("");
        txtCostPerUser.setText("");
        txtCostAnnual.setText("");
        txtURL.setText("");
        updateFormByPlatform();
    }

    private void addField(JPanel panel, GridBagConstraints c, int y, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0.30;

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(l, c);

        c.gridx = 1;
        c.weightx = 0.70;
        panel.add(field, c);
    }

    private static String text(JTextField t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    // “alfabeto americano”: letras A-Z y espacios (sin tildes)
    private static boolean isValidLicenseName(String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.length() < 3 || v.length() > 100) return false;
        return v.matches("^[A-Za-z ]+$");
    }

    private static boolean isValidUrl(String url) {
        if (url == null) return false;
        String u = url.trim();
        return u.matches("^https://[A-Za-z0-9.-]+\\.[A-Za-z]{2,}(/.*)?$");
    }

    // ===== DIBUJO DEL FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
