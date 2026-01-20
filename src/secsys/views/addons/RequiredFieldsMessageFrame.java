package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RequiredFieldsMessageFrame extends JFrame {

    public RequiredFieldsMessageFrame() {
        this("Existen campos obligatorios vacíos.\nPor favor complete toda la información.");
    }

    public RequiredFieldsMessageFrame(String message) {

        setTitle("Campos obligatorios");
        setSize(380, 190);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(25, 25, 20, 25));

        JLabel lblMessage = new JLabel(
                "<html><center>" + message.replace("\n", "<br>") + "</center></html>",
                SwingConstants.CENTER
        );
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // ===== FOOTER =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 0, 0, 0));

        CustomButton btnOk = new CustomButton("Aceptar", "#4A90E2");
        btnOk.setPreferredSize(new Dimension(140, 42));
        btnOk.setMaximumSize(new Dimension(140, 42));

        btnOk.addActionListener(e -> dispose());

        footer.add(btnOk);

        content.add(lblMessage, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        add(content);
    }
}
