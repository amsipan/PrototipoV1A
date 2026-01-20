package secsys.views.actions;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;

import javax.swing.*;
import java.awt.*;

public class PlanningCardActions extends JPanel {

    public PlanningCardActions() {

        setOpaque(false);
        setLayout(new GridLayout(2, 1, 8, 8));

        CustomButton btnUpload = new CustomButton("Subir planificación", "#4A90E2");
        CustomButton btnView = new CustomButton("Ver planificación", "#5DA9E9");

        btnUpload.addActionListener(e ->
                ViewRouter.show("planning-upload")
        );

        btnView.addActionListener(e ->
                ViewRouter.show("planning-view")
        );

        add(btnUpload);
        add(btnView);
    }
}
