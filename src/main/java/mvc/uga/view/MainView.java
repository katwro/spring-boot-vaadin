package mvc.uga.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.theme.lumo.Lumo;
import mvc.uga.security.SecurityService;

public class MainView extends AppLayout {

    private final SecurityService securityService;

    private static boolean isAdmin;
    private static boolean isManager;

    public MainView(SecurityService securityService) {
        this.securityService = securityService;

        checkRoles();

        addToNavbar(createLeftButtons(), createRightButtons());
    }

    private Component createRightButtons() {
        Button logoutBtn = new Button("Log out", VaadinIcon.SIGN_OUT.create(), e ->
                securityService.logout());
        logoutBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        HorizontalLayout layout = new HorizontalLayout(logoutBtn, createToggleButton());
        layout.setWidth("30%");
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layout.getStyle().set("flex-wrap", "wrap");
        return layout;
    }

    private Component createToggleButton() {
        Button themeToggle = new Button(VaadinIcon.ADJUST.create());
        themeToggle.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        themeToggle.getElement().setAttribute("title", "Change mode")
                .getStyle().set("margin-right", "20px");

        themeToggle.addClickListener(click -> {
            ThemeList themeList = UI.getCurrent().getElement().getThemeList();
            if (themeList.contains(Lumo.DARK)) {
                themeList.remove(Lumo.DARK);
                UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'light');");
                UI.getCurrent().getPage().executeJs("document.documentElement.removeAttribute('theme');");
            } else {
                themeList.add(Lumo.DARK);
                UI.getCurrent().getPage().executeJs("localStorage.setItem('theme', 'dark');");
                UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark');");
            }
        });

        UI.getCurrent().getPage().executeJs(
                "if (localStorage.getItem('theme') === 'dark') {" +
                        "  document.documentElement.setAttribute('theme', 'dark');" +
                        "  $0.classList.add('dark');" +
                        "} else {" +
                        "  document.documentElement.removeAttribute('theme');" +
                        "  $0.classList.remove('dark');" +
                        "}", themeToggle);
        UI.getCurrent().getPage().executeJs(
                "if (localStorage.getItem('theme') === 'dark') {" +
                        "  $0.click();" +
                        "}", themeToggle);

        return themeToggle;
    }

    private Component createLeftButtons() {
        String username = securityService.getAuthenticatedUser().getUsername();
        String usernameUpperCase = username.toUpperCase();

        Button backHomeBtn = new Button(usernameUpperCase, VaadinIcon.HOME.create());
        backHomeBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        backHomeBtn.getElement().setAttribute("title", "Home page");
        backHomeBtn.addClickListener(click ->
                getUI().ifPresent(ui -> ui.navigate("")));

        Button newRequestBtn = new Button("New request");
        newRequestBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        newRequestBtn.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("new-request")));

        Button equipmentListBtn = new Button("Equipment list");
        equipmentListBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        equipmentListBtn.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate("equipment-list")));
        equipmentListBtn.setVisible(isManager);

        Button userListBtn = new Button("User list");
        userListBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        userListBtn.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate("user-list")));
        userListBtn.setVisible(isAdmin);

        Button newUserBtn = new Button("New user");
        newUserBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);
        newUserBtn.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate("new-user")));
        newUserBtn.setVisible(isAdmin);

        HorizontalLayout layout =
                new HorizontalLayout(backHomeBtn, newRequestBtn, equipmentListBtn, userListBtn, newUserBtn);
        layout.setWidthFull();
        layout.getStyle().set("flex-wrap", "wrap");
        layout.getElement().getStyle().set("margin-left", "20px");

        return layout;
    }

    private void checkRoles() {
        isAdmin = securityService.getAuthenticatedUser().getAuthorities().stream().anyMatch(auth ->
                auth.getAuthority().equals("ROLE_ADMIN"));

        isManager = securityService.getAuthenticatedUser().getAuthorities().stream().anyMatch(auth ->
                auth.getAuthority().equals("ROLE_MANAGER"));
    }

}
