package secsys.views.admin;

import secsys.router.ViewRouter;
import secsys.repository.SettingsRepository;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.ConfirmDialogFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class AdminSettingsPanel extends JPanel {

    // IVA
    private JTextField txtIva;

    // Moneda
    private JComboBox<String> cmbMoneda;

    // Plantilla PDF
    private JTextField txtDocId;
    private JTextField txtPdfPath;
    private File selectedPdf;

    // Backup
    private JComboBox<String> cmbFrecuencia;

    private final SettingsRepository repo = new SettingsRepository();

    // Placeholders (valores actuales BD)
    private String phIva = "";
    private String phDocId = "";

    private boolean ivaIsPlaceholder = false;
    private boolean docIdIsPlaceholder = false;

    // UI constants
    private static final int FIELD_H = 34;
    private static final int BTN_H = 40;
    private static final int LABEL_W = 160;

    // Colores placeholder
    private static final Color PLACEHOLDER_COLOR = new Color(140, 140, 140);
    private static final Color NORMAL_COLOR = new Color(30, 30, 30);

    public AdminSettingsPanel() {

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(Color.WHITE);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("Configuración del Sistema");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));

        // ===== CONTENIDO (SCROLL) =====
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(0, 0, 10, 0));

        content.add(sectionIva());
        content.add(Box.createVerticalStrut(14));
        content.add(sectionMoneda());
        content.add(Box.createVerticalStrut(14));
        content.add(sectionPlantillaPdf());
        content.add(Box.createVerticalStrut(14));
        content.add(sectionBackups());
        content.add(Box.createVerticalStrut(20));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        // ===== BOTONERA FIJA ABAJO =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 0, 0, 0));

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.setPreferredSize(new Dimension(180, 44));
        btnBack.addActionListener(e -> {
            // ✅ Al salir: limpiar + placeholders con valores BD
            resetAndReload();
            ViewRouter.show("admin");
        });

        footer.add(btnBack);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        // ✅ Cargar valores y poner placeholders
        reloadPlaceholdersFromDb();
        applyPlaceholdersToEmptyFields();
        loadCombosFromDb();
    }

    // =========================================================
    // SECCIONES UI
    // =========================================================

    private JComponent sectionIva() {
        RoundedPanel box = baseBox("IVA");

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = gcBase();
        c.gridy = 0;

        JLabel l1 = new JLabel("Tasa de IVA:");
        l1.setPreferredSize(new Dimension(LABEL_W, FIELD_H));
        grid.add(l1, c);

        txtIva = new JTextField();
        txtIva.setPreferredSize(new Dimension(420, FIELD_H));
        txtIva.setToolTipText("Formato: 00.00");
        c.gridx = 1;
        c.weightx = 1;
        grid.add(txtIva, c);

        // ✅ Placeholder IVA basado en BD
        installPlaceholder(txtIva,
                () -> phIva,
                b -> ivaIsPlaceholder = b,
                () -> ivaIsPlaceholder
        );

        CustomButton btnSave = new CustomButton("Guardar IVA", "#4A90E2");
        btnSave.setPreferredSize(new Dimension(180, BTN_H));
        btnSave.addActionListener(e -> onSaveIva());

        c.gridx = 2;
        c.weightx = 0;
        c.insets = new Insets(6, 12, 6, 6);
        grid.add(btnSave, c);

        box.add(grid, BorderLayout.CENTER);
        return box;
    }

    private JComponent sectionMoneda() {
        RoundedPanel box = baseBox("Moneda por defecto");

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = gcBase();
        c.gridy = 0;

        JLabel l1 = new JLabel("Moneda:");
        l1.setPreferredSize(new Dimension(LABEL_W, FIELD_H));
        grid.add(l1, c);

        // ✅ +10 monedas (más de 7)
        cmbMoneda = new JComboBox<>(new String[]{
                "Seleccione",
                "USD - Dólar estadounidense",
                "EUR - Euro",
                "GBP - Libra esterlina",
                "JPY - Yen japonés",
                "CHF - Franco suizo",
                "CAD - Dólar canadiense",
                "AUD - Dólar australiano",
                "CNY - Yuan chino",
                "INR - Rupia india",
                "BRL - Real brasileño",
                "MXN - Peso mexicano",
                "SEK - Corona sueca",
                "NOK - Corona noruega",
                "DKK - Corona danesa"
        });
        cmbMoneda.setPreferredSize(new Dimension(420, FIELD_H));
        c.gridx = 1;
        c.weightx = 1;
        grid.add(cmbMoneda, c);

        CustomButton btnSave = new CustomButton("Guardar moneda", "#4A90E2");
        btnSave.setPreferredSize(new Dimension(180, BTN_H));
        btnSave.addActionListener(e -> onSaveMoneda());

        c.gridx = 2;
        c.weightx = 0;
        c.insets = new Insets(6, 12, 6, 6);
        grid.add(btnSave, c);

        box.add(grid, BorderLayout.CENTER);
        return box;
    }

    private JComponent sectionPlantillaPdf() {
        RoundedPanel box = baseBox("Cabecera documental PDF");

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = gcBase();

        // Row 0: DocId
        c.gridy = 0;

        JLabel lDoc = new JLabel("ID tipo documento:");
        lDoc.setPreferredSize(new Dimension(LABEL_W, FIELD_H));
        c.gridx = 0;
        c.weightx = 0;
        grid.add(lDoc, c);

        txtDocId = new JTextField();
        txtDocId.setPreferredSize(new Dimension(640, FIELD_H));
        txtDocId.setToolTipText("Formato: doc.XX");
        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        grid.add(txtDocId, c);

        // ✅ Placeholder docId basado en BD (si existe un valor guardado)
        installPlaceholder(txtDocId,
                () -> phDocId,
                b -> docIdIsPlaceholder = b,
                () -> docIdIsPlaceholder
        );

        // Row 1: PDF path + pick
        c.gridy = 1;
        c.gridwidth = 1;

        JLabel lPdf = new JLabel("Archivo PDF:");
        lPdf.setPreferredSize(new Dimension(LABEL_W, FIELD_H));
        c.gridx = 0;
        c.weightx = 0;
        grid.add(lPdf, c);

        txtPdfPath = new JTextField();
        txtPdfPath.setEditable(false);
        txtPdfPath.setPreferredSize(new Dimension(520, FIELD_H));
        c.gridx = 1;
        c.weightx = 1;
        grid.add(txtPdfPath, c);

        CustomButton btnPick = new CustomButton("Seleccionar PDF", "#5DA9E9");
        btnPick.setPreferredSize(new Dimension(180, BTN_H));
        btnPick.addActionListener(e -> pickPdf());

        c.gridx = 2;
        c.weightx = 0;
        c.insets = new Insets(6, 12, 6, 6);
        grid.add(btnPick, c);

        // Row 2: Configurar plantilla
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(12, 6, 0, 6);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;

        CustomButton btnSave = new CustomButton("Configurar plantilla", "#4A90E2");
        btnSave.setPreferredSize(new Dimension(520, 44));
        btnSave.addActionListener(e -> onSavePdfTemplate());
        grid.add(btnSave, c);

        box.add(grid, BorderLayout.CENTER);
        return box;
    }

    private JComponent sectionBackups() {
        RoundedPanel box = baseBox("Respaldos");

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints c = gcBase();

        // Row 0
        c.gridy = 0;

        JLabel l1 = new JLabel("Frecuencia:");
        l1.setPreferredSize(new Dimension(LABEL_W, FIELD_H));
        c.gridx = 0;
        c.weightx = 0;
        grid.add(l1, c);

        cmbFrecuencia = new JComboBox<>(new String[]{
                "Seleccione",
                "Diaria", "Semanal", "Mensual", "Trimestral", "Semestral", "Anual"
        });
        cmbFrecuencia.setPreferredSize(new Dimension(420, FIELD_H));
        c.gridx = 1;
        c.weightx = 1;
        grid.add(cmbFrecuencia, c);

        CustomButton btnSaveSchedule = new CustomButton("Guardar programación", "#4A90E2");
        btnSaveSchedule.setPreferredSize(new Dimension(220, BTN_H));
        btnSaveSchedule.addActionListener(e -> onSaveBackupSchedule());

        c.gridx = 2;
        c.weightx = 0;
        c.insets = new Insets(6, 12, 6, 6);
        grid.add(btnSaveSchedule, c);

        // Row 1: respaldo manual
        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 3;
        c.insets = new Insets(12, 6, 0, 6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;

        CustomButton btnManual = new CustomButton("Ejecutar respaldo manual", "#E53935");
        btnManual.setPreferredSize(new Dimension(260, 44));
        btnManual.addActionListener(e -> onRunManualBackup());
        grid.add(btnManual, c);

        box.add(grid, BorderLayout.CENTER);
        return box;
    }

    private RoundedPanel baseBox(String title) {
        RoundedPanel box = new RoundedPanel(18);
        box.setBackground(new Color(245, 247, 250));
        box.setLayout(new BorderLayout());
        box.setBorder(new EmptyBorder(16, 16, 16, 16));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        box.add(lbl, BorderLayout.NORTH);

        return box;
    }

    private GridBagConstraints gcBase() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.weightx = 0;
        return c;
    }

    // =========================================================
    // ACCIONES
    // =========================================================

    private void onSaveIva() {
        String iva = getRealText(txtIva, () -> ivaIsPlaceholder).trim();

        if (!iva.matches("^\\d{2}\\.\\d{2}$")) {
            ActionMessageFrame.showMsg("Valor de IVA inválido", "Ingrese un valor con formato 00.00.");
            return;
        }

        double v;
        try { v = Double.parseDouble(iva); }
        catch (Exception ex) {
            ActionMessageFrame.showMsg("Valor de IVA inválido", "Ingrese un valor numérico válido (00.00).");
            return;
        }

        if (v < 0.00 || v > 99.99) {
            ActionMessageFrame.showMsg("Valor de IVA inválido", "El valor debe estar entre 00.00 y 99.99.");
            return;
        }

        try {
            repo.upsertParam("fin.iva", iva);
            new SuccessMessageFrame("Tasa de IVA modificada correctamente").setVisible(true);

            // ✅ refrescar placeholders con lo guardado
            reloadPlaceholdersFromDb();
            applyPlaceholdersToEmptyFields();

        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo guardar la tasa de IVA.");
        }
    }

    private void onSaveMoneda() {
        if (cmbMoneda.getSelectedIndex() == 0) {
            ActionMessageFrame.showMsg("Seleccione una moneda valida", "Seleccione una moneda valida.");
            return;
        }

        String sel = String.valueOf(cmbMoneda.getSelectedItem());
        String code = sel.split(" - ")[0].trim();

        try {
            repo.upsertParam("fin.moneda_default", code);
            new SuccessMessageFrame("Moneda por defecto modificada correctamente").setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo guardar la moneda por defecto.");
        }
    }

    private void pickPdf() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Seleccionar PDF de cabecera");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int r = fc.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        if (f == null) return;

        selectedPdf = f;
        txtPdfPath.setText(f.getAbsolutePath());
    }

    private void onSavePdfTemplate() {
        String docId = getRealText(txtDocId, () -> docIdIsPlaceholder).trim();
        if (!docId.matches("^doc\\.\\d+$")) {
            ActionMessageFrame.showMsg("Campos obligatorios", "El identificador debe tener formato doc.XX");
            return;
        }

        if (selectedPdf == null || !selectedPdf.exists()) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Seleccione un archivo .pdf.");
            return;
        }

        String err = validatePdfNoLibs(selectedPdf);
        if (err != null) {
            ActionMessageFrame.showMsg("Error", err);
            return;
        }

        boolean ok = ConfirmDialogFrame.showConfirm(
                "Confirmar configuración",
                "¿Desea configurar esta cabecera para " + docId + "?"
        );
        if (!ok) return;

        try {
            byte[] pdfBytes = readAllBytes(selectedPdf);
            repo.upsertPdfTemplate(docId, selectedPdf.getName(), pdfBytes);
            new SuccessMessageFrame("Plantilla configurada correctamente").setVisible(true);

            // ✅ limpiar selección PDF
            selectedPdf = null;
            txtPdfPath.setText("");

        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo configurar la plantilla.");
        }
    }

    private void onSaveBackupSchedule() {
        if (cmbFrecuencia.getSelectedIndex() == 0) {
            ActionMessageFrame.showMsg("Seleccione un valor valido", "Seleccione un valor valido");
            return;
        }

        String freq = String.valueOf(cmbFrecuencia.getSelectedItem()).trim();
        if (!isAllowedFreq(freq)) {
            ActionMessageFrame.showMsg("Seleccione un valor valido", "Seleccione un valor valido");
            return;
        }

        try {
            repo.saveBackupFrequency(freq);
            new SuccessMessageFrame("Respaldo automático programado").setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo guardar la programación de respaldos.");
        }
    }

    private void onRunManualBackup() {
        boolean ok = ConfirmDialogFrame.showConfirm(
                "Confirmar respaldo",
                "¿Está seguro de que desea ejecutar el respaldo manual?"
        );
        if (!ok) return;

        try {
            repo.logBackupExecution("MANUAL", "OK", "Solicitud de respaldo manual registrada.");
            new SuccessMessageFrame("Respaldo ejecutado correctamente").setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            try { repo.logBackupExecution("MANUAL", "ERROR", "Error registrando respaldo manual."); }
            catch (Exception ignore) {}
            ActionMessageFrame.showMsg("Error", "Error durante el respaldo");
        }
    }

    // =========================================================
    // RESET / PLACEHOLDERS (SALIR)
    // =========================================================

    private void resetAndReload() {
        // limpiar inputs
        txtIva.setText("");
        txtDocId.setText("");
        txtPdfPath.setText("");
        selectedPdf = null;

        cmbMoneda.setSelectedIndex(0);
        cmbFrecuencia.setSelectedIndex(0);

        // recargar valores actuales de BD
        reloadPlaceholdersFromDb();
        applyPlaceholdersToEmptyFields();
        loadCombosFromDb();
    }

    private void reloadPlaceholdersFromDb() {
        try {
            String iva = repo.getParam("fin.iva");
            phIva = (iva == null || iva.isBlank()) ? "" : iva.trim();
        } catch (Exception e) {
            phIva = "";
        }

        // DocId: si manejas un “último docId usado” en BD, lo tomas.
        // Si no existe ese parámetro, lo dejamos vacío.
        try {
            String lastDoc = repo.getParam("doc.pdf_last_id");
            phDocId = (lastDoc == null || lastDoc.isBlank()) ? "" : lastDoc.trim();
        } catch (Exception e) {
            phDocId = "";
        }
    }

    private void applyPlaceholdersToEmptyFields() {
        // IVA
        if (txtIva != null) {
            if (txtIva.getText().trim().isEmpty() && phIva != null && !phIva.isBlank()) {
                setPlaceholder(txtIva, phIva);
                ivaIsPlaceholder = true;
            } else {
                ivaIsPlaceholder = false;
                txtIva.setForeground(NORMAL_COLOR);
            }
        }

        // DocId
        if (txtDocId != null) {
            if (txtDocId.getText().trim().isEmpty() && phDocId != null && !phDocId.isBlank()) {
                setPlaceholder(txtDocId, phDocId);
                docIdIsPlaceholder = true;
            } else {
                docIdIsPlaceholder = false;
                txtDocId.setForeground(NORMAL_COLOR);
            }
        }
    }

    private void loadCombosFromDb() {
        try {
            String mon = repo.getParam("fin.moneda_default");
            if (mon != null) {
                for (int i = 1; i < cmbMoneda.getItemCount(); i++) {
                    String it = String.valueOf(cmbMoneda.getItemAt(i));
                    if (it.startsWith(mon + " ")) {
                        cmbMoneda.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ignore) {}

        try {
            String freq = repo.getBackupFrequency();
            if (freq != null) {
                for (int i = 1; i < cmbFrecuencia.getItemCount(); i++) {
                    String it = String.valueOf(cmbFrecuencia.getItemAt(i));
                    if (it.equalsIgnoreCase(freq)) {
                        cmbFrecuencia.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    // =========================================================
    // PLACEHOLDER UTILS
    // =========================================================

    private interface Getter { String get(); }
    private interface BoolSetter { void set(boolean v); }
    private interface BoolGetter { boolean get(); }

    private void installPlaceholder(JTextField field, Getter placeholderGetter, BoolSetter setFlag, BoolGetter isFlag) {
        field.setForeground(NORMAL_COLOR);

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isFlag.get()) {
                    field.setText("");
                    field.setForeground(NORMAL_COLOR);
                    setFlag.set(false);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                String cur = field.getText() == null ? "" : field.getText().trim();
                String ph = placeholderGetter.get();
                if (cur.isEmpty() && ph != null && !ph.isBlank()) {
                    setPlaceholder(field, ph);
                    setFlag.set(true);
                }
            }
        });
    }

    private void setPlaceholder(JTextField field, String placeholder) {
        field.setText(placeholder);
        field.setForeground(PLACEHOLDER_COLOR);
    }

    private String getRealText(JTextField field, BoolGetter isPlaceholderFlag) {
        if (field == null) return "";
        if (isPlaceholderFlag != null && isPlaceholderFlag.get()) return "";
        return field.getText() == null ? "" : field.getText();
    }

    // =========================================================
    // VALIDACIÓN PDF SIN LIBS
    // =========================================================

    private String validatePdfNoLibs(File f) {
        String name = f.getName() == null ? "" : f.getName().toLowerCase();
        if (!name.endsWith(".pdf")) return "Validación fallida: el archivo debe ser .pdf.";
        if (f.length() <= 0) return "Validación fallida: el archivo está vacío.";

        byte[] head;
        try { head = readUpTo(f, 2 * 1024 * 1024); }
        catch (Exception ex) { return "Validación fallida: no se pudo leer el archivo."; }

        if (head.length < 5) return "Validación fallida: estructura de PDF no válida.";
        String header = new String(head, 0, Math.min(head.length, 20), StandardCharsets.ISO_8859_1);
        if (!header.startsWith("%PDF-")) return "Validación fallida: estructura de PDF no válida.";

        String text = new String(head, StandardCharsets.ISO_8859_1);

        boolean hasEOF = text.contains("%%EOF");
        if (!hasEOF) {
            try {
                String tail = readTailAsText(f, 64 * 1024);
                hasEOF = tail.contains("%%EOF");
            } catch (Exception ignore) {}
        }
        if (!hasEOF) return "Validación fallida: estructura de PDF no válida (sin EOF).";

        String low = text.toLowerCase();
        if (low.contains("/encrypt") || low.contains("/filter/standard") || low.contains("/filter /standard")) {
            return "Validación fallida: el PDF está protegido.";
        }

        int pages = countRegex(text, "/Type\\s*/Page\\b") - countRegex(text, "/Type\\s*/Pages\\b");
        if (pages < 0) pages = 0;
        if (pages > 1) return "Validación fallida: el PDF debe contener máximo 1 página.";

        return null;
    }

    private static int countRegex(String text, String regex) {
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(text);
            int c = 0;
            while (m.find()) c++;
            return c;
        } catch (Exception e) {
            return 0;
        }
    }

    private static byte[] readAllBytes(File f) throws IOException {
        try (InputStream in = new FileInputStream(f);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = in.read(buf)) != -1) out.write(buf, 0, r);
            return out.toByteArray();
        }
    }

    private static byte[] readUpTo(File f, int maxBytes) throws IOException {
        try (InputStream in = new FileInputStream(f);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            byte[] buf = new byte[8192];
            int total = 0, r;
            while ((r = in.read(buf)) != -1) {
                int can = Math.min(r, maxBytes - total);
                if (can > 0) out.write(buf, 0, can);
                total += can;
                if (total >= maxBytes) break;
            }
            return out.toByteArray();
        }
    }

    private static String readTailAsText(File f, int tailBytes) throws IOException {
        long len = f.length();
        long start = Math.max(0, len - tailBytes);
        try (RandomAccessFile raf = new RandomAccessFile(f, "r")) {
            raf.seek(start);
            byte[] b = new byte[(int) (len - start)];
            raf.readFully(b);
            return new String(b, StandardCharsets.ISO_8859_1);
        }
    }

    private static boolean isAllowedFreq(String v) {
        return "Diaria".equalsIgnoreCase(v)
                || "Semanal".equalsIgnoreCase(v)
                || "Mensual".equalsIgnoreCase(v)
                || "Trimestral".equalsIgnoreCase(v)
                || "Semestral".equalsIgnoreCase(v)
                || "Anual".equalsIgnoreCase(v);
    }
}
