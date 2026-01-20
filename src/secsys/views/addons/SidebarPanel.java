package secsys.views.addons;

import secsys.router.ViewRouter;

import javax.swing.*;
import java.awt.*;

public class SidebarPanel extends JPanel {

    public SidebarPanel(boolean isAdmin) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(true);
        setPreferredSize(new Dimension(220, 0));
        setOpaque(true);
        setBackground(new Color(35, 45, 60));


        add(createNavButton("Clientes", "clients"));
        add(Box.createVerticalStrut(10));

        add(createNavButton("Cotizaciones", "finance"));
        add(Box.createVerticalStrut(10));

        add(createNavButton("Planificaciones", "plannings"));
        add(Box.createVerticalStrut(20));

        if (isAdmin) {
            add(createNavButton("Administración", "admin"));
            add(Box.createVerticalStrut(10));

            add(createNavButton("Auditoría", "audit"));
            add(Box.createVerticalStrut(10));

            add(createNavButton("Plataformas", "platforms"));
        }
    }

    private CustomButton createNavButton(String text, String route) {
        CustomButton btn = new CustomButton(text, "#5DA9E9");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> ViewRouter.show(route));
        return btn;
    }
}
