package secsys.views.clients;

import secsys.config.DbConfig;
import secsys.controllers.ClienteController;
import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.dto.ClienteInfoDTO;
import secsys.repository.ClienteRepository;
import secsys.router.ViewRouter;
import secsys.services.ClienteService;
import secsys.views.addons.CustomButton;
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

    private JTextField txtRuc;
    private CustomButton btnSearch;

    private JCheckBox chkAll;
    private List<JCheckBox> fieldChecks;
    private JPanel resultsPanel;

    private final ClienteController clienteController;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ClientInformationPanel() {
        this(createDefaultController());
    }

    public ClientInformationPanel(ClienteController clienteController) {
        this.clienteController = clienteController;

        // Imagen de fondo
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

        // ===== RUC =====
        JPanel rucPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        rucPanel.setOpaque(false);

        txtRuc = new JTextField(15);
        btnSearch = new CustomButton("Consultar", "#4A90E2");

        rucPanel.add(new JLabel("RUC:"));
        rucPanel.add(txtRuc);
        rucPanel.add(btnSearch);

        // ===== CHECKBOXES =====
        JPanel checkPanel = new JPanel(new GridLayout(0, 2, 10, 2));
        checkPanel.setOpaque(false);
        checkPanel.setBorder(new EmptyBorder(2, 0, 2, 0));

        chkAll = new JCheckBox("Seleccionar todos");

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

        // ===== CONSULTAR REAL =====
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

        center.add(rucPanel);
        center.add(Box.createVerticalStrut(2));
        center.add(checkPanel);
        center.add(Box.createVerticalStrut(6));
        center.add(resultScroll);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    private void onSearch() {
        String ruc = txtRuc.getText().trim();

        if (ruc.isEmpty() || !ruc.matches("^\\d{13}$")) {
            new RequiredFieldsMessageFrame("Error en campo: RUC\nIngrese un RUC válido de 13 dígitos.").setVisible(true);
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
            ClienteInfoDTO cliente = clienteController.consultarPorRuc(ruc);

            if (cliente == null) {
                resultsPanel.removeAll();
                resultsPanel.revalidate();
                resultsPanel.repaint();
                new RequiredFieldsMessageFrame("No existe un cliente registrado con el RUC ingresado.").setVisible(true);
                return;
            }

            showResults(cliente);

        } catch (DbException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "No se pudo consultar el cliente.\nDetalle: " + safeMsg(ex),
                    "Error de base de datos",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ocurrió un error inesperado.\nDetalle: " + safeMsg(ex),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showResults(ClienteInfoDTO c) {
        resultsPanel.removeAll();

        boolean all = chkAll.isSelected();

        JCheckBox chkRazon = fieldChecks.get(0);
        JCheckBox chkDireccion = fieldChecks.get(1);
        JCheckBox chkRepresentante = fieldChecks.get(2);
        JCheckBox chkTelefono = fieldChecks.get(3);
        JCheckBox chkCorreo = fieldChecks.get(4);
        JCheckBox chkSector = fieldChecks.get(5);
        JCheckBox chkSize = fieldChecks.get(6);
        JCheckBox chkFechas = fieldChecks.get(7);
        JCheckBox chkEstado = fieldChecks.get(8);

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
            resultsPanel.add(new InfoCard("Sector", nz(c.sector)));

        if (all || chkSize.isSelected())
            resultsPanel.add(new InfoCard("Tamaño", nz(c.tamano)));

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

    private void resetForm() {
        txtRuc.setText("");
        chkAll.setSelected(false);
        for (JCheckBox chk : fieldChecks) chk.setSelected(false);
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }

    private static String nz(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String safeMsg(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }

    private static ClienteController createDefaultController() {
        DbConfig cfg = DbConfig.fromEnv();
        DbConnection db = new DbConnection(cfg);

        ClienteRepository repo = new ClienteRepository(db);
        ClienteService service = new ClienteService(repo);
        return new ClienteController(service);
    }
}
