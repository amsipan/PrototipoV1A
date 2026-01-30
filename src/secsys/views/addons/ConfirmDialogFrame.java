package secsys.views.addons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public final class ConfirmDialogFrame extends JDialog {

    private boolean confirmed = false;

    private ConfirmDialogFrame(Window owner, String title, String message) {
        super(owner, title == null ? "Confirmación" : title, ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(35, 35, 25, 35));

        // ✅ Texto con wrap (no se corta)
        JTextArea txt = new JTextArea(message == null ? "" : message);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setForeground(Color.BLACK);
        txt.setBackground(Color.WHITE);
        txt.setEditable(false);
        txt.setFocusable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setMargin(new Insets(0, 0, 0, 0));

        // Para que quede centrado visualmente:
        JPanel msgWrap = new JPanel(new GridBagLayout());
        msgWrap.setBackground(Color.WHITE);
        msgWrap.add(txt);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.setBackground(Color.WHITE);

        CustomButton btnNo = new CustomButton("No", "#9E9E9E");
        btnNo.setPreferredSize(new Dimension(140, 40));
        btnNo.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        CustomButton btnYes = new CustomButton("Sí", "#E53935");
        btnYes.setPreferredSize(new Dimension(140, 40));
        btnYes.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        btns.add(btnNo);
        btns.add(btnYes);

        root.add(msgWrap, BorderLayout.CENTER);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);

        // ✅ Tamaño suficiente para texto + botones (y que no se corte)
        setSize(520, 230);
        setLocationRelativeTo(owner);
    }

    public static boolean showConfirm(String title, String message) {
        Window owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        ConfirmDialogFrame dialog = new ConfirmDialogFrame(owner, title, message);
        dialog.setVisible(true); // modal -> aquí se bloquea hasta cerrar
        return dialog.confirmed;
    }
}
