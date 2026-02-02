package secsys.views.finance;

import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;
import secsys.repository.PlatformLicensingRepository;
import secsys.repository.PlatformLicensingRepository.PlatformLicenseRow;
import secsys.repository.QuotationRepository;
import secsys.repository.QuotationRepository.CreateQuoteRequest;
import secsys.repository.SystemSettingsRepository;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class QuotationCreatePanel extends JPanel {

    private Image background;

    private JTextField txtRuc;
    private JTextField txtCompany;

    private JTextField txtGenDate;
    private JTextField txtExpDate;

    private JComboBox<String> cmbServiceType;

    private JComboBox<PlatformLicenseRow> cmbPlatform;
    private JLabel lblPlatform;

    private JTextArea txtDescription;

    private JTextField txtUnitPrice;
    private JTextField txtManagementPrice;
    private JTextField txtQuantity;

    private JTextField txtSubtotalService;
    private JTextField txtDiscount;
    private JTextField txtSubtotalNoIva;
    private JTextField txtIvaValue;
    private JTextField txtTotal;

    private final PlatformLicensingRepository platformRepo = new PlatformLicensingRepository();
    private final SystemSettingsRepository settingsRepo = new SystemSettingsRepository();
    private final QuotationRepository quoteRepo = new QuotationRepository();

    private BigDecimal cachedIvaRate = BigDecimal.ZERO; // 0..1
    private boolean ivaWarnShown = false;

    private boolean updatingUI = false;
    private boolean recomputeScheduled = false;

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public QuotationCreatePanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(920, 640));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new javax.swing.border.EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Generar Cotización");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new javax.swing.border.EmptyBorder(0, 0, 10, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        int y = 0;

        txtRuc = new JTextField(15);
        txtCompany = new JTextField(22);

        txtGenDate = new JTextField(10);
        txtExpDate = new JTextField(10);
        txtExpDate.setEditable(false);
        txtExpDate.setEnabled(false);
        txtExpDate.setDisabledTextColor(new Color(60, 60, 60));

        cmbServiceType = new JComboBox<>(new String[]{
                "Seleccione",
                "Evento en la empresa",
                "Auditoria",
                "Plataforma KB4",
                "Plataforma SMF",
                "Plataforma KB4 y gestion",
                "Plataforma SMF y gestion"
        });

        lblPlatform = new JLabel("Licenciamiento:");
        cmbPlatform = new JComboBox<>();
        cmbPlatform.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PlatformLicenseRow) {
                    PlatformLicenseRow r = (PlatformLicenseRow) value;
                    String code = (r.plataformaCodigo == null) ? "-" : r.plataformaCodigo.trim().toUpperCase();
                    String name = (r.nombreLicenciamiento == null) ? "-" : r.nombreLicenciamiento;
                    setText(code + " | " + name);
                } else {
                    setText("-");
                }
                return this;
            }
        });

        txtDescription = new JTextArea(3, 22);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        txtUnitPrice = new JTextField(10);
        txtManagementPrice = new JTextField(10);
        txtQuantity = new JTextField(10);

        txtSubtotalService = buildReadOnlyMoneyField();
        txtSubtotalNoIva = buildReadOnlyMoneyField();
        txtIvaValue = buildReadOnlyMoneyField();
        txtTotal = buildReadOnlyMoneyField();

        txtDiscount = new JTextField(6);

        addField(form, c, y++, "RUC:", txtRuc);
        addField(form, c, y++, "Razón social:", txtCompany);
        addField(form, c, y++, "Fecha generación:", txtGenDate);
        addField(form, c, y++, "Fecha vencimiento:", txtExpDate);

        addField(form, c, y++, "Tipo de servicio:", cmbServiceType);
        addField(form, c, y++, lblPlatform, cmbPlatform);

        addField(form, c, y++, "Descripción del servicio (0-250):", new JScrollPane(txtDescription));

        addField(form, c, y++, "Cantidad / duración (Meses) / usuarios:", txtQuantity);
        addField(form, c, y++, "Costo del servicio (precio base):", txtUnitPrice);
        addField(form, c, y++, "Precio de gestión:", txtManagementPrice);

        addField(form, c, y++, "Subtotal por servicio:", txtSubtotalService);
        addField(form, c, y++, "Descuento (%):", txtDiscount);
        addField(form, c, y++, "Subtotal sin IVA:", txtSubtotalNoIva);
        addField(form, c, y++, "IVA:", txtIvaValue);
        addField(form, c, y++, "Total:", txtTotal);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Guardar cotización", "#4A90E2");
        CustomButton btnClear = new CustomButton("Limpiar", "#9E9E9E");

        btnBack.addActionListener(e -> {
            resetForm(true);
            ViewRouter.show("finance");
        });

        btnClear.addActionListener(e -> resetForm(false));
        btnSave.addActionListener(e -> onSave());

        buttons.add(btnClear);
        buttons.add(btnBack);
        buttons.add(btnSave);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);

        txtGenDate.setText(LocalDate.now().format(DF));
        autoSetExpFromGen();

        cmbServiceType.addActionListener(e -> onServiceChanged());
        cmbPlatform.addActionListener(e -> scheduleRecompute());

        bindRecalc(txtGenDate);
        bindRecalc(txtCompany);
        bindRecalc(txtRuc);
        bindRecalc(txtQuantity);
        bindRecalc(txtUnitPrice);
        bindRecalc(txtManagementPrice);
        bindRecalc(txtDiscount);

        refreshIvaRateCache(true);
        onServiceChanged();
        scheduleRecompute();
    }

    private void onSave() {
        String msg = validateFormMessage();
        if (msg != null) {
            ActionMessageFrame.showMsg("Verifique los datos", msg);
            return;
        }

        try {
            // Construir request para repository
            CreateQuoteRequest req = new CreateQuoteRequest();
            req.rucPotencial = safeText(txtRuc);
            req.nombreEmpresa = safeText(txtCompany);
            req.fechaGeneracion = parseDateOrNull(safeText(txtGenDate));
            req.vigenciaHasta = parseDateOrNull(safeText(txtExpDate));
            req.estado = "Borrador";
            req.descuentoPct = safeInt(safeText(txtDiscount), 0);

            req.servicio = String.valueOf(cmbServiceType.getSelectedItem());
            req.descripcion = safeText(txtDescription);

            req.cantidad = safeInt(safeText(txtQuantity), 1);
            req.precioUnitario = parseMoneyAny(safeText(txtUnitPrice));
            if (req.precioUnitario == null) req.precioUnitario = BigDecimal.ZERO;

            boolean hasGestion = req.servicio != null && req.servicio.toLowerCase().contains("gestion");
            req.incluirGestion = hasGestion;
            req.precioGestion = parseMoneyAny(safeText(txtManagementPrice));

            quoteRepo.createQuotation(req);

            new SuccessMessageFrame("Cotización registrada correctamente.").setVisible(true);
            resetForm(true);
            ViewRouter.show("finance");

        } catch (IllegalArgumentException iae) {
            ActionMessageFrame.showMsg("Error", iae.getMessage() == null ? "Datos inválidos" : iae.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo guardar la cotización. Revise consola.");
        }
    }

    private void onServiceChanged() {
        String type = String.valueOf(cmbServiceType.getSelectedItem());
        if (type == null) type = "";

        boolean isEventOrAudit = "Evento en la empresa".equalsIgnoreCase(type) || "Auditoria".equalsIgnoreCase(type);
        boolean isKB4 = type.toLowerCase().contains("kb4");
        boolean isSMF = type.toLowerCase().contains("smf");
        boolean hasGestion = type.toLowerCase().contains("gestion");
        boolean usesPlatform = isKB4 || isSMF;

        lblPlatform.setVisible(usesPlatform);
        cmbPlatform.setVisible(usesPlatform);
        lblPlatform.setEnabled(usesPlatform);
        cmbPlatform.setEnabled(usesPlatform);

        txtUnitPrice.setEnabled(true);
        txtUnitPrice.setEditable(isEventOrAudit);

        txtManagementPrice.setEnabled(hasGestion);
        txtManagementPrice.setEditable(hasGestion);
        if (!hasGestion) safeSetText(txtManagementPrice, "");

        if (usesPlatform) {
            if (isSMF) {
                safeSetText(txtQuantity, "1");
                txtQuantity.setEnabled(false);
                txtQuantity.setEditable(false);
            } else {
                txtQuantity.setEnabled(false);
                txtQuantity.setEditable(false);
            }
        } else {
            txtQuantity.setEnabled(true);
            txtQuantity.setEditable(true);
        }

        if (usesPlatform) {
            loadPlatforms(isKB4 ? "KB4" : "SMF");
        } else {
            cmbPlatform.removeAllItems();
        }
        if (!hasGestion) safeSetText(txtManagementPrice, "");


        refreshIvaRateCache(false);
        scheduleRecompute();

        revalidate();
        repaint();
    }

    private void loadPlatforms(String platformCode) {
        cmbPlatform.removeAllItems();
        try {
            List<PlatformLicenseRow> rows = platformRepo.listByPlatformCode(platformCode);
            if (rows == null || rows.isEmpty()) return;
            for (PlatformLicenseRow r : rows) cmbPlatform.addItem(r);
            cmbPlatform.setSelectedIndex(0);
        } catch (Exception ex) {
            System.out.println("[QUOTE-CREATE] Error cargando licenciamientos:");
            ex.printStackTrace();
        }
    }

    private void refreshIvaRateCache(boolean showPopupIfFail) {
        try {
            BigDecimal iva = settingsRepo.getIvaRate();
            if (iva == null) {
                cachedIvaRate = BigDecimal.ZERO;
                warnIvaOnce(showPopupIfFail);
                return;
            }
            cachedIvaRate = iva;
        } catch (Exception ex) {
            cachedIvaRate = BigDecimal.ZERO;
            warnIvaOnce(showPopupIfFail);
        }
    }

    private void warnIvaOnce(boolean showPopupIfFail) {
        if (!showPopupIfFail) return;
        if (ivaWarnShown) return;
        ivaWarnShown = true;
        ActionMessageFrame.showMsg(
                "IVA no configurado",
                "No se pudo obtener la tasa de IVA.\nSe usará IVA = 0.00 hasta que sea configurado."
        );
    }

    private String validateFormMessage() {
        String ruc = safeText(txtRuc);
        if (ruc.isBlank() || !ruc.matches("^\\d{13}$"))
            return "RUC inválido debe tener 13 dígitos.";
        if (!isValidEcuadorRuc(ruc))
            return "RUC inválido (verifique el número).";


        String company = safeText(txtCompany);
        if (company.isBlank() || company.length() < 3 || company.length() > 100)
            return "Razón social inválido (3 a 100 caracteres).";
        if (!company.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 .,#\\-]{3,100}$"))
            return "Razón social empresa inválido. Solo letras, números y símbolos . , # -";

        LocalDate gen = parseDateOrNull(safeText(txtGenDate));
        if (gen == null) return "Fecha de generación inválida. Use DD/MM/AAAA.";

        LocalDate exp = parseDateOrNull(safeText(txtExpDate));
        if (exp == null) return "Fecha de vencimiento inválida.";
        if (exp.isBefore(gen)) return "La fecha de vencimiento no puede ser menor a la fecha de generación.";

        String type = String.valueOf(cmbServiceType.getSelectedItem());
        if (type == null || "Seleccione".equalsIgnoreCase(type.trim()))
            return "Debe seleccionar un tipo de servicio válido.";

        String desc = safeText(txtDescription);

        if (desc.length() < 3) return "La descripción debe tener al menos 3 caracteres.";
        if (desc.length() > 250) return "La descripción no puede exceder 250 caracteres.";
        if (!desc.matches("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 .,#\\-]{3,250}$"))
            return "Descripción inválida. Solo letras, números y símbolos . , # -";


        boolean isKB4 = type.toLowerCase().contains("kb4");
        boolean isSMF = type.toLowerCase().contains("smf");
        boolean usesPlatform = isKB4 || isSMF;

        if (usesPlatform && cmbPlatform.getSelectedItem() == null)
            return "Debe seleccionar un licenciamiento registrado para la plataforma.";

        boolean hasGestion = type.toLowerCase().contains("gestion");

if (hasGestion) {
    String mg = safeText(txtManagementPrice);

    // NO vacío
    if (mg.isBlank()) return "El precio de gestión es obligatorio.";

    // Formato válido y NO negativo
    BigDecimal g = parseMoneyAny(mg);
    if (g == null) return "Precio de gestión inválido. Use un valor como 100 o 100.50.";
    if (g.compareTo(BigDecimal.ZERO) < 0) return "El precio de gestión no puede ser negativo.";
}


        String q = safeText(txtQuantity);
        if (q.isBlank() || !q.matches("^\\d{1,4}$")) return "Cantidad inválida (hasta 4 dígitos).";
        int qi = Integer.parseInt(q);
        if (qi <= 0) return "La cantidad debe ser un valor positivo.";

        String d = safeText(txtDiscount);
        if (d.isBlank() || !d.matches("^\\d{1,2}$")) return "Descuento inválido (0 a 99).";

        return null;
    }

    private void scheduleRecompute() {
        if (recomputeScheduled) return;
        recomputeScheduled = true;
        SwingUtilities.invokeLater(() -> {
            recomputeScheduled = false;
            recomputeAll();
        });
    }

    private void recomputeAll() {
        updatingUI = true;
        try {
            autoSetExpFromGen();

            BigDecimal ivaRate = (cachedIvaRate == null) ? BigDecimal.ZERO : cachedIvaRate;

            String type = String.valueOf(cmbServiceType.getSelectedItem());
            if (type == null) type = "";

            boolean isEventOrAudit = "Evento en la empresa".equalsIgnoreCase(type) || "Auditoria".equalsIgnoreCase(type);
            boolean isKB4 = type.toLowerCase().contains("kb4");
            boolean isSMF = type.toLowerCase().contains("smf");
            boolean usesPlatform = isKB4 || isSMF;
            boolean hasGestion = type.toLowerCase().contains("gestion");

            BigDecimal basePrice = BigDecimal.ZERO;

            if (isEventOrAudit) {
                BigDecimal p = parseMoneyAny(safeText(txtUnitPrice));
                basePrice = (p == null) ? BigDecimal.ZERO : p;

            } else if (usesPlatform) {
                Object sel = cmbPlatform.getSelectedItem();
                if (sel instanceof PlatformLicenseRow) {
                    PlatformLicenseRow r = (PlatformLicenseRow) sel;

                    if (isKB4) {
                        Integer users = r.numeroUsuarios;
                        if (users == null || users <= 0) users = 1;

                        safeSetText(txtQuantity, String.valueOf(users));
                        txtQuantity.setEnabled(false);
                        txtQuantity.setEditable(false);

                        BigDecimal cpu = safeBD(r.costoAnualPorUsuario);
                        basePrice = cpu.multiply(new BigDecimal(users));
                    } else {
                        safeSetText(txtQuantity, "1");
                        txtQuantity.setEnabled(false);
                        txtQuantity.setEditable(false);

                        basePrice = safeBD(r.costoAnualTotal);
                    }
                }

                safeSetText(txtUnitPrice, formatMoneyPlain(basePrice));
                txtUnitPrice.setEditable(false);
            }

            BigDecimal gestion = BigDecimal.ZERO;
            if (hasGestion) {
                BigDecimal g = parseMoneyAny(safeText(txtManagementPrice));
                gestion = (g == null) ? BigDecimal.ZERO : g;
            }

            BigDecimal subtotalServicio = basePrice.add(gestion);

            int discountPct = safeInt(safeText(txtDiscount), 0);
            if (discountPct < 0) discountPct = 0;
            if (discountPct > 99) discountPct = 99;

            BigDecimal factor = BigDecimal.ONE.subtract(
                    new BigDecimal(discountPct).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
            );

            BigDecimal subtotalNoIva = subtotalServicio.multiply(factor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal iva = subtotalNoIva.multiply(ivaRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotalNoIva.add(iva).setScale(2, RoundingMode.HALF_UP);

            safeSetText(txtSubtotalService, formatMoneyPlain(subtotalServicio));
            safeSetText(txtSubtotalNoIva, formatMoneyPlain(subtotalNoIva));

            String ivaPct = ivaRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).toPlainString();
            safeSetText(txtIvaValue, ivaPct + "% = " + formatMoneyPlain(iva));

            safeSetText(txtTotal, formatMoneyPlain(total));

        } finally {
            updatingUI = false;
        }
    }

    private void autoSetExpFromGen() {
        LocalDate gen = parseDateOrNull(safeText(txtGenDate));
        if (gen == null) return;
        LocalDate exp = gen.plusDays(30);
        safeSetText(txtExpDate, exp.format(DF));
    }

    private static JTextField buildReadOnlyMoneyField() {
        JTextField t = new JTextField(10);
        t.setEditable(false);
        t.setEnabled(false);
        t.setDisabledTextColor(new Color(60, 60, 60));
        return t;
    }

    private void resetForm(boolean keepToday) {
        updatingUI = true;
        try {
            txtRuc.setText("");
            txtCompany.setText("");
            txtGenDate.setText(keepToday ? LocalDate.now().format(DF) : "");
            txtExpDate.setText("");

            cmbServiceType.setSelectedIndex(0);
            cmbPlatform.removeAllItems();

            txtDescription.setText("");

            txtQuantity.setText("");
            txtQuantity.setEnabled(true);
            txtQuantity.setEditable(true);

            txtUnitPrice.setText("");
            txtUnitPrice.setEnabled(true);
            txtUnitPrice.setEditable(true);

            txtManagementPrice.setText("");
            txtDiscount.setText("");

            txtSubtotalService.setText("");
            txtSubtotalNoIva.setText("");
            txtIvaValue.setText("");
            txtTotal.setText("");

            refreshIvaRateCache(false);
            onServiceChanged();
            scheduleRecompute();
        } finally {
            updatingUI = false;
        }
    }

    private void bindRecalc(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { if (!updatingUI) scheduleRecompute(); }
            @Override public void removeUpdate(DocumentEvent e) { if (!updatingUI) scheduleRecompute(); }
            @Override public void changedUpdate(DocumentEvent e) { if (!updatingUI) scheduleRecompute(); }
        });
    }

    private static void addField(JPanel panel, GridBagConstraints c, int y, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0.35;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 0.65;
        panel.add(field, c);
    }

    private static void addField(JPanel panel, GridBagConstraints c, int y, JComponent label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0.35;
        panel.add(label, c);

        c.gridx = 1;
        c.weightx = 0.65;
        panel.add(field, c);
    }

    private static String safeText(JTextComponent t) {
        String s = t.getText();
        return s == null ? "" : s.trim();
    }

    private static LocalDate parseDateOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return LocalDate.parse(s.trim(), DF);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static BigDecimal parseMoneyAny(String raw) {
        if (raw == null) return null;
        String v = raw.trim().replace(",", ".");
        if (v.isBlank()) return null;
        if (!v.matches("^\\d{1,9}(\\.\\d{1,2})?$")) return null;
        try {
            return new BigDecimal(v);
        } catch (Exception e) {
            return null;
        }
    }

    private static int safeInt(String s, int def) {
        try {
            if (s == null || s.isBlank()) return def;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static BigDecimal safeBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static String formatMoneyPlain(BigDecimal v) {
        if (v == null) v = BigDecimal.ZERO;
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void safeSetText(JTextField t, String value) {
        if (t == null) return;
        String cur = t.getText();
        String val = (value == null) ? "" : value;
        if (!val.equals(cur)) t.setText(val);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }

    // =====================================================
// ✅ Validación completa de RUC ecuatoriano (13 dígitos)
// =====================================================
private static boolean isValidEcuadorRuc(String ruc) {
    if (ruc == null) return false;
    String s = ruc.trim();
    if (!s.matches("^\\d{13}$")) return false;

    int prov = Integer.parseInt(s.substring(0, 2));
    if (prov < 1 || prov > 24) return false;

    int third = s.charAt(2) - '0';

    // sufijo
    int suffix3 = Integer.parseInt(s.substring(10, 13));

    // Persona natural (0..5): valida cédula + sufijo 001..999
    if (third >= 0 && third <= 5) {
        if (suffix3 < 1 || suffix3 > 999) return false;
        return isValidCedulaModulo10(s.substring(0, 10));
    }

    // Pública (6): módulo 11, verificador pos 8, sufijo 0001..9999
    if (third == 6) {
        int suffix4 = Integer.parseInt(s.substring(9, 13));
        if (suffix4 < 1 || suffix4 > 9999) return false;
        return isValidRucPublicoModulo11(s);
    }

    // Privada (9): módulo 11, verificador pos 9, sufijo 001..999
    if (third == 9) {
        if (suffix3 < 1 || suffix3 > 999) return false;
        return isValidRucPrivadoModulo11(s);
    }

    return false;
}

// Cédula / persona natural: módulo 10 (dígito verificador es el 10mo)
private static boolean isValidCedulaModulo10(String ced) {
    if (ced == null || !ced.matches("^\\d{10}$")) return false;

    int prov = Integer.parseInt(ced.substring(0, 2));
    if (prov < 1 || prov > 24) return false;

    int third = ced.charAt(2) - '0';
    if (third < 0 || third > 5) return false;

    int sum = 0;
    for (int i = 0; i < 9; i++) {
        int d = ced.charAt(i) - '0';
        if (i % 2 == 0) { // pos 0,2,4,6,8 *2
            d *= 2;
            if (d > 9) d -= 9;
        }
        sum += d;
    }

    int mod = sum % 10;
    int check = (mod == 0) ? 0 : (10 - mod);

    int last = ced.charAt(9) - '0';
    return check == last;
}

// RUC privado (3er dígito 9): módulo 11, verificador en posición 9
// coef: 4,3,2,7,6,5,4,3,2 sobre dígitos 0..8
private static boolean isValidRucPrivadoModulo11(String ruc) {
    int[] coef = {4, 3, 2, 7, 6, 5, 4, 3, 2};
    int sum = 0;
    for (int i = 0; i < 9; i++) {
        int d = ruc.charAt(i) - '0';
        sum += d * coef[i];
    }
    int mod = sum % 11;
    int check = 11 - mod;
    if (check == 11) check = 0;
    if (check == 10) return false;

    int verifier = ruc.charAt(9) - '0';
    return verifier == check;
}

// RUC público (3er dígito 6): módulo 11, verificador en posición 8
// coef: 3,2,7,6,5,4,3,2 sobre dígitos 0..7
private static boolean isValidRucPublicoModulo11(String ruc) {
    int[] coef = {3, 2, 7, 6, 5, 4, 3, 2};
    int sum = 0;
    for (int i = 0; i < 8; i++) {
        int d = ruc.charAt(i) - '0';
        sum += d * coef[i];
    }
    int mod = sum % 11;
    int check = 11 - mod;
    if (check == 11) check = 0;
    if (check == 10) return false;

    int verifier = ruc.charAt(8) - '0';
    return verifier == check;
}

}
