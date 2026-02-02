package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ActionMessageFrame extends JDialog {

    public ActionMessageFrame(Window owner, String title, String message) {
        super(owner, title, ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setAlwaysOnTop(true);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        root.setBackground(Color.WHITE);

        // ===== TÍTULO =====
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(35, 35, 35));

        // ===== MENSAJE (WRAP REAL) =====
        JTextArea area = new JTextArea(message == null ? "" : message);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setForeground(new Color(60, 60, 60));
        area.setBorder(null);

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // ===== BOTÓN (TU CustomButton) =====
        CustomButton btnOk = new CustomButton("Aceptar", "#4A90E2");
        btnOk.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        bottom.add(btnOk);

        root.add(lblTitle, BorderLayout.NORTH);
        root.add(sp, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);

        // ===== TAMAÑO DECENTE (NO MICRO) =====
        // Puedes ajustar a tu gusto:
        setMinimumSize(new Dimension(520, 240));
        setPreferredSize(new Dimension(520, 260));

        pack();

        // Centrar respecto al owner
        setLocationRelativeTo(owner);

        // Forzar visibilidad encima
        toFront();
        requestFocus();
    }

    public static void showMsg(Component parent, String title, String message) {
        Window owner = (parent == null) ? null : SwingUtilities.getWindowAncestor(parent);
        ActionMessageFrame dlg = new ActionMessageFrame(owner, title, message);
        dlg.setVisible(true);
    }

    public static void showMsg(String title, String message) {
        ActionMessageFrame dlg = new ActionMessageFrame(null, title, message);
        dlg.setVisible(true);
    }
}
