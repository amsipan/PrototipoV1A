package secsys.views.audit;

import secsys.views.addons.CustomButton;
import secsys.views.addons.SuccessMessageFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AuditReportDialog extends JDialog {

    public AuditReportDialog(JFrame parent) {

        super(parent, "Generar reporte de auditoría", true);

        setSize(380, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        setResizable(false);

        // ===== CONTENIDO =====
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 25, 10, 25));
        content.setOpaque(true);

        JLabel title = new JLabel("Seleccione los eventos a incluir");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox chkAll = new JCheckBox("Todos");
        JCheckBox chkCreate = new JCheckBox("Creación");
        JCheckBox chkUpdate = new JCheckBox("Modificación");
        JCheckBox chkDelete = new JCheckBox("Eliminación");
        JCheckBox chkRead   = new JCheckBox("Consulta");

        chkAll.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkCreate.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkUpdate.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkDelete.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkRead.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ===== LÓGICA "TODOS" =====
        chkAll.addActionListener(e -> {
            boolean selected = chkAll.isSelected();
            chkCreate.setSelected(selected);
            chkUpdate.setSelected(selected);
            chkDelete.setSelected(selected);
            chkRead.setSelected(selected);
        });

        content.add(title);
        content.add(Box.createVerticalStrut(12));
        content.add(chkAll);
        content.add(Box.createVerticalStrut(5));
        content.add(chkCreate);
        content.add(chkUpdate);
        content.add(chkDelete);
        content.add(chkRead);

        // ===== FOOTER =====
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBorder(new EmptyBorder(10, 20, 15, 20));
        footer.setOpaque(false);

        CustomButton btnGenerate = new CustomButton(
                "Generar reporte",
                "#4A90E2"
        );

        btnGenerate.addActionListener(e -> {
            new SuccessMessageFrame("Reporte Generado Correctamente").setVisible(true);
            dispose();
        });

        footer.add(btnGenerate);

        // ===== ARMADO FINAL =====
        add(content, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
}
