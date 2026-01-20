package secsys;

import javax.swing.SwingUtilities;

import secsys.config.AppContext;
import secsys.views.planning.RepoFactory;

public class App {
    public static void main(String[] args) {
        AppContext ctx = new AppContext();
        System.out.println(ctx.pingService());

        RepoFactory.init(ctx.db());

        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });

        System.out.println("Sirve");
    }
}
