package secsys.views.planning;

import secsys.router.ViewRouter;
import secsys.views.addons.*;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.dto.PlanningSummaryDTO;
import secsys.dto.ClienteInfoDTO;

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
    private JTextField txtRazonSocial;
    private JTextField txtRuc;
    private JLabel lblInline;

    // Estado
    private UUID selectedClienteId;
    private UUID selectedPlanId;

    // Repos
    private final ClienteRepository clienteRepo;
    private final PlanningRepository planningRepo;

    public PlanningViewPanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        this.clienteRepo = RepoFactory.clienteRepository();
        this.planningRepo = RepoFactory.planningRepository();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(820, 520));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Ver Planificación");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // ===== BÚSQUEDA (Razón Social + RUC) =====
        JPanel searchPanel = new JPanel(new GridBagLayout());
        searchPanel.setOpaque(false);

        GridBagConstraints sc = new GridBagConstraints();
        sc.insets = new Insets(4, 6, 4, 6);
        sc.fill = GridBagConstraints.HORIZONTAL;
        sc.gridy = 0;

        txtRazonSocial = new JTextField(22);
        txtRuc = new JTextField(22);

        CustomButton btnSearch = new CustomButton("Buscar", "#4A90E2");

        // Row 1: Razón social
        sc.gridx = 0; sc.weightx = 0.0;
        searchPanel.add(new JLabel("Razón social del cliente:"), sc);
        sc.gridx = 1; sc.weightx = 1.0;
        searchPanel.add(txtRazonSocial, sc);

        // Row 2: RUC
        sc.gridy++;
        sc.gridx = 0; sc.weightx = 0.0;
        searchPanel.add(new JLabel("RUC del cliente:"), sc);
        sc.gridx = 1; sc.weightx = 1.0;
        txtRuc.setToolTipText("13 dígitos");
        searchPanel.add(txtRuc, sc);

        // Row 3: botón
        sc.gridy++;
        sc.gridx = 1; sc.weightx = 0.0;
        searchPanel.add(btnSearch, sc);

        // Buscar con Enter en cualquiera
        txtRazonSocial.addActionListener(e -> onSearch());
        txtRuc.addActionListener(e -> onSearch());
        btnSearch.addActionListener(e -> onSearch());

        // ===== INLINE MESSAGE (solo informativo, NO errores de validación) =====
        lblInline = new JLabel(" ");
        lblInline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInline.setForeground(new Color(120, 120, 120));

        // ===== RESULTADOS =====
        resultsPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        resultsPanel.setOpaque(false);

        RoundedPanel resultsContainer = new RoundedPanel(18);
        resultsContainer.setLayout(new BorderLayout());
        resultsContainer.setBackground(new Color(245, 247, 250));
        resultsContainer.setBorder(new EmptyBorder(12, 12, 12, 12));
        resultsContainer.setPreferredSize(new Dimension(760, 280));

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        resultsContainer.add(scroll, BorderLayout.CENTER);

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> {
            // ✅ limpiar inputs (razón social y ruc) al salir
            clearInputs();
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
    }

    private void clearInputs() {
        if (txtRazonSocial != null) txtRazonSocial.setText("");
        if (txtRuc != null) txtRuc.setText("");
    }

    private void onSearch() {
        resetResults();
        selectedClienteId = null;
        selectedPlanId = null;

        String ruc = txtRuc.getText() == null ? "" : txtRuc.getText().trim();
        String razon = txtRazonSocial.getText() == null ? "" : txtRazonSocial.getText().trim();

        try {
            ClienteInfoDTO selected = null;

            // PRIORIDAD: si escribió RUC, buscamos por RUC
            if (!ruc.isBlank()) {

                if (!ruc.matches("^\\d{13}$")) {
                    showMsg("Verifique los datos", "RUC debe tener 13 dígitos");
                    return;
                }

                selected = clienteRepo.findByRuc(ruc);

                if (selected == null) {
                    showMsg("No encontrado", "No existe una planificación para el RUC ingresado");
                    return;
                }

            } else {
                // Si NO hay RUC, buscamos por razón social
                if (razon.isBlank()) {
                    showMsg("Verifique los datos", "Ingrese la razón social: ");
                    return;
                }

                List<ClienteInfoDTO> matches = clienteRepo.findByRazonSocialLikeIgnoreCase(razon);

                if (matches == null || matches.isEmpty()) {
                    showMsg("No encontrado", "No existe una planificación para la razón social ingresada.");
                    return;
                }

                if (matches.size() == 1) {
                    selected = matches.get(0);
                } else {
                    String[] options = new String[matches.size()];
                    for (int i = 0; i < matches.size(); i++) {
                        ClienteInfoDTO c = matches.get(i);
                        options[i] = nvl(c.ruc) + " - " + nvl(c.razonSocial);
                    }

                    String pick = CustomSelectDialog.showSelect(
                            SwingUtilities.getWindowAncestor(this),
                            "Seleccionar cliente",
                            "Se encontraron " + matches.size() + " clientes.\nSeleccione uno:",
                            options
                    );

                    if (pick == null) {
                        showMsg("Información", "Selección cancelada.");
                        return;
                    }

                    int idx = 0;
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(pick)) { idx = i; break; }
                    }
                    selected = matches.get(idx);
                }
            }

            selectedClienteId = selected.clienteId;

            List<PlanningSummaryDTO> plans = planningRepo.findByClienteId(selectedClienteId);

            if (plans == null || plans.isEmpty()) {
                showMsg("Información", "El cliente no tiene planificaciones registradas.");
                return;
            }

            showPlanningCards(plans);
            ActionMessageFrame.showMsg("Exito", "Planificaciones encontradas:" + plans.size());

        } catch (Exception ex) {
            showMsg("Error", "Error consultando planificaciones: " + safeMsg(ex));
        }
    }

    private void showPlanningCards(List<PlanningSummaryDTO> plans) {
        resultsPanel.removeAll();

        for (PlanningSummaryDTO p : plans) {
            if (p == null) continue;

            selectedPlanId = p.planificacionId;

            resultsPanel.add(new InfoCard("Archivo", nvl(p.archivoCsvNombre)));
            resultsPanel.add(new InfoCard("Estado", nvl(p.estadoVigencia)));
            resultsPanel.add(new InfoCard("Versión", nvl(p.version)));

            resultsPanel.add(new InfoCard("Tipo servicio", nvl(p.tipoServicio)));

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

    private void downloadCsv(UUID planId) {
        if (planId == null) {
            showMsg("Verifique los datos", "No hay una planificación seleccionada para descargar.");
            return;
        }

        try {
            var up = planningRepo.getUploadByPlanificacionId(planId);
            if (up == null || up.fileBytes == null || up.fileBytes.length == 0) {
                showMsg("Información", "No existe un CSV almacenado para esta planificación.");
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

            showMsg("Información", "CSV descargado en: " + out.getAbsolutePath());

        } catch (Exception ex) {
            showMsg("Error", "Error descargando CSV: " + safeMsg(ex));
        }
    }

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

    private void showMsg(String title, String msg) {
        ActionMessageFrame.showMsg(this, title, msg);
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String safeMsg(Exception ex) {
        String m = ex.getMessage();
        if (m == null || m.isBlank()) m = ex.getClass().getSimpleName();
        return m;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
