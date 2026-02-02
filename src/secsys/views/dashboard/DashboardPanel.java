package secsys.views.dashboard;

import com.toedter.calendar.JDateChooser;
import secsys.dto.CalendarActivityDTO;
import secsys.repository.ClienteRepository;
import secsys.repository.PlanningRepository;
import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.SuccessMessageFrame;
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

    // ==========================================================
    // ✅ rplan22v1.1: Recordatorios automáticos (solo Pendiente)
    // - Evaluación periódica de fechas
    // - Semana calendario en curso
    // - Popup único con # actividades
    // ==========================================================
    private Timer reminderTimer;
    private LocalDate remindedWeekStart;     // semana para la cual ya se mostró recordatorio
    private boolean reminderShownThisWeek;   // único recordatorio por semana

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
        btnSalir.addActionListener(e -> { ViewRouter.show("login"); });

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

        // ✅ rplan22: inicia evaluación periódica (popup único por semana)
        startReminderScheduler();
    }

    private void refreshCalendar() {
        calendarGrid.removeAll();

        LocalDate weekEnd = weekStart.plusDays(4);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblWeekRange.setText(df.format(weekStart) + " - " + df.format(weekEnd));

        calendarGrid.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(4, 4, 4, 4);

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

        try {
            ZoneId zone = ZoneId.systemDefault();
            OffsetDateTime from = weekStart.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime to = weekStart.plusDays(5).atStartOfDay(zone).toOffsetDateTime();

            List<CalendarActivityDTO> acts = planningRepo.findCalendarActivitiesBetween(from, to);

            for (CalendarActivityDTO a : acts) {
                if (a == null || a.fechaInicio == null) continue;

                LocalDate day = a.fechaInicio.atZoneSameInstant(zone).toLocalDate();
                int dow = day.getDayOfWeek().getValue();
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
                openActivityMenuDialog(a);
            }
        });

        return chip;
    }

    // ==========================================================
    // ✅ MENÚ VERTICAL (CustomButton) + restricciones por estado
    // ==========================================================
    private void openActivityMenuDialog(CalendarActivityDTO a) {
        if (a == null || a.actividadId == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Acciones de actividad", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(520, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel t = new JLabel("Acciones de actividad");
        t.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel s = new JLabel("Seleccione una acción para: " + nvl(a.actividad));
        s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        s.setForeground(new Color(110, 110, 110));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(t);
        top.add(Box.createVerticalStrut(4));
        top.add(s);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));

        CustomButton bEstado = new CustomButton("Cambiar estado (Completada/Cancelada)", "#4A90E2");
        CustomButton bNombre = new CustomButton("Editar nombre", "#4A90E2");
        CustomButton bDesc = new CustomButton("Editar descripción", "#4A90E2");
        CustomButton bFechas = new CustomButton("Editar fechas/horas (solo Pendiente)", "#4A90E2");

        bEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        bNombre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        bDesc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        bFechas.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // ✅ Si ya está Completada/Cancelada, NO permitir modificar nombre/desc/fechas
        boolean bloqueada = isFinalState(a.estado);
        bNombre.setEnabled(!bloqueada);
        bDesc.setEnabled(!bloqueada);
        bFechas.setEnabled(!bloqueada);

        menu.add(bEstado);
        menu.add(Box.createVerticalStrut(10));
        menu.add(bNombre);
        menu.add(Box.createVerticalStrut(10));
        menu.add(bDesc);
        menu.add(Box.createVerticalStrut(10));
        menu.add(bFechas);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);

        CustomButton btnClose = new CustomButton("Cerrar", "#9E9E9E");
        btnClose.setPreferredSize(new Dimension(140, 40));
        btnClose.addActionListener(e -> dlg.dispose());
        bottom.add(btnClose);

        bEstado.addActionListener(e -> { dlg.dispose(); openChangeEstadoDialogRestricted(a); });

        bNombre.addActionListener(e -> {
            dlg.dispose();
            if (isFinalState(a.estado)) { showError("No se permite modificar la actividad"); return; }
            openEditNombreDialog(a);
        });

        bDesc.addActionListener(e -> {
            dlg.dispose();
            if (isFinalState(a.estado)) { showError("No se permite modificar la actividad"); return; }
            openEditDescripcionDialog(a);
        });

        bFechas.addActionListener(e -> {
            dlg.dispose();
            if (isFinalState(a.estado)) { showError("No se permite modificar la actividad"); return; }
            openEditFechasDialogPendingOnly(a);
        });

        card.add(top, BorderLayout.NORTH);
        card.add(menu, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // rplan9: solo Completada/Cancelada
    private void openChangeEstadoDialogRestricted(CalendarActivityDTO a) {
        if (a == null || a.actividadId == null) return;

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Cambiar estado", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Cambiar estado");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel sub = new JLabel("Actividad: " + nvl(a.actividad));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 110, 110));

        JComboBox<String> cmb = new JComboBox<>(new String[]{"Completada", "Cancelada"});

        JPanel center = new JPanel(new GridLayout(2, 1, 8, 8));
        center.setOpaque(false);
        center.add(new JLabel("Nuevo estado:"));
        center.add(cmb);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
        btnCancel.setPreferredSize(new Dimension(140, 40));

        CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");
        btnSave.setPreferredSize(new Dimension(160, 40));

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String nuevo = String.valueOf(cmb.getSelectedItem()).trim();
            if (!equalsAnyIgnoreCase(nuevo, "Completada", "Cancelada")) {
                showError("Datos de actividad inválidos");
                return;
            }

            try {
                // ✅ usa tu método del repo
                planningRepo.updateActivityEstado(a.actividadId, nuevo);
                showOk("Actividad actualizada correctamente");
                dlg.dispose();
                refreshCalendar();

                // ✅ rplan22: re-evaluar recordatorio (por si cambió a cancelada/completada)
                evaluatePendingReminderForCurrentWeek();

            } catch (Exception ex) {
                showError("No se pudo actualizar el estado de la actividad");
            }
        });

        buttons.add(btnCancel);
        buttons.add(btnSave);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // rplan6: editar nombre (bloqueado si Completada/Cancelada)
    private void openEditNombreDialog(CalendarActivityDTO a) {
        if (a == null || a.actividadId == null) return;
        if (isFinalState(a.estado)) { showError("No se permite modificar la actividad"); return; }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Editar nombre", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(560, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Editar nombre");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel sub = new JLabel("Actividad actual: " + nvl(a.actividad));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 110, 110));

        JTextField txt = new JTextField(a.actividad == null ? "" : a.actividad.trim());

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(new JLabel("Nuevo nombre:"), BorderLayout.NORTH);
        center.add(txt, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
        btnCancel.setPreferredSize(new Dimension(140, 40));

        CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");
        btnSave.setPreferredSize(new Dimension(160, 40));

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String nuevo = txt.getText() == null ? "" : txt.getText().trim();

            if (!isValidAlphaAmerican(nuevo)) { showError("Nombre de actividad inválido"); return; }
            if (nuevo.length() < 3) { showError("El nombre de actividad debe tener al menos 3 caracteres"); return; }
            if (nuevo.length() > 100) { showError("Datos de actividad inválidos"); return; }

            try {
                planningRepo.updateActivityNombre(a.actividadId, nuevo);
                showOk("Actividad actualizada correctamente");
                dlg.dispose();
                refreshCalendar();
            } catch (Exception ex) {
                showError("No se pudo actualizar el nombre de la actividad");
            }
        });

        buttons.add(btnCancel);
        buttons.add(btnSave);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // rplan7: editar descripción (bloqueado si Completada/Cancelada)
    private void openEditDescripcionDialog(CalendarActivityDTO a) {
        if (a == null || a.actividadId == null) return;
        if (isFinalState(a.estado)) { showError("No se permite modificar la actividad"); return; }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Editar descripción", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(620, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(new Color(245, 247, 250));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Editar descripción");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JLabel sub = new JLabel("Actividad: " + nvl(a.actividad));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(110, 110, 110));

        JTextArea area = new JTextArea(6, 28);
        area.setText(a.descripcion == null ? "" : a.descripcion);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(new JLabel("Nueva descripción:"), BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
        btnCancel.setPreferredSize(new Dimension(140, 40));

        CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");
        btnSave.setPreferredSize(new Dimension(160, 40));

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String nueva = area.getText() == null ? "" : area.getText().trim();

            if (!isValidAlphaAmerican(nueva)) { showError("Descripción de actividad inválida."); return; }
            if (nueva.length() < 3) { showError("La descripción de actividad debe tener al menos 3 caracteres"); return; }
            if (nueva.length() > 250) { showError("Datos de actividad inválidos"); return; }

            try {
                planningRepo.updateActivityDescripcion(a.actividadId, nueva);
                showOk("Actividad actualizada correctamente");
                dlg.dispose();
                refreshCalendar();
            } catch (Exception ex) {
                showError("No se pudo actualizar la descripción la actividad");
            }
        });

        buttons.add(btnCancel);
        buttons.add(btnSave);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(title);
        top.add(Box.createVerticalStrut(4));
        top.add(sub);

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        root.add(card, BorderLayout.CENTER);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // rplan10: fechas/horas solo Pendiente (y bloqueado si Completada/Cancelada)
    private void openEditFechasDialogPendingOnly(CalendarActivityDTO a) {
        if (a == null || a.actividadId == null) return;

        String estadoActual = (a.estado == null) ? "" : a.estado.trim();

        if (isFinalState(estadoActual)) {
            showError("No se permite modificar la actividad");
            return;
        }

        if (!estadoActual.equalsIgnoreCase("Pendiente")) {
            showError("No se permite modificar la actividad");
            return;
        }

        EditFechasDialog dlg = new EditFechasDialog(
                SwingUtilities.getWindowAncestor(this),
                a,
                (ini, fin) -> {
                    try {
                        planningRepo.updateActivityFechas(a.actividadId, ini, fin);
                        showOk("Fechas actualizadas exitosamente");
                        refreshCalendar();
                    } catch (Exception ex) {
                        showError("No se pudo actualizar la actividad");
                    }
                }
        );
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

    private static String safeMsg(Throwable t) {
        if (t == null) return "";
        String m = t.getMessage();
        if (m == null || m.isBlank()) m = t.getClass().getSimpleName();
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

    private static boolean isValidAlphaAmerican(String s) {
        if (s == null) return false;
        String t = s.trim();
        if (t.isEmpty()) return false;
        return t.matches("^[A-Za-z ]+$");
    }

    private static boolean equalsAnyIgnoreCase(String value, String... allowed) {
        if (value == null) return false;
        for (String a : allowed) {
            if (a != null && value.trim().equalsIgnoreCase(a.trim())) return true;
        }
        return false;
    }

    private static boolean isFinalState(String estado) {
        if (estado == null) return false;
        String e = estado.trim();
        return e.equalsIgnoreCase("Completada")
                || e.equalsIgnoreCase("Completado")
                || e.equalsIgnoreCase("Cancelada");
    }

    // ==========================
    // ✅ Mensajes con ActionMessageFrame / SuccessMessageFrame
    // ==========================
    private void showError(String msg) {
        try {
            new ActionMessageFrame(null, "Error", msg).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showOk(String msg) {
        try {
            new SuccessMessageFrame(msg).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // ==========================================================
    // ✅ rplan22v1.1 IMPLEMENTACIÓN
    // ==========================================================
    private void startReminderScheduler() {
        remindedWeekStart = mondayOf(LocalDate.now());
        reminderShownThisWeek = false;

        // evaluación periódica (cada 2 min). Puedes cambiar a 1 min si quieres.
        reminderTimer = new Timer(2 * 60 * 1000, e -> evaluatePendingReminderForCurrentWeek());
        reminderTimer.setInitialDelay(5 * 1000); // a los 5s de abrir el panel
        reminderTimer.start();

        // primera evaluación inmediata (sin esperar 2 min)
        SwingUtilities.invokeLater(this::evaluatePendingReminderForCurrentWeek);
    }

    private void evaluatePendingReminderForCurrentWeek() {
        try {
            LocalDate now = LocalDate.now();
            LocalDate currentWeekStart = mondayOf(now);

            // si cambió la semana, reiniciar "único recordatorio"
            if (remindedWeekStart == null || !remindedWeekStart.equals(currentWeekStart)) {
                remindedWeekStart = currentWeekStart;
                reminderShownThisWeek = false;
            }

            if (reminderShownThisWeek) return;

            ZoneId zone = ZoneId.systemDefault();

            // Semana en curso (Lun–Vie) coherente con tu calendario
            OffsetDateTime from = currentWeekStart.atStartOfDay(zone).toOffsetDateTime();
            OffsetDateTime to = currentWeekStart.plusDays(7).atStartOfDay(zone).toOffsetDateTime();

            List<CalendarActivityDTO> acts = planningRepo.findCalendarActivitiesBetween(from, to);

            int pendingCount = 0;
            if (acts != null) {
                for (CalendarActivityDTO a : acts) {
                    if (a == null) continue;
                    if (a.estado != null && a.estado.trim().equalsIgnoreCase("Pendiente")) pendingCount++;
                }
            }

            if (pendingCount > 0) {
                // ✅ ÚNICO popup con número de actividades pendientes
                String fechaActual = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String msg = "Fecha actual: " + fechaActual + "\n" +
                        "Tiene " + pendingCount + " actividades pendientes esta semana.";

                new ActionMessageFrame(null, "Recordatorio", msg).setVisible(true);
                reminderShownThisWeek = true;
            }

        } catch (Exception ex) {
            // No mostramos error para no molestar con popups de fallo en la evaluación periódica.
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        try {
            if (reminderTimer != null) reminderTimer.stop();
        } catch (Exception ignored) {}
    }

    // ====================================
    // Dialog editar fechas/horas (rplan10)
    // ====================================
    private static class EditFechasDialog extends JDialog {
        interface OnSave {
            void save(OffsetDateTime ini, OffsetDateTime fin);
        }

        private final CalendarActivityDTO current;
        private final OnSave onSave;

        private JDateChooser dcIni;
        private JDateChooser dcFin;
        private JSpinner spIniH, spIniM;
        private JSpinner spFinH, spFinM;

        EditFechasDialog(Window owner, CalendarActivityDTO current, OnSave onSave) {
            super(owner, "Editar fechas/horas", ModalityType.APPLICATION_MODAL);
            this.current = current;
            this.onSave = onSave;

            setSize(620, 360);
            setLocationRelativeTo(owner);
            setResizable(false);

            buildUI();
        }

        private void buildUI() {
            JPanel root = new JPanel(new BorderLayout(10, 10));
            root.setBackground(new Color(245, 247, 250));
            root.setBorder(new EmptyBorder(14, 14, 14, 14));

            RoundedPanel card = new RoundedPanel(20);
            card.setBackground(Color.WHITE);
            card.setLayout(new GridBagLayout());
            card.setBorder(new EmptyBorder(16, 16, 16, 16));

            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(8, 8, 8, 8);
            c.fill = GridBagConstraints.HORIZONTAL;

            JLabel title = new JLabel("Editar fechas/horas (solo Pendiente)");
            title.setFont(new Font("Segoe UI", Font.BOLD, 18));

            c.gridx = 0; c.gridy = 0; c.gridwidth = 4; c.weightx = 1.0;
            card.add(title, c);
            c.gridwidth = 1;

            dcIni = new JDateChooser();
            dcIni.setDateFormatString("dd/MM/yyyy");

            dcFin = new JDateChooser();
            dcFin.setDateFormatString("dd/MM/yyyy");

            ZoneId zone = ZoneId.systemDefault();

            LocalDate iniDate = current.fechaInicio == null ? LocalDate.now() : current.fechaInicio.atZoneSameInstant(zone).toLocalDate();
            LocalDate finDate = current.fechaFin == null ? iniDate : current.fechaFin.atZoneSameInstant(zone).toLocalDate();
            dcIni.setDate(java.sql.Date.valueOf(iniDate));
            dcFin.setDate(java.sql.Date.valueOf(finDate));

            LocalTime ti = current.fechaInicio == null ? LocalTime.of(9, 0) : current.fechaInicio.atZoneSameInstant(zone).toLocalTime();
            LocalTime tf = current.fechaFin == null ? LocalTime.of(10, 0) : current.fechaFin.atZoneSameInstant(zone).toLocalTime();

            spIniH = new JSpinner(new SpinnerNumberModel(ti.getHour(), 0, 23, 1));
            spIniM = new JSpinner(new SpinnerNumberModel(ti.getMinute(), 0, 59, 1));
            spFinH = new JSpinner(new SpinnerNumberModel(tf.getHour(), 0, 23, 1));
            spFinM = new JSpinner(new SpinnerNumberModel(tf.getMinute(), 0, 59, 1));

            c.gridx = 0; c.gridy = 1; c.weightx = 0.2;
            card.add(new JLabel("Fecha inicio:"), c);
            c.gridx = 1; c.gridy = 1; c.weightx = 0.8;
            card.add(dcIni, c);

            c.gridx = 2; c.gridy = 1; c.weightx = 0.2;
            card.add(new JLabel("Hora inicio:"), c);
            c.gridx = 3; c.gridy = 1; c.weightx = 0.8;
            card.add(timePanel(spIniH, spIniM), c);

            c.gridx = 0; c.gridy = 2; c.weightx = 0.2;
            card.add(new JLabel("Fecha fin:"), c);
            c.gridx = 1; c.gridy = 2; c.weightx = 0.8;
            card.add(dcFin, c);

            c.gridx = 2; c.gridy = 2; c.weightx = 0.2;
            card.add(new JLabel("Hora fin:"), c);
            c.gridx = 3; c.gridy = 2; c.weightx = 0.8;
            card.add(timePanel(spFinH, spFinM), c);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttons.setOpaque(false);

            CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
            btnCancel.setPreferredSize(new Dimension(140, 40));

            CustomButton btnSave = new CustomButton("Guardar", "#4A90E2");
            btnSave.setPreferredSize(new Dimension(160, 40));

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> onSaveClick());

            buttons.add(btnCancel);
            buttons.add(btnSave);

            root.add(card, BorderLayout.CENTER);
            root.add(buttons, BorderLayout.SOUTH);

            setContentPane(root);
        }

        private JPanel timePanel(JSpinner h, JSpinner m) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            p.setOpaque(false);
            p.add(h);
            p.add(new JLabel(":"));
            p.add(m);
            return p;
        }

        private void onSaveClick() {
            java.util.Date dIni = dcIni.getDate();
            java.util.Date dFin = dcFin.getDate();

            if (dIni == null || dFin == null) {
                JOptionPane.showMessageDialog(this, "Fechas en formato inválido", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int hi = (Integer) spIniH.getValue();
            int mi = (Integer) spIniM.getValue();
            int hf = (Integer) spFinH.getValue();
            int mf = (Integer) spFinM.getValue();

            ZoneId zone = ZoneId.systemDefault();
            LocalDate iniDate = Instant.ofEpochMilli(dIni.getTime()).atZone(zone).toLocalDate();
            LocalDate finDate = Instant.ofEpochMilli(dFin.getTime()).atZone(zone).toLocalDate();

            if (finDate.isBefore(iniDate)) {
                JOptionPane.showMessageDialog(this, "La fecha de fin no puede ser menor a la fecha de inicio", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDateTime ini = iniDate.atTime(hi, mi);
            LocalDateTime fin = finDate.atTime(hf, mf);

            if (!fin.isAfter(ini)) {
                if (finDate.equals(iniDate)) {
                    JOptionPane.showMessageDialog(this, "La hora de fin no puede ser menor a la hora de inicio", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Datos de actividad inválidos", "Error", JOptionPane.ERROR_MESSAGE);
                }
                return;
            }

            OffsetDateTime iniODT = ini.atZone(zone).toOffsetDateTime();
            OffsetDateTime finODT = fin.atZone(zone).toOffsetDateTime();

            if (onSave != null) onSave.save(iniODT, finODT);
            dispose();
        }
    }

    // =========================
    // ✅ AddActivityDialog con SCROLL + SOLO ActionMessageFrame/SuccessMessageFrame
    // =========================
    private static class AddActivityDialog extends JDialog {
        private final ClienteRepository clienteRepo;
        private final PlanningRepository planningRepo;
        private final Runnable onSaved;

        private JTextField txtRazonSocial;
        private CustomButton btnBuscar;
        private JLabel lblClienteSeleccionado;

        private JTextField txtActividad;
        private JTextArea txtDescripcion;

        private JDateChooser dcIni;
        private JDateChooser dcFin;
        private JSpinner spIniH, spIniM;
        private JSpinner spFinH, spFinM;

        private JComboBox<String> cmbEstado;

        private UUID clienteId;

        AddActivityDialog(Window owner, ClienteRepository clienteRepo, PlanningRepository planningRepo, Runnable onSaved) {
            super(owner, "Agregar actividad", ModalityType.APPLICATION_MODAL);
            this.clienteRepo = clienteRepo;
            this.planningRepo = planningRepo;
            this.onSaved = onSaved;

            setSize(860, 600);
            setLocationRelativeTo(owner);
            setResizable(false);

            buildUI();
        }

        private void buildUI() {
            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(245, 247, 250));
            root.setBorder(new EmptyBorder(14, 14, 14, 14));

            RoundedPanel card = new RoundedPanel(22);
            card.setBackground(Color.WHITE);
            card.setLayout(new BorderLayout(12, 12));
            card.setBorder(new EmptyBorder(18, 18, 18, 18));

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

            sc.gridx = 0; sc.gridy = 0; sc.weightx = 0.0;
            searchWrap.add(lblRS, sc);

            sc.gridx = 1; sc.gridy = 0; sc.weightx = 1.0;
            searchWrap.add(txtRazonSocial, sc);

            sc.gridx = 2; sc.gridy = 0; sc.weightx = 0.0;
            searchWrap.add(btnBuscar, sc);

            sc.gridx = 0; sc.gridy = 1; sc.gridwidth = 3; sc.weightx = 1.0;
            searchWrap.add(lblClienteSeleccionado, sc);

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
            spDesc.setPreferredSize(new Dimension(10, 140));

            dcIni = new JDateChooser();
            dcIni.setDateFormatString("dd/MM/yyyy");
            dcIni.setDate(java.sql.Date.valueOf(LocalDate.now()));

            dcFin = new JDateChooser();
            dcFin.setDateFormatString("dd/MM/yyyy");
            dcFin.setDate(java.sql.Date.valueOf(LocalDate.now()));

            spIniH = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
            spIniM = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
            JPanel timeIni = timePanel(spIniH, spIniM);

            spFinH = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
            spFinM = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
            JPanel timeFin = timePanel(spFinH, spFinM);

            cmbEstado = new JComboBox<>(new String[]{"Pendiente"});
            cmbEstado.setEnabled(false);

            int row = 0;
            row = addRow(form, gc, row, "Actividad (3-100):", txtActividad);
            row = addRow(form, gc, row, "Descripción (3-250):", spDesc);
            row = addRow(form, gc, row, "Fecha inicio:", dcIni);
            row = addRow(form, gc, row, "Hora inicio:", timeIni);
            row = addRow(form, gc, row, "Fecha fin:", dcFin);
            row = addRow(form, gc, row, "Hora fin:", timeFin);
            row = addRow(form, gc, row, "Estado (defecto Pendiente):", cmbEstado);

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.add(searchWrap);
            content.add(form);
            content.add(Box.createVerticalStrut(10));

            JScrollPane sp = new JScrollPane(content);
            sp.setBorder(null);
            sp.getViewport().setOpaque(false);
            sp.setOpaque(false);
            sp.getVerticalScrollBar().setUnitIncrement(16);

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

            card.add(header, BorderLayout.NORTH);
            card.add(sp, BorderLayout.CENTER);
            card.add(buttons, BorderLayout.SOUTH);

            root.add(card, BorderLayout.CENTER);
            setContentPane(root);
        }

        private JPanel timePanel(JSpinner h, JSpinner m) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            p.setOpaque(false);
            p.add(h);
            p.add(new JLabel(":"));
            p.add(m);
            return p;
        }

        private void onBuscarClientePorRazonSocial() {
            clienteId = null;
            lblClienteSeleccionado.setText("Cliente seleccionado: -");

            String razon = txtRazonSocial.getText() == null ? "" : txtRazonSocial.getText().trim();
            if (razon.isBlank()) {
                showError("Datos de actividad inválidos");
                return;
            }

            try {
                List<secsys.dto.ClienteBasicDTO> matches = clienteRepo.findBasicByRazonSocialLikeIgnoreCase(razon);

                if (matches == null || matches.isEmpty()) {
                    showError("Datos de actividad inválidos");
                    return;
                }

                secsys.dto.ClienteBasicDTO selected;
                if (matches.size() == 1) selected = matches.get(0);
                else selected = showClientePicker(matches);

                if (selected == null) {
                    showError("Datos de actividad inválidos");
                    return;
                }

                clienteId = selected.clienteId;
                lblClienteSeleccionado.setText("Cliente seleccionado: " + nvl(selected.razonSocial));

            } catch (Exception ex) {
                showError("Datos de actividad inválidos");
            }
        }

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

            btnCancel.addActionListener(e -> { selected[0] = null; dlg.dispose(); });
            btnOk.addActionListener(e -> { selected[0] = list.getSelectedValue(); dlg.dispose(); });

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
            if (clienteId == null) { showError("Datos de actividad inválidos"); return; }

            
String act = txtActividad.getText() == null ? "" : txtActividad.getText().trim();
if (!isValidTextUnicode(act, 3, 100)) { showError("Nombre de actividad inválido"); return; }
            if (act.length() < 3) { showError("El nombre de actividad debe tener al menos 3 caracteres"); return; }
            if (act.length() > 100) { showError("Datos de actividad inválidos"); return; }

            String desc = txtDescripcion.getText() == null ? "" : txtDescripcion.getText().trim();
if (!isValidTextUnicode(desc, 3, 250)) { showError("Descripción de actividad inválida."); return; }
            if (desc.length() < 3) { showError("La descripción de actividad debe tener al menos 3 caracteres"); return; }
            if (desc.length() > 250) { showError("Datos de actividad inválidos"); return; }

            java.util.Date di = dcIni.getDate();
            java.util.Date df = dcFin.getDate();
            if (di == null || df == null) { showError("Fechas en formato inválido"); return; }

            int hi = (Integer) spIniH.getValue();
            int mi = (Integer) spIniM.getValue();
            int hf = (Integer) spFinH.getValue();
            int mf = (Integer) spFinM.getValue();

            ZoneId zone = ZoneId.systemDefault();
            LocalDate iniDate = Instant.ofEpochMilli(di.getTime()).atZone(zone).toLocalDate();
            LocalDate finDate = Instant.ofEpochMilli(df.getTime()).atZone(zone).toLocalDate();

            LocalDateTime ini = iniDate.atTime(hi, mi);
            LocalDateTime fin = finDate.atTime(hf, mf);

            if (!fin.isAfter(ini)) {
                if (finDate.isBefore(iniDate)) showError("La fecha de fin no puede ser inferior a la fecha de inicio");
                else if (finDate.equals(iniDate)) showError("La hora de fin no puede ser inferior a la hora de inicio");
                else showError("Datos de actividad inválidos");
                return;
            }

            OffsetDateTime iniODT = ini.atZone(zone).toOffsetDateTime();
            OffsetDateTime finODT = fin.atZone(zone).toOffsetDateTime();

            try {
                planningRepo.insertManualActivityToActivePlan(clienteId, iniODT, finODT, act, desc, "Pendiente");
                new SuccessMessageFrame("Actividad registrada exitosamente").setVisible(true);

                if (onSaved != null) onSaved.run();
                dispose();
            } catch (Exception ex) {
                showError("Datos de actividad inválidos");
            }
        }

        private void showError(String msg) {
            try { new ActionMessageFrame(null, "Error", msg).setVisible(true); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
        }

        private static String nvl(String s) {
            return (s == null || s.isBlank()) ? "-" : s;
        }
    }

    // Pega estos helpers dentro de AddActivityDialog (por ejemplo, al final de la clase)

private static boolean isValidTextUnicode(String s, int min, int max) {
    if (s == null) return false;
    String t = s.trim();
    if (t.length() < min || t.length() > max) return false;

    // \p{L} = letras (incluye áéíóúñ, etc.)
    // \p{M} = marcas combinantes (tildes separadas)
    // \p{N} = números
    // Permitimos espacios y signos comunes: . , # - ( ) / : ; + & y saltos de línea NO (por ser JTextArea, si quieres permitirlo te doy la versión)
    return t.matches("^[\\p{L}\\p{M}\\p{N} .,#\\-()/:;+&]{"+min+","+max+"}$");
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
}
