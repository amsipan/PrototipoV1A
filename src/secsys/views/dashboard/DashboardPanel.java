package secsys.views.dashboard;

import com.toedter.calendar.JDateChooser;
import secsys.dto.CalendarActivityDTO;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.views.addons.CustomButton;
import secsys.views.addons.SidebarPanel;
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

    // Header UI
    private JLabel lblWeekRange;

    // Calendar UI
    private JPanel calendarGrid;
    private JScrollPane scroll;

    // Week state
    private LocalDate weekStart; // Monday
    private static final int START_HOUR = 8;
    private static final int END_HOUR = 19; // 8..18 (11 filas)

    // ✅ Solo días laborables
    private static final int WORK_DAYS = 5;
    private static final String[] DAYS = {"Lun", "Mar", "Mié", "Jue", "Vie"};

    public DashboardPanel(boolean SHOW_AUDIT) {

        this.clienteRepo = RepoFactory.clienteRepository();
        this.planningRepo = RepoFactory.planningRepository();

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

        JButton btnMenu = new JButton("☰");
        btnMenu.setFocusPainted(false);
        btnMenu.setBorderPainted(false);
        btnMenu.setBackground(Color.WHITE);
        btnMenu.setFont(new Font("Segoe UI", Font.BOLD, 18));

        leftHeader.add(btnMenu);
        leftHeader.add(title);

        sidebar = new SidebarPanel(SHOW_AUDIT);
        sidebar.setVisible(false);

        btnMenu.addActionListener(e -> {
            sidebar.setVisible(!sidebar.isVisible());
            sidebar.getParent().revalidate();
            sidebar.getParent().repaint();
        });

        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightHeader.setOpaque(false);

        // ✅ Botones con tu clase CustomButton
        CustomButton btnPrev = new CustomButton("->", "#9E9E9E");
        CustomButton btnToday = new CustomButton("Hoy", "#9E9E9E");
        CustomButton btnNext = new CustomButton("<-", "#9E9E9E");

        btnPrev.addActionListener(e -> {
            weekStart = weekStart.minusWeeks(1);
            refreshCalendar();
        });
        btnToday.addActionListener(e -> {
            weekStart = mondayOf(LocalDate.now());
            refreshCalendar();
        });
        btnNext.addActionListener(e -> {
            weekStart = weekStart.plusWeeks(1);
            refreshCalendar();
        });

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

        header.add(leftHeader, BorderLayout.WEST);
        header.add(rightHeader, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ===== CONTENEDOR CENTRAL =====
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

        // ===== Inicialización semana actual =====
        weekStart = mondayOf(LocalDate.now());
        refreshCalendar();
    }

    // ===========================
    // Render calendar
    // ===========================
    private void refreshCalendar() {
        calendarGrid.removeAll();

        // ✅ Rango laboral: lunes a viernes
        LocalDate weekEnd = weekStart.plusDays(WORK_DAYS - 1);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblWeekRange.setText(df.format(weekStart) + " - " + df.format(weekEnd));

        // Grilla: 1 col hora + 5 cols dias (sin sábados ni domingos)
        calendarGrid.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 0;
        gc.insets = new Insets(4, 4, 4, 4);

        // Header row
        gc.gridy = 0;

        // esquina (vacía)
        gc.gridx = 0;
        gc.weightx = 0.2;
        calendarGrid.add(makeHeaderCell(""), gc);

        // días (L-V)
        for (int d = 0; d < WORK_DAYS; d++) {
            gc.gridx = d + 1;
            gc.weightx = 1.0;
            LocalDate date = weekStart.plusDays(d);
            calendarGrid.add(makeHeaderCell(DAYS[d] + " " + date.format(df)), gc);
        }

        // Body: horas
        JPanel[][] cells = new JPanel[(END_HOUR - START_HOUR)][WORK_DAYS];

        for (int h = START_HOUR; h < END_HOUR; h++) {
            int row = (h - START_HOUR) + 1;
            gc.gridy = row;

            // col hora
            gc.gridx = 0;
            gc.weightx = 0.2;
            calendarGrid.add(makeTimeCell(String.format("%02d:00", h)), gc);

            // col días (L-V)
            for (int d = 0; d < WORK_DAYS; d++) {
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

        // Cargar actividades desde BD (semanal completa, pero pintamos solo L-V)
        try {
            ZoneId zone = ZoneId.systemDefault();
            OffsetDateTime from = weekStart.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime to = weekStart.plusDays(7).atStartOfDay(zone).toOffsetDateTime();

            List<CalendarActivityDTO> acts = planningRepo.findCalendarActivitiesBetween(from, to);

            for (CalendarActivityDTO a : acts) {
                if (a == null || a.fechaInicio == null) continue;

                LocalDate day = a.fechaInicio.atZoneSameInstant(zone).toLocalDate();
                int dayIdx = (day.getDayOfWeek().getValue() + 6) % 7; // Mon=0..Sun=6

                // ✅ ignorar sábados(5) y domingos(6)
                if (dayIdx >= WORK_DAYS) continue;

                int hour = a.fechaInicio.atZoneSameInstant(zone).getHour();
                int targetHour = clamp(hour, START_HOUR, END_HOUR - 1);

                JPanel target = cells[targetHour - START_HOUR][dayIdx];
                if (target != null) {
                    target.add(makeActivityChip(a));
                }
            }

        } catch (Exception ex) {
            gc.gridx = 0;
            gc.gridy = (END_HOUR - START_HOUR) + 2;
            gc.gridwidth = 6; // 1 hora + 5 días
            gc.weightx = 1;
            gc.weighty = 0;
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
        p.setBackground(new Color(255, 255, 255));
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

        String tip = (a.razonSocial == null ? "" : a.razonSocial) +
                (a.version == null ? "" : (" | " + a.version)) +
                (a.descripcion == null ? "" : ("\n" + a.descripcion));
        chip.setToolTipText("<html>" + esc(tip).replace("\n", "<br>") + "</html>");

        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
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
                 "Pendiente", "Completada", "Cancelada"
        });
        cmb.setEditable(true);
        if (a.estado != null) cmb.setSelectedItem(a.estado);

        mid.add(new JLabel("Nuevo estado:"));
        mid.add(cmb);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
        CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");

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

    // ============================================================
    // Inner Dialog Class: AddActivityDialog
    // ============================================================
    private static class AddActivityDialog extends JDialog {

        private final ClienteRepository clienteRepo;
        private final PlanningRepository planningRepo;
        private final Runnable onSaved;

        private JTextField txtRuc;
        private JLabel lblRazonSocial;
        private JLabel lblInline;

        private JTextField txtActividad;
        private JTextArea txtDescripcion;

        private JDateChooser dateChooser;
        private JSpinner spHour;
        private JSpinner spMinute;

        private JSpinner spDurHour;
        private JSpinner spDurMin;

        private JComboBox<String> cmbEstado;

        private UUID clienteId;

        AddActivityDialog(Window owner,
                          ClienteRepository clienteRepo,
                          PlanningRepository planningRepo,
                          Runnable onSaved) {

            super(owner, "Agregar actividad", ModalityType.APPLICATION_MODAL);
            this.clienteRepo = clienteRepo;
            this.planningRepo = planningRepo;
            this.onSaved = onSaved;

            setSize(520, 520);
            setLocationRelativeTo(owner);

            buildUI();
        }

        private void buildUI() {
            JPanel root = new JPanel(new BorderLayout(10, 10));
            root.setBorder(new EmptyBorder(12, 12, 12, 12));

            JPanel top = new JPanel(new GridLayout(4, 1, 6, 6));
            top.setOpaque(false);

            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            row1.setOpaque(false);

            txtRuc = new JTextField(16);
            CustomButton btnBuscar = new CustomButton("Buscar", "#4A90E2");

            row1.add(new JLabel("RUC:"));
            row1.add(txtRuc);
            row1.add(btnBuscar);

            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            row2.setOpaque(false);

            lblRazonSocial = new JLabel("-");
            lblRazonSocial.setFont(new Font("Segoe UI", Font.BOLD, 12));
            row2.add(new JLabel("Razón social:"));
            row2.add(lblRazonSocial);

            lblInline = new JLabel(" ");
            lblInline.setForeground(new Color(120, 120, 120));
            lblInline.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            top.add(row1);
            top.add(row2);
            top.add(lblInline);

            JPanel mid = new JPanel(new GridLayout(6, 2, 8, 8));
            mid.setOpaque(false);

            txtActividad = new JTextField();
            txtDescripcion = new JTextArea(4, 20);
            txtDescripcion.setLineWrap(true);
            txtDescripcion.setWrapStyleWord(true);
            JScrollPane spDesc = new JScrollPane(txtDescripcion);

            dateChooser = new JDateChooser();
            dateChooser.setDate(java.sql.Date.valueOf(LocalDate.now()));
            dateChooser.setDateFormatString("dd/MM/yyyy");

            spHour = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
            spMinute = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));

            spDurHour = new JSpinner(new SpinnerNumberModel(1, 0, 12, 1));
            spDurMin = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));

            cmbEstado = new JComboBox<>(new String[]{"Pendiente", "Completado", "Cancelada"});
            cmbEstado.setEditable(true);

            mid.add(new JLabel("Actividad:"));
            mid.add(txtActividad);

            mid.add(new JLabel("Descripción:"));
            mid.add(spDesc);

            mid.add(new JLabel("Fecha:"));
            mid.add(dateChooser);

            JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            timePanel.setOpaque(false);
            timePanel.add(new JLabel("Hora:"));
            timePanel.add(spHour);
            timePanel.add(new JLabel(":"));
            timePanel.add(spMinute);

            mid.add(new JLabel("Hora inicio:"));
            mid.add(timePanel);

            JPanel durPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            durPanel.setOpaque(false);
            durPanel.add(new JLabel("Duración:"));
            durPanel.add(spDurHour);
            durPanel.add(new JLabel("h"));
            durPanel.add(spDurMin);
            durPanel.add(new JLabel("m"));

            mid.add(new JLabel("Duración:"));
            mid.add(durPanel);

            mid.add(new JLabel("Estado:"));
            mid.add(cmbEstado);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.setOpaque(false);

            CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
            CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> onSave());

            buttons.add(btnCancel);
            buttons.add(btnSave);

            btnBuscar.addActionListener(e -> onBuscarCliente());

            root.add(top, BorderLayout.NORTH);
            root.add(mid, BorderLayout.CENTER);
            root.add(buttons, BorderLayout.SOUTH);

            setContentPane(root);
        }

        private void onBuscarCliente() {
            clienteId = null;
            lblRazonSocial.setText("-");

            String ruc = txtRuc.getText() == null ? "" : txtRuc.getText().trim();
            if (ruc.isBlank()) {
                setInline("Ingrese el RUC del cliente.", true);
                return;
            }

            try {
                var c = clienteRepo.findBasicByRuc(ruc);
                if (c == null) {
                    setInline("RUC de cliente no válido", true);
                    return;
                }
                clienteId = c.clienteId;
                lblRazonSocial.setText(c.razonSocial);
                setInline("Cliente encontrado.", false);

            } catch (Exception ex) {
                setInline("Error buscando cliente: " + safeMsg(ex), true);
            }
        }

        private void onSave() {
            if (clienteId == null) {
                setInline("Debe buscar un cliente válido antes de guardar.", true);
                return;
            }

            String act = txtActividad.getText() == null ? "" : txtActividad.getText().trim();
            if (act.isBlank()) {
                setInline("El campo 'Actividad' es obligatorio.", true);
                return;
            }

            java.util.Date date = dateChooser.getDate();
            if (date == null) {
                setInline("Seleccione una fecha.", true);
                return;
            }

            int h = (Integer) spHour.getValue();
            int m = (Integer) spMinute.getValue();

            int dh = (Integer) spDurHour.getValue();
            int dm = (Integer) spDurMin.getValue();
            if (dh == 0 && dm == 0) {
                setInline("La duración no puede ser 0.", true);
                return;
            }

            String estado = String.valueOf(cmbEstado.getSelectedItem()).trim();
            if (estado.isBlank()) {
                setInline("El estado es obligatorio.", true);
                return;
            }

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
            lblInline.setText(msg == null ? " " : msg);
            lblInline.setForeground(err ? new Color(180, 0, 0) : new Color(0, 120, 0));
        }
    }
}
