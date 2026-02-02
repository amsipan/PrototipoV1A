package secsys.views.finance;

import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;
import secsys.repository.QuotationRepository;
import secsys.repository.QuotationRepository.QuoteRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

public class QuotationUpdatePanel extends JPanel {

    private Image background;

    private JTextField txtRucSearch;
    private JPanel listContainer;

    private final QuotationRepository repo = new QuotationRepository();

    public QuotationUpdatePanel() {
        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(980, 640));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        JLabel title = new JLabel("Modificar cotizaciones (Borrador / Revisión)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbl = new JLabel("RUC potencial (13 dígitos):");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        txtRucSearch = new JTextField(16);

        CustomButton btnSearch = new CustomButton("Buscar", "#4A90E2");
        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");

        c.gridx = 0; c.gridy = 0; c.weightx = 0.25;
        top.add(lbl, c);

        c.gridx = 1; c.weightx = 0.45;
        top.add(txtRucSearch, c);

        c.gridx = 2; c.weightx = 0.15;
        top.add(btnSearch, c);

        c.gridx = 3; c.weightx = 0.15;
        top.add(btnBack, c);

        listContainer = new JPanel();
        listContainer.setOpaque(false);
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));

        JScrollPane scroll = new JScrollPane(listContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(top, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(wrapper, BorderLayout.CENTER);

        add(card);

        btnBack.addActionListener(e -> ViewRouter.show("finance"));
        btnSearch.addActionListener(e -> loadQuotes());
        txtRucSearch.addActionListener(e -> loadQuotes());

        renderEmpty("Ingrese un RUC y presione Buscar.");
    }

    private void loadQuotes() {
        String ruc = safe(txtRucSearch);
        if (!ruc.matches("^\\d{13}$")) {
            ActionMessageFrame.showMsg("Dato inválido", "RUC inválido (debe tener 13 dígitos).");
            return;
        }

        try {
            List<QuoteRow> rows = repo.listEditableByRuc(ruc);
            if (rows.isEmpty()) {
                renderEmpty("No hay cotizaciones en Borrador o Revisión para ese RUC.");
                return;
            }
            renderCards(rows);

        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo cargar cotizaciones. Revise consola.");
            renderEmpty("Error al cargar cotizaciones.");
        }
    }

    private void renderEmpty(String msg) {
        listContainer.removeAll();

        RoundedPanel p = new RoundedPanel(18);
        p.setBackground(new Color(245, 245, 245));
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(16, 16, 16, 16));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel l = new JLabel(msg);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(l, BorderLayout.CENTER);

        listContainer.add(p);
        listContainer.add(Box.createVerticalStrut(10));

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void renderCards(List<QuoteRow> rows) {
        listContainer.removeAll();
        for (QuoteRow r : rows) {
            listContainer.add(new QuoteCard(r));
            listContainer.add(Box.createVerticalStrut(12));
        }
        listContainer.revalidate();
        listContainer.repaint();
    }

    private final class QuoteCard extends RoundedPanel {
        private final QuoteRow row;

        QuoteCard(QuoteRow row) {
            super(20);
            this.row = row;

            setBackground(new Color(250, 250, 250));
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(14, 16, 14, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

            JLabel header = new JLabel("Cotización #" + row.numero + " | Estado: " + nn(row.estado) + " | RUC: " + nn(row.rucPotencial));
            header.setFont(new Font("Segoe UI", Font.BOLD, 13));

            JTextArea body = new JTextArea();
            body.setEditable(false);
            body.setLineWrap(true);
            body.setWrapStyleWord(true);
            body.setOpaque(false);
            body.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            body.setText(
                    "Empresa: " + nn(row.nombreEmpresa) + "\n" +
                    "Descripción servicio: " + nn(row.descripcionServicio) + "\n" +
                    "Descuento: " + row.descuentoTotal + "%\n" +
                    "Subtotal sin IVA: " + money(row.subtotalSinIva) +
                    " | IVA: " + money(row.ivaValor) +
                    " | Total: " + money(row.total) + "\n" +
                    "Actualizado: " + nn(row.actualizadoEn)
            );

            CustomButton btnModify = new CustomButton("Modificar", "#4A90E2");
            btnModify.addActionListener(e -> openEditDialog(row));

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            bottom.setOpaque(false);
            bottom.add(btnModify);

            add(header, BorderLayout.NORTH);
            add(body, BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);
        }
    }

    private void openEditDialog(QuoteRow row) {
        if (row == null || row.cotizacionId == null) {
            ActionMessageFrame.showMsg("Error", "Cotización inválida.");
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Modificar cotización", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBorder(new EmptyBorder(14, 14, 14, 14));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        JTextArea txtNewDesc = new JTextArea(4, 24);
        txtNewDesc.setLineWrap(true);
        txtNewDesc.setWrapStyleWord(true);
        txtNewDesc.setText(row.descripcionServicio == null ? "" : row.descripcionServicio);

        JTextField txtNewDiscount = new JTextField(String.valueOf(row.descuentoTotal), 8);

        JLabel hint = new JLabel("Descripción máx 250 (letras/números y . , # -). Descuento 0..99.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(new Color(90, 90, 90));

        c.gridx = 0; c.gridy = 0;
        content.add(new JLabel("Nueva descripción del servicio:"), c);

        c.gridy = 1;
        content.add(new JScrollPane(txtNewDesc), c);

        c.gridy = 2;
        content.add(new JLabel("Nuevo descuento (%):"), c);

        c.gridy = 3;
        content.add(txtNewDiscount, c);

        c.gridy = 4;
        content.add(hint, c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
        CustomButton btnApply = new CustomButton("Aplicar cambios", "#4A90E2");

        btnCancel.addActionListener(e -> dlg.dispose());

        btnApply.addActionListener(e -> {
            String ruc = safe(txtRucSearch);

            if (!ruc.matches("^\\d{13}$")) {
                ActionMessageFrame.showMsg(this,"Error", "RUC inválido (13 dígitos).");
                return;
            }

            // ===== rcot5v1.1: descripción =====
            String newDesc = safeArea(txtNewDesc);

            if (newDesc.length() < 3) {
                ActionMessageFrame.showMsg(this,"Descripción inválida", "La descripción debe tener al menos 3 caracteres.");
                return;
            }
            if (newDesc.length() > 250) {
                ActionMessageFrame.showMsg(this,"Descripción inválida", "La descripción no puede exceder 250 caracteres.");
                return;
            }
            if (!newDesc.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 .,#\\-]{3,250}$")) {
                ActionMessageFrame.showMsg(this,"Descripción inválida", "Solo letras/números y símbolos . , # -");
                return;
            }


            // ===== rcot7.1v1.0: descuento =====
            String discRaw = safe(txtNewDiscount);
            if (discRaw.isBlank() || !discRaw.matches("^\\d{1,2}$")) {
                ActionMessageFrame.showMsg(this,"Descuento inválido", "Descuento inválido");
                return;
            }
            int disc = Integer.parseInt(discRaw);
            if (disc < 0) {
                ActionMessageFrame.showMsg(this,"Descuento inválido", "El descuento debe ser positivo");
                return;
            }
            if (disc > 99) {
                ActionMessageFrame.showMsg(this,"Descuento inválido", "Descuento inválido");
                return;
            }

            try {
                // 1) Descripción
                if (row.detalleId != null) {
                    boolean okDesc = repo.updateServiceDescription(ruc, row.detalleId, newDesc);
                    if (!okDesc) {
                        ActionMessageFrame.showMsg(this,"No permitido", "Solo se puede modificar si está en Borrador o Revisión.");
                        return;
                    }
                }

                // 2) Descuento + totales
                boolean okDisc = repo.updateDiscountAndTotals(ruc, row.cotizacionId, disc);


                if (!okDisc) {
                    ActionMessageFrame.showMsg("No permitido", "Solo se puede modificar si está en Borrador o Revisión.");
                    return;
                }

                new SuccessMessageFrame("Descripción actualizada / Descuento actualizado").setVisible(true);
                dlg.dispose();
                loadQuotes();

            } catch (IllegalArgumentException iae) {
                // Mensajes de negocio del repo
                String m = iae.getMessage() == null ? "Datos inválidos" : iae.getMessage();
                if ("El descuento debe ser positivo".equalsIgnoreCase(m)) {
                    ActionMessageFrame.showMsg(this,"Descuento inválido", "El descuento debe ser positivo");
                } else if ("Descuento inválido".equalsIgnoreCase(m)) {
                    ActionMessageFrame.showMsg(this,"Descuento inválido", "Descuento inválido");
                } else {
                    ActionMessageFrame.showMsg("Error", m);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                ActionMessageFrame.showMsg("Error", "No se pudo aplicar cambios. Revise consola.");
            }
        });

        actions.add(btnCancel);
        actions.add(btnApply);

        dlg.add(content, BorderLayout.CENTER);
        dlg.add(actions, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private static String safe(JTextField t) {
        String s = t.getText();
        return s == null ? "" : s.trim();
    }

    private static String safeArea(JTextArea t) {
        String s = t.getText();
        return s == null ? "" : s.trim();
    }

    private static String nn(String s) {
        return s == null ? "-" : s;
    }

    private static String money(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
