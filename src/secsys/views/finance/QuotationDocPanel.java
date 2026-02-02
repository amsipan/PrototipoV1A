package secsys.views.finance;

import secsys.config.DbConfig;
import secsys.db.DbConnection;
import secsys.repository.QuotationDocRepository;
import secsys.repository.SystemSettingsRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;

// PDFBox 3.x (SOLO si dejas pdfbox-app-3.0.6.jar)
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class QuotationDocPanel extends JPanel {

    // ===== SMTP =====
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SMTP_USER = "ultimadmo@gmail.com";
    private static final String SMTP_PASS = "rkat avmx gbrn jyqu";

    // UI
    private Image background;

    private JTextField txtRuc;
    private JTextField txtEmail;

    private CustomButton btnGenerarPdf;
    private CustomButton btnEnviarEmail;
    private CustomButton btnVolver;

    // Repos (mismo estilo del proyecto)
    private final QuotationDocRepository repo;
    private final SystemSettingsRepository sysRepo;

    public QuotationDocPanel() {
        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        DbConfig cfg = DbConfig.fromEnv();
        DbConnection db = new DbConnection(cfg);

        this.repo = new QuotationDocRepository(db);
        this.sysRepo = new SystemSettingsRepository();

        buildUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        setOpaque(false);

        // CARD (blanca) centrada
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(Color.WHITE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(22, 26, 22, 26));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;

        JLabel title = new JLabel("Documento de Cotización");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 6, 0));
        card.add(title, c);

        // RUC
        c.gridy++;
        JLabel lblRuc = new JLabel("RUC del potencial cliente");
        lblRuc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(lblRuc, c);

        c.gridy++;
        txtRuc = new JTextField();
        txtRuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRuc.setToolTipText("Ej: 1790012345001");
        card.add(txtRuc, c);

        // Botón generar PDF
        c.gridy++;
        btnGenerarPdf = new CustomButton("Generar PDF (Descargar)", "#4A90E2");
        btnGenerarPdf.addActionListener(e -> onGeneratePdf());
        card.add(btnGenerarPdf, c);

        // Separador
        c.gridy++;
        card.add(new JSeparator(), c);

        // Email
        c.gridy++;
        JLabel lblEmail = new JLabel("Correo electrónico de destino");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        card.add(lblEmail, c);

        c.gridy++;
        txtEmail = new JTextField();
        txtEmail.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtEmail.setToolTipText("Permite: _, &, $, #, . (además de @)");
        card.add(txtEmail, c);

        // Botón enviar correo
        c.gridy++;
        btnEnviarEmail = new CustomButton("Enviar por correo", "#4A90E2");
        btnEnviarEmail.addActionListener(e -> onSendEmail());
        card.add(btnEnviarEmail, c);

        // ✅ Botón VOLVER
        c.gridy++;
        btnVolver = new CustomButton("Volver", "#9E9E9E");
        btnVolver.addActionListener(e -> ViewRouter.show("finance"));
        card.add(btnVolver, c);

        // Poner card centrada
        GridBagConstraints wrap = new GridBagConstraints();
        wrap.insets = new Insets(20, 20, 20, 20);
        wrap.anchor = GridBagConstraints.CENTER;
        wrap.fill = GridBagConstraints.NONE;
        add(card, wrap);
    }

    // ===================== rcot10v1.1 =====================
    private void onGeneratePdf() {
        try {
            String ruc = parseRuc();
            if (ruc == null) return;

            byte[] pdfBytes = buildQuotationPdfBytes(ruc);

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Guardar cotización PDF");
            fc.setSelectedFile(new File("cotizacion_" + ruc + ".pdf"));

            int res = fc.showSaveDialog(this);
            if (res != JFileChooser.APPROVE_OPTION) return;

            File file = fc.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".pdf")) {
                file = new File(file.getAbsolutePath() + ".pdf");
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBytes);
            }

            ActionMessageFrame.showMsg(this, "Información", "Cotización descargada correctamente.");

        } catch (IllegalStateException ex) {
            ActionMessageFrame.showMsg(this, "Error", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            ActionMessageFrame.showMsg(this, "Error", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg(this, "Error", "Error al generar la cotización.");
        }
    }

    // ===================== rcot11v1.1 =====================
    private void onSendEmail() {
        try {
            String ruc = parseRuc();
            if (ruc == null) return;

            String email = (txtEmail.getText() == null) ? "" : txtEmail.getText().trim();

            if (!isValidEmailRestricted(email)) {
                ActionMessageFrame.showMsg(this, "Error",
                        "Correo inválido. Debe tener 10 a 30 caracteres.");
                return;
            }

            byte[] pdfBytes = buildQuotationPdfBytes(ruc);

            sendMailWithAttachment(email, pdfBytes, "cotizacion.pdf");

            try {
                QuotationDocRepository.QuotationHeaderDTO q = repo.findLatestQuotationByRuc(ruc);
                if (q != null && q.cotizacionId != null) {
                    repo.logEmailSentSafe(q.cotizacionId, ruc, email);
                }
            } catch (Exception ignore) {}

            ActionMessageFrame.showMsg(this, "Información", "Notificación enviada.");

        } catch (IllegalStateException ex) {
            ActionMessageFrame.showMsg(this, "Error", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            ActionMessageFrame.showMsg(this, "Error", ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            ActionMessageFrame.showMsg(this, "Error", "Error al enviar notificación: " + ex.getMessage());
        }
    }

    // ===================== Construcción PDF =====================
    private byte[] buildQuotationPdfBytes(String ruc) throws Exception {

        // 1) Obtener doc_id cabecera desde BD (NO quemado)
        String cabeceraDocId = sysRepo.getPdfHeaderDocId();
        if (cabeceraDocId == null || cabeceraDocId.isBlank()) {
            throw new IllegalStateException("No existe cabecera documental configurada.");
        }

        // 2) Cabecera desde tabla plantilla
        QuotationDocRepository.PdfTemplateDTO header = repo.findHeaderTemplateByDocId(cabeceraDocId);
        if (header == null || header.archivoPdf == null || header.archivoPdf.length == 0) {
            throw new IllegalStateException("No existe cabecera documental configurada.");
        }

        // 3) Cotización (última) por RUC
        QuotationDocRepository.QuotationHeaderDTO q = repo.findLatestQuotationByRuc(ruc);
        if (q == null) {
            throw new IllegalArgumentException("Potencial cliente no encontrado");
        }

        List<QuotationDocRepository.QuotationDetailDTO> details = repo.listDetails(q.cotizacionId);

        // 4) 1 sola hoja: usar la cabecera como fondo y escribir encima
        try (PDDocument headerDoc = Loader.loadPDF(header.archivoPdf);
             PDDocument out = new PDDocument()) {

            PDFMergerUtility merger = new PDFMergerUtility();
            merger.appendDocument(out, headerDoc);

            PDPage contentPage = out.getPage(0);

            PDFont fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            try (PDPageContentStream cs = new PDPageContentStream(
                    out,
                    contentPage,
                    PDPageContentStream.AppendMode.APPEND,
                    true,
                    true
            )) {

                float margin = 55;
                float y = 520;

                y = writeLine(cs, fontBold, 14, margin, y, "Cotización");
                y -= 4;

                y = writeLine(cs, fontRegular, 11, margin, y, "N°: " + safe(q.numero));
                y = writeLine(cs, fontRegular, 11, margin, y, "Empresa: " + safe(q.nombreEmpresa));
                y = writeLine(cs, fontRegular, 11, margin, y, "RUC: " + safe(q.rucPotencial));
                y = writeLine(cs, fontRegular, 11, margin, y, "Estado: " + estadoUi(safe(q.estado)));
                y = writeLine(cs, fontRegular, 11, margin, y, "Actualizado: " + (q.actualizadoEn == null ? "" :
                        q.actualizadoEn.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

                y -= 10;

                y = drawDetailTableOnePage(cs, fontBold, fontRegular, margin, y, details);

                y -= 10;

                y = writeLine(cs, fontBold, 12, margin, y, "Totales");
                y = writeLine(cs, fontRegular, 11, margin, y, "Descuento: " + fmtMoney(q.descuentoTotal));
                y = writeLine(cs, fontRegular, 11, margin, y, "Subtotal: " + fmtMoney(q.subtotalSinIva));
                y = writeLine(cs, fontRegular, 11, margin, y, "IVA: " + fmtMoney(q.ivaValor));
                y = writeLine(cs, fontRegular, 11, margin, y, "Total: " + fmtMoney(q.total));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            out.save(baos);
            return baos.toByteArray();
        }
    }

    private float writeLine(PDPageContentStream cs, PDFont font, float fontSize,
                            float x, float y, String text) throws Exception {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "" : text);
        cs.endText();
        return y - (fontSize + 3);
    }

    // ===================== Email =====================
    private void sendMailWithAttachment(String to, byte[] pdfBytes, String filename) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(SMTP_USER));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject("Cotización SGDV", "UTF-8");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Adjunto encontrará su cotización en formato PDF.", "UTF-8");

        MimeBodyPart attachPart = new MimeBodyPart();
        ByteArrayDataSource ds = new ByteArrayDataSource(pdfBytes, "application/pdf");
        attachPart.setDataHandler(new DataHandler(ds));
        attachPart.setFileName(filename);

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(textPart);
        mp.addBodyPart(attachPart);

        msg.setContent(mp);
        Transport.send(msg);
    }

    // ===================== Validaciones =====================
    private String parseRuc() {
        String raw = (txtRuc.getText() == null) ? "" : txtRuc.getText().trim();

        if (raw.isEmpty()) {
            ActionMessageFrame.showMsg(this, "Error", "Debe ingresar el RUC del potencial cliente.");
            return null;
        }

        if (raw.length() < 13) {
            ActionMessageFrame.showMsg(this, "Error", "El RUC debe tener 13 dígitos.");
            return null;
        }

        if (!raw.matches("^\\d{13}$")) {
            ActionMessageFrame.showMsg(this, "Error", "El RUC debe contener solo números (13 dígitos).");
            return null;
        }

        return raw;
    }

    private boolean isValidEmailRestricted(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.length() < 10 || e.length() > 30) return false;

        String re = "^[A-Za-z0-9_&$#\\.]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return e.matches(re);
    }

    // ===================== Helpers =====================
    private String estadoUi(String estadoBd) {
        if (estadoBd == null) return "";
        String e = estadoBd.trim();
        if (e.equalsIgnoreCase("Revision")) return "Revisión";
        return e;
    }

    private String safe(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private String fmtMoney(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String fmtQty(BigDecimal v) {
        if (v == null) return "0";
        BigDecimal x = v.stripTrailingZeros();
        return x.toPlainString();
    }

    private float drawDetailTableOnePage(PDPageContentStream cs,
                                         PDFont bold, PDFont regular,
                                         float x, float y,
                                         List<QuotationDocRepository.QuotationDetailDTO> details) throws Exception {

        float tableW = 595 - (2 * x);

        float colServicio = x;
        float colDesc    = x + 140;
        float colPU      = x + 360;
        float colCant    = x + 440;
        float colSubt    = x + 500;

        y = writeLine(cs, bold, 12, x, y, "Detalle");
        y -= 3;

        cs.setFont(bold, 10);
        writeAt(cs, "Servicio", colServicio, y);
        writeAt(cs, "Descripción", colDesc, y);
        writeAt(cs, "P.Unit", colPU, y);
        writeAt(cs, "Cant", colCant, y);
        writeAt(cs, "Subtotal", colSubt, y);
        y -= 12;

        drawLine(cs, x, y, x + tableW, y);
        y -= 10;

        cs.setFont(regular, 9);

        float minY = 170;

        for (int i = 0; i < details.size(); i++) {
            QuotationDocRepository.QuotationDetailDTO d = details.get(i);

            String serv = clip(safe(d.servicio), 18);
            String desc = clip(safe(d.descripcion), 34);
            String pu   = fmtMoney(d.precioUnitario);
            String cant = fmtQty(d.cantidad);
            String sub  = fmtMoney(d.subtotal);

            writeAt(cs, serv, colServicio, y);
            writeAt(cs, desc, colDesc, y);
            writeAt(cs, pu, colPU, y);
            writeAt(cs, cant, colCant, y);
            writeAt(cs, sub, colSubt, y);

            y -= 12;

            if (y < minY) {
                writeAt(cs, "...", colDesc, y + 12);
                break;
            }
        }

        y -= 2;
        drawLine(cs, x, y, x + tableW, y);
        y -= 8;

        return y;
    }

    private void writeAt(PDPageContentStream cs, String text, float x, float y) throws Exception {
        cs.beginText();
        cs.newLineAtOffset(x, y);
        cs.showText(text == null ? "" : text);
        cs.endText();
    }

    private void drawLine(PDPageContentStream cs, float x1, float y1, float x2, float y2) throws Exception {
        cs.moveTo(x1, y1);
        cs.lineTo(x2, y2);
        cs.stroke();
    }

    private String clip(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        if (t.length() <= max) return t;
        return t.substring(0, Math.max(0, max - 3)) + "...";
    }
}
