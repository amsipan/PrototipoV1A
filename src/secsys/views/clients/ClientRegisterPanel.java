package secsys.views.clients;

import com.toedter.calendar.JDateChooser;
import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RequiredFieldsMessageFrame;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import secsys.config.DbConfig;
import secsys.controllers.ClienteController;
import secsys.db.DbConnection;
import secsys.db.DbException;
import secsys.repository.ClienteRepository;
import secsys.services.ClienteService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


public class ClientRegisterPanel extends JPanel {

    private Image background;

    private JTextField txtRuc;
    private JTextField txtRazonSocial;
    private JTextField txtDireccion;
    private JTextField txtRepresentante;
    private JTextField txtTelefono;
    private JTextField txtCorreo;

    private JComboBox<String> cmbSector;
    private JComboBox<String> cmbSize;

    // Estado fijo "Activo" (no modificable)
    private JComboBox<String> cmbStatus;

    // Fechas de contrato
    private JDateChooser dcInicioContrato;
    private JDateChooser dcFinContrato;

    private final ClienteController clienteController;

    public ClientRegisterPanel() {
        this(createDefaultController());
        initUI();
    }

    public ClientRegisterPanel(ClienteController clienteController) {
        this.clienteController = clienteController;
    }

    private void initUI() {
        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(820, 640));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Registrar Nuevo Cliente");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 15, 0));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        txtRuc = new JTextField(20);
        txtRazonSocial = new JTextField(20);
        txtDireccion = new JTextField(20);
        txtRepresentante = new JTextField(20);
        txtTelefono = new JTextField(20);
        txtCorreo = new JTextField(20);

        cmbSector = new JComboBox<>(new String[]{
                "Seleccione", "Comercial", "Industrial", "Servicios", "Tecnologico", "Otro"
        });


        cmbSize = new JComboBox<>(new String[]{
                "Seleccione", "Microempresa", "Pequena", "Mediana", "Grande"
        });

        // Estado SIEMPRE "Activo" y deshabilitado
        cmbStatus = new JComboBox<>(new String[]{"Activo"});
        cmbStatus.setSelectedIndex(0);
        cmbStatus.setEnabled(false);
        cmbStatus.setFocusable(false);

        // Fechas con JDateChooser
        dcInicioContrato = new JDateChooser();
        dcFinContrato = new JDateChooser();
        dcInicioContrato.setDateFormatString("dd/MM/yyyy");
        dcFinContrato.setDateFormatString("dd/MM/yyyy");

        addField(form, c, y++, "RUC:", txtRuc);
        addField(form, c, y++, "Razón social:", txtRazonSocial);
        addField(form, c, y++, "Dirección:", txtDireccion);
        addField(form, c, y++, "Tamaño de la empresa:", cmbSize);
        addField(form, c, y++, "Sector empresarial:", cmbSector);
        addField(form, c, y++, "Representante legal:", txtRepresentante);
        addField(form, c, y++, "Teléfono de contacto:", txtTelefono);
        addField(form, c, y++, "Correo electrónico:", txtCorreo);

        addField(form, c, y++, "Fecha inicio contrato:", dcInicioContrato);
        addField(form, c, y++, "Fecha fin contrato:", dcFinContrato);

        addField(form, c, y++, "Estado del cliente:", cmbStatus);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");

        btnCancel.addActionListener(e -> ViewRouter.show("clients"));
        btnSave.addActionListener(e -> onSave());

        buttons.add(btnCancel);
        buttons.add(btnSave);

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);
    }

    private void onSave() {
        String msg = validateFormMessage();
        if (msg != null) {
            new RequiredFieldsMessageFrame("Error en campo: " + msg).setVisible(true);
            return;
        }

        try {

            new SuccessMessageFrame("Cliente registrado correctamente.").setVisible(true);
            resetForm();
            ViewRouter.show("dashboard");

        } catch (IllegalArgumentException ex) {
            new RequiredFieldsMessageFrame("Error en campo: " + ex.getMessage()).setVisible(true);

        } catch (DbException ex) {
                
            JOptionPane.showMessageDialog(
                    this,
                    mapDbErrorToFieldMessage(ex),
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


    private String validateFormMessage() {
        String ruc = txtRuc.getText().trim();
        String razon = txtRazonSocial.getText().trim();
        String dir = txtDireccion.getText().trim();
        String rep = txtRepresentante.getText().trim();
        String tel = txtTelefono.getText().trim();
        String email = txtCorreo.getText().trim();

        if (ruc.isEmpty() || !ruc.matches("^[0-9]{13}$")) return "RUC inválido (13 dígitos).";
        if (razon.isEmpty() || razon.length() < 3) return "Razón social inválida (mínimo 3 caracteres).";

        // Ajusta el mínimo a tu regla real de negocio (aquí: mínimo 3 para pruebas)
        if (dir.isEmpty() || dir.length() < 3) return "Dirección inválida (mínimo 3 caracteres).";

        if (rep.isEmpty() || rep.length() < 3) return "Representante legal inválido (mínimo 3 caracteres).";
        if (tel.isEmpty() || !tel.matches("^[0-9]{10}$")) return "Teléfono inválido (10 dígitos).";
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            return "Correo electrónico inválido.";

        if (cmbSector.getSelectedIndex() == 0) return "Sector empresarial (seleccione una opción).";
        if (cmbSize.getSelectedIndex() == 0) return "Tamaño de la empresa (seleccione una opción).";

        Date iniD = dcInicioContrato.getDate();
        Date finD = dcFinContrato.getDate();
        if (iniD == null) return "Fecha inicio contrato (seleccione una fecha).";
        if (finD == null) return "Fecha fin contrato (seleccione una fecha).";

        LocalDate ini = toLocalDate(iniD);
        LocalDate fin = toLocalDate(finD);
        if (!fin.isAfter(ini)) return "Fechas de contrato (la fecha fin debe ser mayor a la fecha inicio).";

        return null;
    }

    private String mapDbErrorToFieldMessage(DbException ex) {
            String raw = safeMsg(ex);
            String low = raw.toLowerCase();
        
            // Dominio / catálogo de sector
            if (low.contains("d_sector_empresa_check") || low.contains("d_sector_empresa")) {
                return "Error en campo: Sector empresarial\n";
            }

            // Dominio / catálogo de tamaño
            if (low.contains("d_tamano_empresa_check") || low.contains("d_tamano_empresa")) {
                return "Error en campo: Tamaño de la empresa\n";
            }
    
            // Uniques típicos
            if (low.contains("duplicate key") && low.contains("ruc")) {
                return "Error en campo: RUC\nYa existe un cliente registrado con ese RUC.";
            }
    
            // Check de fechas
            if (low.contains("ck_cliente_fechas_contrato") || low.contains("fecha_fin_contrato")) {
                return "Error en campo: Fechas de contrato\nLa fecha fin debe ser mayor a la fecha inicio.";
            }
    
            // Not null
            if (low.contains("null value in column")) {
                if (low.contains("fecha_inicio_contrato"))
                    return "Error en campo: Fecha inicio contrato\nDebe seleccionar una fecha.";
                if (low.contains("fecha_fin_contrato"))
                    return "Error en campo: Fecha fin contrato\nDebe seleccionar una fecha.";
                if (low.contains("ruc"))
                    return "Error en campo: RUC\nNo puede estar vacío.";
                if (low.contains("correo"))
                    return "Error en campo: Correo electrónico\nNo puede estar vacío.";
            }
    
            // Tabla no encontrada (schema incorrecto)
            if (low.contains("relation") && low.contains("does not exist") && low.contains("cliente")) {
                return "Error de configuración\nNo se encontró la tabla 'cliente'. Verifique currentSchema=sgsis en la JDBC URL.";
            }
    
            // Por defecto: mostrar detalle real para depurar
            return "No se pudo registrar el cliente.\nDetalle: " + raw;
        }


    private void resetForm() {
        txtRuc.setText("");
        txtRazonSocial.setText("");
        txtDireccion.setText("");
        txtRepresentante.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");

        cmbSector.setSelectedIndex(0);
        cmbSize.setSelectedIndex(0);

        dcInicioContrato.setDate(null);
        dcFinContrato.setDate(null);

        // Estado fijo
        cmbStatus.setSelectedIndex(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }

    private void addField(JPanel panel, GridBagConstraints c, int y, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        panel.add(field, c);
    }

    private static String safeMsg(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Stack mínimo para pruebas rápidas:
     * DbConfig -> DbConnection -> Repo -> Service -> Controller
     * Luego puedes mover esto a AppContext (inyección) si quieres.
     */
    private static ClienteController createDefaultController() {
        DbConfig cfg = DbConfig.fromEnv();
        DbConnection db = new DbConnection(cfg);

        // IMPORTANTE: Asegúrate de que tu ClienteRepository use 10 parámetros
        // (sin estado) y setee el 9 y 10 para las fechas.
        ClienteRepository repo = new ClienteRepository(db);
        ClienteService service = new ClienteService(repo);

        return new ClienteController(service);
    }
}
