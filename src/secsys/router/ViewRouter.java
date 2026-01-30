package secsys.router;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public final class ViewRouter {

    private static Container root;
    private static CardLayout layout;
    private static final Map<String, JComponent> routes = new HashMap<>();

    private ViewRouter() {}

    public static void init(Container container) {
        root = container;
        layout = new CardLayout();
        root.setLayout(layout);
    }

    public static void register(String name, JComponent view) {
        if (root == null || layout == null) {
            throw new IllegalStateException("ViewRouter no inicializado. Llama ViewRouter.init(container).");
        }

        // ✅ reemplazo seguro
        if (routes.containsKey(name)) {
            JComponent old = routes.get(name);
            root.remove(old);
        }

        routes.put(name, view);
        root.add(view, name);
        root.revalidate();
        root.repaint();
    }

    public static void show(String name) {
        if (!routes.containsKey(name)) {
            throw new IllegalArgumentException("Vista no registrada: " + name);
        }
        layout.show(root, name);
        root.revalidate();
        root.repaint();
    }
}
