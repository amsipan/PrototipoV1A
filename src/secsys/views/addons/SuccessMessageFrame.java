package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SuccessMessageFrame extends JDialog {

    public SuccessMessageFrame(String message) {
        super((Window) null, "Operación exitosa", ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setAlwaysOnTop(true);

        JPanel content = new JPanel(new BorderLayout(14, 14));
        content.setBorder(new EmptyBorder(18, 18, 18, 18));
        content.setBackground(Color.WHITE);

        // ===== TÍTULO =====
        JLabel lblTitle = new JLabel("Operación exitosa");
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

        // ===== FOOTER =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);

        CustomButton btnOk = new CustomButton("OK", "#4A90E2");
        btnOk.addActionListener(e -> dispose());

        footer.add(btnOk);

        content.add(lblTitle, BorderLayout.NORTH);
        content.add(sp, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        setContentPane(content);

        // Tamaño decente (evita ventanas mini)
        setMinimumSize(new Dimension(520, 240));
        setPreferredSize(new Dimension(520, 260));

        pack();
        setLocationRelativeTo(null);

        // Forzar encima
        toFront();
        requestFocus();
    }
}
