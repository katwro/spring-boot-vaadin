package mvc.uga.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import mvc.uga.constant.AppConstants;
import mvc.uga.entity.Equipment;
import mvc.uga.entity.User;
import mvc.uga.security.SecurityService;
import mvc.uga.service.UgaService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static mvc.uga.service.NotificationService.createNotification;

@Route(value = "user-list", layout = MainView.class)
@PageTitle("User list")
@RolesAllowed("ROLE_ADMIN")
public class UserListView extends VerticalLayout
        implements SelectionListener<Grid<User>, User> {

    private final UgaService ugaService;
    private final SecurityService securityService;

    private Grid<User> userGrid;
    private Set<User> selected;
    private TextField searchUser;
    private ListDataProvider<User> dataProvider;

    private Button removeBtn;
    private Button backBtn;

    public UserListView(UgaService ugaService, SecurityService securityService) {
        this.ugaService = ugaService;
        this.securityService = securityService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        createVariables();
        loadUsers();

        add(createTitle(), createControlPanel(), createUserGrid());
    }

    private Component createUserGrid() {
        userGrid.setColumns("id", "username", "firstName", "lastName", "email", "phone");
        userGrid.addComponentColumn(this::createEnabledIcon).setHeader("Enabled");
        userGrid.addComponentColumn(this::createEditButton).setHeader("Edit");
        userGrid.setDataProvider(dataProvider);

        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        userGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        userGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        return  userGrid;
    }

    private Button createEditButton(User u) {
        Button edit = new Button(VaadinIcon.EDIT.create());
        edit.addClickListener(e -> UI.getCurrent().navigate("new-user/" + u.getId()));

        return edit;
    }

    private Icon createEnabledIcon(User u) {
        Icon icon;
        if (u.isEnabled()) {
            icon = VaadinIcon.CHECK_CIRCLE.create();
            icon.setColor("green");
        } else {
            icon = VaadinIcon.CLOSE_CIRCLE.create();
            icon.setColor("red");
        }
        return icon;
    }

    private Component createControlPanel() {
        configureSearchField();
        createButtons();

        HorizontalLayout layout = new HorizontalLayout(searchUser, backBtn, removeBtn);
        layout.setWidthFull();

        return layout;
    }

    private void createButtons() {
        removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        removeBtn.addClickListener(e -> removeSelected());

        backBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        backBtn.addClickListener(e -> closeView());
    }

    private void closeView() {
        getUI().ifPresent(ui -> ui.getPage().getHistory().back());
    }

    private void removeSelected() {
        if (selected == null) {
            selected = Collections.emptySet();
        }
        if (selected.isEmpty()) {
            createNotification("No users selected.", NotificationVariant.LUMO_CONTRAST);
            return;
        }
        String authenticatedUsername = securityService.getAuthenticatedUser().getUsername();
        if (selected.stream().anyMatch(user -> user.getUsername().equals(authenticatedUsername))) {
            createNotification("You cannot remove yourself.", NotificationVariant.LUMO_ERROR);
            return;
        }

        Dialog confirmDialog = createConfirmDialog();
        confirmDialog.open();
    }

    private Dialog createConfirmDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.add(new Text("Are you sure you want to remove selected user(s)?"));

        Button confirmButton = new Button("Yes", event -> {
            selected.forEach(user -> {
                List<Equipment> userEquipment = ugaService.findAllEquipmentByUsername(user.getUsername());
                userEquipment.forEach(equipment -> {
                    equipment.setUser(null);
                    ugaService.saveEquipment(equipment);
                });
                ugaService.deleteUser(user);
            });
            createNotification(AppConstants.USER_REMOVED_SUCCESSFULLY, NotificationVariant.LUMO_SUCCESS);
            updateUserGrid();
            searchUser.clear();
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("No", event -> confirmDialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        confirmDialog.add(buttonsLayout);

        return confirmDialog;
    }

    private void updateUserGrid() {
        List<User> users = ugaService.findAllUsers();
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(users);
        dataProvider.refreshAll();
        userGrid.getSelectionModel().deselectAll();
    }

    private void configureSearchField() {
        searchUser = new TextField();
        searchUser.setHelperText("username/first name/last name");
        searchUser.setPlaceholder("Search");
        searchUser.setClearButtonVisible(true);
        searchUser.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchUser.addValueChangeListener(event -> {
            String searchTerm = event.getValue();
            List<User> filteredUser = ugaService.searchUser(searchTerm);

            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(filteredUser);
            dataProvider.refreshAll();
        });
    }

    private Component createTitle() {
        H3 title = new H3("User list");
        title.getElement().getStyle().set("font-weight", "bold");
        return title;
    }

    private void loadUsers() {
        List<User> users = ugaService.findAllUsers();
        dataProvider = new ListDataProvider<>(users);
        userGrid.setDataProvider(dataProvider);
        userGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        userGrid.addSelectionListener(this);
    }

    private void createVariables() {
        userGrid = new Grid<>(User.class);
        removeBtn = new Button("Remove");
        backBtn = new Button("Back");
    }

    @Override
    public void selectionChange(SelectionEvent<Grid<User>, User> selectionEvent) {
        selected = selectionEvent.getAllSelectedItems();
    }

}
