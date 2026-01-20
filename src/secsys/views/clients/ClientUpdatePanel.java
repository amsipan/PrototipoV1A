package secsys.views.clients;

import com.toedter.calendar.JDateChooser;
import secsys.dto.ClienteInfoDTO;
import secsys.repository.ClienteRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;
import secsys.views.planning.RepoFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class ClientUpdatePanel extends JPanel {

    private Image background;

    private final ClienteRepository clienteRepo;

    // Search
    private JTextField txtRucSearch;
    private CustomButton btnBuscar;

    // Inline
    private JLabel lblInline;

    // Form fields
    private JTextField txtRuc; // NO editable
    private JTextField txtRazonSocial;
    private JTextField txtDireccion;
    private JTextField txtRepresentante;
    private JTextField txtTelefono;
    private JTextField txtCorreo;

    private JComboBox<String> cmbSector;
    private JComboBox<String> cmbTamano;
    private JComboBox<String> cmbEstado;

    private JDateChooser dcInicioContrato;
    private JDateChooser dcFinContrato;

    // Buttons
    private CustomButton btnGuardar;
    private CustomButton btnCancelar;
    private CustomButton btnVolver;

    // State
    private UUID clienteIdLoaded;

    public ClientUpdatePanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();
        this.clienteRepo = RepoFactory.clienteRepository();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(900, 580));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Actualizar Cliente");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 12, 0));

        // ===== Top: búsqueda =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        searchPanel.setOpaque(false);

        txtRucSearch = new JTextField(18);
        btnBuscar = new CustomButton("Buscar", "#4A90E2");

        searchPanel.add(new JLabel("RUC del cliente:"));
        searchPanel.add(txtRucSearch);
        searchPanel.add(btnBuscar);

        lblInline = new JLabel(" ");
        lblInline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblInline.setForeground(new Color(120, 120, 120));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(searchPanel);
        top.add(Box.createVerticalStrut(6));
        top.add(lblInline);

        // ===== Center: formulario =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        txtRuc = new JTextField();
        txtRuc.setEditable(false);      // ✅ NO se edita
        txtRuc.setEnabled(false);       // ✅ visualmente bloqueado (gris)
        txtRuc.setDisabledTextColor(new Color(60, 60, 60)); // que se vea legible

        txtRazonSocial = new JTextField();
        txtDireccion = new JTextField();
        txtRepresentante = new JTextField();
        txtTelefono = new JTextField();
        txtCorreo = new JTextField();

        cmbSector = new JComboBox<>(new String[]{
                "Seleccione", "Comercial", "Industrial", "Servicios", "Tecnologico", "Otro"
        });

        cmbTamano = new JComboBox<>(new String[]{
                "Seleccione", "Microempresa", "Pequena", "Mediana", "Grande"
        });

        cmbEstado = new JComboBox<>(new String[]{
                "Activo", "Inactivo"
        });

        dcInicioContrato = new JDateChooser();
        dcInicioContrato.setDateFormatString("dd/MM/yyyy");

        dcFinContrato = new JDateChooser();
        dcFinContrato.setDateFormatString("dd/MM/yyyy");

        int row = 0;
        row = addRow(form, gc, row, "RUC:", txtRuc);
        row = addRow(form, gc, row, "Razón social:", txtRazonSocial);
        row = addRow(form, gc, row, "Dirección:", txtDireccion);
        row = addRow(form, gc, row, "Representante legal:", txtRepresentante);
        row = addRow(form, gc, row, "Teléfono:", txtTelefono);
        row = addRow(form, gc, row, "Correo:", txtCorreo);
        row = addRow(form, gc, row, "Sector:", cmbSector);
        row = addRow(form, gc, row, "Tamaño:", cmbTamano);
        row = addRow(form, gc, row, "Estado:", cmbEstado);
        row = addRow(form, gc, row, "Inicio contrato:", dcInicioContrato);
        row = addRow(form, gc, row, "Fin contrato:", dcFinContrato);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);

        // ===== Bottom: botones =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        btnGuardar = new CustomButton("Guardar cambios", "#4A90E2");
        btnCancelar = new CustomButton("Cancelar", "#9E9E9E");
        btnVolver = new CustomButton("Volver", "#9E9E9E");

        buttons.add(btnCancelar);
        buttons.add(btnGuardar);
        buttons.add(btnVolver);

        card.add(title, BorderLayout.NORTH);
        card.add(top, BorderLayout.BEFORE_FIRST_LINE);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);

        // ===== Events =====
        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> clearForm());
        btnVolver.addActionListener(e -> {
            clearForm();
            ViewRouter.show("clients");
        });

        setFormEnabled(false);
        setInlineMessage("Ingrese el RUC y presione Buscar.", false);
    }

    private void onBuscar() {
        clearForm();

        String ruc = txtRucSearch.getText() == null ? "" : txtRucSearch.getText().trim();
        if (!isValidRuc(ruc)) {
            setInlineMessage("RUC inválido. Debe tener 13 dígitos numéricos.", true);
            return;
        }

        try {
            ClienteInfoDTO c = clienteRepo.findByRuc(ruc);
            if (c == null) {
                setFormEnabled(false);
                setInlineMessage("RUC de cliente no válido", true);
                return;
            }

            clienteIdLoaded = c.clienteId;

            txtRuc.setText(nvl(c.ruc)); // ✅ solo lectura
            txtRazonSocial.setText(nvl(c.razonSocial));
            txtDireccion.setText(nvl(c.direccion));
            txtRepresentante.setText(nvl(c.representanteLegal));
            txtTelefono.setText(nvl(c.telefono));
            txtCorreo.setText(nvl(c.correo));

            selectCombo(cmbSector, c.sector);
            selectCombo(cmbTamano, c.tamano);
            selectCombo(cmbEstado, c.estado);

            dcInicioContrato.setDate(toDate(c.fechaInicioContrato));
            dcFinContrato.setDate(toDate(c.fechaFinContrato));

            setFormEnabled(true);
            setInlineMessage("Cliente encontrado. Edite los campos y presione Guardar cambios.", false);

        } catch (Exception ex) {
            setFormEnabled(false);
            setInlineMessage("Error buscando cliente: " + safeMsg(ex), true);
        }
    }


    private void onGuardar() {
        // NOTA: aquí solo validamos y llamas al update que ya vas a implementar en ClienteRepository
        if (clienteIdLoaded == null) {
            setInlineMessage("Primero busque un cliente válido.", true);
            return;
        }

        String razon = text(txtRazonSocial);
        String dir = text(txtDireccion);
        String rep = text(txtRepresentante);
        String tel = text(txtTelefono);
        String correo = text(txtCorreo);

        String sector = String.valueOf(cmbSector.getSelectedItem());
        String tamano = String.valueOf(cmbTamano.getSelectedItem());
        String estado = String.valueOf(cmbEstado.getSelectedItem());

        if (razon.isBlank()) {
            setInlineMessage("Error en campo: Razón social (obligatorio).", true);
            return;
        }
        if ("Seleccione".equalsIgnoreCase(sector)) {
            setInlineMessage("Error en campo: Sector (seleccione un valor).", true);
            return;
        }
        if ("Seleccione".equalsIgnoreCase(tamano)) {
            setInlineMessage("Error en campo: Tamaño (seleccione un valor).", true);
            return;
        }

        LocalDate ini = toLocalDate(dcInicioContrato.getDate());
        LocalDate fin = toLocalDate(dcFinContrato.getDate());

        if (ini == null) {
            setInlineMessage("Error en campo: Inicio contrato (obligatorio).", true);
            return;
        }
        if (fin == null) {
            setInlineMessage("Error en campo: Fin contrato (obligatorio).", true);
            return;
        }
        if (!fin.isAfter(ini)) {
            setInlineMessage("Error en campo: Fin contrato (debe ser mayor a Inicio contrato).", true);
            return;
        }

        try {
            // ✅ Si todavía no tienes update en el repo, dímelo y te doy ClienteRepository completo con update()
            clienteRepo.updateById(
                    clienteIdLoaded,
                    razon,
                    dir.isBlank() ? null : dir,
                    rep.isBlank() ? null : rep,
                    tel.isBlank() ? null : tel,
                    correo.isBlank() ? null : correo,
                    sector,
                    tamano,
                    estado,
                    ini,
                    fin
            );

            setInlineMessage("Cliente actualizado correctamente.", false);

        } catch (Exception ex) {
            setInlineMessage("No se pudo actualizar: " + safeMsg(ex), true);
        }
    }

    private void clearForm() {
        clienteIdLoaded = null;

        txtRuc.setText("");
        txtRazonSocial.setText("");
        txtDireccion.setText("");
        txtRepresentante.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");

        cmbSector.setSelectedIndex(0);
        cmbTamano.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);

        dcInicioContrato.setDate(null);
        dcFinContrato.setDate(null);

        setFormEnabled(false);
        txtRucSearch.requestFocusInWindow();
    }

    private void setFormEnabled(boolean enabled) {
        // RUC siempre bloqueado
        txtRuc.setEnabled(false);

        txtRazonSocial.setEnabled(enabled);
        txtDireccion.setEnabled(enabled);
        txtRepresentante.setEnabled(enabled);
        txtTelefono.setEnabled(enabled);
        txtCorreo.setEnabled(enabled);

        cmbSector.setEnabled(enabled);
        cmbTamano.setEnabled(enabled);
        cmbEstado.setEnabled(enabled);

        dcInicioContrato.setEnabled(enabled);
        dcFinContrato.setEnabled(enabled);

        btnGuardar.setEnabled(enabled);
        btnCancelar.setEnabled(enabled);
    }

    private void setInlineMessage(String msg, boolean isError) {
        lblInline.setText(msg == null || msg.isBlank() ? " " : msg);
        lblInline.setForeground(isError ? new Color(180, 0, 0) : new Color(0, 120, 0));
    }

    private static int addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridy = row;

        gc.gridx = 0;
        gc.weightx = 0.35;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(l, gc);

        gc.gridx = 1;
        gc.weightx = 0.65;
        p.add(field, gc);

        return row + 1;
    }

    private static void selectCombo(JComboBox<String> cmb, String value) {
        if (value == null) {
            cmb.setSelectedIndex(0);
            return;
        }
        String v = value.trim();
        for (int i = 0; i < cmb.getItemCount(); i++) {
            String it = cmb.getItemAt(i);
            if (it != null && it.equalsIgnoreCase(v)) {
                cmb.setSelectedIndex(i);
                return;
            }
        }
        cmb.setSelectedIndex(0);
    }

    private static boolean isValidRuc(String ruc) {
        if (ruc == null) return false;
        String x = ruc.trim();
        if (x.length() != 13) return false;
        for (int i = 0; i < x.length(); i++) {
            if (!Character.isDigit(x.charAt(i))) return false;
        }
        return true;
    }

    private static String text(JTextField t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static LocalDate toLocalDate(Date d) {
        if (d == null) return null;
        return java.time.Instant.ofEpochMilli(d.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toDate(LocalDate d) {
        if (d == null) return null;
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
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
