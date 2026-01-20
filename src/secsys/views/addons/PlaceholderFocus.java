package secsys.views.addons;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlaceholderFocus extends FocusAdapter {

    private final JTextComponent field;
    private final String placeholder;

    public PlaceholderFocus(JTextComponent field, String placeholder) {
        this.field = field;
        this.placeholder = placeholder;
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (field.getText().equals(placeholder)) {
            field.setText("");
            field.setForeground(Color.BLACK);
            if (field instanceof JPasswordField) {
                ((JPasswordField) field).setEchoChar('•');
            }
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (field.getText().isEmpty()) {
            field.setText(placeholder);
            field.setForeground(Color.GRAY);
            if (field instanceof JPasswordField) {
                ((JPasswordField) field).setEchoChar((char) 0);
            }
        }
    }
}
