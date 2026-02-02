package secsys.views.platforms;

import secsys.router.ViewRouter;
import secsys.views.addons.ActionMessageFrame;
import secsys.views.addons.CustomButton;
import secsys.views.addons.CustomSelectDialog;
import secsys.views.addons.InfoCard;
import secsys.views.addons.RoundedPanel;

import secsys.repository.PlatformLicensingRepository;
import secsys.repository.PlatformLicensingRepository.PlatformLicenseRow;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PlatformsConsultPanel extends JPanel {

    private Image background;

    private JTextField txtLicenseSearch;
    private JPanel resultsPanel;

    private final PlatformLicensingRepository repo = new PlatformLicensingRepository();

    public PlatformsConsultPanel() {

        // ===== IMAGEN DE FONDO =====
        background = new ImageIcon("src\\secsys\\resources\\imagenFondo.png").getImage();

        setLayout(new GridBagLayout());
        setOpaque(false);

        // ===== CARD PRINCIPAL =====
        RoundedPanel card = new RoundedPanel(25);
        card.setPreferredSize(new Dimension(860, 540));
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ===== TÍTULO =====
        JLabel title = new JLabel("Consultar Licenciamiento");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));

        // ===== PANEL DE BÚSQUEDA =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        searchPanel.setOpaque(false);

        txtLicenseSearch = new JTextField(22);

        CustomButton btnSearch = new CustomButton("Consultar", "#4A90E2");
        CustomButton btnClear = new CustomButton("Limpiar", "#9E9E9E");

        searchPanel.add(new JLabel("Nombre del licenciamiento:"));
        searchPanel.add(txtLicenseSearch);
        searchPanel.add(btnSearch);
        searchPanel.add(btnClear);

        // ===== RESULTADOS =====
        resultsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        resultsPanel.setOpaque(false);

        RoundedPanel resultsContainer = new RoundedPanel(18);
        resultsContainer.setLayout(new BorderLayout());
        resultsContainer.setBackground(new Color(245, 247, 250));
        resultsContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        resultsContainer.setPreferredSize(new Dimension(800, 300));
        resultsContainer.add(resultsPanel, BorderLayout.CENTER);

        // ===== BOTONES =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);

        CustomButton btnBack = new CustomButton("Volver", "#9E9E9E");
        btnBack.addActionListener(e -> {
            resetView();
            ViewRouter.show("platforms");
        });

        footer.add(btnBack);

        // ===== CONTENEDOR CENTRAL =====
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        center.add(searchPanel);
        center.add(Box.createVerticalStrut(15));
        center.add(resultsContainer);

        // ===== ARMADO FINAL =====
        card.add(title, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        add(card);

        // ===== ACCIONES =====
        btnSearch.addActionListener(e -> onSearch());
        btnClear.addActionListener(e -> resetView());
    }

    private void onSearch() {
        clearResults();

        String licenseIn = txtLicenseSearch.getText() == null ? "" : txtLicenseSearch.getText().trim();

        if (licenseIn.isBlank()) {
            ActionMessageFrame.showMsg("Campos obligatorios", "Ingrese el nombre del licenciamiento.");
            return;
        }

        try {
            // LIKE / coincidencia
            List<PlatformLicenseRow> matches = repo.searchByLicenseNameLike(licenseIn);

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
                            ? ("Usuarios: " + nvlNum(r.numeroUsuarios))
                            : ("Costo anual: " + money(r.costoAnualTotal));

                    options[i] =
                            platformLabel(r.plataformaCodigo) + "  |  " +
                            nvl(r.nombreLicenciamiento) + "  |  " +
                            extra;
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

            renderResult(picked);

        } catch (Exception ex) {
            System.out.println("[PLATFORMS-CONSULT] Error consultando licenciamiento:");
            ex.printStackTrace();
            ActionMessageFrame.showMsg("Error", "No se pudo consultar la plataforma.");
        }
    }

    private void renderResult(PlatformLicenseRow r) {
        clearResults();

        boolean usa = isUSA(r.plataformaCodigo);

        resultsPanel.add(new InfoCard("Plataforma", platformLabel(r.plataformaCodigo)));
        resultsPanel.add(new InfoCard("Nombre de licenciamiento", nvl(r.nombreLicenciamiento)));

        if (usa) {
            resultsPanel.add(new InfoCard("Número de usuarios", nvlNum(r.numeroUsuarios)));
            resultsPanel.add(new InfoCard("Costo anual por usuario", money(r.costoAnualPorUsuario)));
        } else {
            resultsPanel.add(new InfoCard("Costo anual total", money(r.costoAnualTotal)));
            resultsPanel.add(new InfoCard("URL", nvl(r.url)));
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void clearResults() {
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private void resetView() {
        txtLicenseSearch.setText("");
        clearResults();
    }

    private static boolean isUSA(String plataformaCodigo) {
        return plataformaCodigo != null && plataformaCodigo.trim().equalsIgnoreCase("KB4");
    }

    private static String platformLabel(String plataformaCodigo) {
        if (plataformaCodigo == null) return "-";
        String c = plataformaCodigo.trim().toUpperCase();
        if (c.equals("KB4")) return "KnowBe4 (USA)";
        if (c.equals("SMF")) return "SMARTFENSE (LATAM)";
        return c;
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String nvlNum(Integer n) {
        return (n == null) ? "-" : String.valueOf(n);
    }

    private static String money(Object v) {
        if (v == null) return "-";
        return "$" + String.valueOf(v);
    }

    // ===== DIBUJO DEL FONDO =====
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
    }
}
