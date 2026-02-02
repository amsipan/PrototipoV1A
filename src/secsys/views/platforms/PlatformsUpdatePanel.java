package secsys.views.platforms;

import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.ConfirmDialogFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.CustomSelectDialog;
import secsys.views.addons.RoundedPanel;
import secsys.views.addons.SuccessMessageFrame;

import secsys.repository.PlatformLicensingRepository;
import secsys.repository.PlatformLicensingRepository.PlatformLicenseRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PlatformsUpdatePanel extends JPanel {

    private Image background;

    // Buscar por coincidencia (LIKE)
    private JTextField txtSearchLike;
    private CustomButton btnBuscar;

    // Autocompletados (NO editables)
    private JTextField txtLicenseName; // nombre del licenciamiento
    private JTextField txtUrl;         // URL (solo Smartfense)

    // Editables según plataforma
    private JTextField txtUsers;       // KnowBe4
    private JTextField txtCostPerUser; // KnowBe4
    private JTextField txtCostAnnual;  // Smartfense

    private CustomButton btnUpdate;
    private CustomButton btnClear;
    private CustomButton btnBack;

    private final PlatformLicensingRepository repo = new PlatformLicensingRepository();

    // Estado cargado
    private UUID loadedLicId;
    private String loadedPlatformCode; // KB4 / SMF

    public PlatformsUpdatePanel() {

        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(900, 560));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel title = new JLabel("Actualizar Licenciamiento");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 18, 0));

        // ===== PANEL BÚSQUEDA =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        searchPanel.setOpaque(false);

        txtSearchLike = new JTextField(26);
        btnBuscar = new CustomButton("Buscar", "#4A90E2");

        searchPanel.add(new JLabel("Buscar licenciamiento:"));
        searchPanel.add(txtSearchLike);
        searchPanel.add(btnBuscar);

        // ===== FORMULARIO =====
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        txtLicenseName = buildReadOnlyField();
        txtUrl = buildReadOnlyField();

        txtUsers = new JTextField(20);
        txtCostPerUser = new JTextField(20);
        txtCostAnnual = new JTextField(20);

        addField(form, c, y++, "Nombre del licenciamiento:", txtLicenseName);
        addField(form, c, y++, "URL (Smartfense):", txtUrl);

        addField(form, c, y++, "Número de usuarios (KnowBe4):", txtUsers);
        addField(form, c, y++, "Costo anual por usuario (KnowBe4):", txtCostPerUser);
        addField(form, c, y++, "Costo anual total (Smartfense):", txtCostAnnual);

        // ===== BOTONES =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        btnBack = new CustomButton("Volver", "#9E9E9E");
        btnUpdate = new CustomButton("Actualizar", "#4A90E2");
        btnClear = new CustomButton("Limpiar", "#9E9E9E");

        buttons.add(btnClear);
        buttons.add(btnBack);
        buttons.add(btnUpdate);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(searchPanel);
        center.add(Box.createVerticalStrut(12));
        center.add(form);

        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        add(card);

        // ===== EVENTOS =====
        btnBuscar.addActionListener(e -> onBuscar());
        btnClear.addActionListener(e -> resetForm());

        btnBack.addActionListener(e -> {
            resetForm();
            ViewRouter.show("platforms");
        });

        btnUpdate.addActionListener(e -> onUpdate());

        // Estado inicial
        setEditEnabled(false, false);
    }

    private void onBuscar() {
        resetLoadedOnly();

        String like = txtSearchLike.getText() == null ? "" : txtSearchLike.getText().trim();
        if (like.isBlank()) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Ingrese el nombre del licenciamiento");
            return;
        }

        try {
            List<PlatformLicenseRow> matches = repo.searchByLicenseNameLike(like);

            if (matches == null || matches.isEmpty()) {
                ActionMessageFrame.showMsg("Plataforma no encontrada", "Plataforma no encontrada");
                return;
            }

            PlatformLicenseRow picked;

            if (matches.size() == 1) {
                picked = matches.get(0);
            } else {
                String[] options = new String[matches.size()];
                for (int i = 0; i < matches.size(); i++) {
                    PlatformLicenseRow r = matches.get(i);
                    boolean usa = isUSA(r.plataformaCodigo);

                    String extra = usa
                            ? ("Usuarios: " + nvlNum(r.numeroUsuarios) + " | $" + safe(r.costoAnualPorUsuario))
                            : ("Costo anual: $" + safe(r.costoAnualTotal));

                    options[i] = platformLabel(r.plataformaCodigo) + "  |  " +
                            nvl(r.nombreLicenciamiento) + "  |  " + extra;
                }

                String pick = CustomSelectDialog.showSelect(
                        SwingUtilities.getWindowAncestor(this),
                        "Seleccionar licenciamiento",
                        "Se encontraron " + matches.size() + " coincidencias.\nSeleccione una:",
                        options
                );

                if (pick == null) return;

                int idx = 0;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(pick)) { idx = i; break; }
                }
                picked = matches.get(idx);
            }

            loadPicked(picked);

        } catch (Exception ex) {
            System.out.println("[PLATFORMS-UPDATE] Error buscando licenciamiento:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo consultar la plataforma.");
        }
    }

    // ✅ AHORA AUTOCOMPLETA TODO según plataforma
    private void loadPicked(PlatformLicenseRow r) {
        if (r == null) return;

        loadedLicId = r.licenciamientoId;
        loadedPlatformCode = r.plataformaCodigo;

        boolean usa = isUSA(loadedPlatformCode);

        // Autocompletados NO editables
        txtLicenseName.setText(nvl(r.nombreLicenciamiento));
        txtUrl.setText(usa ? "-" : nvl(r.url));

        // Habilita lo editable según plataforma
        setEditEnabled(true, usa);

        // ✅ Autocompleta editables con valores actuales (para NO obligar a editar todo)
        if (usa) {
            txtUsers.setText(r.numeroUsuarios == null ? "" : String.valueOf(r.numeroUsuarios));
            txtCostPerUser.setText(r.costoAnualPorUsuario == null ? "" : String.valueOf(r.costoAnualPorUsuario));
            txtCostAnnual.setText(""); // no aplica
        } else {
            txtCostAnnual.setText(r.costoAnualTotal == null ? "" : String.valueOf(r.costoAnualTotal));
            txtUsers.setText("");      // no aplica
            txtCostPerUser.setText(""); // no aplica
        }
    }

    // ✅ Guarda con UN SOLO MÉTODO, usando los valores del form (ya vienen autocompletados)
    private void onUpdate() {
        if (loadedLicId == null) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Primero busque y seleccione un licenciamiento.");
            return;
        }

        boolean usa = isUSA(loadedPlatformCode);

        if (usa) {
            String usersIn = text(txtUsers);
            String costIn = text(txtCostPerUser);

            if (usersIn.isBlank()) {
                ActionMessageFrame.showMsg("Número de usuarios inválido", "Ingrese el número de usuarios");
                return;
            }
            if (!usersIn.matches("\\d{1,4}")) {
                ActionMessageFrame.showMsg("Número de usuarios inválido", "Número de usuarios inválido");
                return;
            }

            int newUsers = Integer.parseInt(usersIn);
            if (newUsers < 0) {
                ActionMessageFrame.showMsg("Número de usuarios inválido", "El número de usuarios no puede ser negativo");
                return;
            }

            if (costIn.isBlank()) {
                ActionMessageFrame.showMsg("Costo anual por usuario inválido", "Ingrese el costo anual por usuario");
                return;
            }

            BigDecimal newCostPerUser = parseMoney(costIn);
            if (newCostPerUser == null) {
                ActionMessageFrame.showMsg("Costo anual por usuario inválido", "Costo anual por usuario inválido");
                return;
            }
            if (newCostPerUser.compareTo(BigDecimal.ZERO) < 0) {
                ActionMessageFrame.showMsg("Costo anual por usuario inválido", "El costo anual por usuario no puede ser negativo");
                return;
            }

            boolean confirm = ConfirmDialogFrame.showConfirm(
                    "Confirmar cambios",
                    "¿Desea actualizar el licenciamiento seleccionado?"
            );
            if (!confirm) return;

            try {
                int updated = repo.updateKnowBe4All(loadedLicId, newUsers, newCostPerUser);

                if (updated <= 0) {
                    ActionMessageFrame.showMsg("Plataforma no encontrada", "Plataforma no encontrada");
                    return;
                }

                new SuccessMessageFrame("Plataforma actualizada exitosamente").setVisible(true);
                resetForm();

            } catch (Exception ex) {
                System.out.println("[PLATFORMS-UPDATE] Error actualizando KnowBe4:");
                ex.printStackTrace();
                ActionMessageFrame.showMsg("Error", "No se pudo actualizar la plataforma.");
            }

        } else {
            String annualIn = text(txtCostAnnual);

            if (annualIn.isBlank()) {
                ActionMessageFrame.showMsg("Costo anual total inválido", "Ingrese el costo anual total");
                return;
            }

            BigDecimal newAnnualTotal = parseMoney(annualIn);
            if (newAnnualTotal == null) {
                ActionMessageFrame.showMsg("Costo anual total inválido", "Costo anual total inválido");
                return;
            }
            if (newAnnualTotal.compareTo(BigDecimal.ZERO) < 0) {
                ActionMessageFrame.showMsg("Costo anual total inválido", "El costo anual total no puede ser negativo");
                return;
            }

            boolean confirm = ConfirmDialogFrame.showConfirm(
                    "Confirmar cambios",
                    "¿Desea actualizar el licenciamiento seleccionado?"
            );
            if (!confirm) return;

            try {
                int updated = repo.updateSmartfenseAll(loadedLicId, newAnnualTotal);

                if (updated <= 0) {
                    ActionMessageFrame.showMsg("Plataforma no encontrada", "Plataforma no encontrada");
                    return;
                }

                new SuccessMessageFrame("Plataforma actualizada exitosamente").setVisible(true);
                resetForm();

            } catch (Exception ex) {
                System.out.println("[PLATFORMS-UPDATE] Error actualizando Smartfense:");
                ex.printStackTrace();
                ActionMessageFrame.showMsg("Error", "No se pudo actualizar la plataforma.");
            }
        }
    }

    private void setEditEnabled(boolean enabled, boolean isUSA) {
        txtLicenseName.setEnabled(false);
        txtUrl.setEnabled(false);

        txtUsers.setEnabled(enabled && isUSA);
        txtCostPerUser.setEnabled(enabled && isUSA);

        txtCostAnnual.setEnabled(enabled && !isUSA);
    }

    private void resetLoadedOnly() {
        loadedLicId = null;
        loadedPlatformCode = null;

        txtLicenseName.setText("");
        txtUrl.setText("");

        txtUsers.setText("");
        txtCostPerUser.setText("");
        txtCostAnnual.setText("");

        setEditEnabled(false, false);
    }

    private void resetForm() {
        txtSearchLike.setText("");
        resetLoadedOnly();
    }

    private static JTextField buildReadOnlyField() {
        JTextField t = new JTextField(22);
        t.setEditable(false);
        t.setEnabled(false);
        t.setDisabledTextColor(new Color(60, 60, 60));
        return t;
    }

    private void addField(JPanel panel, GridBagConstraints c, int y, String label, JComponent field) {
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 0.0;
        panel.add(new JLabel(label), c);

        c.gridx = 1;
        c.weightx = 1.0;
        panel.add(field, c);
    }

    private static boolean isUSA(String plataformaCodigo) {
        return plataformaCodigo != null && plataformaCodigo.trim().equalsIgnoreCase("KB4");
    }

    private static String platformLabel(String plataformaCodigo) {
        if (plataformaCodigo == null) return "-";
        String c = plataformaCodigo.trim().toUpperCase();
        if ("KB4".equals(c)) return "KnowBe4 (USA)";
        if ("SMF".equals(c)) return "SMARTFENSE (LATAM)";
        return c;
    }

    private static String text(JTextField t) {
        return t.getText() == null ? "" : t.getText().trim();
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String nvlNum(Integer n) {
        return (n == null) ? "-" : String.valueOf(n);
    }

    private static String safe(Object v) {
        return v == null ? "-" : String.valueOf(v);
    }

    private static BigDecimal parseMoney(String raw) {
        if (raw == null) return null;
        String v = raw.trim().replace(",", ".");
        if (!v.matches("^\\d{1,3}(\\.\\d{1,2})?$")) return null;
        try {
            return new BigDecimal(v);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
