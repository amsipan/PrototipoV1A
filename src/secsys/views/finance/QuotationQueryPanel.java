package secsys.views.finance;

import com.toedter.calendar.JDateChooser;
import secsys.AppSession;
import secsys.repository.QuotationRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.UUID;

public class QuotationQueryPanel extends JPanel {

    private final QuotationRepository repo = new QuotationRepository();

    // ====== IDs por fila (NO visibles) ======
    private final List<UUID> rowIds = new ArrayList<>();

    // ====== Última búsqueda (para refrescar después de cambiar estado) ======
    private enum LastSearchType { NONE, BY_RUC, BY_DATES }
    private LastSearchType lastSearchType = LastSearchType.NONE;
    private String lastRuc = null;
    private LocalDate lastInicio = null;
    private LocalDate lastFin = null;

    private JTextField txtRuc;
    private CustomButton btnBuscarRuc;

    private JDateChooser dcInicio;
    private JDateChooser dcFin;
    private CustomButton btnBuscarFechas;

    private JTable tbl;
    private DefaultTableModel model;

    // Columnas (SIN ID)
    private static final int COL_NUMERO = 0;
    private static final int COL_EMPRESA = 1;
    private static final int COL_RUC = 2;
    private static final int COL_ESTADO = 3;
    private static final int COL_SERVICIO = 4;
    private static final int COL_DESC = 5;
    private static final int COL_DESCUENTO = 6;
    private static final int COL_SUBTOTAL = 7;
    private static final int COL_IVA = 8;
    private static final int COL_TOTAL = 9;
    private static final int COL_ACTUALIZADO = 10;
    private static final int COL_ACCIONES = 11;

    public QuotationQueryPanel() {
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // ===== TOP: Título + botón volver =====
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel title = new JLabel("Consulta de Cotizaciones");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        CustomButton btnVolver = new CustomButton("Volver", "#9E9E9E");
        btnVolver.setPreferredSize(new Dimension(140, 38));
        btnVolver.addActionListener(e -> {
            // (opcional) limpiar última búsqueda / tabla al salir
            // model.setRowCount(0);
            // rowIds.clear();
            ViewRouter.show("finance");
        });

        top.add(title, BorderLayout.WEST);
        top.add(btnVolver, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("Por RUC", buildRucTab());
        tabs.addTab("Por Fechas", buildDatesTab());

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(tabs, BorderLayout.NORTH);
        left.setPreferredSize(new Dimension(360, 1));
        add(left, BorderLayout.WEST);

        add(buildTablePanel(), BorderLayout.CENTER);
    }

    private JPanel buildRucTab() {
        RoundedPanel panel = new RoundedPanel(18);
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(340, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel lbl = new JLabel("RUC del potencial cliente");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl, gbc);

        gbc.gridy++;
        txtRuc = new JTextField();
        txtRuc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtRuc.setToolTipText("13 dígitos");
        panel.add(txtRuc, gbc);

        gbc.gridy++;
        btnBuscarRuc = new CustomButton("Buscar", "#C8102C");
        btnBuscarRuc.addActionListener(e -> onSearchByRuc());
        panel.add(btnBuscarRuc, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildDatesTab() {
        RoundedPanel panel = new RoundedPanel(18);
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(340, 260));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel lbl1 = new JLabel("Fecha inicio (DD/MM/AAAA)");
        lbl1.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl1, gbc);

        gbc.gridy++;
        dcInicio = new JDateChooser();
        dcInicio.setDateFormatString("dd/MM/yyyy");
        panel.add(dcInicio, gbc);

        gbc.gridy++;
        JLabel lbl2 = new JLabel("Fecha fin (DD/MM/AAAA)");
        lbl2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lbl2, gbc);

        gbc.gridy++;
        dcFin = new JDateChooser();
        dcFin.setDateFormatString("dd/MM/yyyy");
        panel.add(dcFin, gbc);

        gbc.gridy++;
        btnBuscarFechas = new CustomButton("Filtrar", "#C8102C");
        btnBuscarFechas.addActionListener(e -> onSearchByDates());
        panel.add(btnBuscarFechas, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel buildTablePanel() {
        String[] cols = {
                "N°", "Empresa", "RUC", "Estado", "Servicio", "Descripción",
                "Descuento", "Subtotal", "IVA", "Total", "Actualizado", "Acciones"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col == COL_ACCIONES; // ✅ acciones editables
            }
        };

        tbl = new JTable(model);
        tbl.setRowHeight(34);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Acciones
        tbl.getColumnModel().getColumn(COL_ACCIONES).setCellRenderer(new ActionsRenderer());
        tbl.getColumnModel().getColumn(COL_ACCIONES).setCellEditor(new ActionsEditor());

        // Dale espacio a los botones
        tbl.getColumnModel().getColumn(COL_ACCIONES).setPreferredWidth(520);
        tbl.getColumnModel().getColumn(COL_ACCIONES).setMinWidth(520);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        // Scroll horizontal abajo
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ========= EVENTOS =========

    private void onSearchByRuc() {
        String ruc = (txtRuc.getText() == null) ? "" : txtRuc.getText().trim();

        if (ruc.isEmpty()) {
            showMsg("Error", "Ingrese el RUC del potencial cliente");
            return;
        }
        if (!ruc.matches("^\\d{13}$")) {
            showMsg("Error", "El RUC debe tener 13 dígitos");
            return;
        }

        try {
            showMsg("Éxito", "Cotizaciones encontradas");
            List<QuotationRepository.QuoteRow> rows = repo.listByPotentialRucDetailed(ruc);
            lastSearchType = LastSearchType.BY_RUC;
            lastRuc = ruc;
            lastInicio = null;
            lastFin = null;
            fillTable(rows);
        } catch (Exception ex) {
            showMsg("Error", "Error al consultar cotizaciones: " + ex.getMessage());
        }
    }

    private void onSearchByDates() {
        Date d1 = dcInicio.getDate();
        Date d2 = dcFin.getDate();

        if (d1 == null || d2 == null) {
            showMsg("Error", "Debe seleccionar la fecha de inicio y fin.");
            return;
        }

        LocalDate inicio = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fin = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (fin.isBefore(inicio)) {
            showMsg("Error", "La fecha de fin no puede ser mayor a la fecha de inicio");
            return;
        }

        try {
            List<QuotationRepository.QuoteRow> rows = repo.listByDateRangeDetailed(inicio, fin);
            lastSearchType = LastSearchType.BY_DATES;
            lastRuc = null;
            lastInicio = inicio;
            lastFin = fin;
            fillTable(rows);
        } catch (Exception ex) {
            showMsg("Error", "Error al filtrar cotizaciones: " + ex.getMessage());
        }
    }

    private void refreshLastSearch() {
        try {
            if (lastSearchType == LastSearchType.BY_RUC && lastRuc != null) {
                fillTable(repo.listByPotentialRucDetailed(lastRuc));
            } else if (lastSearchType == LastSearchType.BY_DATES && lastInicio != null && lastFin != null) {
                fillTable(repo.listByDateRangeDetailed(lastInicio, lastFin));
            }
        } catch (Exception ex) {
            showMsg("Error", "Error al refrescar: " + ex.getMessage());
        }
    }

    // ========= HELPERS =========

    private void fillTable(List<QuotationRepository.QuoteRow> rows) {
        model.setRowCount(0);
        rowIds.clear();

        if (rows == null || rows.isEmpty()) {
            showMsg("Información", "No se encontraron cotizaciones");
            return;
        }

        for (QuotationRepository.QuoteRow r : rows) {
            rowIds.add(r.cotizacionId);
            model.addRow(new Object[]{
                    (r.numero == null ? "-" : r.numero),
                    nullSafe(r.nombreEmpresa),
                    nullSafe(r.rucPotencial),
                    estadoUi(r.estado),
                    nullSafe(r.servicioPrincipal),
                    nullSafe(r.descripcionServicio),
                    fmtMoney(r.descuentoTotal),
                    fmtMoney(r.subtotalSinIva),
                    fmtMoney(r.ivaValor),
                    fmtMoney(r.total),
                    nullSafe(r.actualizadoEn),
                    "" // acciones
            });
        }
    }

    // BD: "Revision" -> UI: "Revisión"
    private String estadoUi(String estadoBd) {
        if (estadoBd == null) return "";
        String e = estadoBd.trim();
        if (e.equalsIgnoreCase("Revision")) return "Revisión";
        return e;
    }

    // UI: "Revisión" -> BD: "Revision"
    private String estadoBdFromUi(String estadoUi) {
        if (estadoUi == null) return "";
        String e = estadoUi.trim();
        if (e.equalsIgnoreCase("Revisión")) return "Revision";
        return e;
    }

    private boolean isEditableStateUi(String estadoUi) {
        String e = estadoBdFromUi(estadoUi);
        return e.equalsIgnoreCase("Borrador") || e.equalsIgnoreCase("Revision");
    }

    // ✅ usando AppSession.isOperative()
    private boolean canApproveReject() {
        // Si es operativo -> NO puede aceptar / rechazar
        return !AppSession.isOperative();
    }

    private String nullSafe(String s) {
        return (s == null) ? "" : s;
    }

    private String fmtMoney(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void showMsg(String title, String msg) {
        ActionMessageFrame.showMsg(this, title, msg);
    }

    // ========= Renderer / Editor =========

    private class ActionsPanel extends JPanel {
        JButton btnRevision = new JButton("En revisión");
        JButton btnAceptar = new JButton("Aceptar");
        JButton btnRechazar = new JButton("Rechazar");

        ActionsPanel(boolean forRenderer) {
            setOpaque(true);
            setLayout(new GridBagLayout());

            Font f = new Font("Segoe UI", Font.BOLD, 11);
            btnRevision.setFont(f);
            btnAceptar.setFont(f);
            btnRechazar.setFont(f);

            btnRevision.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnRechazar.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (forRenderer) {
                btnRevision.setFocusable(false);
                btnAceptar.setFocusable(false);
                btnRechazar.setFocusable(false);
            }

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.insets = new Insets(0, 4, 0, 4);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;

            gbc.gridx = 0; add(btnRevision, gbc);
            gbc.gridx = 1; add(btnAceptar, gbc);
            gbc.gridx = 2; add(btnRechazar, gbc);
        }
    }

    private class ActionsRenderer implements TableCellRenderer {
        private final ActionsPanel panel = new ActionsPanel(true);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                      boolean hasFocus, int row, int col) {

            String estadoUi = String.valueOf(table.getValueAt(row, COL_ESTADO));
            boolean editable = isEditableStateUi(estadoUi);

            // Mostrar acciones SOLO para Borrador / Revision
            panel.btnRevision.setVisible(editable);

            boolean canAR = editable && canApproveReject();
            panel.btnAceptar.setVisible(canAR);
            panel.btnRechazar.setVisible(canAR);

            // En revisión SOLO habilitado si es Borrador
            boolean isBorrador = estadoBdFromUi(estadoUi).equalsIgnoreCase("Borrador");
            panel.btnRevision.setEnabled(editable && isBorrador);

            panel.btnAceptar.setEnabled(canAR);
            panel.btnRechazar.setEnabled(canAR);

            panel.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return panel;
        }
    }

    private class ActionsEditor extends AbstractCellEditor implements TableCellEditor {
        private final ActionsPanel panel = new ActionsPanel(false);
        private int editingRow = -1;

        ActionsEditor() {
            panel.btnRevision.addActionListener(e -> onClickRevision());
            panel.btnAceptar.addActionListener(e -> onClickAceptar());
            panel.btnRechazar.addActionListener(e -> onClickRechazar());
        }

        @Override
        public boolean isCellEditable(EventObject e) {
            return true;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
                                                     int row, int column) {

            editingRow = row;

            String estadoUi = String.valueOf(table.getValueAt(row, COL_ESTADO));
            boolean editable = isEditableStateUi(estadoUi);

            panel.btnRevision.setVisible(editable);

            boolean canAR = editable && canApproveReject();
            panel.btnAceptar.setVisible(canAR);
            panel.btnRechazar.setVisible(canAR);

            boolean isBorrador = estadoBdFromUi(estadoUi).equalsIgnoreCase("Borrador");
            panel.btnRevision.setEnabled(editable && isBorrador);

            panel.btnAceptar.setEnabled(canAR);
            panel.btnRechazar.setEnabled(canAR);

            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        private void onClickRevision() {
            if (editingRow < 0) return;

            try {
                UUID cotId = rowIds.get(editingRow);
                String estadoUi = String.valueOf(tbl.getValueAt(editingRow, COL_ESTADO));
                String estadoBd = estadoBdFromUi(estadoUi);

                // Solo Borrador -> Revision
                if (!estadoBd.equalsIgnoreCase("Borrador")) {
                    stopCellEditing();
                    return;
                }

                boolean ok = repo.markQuotationAsRevision(cotId);
                stopCellEditing();

                if (!ok) {
                    showMsg("Error", "No se pudo actualizar el estado a Revision.");
                    return;
                }

                showMsg("Información", "Estado \"Revisión\" cambiado");
                refreshLastSearch();

            } catch (Exception ex) {
                stopCellEditing();
                showMsg("Error", "Error al actualizar estado: " + ex.getMessage());
            }
        }

        private void onClickAceptar() {
            if (editingRow < 0) return;

            if (!canApproveReject()) {
                stopCellEditing();
                showMsg("Error", "No tiene permisos para aceptar cotizaciones.");
                return;
            }

            try {
                UUID cotId = rowIds.get(editingRow);

                boolean ok = repo.markQuotationAsAccepted(cotId);
                stopCellEditing();

                if (!ok) {
                    showMsg("Error", "No se pudo actualizar el estado a Aceptada.");
                    return;
                }

                showMsg("Información", "Estado \"Aceptada\" cambiado");
                refreshLastSearch();

            } catch (Exception ex) {
                stopCellEditing();
                showMsg("Error", "Error al actualizar estado: " + ex.getMessage());
            }
        }

        private void onClickRechazar() {
            if (editingRow < 0) return;

            if (!canApproveReject()) {
                stopCellEditing();
                showMsg("Error", "No tiene permisos para rechazar cotizaciones.");
                return;
            }

            try {
                UUID cotId = rowIds.get(editingRow);

                boolean ok = repo.markQuotationAsRejected(cotId);
                stopCellEditing();

                if (!ok) {
                    showMsg("Error", "No se pudo actualizar el estado a Rechazada.");
                    return;
                }

                showMsg("Información", "Estado \"Rechazada\" cambiado");
                refreshLastSearch();

            } catch (Exception ex) {
                stopCellEditing();
                showMsg("Error", "Error al actualizar estado: " + ex.getMessage());
            }
        }
    }
}
