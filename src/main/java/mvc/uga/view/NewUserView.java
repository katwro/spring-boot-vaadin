package mvc.uga.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import mvc.uga.constant.AppConstants;
import mvc.uga.entity.Role;
import mvc.uga.entity.User;
import mvc.uga.service.UgaService;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Set;

import static mvc.uga.service.NotificationService.createNotification;

@Route(value = "new-user", layout = MainView.class)
@PageTitle("New User")
@RolesAllowed("ROLE_ADMIN")
public class NewUserView extends VerticalLayout implements HasUrlParameter<Integer> {

    private final UgaService ugaService;

    private User user;
    private Binder<User> binder;
    private List<Role> roleList;

    private TextField username;
    private PasswordField password;
    private PasswordField passwordConfirmation;
    private String originalPassword;
    private TextField firstName;
    private TextField lastName;
    private TextField email;
    private TextField phone;
    private Checkbox enabled;
    private CheckboxGroup<Role> roles;

    private Button changePasswordBtn;
    private Button cancelBtn;
    private Button saveBtn;

    public NewUserView(UgaService ugaService) {
        this.ugaService = ugaService;

        setAlignItems(Alignment.CENTER);

        createVariables();
        createBinder();
        configureRoleCheckboxGroup();

        add(createTitle(), createUserForm(), createButtons());
    }

    private Component createButtons() {
        configureChangePasswordButton();
        configureSaveButton();
        configureCancelButton();

        HorizontalLayout buttonLayout = new HorizontalLayout(saveBtn, cancelBtn, changePasswordBtn);
        buttonLayout.getStyle().set("margin-top", "30px");

        return buttonLayout;
    }

    private void configureChangePasswordButton() {
        changePasswordBtn.addClickListener(e -> {
            password.setVisible(true);
            passwordConfirmation.setVisible(true);
            password.clear();
            changePasswordBtn.setVisible(false);
            binder.forField(password)
                    .withValidator(value -> value != null && !value.isEmpty(), "Please enter new password")
                    .bind(User::getPassword, User::setPassword);
        });
    }

    private void configureSaveButton() {
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.addClickListener(e -> {
            if (binder.validate().isOk()) {
                try {
                    binder.writeBean(user);
                    if (password.isVisible()) {
                        String password = user.getPassword();
                        String confirmation = passwordConfirmation.getValue();
                        if (!password.equals(confirmation)) {
                            createNotification(AppConstants.PASSWORDS_DO_NOT_MATCH, NotificationVariant.LUMO_ERROR);
                            return;
                        }
                    } else {
                        user.setPassword(originalPassword);
                    }
                    ugaService.saveUser(user, originalPassword);
                    createNotification(AppConstants.USER_SAVED_SUCCESSFULLY, NotificationVariant.LUMO_SUCCESS);
                    UI.getCurrent().navigate("user-list");
                } catch (DataIntegrityViolationException exception) {
                    createNotification(AppConstants.USERNAME_EXISTS, NotificationVariant.LUMO_ERROR);
                } catch (Exception exception) {
                    createNotification(AppConstants.ERROR_SAVING, NotificationVariant.LUMO_ERROR);
                }
            } else {
                createNotification(AppConstants.REQUIRED_FIELDS_NOTIFICATION, NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private void configureCancelButton() {
        cancelBtn.addClickListener(e -> getUI().ifPresent(ui -> ui.getPage().getHistory().back()));
    }

    private Component createUserForm() {
        FormLayout userForm = new FormLayout();
        userForm.add(enabled, roles, username, password, passwordConfirmation, firstName, lastName, email, phone);
        userForm.setColspan(username, 2);
        userForm.setWidth("90%");

        HorizontalLayout centeredLayout = new HorizontalLayout(userForm);
        centeredLayout.setJustifyContentMode(JustifyContentMode.CENTER);

        return centeredLayout;
    }

    private Component createTitle() {
        H3 title = new H3("User form");
        title.getElement().getStyle().set("font-weight", "bold");
        return title;
    }

    private void configureRoleCheckboxGroup() {
        roles.setItems(roleList);
        roles.setItemLabelGenerator(Role::getName);
        roles.select(roleList.get(0));

        roles.addSelectionListener(event -> {
            Set<Role> selectedRoles = event.getAllSelectedItems();
            if (selectedRoles.isEmpty()) {
                roles.select(roleList.get(0));
            } else {
                for (Role selectedRole : selectedRoles) {
                    int selectedIndex = roleList.indexOf(selectedRole);
                    for (int i = 0; i < selectedIndex; i++) {
                        roles.select(roleList.get(i));
                    }
                }
            }
        });
    }

    private void createBinder() {
        user = new User();
        binder = new BeanValidationBinder<>(User.class);
        binder.bindInstanceFields(this);
    }

    private void createVariables() {
        username = new TextField("Username");
        username.setPrefixComponent(VaadinIcon.USER.create());
        username.setMaxLength(50);

        email = new TextField("Email");
        email.setPrefixComponent(VaadinIcon.ENVELOPE.create());
        email.setMaxLength(100);
        email.setClearButtonVisible(true);

        phone = new TextField("Phone");
        phone.setPrefixComponent(VaadinIcon.PHONE.create());
        phone.setMaxLength(50);
        phone.setClearButtonVisible(true);

        password = new PasswordField("Password");
        password.setMaxLength(72);
        passwordConfirmation = new PasswordField("Confirm Password");

        firstName = new TextField("First name");
        firstName.setMaxLength(25);

        lastName = new TextField("Last name");
        lastName.setMaxLength(50);

        enabled = new Checkbox("Enabled");

        roles = new CheckboxGroup<>("Roles");
        roleList = ugaService.findAllRoles();

        saveBtn = new Button("Save");
        cancelBtn = new Button("Cancel");
        changePasswordBtn = new Button("Change password");
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter Integer parameter) {
        if (parameter != null) {
            try {
                user = ugaService.findUserById(parameter);
                originalPassword = user.getPassword();
                binder.readBean(user);
                password.setVisible(false);
                passwordConfirmation.setVisible(false);
                changePasswordBtn.setVisible(true);
            } catch (RuntimeException e) {
                beforeEvent.forwardTo(UserListView.class);
            }
        } else {
            user = new User();
            binder.readBean(user);
            enabled.setValue(true);
            originalPassword = null;
            password.setVisible(true);
            passwordConfirmation.setVisible(true);
            changePasswordBtn.setVisible(false);
        }
    }

}
