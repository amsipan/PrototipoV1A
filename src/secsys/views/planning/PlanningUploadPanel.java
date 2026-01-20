package secsys.views.planning;

import secsys.config.DbConfig;
import secsys.controllers.PlanningController;
import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.dto.ClienteBasicDTO;
import secsys.dto.PlanningUploadDTO;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.router.ViewRouter;
import secsys.services.PlanningService;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.UUID;

public class PlanningUploadPanel extends JPanel {

    private Image background;

    // Cliente
    private JTextField txtRuc;
    private CustomButton btnBuscar;
    private JLabel lblRazonSocialValue;
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

        JLabel lblRuc = new JLabel("RUC del cliente:");
        lblRuc.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtRuc = new JTextField(16);

        btnBuscar = new CustomButton("Buscar", "#4A90E2");
        btnBuscar.setPreferredSize(new Dimension(130, 38));

        c.gridx = 0; c.gridy = y; c.weightx = 0.0;
        form.add(lblRuc, c);

        c.gridx = 1; c.gridy = y; c.weightx = 1.0;
        form.add(txtRuc, c);

        c.gridx = 2; c.gridy = y; c.weightx = 0.0;
        form.add(btnBuscar, c);

        y++;
        JLabel lblRazon = new JLabel("Razón social:");
        lblRazon.setFont(new Font("Segoe UI", Font.BOLD, 12));

        lblRazonSocialValue = new JLabel("-");
        lblRazonSocialValue.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        c.gridx = 0; c.gridy = y; c.weightx = 0.0;
        form.add(lblRazon, c);

        c.gridx = 1; c.gridy = y; c.weightx = 1.0; c.gridwidth = 2;
        form.add(lblRazonSocialValue, c);
        c.gridwidth = 1;

        y++;
        lblInlineMsg = new JLabel(" ");
        lblInlineMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInlineMsg.setForeground(new Color(200, 40, 40));

        c.gridx = 0; c.gridy = y; c.weightx = 1.0; c.gridwidth = 3;
        form.add(lblInlineMsg, c);
        c.gridwidth = 1;

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

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttons.setOpaque(false);

        btnBack = new CustomButton("Volver", "#9E9E9E");
        btnUpload = new CustomButton("Subir planificación", "#4A90E2");
        btnUpload.setEnabled(false);

        buttons.add(btnBack);
        buttons.add(btnUpload);

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

    private void onBuscarCliente() {
        clearInlineMessage();

        String ruc = txtRuc.getText() == null ? "" : txtRuc.getText().trim();

        if (!ruc.matches("^\\d{13}$")) {
            invalidateCliente();
            setInlineMessage("RUC de cliente no válido", true);
            refreshUploadEnabled();
            return;
        }

        try {
            ClienteBasicDTO cliente = clienteRepo.findBasicByRuc(ruc);

            if (cliente == null) {
                invalidateCliente();
                setInlineMessage("RUC de cliente no válido", true);
            } else {
                selectedClienteId = cliente.clienteId;
                lblRazonSocialValue.setText(cliente.razonSocial);
                setInlineMessage("Cliente encontrado.", false);
            }

        } catch (DbException ex) {
            invalidateCliente();
            setInlineMessage("Error consultando cliente: " + safeMsg(ex), true);
        } catch (Exception ex) {
            invalidateCliente();
            setInlineMessage("Error inesperado: " + safeMsg(ex), true);
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
        clearInlineMessage();

        if (selectedClienteId == null) {
            setInlineMessage("Debe buscar un cliente válido antes de subir la planificación.", true);
            return;
        }
        if (selectedCsv == null) {
            setInlineMessage("Debe seleccionar un archivo CSV.", true);
            return;
        }

        try {
            byte[] bytes = Files.readAllBytes(selectedCsv.toPath());

            PlanningUploadDTO dto = new PlanningUploadDTO();
            dto.clienteId = selectedClienteId;
            dto.fileName = selectedCsv.getName();
            dto.fileBytes = bytes;

            UUID planId = planningController.uploadPlanning(dto);

            setInlineMessage("Planificación subida correctamente. ID: " + planId, false);

            // Limpieza parcial
            selectedCsv = null;
            lblSelectedFile.setText("Ningún archivo seleccionado");
            refreshUploadEnabled();

        } catch (IllegalArgumentException ex) {
            setInlineMessage(ex.getMessage(), true);
        } catch (DbException ex) {
            setInlineMessage("Error de base de datos: " + safeMsg(ex), true);
        } catch (Exception ex) {
            setInlineMessage("Error inesperado: " + safeMsg(ex), true);
        }
    }

    private void refreshUploadEnabled() {
        btnUpload.setEnabled(selectedClienteId != null && selectedCsv != null);
    }

    private void invalidateCliente() {
        selectedClienteId = null;
        lblRazonSocialValue.setText("-");
    }

    private void resetForm() {
        txtRuc.setText("");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }

    private static String safeMsg(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
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
