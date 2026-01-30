package secsys.views.clients;

import secsys.config.DbConfig;
import secsys.controllers.ClienteController;
import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.dto.ClienteInfoDTO;
import secsys.repository.ClienteRepository;
import secsys.router.ViewRouter;
import secsys.services.ClienteService;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.CustomSelectDialog;
import secsys.views.addons.ErrorActionDialog;
import secsys.views.addons.InfoCard;
import secsys.views.addons.RequiredFieldsMessageFrame;
import secsys.views.addons.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ClientInformationPanel extends JPanel {

    private Image background;

    private JTextField txtRazonSocial;
    private JTextField txtRuc;
    private CustomButton btnSearch;

    private JCheckBox chkAll;
    private List<JCheckBox> fieldChecks;
    private JPanel resultsPanel;

    private final ClienteController clienteController;
    private final ClienteRepository clienteRepo; // ✅ para buscar por RUC exacto

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ClientInformationPanel() {
        this(createDefaultController(), createDefaultRepo());
    }

    public ClientInformationPanel(ClienteController clienteController, ClienteRepository clienteRepo) {
        this.clienteController = clienteController;
        this.clienteRepo = clienteRepo;

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(900, 620));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Consultar Información del Cliente");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // ===== FILTROS =====
        JPanel razonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        razonPanel.setOpaque(false);

        txtRazonSocial = new JTextField(18);
        txtRuc = new JTextField(14);

        btnSearch = new CustomButton("Consultar", "#4A90E2");

        razonPanel.add(new JLabel("Razón social:"));
        razonPanel.add(txtRazonSocial);
        razonPanel.add(new JLabel("RUC:"));
        razonPanel.add(txtRuc);
        razonPanel.add(btnSearch);

        // ===== CHECKBOXES =====
        JPanel checkPanel = new JPanel(new GridLayout(0, 2, 10, 2));
        checkPanel.setOpaque(false);
        checkPanel.setBorder(new EmptyBorder(2, 0, 2, 0));

        chkAll = new JCheckBox("Seleccionar todos");

        JCheckBox chkRuc = new JCheckBox("RUC");
        JCheckBox chkRazon = new JCheckBox("Razón social");
        JCheckBox chkDireccion = new JCheckBox("Dirección");
        JCheckBox chkRepresentante = new JCheckBox("Representante legal");
        JCheckBox chkTelefono = new JCheckBox("Teléfono");
        JCheckBox chkCorreo = new JCheckBox("Correo electrónico");
        JCheckBox chkSector = new JCheckBox("Sector empresarial");
        JCheckBox chkSize = new JCheckBox("Tamaño de la empresa");
        JCheckBox chkFechas = new JCheckBox("Fechas de contrato");
        JCheckBox chkEstado = new JCheckBox("Estado del cliente");

        fieldChecks = new ArrayList<>();
        fieldChecks.add(chkRuc);
        fieldChecks.add(chkRazon);
        fieldChecks.add(chkDireccion);
        fieldChecks.add(chkRepresentante);
        fieldChecks.add(chkTelefono);
        fieldChecks.add(chkCorreo);
        fieldChecks.add(chkSector);
        fieldChecks.add(chkSize);
        fieldChecks.add(chkFechas);
        fieldChecks.add(chkEstado);

        chkAll.addActionListener(e -> {
            boolean selected = chkAll.isSelected();
            for (JCheckBox chk : fieldChecks) chk.setSelected(selected);
        });

        checkPanel.add(chkAll);
        checkPanel.add(new JLabel(""));
        for (JCheckBox chk : fieldChecks) checkPanel.add(chk);

        // ===== RESULTADOS =====
        resultsPanel = new JPanel(new GridLayout(0, 3, 14, 14));
        resultsPanel.setOpaque(false);

        RoundedPanel resultsContainer = new RoundedPanel(18);
        resultsContainer.setLayout(new BorderLayout());
        resultsContainer.setBackground(new Color(245, 247, 250));
        resultsContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        resultsContainer.setPreferredSize(new Dimension(820, 340));
        resultsContainer.add(resultsPanel, BorderLayout.CENTER);

        JScrollPane resultScroll = new JScrollPane(resultsContainer);
        resultScroll.setBorder(null);
        resultScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        resultScroll.getViewport().setOpaque(false);
        resultScroll.setOpaque(false);

        btnSearch.addActionListener(e -> onSearch());

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("clients");
        });

        buttons.add(btnBack);

        // ===== CENTRO =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(razonPanel);
        center.add(Box.createVerticalStrut(2));
        center.add(checkPanel);
        center.add(Box.createVerticalStrut(6));
        center.add(resultScroll);

        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    private void onSearch() {
        String razon = (txtRazonSocial.getText() == null) ? "" : txtRazonSocial.getText().trim();
        String ruc   = (txtRuc.getText() == null) ? "" : txtRuc.getText().trim();

        if (razon.isEmpty() && ruc.isEmpty()) {
            new RequiredFieldsMessageFrame("Ingrese una razón social o un RUC.").setVisible(true);
            return;
        }

        boolean anySelected = chkAll.isSelected();
        if (!anySelected) {
            for (JCheckBox chk : fieldChecks) {
                if (chk.isSelected()) { anySelected = true; break; }
            }
        }
        if (!anySelected) {
            new RequiredFieldsMessageFrame("Seleccione al menos un campo para consultar.").setVisible(true);
            return;
        }

        try {
            ClienteInfoDTO selected;

            // ==========================================================
            // 1) PRIORIDAD: si hay RUC, buscar por RUC exacto
            // ==========================================================
            if (!ruc.isBlank()) {
                if (!ruc.matches("^\\d{13}$")) {
                    ActionMessageFrame.showMsg("No hay coincidencias", "Cliente no encontrado por su RUC");
                    resetResultsOnly();
                    return;
                }

                selected = clienteRepo.findByRuc(ruc);

                if (selected == null) {
                    ActionMessageFrame.showMsg("Campos vacios", "Debe ingresar una razón social o un RUC");
                    resetResultsOnly();
                    return;
                }

            } else {
                // ==========================================================
                // 2) Si NO hay RUC, buscar por Razón Social (LIKE/ILIKE)
                // ==========================================================
                List<ClienteInfoDTO> matches = clienteController.consultarPorRazonSocial(razon);

                if (matches == null || matches.isEmpty()) {
                    ActionMessageFrame.showMsg("No hay coincidencias", "Cliente no encontrado por su razón social");;
                    resetResultsOnly();
                    return;
                }

                if (matches.size() == 1) {
                    selected = matches.get(0);
                } else {
                    String[] options = new String[matches.size()];
                    for (int i = 0; i < matches.size(); i++) {
                        ClienteInfoDTO c = matches.get(i);
                        options[i] = nz(c.ruc) + " - " + nz(c.razonSocial);
                    }

                    String pick = CustomSelectDialog.showSelect(
                            SwingUtilities.getWindowAncestor(this),
                            "Seleccionar cliente",
                            "Se encontraron " + matches.size() + " clientes.\nSeleccione uno:",
                            options
                    );

                    if (pick == null) return;

                    int idx = 0;
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(pick)) { idx = i; break; }
                    }
                    selected = matches.get(idx);
                }
            }

            showResults(selected);

        } catch (DbException ex) {
            ErrorActionDialog.showError(
                    SwingUtilities.getWindowAncestor(this),
                    "Campo inválido. No se puede actualizar."
            );
            resetResultsOnly();
        } catch (Exception ex) {
            ErrorActionDialog.showError(
                    SwingUtilities.getWindowAncestor(this),
                    "Campo inválido. No se puede actualizar."
            );
            resetResultsOnly();
        }
    }

    private void showResults(ClienteInfoDTO c) {
        resultsPanel.removeAll();
        boolean all = chkAll.isSelected();

        JCheckBox chkRuc = fieldChecks.get(0);
        JCheckBox chkRazon = fieldChecks.get(1);
        JCheckBox chkDireccion = fieldChecks.get(2);
        JCheckBox chkRepresentante = fieldChecks.get(3);
        JCheckBox chkTelefono = fieldChecks.get(4);
        JCheckBox chkCorreo = fieldChecks.get(5);
        JCheckBox chkSector = fieldChecks.get(6);
        JCheckBox chkSize = fieldChecks.get(7);
        JCheckBox chkFechas = fieldChecks.get(8);
        JCheckBox chkEstado = fieldChecks.get(9);

        if (all || chkRuc.isSelected())
            resultsPanel.add(new InfoCard("RUC", nz(c.ruc)));

        if (all || chkRazon.isSelected())
            resultsPanel.add(new InfoCard("Razón social", nz(c.razonSocial)));

        if (all || chkDireccion.isSelected())
            resultsPanel.add(new InfoCard("Dirección", nz(c.direccion)));

        if (all || chkRepresentante.isSelected())
            resultsPanel.add(new InfoCard("Representante", nz(c.representanteLegal)));

        if (all || chkTelefono.isSelected())
            resultsPanel.add(new InfoCard("Teléfono", nz(c.telefono)));

        if (all || chkCorreo.isSelected())
            resultsPanel.add(new InfoCard("Correo", nz(c.correo)));

        if (all || chkSector.isSelected())
            resultsPanel.add(new InfoCard("Sector", prettySector(c.sector)));

        if (all || chkSize.isSelected())
            resultsPanel.add(new InfoCard("Tamaño", prettyTamano(c.tamano)));

        if (all || chkFechas.isSelected()) {
            String ini = (c.fechaInicioContrato == null) ? "-" : c.fechaInicioContrato.format(FMT);
            String fin = (c.fechaFinContrato == null) ? "-" : c.fechaFinContrato.format(FMT);
            resultsPanel.add(new InfoCard("Contrato", ini + " - " + fin));
        }

        if (all || chkEstado.isSelected())
            resultsPanel.add(new InfoCard("Estado", nz(c.estado)));

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    // ✅ Mapeo SOLO para presentar (BD queda sin tildes/ñ)
    private static String prettySector(String s) {
        if (s == null || s.isBlank()) return "-";
        String v = s.trim();

        // Normal (sin tildes) → Presentación (con tilde)
        if (v.equalsIgnoreCase("Tecnologico")) return "Tecnológico";

        // Si por algún motivo llega “dañado” con �, lo corregimos igual
        String low = v.toLowerCase();
        if (low.contains("tecnol")) return "Tecnológico";

        return v;
    }

    private static String prettyTamano(String s) {
        if (s == null || s.isBlank()) return "-";
        String v = s.trim();

        if (v.equalsIgnoreCase("Pequena")) return "Pequeña";

        String low = v.toLowerCase();
        if (low.contains("peque")) return "Pequeña";

        return v;
    }

    private void resetResultsOnly() {
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void resetForm() {
        txtRazonSocial.setText("");
        txtRuc.setText("");
        chkAll.setSelected(false);
        for (JCheckBox chk : fieldChecks) chk.setSelected(false);
        resetResultsOnly();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static ClienteController createDefaultController() {
        DbConfig cfg = DbConfig.fromEnv();
        DbConnection db = new DbConnection(cfg);

        ClienteRepository repo = new ClienteRepository(db);
        ClienteService service = new ClienteService(repo);
        return new ClienteController(service);
    }

    private static ClienteRepository createDefaultRepo() {
        DbConfig cfg = DbConfig.fromEnv();
        DbConnection db = new DbConnection(cfg);
        return new ClienteRepository(db);
    }
}
