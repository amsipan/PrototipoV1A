package secsys.views.finance;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RequiredFieldsMessageFrame;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class QuotationCreatePanel extends JPanel {

    private Image background;

    // ===== CAMPOS =====
    private JTextField txtRuc;
    private JTextField txtCompany;
    private JTextField txtDate;
    private JTextArea txtServices;
    private JTextArea txtDescription;
    private JTextField txtUnitPrice;
    private JTextField txtQuantity;
    private JTextField txtSubtotalService;
    private JTextField txtDiscount;
    private JTextField txtSubtotal;
    private JTextField txtIVA;
    private JTextField txtTotal;
    private JTextField txtValidity;

    public QuotationCreatePanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(900, 620));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Generar Cotización");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        txtRuc = new JTextField(15);
        txtCompany = new JTextField(20);
        txtDate = new JTextField();
        txtServices = new JTextArea(3, 20);
        txtDescription = new JTextArea(3, 20);
        txtUnitPrice = new JTextField(10);
        txtQuantity = new JTextField(10);
        txtSubtotalService = new JTextField(10);
        txtDiscount = new JTextField(10);
        txtSubtotal = new JTextField(10);
        txtIVA = new JTextField(10);
        txtTotal = new JTextField(10);
        txtValidity = new JTextField();

        addField(form, c, y++, "RUC:", txtRuc);
        addField(form, c, y++, "Nombre empresa:", txtCompany);
        addField(form, c, y++, "Fecha generación:", txtDate);
        addField(form, c, y++, "Servicios cotizados:", new JScrollPane(txtServices));
        addField(form, c, y++, "Descripción del servicio:", new JScrollPane(txtDescription));
        addField(form, c, y++, "Precio unitario:", txtUnitPrice);
        addField(form, c, y++, "Cantidad / duración:", txtQuantity);
        addField(form, c, y++, "Subtotal por servicio:", txtSubtotalService);
        addField(form, c, y++, "Descuentos:", txtDiscount);
        addField(form, c, y++, "Subtotal sin IVA:", txtSubtotal);
        addField(form, c, y++, "IVA:", txtIVA);
        addField(form, c, y++, "Total:", txtTotal);
        addField(form, c, y++, "Vigencia de cotización:", txtValidity);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);

        // ===== BOTÓN TÉRMINOS =====
        CustomButton btnTerms = new CustomButton("T&C", "#5DA9E9");
        btnTerms.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Aquí se abriría el archivo de términos y condiciones.",
                        "Prototipo",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Guardar cotización", "#4A90E2");

        btnBack.addActionListener(e -> ViewRouter.show("dashboard"));

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

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JPanel termsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        termsPanel.setOpaque(false);
        termsPanel.add(btnTerms);

        footer.add(termsPanel, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        add(card);
    }

    // ===== VALIDACIÓN =====
    private boolean validateForm() {

        if (txtRuc.getText().trim().isEmpty()) return false;
        if (txtCompany.getText().trim().isEmpty()) return false;
        if (txtDate.getText().trim().isEmpty()) return false;
        if (txtServices.getText().trim().isEmpty()) return false;
        if (txtDescription.getText().trim().isEmpty()) return false;
        if (txtUnitPrice.getText().trim().isEmpty()) return false;
        if (txtQuantity.getText().trim().isEmpty()) return false;
        if (txtSubtotalService.getText().trim().isEmpty()) return false;
        if (txtDiscount.getText().trim().isEmpty()) return false;
        if (txtSubtotal.getText().trim().isEmpty()) return false;
        if (txtIVA.getText().trim().isEmpty()) return false;
        if (txtTotal.getText().trim().isEmpty()) return false;
        if (txtValidity.getText().trim().isEmpty()) return false;

        return true;
    }

    private void addField(JPanel panel, GridBagConstraints c,
                          int y, String label, JComponent field) {

        c.gridx = 0;
        c.gridy = y;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        panel.add(field, c);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
