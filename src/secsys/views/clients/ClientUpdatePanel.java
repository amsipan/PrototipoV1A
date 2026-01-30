package secsys.views.clients;

import secsys.db.DbException;
import secsys.dto.ClienteInfoDTO;
import secsys.repository.ClienteRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.CustomSelectDialog;
import secsys.views.addons.RoundedPanel;
import secsys.views.planning.RepoFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.UUID;

public class ClientUpdatePanel extends JPanel {

    private Image background;

    private final ClienteRepository clienteRepo;

    // Search
    private JTextField txtRazonSearch;
    private JTextField txtRucSearch;
    private CustomButton btnBuscar;

    // Form fields
    private JTextField txtRuc;          // NO editable
    private JTextField txtRazonSocial;  // NO editable
    private JTextField txtSector;
    private JTextField txtTamano;
    private JTextField txtDireccion;
    private JTextField txtRepresentante;
    private JTextField txtTelefono;
    private JTextField txtCorreo;

    private JComboBox<String> cmbEstado;

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
        card.setPreferredSize(new Dimension(920, 600));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("Actualizar Cliente");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));

        // =========================
        // Panel búsqueda (GridBag alineado)
        // =========================
        JPanel searchWrap = new JPanel(new GridBagLayout());
        searchWrap.setOpaque(false);
        searchWrap.setBorder(new EmptyBorder(0, 0, 10, 0));

        GridBagConstraints sc = new GridBagConstraints();
        sc.insets = new Insets(6, 6, 6, 6);
        sc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblRazon = new JLabel("Razón social del cliente:");
        lblRazon.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel lblRuc = new JLabel("RUC:");
        lblRuc.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        txtRazonSearch = new JTextField(20);
        txtRucSearch = new JTextField(14);

        btnBuscar = new CustomButton("Buscar", "#4A90E2");
        btnBuscar.setPreferredSize(new Dimension(180, 42));

        sc.gridx = 0; sc.gridy = 0; sc.weightx = 0.0;
        searchWrap.add(lblRazon, sc);

        sc.gridx = 1; sc.gridy = 0; sc.weightx = 1.0;
        searchWrap.add(txtRazonSearch, sc);

        sc.gridx = 2; sc.gridy = 0; sc.weightx = 0.0;
        searchWrap.add(lblRuc, sc);

        sc.gridx = 3; sc.gridy = 0; sc.weightx = 0.6;
        searchWrap.add(txtRucSearch, sc);

        sc.gridx = 4; sc.gridy = 0; sc.weightx = 0.0;
        searchWrap.add(btnBuscar, sc);

        JLabel hint = new JLabel("Ingrese Razón social o RUC y presione Buscar.");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setBorder(new EmptyBorder(4, 6, 0, 0));
        hint.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(title);
        north.add(searchWrap);
        north.add(hint);

        // =========================
        // Formulario
        // =========================
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        txtRuc = new JTextField();
        txtRuc.setEditable(false);
        txtRuc.setEnabled(false);
        txtRuc.setDisabledTextColor(new Color(60, 60, 60));

        txtSector = new JTextField();
        txtSector.setEditable(false);
        txtSector.setEnabled(false);
        txtSector.setDisabledTextColor(new Color(60, 60, 60));

        txtRazonSocial = new JTextField();
        txtRazonSocial.setEditable(false);
        txtRazonSocial.setEnabled(false);
        txtRazonSocial.setDisabledTextColor(new Color(60, 60, 60));

        txtTamano = new JTextField();
        txtTamano.setEditable(false);
        txtTamano.setEnabled(false);
        txtTamano.setDisabledTextColor(new Color(60, 60, 60));

        txtDireccion = new JTextField();
        txtRepresentante = new JTextField();
        txtTelefono = new JTextField();
        txtCorreo = new JTextField();

        cmbEstado = new JComboBox<>(new String[]{"Activo", "Inactivo"});

        int row = 0;
        row = addRow(form, gc, row, "RUC:", txtRuc);
        row = addRow(form, gc, row, "Razón social:", txtRazonSocial);
        row = addRow(form, gc, row, "Dirección:", txtDireccion);
        row = addRow(form, gc, row, "Sector:", txtSector);
        row = addRow(form, gc, row, "Tamaño:", txtTamano);
        row = addRow(form, gc, row, "Representante legal:", txtRepresentante);
        row = addRow(form, gc, row, "Teléfono:", txtTelefono);
        row = addRow(form, gc, row, "Correo:", txtCorreo);
        row = addRow(form, gc, row, "Estado:", cmbEstado);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);

        // =========================
        // Botones
        // =========================
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        btnGuardar = new CustomButton("Guardar cambios", "#4A90E2");
        btnCancelar = new CustomButton("Cancelar", "#9E9E9E");
        btnVolver = new CustomButton("Volver", "#9E9E9E");

        buttons.add(btnCancelar);
        buttons.add(btnGuardar);
        buttons.add(btnVolver);

        card.add(north, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);

        // Eventos
        btnBuscar.addActionListener(e -> onBuscar());
        btnGuardar.addActionListener(e -> onGuardar());
        btnCancelar.addActionListener(e -> clearForm());
        btnVolver.addActionListener(e -> {
            clearForm();
            ViewRouter.show("clients");
        });

        setFormEnabled(false);
    }

    private void onBuscar() {
        clearFormOnlyFields();

        String razon = (txtRazonSearch.getText() == null) ? "" : txtRazonSearch.getText().trim();
        String ruc   = (txtRucSearch.getText() == null) ? "" : txtRucSearch.getText().trim();

        Window owner = SwingUtilities.getWindowAncestor(this);

        if (razon.isBlank() && ruc.isBlank()) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Ingrese Razón social o RUC.");
            setFormEnabled(false);
            return;
        }

        try {
            ClienteInfoDTO selected;

            // 1) Si hay RUC: exacto
            if (!ruc.isBlank()) {
                if (!ruc.matches("^\\d{13}$")) {
                    ActionMessageFrame.showMsg("Campos obligatorios", "El RUC debe tener 13 dígitos.");
                    setFormEnabled(false);
                    return;
                }

                selected = clienteRepo.findByRuc(ruc);
                if (selected == null) {
                    ActionMessageFrame.showMsg("Error", "No se encontró un cliente con ese RUC.");
                    setFormEnabled(false);
                    return;
                }

                loadCliente(selected);
                setFormEnabled(true);
                return;
            }

            // 2) Razón social: ILIKE
            List<ClienteInfoDTO> matches = clienteRepo.findByRazonSocialLikeIgnoreCase(razon);
            if (matches == null || matches.isEmpty()) {
                ActionMessageFrame.showMsg("Error", "No se encontró un cliente con esa Razón social.");
                setFormEnabled(false);
                return;
            }

            if (matches.size() == 1) {
                selected = matches.get(0);
            } else {
                String[] options = new String[matches.size()];
                for (int i = 0; i < matches.size(); i++) {
                    ClienteInfoDTO c = matches.get(i);
                    options[i] = (c.ruc == null ? "-" : c.ruc) + "  |  " +
                            (c.razonSocial == null ? "-" : c.razonSocial);
                }

                String pick = CustomSelectDialog.showSelect(
                        owner,
                        "Seleccionar cliente",
                        "Se encontraron " + matches.size() + " clientes.\nSeleccione uno:",
                        options
                );

                if (pick == null) {
                    setFormEnabled(false);
                    return;
                }

                int idx = 0;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(pick)) { idx = i; break; }
                }
                selected = matches.get(idx);
            }

            loadCliente(selected);
            setFormEnabled(true);

        } catch (DbException ex) {
            System.err.println("[CLIENT-UPDATE] Error buscando cliente:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo consultar el cliente.");
            setFormEnabled(false);
        } catch (Exception ex) {
            System.err.println("[CLIENT-UPDATE] Error inesperado buscando cliente:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo consultar el cliente.");
            setFormEnabled(false);
        }
    }

    private void onGuardar() {
        if (clienteIdLoaded == null) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Primero busque un cliente válido.");
            return;
        }

        String msg = validateFormMessage();
        if (msg != null) {
            ActionMessageFrame.showMsg("Campos obligatorios", msg);
            return;
        }

        String dir = text(txtDireccion);
        String rep = text(txtRepresentante);
        String tel = text(txtTelefono);
        String correo = text(txtCorreo);
        String estado = String.valueOf(cmbEstado.getSelectedItem());

        try {
            clienteRepo.updateById(
                    clienteIdLoaded,
                    dir.isBlank() ? null : dir,
                    rep.isBlank() ? null : rep,
                    tel.isBlank() ? null : tel,
                    correo.isBlank() ? null : correo,
                    estado
            );

            ActionMessageFrame.showMsg("Éxito", "Cliente actualizado correctamente.");

        } catch (DbException ex) {
            System.err.println("[CLIENT-UPDATE] Error actualizando cliente:");
            ex.printStackTrace();

            String human = mapDbErrorToHumanMessage(ex);
            ActionMessageFrame.showMsg("Error", human);

        } catch (Exception ex) {
            System.err.println("[CLIENT-UPDATE] Error inesperado actualizando cliente:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo actualizar el cliente. Intente nuevamente.");
        }
    }

    private void loadCliente(ClienteInfoDTO c) {
        if (c == null) return;

        clienteIdLoaded = c.clienteId;

        txtRuc.setText(nvl(c.ruc));
        txtRazonSocial.setText(nvl(c.razonSocial));
        txtDireccion.setText(nvl(c.direccion));
        txtRepresentante.setText(nvl(c.representanteLegal));

        // ✅ Sector: Tecnologico -> Tecnológico
        String sector = nvl(c.sector);
        if (sector.equalsIgnoreCase("Tecnologico")) {
            sector = "Tecnológico";
        }
        txtSector.setText(sector);

        // ✅ Tamaño: Pequena -> Pequeña
        String tamano = nvl(c.tamano);
        if (tamano.equalsIgnoreCase("Pequena")) {
            tamano = "Pequeña";
        }
        txtTamano.setText(tamano);

        txtTelefono.setText(nvl(c.telefono));
        txtCorreo.setText(nvl(c.correo));
        selectCombo(cmbEstado, c.estado);
    }


    private String validateFormMessage() {
        String dir = text(txtDireccion);
        String rep = text(txtRepresentante);
        String tel = text(txtTelefono);
        String email = text(txtCorreo);

        if (dir.isBlank() || dir.length() < 3) return "Dirección inválida (mínimo 3 caracteres).";
        if (rep.isBlank() || rep.length() < 3) return "Representante legal inválido (mínimo 3 caracteres";
        if (tel.isBlank() || !tel.matches("^[0-9]{10}$")) return "Teléfono inválido (10 dígitos).";
        if (email.isBlank() || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            return "Correo electrónico inválido.";

        return null;
    }

    private String mapDbErrorToHumanMessage(DbException ex) {
        String raw = safeMsg(ex).toLowerCase();

        if (raw.contains("d_email_check") || raw.contains("d_email")) {
            return "No se pudo actualizar: el correo no es válido.";
        }
        if (raw.contains("telefono") && raw.contains("check")) {
            return "No se pudo actualizar: el teléfono no es válido.";
        }
        if (raw.contains("representante") && raw.contains("check")) {
            return "No se pudo actualizar: el representante no es válido.";
        }
        if (raw.contains("direccion") && raw.contains("check")) {
            return "No se pudo actualizar: la dirección no es válida.";
        }

        return "No se pudo actualizar el cliente. Revise los datos e intente nuevamente.";
    }

    private void clearForm() {
        clienteIdLoaded = null;

        txtRuc.setText("");
        txtRazonSocial.setText("");
        txtDireccion.setText("");
        txtRepresentante.setText("");
        txtSector.setText("");
        txtTamano.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");

        cmbEstado.setSelectedIndex(0);

        setFormEnabled(false);
        txtRazonSearch.requestFocusInWindow();
    }

    private void clearFormOnlyFields() {
        clienteIdLoaded = null;

        txtRuc.setText("");
        txtRazonSocial.setText("");
        txtDireccion.setText("");
        txtRepresentante.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");

        cmbEstado.setSelectedIndex(0);
        setFormEnabled(false);
    }

    private void setFormEnabled(boolean enabled) {
        txtRuc.setEnabled(false);
        txtRazonSocial.setEnabled(false);

        txtDireccion.setEnabled(enabled);
        txtRepresentante.setEnabled(enabled);
        txtTelefono.setEnabled(enabled);
        txtCorreo.setEnabled(enabled);

        cmbEstado.setEnabled(enabled);

        btnGuardar.setEnabled(enabled);
        btnCancelar.setEnabled(enabled);
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

    private static String text(JTextField t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String safeMsg(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
