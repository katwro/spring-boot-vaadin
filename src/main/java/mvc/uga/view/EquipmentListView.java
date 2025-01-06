package mvc.uga.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import mvc.uga.constant.AppConstants;
import mvc.uga.entity.Equipment;
import mvc.uga.entity.User;
import mvc.uga.service.UgaService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static mvc.uga.service.NotificationService.createNotification;

@Route(value = "equipment-list", layout = MainView.class)
@PageTitle("Equipment list")
@RolesAllowed("ROLE_MANAGER")
public class EquipmentListView extends VerticalLayout
        implements SelectionListener<Grid<Equipment>, Equipment> {

    private final UgaService ugaService;

    private Grid<Equipment> equipmentGrid;
    private Set<Equipment> selected;
    private TextField searchEquipment;
    private ListDataProvider<Equipment> dataProvider;

    private Dialog newEqDialog;
    private Equipment equipment;
    private Binder<Equipment> binder;

    private TextField name;
    private TextField serialNumber;
    private TextField inventoryNumber;

    private Button removeBtn;
    private Button backBtn;
    private Button addEquipmentBtn;
    private Button saveBtn;
    private Button cancelBtn;

    private Notification currentNotification;

    public EquipmentListView(UgaService ugaService) {
        this.ugaService = ugaService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);

        createVariables();
        loadEquipment();

        add(createTitle(), createControlPanel(), createEquipmentGrid());
    }

    private Component createEquipmentGrid() {
        equipmentGrid.setColumns("id", "name", "serialNumber", "inventoryNumber");
        equipmentGrid.addColumn(this::getUsername).setHeader("Username");
        equipmentGrid.addColumn(createAssignUserColumn()).setHeader("Assign User");

        equipmentGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        equipmentGrid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);
        equipmentGrid.getColumns().forEach(col -> col.setAutoWidth(true));
        equipmentGrid.setDataProvider(dataProvider);

        return equipmentGrid;
    }

    private String getUsername(Equipment equipment) {
        User user = equipment.getUser();
        return user != null ? user.getUsername() : "-";
    }

    private Renderer<Equipment> createAssignUserColumn() {
        return new ComponentRenderer<>(equipment -> {
            if (equipment.getUser() == null) {
                return createAssignButton(equipment);
            } else {
                return createUnassignButton(equipment);
            }
        });
    }

    private Button createUnassignButton(Equipment equipment) {
        Button unassignButton = new Button(VaadinIcon.MINUS.create(), clickEvent -> {
            Dialog confirmDialog = createUnassignConfirmDialog(equipment);
            confirmDialog.open();
        });
        unassignButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        unassignButton.getElement().setAttribute("title", "Unassign user");
        return unassignButton;
    }

    private Dialog createUnassignConfirmDialog(Equipment equipment) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.add(new Text("Are you sure you want to unassign the user from this equipment?"));

        Button confirmButton = new Button("Yes", event -> {
            equipment.setUser(null);
            ugaService.saveEquipment(equipment);
            equipmentGrid.getDataProvider().refreshItem(equipment);
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("No", event -> confirmDialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        confirmDialog.add(buttonsLayout);

        return confirmDialog;
    }

    private Button createAssignButton(Equipment equipment) {
        Button assignButton = new Button(VaadinIcon.PLUS.create(),
                clickEvent -> openAssignDialog(equipment));
        assignButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        assignButton.getElement().setAttribute("title", "Assign user");
        return assignButton;
    }

    private void openAssignDialog(Equipment equipment) {
        Dialog dialog = new Dialog();
        ListDataProvider<User> userDataProvider = new ListDataProvider<>(ugaService.findAllUsers());

        TextField searchUser = new TextField();
        searchUser.setPlaceholder("Search");
        searchUser.setClearButtonVisible(true);
        searchUser.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchUser.addValueChangeListener(event -> filterUsers(event.getValue(), userDataProvider));

        Grid<User> userGrid = createUserGrid(equipment, dialog, userDataProvider);

        dialog.setHeight("70%");
        dialog.setWidth("70%");
        dialog.add(searchUser, userGrid);
        dialog.open();
    }

    private Grid<User> createUserGrid(Equipment equipment, Dialog dialog, ListDataProvider<User> userDataProvider) {
        Grid<User> userGrid = new Grid<>(User.class);
        userGrid.setColumns("id", "username", "firstName", "lastName");
        userGrid.setItems(ugaService.findAllUsers());
        userGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        userGrid.getColumns().forEach(col -> col.setAutoWidth(true));

        userGrid.asSingleSelect().addValueChangeListener(event -> {
            User selectedUser = event.getValue();
            assignUserToEquipment(equipment, selectedUser, dialog);
        });

        userGrid.setDataProvider(userDataProvider);
        return userGrid;
    }

    private void assignUserToEquipment(Equipment equipment, User selectedUser, Dialog dialog) {
        equipment.setUser(selectedUser);
        ugaService.saveEquipment(equipment);
        equipmentGrid.getDataProvider().refreshItem(equipment);
        dialog.close();
    }

    private void filterUsers(String searchTerm, ListDataProvider<User> userDataProvider) {
        List<User> filteredUsers = ugaService.searchUser(searchTerm);
        userDataProvider.getItems().clear();
        userDataProvider.getItems().addAll(filteredUsers);
        userDataProvider.refreshAll();
    }

    private Component createControlPanel() {
        configureSearchEquipmentField();
        createButtons();

        HorizontalLayout layout = new HorizontalLayout(searchEquipment, backBtn, removeBtn, addEquipmentBtn);
        layout.setWidthFull();

        return layout;
    }

    private void createButtons() {
        removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        backBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_CONTRAST);
        addEquipmentBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        removeBtn.addClickListener(e -> removeSelected());
        backBtn.addClickListener(e -> closeView());
        addEquipmentBtn.addClickListener(e -> openNewEquipmentDialog());
    }

    private void openNewEquipmentDialog() {
        newEqDialog.removeAll();
        newEqDialog.setHeaderTitle("New equipment");
        createBinder();
        binder.readBean(new Equipment());

        VerticalLayout layout = new VerticalLayout();
        layout.add(name, serialNumber, inventoryNumber, createNewEquipmentDialogButtons());

        newEqDialog.add(layout);
        newEqDialog.open();
    }

    private Component createNewEquipmentDialogButtons() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        saveBtn.addClickListener(e -> saveEquipment());
        cancelBtn.addClickListener(e -> newEqDialog.close());

        return new HorizontalLayout(saveBtn, cancelBtn);
    }

    private void saveEquipment() {
        try {
            binder.writeBean(equipment);
            ugaService.saveEquipment(equipment);
            showNotification(AppConstants.EQUIPMENT_SAVED_SUCCESSFULLY, NotificationVariant.LUMO_SUCCESS);
            newEqDialog.close();
            searchEquipment.clear();
            updateEquipmentGrid();
        } catch (ValidationException exception) {
            showNotification(AppConstants.REQUIRED_FIELDS_NOTIFICATION, NotificationVariant.LUMO_ERROR);
        } catch (Exception exception) {
            showNotification(AppConstants.ERROR_SAVING, NotificationVariant.LUMO_ERROR);
        }
    }

    private void showNotification(String message, NotificationVariant variant) {
        if (currentNotification != null) {
            currentNotification.close();
        }
        currentNotification = createNotification(message, variant);
    }

    private void createBinder() {
        equipment = new Equipment();
        binder = new BeanValidationBinder<>(Equipment.class);
        binder.bindInstanceFields(this);
    }

    private void configureSearchEquipmentField() {
        searchEquipment = new TextField();
        searchEquipment.setPlaceholder("Search");
        searchEquipment.setClearButtonVisible(true);
        searchEquipment.setPrefixComponent(VaadinIcon.SEARCH.create());

        searchEquipment.addValueChangeListener(event -> {
            String searchTerm = event.getValue();
            List<Equipment> filteredEquipment = ugaService.searchEquipment(searchTerm);

            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(filteredEquipment);
            dataProvider.refreshAll();
        });
    }

    private void closeView() {
        getUI().ifPresent(ui -> ui.getPage().getHistory().back());
    }

    private void removeSelected() {
        if (selected == null) {
            selected = Collections.emptySet();
        }
        if (selected.isEmpty()) {
            createNotification("No equipment selected.", NotificationVariant.LUMO_PRIMARY);
            return;
        }

        Dialog confirmDialog = createConfirmDialog();
        confirmDialog.open();
    }

    private Dialog createConfirmDialog() {
        Dialog confirmDialog = new Dialog();
        confirmDialog.add(new Text("Are you sure you want to remove selected equipment?"));

        Button confirmButton = new Button("Yes", event -> {
            selected.forEach(ugaService::deleteEquipment);
            createNotification(AppConstants.EQUIPMENT_REMOVED_SUCCESSFULLY, NotificationVariant.LUMO_SUCCESS);
            updateEquipmentGrid();
            searchEquipment.clear();
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("No", event -> confirmDialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(confirmButton, cancelButton);
        confirmDialog.add(buttonsLayout);

        return confirmDialog;
    }

    private void updateEquipmentGrid() {
        List<Equipment> equipmentList = ugaService.findAllEquipment();
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(equipmentList);
        dataProvider.refreshAll();
        equipmentGrid.getSelectionModel().deselectAll();
    }

    private Component createTitle() {
        H3 title = new H3("Equipment list");
        title.getElement().getStyle().set("font-weight", "bold");

        return title;
    }

    private void loadEquipment() {
        List<Equipment> equipmentList = ugaService.findAllEquipment();
        dataProvider = new ListDataProvider<>(equipmentList);
        equipmentGrid.setDataProvider(dataProvider);
        equipmentGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        equipmentGrid.addSelectionListener(this);
    }

    private void createVariables() {
        equipmentGrid = new Grid<>(Equipment.class);

        name = new TextField("Name");
        name.setMaxLength(100);

        serialNumber = new TextField("Serial number");
        serialNumber.setMaxLength(30);

        inventoryNumber = new TextField("Inventory number");
        inventoryNumber.setMaxLength(30);

        removeBtn = new Button("Remove");
        backBtn = new Button("Back");
        addEquipmentBtn = new Button("New equipment");

        newEqDialog = new Dialog("New equipment");
        saveBtn = new Button("Save");
        cancelBtn = new Button("Cancel");
    }

    @Override
    public void selectionChange(SelectionEvent<Grid<Equipment>, Equipment> selectionEvent) {
        selected = selectionEvent.getAllSelectedItems();
    }

}
