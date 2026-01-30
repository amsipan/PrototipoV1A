package secsys.views.dashboard;

import com.toedter.calendar.JDateChooser;
import secsys.dto.CalendarActivityDTO;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;
import secsys.views.addons.SidebarPanel;
import secsys.views.addons.RoundedPanel;
import secsys.views.planning.RepoFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class DashboardPanel extends JPanel {

    private final SidebarPanel sidebar;
    private final ClienteRepository clienteRepo;
    private final PlanningRepository planningRepo;

    private JLabel lblWeekRange;
    private JPanel calendarGrid;
    private JScrollPane scroll;

    private LocalDate weekStart; // Monday
    private static final int START_HOUR = 8;
    private static final int END_HOUR = 19; // 8..18

    private ImageIcon menu;
    private ImageIcon left;
    private ImageIcon right;

    public DashboardPanel(boolean showAudit, boolean showAdmin, boolean showPlatforms) {

        this.clienteRepo = RepoFactory.clienteRepository();
        this.planningRepo = RepoFactory.planningRepository();

        menu = new ImageIcon("src\\secsys\\resources\\category.png");
        left = new ImageIcon("src\\secsys\\resources\\chevron-left.png");
        right = new ImageIcon("src\\secsys\\resources\\chevron-right.png");

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftHeader.setOpaque(false);

        JLabel title = new JLabel("Calendario");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JButton btnMenu = new JButton();
        btnMenu.setFocusPainted(false);
        btnMenu.setBorderPainted(false);
        btnMenu.setBackground(Color.WHITE);
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btnMenu.setIcon(menu);

        leftHeader.add(btnMenu);
        leftHeader.add(title);

        sidebar = new SidebarPanel(showAudit, showAdmin, showPlatforms);
        sidebar.setVisible(false);

        btnMenu.addActionListener(e -> {
            sidebar.setVisible(!sidebar.isVisible());
            sidebar.getParent().revalidate();
            sidebar.getParent().repaint();
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);

        CustomButton btnPrev = new CustomButton(left, "#4A90E2");
        btnPrev.setPreferredSize(new Dimension(70, 40));

        CustomButton btnSalir = new CustomButton("Cerrar Sesión", "#24282c");
        btnSalir.setPreferredSize(new Dimension(150, 40));

        CustomButton btnToday = new CustomButton("Hoy", "#4A90E2");
        btnToday.setPreferredSize(new Dimension(100, 40));

        CustomButton btnNext = new CustomButton(right, "#4A90E2");
        btnNext.setPreferredSize(new Dimension(70, 40));

        btnPrev.addActionListener(e -> { weekStart = weekStart.minusWeeks(1); refreshCalendar(); });
        btnToday.addActionListener(e -> { weekStart = mondayOf(LocalDate.now()); refreshCalendar(); });
        btnNext.addActionListener(e -> { weekStart = weekStart.plusWeeks(1); refreshCalendar(); });
        btnSalir.addActionListener(e -> {ViewRouter.show("login");});

        CustomButton btnAdd = new CustomButton("Agregar actividad", "#4A90E2");
        btnAdd.addActionListener(e -> openAddActivityDialog());

        lblWeekRange = new JLabel("");
        lblWeekRange.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblWeekRange.setForeground(new Color(90, 90, 90));

        rightHeader.add(btnPrev);
        rightHeader.add(btnToday);
        rightHeader.add(btnNext);
        rightHeader.add(lblWeekRange);
        rightHeader.add(btnAdd);
        rightHeader.add(btnSalir);

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== MAIN =====
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.add(sidebar, BorderLayout.WEST);

        calendarGrid = new JPanel();
        calendarGrid.setOpaque(false);

        scroll = new JScrollPane(calendarGrid);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        mainPanel.add(scroll, BorderLayout.CENTER);
        add(mainPanel, BorderLayout.CENTER);

        weekStart = mondayOf(LocalDate.now());
        refreshCalendar();
    }

    private void refreshCalendar() {
        calendarGrid.removeAll();

        // ✅ Solo L-V (sin Sáb/Dom)
        LocalDate weekEnd = weekStart.plusDays(4);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblWeekRange.setText(df.format(weekStart) + " - " + df.format(weekEnd));

        calendarGrid.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(4, 4, 4, 4);

        // Header
        gc.gridy = 0;
        gc.gridx = 0;
        gc.weightx = 0.2;
        calendarGrid.add(makeHeaderCell(""), gc);

        String[] days = {"Lun", "Mar", "Mié", "Jue", "Vie"};
        for (int d = 0; d < 5; d++) {
            gc.gridx = d + 1;
            gc.weightx = 1.0;
            LocalDate date = weekStart.plusDays(d);
            calendarGrid.add(makeHeaderCell(days[d] + " " + date.format(df)), gc);
        }

        JPanel[][] cells = new JPanel[(END_HOUR - START_HOUR)][5];

        for (int h = START_HOUR; h < END_HOUR; h++) {
            int row = (h - START_HOUR) + 1;
            gc.gridy = row;

            gc.gridx = 0;
            gc.weightx = 0.2;
            calendarGrid.add(makeTimeCell(String.format("%02d:00", h)), gc);

            for (int d = 0; d < 5; d++) {
                gc.gridx = d + 1;
                gc.weightx = 1.0;

                JPanel cell = new JPanel();
                cell.setOpaque(true);
                cell.setBackground(Color.WHITE);
                cell.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
                cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));

                cells[h - START_HOUR][d] = cell;
                calendarGrid.add(cell, gc);
            }
        }

        // BD
        try {
            ZoneId zone = ZoneId.systemDefault();
            OffsetDateTime from = weekStart.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime to = weekStart.plusDays(5).atStartOfDay(zone).toOffsetDateTime();

            List<CalendarActivityDTO> acts = planningRepo.findCalendarActivitiesBetween(from, to);

            for (CalendarActivityDTO a : acts) {
                if (a == null || a.fechaInicio == null) continue;

                LocalDate day = a.fechaInicio.atZoneSameInstant(zone).toLocalDate();
                int dow = day.getDayOfWeek().getValue(); // Mon=1..Sun=7
                if (dow < 1 || dow > 5) continue;

                int dayIdx = dow - 1;
                int hour = a.fechaInicio.atZoneSameInstant(zone).getHour();
                int targetHour = clamp(hour, START_HOUR, END_HOUR - 1);

                JPanel target = cells[targetHour - START_HOUR][dayIdx];
                if (target != null) target.add(makeActivityChip(a));
            }

        } catch (Exception ex) {
            gc.gridx = 0;
            gc.gridy = (END_HOUR - START_HOUR) + 2;
            gc.gridwidth = 6;
            JLabel err = new JLabel("Error cargando actividades: " + safeMsg(ex));
            err.setForeground(new Color(180, 0, 0));
            calendarGrid.add(err, gc);
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private JComponent makeHeaderCell(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(60, 60, 60));
        p.add(l, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(100, 34));
        return p;
    }

    private JComponent makeTimeCell(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(new Color(248, 248, 248));
        p.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(80, 80, 80));
        p.add(l, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(70, 64));
        return p;
    }

    private JComponent makeActivityChip(CalendarActivityDTO a) {
        JPanel chip = new JPanel(new BorderLayout(6, 0));
        chip.setOpaque(true);
        chip.setBackground(colorOrDefault(a.colorHex, new Color(74, 144, 226)));
        chip.setBorder(new EmptyBorder(6, 8, 6, 8));
        chip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        ZoneId zone = ZoneId.systemDefault();
        String time = "-";
        if (a.fechaInicio != null && a.fechaFin != null) {
            LocalTime ini = a.fechaInicio.atZoneSameInstant(zone).toLocalTime();
            LocalTime fin = a.fechaFin.atZoneSameInstant(zone).toLocalTime();
            time = String.format("%02d:%02d-%02d:%02d", ini.getHour(), ini.getMinute(), fin.getHour(), fin.getMinute());
        }

        JLabel left = new JLabel("<html><b>" + esc(time) + "</b> " + esc(nvl(a.actividad)) + "</html>");
        left.setForeground(Color.WHITE);
        left.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel right = new JLabel(esc(nvl(a.estado)));
        right.setForeground(Color.WHITE);
        right.setFont(new Font("Segoe UI", Font.BOLD, 11));

        chip.add(left, BorderLayout.CENTER);
        chip.add(right, BorderLayout.EAST);

        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                openChangeEstadoDialog(a);
            }
        });

        return chip;
    }

    private void openChangeEstadoDialog(CalendarActivityDTO a) {
        if (a == null || a.actividadId == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Cambiar estado", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 220);
        dlg.setLocationRelativeTo(this);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel lbl = new JLabel("Actividad: " + nvl(a.actividad));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel mid = new JPanel(new GridLayout(2, 1, 8, 8));
        mid.setOpaque(false);

        JComboBox<String> cmb = new JComboBox<>(new String[]{
                "Activa", "Pendiente", "En proceso", "Completado", "Cancelada"
        });
        cmb.setEditable(true);
        if (a.estado != null) cmb.setSelectedItem(a.estado);

        mid.add(new JLabel("Nuevo estado:"));
        mid.add(cmb);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancelar");
        JButton btnSave = new JButton("Guardar");

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String nuevo = String.valueOf(cmb.getSelectedItem()).trim();
            if (nuevo.isBlank()) return;
            try {
                planningRepo.updateActivityEstado(a.actividadId, nuevo);
                dlg.dispose();
                refreshCalendar();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar: " + safeMsg(ex), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttons.add(btnCancel);
        buttons.add(btnSave);

        root.add(lbl, BorderLayout.NORTH);
        root.add(mid, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    private void openAddActivityDialog() {
        AddActivityDialog dlg = new AddActivityDialog(
                SwingUtilities.getWindowAncestor(this),
                clienteRepo,
                planningRepo,
                this::refreshCalendar
        );
        dlg.setVisible(true);
    }

    private static LocalDate mondayOf(LocalDate d) {
        return d.with(DayOfWeek.MONDAY);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String safeMsg(Exception ex) {
        String m = ex.getMessage();
        if (m == null || m.isBlank()) m = ex.getClass().getSimpleName();
        return m;
    }

    private static Color colorOrDefault(String hex, Color def) {
        try {
            if (hex == null) return def;
            String h = hex.trim();
            if (!h.startsWith("#") || h.length() != 7) return def;
            int r = Integer.parseInt(h.substring(1, 3), 16);
            int g = Integer.parseInt(h.substring(3, 5), 16);
            int b = Integer.parseInt(h.substring(5, 7), 16);
            return new Color(r, g, b);
        } catch (Exception e) {
            return def;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // =========================
    // DIALOGO CUSTOM (REDONDEADO)
    // =========================
    private static class AddActivityDialog extends JDialog {
        private final ClienteRepository clienteRepo;
        private final PlanningRepository planningRepo;
        private final Runnable onSaved;

        // Search (Razón social)
        private JTextField txtRazonSocial;
        private CustomButton btnBuscar;
        private JLabel lblClienteSeleccionado; // muestra la razón social elegida
        private JLabel lblInline;

        // Form
        private JTextField txtActividad;
        private JTextArea txtDescripcion;

        private JDateChooser dateChooser;
        private JSpinner spHour, spMinute, spDurHour, spDurMin;
        private JComboBox<String> cmbEstado;

        // Estado
        private UUID clienteId;

        AddActivityDialog(Window owner, ClienteRepository clienteRepo, PlanningRepository planningRepo, Runnable onSaved) {
            super(owner, "Agregar actividad", ModalityType.APPLICATION_MODAL);
            this.clienteRepo = clienteRepo;
            this.planningRepo = planningRepo;
            this.onSaved = onSaved;

            // ✅ “usar toda la ventana”
            setSize(820, 560);
            setLocationRelativeTo(owner);
            setResizable(false);

            buildUI();
        }

        private void buildUI() {
            // Fondo del dialog
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(245, 247, 250));
            root.setBorder(new EmptyBorder(14, 14, 14, 14));

            // ✅ Card que ocupa TODO el dialog (no centrado pequeño)
            RoundedPanel card = new RoundedPanel(22);
            card.setBackground(Color.WHITE);
            card.setLayout(new BorderLayout(12, 12));
            card.setBorder(new EmptyBorder(18, 18, 18, 18));

            // ===== Header =====
            JPanel header = new JPanel();
            header.setOpaque(false);
            header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

            JLabel title = new JLabel("Agregar actividad");
            title.setFont(new Font("Segoe UI", Font.BOLD, 22));
            title.setForeground(new Color(35, 35, 35));

            JLabel subtitle = new JLabel("Complete los datos y guarde la actividad.");
            subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            subtitle.setForeground(new Color(110, 110, 110));

            header.add(title);
            header.add(Box.createVerticalStrut(4));
            header.add(subtitle);

            // ===== Top: búsqueda por razón social =====
            JPanel searchWrap = new JPanel(new GridBagLayout());
            searchWrap.setOpaque(false);
            searchWrap.setBorder(new EmptyBorder(8, 0, 8, 0));

            GridBagConstraints sc = new GridBagConstraints();
            sc.insets = new Insets(6, 6, 6, 6);
            sc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblRS = new JLabel("Razón social:");
            lblRS.setFont(new Font("Segoe UI", Font.BOLD, 12));

            txtRazonSocial = new JTextField();
            txtRazonSocial.setPreferredSize(new Dimension(380, 32));

            btnBuscar = new CustomButton("Buscar", "#4A90E2");
            btnBuscar.setPreferredSize(new Dimension(140, 38));

            lblClienteSeleccionado = new JLabel("Cliente seleccionado: -");
            lblClienteSeleccionado.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblClienteSeleccionado.setForeground(new Color(60, 60, 60));

            lblInline = new JLabel(" ");
            lblInline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblInline.setForeground(new Color(120, 120, 120));

            sc.gridx = 0; sc.gridy = 0; sc.weightx = 0.0;
            searchWrap.add(lblRS, sc);

            sc.gridx = 1; sc.gridy = 0; sc.weightx = 1.0;
            searchWrap.add(txtRazonSocial, sc);

            sc.gridx = 2; sc.gridy = 0; sc.weightx = 0.0;
            searchWrap.add(btnBuscar, sc);

            sc.gridx = 0; sc.gridy = 1; sc.gridwidth = 3; sc.weightx = 1.0;
            searchWrap.add(lblClienteSeleccionado, sc);

            sc.gridx = 0; sc.gridy = 2; sc.gridwidth = 3;
            searchWrap.add(lblInline, sc);

            // ===== Form (dos columnas y más ancho) =====
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(10, 10, 10, 10);
            gc.fill = GridBagConstraints.HORIZONTAL;

            txtActividad = new JTextField();
            txtDescripcion = new JTextArea(4, 20);
            txtDescripcion.setLineWrap(true);
            txtDescripcion.setWrapStyleWord(true);
            JScrollPane spDesc = new JScrollPane(txtDescripcion);
            spDesc.setPreferredSize(new Dimension(10, 120));

            dateChooser = new JDateChooser();
            dateChooser.setDate(java.sql.Date.valueOf(LocalDate.now()));
            dateChooser.setDateFormatString("dd/MM/yyyy");

            spHour = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
            spMinute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            timePanel.setOpaque(false);
            timePanel.add(spHour);
            timePanel.add(new JLabel(":"));
            timePanel.add(spMinute);

            spDurHour = new JSpinner(new SpinnerNumberModel(1, 0, 12, 1));
            spDurMin = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
            JPanel durPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            durPanel.setOpaque(false);
            durPanel.add(spDurHour);
            durPanel.add(new JLabel("h"));
            durPanel.add(spDurMin);
            durPanel.add(new JLabel("m"));

            cmbEstado = new JComboBox<>(new String[]{"Activa", "Pendiente", "En proceso", "Completado", "Cancelada"});
            cmbEstado.setEditable(true);

            int row = 0;
            row = addRow(form, gc, row, "Actividad:", txtActividad);
            row = addRow(form, gc, row, "Descripción:", spDesc);
            row = addRow(form, gc, row, "Fecha:", dateChooser);
            row = addRow(form, gc, row, "Hora inicio:", timePanel);
            row = addRow(form, gc, row, "Duración:", durPanel);
            row = addRow(form, gc, row, "Estado:", cmbEstado);

            // ===== Buttons =====
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);

            CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
            btnCancel.setPreferredSize(new Dimension(140, 40));

            CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");
            btnSave.setPreferredSize(new Dimension(160, 40));

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> onSave());
            btnBuscar.addActionListener(e -> onBuscarClientePorRazonSocial());

            buttons.add(btnCancel);
            buttons.add(btnSave);

            // Center wrapper para dejar scroll si luego agregas más campos
            JPanel center = new JPanel(new BorderLayout(0, 8));
            center.setOpaque(false);
            center.add(searchWrap, BorderLayout.NORTH);
            center.add(form, BorderLayout.CENTER);

            card.add(header, BorderLayout.NORTH);
            card.add(center, BorderLayout.CENTER);
            card.add(buttons, BorderLayout.SOUTH);

            root.add(card, BorderLayout.CENTER);
            setContentPane(root);

            setInline("Ingrese la razón social y presione Buscar.", false);
        }

        // ✅ LIKE/ILIKE ignorando mayúsculas/minúsculas, y selector custom si hay varios
        private void onBuscarClientePorRazonSocial() {
            clienteId = null;
            lblClienteSeleccionado.setText("Cliente seleccionado: -");

            String razon = txtRazonSocial.getText() == null ? "" : txtRazonSocial.getText().trim();
            if (razon.isBlank()) {
                setInline("Ingrese la razón social del cliente.", true);
                return;
            }

            try {
                // Este método lo agregas en ClienteRepository (abajo te dejo el código)
                List<secsys.dto.ClienteBasicDTO> matches = clienteRepo.findBasicByRazonSocialLikeIgnoreCase(razon);

                if (matches == null || matches.isEmpty()) {
                    setInline("No se encontraron clientes con esa razón social.", true);
                    return;
                }

                secsys.dto.ClienteBasicDTO selected;
                if (matches.size() == 1) {
                    selected = matches.get(0);
                } else {
                    selected = showClientePicker(matches);
                    if (selected == null) {
                        setInline("Selección cancelada.", true);
                        return;
                    }
                }

                clienteId = selected.clienteId;
                lblClienteSeleccionado.setText("Cliente seleccionado: " + nvl(selected.razonSocial));
                setInline("Cliente encontrado.", false);

            } catch (Exception ex) {
                setInline("Error buscando cliente: " + safeMsg(ex), true);
            }
        }

        // Selector custom (RoundedPanel + CustomButton)
        private secsys.dto.ClienteBasicDTO showClientePicker(List<secsys.dto.ClienteBasicDTO> matches) {
            JDialog dlg = new JDialog(this, "Seleccionar cliente", ModalityType.APPLICATION_MODAL);
            dlg.setSize(560, 420);
            dlg.setLocationRelativeTo(this);
            dlg.setResizable(false);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(245, 247, 250));
            root.setBorder(new EmptyBorder(12, 12, 12, 12));

            RoundedPanel card = new RoundedPanel(20);
            card.setBackground(Color.WHITE);
            card.setLayout(new BorderLayout(10, 10));
            card.setBorder(new EmptyBorder(14, 14, 14, 14));

            JLabel t = new JLabel("Se encontraron " + matches.size() + " clientes");
            t.setFont(new Font("Segoe UI", Font.BOLD, 16));

            JLabel s = new JLabel("Seleccione uno para continuar.");
            s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            s.setForeground(new Color(110, 110, 110));

            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
            top.add(t);
            top.add(Box.createVerticalStrut(4));
            top.add(s);

            DefaultListModel<secsys.dto.ClienteBasicDTO> model = new DefaultListModel<>();
            for (var c : matches) model.addElement(c);

            JList<secsys.dto.ClienteBasicDTO> list = new JList<>(model);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            list.setCellRenderer((jl, value, index, isSelected, cellHasFocus) -> {
                JLabel lbl = new JLabel(nvl(value == null ? null : value.razonSocial));
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(8, 10, 8, 10));
                lbl.setBackground(isSelected ? new Color(230, 240, 255) : Color.WHITE);
                lbl.setForeground(new Color(40, 40, 40));
                return lbl;
            });

            list.setSelectedIndex(0);

            JScrollPane sp = new JScrollPane(list);
            sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);

            CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
            btnCancel.setPreferredSize(new Dimension(120, 38));

            CustomButton btnOk = new CustomButton("Seleccionar", "#4A90E2");
            btnOk.setPreferredSize(new Dimension(140, 38));

            final secsys.dto.ClienteBasicDTO[] selected = new secsys.dto.ClienteBasicDTO[1];

            btnCancel.addActionListener(e -> {
                selected[0] = null;
                dlg.dispose();
            });

            btnOk.addActionListener(e -> {
                selected[0] = list.getSelectedValue();
                dlg.dispose();
            });

            // doble click selecciona
            list.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        selected[0] = list.getSelectedValue();
                        dlg.dispose();
                    }
                }
            });

            buttons.add(btnCancel);
            buttons.add(btnOk);

            card.add(top, BorderLayout.NORTH);
            card.add(sp, BorderLayout.CENTER);
            card.add(buttons, BorderLayout.SOUTH);

            root.add(card, BorderLayout.CENTER);
            dlg.setContentPane(root);
            dlg.setVisible(true);

            return selected[0];
        }

        private void onSave() {
            if (clienteId == null) { setInline("Debe buscar un cliente válido antes de guardar.", true); return; }

            String act = txtActividad.getText() == null ? "" : txtActividad.getText().trim();
            if (act.isBlank()) { setInline("El campo 'Actividad' es obligatorio.", true); return; }

            java.util.Date date = dateChooser.getDate();
            if (date == null) { setInline("Seleccione una fecha.", true); return; }

            int h = (Integer) spHour.getValue();
            int m = (Integer) spMinute.getValue();
            int dh = (Integer) spDurHour.getValue();
            int dm = (Integer) spDurMin.getValue();
            if (dh == 0 && dm == 0) { setInline("La duración no puede ser 0.", true); return; }

            String estado = String.valueOf(cmbEstado.getSelectedItem()).trim();
            if (estado.isBlank()) { setInline("El estado es obligatorio.", true); return; }

            LocalDate selected = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDateTime iniLocal = selected.atTime(h, m);
            LocalDateTime finLocal = iniLocal.plusHours(dh).plusMinutes(dm);

            ZoneId zone = ZoneId.systemDefault();
            OffsetDateTime ini = iniLocal.atZone(zone).toOffsetDateTime();
            OffsetDateTime fin = finLocal.atZone(zone).toOffsetDateTime();

            String desc = txtDescripcion.getText();
            if (desc != null) desc = desc.trim();
            if (desc != null && desc.isBlank()) desc = null;

            try {
                planningRepo.insertManualActivityToActivePlan(clienteId, ini, fin, act, desc, estado);
                if (onSaved != null) onSaved.run();
                dispose();
            } catch (Exception ex) {
                setInline("No se pudo guardar: " + safeMsg(ex), true);
            }
        }

        private void setInline(String msg, boolean err) {
            lblInline.setText(msg == null || msg.isBlank() ? " " : msg);
            lblInline.setForeground(err ? new Color(180, 0, 0) : new Color(0, 120, 0));
        }

        private static int addRow(JPanel p, GridBagConstraints gc, int row, String label, JComponent field) {
            gc.gridy = row;

            gc.gridx = 0;
            gc.weightx = 0.25;
            JLabel l = new JLabel(label);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(new Color(60, 60, 60));
            p.add(l, gc);

            gc.gridx = 1;
            gc.weightx = 0.75;
            p.add(field, gc);

            return row + 1;
        }

        private static String nvl(String s) {
            return (s == null || s.isBlank()) ? "-" : s;
        }

        private static String safeMsg(Throwable t) {
            if (t == null) return "";
            String m = t.getMessage();
            return (m == null || m.isBlank()) ? t.getClass().getSimpleName() : m;
        }
    }

}
