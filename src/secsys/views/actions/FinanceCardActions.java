package secsys.views.actions;

import secsys.router.ViewRouter;
import secsys.views.addons.CustomButton;

import javax.swing.*;
import java.awt.*;

public class FinanceCardActions extends JPanel {

    public FinanceCardActions() {

        setOpaque(false);
        setLayout(new GridLayout(2, 1, 8, 8));

        CustomButton btnGenerateQuote = new CustomButton("Generar cotización","#4A90E2");
        CustomButton btnLinkQuote = new CustomButton("Enlazar cotización","#5DA9E9");

        btnGenerateQuote.addActionListener(e ->
                ViewRouter.show("finance-quote")
        );

        btnLinkQuote.addActionListener(e ->
                ViewRouter.show("finance-link")
        );

        add(btnGenerateQuote);
        add(btnLinkQuote);
    }
}
