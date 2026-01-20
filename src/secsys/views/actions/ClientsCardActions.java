package secsys.views.actions;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;

import javax.swing.*;
import java.awt.*;

public class ClientsCardActions extends JPanel {

    public ClientsCardActions() {
        setOpaque(false);
        setLayout(new GridLayout(2, 1, 8, 8));

        CustomButton btnRegister = new CustomButton("Registrar cliente","#4A90E2");
        CustomButton btnConsult = new CustomButton("Consultar clientes", "#5DA9E9");

        btnRegister.addActionListener(e ->
                ViewRouter.show("clients-register")
        );

        btnConsult.addActionListener(e ->
                ViewRouter.show("clients-consult")
        );

        add(btnRegister);
        add(btnConsult);
    }
}
