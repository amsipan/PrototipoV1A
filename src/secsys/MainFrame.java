package secsys;

import secsys.router.ViewRouter;
import secsys.views.*;
import secsys.views.admin.AdminConsultUserPanel;
import secsys.views.admin.AdminCreateUserPanel;
import secsys.views.admin.AdminDeleteUserPanel;
import secsys.views.admin.AdminModifyUserPanel;
import secsys.views.admin.AdminPanel;
import secsys.views.audit.AuditPanel;
import secsys.views.clients.ClientInformationPanel;
import secsys.views.clients.ClientRegisterPanel;
import secsys.views.clients.ClientUpdatePanel;
import secsys.views.clients.ClientsActionsView;
import secsys.views.dashboard.DashboardPanel;
import secsys.views.finance.QuotationCreatePanel;
import secsys.views.finance.QuotationLinkPanel;
import secsys.views.planning.PlanningActionsView;
import secsys.views.planning.PlanningUploadPanel;
import secsys.views.planning.PlanningViewPanel;
import secsys.views.platforms.PlatformsConsultPanel;
import secsys.views.platforms.PlatformsPanel;
import secsys.views.platforms.PlatformsRegisterPanel;
import secsys.views.platforms.PlatformsUpdatePanel;

import javax.swing.*;

public class MainFrame extends JFrame {

    private static final boolean SHOW_AUDIT = false;

    public MainFrame() {
        setTitle("SECSYS - Prototipo UI");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        ViewRouter.init(getContentPane());
        

        ViewRouter.register("login", new LoginPanel());
        ViewRouter.register("dashboard", new DashboardPanel(SHOW_AUDIT));
        ViewRouter.register("clients", new ClientsActionsView());
        ViewRouter.register("clients-register", new ClientRegisterPanel());
        ViewRouter.register("clients-consult", new ClientInformationPanel());
        ViewRouter.register("clients-update", new ClientUpdatePanel());
        ViewRouter.register("finance-quote", new QuotationCreatePanel());
        ViewRouter.register("finance-link", new QuotationLinkPanel());
        ViewRouter.register("plannings", new PlanningActionsView());
        ViewRouter.register("planning-upload", new PlanningUploadPanel());
        ViewRouter.register("planning-view", new PlanningViewPanel());
        ViewRouter.register("admin", new AdminPanel());
        ViewRouter.register("admin-create", new AdminCreateUserPanel());
        ViewRouter.register("admin-modify", new AdminModifyUserPanel());
        ViewRouter.register("admin-delete", new AdminDeleteUserPanel());
        ViewRouter.register("admin-consult", new AdminConsultUserPanel());
        ViewRouter.register("audit", new AuditPanel());
        ViewRouter.register("platforms", new PlatformsPanel());
        ViewRouter.register("platforms-register", new PlatformsRegisterPanel());
        ViewRouter.register("platforms-consult", new PlatformsConsultPanel());
        ViewRouter.register("platforms-update", new PlatformsUpdatePanel());

        ViewRouter.show("login");
    }
}
