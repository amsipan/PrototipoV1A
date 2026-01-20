package secsys.views.actions;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;

import javax.swing.*;
import java.awt.*;

public class AuditAdminCardActions extends JPanel {

    public AuditAdminCardActions() {

        setOpaque(false);
        setLayout(new GridLayout(3, 1, 8, 8));

        CustomButton btnAudit = new CustomButton("Auditoría", "#4A90E2");
        CustomButton btnAdmin = new CustomButton("Administración", "#5DA9E9");
        CustomButton btnPlatforms = new CustomButton("Plataformas", "#6C63FF");

        btnAudit.addActionListener(e ->
                ViewRouter.show("audit")
        );

        btnAdmin.addActionListener(e ->
                ViewRouter.show("admin")
        );

        btnPlatforms.addActionListener(e ->
                ViewRouter.show("platforms")
        );

        add(btnAudit);
        add(btnAdmin);
        add(btnPlatforms);
    }
}
