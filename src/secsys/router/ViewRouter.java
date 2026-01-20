package secsys.router;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ViewRouter {

    private static JPanel container;
    private static CardLayout layout;
    private static final Map<String, JPanel> views = new HashMap<>();

    public static void init(Container parent) {
        layout = new CardLayout();
        container = new JPanel(layout);
        parent.add(container, BorderLayout.CENTER);
    }

    public static void register(String name, JPanel panel) {
        views.put(name, panel);
        container.add(panel, name);
    }

    public static void show(String name) {
        layout.show(container, name);
    }
}
