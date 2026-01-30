package secsys.views.addons;

import secsys.router.ViewRouter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SidebarPanel extends JPanel {

    public SidebarPanel(boolean showAudit, boolean showAdmin, boolean showPlatforms) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 12, 15, 12));
        setPreferredSize(new Dimension(220, 0));

        add(makeNavButton("Clientes", () -> ViewRouter.show("clients")));
        add(Box.createVerticalStrut(10));

        add(makeNavButton("Cotizaciones", () -> ViewRouter.show("finance-quote")));
        add(Box.createVerticalStrut(10));

        add(makeNavButton("Planificaciones", () -> ViewRouter.show("plannings")));
        add(Box.createVerticalStrut(10));

        if (showPlatforms) {
            add(makeNavButton("Plataformas", () -> ViewRouter.show("platforms")));
            add(Box.createVerticalStrut(10));
        }

        if (showAdmin) {
            add(makeNavButton("Administración del Sistema", () -> ViewRouter.show("admin")));
            add(Box.createVerticalStrut(10));
        }

        if (showAudit) {
            add(makeNavButton("Auditoría", () -> ViewRouter.show("audit")));
            add(Box.createVerticalStrut(10));
        }
    }

    private JComponent makeNavButton(String text, Runnable action) {
        CustomButton btn = new CustomButton(text, "#4A90E2");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 44));
        btn.addActionListener(e -> action.run());
        return btn;
    }
}
