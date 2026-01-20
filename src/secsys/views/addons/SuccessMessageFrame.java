package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SuccessMessageFrame extends JFrame {

    public SuccessMessageFrame(String message) {

        setTitle("Operación exitosa");
        setSize(360, 180);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setBorder(new EmptyBorder(25, 25, 20, 25));
        content.setBackground(Color.WHITE);

        JLabel lblMessage = new JLabel(message, SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 0, 0, 0));

        CustomButton btnOk = new CustomButton("OK", "#4A90E2");
        btnOk.addActionListener(e -> dispose());

        footer.add(btnOk);

        content.add(lblMessage, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        add(content);
    }
}
