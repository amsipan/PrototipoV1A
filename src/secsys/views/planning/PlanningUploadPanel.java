package secsys.views.planning;

import secsys.config.DbConfig;
import secsys.controllers.PlanningController;
import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.dto.ClienteInfoDTO;
import secsys.dto.PlanningUploadDTO;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.router.ViewRouter;
import secsys.services.PlanningService;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.CustomSelectDialog;
import secsys.views.addons.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

public class PlanningUploadPanel extends JPanel {

    private Image background;

    // Cliente
    private JTextField txtRazonSocial;
    private CustomButton btnBuscar;
    private JLabel lblRazonSocialValue;
    private JLabel lblRucValue;
    private UUID selectedClienteId;

    // CSV
    private CustomButton btnSelectFile;
    private JLabel lblSelectedFile;
    private File selectedCsv;

    // Mensaje inline
    private JLabel lblInlineMsg;

    // Botones
    private CustomButton btnBack;
    private CustomButton btnUpload;

    // Dependencias
    private final ClienteRepository clienteRepo;
    private final PlanningController planningController;

    public PlanningUploadPanel() {
        this(createDefaultClienteRepo(), createDefaultPlanningController());
    }

    public PlanningUploadPanel(ClienteRepository clienteRepo, PlanningController planningController) {
        this.clienteRepo = clienteRepo;
        this.planningController = planningController;

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();
        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(900, 620));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Subir Planificación");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        // ===== RAZÓN SOCIAL (CRITERIO) =====
        JLabel lblRazon = new JLabel("Razón social del cliente:");
        lblRazon.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtRazonSocial = new JTextField(24);

        btnBuscar = new CustomButton("Buscar", "#4A90E2");
        btnBuscar.setPreferredSize(new Dimension(130, 38));

        c.gridx = 0; c.gridy = y; c.weightx = 0.0;
        form.add(lblRazon, c);

        c.gridx = 1; c.gridy = y; c.weightx = 1.0;
        form.add(txtRazonSocial, c);

        c.gridx = 2; c.gridy = y; c.weightx = 0.0;
        form.add(btnBuscar, c);

        // ===== RESULTADO: RAZÓN SOCIAL + RUC =====
        y++;
        JLabel lblSel = new JLabel("Cliente seleccionado:");
        lblSel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblRazonSocialValue = new JLabel("-");
        lblRazonSocialValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        c.gridx = 0; c.gridy = y; c.weightx = 0.0;
        form.add(lblSel, c);

        c.gridx = 1; c.gridy = y; c.weightx = 1.0; c.gridwidth = 2;
        form.add(lblRazonSocialValue, c);
        c.gridwidth = 1;

        y++;
        JLabel lblRuc = new JLabel("RUC:");
        lblRuc.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblRucValue = new JLabel("-");
        lblRucValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        c.gridx = 0; c.gridy = y; c.weightx = 0.0;
        form.add(lblRuc, c);

        c.gridx = 1; c.gridy = y; c.weightx = 1.0; c.gridwidth = 2;
        form.add(lblRucValue, c);
        c.gridwidth = 1;

        // ===== MENSAJE INLINE =====
        y++;
        lblInlineMsg = new JLabel(" ");
        lblInlineMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInlineMsg.setForeground(new Color(200, 40, 40));

        c.gridx = 0; c.gridy = y; c.weightx = 1.0; c.gridwidth = 3;
        form.add(lblInlineMsg, c);
        c.gridwidth = 1;

        // ===== CSV =====
        y++;
        JLabel lblCsv = new JLabel("Archivo CSV:");
        lblCsv.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnSelectFile = new CustomButton("Seleccionar archivo...", "#4A90E2");
        btnSelectFile.setPreferredSize(new Dimension(250, 45));

        c.gridx = 0; c.gridy = y; c.weightx = 0.0;
        form.add(lblCsv, c);

        c.gridx = 1; c.gridy = y; c.weightx = 1.0; c.gridwidth = 2;
        form.add(btnSelectFile, c);
        c.gridwidth = 1;

        y++;
        lblSelectedFile = new JLabel("Ningún archivo seleccionado");
        lblSelectedFile.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        c.gridx = 1; c.gridy = y; c.weightx = 1.0; c.gridwidth = 2;
        form.add(lblSelectedFile, c);
        c.gridwidth = 1;

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);

        btnBack = new CustomButton("Volver", "#9E9E9E");
        btnUpload = new CustomButton("Subir planificación", "#4A90E2");
        btnUpload.setEnabled(false);

        buttons.add(btnBack);
        buttons.add(btnUpload);

        // ===== EVENTOS =====
        btnBuscar.addActionListener(e -> onBuscarCliente());
        btnSelectFile.addActionListener(e -> onSelectCsv());
        btnUpload.addActionListener(e -> onUpload());
        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("plannings");
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalStrut(20));
        center.add(form);
        center.add(Box.createVerticalGlue());

        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    // ===============================
    // Buscar cliente por Razón Social (ILIKE)
    // ===============================
    private void onBuscarCliente() {
        clearInlineMessage();
        invalidateCliente();

        String razon = txtRazonSocial.getText() == null ? "" : txtRazonSocial.getText().trim();
        if (razon.isBlank()) {
            ActionMessageFrame.showMsg("Ingresar Razón Social","Debe ingresar una razón social");
            refreshUploadEnabled();
            return;
        }

        try {
            List<ClienteInfoDTO> matches = clienteRepo.findByRazonSocialLikeIgnoreCase(razon);

            if (matches == null || matches.isEmpty()) {
                ActionMessageFrame.showMsg("Error Razón Social", "No existe un cliente con la razón social ingresada");
                refreshUploadEnabled();
                return;
            }

            ClienteInfoDTO selected;

            if (matches.size() == 1) {
                selected = matches.get(0);
            } else {
                String[] options = new String[matches.size()];
                for (int i = 0; i < matches.size(); i++) {
                    ClienteInfoDTO cli = matches.get(i);
                    options[i] = safe(cli.ruc) + " - " + safe(cli.razonSocial);
                }

                String pick = CustomSelectDialog.showSelect(
                        SwingUtilities.getWindowAncestor(this),
                        "Seleccionar cliente",
                        "Se encontraron " + matches.size() + " clientes.\nSeleccione uno:",
                        options
                );

                if (pick == null) { // cancel
                    setInlineMessage("Selección cancelada.", true);
                    refreshUploadEnabled();
                    return;
                }

                int idx = 0;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(pick)) { idx = i; break; }
                }
                selected = matches.get(idx);
            }

            selectedClienteId = selected.clienteId;
            lblRazonSocialValue.setText(safe(selected.razonSocial));
            lblRucValue.setText(safe(selected.ruc));

        } catch (DbException ex) {
            setInlineMessage("Error consultando cliente.", true);
        } catch (Exception ex) {
            setInlineMessage("Error inesperado.", true);
        }

        refreshUploadEnabled();
    }

    private void onSelectCsv() {
        clearInlineMessage();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar archivo CSV");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos CSV (*.csv)", "csv"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();

            if (f == null || !f.getName().toLowerCase().endsWith(".csv")) {
                selectedCsv = null;
                lblSelectedFile.setText("Ningún archivo seleccionado");
                setInlineMessage("Seleccione un archivo con extensión .csv", true);
            } else {
                selectedCsv = f;
                lblSelectedFile.setText(f.getName());
            }
        }

        refreshUploadEnabled();
    }

    private void onUpload() {

    if (selectedClienteId == null || selectedCsv == null) {
        ActionMessageFrame.showMsg("Campos obligatorios", "No se pudo cargar la planificación.");
        return;
    }

    try {
        byte[] bytes = Files.readAllBytes(selectedCsv.toPath());

        PlanningUploadDTO dto = new PlanningUploadDTO();
        dto.clienteId = selectedClienteId;
        dto.fileName = selectedCsv.getName();
        dto.fileBytes = bytes;

        planningController.uploadPlanning(dto);

        // ✅ Éxito como tu screenshot
        ActionMessageFrame.showMsg("Campos obligatorios", "Planificación cargada exitosamente");

        selectedCsv = null;
        lblSelectedFile.setText("Ningún archivo seleccionado");
        refreshUploadEnabled();

    } catch (DbException ex) {

        if (isDuplicateKey(ex)) {
            ActionMessageFrame.showMsg(
                "Campos obligatorios",
                "No se pudo cargar la planificación. La versión ya esta en el sistema"
            );
            return;
        }

        ActionMessageFrame.showMsg("Campos obligatorios", "No se pudo cargar la planificación. La versión ya esta en el sistema");

    } catch (Exception ex) {
        ActionMessageFrame.showMsg("Campos", "No se pudo cargar la planificación.");
    }
}

    private void refreshUploadEnabled() {
        btnUpload.setEnabled(selectedClienteId != null && selectedCsv != null);
    }

    private void invalidateCliente() {
        selectedClienteId = null;
        lblRazonSocialValue.setText("-");
        lblRucValue.setText("-");
    }

    private void resetForm() {
        txtRazonSocial.setText("");
        invalidateCliente();
        selectedCsv = null;
        lblSelectedFile.setText("Ningún archivo seleccionado");
        clearInlineMessage();
        refreshUploadEnabled();
    }

    private void setInlineMessage(String msg, boolean isError) {
        lblInlineMsg.setText((msg == null || msg.isBlank()) ? " " : msg);
        lblInlineMsg.setForeground(isError ? new Color(200, 40, 40) : new Color(30, 130, 50));
    }

    private void clearInlineMessage() {
        lblInlineMsg.setText(" ");
        lblInlineMsg.setForeground(new Color(200, 40, 40));
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }


    private static boolean isDuplicateKey(DbException ex) {
        String m = (ex.getMessage() == null ? "" : ex.getMessage()).toLowerCase();
        return m.contains("duplicate key")
                || m.contains("violates unique constraint")
                || m.contains("23505");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }


    private static ClienteRepository createDefaultClienteRepo() {
        DbConfig cfg = DbConfig.fromEnv();
        DbConnection db = new DbConnection(cfg);
        return new ClienteRepository(db);
    }

    private static PlanningController createDefaultPlanningController() {
        DbConfig cfg = DbConfig.fromEnv();
        DbConnection db = new DbConnection(cfg);
        PlanningRepository repo = new PlanningRepository(db);
        PlanningService service = new PlanningService(repo);
        return new PlanningController(service);
    }
}
