package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ErrorActionDialog extends JDialog {

    private ErrorActionDialog(Window owner, String title, String message) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Fondo semi-transparente (como overlay)
        JPanel overlay = new JPanel(new GridBagLayout());
        overlay.setBackground(new Color(0, 0, 0, 120));
        setContentPane(overlay);

        // Card redondeada
        RoundedPanel card = new RoundedPanel(22);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(20, 22, 16, 22));
        card.setPreferredSize(new Dimension(420, 170));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(40, 40, 40));

        JLabel lblMsg = new JLabel("<html><div style='width:360px;'>" + esc(message) + "</div></html>");
        lblMsg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblMsg.setForeground(new Color(80, 80, 80));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);

        CustomButton btnOk = new CustomButton("Aceptar", "#9E9E9E");
        btnOk.setPreferredSize(new Dimension(120, 38));
        btnOk.addActionListener(e -> dispose());

        buttons.add(btnOk);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblMsg, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        overlay.add(card);

        setUndecorated(true); // para que se vea como modal bonito
        pack();
        setLocationRelativeTo(owner);
    }

    public static void showError(Window owner, String message) {
        new ErrorActionDialog(owner, "Error", message).setVisible(true);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
