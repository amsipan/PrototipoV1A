package secsys.views.planning;

import secsys.router.ViewRouter;
import secsys.views.addons.*;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.dto.PlanningSummaryDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class PlanningViewPanel extends JPanel {

    private Image background;
    private JPanel resultsPanel;

    // UI
    private JTextField txtRuc;
    private JLabel lblInline;

    // Estado
    private UUID selectedClienteId;
    private UUID selectedPlanId; // se usa para descargar (última card tocada si decides usarlo)

    // Repos
    private final ClienteRepository clienteRepo;
    private final PlanningRepository planningRepo;

    public PlanningViewPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        // ===== Repos (vía RepoFactory para no romper tu MainFrame) =====
        // IMPORTANTE: RepoFactory.init(dbConnection) debe ejecutarse al iniciar la app.
        this.clienteRepo = RepoFactory.clienteRepository();
        this.planningRepo = RepoFactory.planningRepository();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(800, 480));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Ver Planificación");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // ===== BÚSQUEDA =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        searchPanel.setOpaque(false);

        txtRuc = new JTextField(15);
        CustomButton btnSearch = new CustomButton("Buscar", "#4A90E2");

        searchPanel.add(new JLabel("RUC del cliente:"));
        searchPanel.add(txtRuc);
        searchPanel.add(btnSearch);

        // ===== INLINE MESSAGE =====
        lblInline = new JLabel(" ");
        lblInline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInline.setForeground(new Color(120, 120, 120));

        // ===== RESULTADOS =====
        // OJO: ahora debe soportar muchas planificaciones, así que usamos JScrollPane
        resultsPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        resultsPanel.setOpaque(false);

        RoundedPanel resultsContainer = new RoundedPanel(18);
        resultsContainer.setLayout(new BorderLayout());
        resultsContainer.setBackground(new Color(245, 247, 250));
        resultsContainer.setBorder(new EmptyBorder(12, 12, 12, 12));
        resultsContainer.setPreferredSize(new Dimension(740, 260));

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        resultsContainer.add(scroll, BorderLayout.CENTER);

        // Acción buscar: cargar desde BD y pintar cards
        btnSearch.addActionListener(e -> onSearch());

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> {
            resetResults();
            clearState();
            ViewRouter.show("plannings");
        });

        buttons.add(btnBack);

        // ===== CENTRO =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(searchPanel);
        center.add(Box.createVerticalStrut(8));
        center.add(lblInline);
        center.add(Box.createVerticalStrut(10));
        center.add(resultsContainer);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);

        // Estado inicial
        setInlineMessage("Ingrese un RUC y presione Buscar.", false);
    }

    private void onSearch() {
        resetResults();
        selectedClienteId = null;
        selectedPlanId = null;

        String ruc = txtRuc.getText() == null ? "" : txtRuc.getText().trim();
        if (ruc.isEmpty()) {
            setInlineMessage("Ingrese el RUC del cliente.", true);
            return;
        }

        try {
            // Buscamos cliente (básico)
            var cliente = clienteRepo.findBasicByRuc(ruc);
            if (cliente == null) {
                setInlineMessage("RUC de cliente no válido", true);
                return;
            }
            selectedClienteId = cliente.clienteId;

            // ✅ Traer TODAS las planificaciones del cliente (v1.0, v1.1, ...)
            List<PlanningSummaryDTO> plans = planningRepo.findByClienteId(selectedClienteId);

            if (plans == null || plans.isEmpty()) {
                setInlineMessage("El cliente no tiene planificaciones registradas.", true);
                return;
            }

            // Pintar cards: un "bloque" por planificación (Archivo/Estado/Versión/Descarga)
            showPlanningCards(plans);

            setInlineMessage("Planificaciones encontradas: " + plans.size(), false);

        } catch (Exception ex) {
            setInlineMessage("Error consultando planificaciones: " + safeMsg(ex), true);
        }
    }

    // ===== RESULTADOS (REAL, mantiene estilo de InfoCard) =====
    private void showPlanningCards(List<PlanningSummaryDTO> plans) {
        resultsPanel.removeAll();

        for (PlanningSummaryDTO p : plans) {
            // Si viene null por cualquier razón, lo saltamos
            if (p == null) continue;

            // Guardamos el último planId "visto" (no es obligatorio, pero sirve si luego quieres otra acción)
            selectedPlanId = p.planificacionId;

            // 1) Archivo
            resultsPanel.add(new InfoCard("Archivo", nvl(p.archivoCsvNombre)));

            // 2) Estado (vigencia Activa/Inactiva)
            resultsPanel.add(new InfoCard("Estado", nvl(p.estadoVigencia)));

            // 3) Versión
            resultsPanel.add(new InfoCard("Versión", nvl(p.version)));

            // 4) Descargar CSV (ocupa una celda adicional; se verá en la siguiente fila del grid)
            resultsPanel.add(createDownloadCard(
                    "Descargar CSV (" + nvl(p.version) + ")",
                    "Descargar",
                    () -> downloadCsv(p.planificacionId)
            ));

            // 5) (Opcional pero útil) Tipo servicio para diferenciar si luego hay varios servicios
            resultsPanel.add(new InfoCard("Tipo servicio", nvl(p.tipoServicio)));

            // 6) Rango de fechas
            String rango = "-";
            if (p.fechaInicio != null && p.fechaFin != null) {
                rango = p.fechaInicio + "  →  " + p.fechaFin;
            } else if (p.fechaInicio != null) {
                rango = p.fechaInicio.toString();
            }
            resultsPanel.add(new InfoCard("Rango", rango));
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel createDownloadCard(String title, String buttonText, Runnable onClick) {
        RoundedPanel card = new RoundedPanel(18);
        card.setLayout(new BorderLayout(8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(70, 70, 70));

        JButton btn = new JButton(buttonText);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            try {
                onClick.run();
            } catch (Exception ex) {
                setInlineMessage("No se pudo descargar el CSV: " + safeMsg(ex), true);
            }
        });

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(btn, BorderLayout.CENTER);
        return card;
    }

    private void downloadCsv(UUID planId) {
        if (planId == null) {
            setInlineMessage("No hay una planificación seleccionada para descargar.", true);
            return;
        }

        try {
            var up = planningRepo.getUploadByPlanificacionId(planId);
            if (up == null || up.fileBytes == null || up.fileBytes.length == 0) {
                setInlineMessage("No existe un CSV almacenado para esta planificación.", true);
                return;
            }

            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Guardar CSV");
            fc.setSelectedFile(new File(
                    (up.fileName == null || up.fileName.isBlank()) ? "planificacion.csv" : up.fileName
            ));

            int res = fc.showSaveDialog(this);
            if (res != JFileChooser.APPROVE_OPTION) return;

            File out = fc.getSelectedFile();
            Files.write(out.toPath(), up.fileBytes);

            setInlineMessage("CSV descargado en: " + out.getAbsolutePath(), false);

        } catch (Exception ex) {
            setInlineMessage("Error descargando CSV: " + safeMsg(ex), true);
        }
    }

    // ===== RESET VISUAL =====
    private void resetResults() {
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void clearState() {
        selectedClienteId = null;
        selectedPlanId = null;
        setInlineMessage(" ", false);
    }

    private void setInlineMessage(String msg, boolean isError) {
        lblInline.setText(msg == null || msg.isBlank() ? " " : msg);
        lblInline.setForeground(isError ? new Color(180, 0, 0) : new Color(0, 120, 0));
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String safeMsg(Exception ex) {
        String m = ex.getMessage();
        if (m == null || m.isBlank()) m = ex.getClass().getSimpleName();
        return m;
    }

    // ===== FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
