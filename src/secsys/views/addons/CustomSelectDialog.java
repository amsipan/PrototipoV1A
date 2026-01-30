package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class CustomSelectDialog extends JDialog {

    private String selectedValue = null;

    private final JLabel lblMessage;
    private final JComboBox<String> combo;

    public CustomSelectDialog(Window owner, String title, String message, String[] options) {
        super(owner, title, ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // ===== Root =====
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(new Color(245, 247, 250)); // fondo suave como tu app
        root.setBorder(new EmptyBorder(18, 18, 18, 18));

        // ===== Card =====
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(12, 12));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setPreferredSize(new Dimension(520, 220));

        // ===== Header =====
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(40, 40, 40));

        lblMessage = new JLabel(toHtml(message));
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblMessage.setForeground(new Color(90, 90, 90));
        lblMessage.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(lblTitle);
        header.add(lblMessage);

        // ===== Center =====
        combo = new JComboBox<>(options == null ? new String[]{} : options);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setMaximumRowCount(8);
        combo.setPreferredSize(new Dimension(420, 36));

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 0, 0, 0);
        center.add(combo, gc);

        // ===== Buttons =====
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        CustomButton btnCancel = new CustomButton("Cancelar", "#9E9E9E");
        CustomButton btnOk = new CustomButton("Seleccionar", "#4A90E2");

        btnCancel.setPreferredSize(new Dimension(140, 40));
        btnOk.setPreferredSize(new Dimension(160, 40));

        btnCancel.addActionListener(e -> {
            selectedValue = null;
            dispose();
        });

        btnOk.addActionListener(e -> {
            Object sel = combo.getSelectedItem();
            selectedValue = (sel == null) ? null : String.valueOf(sel);
            dispose();
        });

        buttons.add(btnCancel);
        buttons.add(btnOk);

        // ===== Assemble card =====
        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        root.add(card);

        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);

        // ENTER = aceptar, ESC = cancelar
        getRootPane().setDefaultButton(btnOk);
        installEscToClose();
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public void setSelectedIndex(int idx) {
        if (idx >= 0 && idx < combo.getItemCount()) combo.setSelectedIndex(idx);
    }

    private void installEscToClose() {
        JRootPane rp = getRootPane();
        rp.registerKeyboardAction(
                e -> {
                    selectedValue = null;
                    dispose();
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private static String toHtml(String s) {
        if (s == null) return "<html></html>";
        String esc = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html>" + esc.replace("\n", "<br>") + "</html>";
    }

    // ===== Helper estático para usarlo como JOptionPane =====
    public static String showSelect(Window owner, String title, String message, String[] options) {
        CustomSelectDialog dlg = new CustomSelectDialog(owner, title, message, options);
        dlg.setSelectedIndex(0);
        dlg.setVisible(true);
        return dlg.getSelectedValue();
    }
}
