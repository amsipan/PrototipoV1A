package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class ActionMessageFrame extends JFrame {

    public ActionMessageFrame(String title, String message) {
        super(title == null ? "Mensaje" : title);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(35, 35, 25, 35));

        JLabel lbl = new JLabel(message == null ? "" : message, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        // ✅ SIN color personalizado: queda negro por defecto

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btns.setBackground(Color.WHITE);

        CustomButton btnOk = new CustomButton("Aceptar", "#4A90E2");
        btnOk.setPreferredSize(new Dimension(150, 40));
        btnOk.addActionListener(e -> dispose());

        btns.add(btnOk);

        root.add(lbl, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);

        // Tamaño parecido al de tu captura
        setSize(350, 200);
        setLocationRelativeTo(null);
    }

    public static void showMsg(String title, String message) {
        new ActionMessageFrame(title, message).setVisible(true);
    }
}
